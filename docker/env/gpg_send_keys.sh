#!/bin/bash

function sendKeys(){
  gpg -k | grep 'pub ' | while read line
  do
    line=`echo $line | cut -d / -f 2 | cut -d " " -f 1`
    echo "check gpg key $line in server[hkp://pool.sks-keyservers.net]..."
    str=`gpg --keyserver hkp://pool.sks-keyservers.net --recv-keys $line 2>&1 | grep "Total number" | cut -d : -f 3`
    if [[ $str =~ "0" ]]; then
      echo "push to server..."
      gpg --keyserver hkp://pool.sks-keyservers.net --send-keys $line
    elif [[ $str =~ "1" ]]; then
      echo "the key $line has been push." 
    else
      echo "the key $line has error."
    fi
  done
}

function checkKeys(){
  gpg -k | grep 'pub ' | while read line
  result=-1
  do
    line=`echo $line | cut -d / -f 2 | cut -d " " -f 1`
    echo "check gpg key $line in server[hkp://pool.sks-keyservers.net]..."
    str=`gpg --keyserver hkp://pool.sks-keyservers.net --recv-keys $line 2>&1 | grep "Total number" | cut -d : -f 3`
    if [[ $str =~ "1" ]]; then
      return 1
    elif [[ $str =~ "0" ]]; then
      result=0
    fi
  done
  return $result
}
#
#----------------------------------------------
#
echo "doCall fun."
checkKeys
echo "result is $?"

if [[ $? == 1 ]]; then
  echo "do sendKeys..."
  #sendKeys
fi

