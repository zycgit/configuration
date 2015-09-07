#!/usr/bin/env python
import os
#
#
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
def checkKey(keyVal):
    def foo(key):
        print("check gpg key " + keyVal + " in server 'hkp://pool.sks-keyservers.net'...")
        execResult = os.popen("gpg --keyserver hkp://pool.sks-keyservers.net --recv-keys " + keyVal + " 2>&1 | grep 'Total number'").read()
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
        execResult = os.system("gpg --keyserver hkp://pool.sks-keyservers.net --send-keys " + keyVal)
        if execResult == 0 :
            print("the key " + key + " has been push.")
            return True
        else:
            print("the key " + key + " has error , exit code =" + execResult)
            return False
    return inKey(keyVal, foo)
#
#
#
#
ck = foreachKey(lambda key:sendKey(key))
print(ck)

# gpg --keyserver hkp://pool.sks-keyservers.net --send-keys C990D076
# gpg --keyserver hkp://pool.sks-keyservers.net --recv-keys C990D076
