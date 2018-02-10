package com.otokarev.stellarctl

import com.otokarev.stellarctl.Stellar.Order

import scalaj.http._
import org.stellar.sdk._
import org.stellar.sdk.requests.RequestBuilder.{Order => SdkOrder}
import org.stellar.sdk.responses._

object Stellar {
  case class GenerateKeyPairResult(
                                    account: String,
                                    accountSecret: String
                                  )

  object Order extends Enumeration {
    type Order = Value
    val desc: Stellar.Order.Value = Value("desc")
    val asc: Stellar.Order.Value = Value("asc")
  }
}

class Stellar()(implicit context: Context) {
  private val server = new Server(context.horizonUrl)

  case class ManageOfferStellarOperation(
                                          offerId: Option[String] = None,
                                          sellingAssetType: Option[String] = Some("notNative"),
                                          sellingAssetCode: Option[String] = None,
                                          sellingAssetIssuer: Option[String] = None,
                                          buyingAssetType: Option[String] = Some("notNative"),
                                          buyingAssetCode: Option[String] = None,
                                          buyingAssetIssuer: Option[String] = None,
                                          price: String,
                                          amount: String,
                                        )

  case class PaymentStellarOperation(
                               destinationAccount: String,
                               amount: String,
                               assetType: Option[String] = Some("notNative"),
                               assetCode: Option[String] = None,
                               assetIssuer: Option[String] = None,
                             )

  case class ChangeTrustStellarOperation(
                                          assetType: Option[String] = Some("notNative"),
                                          assetCode: Option[String] = None,
                                          assetIssuer: Option[String] = None,
                                          limit: String
                                        )

  case class CreateAccountStellarOperation(
                                          accountSecret: String,
                                          startingBalance: String
                                        )


  object OperationAdapter {
    import annotation.implicitNotFound

    @implicitNotFound("No member of type class StellarOperationBuilder in scope for ${T}")
    trait Builder[T] {
      def build(operation: T): Operation
    }

    object Builder {
      implicit object PaymentOperationBuilder extends Builder[PaymentStellarOperation] {
        def build(operation: PaymentStellarOperation): Operation = {
          val destinationKeyPair: KeyPair = KeyPair.fromAccountId(operation.destinationAccount)

          /* Check that destination exists */
          server.accounts().account(destinationKeyPair)

          val assetOption = operation.assetType map {
            case "native" => new AssetTypeNative()
            case _ => Asset.createNonNativeAsset(operation.assetCode.get, KeyPair.fromAccountId(operation.assetIssuer.get))
          }

          new PaymentOperation.Builder(destinationKeyPair, assetOption.get, operation.amount).build()
        }
      }

      implicit object ChangeTrustOperationBuilder extends Builder[ChangeTrustStellarOperation] {
        def build(operation: ChangeTrustStellarOperation): Operation = {
          val assetOption = operation.assetType map {
            case "native" => new AssetTypeNative()
            case _ => Asset.createNonNativeAsset(operation.assetCode.get, KeyPair.fromAccountId(operation.assetIssuer.get))
          }

          new ChangeTrustOperation.Builder(assetOption.get, operation.limit).build()
        }
      }

      implicit object ManageOfferOperationBuilder extends Builder[ManageOfferStellarOperation] {
        def build(operation: ManageOfferStellarOperation): Operation = {
          val sellingAssetOption = operation.sellingAssetType map {
            case "native" => new AssetTypeNative()
            case _ => Asset.createNonNativeAsset(operation.sellingAssetCode.get, KeyPair.fromAccountId(operation.sellingAssetIssuer.get))
          }
          val buyingAssetOption = operation.buyingAssetType map {
            case "native" => new AssetTypeNative()
            case _ => Asset.createNonNativeAsset(operation.buyingAssetCode.get, KeyPair.fromAccountId(operation.buyingAssetIssuer.get))
          }

          val builder = new ManageOfferOperation.Builder(sellingAssetOption.get, buyingAssetOption.get, operation.amount, operation.price)

          operation.offerId.foreach(id => builder.setOfferId(id.toLong))

          builder.build()
        }
      }

      implicit object CreateAccountOperationBuilder extends Builder[CreateAccountStellarOperation] {
        def build(operation: CreateAccountStellarOperation): Operation = {
          new CreateAccountOperation.Builder(KeyPair.fromSecretSeed(operation.accountSecret), operation.startingBalance).build()
        }
      }
    }
  }


  object TransactionAdapter {
    import OperationAdapter.Builder


    def process[T: Builder](
                             accountSecret: String,
                             operations: List[T],
                             memo: Option[String] = None,
                             memoType: Option[String] = Some("text"),
                           ): SubmitTransactionResponse = {
      val keyPair: KeyPair = KeyPair.fromSecretSeed(accountSecret)

      val source: AccountResponse = server.accounts().account(keyPair)

      val builder = new Transaction.Builder(source)
      operations.foreach(operation => builder.addOperation(implicitly[Builder[T]].build(operation)))

      memo.foreach(memo => {
        val memoOption = memoType map {
          case "text" => Memo.text(memo)
          case "hash" => Memo.hash(memo)
          case "id" => Memo.id(memo.toLong)
          case _ => Memo.none()
        }
        builder.addMemo(memoOption.get)
      })

      val transaction = builder.build()
      transaction.sign(keyPair)

      server.submitTransaction(transaction)
    }
  }

  def generateKeyPair(): Stellar.GenerateKeyPairResult = {
    val pair: KeyPair = KeyPair.random()
    Stellar.GenerateKeyPairResult(account = pair.getAccountId, accountSecret = pair.getSecretSeed.mkString(""))
  }

  def createTestAccount(account: String): String = {
    Http(s"https://horizon-testnet.stellar.org/friendbot?addr=$account").asString.body
  }

  def createAccount(accountSecret: String, newAccountSecret: String, startingBalance: String): Unit = {
    TransactionAdapter.process(
      accountSecret = accountSecret,
      operations = List(CreateAccountStellarOperation(
        accountSecret = newAccountSecret,
        startingBalance = startingBalance

      ))
    )
  }

  def getAccountInfo(account: String): AccountResponse = server.accounts().account(KeyPair.fromAccountId(account))

  def getOffers(
                 account: String,
                 cursor: Option[String] = None,
                 limit: Int = 25,
                 order: Order.Value = Order.asc
               ): Page[OfferResponse] = {
    val builder = server.offers().order(order match {
      case Order.desc => SdkOrder.DESC
      case _ => SdkOrder.ASC
    }).limit(limit).forAccount(KeyPair.fromAccountId(account))

    cursor.foreach(builder.cursor)

    builder.execute()
  }

  def getPaths(
                 sourceAccount: String,
                 destinationAccount: String,
                 destinationAssetType: Option[String] = Some("notNative"),
                 destinationAssetCode: Option[String] = None,
                 destinationAssetIssuer: Option[String] = None,
                 destinationAmount: String,
                 cursor: Option[String] = None,
                 limit: Int = 25,
                 order: Order.Value = Order.asc
               ): Page[PathResponse] = {
    val builder = server.paths()
    builder.sourceAccount(KeyPair.fromAccountId(sourceAccount))
    builder.destinationAccount(KeyPair.fromAccountId(destinationAccount))
    builder.destinationAmount(destinationAmount)
    builder.destinationAsset(destinationAssetType map {
      case "native" => new AssetTypeNative()
      case _ => Asset.createNonNativeAsset(destinationAssetCode.get, KeyPair.fromAccountId(destinationAssetIssuer.get))
    } get)
    builder.order(order match {
      case Order.desc => SdkOrder.DESC
      case _ => SdkOrder.ASC
    }).limit(limit)

    cursor.foreach(builder.cursor)

    builder.execute()
  }

  def manageOffer(
                   accountSecret: String,
                   offerId: Option[String] = None,
                   sellingAssetType: Option[String] = Some("notNative"),
                   sellingAssetCode: Option[String] = None,
                   sellingAssetIssuer: Option[String] = None,
                   buyingAssetType: Option[String] = Some("notNative"),
                   buyingAssetCode: Option[String] = None,
                   buyingAssetIssuer: Option[String] = None,
                   price: String,
                   amount: String,
                 ): SubmitTransactionResponse = {
    TransactionAdapter.process(
      accountSecret = accountSecret,
      operations = List(ManageOfferStellarOperation(
        offerId = offerId,
        sellingAssetType = sellingAssetType,
        sellingAssetCode = sellingAssetCode,
        sellingAssetIssuer = sellingAssetIssuer,
        buyingAssetType = buyingAssetType,
        buyingAssetCode = buyingAssetCode,
        buyingAssetIssuer = buyingAssetIssuer,
        price = price,
        amount = amount,
      ))
    )
  }

  def payToAccount(
                  sourceSecret: String,
                  destinationAccount: String,
                  amount: String,
                  assetType: Option[String] = Some("notNative"),
                  assetCode: Option[String] = None,
                  assetIssuer: Option[String] = None,
                  memo: Option[String] = None,
                  memoType: Option[String] = Some("text"),
                  ): SubmitTransactionResponse = {
    TransactionAdapter.process(
      accountSecret = sourceSecret,
      operations = List(PaymentStellarOperation(
        destinationAccount = destinationAccount,
        amount = amount,
        assetType = assetType,
        assetCode = assetCode,
        assetIssuer = assetIssuer
      )),
      memo = memo,
      memoType = memoType
    )
  }

  def addAnchorToTrustline(
                            accountSecret: String,
                            assetType: Option[String] = Some("notNative"),
                            assetCode: Option[String] = None,
                            assetIssuer: Option[String] = None,
                            limit: String

                          ): SubmitTransactionResponse = {
    TransactionAdapter.process(
      accountSecret = accountSecret,
      operations = List(ChangeTrustStellarOperation(
        assetType = assetType,
        assetCode = assetCode,
        assetIssuer = assetIssuer,
        limit = limit
      ))
    )
  }
}
