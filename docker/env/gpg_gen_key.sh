#!/usr/bin/expect
set timeout 5
set userName   [lindex $user 0]
set userMail   [lindex $mail 1]
set passphrase [lindex $passphrase 2]

spawn rm -rf ~/.gnupg/random_seed
spawn dd if=/dev/urandom of=~/.gnupg/random_seed bs=1024b count=1

spawn gpg --gen-key
expect {
  "Your selection?"           { send "\r";            exp_continue; }
  "What keysize do you want?" { send "\r";            exp_continue; }
  "Key is valid for?"         { send "\r";            exp_continue; }
  "Is this correct? (y/N)"    { send "y\r";           exp_continue; }
  "Real name:"                { send "$userName\r";   exp_continue; }
  "Email address:"            { send "$userMail\r";   exp_continue; }
  "Comment:"                  { send "\r";            exp_continue; }
  "(O)kay/(Q)uit?"            { send "O\r";           exp_continue; }
  "Enter passphrase:"         { send "$passphrase\r"; exp_continue; }
  "Repeat passphrase:"        { send "$passphrase\r"; exp_continue; }
}