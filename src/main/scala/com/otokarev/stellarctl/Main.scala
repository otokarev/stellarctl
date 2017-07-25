package com.otokarev.stellarctl

import java.io.File

import com.otokarev.stellarctl.Stellar._
import com.typesafe.config.ConfigFactory

object Main {
  case class Config(
                     command: String = "",
                     config: String = "",
                     account: String = "",
                     newAccountSecret: String = "",
                     accountSecret: String = "",
                     assetType: String = "notNative",
                     assetCode: String = "",
                     assetIssuer: String = "",
                     limit: String = "100",
                     destinationAccount: String = "",
                     amount: String = "0.0",
                     startingBalance: String = "0.0",
                     memo: String = "",
                     memoType: String = "",
                     sellingAssetType: String = "notNative",
                     sellingAssetCode: String = "",
                     sellingAssetIssuer: String = "",
                     buyingAssetType: String = "notNative",
                     buyingAssetCode: String = "",
                     buyingAssetIssuer: String = "",
                     price: String = "",
                     cursor: String = "",
                     order: String = "asc",
                     pageSize: Int = 25,
  )

  def main(args: Array[String]) {
    val parser = new scopt.OptionParser[Config]("stellarctl") {
      head("stellarctl", "0.1.0")

      help("help").text("prints this usage text")

      opt[String]("config").abbr("c").action( (x, c) =>
        c.copy(config = x) ).text("configuration file")

      cmd("generate-keypair").action( (_, c) => c.copy(command = "generate-keypair") ).
        text("Generate key pair")

      cmd("account-info").action( (_, c) => c.copy(command = "account-info") ).
        text("Get info on existing Stellar account").
        children(
          opt[String]("account").required().abbr("p")
            .action( (x, c) => c.copy(account = x) ).text("account ID (public key)")
        )

      cmd("create-test-account").action((_, c) => c.copy(command = "create-test-account"))
        .text("Create Test Stellar account")
        .children(
          opt[String]("account").required().abbr("p")
            .action( (x, c) => c.copy(account = x) ).text("account ID (public key)"),
        )

      cmd("create-account").action((_, c) => c.copy(command = "create-account"))
        .text("Create Stellar account")
        .children(
          opt[String]("account-secret").required().abbr("s")
            .action( (x, c) => c.copy(accountSecret = x) ).text("account secret (private key)"),
          opt[String]("new-account-secret").required().abbr("ns")
            .action( (x, c) => c.copy(newAccountSecret = x) ).text("new account secret (private key)"),
          opt[String]("starting-balance").required().abbr("b")
            .action( (x, c) => c.copy(startingBalance = x) ).text("startingBalance"),
        )

      cmd("add-asset-to-trustline").action( (_, c) => c.copy(command = "add-asset-to-trustline") ).
        text("Add asset to account's trustline").
        children(
          opt[String]("account-secret").required().abbr("s")
            .action( (x, c) => c.copy(accountSecret = x) ).text("account secret (private key)"),
          opt[String]("asset-type").abbr("at")
            .action( (x, c) => c.copy(assetType = x) ).text("asset type"),
          opt[String]("asset-code").abbr("ac")
            .action( (x, c) => c.copy(assetCode = x) ).text("asset code"),
          opt[String]("asset-issuer").abbr("ai")
            .action( (x, c) => c.copy(assetIssuer = x) ).text("asset issuer account ID"),
          opt[String]("limit").required().abbr("l")
            .action( (x, c) => c.copy(limit = x) ).text("asset limit"),
          checkConfig(c => {
            if (c.command != "add-asset-to-trustline")
              success
            else if (c.assetType != "native" && (c.assetIssuer.length == 0 || c.assetCode.length == 0))
              failure("'--asset-type' must be 'native' or both '--asset-code' and '--asset-issuer' must be specified")
            else
              success
          })
      )

      cmd("pay-to-account")
        .action( (_, c) => c.copy(command = "pay-to-account") ).
        text("Send coins to account").
        children(
          opt[String]("account-secret").required().abbr("s")
            .action( (x, c) => c.copy(accountSecret = x) ).text("source account secret (private key)"),
          opt[String]("destination-account").required().abbr("dp")
            .action( (x, c) => c.copy(destinationAccount = x) ).text("destination account ID (public key)"),
          opt[String]("asset-type").abbr("at")
            .action( (x, c) => c.copy(assetType = x) ).text("asset type"),
          opt[String]("asset-code").abbr("ac")
            .action( (x, c) => c.copy(assetCode = x) ).text("asset code"),
          opt[String]("asset-issuer").abbr("ai")
            .action( (x, c) => c.copy(assetIssuer = x) ).text("asset issuer account ID"),
          opt[String]("amount").required().abbr("a")
            .action( (x, c) => c.copy(amount = x) ).text("amount"),
          opt[String]("memo").abbr("m")
            .action( (x, c) => c.copy(memo = x) ).text("memo"),
          opt[String]("memo-type").abbr("mt")
            .action( (x, c) => c.copy(memoType = x) ).text("memo-type"),
          checkConfig(c => {
            if (c.command != "pay-to-account")
              success
            else if (c.assetType != "native" && (c.assetIssuer.length == 0 || c.assetCode.length == 0))
              failure("'--asset-type' must be 'native' or both '--asset-code' and '--asset-issuer' must be specified")
            else
              success
          })
        )

      cmd("create-offer")
        .action( (_, c) => c.copy(command = "create-offer") ).
        text("Create an offer").
        children(
          opt[String]("account-secret").required().abbr("s")
            .action( (x, c) => c.copy(accountSecret = x) ).text("source account secret (private key)"),
          opt[String]("selling-asset-type").abbr("sat")
            .action( (x, c) => c.copy(sellingAssetType = x) ).text("selling asset type"),
          opt[String]("selling-asset-code").abbr("sac")
            .action( (x, c) => c.copy(sellingAssetCode = x) ).text("selling asset code"),
          opt[String]("selling-asset-issuer").abbr("sai")
            .action( (x, c) => c.copy(sellingAssetIssuer = x) ).text("selling asset issuer account ID"),
          opt[String]("buying-asset-type").abbr("bat")
            .action( (x, c) => c.copy(buyingAssetType = x) ).text("buying asset type"),
          opt[String]("buying-asset-code").abbr("bac")
            .action( (x, c) => c.copy(buyingAssetCode = x) ).text("buying asset code"),
          opt[String]("buying-asset-issuer").abbr("bai")
            .action( (x, c) => c.copy(buyingAssetIssuer = x) ).text("buying asset issuer account ID"),
          opt[String]("amount").required().abbr("a")
            .action( (x, c) => c.copy(amount = x) ).text("amount of selling items"),
          opt[String]("price").required().abbr("w")
            .action( (x, c) => c.copy(price = x) ).text("price of a selling item"),
          checkConfig(c => {
            if (c.command != "create-offer")
              success
            else if (c.sellingAssetType != "native" && (c.sellingAssetIssuer.length == 0 || c.sellingAssetCode.length == 0))
              failure("'--selling-asset-type' must be 'native' or both '--selling-asset-code' and '--selling-asset-issuer' must be specified")
            else if (c.buyingAssetType != "native" && (c.buyingAssetIssuer.length == 0 || c.buyingAssetCode.length == 0))
              failure("'--buying-asset-type' must be 'native' or both '--buying-asset-code' and '--buying-asset-issuer' must be specified")
            else
              success
          })
        )

      cmd("get-offers")
        .action( (_, c) => c.copy(command = "get-offers") ).
        text("Get existing account's offers").
        children(
          opt[String]("account").required().abbr("p")
            .action( (x, c) => c.copy(account = x) ).text("account ID (public key)"),
          opt[String]("cursor").abbr("t")
            .action( (x, c) => c.copy(cursor = x) ).text("cursor (paging token)"),
          opt[Int]("limit").abbr("l")
            .action( (x, c) => c.copy(pageSize = x) ).text("limit (page size)"),
          opt[String]("order").abbr("o")
            .action( (x, c) => c.copy(order = x) ).text("order (`asc` or `desc`)")
        )

      checkConfig {c =>
        if (c.command.length == 0) failure("No command given") else success
      }
    }
    parser.parse(args, Config()) match {
      case Some(config) =>
        import net.liftweb.json._
        import net.liftweb.json.Extraction._
        import com.otokarev.stellarctl.SerializerImplicits._

        implicit val context = new Context(if (config.config.length > 0) ConfigFactory.parseFile(new File(config.config)) else ConfigFactory.load())

        val stellar = new Stellar()

        config.command match {
          case "generate-keypair" => println(prettyRender(decompose(stellar.generateKeyPair())))
          case "create-test-account" => println(stellar.createTestAccount(config.account))
          case "create-account" =>
            println(prettyRender(decompose(stellar.createAccount(
              accountSecret = config.accountSecret,
              newAccountSecret = config.newAccountSecret,
              startingBalance = config.startingBalance
            ))))
          case "account-info" => println(prettyRender(decompose(stellar.getAccountInfo(config.account))))
          case "add-asset-to-trustline" =>
            println(prettyRender(decompose(stellar.addAnchorToTrustline(
              accountSecret = config.accountSecret,
              assetType = if (config.assetType.length > 0) Option(config.assetType) else None,
              assetCode = if (config.assetCode.length > 0) Option(config.assetCode) else None,
              assetIssuer = if (config.assetIssuer.length > 0) Option(config.assetIssuer) else None,
              limit = config.limit
            ))))
          case "pay-to-account" =>
            println(prettyRender(decompose(stellar.payToAccount(
              sourceSecret = config.accountSecret,
              destinationAccount = config.destinationAccount,
              assetType = if (config.assetType.length > 0) Option(config.assetType) else None,
              assetCode = if (config.assetCode.length > 0) Option(config.assetCode) else None,
              assetIssuer = if (config.assetIssuer.length > 0) Option(config.assetIssuer) else None,
              amount = config.amount,
              memo = if (config.memo.length > 0) Option(config.memo) else None,
              memoType = if (config.memoType.length > 0) Option(config.memoType) else None,
            ))))
          case "create-offer" =>
            println(prettyRender(decompose(stellar.createOffer(
              accountSecret = config.accountSecret,
              sellingAssetType = if (config.sellingAssetType.length > 0) Option(config.sellingAssetType) else None,
              sellingAssetCode = if (config.sellingAssetCode.length > 0) Option(config.sellingAssetCode) else None,
              sellingAssetIssuer = if (config.sellingAssetIssuer.length > 0) Option(config.sellingAssetIssuer) else None,
              buyingAssetType = if (config.buyingAssetType.length > 0) Option(config.buyingAssetType) else None,
              buyingAssetCode = if (config.buyingAssetCode.length > 0) Option(config.buyingAssetCode) else None,
              buyingAssetIssuer = if (config.buyingAssetIssuer.length > 0) Option(config.buyingAssetIssuer) else None,
              price = config.price,
              amount = config.amount
            ))))
          case "get-offers" =>
            println(prettyRender(decompose(stellar.getOffers(
              account = config.account,
              cursor = if (config.cursor.length > 0) Option(config.cursor) else None,
              limit = config.pageSize,
              order = if (config.order == "desc") Stellar.Order.desc else Stellar.Order.asc
            ))))
        }

      case None =>
      // arguments are bad, error message will have been displayed
    }
  }
}
