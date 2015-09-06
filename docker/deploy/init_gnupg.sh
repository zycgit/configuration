#!/bin/bash

gpg -k| grep 'pub ' | while read line
do
	echo ${gpgKey:12:8}
done


#gpg --keyserver hkp://pool.sks-keyservers.net --send-keys C990D076
#gpg --keyserver hkp://pool.sks-keyservers.net --recv-keys C990D076
#init_gnupg.sh