Damn simple CLI application to communicate with [Stellar](https://www.stellar.org/) platform

# Install

Deb package: [stellarctl_0.1.0-5_all.deb](https://github.com/otokarev/stellarctl/releases/download/v0.1.0-5/stellarctl_0.1.0-5_all.deb)

Universal package: [stellarctl-0.1.0-5.zip](https://github.com/otokarev/stellarctl/releases/download/v0.1.0-5/stellarctl-0.1.0-5.zip)

# Usage
```
# stellarctl --help
stellarctl 0.1.0
Usage: stellarctl [generate-keypair|account-info|create-test-account|create-account|add-asset-to-trustline|pay-to-account|manage-offer|get-offers|get-paths|get-orderbook] [options]

  --help                   prints this usage text
  -c, --config <value>     configuration file
Command: generate-keypair
Generate key pair
Command: account-info [options]
Get info on existing Stellar account
  -p, --account <value>    account ID (public key)
Command: create-test-account [options]
Create Test Stellar account
  -p, --account <value>    account ID (public key)
Command: create-account [options]
Create Stellar account
  -s, --account-secret <value>
                           account secret (private key)
  -ns, --new-account-secret <value>
                           new account secret (private key)
  -b, --starting-balance <value>
                           startingBalance
Command: add-asset-to-trustline [options]
Add asset to account's trustline
  -s, --account-secret <value>
                           account secret (private key)
  -at, --asset-type <value>
                           asset type
  -ac, --asset-code <value>
                           asset code
  -ai, --asset-issuer <value>
                           asset issuer account ID
  -l, --limit <value>      asset limit
Command: pay-to-account [options]
Send coins to account
  -s, --account-secret <value>
                           source account secret (private key)
  -dp, --destination-account <value>
                           destination account ID (public key)
  -at, --asset-type <value>
                           asset type
  -ac, --asset-code <value>
                           asset code
  -ai, --asset-issuer <value>
                           asset issuer account ID
  -a, --amount <value>     amount
  -m, --memo <value>       memo
  -mt, --memo-type <value>
                           memo-type
Command: manage-offer [options]
Manage an offer.
To remove an order just specify --offer-id and set --amount to 0, in the case the parameters --(buying|selling)-asset-(code|type|issuer) and --price can have any valid values except that buying asset must differ selling asset.
  -s, --account-secret <value>
                           source account secret (private key)
  -sat, --selling-asset-type <value>
                           selling asset type
  -sac, --selling-asset-code <value>
                           selling asset code
  -sai, --selling-asset-issuer <value>
                           selling asset issuer account ID
  -bat, --buying-asset-type <value>
                           buying asset type
  -bac, --buying-asset-code <value>
                           buying asset code
  -bai, --buying-asset-issuer <value>
                           buying asset issuer account ID
  -a, --amount <value>     amount of selling items
  -w, --price <value>      price of a selling item
  -oi, --offer-id <value>  offer ID
Command: get-offers [options]
Get existing account's offers
  -p, --account <value>    account ID (public key)
  -t, --cursor <value>     cursor (paging token)
  -l, --limit <value>      limit (page size)
  -o, --order <value>      order (`asc` or `desc`)
Command: get-paths [options]
Get payment paths
  -sp, --source-account <value>
                           source account ID (public key)
  -dp, --destination-account <value>
                           destination account ID (public key)
  -dat, --destination-asset-type <value>
                           destination asset type
  -dac, --destination-asset-code <value>
                           destination asset code
  -dai, --destination-asset-issuer <value>
                           destination asset issuer account ID
  -da, --destination-amount <value>
                           amount
  -t, --cursor <value>     cursor (paging token)
  -l, --limit <value>      limit (page size)
  -o, --order <value>      order (`asc` or `desc`)
Command: get-orderbook [options]
Get orderbook
  -sat, --selling-asset-type <value>
                           selling asset type
  -sac, --selling-asset-code <value>
                           selling asset code
  -sai, --selling-asset-issuer <value>
                           selling asset issuer account ID
  -bat, --buying-asset-type <value>
                           buying asset type
  -bac, --buying-asset-code <value>
                           buying asset code
  -bai, --buying-asset-issuer <value>
                           buying asset issuer account ID

```

# Build
## Universal
```
git clone git@github.com:otokarev/stellarctl.git
cd stellarctl

sbt universal:packageBin
```
now you got `target/universal/stellarctl-0.1.0-5.zip` that you can unpack and use
```
unzip stellarctl-0.1.0-5.zip
./stellarctl-0.1.0-5/bin/stellarctl --help
```

## Debian
```
git clone git@github.com:otokarev/stellarctl.git
cd stellarctl

sbt debian:packageBin
sudo apt install stellarctl/target/stellarctl_0.1.0-5_all.deb
```
