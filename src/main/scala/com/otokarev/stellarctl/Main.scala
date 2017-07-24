package com.otokarev.stellarctl

import java.io.File

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
                     memoType: String = ""
  )

  def main(args: Array[String]) {
    val parser = new scopt.OptionParser[Config]("scopt") {
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
            .action( (x, c) => c.copy(limit = x) ).text("asset limit")
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
          opt[String]("asset-code").required().abbr("ac")
            .action( (x, c) => c.copy(assetCode = x) ).text("asset code"),
          opt[String]("asset-issuer").required().abbr("ai")
            .action( (x, c) => c.copy(assetIssuer = x) ).text("asset issuer account ID"),
          opt[String]("amount").required().abbr("a")
            .action( (x, c) => c.copy(amount = x) ).text("amount"),
          opt[String]("memo").abbr("m")
            .action( (x, c) => c.copy(memo = x) ).text("memo"),
          opt[String]("memo-type").abbr("mt")
            .action( (x, c) => c.copy(memoType = x) ).text("memo-type"),
        )
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
        }

      case None =>
      // arguments are bad, error message will have been displayed
    }
  }
}
