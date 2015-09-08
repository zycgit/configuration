#!/usr/bin/env python
import os
import sys
#
def foreachKey(function):
    lines = os.popen("gpg -k | grep 'pub '").readlines()
    for value in lines:
        keyVal = value.split("/")[1].split(" ")[0]
        result = function(keyVal)
        if result != None:
            return result
    return None
#
def inKey(keyVal, function):
    def foo(key):
        if key == keyVal:
            return function(key)
    return foreachKey(foo)
#
def recvKey(keyVal):
    def foo(key):
        print("check gpg key %s in server 'hkp://pool.sks-keyservers.net'..." % (keyVal))
        execResult = os.popen("gpg --keyserver hkp://pool.sks-keyservers.net --recv-keys %s 2>&1 | grep 'Total number'" % (keyVal)).read()
        execResult = execResult.split(":")[2].strip()
        if execResult == "1" :
            return True
        elif execResult == "0":
            return False
        else:
            return False
    return inKey(keyVal, foo)
#
def sendKey(keyVal):
    def foo(key):
        execResult = os.system("gpg --keyserver hkp://pool.sks-keyservers.net --send-keys %s" % (keyVal))
        if execResult == 0 :
            print("the key %s has been push." % (key))
            return True
        else:
            print("the key %s has error , exit code = %s" % (key, execResult))
            return False
    return inKey(keyVal, foo)
#
def redFile(file):
    body = ''
    file_pub = open(file)
    try :
        body = file_pub.read()
    finally:
        file_pub.close()
    return body.strip()
#
def initGPG(user, email, passphrase):
    gpgKey = ''
    lockKeyFile = '/script/key.lock'
    pubKeyFile = '/script/key.pub'
    subKeyFile = '/script/key.sub'
    if os.path.exists("/script") == False:
        os.makedirs(r"/script")
    if os.path.exists(lockKeyFile) :
        pub_str = redFile(pubKeyFile)
        sub_str = redFile(subKeyFile)
        gpgKey = pub_str + ":" + sub_str
        print("gpgKey %s from cache." % (gpgKey))
    else :
        while True:  # may be failed ,so do while
            execLines = os.popen("%s/gpg_gen_key.sh %s %s %s 2>&1" % (os.getcwd(), user, email, passphrase)).readlines()
            os.system("rm -rf ~/.gnupg/random_seed")
            for line in execLines :
                if line.startswith('pub   '):
                    keyVal = line.split("/")[1].split(" ")[0]
                    os.system("echo '%s' > %s" % (keyVal, pubKeyFile))
                elif line.startswith('sub   '):
                    keyVal = line.split("/")[1].split(" ")[0]
                    os.system("echo '%s' > %s" % (keyVal, subKeyFile))
            #
            if os.path.exists(pubKeyFile) and os.path.exists(subKeyFile):
                pub_str = redFile(pubKeyFile)
                sub_str = redFile(subKeyFile)
                gpgKey = pub_str + ":" + sub_str
                os.system("echo '' > " + lockKeyFile)
                break
            else:
                os.system("rm -rf " + lockKeyFile)
                os.system("rm -rf " + pubKeyFile)
                os.system("rm -rf " + subKeyFile)
            #
        print("gpgKey %s from gen." % (gpgKey))
    return gpgKey
#
#
#
userName = os.getenv('userName')
mail = os.getenv('mail')
passphrase = os.getenv('passphrase')
if len(sys.argv) == 4:
    userName = sys.argv[1]
    mail = sys.argv[2]
    passphrase = sys.argv[3]
if userName != None and mail != None and passphrase != None:
    print("userName: %s, mail: %s, passphrase: %s" % (userName , mail, passphrase))
    ck = initGPG(userName , mail, passphrase)
else:
    print("args error.")
#
