package com.otokarev.stellarctl

import com.typesafe.config.ConfigFactory

object Main {
  case class Config(
                     command: String = "",
                     config: String = "",
                     account: String = "",
                     accountSecret: String = "",
                     assetCode: String = "",
                     assetIssuer: String = "",
                     limit: Int = 100,
                     destinationAccount: String = "",
                     sourceAccount: String = "",
                     sourceAccountSecret: String = "",
                     amount: Double = 0.0,
                     memo: String = "",
                     memoType: String = ""
  )

  def main(args: Array[String]) {
    val parser = new scopt.OptionParser[Config]("scopt") {
      opt[String]("config").abbr("c").action( (x, c) =>
        c.copy(config = x) ).text("configuration file")
      cmd("account-info").action( (_, c) => c.copy(command = "account-info") ).
        text("Get info on existing Stellar account").
        children(
          opt[String]("account").required().abbr("p")
            .action( (x, c) => c.copy(account = x) ).text("account ID (public key)")
        )

      cmd("create-account").action( (_, c) => c.copy(command = "create-account") ).
        text("Create Stellar account")

      cmd("add-asset-to-trustline").action( (_, c) => c.copy(command = "add-asset-to-trustline") ).
        text("Add asset to account's trustline").
        children(
          opt[String]("account").required().abbr("p")
            .action( (x, c) => c.copy(account = x) ).text("account ID (public key)"),
          opt[String]("account-secret").required().abbr("s")
            .action( (x, c) => c.copy(accountSecret = x) ).text("account secret (private key)"),
          opt[String]("asset-code").required().abbr("ac")
            .action( (x, c) => c.copy(assetCode = x) ).text("asset code"),
          opt[String]("asset-issuer").required().abbr("ai")
            .action( (x, c) => c.copy(assetIssuer = x) ).text("asset issuer account ID"),
          opt[Int]("limit").required().abbr("l")
            .action( (x, c) => c.copy(limit = x) ).text("asset limit")
      )

      cmd("pay-to-account")
        .action( (_, c) => c.copy(command = "pay-to-account") ).
        text("Send coins to account").
        children(
          opt[String]("destination-account").required().abbr("dp")
            .action( (x, c) => c.copy(destinationAccount = x) ).text("destination account ID (public key)"),
          opt[String]("source-account").required().abbr("sp")
            .action( (x, c) => c.copy(sourceAccount = x) ).text("source account ID (public key)"),
          opt[String]("source-account-secret").required().abbr("ss")
            .action( (x, c) => c.copy(sourceAccountSecret = x) ).text("source account secret (private key)"),
          opt[String]("asset-code").required().abbr("ac")
            .action( (x, c) => c.copy(assetCode = x) ).text("asset code"),
          opt[String]("asset-issuer").required().abbr("ai")
            .action( (x, c) => c.copy(assetCode = x) ).text("asset issuer account ID"),
          opt[Double]("amount").required().abbr("a")
            .action( (x, c) => c.copy(amount = x) ).text("amount"),
          opt[String]("memo").abbr("m")
            .action( (x, c) => c.copy(memo = x) ).text("memo"),
          opt[String]("memo-type").abbr("mt")
            .action( (x, c) => c.copy(memoType = x) ).text("memo-type"),
        )
    }
    parser.parse(args, Config()) match {
      case Some(config) =>
        implicit val context = new Context(ConfigFactory.load())

        config.command match {
          case "create-account" => createAccount()
          case "account-info" => getAccountInfo()
          case "add-asset-to-trustline" => addAssetToTrustline()
          case "pay-to-account" => payToAccount()
        }

      case None =>
      // arguments are bad, error message will have been displayed
    }
  }

  def createAccount ()(implicit context: Context): Unit = {

  }

  def getAccountInfo ()(implicit context: Context): Unit = {

  }

  def addAssetToTrustline ()(implicit context: Context): Unit = {

  }

  def payToAccount ()(implicit context: Context): Unit = {

  }

}
