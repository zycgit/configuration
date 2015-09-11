#!/usr/bin/expect
set passphrase [lindex $argv 0]

spawn mvn clean release:clean release:prepare -P release -Dgpg.passphrase=$passphrase
expect {
  "What is the release version for"         { send "\r"; exp_continue; }
  "What is SCM release tag or label for"    { send "\r"; exp_continue; }
  "What is the new development version for" { send "\r"; exp_continue; }
}