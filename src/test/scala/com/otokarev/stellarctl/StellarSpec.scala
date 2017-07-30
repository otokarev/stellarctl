package com.otokarev.stellarctl

import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}
import org.stellar.sdk.KeyPair
import scala.collection.JavaConverters._

class StellarSpec extends FlatSpec with Matchers {
  import net.liftweb.json._
  import net.liftweb.json.Extraction._
  import com.otokarev.stellarctl.SerializerImplicits._
  implicit val context = new Context()
  private val stellar = new Stellar()
  private val config = ConfigFactory.load()
  private val account1 = config.getString("stellar.test.account1")
  private val accountSecret1 = config.getString("stellar.test.accountSecret1")
  private val account2 = config.getString("stellar.test.account2")
  private val accountSecret2 = config.getString("stellar.test.accountSecret2")
  private val issuerAccount = config.getString("stellar.test.issuerAccount")

  // Will not work in test mode
  it should "Create account" in {
    val pair = KeyPair.random()
    println(prettyRender(decompose(stellar.createAccount(
      accountSecret = accountSecret1,
      newAccountSecret = pair.getSecretSeed.mkString(""),
      startingBalance = "100"
    ))))
  }

  it should "Generate Key Pair" in {
    println(prettyRender(decompose(stellar.generateKeyPair())))
  }

  it should "Create test account" in {
    println(stellar.createTestAccount(account1))
  }

  it should "Get account info" in {
    println(prettyRender(decompose(stellar.getAccountInfo(account1))))

  }

  it should "Add new asset in account's trustline" in {
    println(prettyRender(decompose(stellar.addAnchorToTrustline(
      accountSecret = accountSecret1,
      assetCode = Option("BTC"),
      assetIssuer = Option(issuerAccount),
      limit = "100000"
    ))))
  }

  it should "Send coins to account" in {
    println(prettyRender(decompose(stellar.payToAccount(
      sourceSecret = accountSecret1,
      destinationAccount = account2,
      assetCode = Option("BTC"),
      assetIssuer = Option(issuerAccount),
      amount = "0.0001"
    ))))
  }

  it should "Create an offer" in {
    println(prettyRender(decompose(stellar.manageOffer(
      accountSecret = accountSecret1,
      sellingAssetCode = Option("BTC"),
      sellingAssetIssuer = Option(issuerAccount),
      buyingAssetCode = Option("XLM"),
      buyingAssetIssuer = Option(issuerAccount),
      price = "1.2",
      amount = "0.001"
    ))))
  }

  it should "Get account's offers" in {
    println(prettyRender(decompose(stellar.getOffers(account = account1))))
  }
}
