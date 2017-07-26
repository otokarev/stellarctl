Damn simple CLI application to communicate with [Stellar](https://www.stellar.org/) platform

# Install

Deb package: [stellarctl_0.1.0-2_all.deb](https://github.com/otokarev/stellarctl/releases/download/v0.1.0-2/stellarctl_0.1.0-2_all.deb)

Universal package: [stellarctl-0.1.0-2.zip](https://github.com/otokarev/stellarctl/releases/download/v0.1.0-2/stellarctl-0.1.0-2.zip)

# Usage
```
# stellarctl --help
stellarctl 0.1.0
Usage: stellarctl [generate-keypair|account-info|create-test-account|create-account|add-asset-to-trustline|pay-to-account] [options]

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

```

# Build
## Universal
```
git clone git@github.com:otokarev/stellarctl.git
cd stellarctl

sbt universal:packageBin
```
now you got `target/universal/stellarctl-0.1.0-2.zip` that you can unpack and use
```
unzip stellarctl-0.1.0-2.zip
./stellarctl-0.1.0-2/bin/stellarctl --help
```

## Debian
```
git clone git@github.com:otokarev/stellarctl.git
cd stellarctl

sbt debian:packageBin
sudo apt install stellarctl/target/stellarctl_0.1.0-2_all.deb
```
