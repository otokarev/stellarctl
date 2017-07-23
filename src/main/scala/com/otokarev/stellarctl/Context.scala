package com.otokarev.stellarctl

import com.typesafe.config.{Config, ConfigFactory}
import org.stellar.sdk.Network

class Context (config: Config) {

  config.checkValid(ConfigFactory.defaultReference(), "stellarctl")

  def this() {
    this(ConfigFactory.load())
  }

  val horizonUrl: String = config.getString("stellarctl.horizon_url")

  val env: String = config.getString("stellarctl.env")

  if (env == "prod") {
    Network.usePublicNetwork()
  } else {
    Network.useTestNetwork()
  }

}
