package com.otokarev.stellarctl
import com.otokarev.stellarctl.Stellar._

import org.stellar.sdk._
import org.stellar.sdk.responses.{AccountResponse, OfferResponse, Page, SubmitTransactionResponse}

object SerializerImplicits {
  import net.liftweb.json._
  import net.liftweb.json.Extraction._
  import net.liftweb.json.JsonDSL._
  import scala.collection.JavaConverters._

  class AssetSerializer extends CustomSerializer[Asset](format => (
    {
      case JObject(JField("type", JString(s)) :: JField("code", JString(code)) :: JField("issuer", JString(issuer)) :: Nil) =>
        Asset.createNonNativeAsset(code, KeyPair.fromAccountId(issuer))
      case JObject(JField("type", JString("native")) :: _) => new AssetTypeNative()
    },
    {
      case asset: Asset => asset  match {
        case a: AssetTypeCreditAlphaNum => JObject(JField("type", JString(a.getType)) :: JField("code", JString(a.getCode)) :: JField("issuer", JString(a.getIssuer.getAccountId)) :: Nil)
        case _: AssetTypeNative => JObject(JField("type", JString("native")))
      }
    },
  ))

  class GenerateKeyPairResultSerializer extends CustomSerializer[Stellar.GenerateKeyPairResult](format => (
    {
      // We do not need deserialization so put here something for scala to compile
      case _ => null.asInstanceOf[Stellar.GenerateKeyPairResult]
    },
    {
      case result: Stellar.GenerateKeyPairResult => JObject(JField("account", JString(result.account)) :: JField("accountSecret", JString(result.accountSecret)) :: Nil)
    },
  ))

  class AccountResponseSerializer extends CustomSerializer[AccountResponse](format => (
    {
      // We do not need deserialization so put here something for scala to compile
      case _ => null.asInstanceOf[AccountResponse]
    },
    {
      case response: AccountResponse =>
        val balances = response.getBalances.map(b => {
          var balance: JObject = "assetType" -> b.getAssetType
          if (b.getAssetType != "native") {
            balance = balance ~ ("assetCode" -> b.getAssetCode) ~ ("assetIssuer" -> b.getAssetIssuer.getAccountId)
          }
          balance = balance ~ ("balance" -> b.getBalance) ~ ("limit" -> b.getLimit)
          balance
        })
        "balances" -> balances.toList
    }
  ))

  implicit val formats: Formats = DefaultFormats + new AssetSerializer + new AccountResponseSerializer + new PageOfferResponseSerializer + new SubmitTransactionResponseSerializer + new GenerateKeyPairResultSerializer

  class PageOfferResponseSerializer extends CustomSerializer[Page[OfferResponse]](format => (
    {
      // We do not need deserialization so put here something for scala to compile
      case _ => null.asInstanceOf[Page[OfferResponse]]
    },
    {
      case response: Page[OfferResponse] =>
        val records = response.getRecords.asScala.toList.map(record => {
          val result = ("id" -> record.getId.toString) ~
            ("seller" -> record.getSeller.getAccountId) ~
            ("pagingToken" -> record.getPagingToken) ~
            ("buying" -> decompose(record.getBuying)) ~
            ("selling" -> decompose(record.getSelling)) ~
            ("price" -> record.getPrice) ~
            ("amount" -> record.getAmount)
          result
        })
        records
    }
  ))

  class SubmitTransactionResponseSerializer extends CustomSerializer[SubmitTransactionResponse](format => (
    {
      // We do not need deserialization so put here something for scala to compile
      case _ => null.asInstanceOf[SubmitTransactionResponse]
    },
    {
      case response: SubmitTransactionResponse =>
        var result: JObject = ("isSuccess" -> response.isSuccess) ~
          ("hash" -> response.getHash) ~
          ("envelopeXdr" -> response.getEnvelopeXdr) ~
          ("resultXdr" -> response.getResultXdr)

        if (response.isSuccess) {
          result = result ~ ("ledger" -> response.getLedger.toString)
        } else {
          val resultCodes: JObject = ("operationsResultCode" -> response.getExtras.getResultCodes.getOperationsResultCodes.asScala) ~
            ("transactionResultCode" -> response.getExtras.getResultCodes.getTransactionResultCode)

          result = result ~ ("envelopeXdr" -> response.getExtras.getEnvelopeXdr) ~
            ("resultXdr" -> response.getExtras.getResultXdr) ~
            ("resultCodes" -> resultCodes)

        }

        result
    }
  ))
}
