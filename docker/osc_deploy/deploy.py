#!/usr/bin/env python
import datetime, time
from http.server import HTTPServer, BaseHTTPRequestHandler  
import io, shutil , urllib, cgi
import os, sys, pexpect, threading
#
#
global ARG_KEY_GIT_NAME
global ARG_KEY_GIT_MAIL
global ARG_KEY_GIT_PASSPHRASE
global ARG_KEY_GIT_USER
global ARG_KEY_GIT_PASSWORD
global ARG_KEY_GIT_BRANCH
global ARG_KEY_GIT_REPOSITORY
global ARG_KEY_GIT_SSH_HOST
global ARG_KEY_MAVEN_USER
global ARG_KEY_MAVEN_PASSWORD
global ARG_KEY_GIT_WORK_HOME
global ARG_KEY_SERVER_PORT
ARG_KEY_GIT_NAME = "name"
ARG_KEY_GIT_MAIL = "mail"
ARG_KEY_GIT_PASSPHRASE = "passphrase"
ARG_KEY_GIT_USER = "user"
ARG_KEY_GIT_PASSWORD = "pwd"
ARG_KEY_GIT_BRANCH = "branch"
ARG_KEY_GIT_REPOSITORY = "repo"
ARG_KEY_GIT_SSH_HOST = "ssh_host"
ARG_KEY_MAVEN_USER = "mvn_user"
ARG_KEY_MAVEN_PASSWORD = "mvn_pwd"
ARG_KEY_GIT_WORK_HOME = "WORK_HOME"
ARG_KEY_SERVER_PORT = "port"
#
#-------------------------------------------------------------------
#
# - 写入文件（追加）
def writeFile(file, lines):
    fileObj = open(file, "a")
    try :
        fileObj.writelines(lines)
    finally:
        fileObj.close()
# - 读取文件所有内容
def readFile(file):
    print("reading file ->" + file)
    body = ''
    fileObj = open(file)
    try :
        body = fileObj.read()
    finally:
        fileObj.close()
    return body.strip()
#
# - 调用“os.system”
def os_system(cmd):
    print(cmd)
    return os.system(cmd)
#
# - 调用“os.popen”
def os_popen(cmd):
    print(cmd)
    return os.popen(cmd)
# -将当前工作目录临时切换到指定的目录下执行，当方法执行完毕切换回来。
def os_onpath(workDir, function):
    currentDir = os.getcwd()
    os.chdir(workDir)
    result = function()
    os.chdir(currentDir)
    return result
# -首先从命令行参数中获取参数值，如果没有则从系统环境变量中取得。如果环境变量也没有配置，那么从第二个参数取得默认值（可以不传）。
def args(name, defaultVal=""):
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
# - 当第一个值为 None或 ""时候返回第二值。
def chooseVal(val_a, val_b):
    if val_a == None or val_a == "" :
        return val_b
    return val_a
#
#-------------------------------------------------------------------
#
# 调用“gpg -k”命令枚举已经生成的key找到匹配key的公钥和私钥ID
#   - 返回形式：{"pubKey":findPUB, "subKey":findSUB, "mail":userEmail}
def gpg_findKey(userEmail):
    findPUB = None
    findUID = None
    findSUB = None
    for line in os_popen("gpg -k").readlines():
        if line.startswith("pub"):
            findPUB = line.split("/")[1].split(" ")[0]
        elif line.startswith("uid"):
            findUID = line.split(" <")[1].split(">")[0]
        elif line.startswith("sub"):
            findSUB = line.split("/")[1].split(" ")[0]
        elif line.strip() == "" :
            findPUB = None
            findUID = None
            findSUB = None
        #
        if findPUB != None and findUID != None and findSUB != None and findUID == userEmail:
            print("%s:%s <%s>" % (findPUB, findSUB, findUID))
            return {"pubKey":findPUB, "subKey":findSUB, "mail":userEmail}
    return None
#
# 调用 “gpg --gen-key” 命令产生 gpg key，并返回key的公钥和私钥ID
#   - 返回形式：{"pubKey":findPUB, "subKey":findSUB, "mail":userEmail}
def gpg_genKey(userName, userEmail, passphrase):
    findResult = gpg_findKey(userEmail)
    if findResult != None:
        print("find key ->" + findResult["pubKey"])
        return findResult
    #
    print("gen key...")
    child = pexpect.spawnu("gpg", ["--gen-key"])
    child.logfile = sys.stdout
    child.delaybeforesend = 0.1
    expectList = ["Your selection?",
                "What keysize do you want?",
                "Key is valid for?",
                "Is this correct?",
                "Real name:",
                "Email address:",
                "Comment:",
                "Change",
                "Enter passphrase:",
                "Repeat passphrase:",
                pexpect.EOF,
                pexpect.TIMEOUT,
                "Name must be at least 5 characters long",
                "Name may not start with a digit",
                "Not a valid email address"]
    exeStatus = None
    while True:
        index = child.expect(expectList)
        if index == 0 or index == 1 or index == 2:
            child.sendline()
        elif index == 3:
            child.sendline("y")
        elif index == 4:
            child.sendline(userName)
        elif index == 5:
            child.sendline(userEmail)
        elif index == 6:
            child.sendline("auto gen key.")
        elif index == 7 :
            child.sendline("O")
        elif index == 8 or index == 9:
            child.sendline(passphrase)
        elif index == 10 or index == 11:
            if index == 10:
                print("gen finish , test result later.")
                exeStatus = True
            elif index == 11:
                print("gen failed , timeout.")
                exeStatus = False
            break
        else:
            print("failed --> " + expectList[index])
            exeStatus = False
            break
    #
    if child.isalive():
        print("gen process kill.")
        child.close(force=True)
    if exeStatus == False:
        return None
    # 查找刚刚生成的Key
    return gpg_findKey(userEmail)
#
# 通过公钥ID从“hkp://pool.sks-keyservers.net”上恢复公钥Key
#   - 返回形式：True or False
def gpg_recvPubKey(pubKey):
    for line in os_popen("gpg --keyserver hkp://pool.sks-keyservers.net --recv-keys " + pubKey).readlines():
        if line.find("Total number") > 0:
            line = line.split(":")[2].strip()
            if line == "1" :
                return True
    return False
#
# 将指定的公钥ID发布到“hkp://pool.sks-keyservers.net”服务器上
#   - 返回形式：True or False
def gpg_sendKey(keyVal):
    execResult = os_system("gpg --keyserver hkp://pool.sks-keyservers.net --send-keys %s" % (keyVal))
    if execResult == 0 :
        print("the key %s has been push." % (keyVal))
        print("check push...")
        return gpg_recvPubKey(keyVal)
    else:
        print("the key %s has error , exit code = %s" % (keyVal, execResult))
        return False
#
# 初始化gpg环境，如果不存在key则生成，并推送到“hkp://pool.sks-keyservers.net”服务器上
#   - 返回形式：True or False
def gpg_init(userName, userEmail, passphrase):
    keyData = gpg_findKey(userEmail)
    # form local
    if keyData != None:
        print("gen gpgKey %s from local." % (keyData["pubKey"]))
        return True
    # form gen
    try :
        if len(userName) < 5:
            userName = "gpgAuto_" + userName
        print("gen gpgkey ,userName: %s, mail: %s, passphrase: %s" % (userName , userEmail, passphrase))
        os_system("dd if=/dev/urandom of=~/.gnupg/random_seed bs=1M count=1")
        keyData = gpg_genKey(userName, userEmail, passphrase)
        # failed
        if keyData == None:
            print("gen gnupg failed.")
            return False
    finally:
        os_system("rm -rf ~/.gnupg/random_seed")
    # success
    gpg_sendKey(keyData["pubKey"])
    print("gpg genkey success.")
    return True
#
#-------------------------------------------------------------------
#
# 获取当前ssh公钥“id_rsa.pub”储存的内容。
#   - 返回形式：{email:xxxx , pubKey: xxxx}，失败返回 None
def ssh_pub():
    rsapub = os_popen("cat ~/.ssh/id_rsa.pub").read()
    rsapubSplit = rsapub.strip().split(" ")
    if len(rsapubSplit) == 2:
        return {"pubKey":rsapub, "email":rsapubSplit[2]}
    else:
        return None
#
# 生成公钥和私钥。
#   - 返回形式：{email:xxxx , pubKey: xxxx}，失败返回 None
def ssh_gen(git_mail, passphrase):
    sshKey = ssh_pub()
    if sshKey != None and sshKey["email"] == git_mail:
        print("ssh key form local.")
        return sshKey
    # gen sshkey
    cmdStr = "ssh-keygen -t rsa -C '%s'" % (git_mail)
    print(cmdStr)
    child = pexpect.spawnu(cmdStr)
    child.logfile = sys.stdout
    child.delaybeforesend = 0.1
    expectList = ["Enter file in which to save the key",
                "Enter passphrase",
                "Enter same passphrase again",
                "Overwrite (y/n)?",
                pexpect.EOF,
                pexpect.TIMEOUT]
    exeStatus = None
    while True:
        index = child.expect(expectList)
        if index == 0 :
            child.sendline()
        elif index == 1 or index == 2:
            child.sendline(passphrase)
        elif index == 3:
            child.sendline("y")
        elif index == 4 or index == 5:
            if index == 4:
                print("ssh-keygen finish.")
                exeStatus = True
            elif index == 5:
                print("ssh-keygen failed , timeout.")
                exeStatus = False
            break
    if child.isalive():
        print("ssh-keygen process kill.")
        child.close(force=True)
    if exeStatus != True:
        print("ssh-keygen failed.")
        return None;
    #
    return ssh_pub()
#
# 生成公钥和私钥。例子：git@git.oschina.net
#   - 返回形式：True or False
def ssh_test(sshHost, passphrase):
    cmdStr = "ssh -T %s" % (sshHost)
    print(cmdStr)
    child = pexpect.spawnu(cmdStr)
    child.logfile = sys.stdout
    child.delaybeforesend = 0.1
    expectList = ["Enter passphrase for key",
                "Welcome to",
                "Are you sure you want to continue connecting (yes/no)?",
                "Permission denied, please try again.",
                pexpect.EOF,
                pexpect.TIMEOUT]
    exeStatus = None
    while True:
        index = child.expect(expectList)
        if index == 0 :
            child.sendline(passphrase)
        elif index == 1:
            exeStatus = True
            break
        elif index == 2 or index == 3 or index == 4 or index == 5:
            print("\nplease add the public key.")
            exeStatus = False
            break
    if child.isalive():
        print("ssh process kill.")
        child.close(force=True)
    if exeStatus != True:
        print("ssh failed.")
        return False;
    #
    return True
#
#-------------------------------------------------------------------
#
# 克隆远程版本库，并且签出指定分支。
#   - 返回形式：成功之返回源码路径否则返回 None
def git_clone(git_workDir, git_branch, git_repository, git_name, git_mail, git_user, git_password):
    workDir = git_workDir + "/" + datetime.datetime.now().strftime("%Y%m%d-%H%M%S-%f")
    print("clone to ‘%s’" % (workDir))
    resultCode = os_system('git clone --branch %s --progress -v --depth 1 "%s" "%s"' % (git_branch, git_repository, workDir))
    if resultCode != 0:
        print("clone error -> %s" % (resultCode))
        return None
    # 配置项目
    os_system('git config --global user.name "%s"' % (git_name))
    os_system('git config --global user.email %s' % (git_mail))
    return workDir
#
#
#
def mvn_deploy(maven_user, maven_password, workDir, passphrase, git_mail, sshHost):
    # server
    ossrhStr = os_popen("cat $MAVEN_HOME/conf/settings.xml | grep '<id>ossrh</id>'").read()
    if ossrhStr.strip() == "" :
        print("write 'ossrh' to settings.xml")
        result = os_system("sed -i '/<\/servers>/i\<server><id>ossrh</id><username>" + maven_user + "</username><password>" + maven_password + "</password></server>' $MAVEN_HOME/conf/settings.xml")
        if result != 0:
            print("write 'ossrh' to settings.xml -> failed ,exit code -> %s" % (result))
            return result;
    else:
        print("do not write maven user.")
    # mirror
    ossrhStr = os_popen("cat $MAVEN_HOME/conf/settings.xml | grep '<id>nexus-osc</id>'").read()
    if ossrhStr.strip() == "" :
        print("write 'mirror' to settings.xml")
        appendStr = "<mirror><id>nexus-osc</id><mirrorOf>*</mirrorOf><name>Nexus osc</name><url>http://maven.oschina.net/content/groups/public/</url></mirror>"
        result = os_system("sed -i '/<\/mirrors>/i\%s' $MAVEN_HOME/conf/settings.xml" % (appendStr))
        if result != 0:
            print("write 'mirror' to settings.xml -> failed ,exit code -> %s" % (result))
    else:
        print("do not write mirror.")
    #
    def deploy():
        # 检查公钥
        if ssh_test(sshHost, passphrase) == False:
            print("ssh -T %s , test failed." % (sshHost))
            print("Please add the public key to %s" % (sshHost))
            return 1
        # deploy
        cmdStr = "mvn clean release:clean release:prepare -P release -Dgpg.passphrase=" + passphrase
        print(cmdStr)
        child = pexpect.spawnu(cmdStr)
        child.logfile = sys.stdout
        child.delaybeforesend = 0.1
        expectList = ["What is the release version for",
                    "What is SCM release tag or label for",
                    "What is the new development version for",
                    "Are you sure you want to continue connecting (yes/no)?",
                    "Enter passphrase for key",
                    pexpect.EOF,
                    pexpect.TIMEOUT]
        exeStatus = None
        while True:
            index = child.expect(expectList)
            if index == 0 or index == 1 or index == 2:
                child.sendline()
            elif index == 3:
                child.sendline("yes")
            elif index == 4:
                child.sendline(passphrase)
            elif index == 5 or index == 6:
                if index == 5:
                    print("release:prepare finish.")
                    exeStatus = True
                elif index == 6:
                    continue
                break
        #
        if child.isalive():
            print("mvn process kill.")
            child.close(force=True)
        if exeStatus != True:
            print("deploy on release:prepare failed.")
            return 1;
        #
        result = os_system("mvn release:perform")
        if result != 0:
            print("deploy on release:perform failed ,exit code -> %s" % (result))
            return result;
        return 0
    return os_onpath(workDir, deploy)
#
#-------------------------------------------------------------------
#
# -生成随机用户和email
def run_genUserInfo():
    randomUser = datetime.datetime.now().strftime("u%Y%m%d%H%M%S")
    randomMail = randomUser + "@t.hasor.net"
    git_name = args("user", randomUser)
    git_mail = args("mail", randomMail)
    return {"user":git_name, "mail":git_mail}
#
# －检测ssh连通性并返回相应的html代码。
def run_configSSH():
    ssh_host = args(ARG_KEY_GIT_SSH_HOST)
    passphrase = args(ARG_KEY_GIT_PASSPHRASE, "123456")
    git_mail = args(ARG_KEY_GIT_MAIL, run_genUserInfo()["mail"])
    sshKey = ssh_pub()
    if sshKey == None:
        sshKey = ssh_gen(git_mail, passphrase)
    if ssh_test(ssh_host, passphrase) == False:
        bodyStr = '<form action="#" method="GET">\
<div>(status:no, please add public key) SSH Public Key<hr/>%s</div>\
<hr />\
<label><span>ssh_mail:</span><input name="ssh_mail" type="text" value="' + sshKey["email"] + '"/></label>\
<label><span>ssh_host:</span><input name="' + ARG_KEY_GIT_SSH_HOST + '" type="text" value="' + ssh_host + '"/></label>\
<input type="submit" value="resetSSH">\
</form>' % (sshKey["pubKey"])
        return {"html":bodyStr, "result":False}
    else:
        bodyStr = "<div>(status:ok) SSH Public Key <hr/>%s</div>" % (sshKey["pubKey"])
        return {"html":bodyStr, "result":True}
#
# －deploy的form代码。
def run_deployForm():
    maven_user = args(ARG_KEY_MAVEN_USER, "admin")
    maven_password = args(ARG_KEY_MAVEN_PASSWORD)
    git_user = args(ARG_KEY_GIT_USER)
    git_password = args(ARG_KEY_GIT_PASSWORD)
    git_branch = args(ARG_KEY_GIT_BRANCH, "master")
    git_repository = args(ARG_KEY_GIT_REPOSITORY)
    sshKey = ssh_pub()
    bodyStr = '\
<form action="/request.do" method="GET">\
<label><span>mvn_user:</span><input name="mvn_user" type="text" value="' + maven_user + '"/></label>\
<label><span>mvn_pwd:</span><input name="mvn_pwd" type="text" value="' + maven_password + '"/></label>\
<hr/>\
<label><span>git_name:</span>' + sshKey["email"].split("@")[0] + '</label>\
<label><span>git_mail:</span>' + sshKey["email"] + '</label>\
<label><span>git_user:</span><input name="' + ARG_KEY_GIT_USER + '" type="text" value="' + git_user + '"/></label>\
<label><span>git_pwd:</span><input name="' + ARG_KEY_GIT_PASSWORD + '" type="text" value="' + git_password + '"/></label>\
<hr/>\
<label><span>git_branch:</span><input name="' + ARG_KEY_GIT_BRANCH + '" type="text" value="' + git_branch + '"/></label>\
<label><span>git_repo:</span><input name="' + ARG_KEY_GIT_REPOSITORY + '" type="text" value="' + git_repository + '"/></label>\
<hr />\
<input type="submit" value="deploy">\
</form>'
    return bodyStr
#
def run_server(server_port):
    class DeployServerHandler(BaseHTTPRequestHandler):
        def writeHTML(self, enc):
            htmlBody = '\
<html>\
<style type="text/css">\
label { display: block; padding: 3px 3px }\
span  { display: block; float:left; width: 100px;}\
input { display: block; flout:left; width: 80%%;}\
</style>' + run_configSSH() + "<hr/>" + run_deployForm() + '</html>'
            return "".join(htmlBody).encode(enc)
        #---------------------------------------------------------------------------------
        def do_POST(self):
            do_GET(self)
        #---------------------------------------------------------------------------------
        def do_GET(self):
            if self.path == "/favicon.ico":
                return
            enc = "UTF-8"
            arrayData = self.path.split("?")
            requestURI = arrayData[0]
            queryString = ""
            if len(arrayData) > 1:
                requestURI = arrayData[0]
                queryString = arrayData[1]
            #
            if requestURI == "/request.do"  :
                params = urllib.parse.parse_qs(queryString)
                userInfo = run_genUserInfo()
                radnomUserName = userInfo["user"]
                radnomUserMail = userInfo["mail"]
                exeArgs = {}
                exeArgs[ARG_KEY_GIT_NAME] = chooseVal(params[ARG_KEY_GIT_NAME][0], args(ARG_KEY_GIT_NAME, radnomUserName))
                exeArgs[ARG_KEY_GIT_MAIL] = chooseVal(params[ARG_KEY_GIT_MAIL][0], args(ARG_KEY_GIT_MAIL, radnomUserMail))
                exeArgs[ARG_KEY_GIT_USER] = chooseVal(params[ARG_KEY_GIT_USER][0], args(ARG_KEY_GIT_USER))
                exeArgs[ARG_KEY_GIT_PASSWORD] = chooseVal(params[ARG_KEY_GIT_PASSWORD][0], args(ARG_KEY_GIT_PASSWORD))
                exeArgs[ARG_KEY_GIT_BRANCH] = chooseVal(params[ARG_KEY_GIT_BRANCH][0], args(ARG_KEY_GIT_BRANCH, "master"))
                exeArgs[ARG_KEY_GIT_REPOSITORY] = chooseVal(params[ARG_KEY_GIT_REPOSITORY][0], args(ARG_KEY_GIT_REPOSITORY))
                exeArgs[ARG_KEY_MAVEN_USER] = chooseVal(params[ARG_KEY_MAVEN_USER][0], args(ARG_KEY_MAVEN_USER, "admin"))
                exeArgs[ARG_KEY_MAVEN_PASSWORD] = chooseVal(params[ARG_KEY_MAVEN_PASSWORD][0], args(ARG_KEY_MAVEN_PASSWORD))
                exeArgs[ARG_KEY_GIT_SSH_HOST] = chooseVal(params[ARG_KEY_GIT_SSH_HOST][0], args(ARG_KEY_GIT_SSH_HOST))
                #
                shellArgs = ""
                for arg in exeArgs:
                    shellArgs = shellArgs + ' -%s="%s"' % (arg, exeArgs[arg])
                shellPath = os.path.split(os.path.realpath(__file__))[0]
                logName = datetime.datetime.now().strftime("%Y%m%d%H%M%S")
                writeFileName = "%s/logs/deploy.%s.log" % (shellPath, logName)
                #
                def foo(shellPath, shellArgs):
                    os_system("mkdir %s/logs/" % (shellPath))
                    console = os_popen("%s/deploy.py deploy %s > %s 2>&1" % (shellPath, shellArgs, writeFileName))
                #
                workThread = threading.Thread(target=foo, args=(shellPath, shellArgs))
                workThread.start()
                #
                self.send_response(200)
                self.send_header("Content-type", "text/html; charset=%s" % enc)
                self.end_headers()
                self.wfile.write(("<a href='/status.do?logName=%s'>点击查看日志</a>" % (logName)).encode())
                return
                #
            elif requestURI == "/status.do":
                params = urllib.parse.parse_qs(queryString)
                logName = params["logName"][0]
                self.send_response(200)
                self.send_header("Content-type", "text/html; charset=%s" % enc)
                self.end_headers()
                #
                shellPath = os.path.split(os.path.realpath(__file__))[0]
                readFileName = "%s/logs/deploy.%s.log" % (shellPath, logName)
                logBody = readFile(readFileName)
                for line in logBody.split("\n"):
                    self.wfile.write((cgi.escape(line) + "<br>").encode())
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
        #---------------------------------------------------------------------------------
    httpd = HTTPServer(("", int(server_port)), DeployServerHandler)
    print("deploy Server started on 127.0.0.1,port %s....." % (server_port))
    httpd.serve_forever()
#
#-------------------------------------------------------------------
#
if sys.argv[1] == "deploy":
    userInfo = run_genUserInfo()
    radnomUserName = userInfo["user"]
    radnomUserMail = userInfo["mail"]
    git_name = args(ARG_KEY_GIT_NAME, radnomUserName)
    git_mail = args(ARG_KEY_GIT_MAIL, radnomUserMail)
    passphrase = args(ARG_KEY_GIT_PASSPHRASE, "123456")
    git_user = args(ARG_KEY_GIT_USER)
    git_password = args(ARG_KEY_GIT_PASSWORD)
    git_branch = args(ARG_KEY_GIT_BRANCH, "master")
    git_repository = args(ARG_KEY_GIT_REPOSITORY)
    maven_user = args(ARG_KEY_MAVEN_USER, "admin")
    maven_password = args(ARG_KEY_MAVEN_PASSWORD)
    git_sshHost = args(ARG_KEY_GIT_SSH_HOST, "git@git.oschina.net")
    git_workDir = args(ARG_KEY_GIT_WORK_HOME)
    if git_workDir == None or git_workDir.strip() == "" :
        git_workDir = os.path.expanduser("~") + "/work"
    #
    if gpg_init(git_name, git_mail, passphrase) != True:
        print("--> gpg_init failed.")
        exit(1)
    workDir = git_clone(git_workDir, git_branch , git_repository, git_name , git_mail, git_user , git_password)
    if workDir == None:
        print("--> git_clone '%s' failed." % (git_repository))
        exit(2)
    resultCode = mvn_deploy(maven_user, maven_password, workDir, passphrase, git_mail, git_sshHost)
    if resultCode != 0:
        print("--> mvn_deploy '%s' failed." % (workDir))
        exit(3)
    print("deploy finish.")
elif sys.argv[1] == "server":
    server_port = args(ARG_KEY_SERVER_PORT, "7001")
    run_server(server_port)
else:
    print("args error.")
#
#
#
# ARG_KEY_GIT_NAME = "name"
# ARG_KEY_GIT_MAIL = "mail"
# ARG_KEY_GIT_PASSPHRASE = "passphrase"
# ARG_KEY_GIT_USER = "user"
# ARG_KEY_GIT_PASSWORD = "pwd"
# ARG_KEY_GIT_BRANCH = "branch"
# ARG_KEY_GIT_REPOSITORY = "repo"
# ARG_KEY_MAVEN_USER = "mvn_user"
# ARG_KEY_MAVEN_PASSWORD = "mvn_pwd"
# ARG_KEY_GIT_WORK_HOME = "WORK_HOME"
# ARG_KEY_SERVER_PORT = "port"
#
# ENV passphrase "123456"
# ENV mvn_user "admin"
# ENV mvn_pwd ""
# ENV branch "master"
# ENV ssh_host "git@git.oschina.net"
#
# deploy server -port=7001
# deploy deploy "-name=zyc" "-mail=zyc@hasor.net" "-user=zycgit" "-pwd=password" "-repo=https://git.oschina.net/zycgit/hasor.git"
# deploy deploy "-name=zyc" "-mail=zyc@hasor.net" "-user=zycgit" "-pwd=password" "-repo=https://git.oschina.net/zycgit/hasor.git" "-branch=master"
#
