package com.otokarev.stellarctl

import org.stellar.sdk._
import org.stellar.sdk.responses._

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

  implicit val formats: Formats =
    DefaultFormats +
      new PriceSerializer +
      new AssetSerializer +
      new AccountResponseSerializer +
      new PageOfferResponseSerializer +
      new PagePathResponseSerializer +
      new SubmitTransactionResponseSerializer +
      new GenerateKeyPairResultSerializer +
      new OrderBookResponseRowSerializer +
      new OrderBookResponseSerializer +
      new SubmitTransactionExtrasSerializer +
      new SubmitTransactionExtrasResultCodesSerializer

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

  class PagePathResponseSerializer extends CustomSerializer[Page[PathResponse]](format => (
    {
      // We do not need deserialization so put here something for scala to compile
      case _ => null.asInstanceOf[Page[PathResponse]]
    },
    {
      case response: Page[PathResponse] =>
        val records = response.getRecords.asScala.toList.map(record => {
          var result = null.asInstanceOf[JObject]
          record.getSourceAsset match {
            case a: AssetTypeCreditAlphaNum => result = ("source_asset_type" -> a.getType) ~ ("source_asset_code" -> a.getCode) ~ ("source_asset_issuer" -> a.getIssuer.getAccountId)
            case _: AssetTypeNative => result = "source_asset_type" -> "native"
          }
          result = result ~ ("source_amount" -> record.getSourceAmount)
          record.getDestinationAsset match {
            case a: AssetTypeCreditAlphaNum => result = result ~ ("destination_asset_type" -> a.getType) ~ ("destination_asset_code" -> a.getCode) ~ ("destination_asset_issuer" -> a.getIssuer.getAccountId)
            case _: AssetTypeNative => result = result ~ ("destination_asset_type" -> "native")
          }
          result = result ~ ("destination_amount" -> record.getDestinationAmount) ~
            ("path" -> decompose(record.getPath.asScala.toList))
          result
        })
        records
    }
  ))

  class PriceSerializer extends CustomSerializer[Price](format => (
    {
      // We do not need deserialization so put here something for scala to compile
      case _ => null.asInstanceOf[Price]
    },
    {
      case response: Price => (response.getNumerator.toFloat / response.getDenominator).toString
    }
  ))

  class OrderBookResponseRowSerializer extends CustomSerializer[OrderBookResponse.Row](format => (
    {
      // We do not need deserialization so put here something for scala to compile
      case _ => null.asInstanceOf[OrderBookResponse.Row]
    },
    {
      case response: OrderBookResponse.Row =>
        val result: JObject = ("amount" -> response.getAmount) ~
          ("price" -> decompose(response.getPrice)) ~
          ("price_r" -> decompose(response.getPriceR))
        result
    }
  ))

  class OrderBookResponseSerializer extends CustomSerializer[OrderBookResponse](format => (
    {
      // We do not need deserialization so put here something for scala to compile
      case _ => null.asInstanceOf[OrderBookResponse]
    },
    {
      case response: OrderBookResponse =>
        val result: JObject = ("bids" -> decompose(response.getBids)) ~
          ("asks" -> decompose(response.getAsks)) ~
          ("base" -> decompose(response.getBase)) ~
          ("counter" -> decompose(response.getCounter))
        result
    }
  ))

  class SubmitTransactionExtrasResultCodesSerializer extends CustomSerializer[SubmitTransactionResponse.Extras.ResultCodes](format => (
    {
      // We do not need deserialization so put here something for scala to compile
      case _ => null.asInstanceOf[SubmitTransactionResponse.Extras.ResultCodes]
    },
    {
      case response: SubmitTransactionResponse.Extras.ResultCodes =>
        var result = JObject()
        Option(response.getTransactionResultCode).foreach(_ => result = result ~ ("transaction" -> response.getTransactionResultCode))
        Option(response.getOperationsResultCodes).foreach(_ => result = result ~ ("operations" -> response.getOperationsResultCodes.asScala))
        result
    }
  ))

  class SubmitTransactionExtrasSerializer extends CustomSerializer[SubmitTransactionResponse.Extras](format => (
    {
      // We do not need deserialization so put here something for scala to compile
      case _ => null.asInstanceOf[SubmitTransactionResponse.Extras]
    },
    {
      case response: SubmitTransactionResponse.Extras =>
        var result: JObject = "result_xdr" -> response.getResultXdr
        result = result ~ ("envelope_xdr" -> response.getEnvelopeXdr)
        result = result ~ ("result_codes" -> decompose(response.getResultCodes))
        result
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
          ("ledger" -> Option(response.getLedger).map(_.longValue())) ~
          ("envelopeXdr" -> response.getEnvelopeXdr) ~
          ("resultXdr" -> response.getResultXdr) ~
          ("extras" -> decompose(response.getExtras))

        result
    }
  ))
}
