#!/bin/bash

function sendKeys(){
  gpg -k | grep 'pub ' | while read line
  do
    line=`echo $line | cut -d / -f 2 | cut -d " " -f 1`
    echo "check gpg key $line in server[hkp://pool.sks-keyservers.net]..."
    str=`gpg --keyserver hkp://pool.sks-keyservers.net --recv-keys $line 2>&1 | grep "Total number" | cut -d : -f 3 | cut -d " " -f 2`
    if [[ "$str" == "0" ]]; then
      echo "push to server..."
      gpg --keyserver hkp://pool.sks-keyservers.net --send-keys $line
    elif [[ "$str" == "1" ]]; then
      echo "the key $line has been push."
    else
      echo "the key $line has error."
    fi
  done
}

function checkKeys(){
  result=2
  gpg -k | grep 'pub ' | while read oriLine
  do
    line=`echo $oriLine | cut -d / -f 2 | cut -d " " -f 1`
    echo "check gpg key $line in server[hkp://pool.sks-keyservers.net]..."
    str=`gpg --keyserver hkp://pool.sks-keyservers.net --recv-keys $line 2>&1 | grep "Total number" | cut -d : -f 3 | cut -d " " -f 2`
    if [[ "$str" == "1" ]]; then
      echo "1----$str"
      return $[1]
    elif [[ "$str" == "0" ]]; then
      echo "2----$str"
      result=0
    else
      echo "message = $str"
    fi
  done
  echo "result is --$result--"
  return $[ $result ]
}
#
#----------------------------------------------
#
echo "doCall fun."
checkKeys
echo "result is $?"

if [[ "$?" == "1" ]]; then
  echo "do sendKeys..."
  sendKeys
fi






#gpg -k| grep 'pub ' | while read line
#do
#	echo ${gpgKey:12:8}
#done
#gpg --keyserver hkp://pool.sks-keyservers.net --send-keys C990D076
#gpg --keyserver hkp://pool.sks-keyservers.net --recv-keys C990D076
#init_gnupg.sh