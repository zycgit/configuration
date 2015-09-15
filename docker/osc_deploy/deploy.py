#!/usr/bin/env python
import datetime, time
from http.server import HTTPServer, BaseHTTPRequestHandler  
import io, shutil , urllib
import os, sys
#
#-------------------------------------------------------------------
#
def readFile(file):
    body = ''
    file_pub = open(file)
    try :
        body = file_pub.read()
    finally:
        file_pub.close()
    return body.strip()
#
def os_system(cmd):
    print(cmd)
    return os.system(cmd)
#
def os_popen(cmd):
    print(cmd)
    return os.popen(cmd)
#
def onPath(workDir, function):
    currentDir = os.getcwd()
    os.chdir(workDir)
    result = function()
    os.chdir(currentDir)
    return result
#
def args(name, defaultVal):
    argvLen = len(sys.argv)
    result = None
    for i in range(1, argvLen):
        val = sys.argv[i]
        if val.startswith("-" + name):
            index = val.find("=")
            if index > 0:
                val = val[index + 1:]
                result = val.strip()
    if result == None or result.strip() == "":
        result = os.getenv(name)
    if result == None or result.strip() == "":
        result = defaultVal
    return result
#
def chooseVal(val_a, val_b):
    if val_a == None or val_a == "" :
        return val_b
    return val_a
#
#-------------------------------------------------------------------
#
def gpg_foreachKey(function):
    lines = os_popen("gpg -k | grep 'pub '").readlines()
    for value in lines:
        keyVal = value.split("/")[1].split(" ")[0]
        result = function(keyVal)
        if result != None:
            return result
    return None
#
def gpg_inKey(keyVal, function):
    def foo(key):
        if key == keyVal:
            return function(key)
    return gpg_foreachKey(foo)
#
def gpg_recvKey(keyVal):
    def foo(key):
        execResult = os_popen("gpg --keyserver hkp://pool.sks-keyservers.net --recv-keys %s 2>&1 | grep 'Total number'" % (keyVal)).read()
        execResult = execResult.split(":")[2].strip()
        if execResult == "1" :
            return True
        elif execResult == "0":
            return False
        else:
            return False
    return gpg_inKey(keyVal, foo)
#
def gpg_sendKey(keyVal):
    def foo(key):
        execResult = os_system("gpg --keyserver hkp://pool.sks-keyservers.net --send-keys %s" % (keyVal))
        if execResult == 0 :
            print("the key %s has been push." % (key))
            return True
        else:
            print("the key %s has error , exit code = %s" % (key, execResult))
            return False
    return gpg_inKey(keyVal, foo)
#
def gpg_init(user, email, passphrase):
    gpgKey = ""
    lockKeyFile = "/script/key.lock"
    pubKeyFile = "/script/key.pub"
    subKeyFile = "/script/key.sub"
    if os.path.exists("/script") == False:
        os.makedirs(r"/script")
    if os.path.exists(lockKeyFile) :
        pub_str = readFile(pubKeyFile)
        sub_str = readFile(subKeyFile)
        gpgKey = pub_str + ":" + sub_str
        print("gpgKey %s from cache." % (gpgKey))
    else :
        print("userName: %s, mail: %s, passphrase: %s" % (user , email, passphrase))
        tryCount = 0
        maxTryCount = 10
        while True:  # may be failed ,so do while
            tryCount = tryCount + 1
            if tryCount >= maxTryCount:
                print("gen gpgkeys failed, maximizing 10.")
                exit(1)
            #
            os_system("dd if=/dev/urandom of=~/.gnupg/random_seed bs=1M count=1")
            shellPath = os.path.split(os.path.realpath(__file__))[0]
            execLines = os_popen("%s/gpg_gen_key.sh %s %s %s" % (shellPath, user, email, passphrase)).readlines()
            os_system("rm -rf ~/.gnupg/random_seed")
            print("process result...")
            for line in execLines :
                if line.startswith("pub   "):
                    keyVal = line.split("/")[1].split(" ")[0]
                    os_system("echo '%s' > %s" % (keyVal, pubKeyFile))
                elif line.startswith('sub   '):
                    keyVal = line.split("/")[1].split(" ")[0]
                    os_system("echo '%s' > %s" % (keyVal, subKeyFile))
            #
            if os.path.exists(pubKeyFile) and os.path.exists(subKeyFile):
                pub_str = readFile(pubKeyFile)
                sub_str = readFile(subKeyFile)
                gpgKey = pub_str + ":" + sub_str
                os_system("echo '' > " + lockKeyFile)
                print("gen gnupg ok.")
                gpg_sendKey(pub_str)  # push to server
                break
            else:
                os_system("rm -rf " + lockKeyFile)
                os_system("rm -rf " + pubKeyFile)
                os_system("rm -rf " + subKeyFile)
                print("gen gnupg failed , try again.")
                print("3.")
                time.sleep(1)
                print("2.")
                time.sleep(1)
                print("1.")
                time.sleep(1)
            #
        print("gpgKey %s from gen." % (gpgKey))
    return gpgKey
#
#-------------------------------------------------------------------
#
def git_project(workDir, git_name, git_mail, git_account, git_pwd):
    def foo():
        os_system('git config user.name "%s"' % (git_name))
        os_system('git config user.email %s' % (git_mail))
        os_system('git config credential.helper store')
        #
        creditsStr = "https://%s:%s@git.oschina.net" % (git_account, git_pwd)
        credentialsStr = os_popen("cat ~/.git-credentials | grep '%s'" % (creditsStr)).read()
        if credentialsStr.strip() == "" :
            os_system('echo "%s" > ~/.git-credentials' % (creditsStr,))
        else:
            print("do not write git-credentials.")
    return onPath(workDir, foo)
#
def git_clone(git_branch, git_repository, git_name, git_mail, git_account, git_pwd):
    WORK_HOME = args("WORK_HOME" , None)
    if WORK_HOME == None or WORK_HOME.strip() == "" :
        WORK_HOME = os.path.expanduser("~") + "/work"
    workDir = WORK_HOME + "/" + datetime.datetime.now().strftime("%Y%m%d-%H%M%S-%f")
    print("workDir at -> " + workDir)
    #
    result = os_system('git clone --branch %s --progress -v "%s" "%s"' % (git_branch, git_repository, workDir))
    if result != 0:
        print("clone error -> " + result)
        exit(result)  # exit
    #
    git_project(workDir, git_name, git_mail, git_account, git_pwd)
    return workDir
#
def git_userInfo():
    randomUser = datetime.datetime.now().strftime("u%Y%m%d%H%M%S")
    randomMail = randomUser + "@t.hasor.net"
    git_name = args("user", randomUser)
    git_mail = args("mail", randomMail)
    return {"user":git_name, "mail":git_mail}
#
#-------------------------------------------------------------------
#
def mvn_config(maven_user, maven_pass):
    ossrhStr = os_popen("cat $MAVEN_HOME/conf/settings.xml | grep '<id>ossrh</id>'").read()
    if ossrhStr.strip() == "" :
        print("write 'ossrh' to settings.xml")
        os_system("sed -i '/<\/servers>/i\<server><id>ossrh</id><username>" + maven_user + "</username><password>" + maven_pass + "</password></server>' $MAVEN_HOME/conf/settings.xml")
    else:
        print("do not write maven user.")
    #
    ossrhStr = os_popen("cat $MAVEN_HOME/conf/settings.xml | grep '<id>nexus-osc</id>'").read()
    if ossrhStr.strip() == "" :
        print("write 'mirror' to settings.xml")
        appendStr = "<mirror><id>nexus-osc</id><mirrorOf>*</mirrorOf><name>Nexus osc</name><url>http://maven.oschina.net/content/groups/public/</url></mirror>"
        os_system("sed -i '/<\/mirrors>/i\%s' $MAVEN_HOME/conf/settings.xml" % (appendStr))
    else:
        print("do not write mirror.")
#
def mvn_deploy(workDir, passphrase):
    def foo():
        print("mvn clean release:clean release:prepare -P release -Dgpg.passphrase=" + passphrase)
        os.system("mvn_release.sh " + passphrase)
        os_system("mvn release:perform")
    return onPath(workDir, foo)
#
#-------------------------------------------------------------------
#
def runServer():
    class DeployServerHandler(BaseHTTPRequestHandler):
        def writeHTML(self, enc):
            userInfo = git_userInfo()
            git_name = userInfo["user"]
            git_mail = userInfo["mail"]
            git_branch = args("branch", "master")
            htmlBody = '\
<html>\
<style type="text/css">\
label { display: block; padding: 3px 3px }\
span  { display: block; float:left; width: 100px;}\
input { display: block; flout:left; width: 80%%;}\
</style>\
<form action="/request.do" method="GET">\
<label><span>mvn_user:</span><input id="mvn_user" name="mvn_user" type="text" value=""/></label>\
<label><span>mvn_pwd:</span><input id="mvn_pwd" name="mvn_pwd" type="text" value=""/></label>\
<hr/>\
<label><span>git_name:</span><input id="git_name" name="git_name" type="text" value="%s"/></label>\
<label><span>git_mail:</span><input id="git_mail" name="git_mail" type="text" value="%s"/></label>\
<label><span>git_user:</span><input id="git_user" name="git_user" type="text" value=""/></label>\
<label><span>git_pwd:</span><input id="git_pwd" name="git_pwd" type="text" value=""/></label>\
<hr/>\
<label><span>git_repo:</span><input id="git_repo" name="git_repo" type="text" value=""/></label>\
<label><span>git_branch:</span><input id="git_branch" name="git_branch" type="text" value="%s"/></label>\
<hr />\
<input type="submit" value="deploy">\
</form></html>' % (git_name, git_mail, git_branch)
            return "".join(htmlBody).encode(enc)
        #
        def do_POST(self):
            do_GET(self)
        #
        def do_GET(self):
            if self.path == "/favicon.ico":
                return
            enc = "UTF-8"
            requestURI = self.path.split("?")[0]
            queryString = self.path.split("?")[1]
            if requestURI == "/request.do"  :
                params = urllib.parse.parse_qs(queryString)     
                userInfo = git_userInfo()
                exeArgs = {}
                exeArgs["user"] = chooseVal(params["git_name"][0], args("git_name", userInfo["user"]))
                exeArgs["mail"] = chooseVal(params["git_mail"][0], args("git_mail", userInfo["mail"]))
                exeArgs["mvn_user"] = chooseVal(params["mvn_user"][0], args("mvn_user", "admin"))
                exeArgs["mvn_pwd"] = chooseVal(params["mvn_pwd"][0], args("mvn_pwd", ""))
                exeArgs["git_user"] = chooseVal(params["git_user"][0], "")
                exeArgs["git_pwd"] = chooseVal(params["git_pwd"][0], "")
                exeArgs["repo"] = chooseVal(params["git_repo"][0], "")
                exeArgs["branch"] = chooseVal(params["git_branch"][0], args("branch", "master"))
                shellArgs = ""
                for arg in exeArgs:
                    shellArgs = shellArgs + ' -%s="%s"' % (arg, exeArgs[arg])
                shellPath = os.path.split(os.path.realpath(__file__))[0]
                exe = os_popen("%s/deploy.py deploy %s" % (shellPath, shellArgs))
                #
                self.send_response(200)
                self.send_header("Content-type", "text/html; charset=%s" % enc)
                self.end_headers()
                #
                for line in exe:
                    self.wfile.write((line + "<br>").encode())
                #
            elif requestURI == "/status.do":
                return
            else:
                encoded = self.writeHTML(enc)
                f = io.BytesIO()
                f.write(encoded)
                f.seek(0)
                self.send_response(200)
                self.send_header("Content-type", "text/html; charset=%s" % enc)
                self.send_header("Content-Length", str(len(encoded)))
                self.end_headers()
                shutil.copyfileobj(f, self.wfile)
                return
    #
    httpd = HTTPServer(("", 8080), DeployServerHandler)
    print("deploy Server started on 127.0.0.1,port 8080.....")
    httpd.serve_forever()
#
#-------------------------------------------------------------------
#
if sys.argv[1] == "init":
    userInfo = git_userInfo()
    git_name = userInfo["user"]
    git_mail = userInfo["mail"]
    passphrase = args("passphrase", "123456")
    gpg_init(git_name, git_mail, passphrase)
    print("init finish.")
elif sys.argv[1] == "deploy":
    userInfo = git_userInfo()
    git_name = userInfo["user"]
    git_mail = userInfo["mail"]
    passphrase = args("passphrase", "123456")
    git_branch = args("branch", "master")
    git_repo = args("repo", "")
    git_account = args("git_user", "")
    git_pwd = args("git_pwd", "")
    maven_user = args("mvn_user", "admin")
    maven_pwd = args("mvn_pwd", "")
    gpg_init(git_name, git_mail, passphrase)
    workDir = git_clone(git_branch , git_repo, git_name, git_mail, git_account, git_pwd)
    mvn_config(maven_user, maven_pwd)
    mvn_deploy(workDir, passphrase)
    print("deploy finish.")
elif sys.argv[1] == "server":
    runServer()
else:
    print("args error.")
#
#
#
# env user=zyc
# env mail=zyc@hasor.net
# env mvn_user=zyc
# env mvn_pwd=
#
# deploy "-git_user=zycgit" "-git_pwd=password" "-branch=master" "-repo=https://git.oschina.net/zycgit/hasor-garbage.git"
# deploy "-git_user=zycgit" "-git_pwd=password" "-repo=https://git.oschina.net/zycgit/hasor-garbage.git”
