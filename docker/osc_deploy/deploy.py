#!/usr/bin/env python
# -*- coding: UTF－8 -*-

import datetime, time
from http.server import HTTPServer, BaseHTTPRequestHandler  
import io, shutil , urllib, cgi
import os, sys, pexpect, threading


#
#--------------------------------------------------------------------------------------------------------------------------------------------------------------
def writeFile(file, lines):
    fileObj = open(file, "a")
    try :
        fileObj.writelines(lines)
    finally:
        fileObj.close()
# - 读取文件所有内容
def readFile(file):
    body = ''
    if os.path.exists(file):
        print("reading file ->" + file)
        fileObj = open(file)
        try :
            body = fileObj.read()
        finally:
            fileObj.close()
    else:
        print("file is not exists ->" + file)
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
#--------------------------------------------------------------------------------------------------------------------------------------------------------------
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
    print("gen2 key...")
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
#   - {git_host:'',git_mail:'',ssh_config:'',passphrase:''}
#   - 返回形式：{"html":bodyStr, "result":False}
def gpg_config(params):
    gpg_email = params.get("gpg_email")[0] if params.get("gpg_email") != None else "deployer@hasor.net"
    gpg_config = params.get("gpg_config")[0] if params.get("gpg_config") != None else False
    gpg_config = True if gpg_config == True or gpg_config == "true" or gpg_config == "True" else False
    passphrase = params.get("passphrase")[0] if params.get("passphrase") != None else "123456"
    #
    keyData = gpg_findKey(gpg_email)
    if keyData == None:
        bodyStr = "<form action='/gpg_config.do' method='GET'>\
        <div><b><font color='#FF0000'>failed</font> - (Step 1 of 2)</b> init Gnupg Key <hr/></div>\
        <label><span>gpg_email</span>" + gpg_email + "</label>\
        <label><span>passphrase</span>" + passphrase + "</label>\
        <input name='gpg_config' type='hidden' value='true'><input type='submit' value='config Gnupg'>\
        <br/><hr />\
        </form>"
        if gpg_config == False:
            return {"html":bodyStr, "result":False}
        else:
            try :
                userName = gpg_email.split("@")[0]
                if len(userName) < 5:
                    userName = "gpgAuto_" + userName
                print("gen gpgkey ,userName: %s, mail: %s, passphrase: %s" % (userName , gpg_email, passphrase))
                os_system("dd if=/dev/urandom of=~/.gnupg/random_seed bs=1M count=1")
                keyData = gpg_genKey(userName, gpg_email, passphrase)
                if keyData != None:
                    gpg_sendKey(keyData["pubKey"])
                    print("gpg genkey success.")
            finally:
                os_system("rm -rf ~/.gnupg/random_seed")
    #
    if keyData != None:
        print("gen gpgKey %s from local." % (keyData["pubKey"]))
        bodyStr = "<div><b><font color='#00FF00'>success</font> - (Step 2 of 2)</b> Gnupg Key <hr/></div>\
        <label><span>Key</span>" + keyData["pubKey"] + "</label>\
        <label><span>passphrase</span>" + passphrase + "</label>\
        <br/><hr/>"
        return {"html":bodyStr, "result":True}
    else:
        return {"html":bodyStr, "result":False}
#
#--------------------------------------------------------------------------------------------------------------------------------------------------------------
# 获取当前ssh公钥“id_rsa.pub”储存的内容。
#   - 返回形式：{email:xxxx , pubKey: xxxx}，失败返回 None
def ssh_pub():
    ssh_pub_file = os.path.expanduser('~') + '/.ssh/id_rsa.pub'
    rsapub = readFile(ssh_pub_file)
    print(rsapub)
    rsapubSplit = rsapub.strip().split(" ")
    if len(rsapubSplit) >= 2:
        print("public key of email " + rsapubSplit[2])
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
# 测试SSH联通性，例子：git@git.oschina.net
#   - 返回形式：True or False
def ssh_test(sshHost, passphrase):
    cmdStr = "ssh -T git@%s" % (sshHost)
    print(cmdStr)
    if sshHost == "" or sshHost == None:
        return False
    child = pexpect.spawnu(cmdStr)
    child.logfile = sys.stdout
    child.delaybeforesend = 0.1
    child.timeout = 10
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
        elif index == 2:
            child.sendline("yes")
        elif index == 3 or index == 4 or index == 5:
            print("\nplease add the public key.")
            exeStatus = False
            if index != 5:
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
# 检测ssh连通性并返回相应的html代码，ssh_config({git_host:'',git_mail:'',ssh_config:'',passphrase:''})
#   - 返回形式：{"html":bodyStr, "result":"False"}
def ssh_config(params):
    git_host = params.get("git_host")[0] if params.get("git_host") != None else ""
    git_mail = params.get("git_mail")[0] if params.get("git_mail") != None else "deployer@hasor.net"
    ssh_config = params.get("ssh_config")[0] if params.get("ssh_config") != None else False
    ssh_config = True if ssh_config == True or ssh_config == "true" or ssh_config == "True" else False
    passphrase = params.get("passphrase")[0] if params.get("passphrase") != None else "123456"
    #
    sshKey = ssh_pub()
    if sshKey == None:
        bodyStr = "<form action='/ssh_config.do' method='GET'>\
<div><b><font color='#FF0000'>failed</font> - (Step 1 of 3)</b> please gen SSH Public Key<hr/></div>\
<label><span>git_host:</span>" + git_host + "</label>\
<label><span>git_mail:</span><input name='git_mail' type='text' value='" + git_mail + "'></label>\
<label><span>passphrase:</span>" + passphrase + "</label>\
<input name='ssh_config' type='hidden' value='true'><input type='submit' value='config SSH'>\
<br/><hr />\
</form>"
        if (ssh_config == False):
            return {"html":bodyStr, "result":False}
        else:
            sshKey = ssh_gen(git_mail, passphrase)
    #
    if ssh_config != None and ssh_config == True and sshKey["email"] != git_mail:
        sshKey = ssh_gen(git_mail, passphrase)
    #
    if ssh_test(git_host, passphrase) == False:
        bodyStr = "<form action='/ssh_config.do' method='GET'>\
<div><b><font color='#FF0000'>failed</font> - (Step 2 of 3)</b> please add Public Key to git host :" + git_host + "<hr/>\
  <div style='background-color:#FFFFcc'>" + sshKey["pubKey"] + "</div>\
</div>\
<label><span>git_mail:</span>" + sshKey["email"] + "</label>\
<label><span>git_host:</span>" + git_host + "</label>\
<br/><hr />\
<label><span>new mail:</span><input name='git_mail' type='text' value='" + git_mail + "'></label>\
<input name='ssh_config' type='hidden' value='true'><input type='submit' value='reset SSH'>\
</form>"
        return {"html":bodyStr, "result":False}
    else:
        bodyStr = "<div><b><font color='#00FF00'>success</font> - (Step 3 of 3)</b> SSH Public Key <hr/>\
        <div style='background-color:#FFFFcc'>" + sshKey["pubKey"] + "</div>\
        </div>\
        <label><span>passphrase:</span>" + passphrase + "</label><hr/>"
        return {"html":bodyStr, "result":True, "email": sshKey["email"]}
#
#--------------------------------------------------------------------------------------------------------------------------------------------------------------
#   - 返回形式：{"html":bodyStr, "result":"False"}
def mvn_config(params):
    maven_user = params.get("maven_user")[0] if params.get("maven_user") != None else "admin"
    maven_password = params.get("maven_password")[0] if params.get("maven_password") != None else "admin"
    mvn_config = params.get("mvn_config")[0] if params.get("mvn_config") != None else False
    mvn_config = True if mvn_config == True or mvn_config == "true" or mvn_config == "True" else False
    #
    ossrhStr = os_popen("cat $MAVEN_HOME/conf/settings.xml | grep '<id>ossrh</id>'").read()
    if ossrhStr.strip() == "" :
        bodyStr = "<form action='/mvn_config.do' method='GET'>\
<b><font color='#FF0000'>failed</font> - (Step 1 of 2)</b> maven user of ossrh<hr/>\
<label><span>maven_user:</span><input name='maven_user' type='text' value='" + maven_user + "'></label>\
<label><span>maven_password:</span><input name='maven_password' type='text' value='" + maven_password + "'></label>\
<input name='mvn_config' type='hidden' value='true'><input type='submit' value='config Maven'>\
<br/><hr /></form>"
        if mvn_config == False:
            return {"html":bodyStr, "result":False}
        else:
            print("write 'ossrh' to settings.xml")
            result = os_system("sed -i '/<\/servers>/i\<server><id>ossrh</id><username>" + maven_user + "</username><password>" + maven_password + "</password></server>' $MAVEN_HOME/conf/settings.xml")
            if result != 0:
                print("write 'ossrh' to settings.xml -> failed ,exit code -> " + result)
                bodyStr
    else:
        print("do not write ossrh.")
    # mirror
    mirrorStr = os_popen("cat $MAVEN_HOME/conf/settings.xml | grep '<id>nexus-osc</id>'").read()
    if mirrorStr.strip() == "" :
        print("write 'mirror' to settings.xml")
        appendStr = "<mirror><id>nexus-osc</id><mirrorOf>*</mirrorOf><name>Nexus osc</name><url>http://maven.oschina.net/content/groups/public/</url></mirror>"
        result = os_system("sed -i '/<\/mirrors>/i\%s' $MAVEN_HOME/conf/settings.xml" % (appendStr))
        if result != 0:
            print("write 'mirror' to settings.xml -> failed ,exit code -> " + result)
    else:
        print("do not write mirror.")
    #
    bodyStr = "<b><font color='#00FF00'>success</font> - (Step 2 of 2)</b> maven user of ossrh<hr/>\
    <label><span>ossrhStr:</span>" + ossrhStr + "</label>\
    <label><span>mirrorStr:</span>" + mirrorStr + "</label><hr/>"
    return {"html":bodyStr, "result":True}
#
#--------------------------------------------------------------------------------------------------------------------------------------------------------------
#
def git_list():
    workPath = os_popen("echo $WORK_HOME").read()
    works = os.listdir(workPath.split("\n")[0])
    listBody = "Git Work Space"
    for item in works:
        listBody = listBody + ("<br/>&nbsp;&nbsp;&nbsp;&nbsp;<a href='/log.do?logName=" + item + "' target='_blank'>" + item + "</a>")
    return listBody + "<hr/>"
#
# 克隆远程版本库，并且签出指定分支。
#   - 返回形式：成功之返回源码路径否则返回 None
def git_clone(params):
    workPath = os_popen("echo $WORK_HOME").read()
    workPath = workPath.split("\n")[0]
    workPath = workPath + "/" + datetime.datetime.now().strftime("%Y%m%d-%H%M%S-%f")
    print("clone to ‘" + workPath + "’")
    #
    git_name = params["git_name"]
    git_mail = params["git_mail"]
    git_branch = params["git_branch"]
    git_repository = params["git_repository"]
    #
    if git_name == "" or git_branch == "" or git_repository == "":
        return False
    resultCode = os_system('git clone --branch %s --progress -v --depth 1 "%s" "%s"' % (git_branch, git_repository, workPath))
    if resultCode != 0:
        print("clone error -> %s" % (resultCode))
        return False
    os_system('git config --global user.name "%s"' % (git_name))
    os_system('git config --global user.email %s' % (git_mail))
    return workPath
#
#
#--------------------------------------------------------------------------------------------------------------------------------------------------------------
def run_server(server_port):
    class DeployServerHandler(BaseHTTPRequestHandler):
        #----------------------------------------
        def doResponse(self, htmlBody="", enc="UTF-8"):
            htmlBody = '<html>\
 <style type="text/css">\
 label { display: block; padding: 3px 3px }\
 span  { display: block; float:left; width: 100px;}\
 input { display: block; flout:left; width: 80%%;}\
 * {word-wrap:break-word;}\
 </style>' + htmlBody + '</html>'
            encoded = "".join(htmlBody).encode(enc)
            #
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
        def do_POST(self):
            do_GET(self)
        #----------------------------------------
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
            params = urllib.parse.parse_qs(queryString)
            params["git_host"] = ["git.oschina.net"]
            #
            # －－－－Log－－－－
            if requestURI == "/log.do":
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
            #
            # －－－－发布－－－－
            if requestURI == "/deploy.do":
                shellPath = os.path.split(os.path.realpath(__file__))[0]
                logName = datetime.datetime.now().strftime("%Y%m%d-%H%M%S-%f");
                writeFileName = "%s/logs/deploy.%s.log" % (shellPath, logName)
                shellArgs = ""
                for arg in params:
                    shellArgs = shellArgs + ' -%s="%s"' % (arg, params[arg][0])
                print ("deploy args -> " + shellArgs)
                #
                def foo(shellPath, shellArgs):
                    os_system("mkdir %s/logs/" % (shellPath))
                    console = os_popen("%s/deploy.py deploy %s > %s 2>&1" % (shellPath, shellArgs, writeFileName))
                self.send_response(200)
                self.send_header("Content-type", "text/html; charset=%s" % enc)
                self.end_headers()
                self.wfile.write(("<a href='/log.do?logName=%s'>点击查看日志</a>" % (logName)).encode())
                #
                workThread = threading.Thread(target=foo, args=(shellPath, shellArgs))
                workThread.start()
                return
            #
            # －－－－首页－－－－
            # config ssh
            sshInfo = ssh_config(params)
            if sshInfo["result"] == False:
                self.doResponse(sshInfo["html"])
                return
            # config gpg
            params["gpg_email"] = [sshInfo["email"]] 
            gpgInfo = gpg_config(params)
            if gpgInfo["result"] == False:
                self.doResponse(sshInfo["html"] + "<br/>" + gpgInfo["html"])
            # config maven
            mvnInfo = mvn_config(params)
            self.doResponse(sshInfo["html"] + "<br/>" + gpgInfo["html"] + "<br/>" + mvnInfo["html"] + "<br/>" + git_list())
            return
        #----------------------------------------
    httpd = HTTPServer(("", int(server_port)), DeployServerHandler)
    print("deploy Server started on 127.0.0.1,port %s....." % (server_port))
    httpd.serve_forever()
#
#--------------------------------------------------------------------------------------------------------------------------------------------------------------
#
#
def deploy():
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
#
#--------------------------------------------------------------------------------------------------------------------------------------------------------------
#
#
#
#

if sys.argv[1] == "server":
    run_server(7001)

if sys.argv[1] == "clone":
    sshKey = ssh_pub()
    if sshKey == None:
        print("clone failed. -> ssh need config.")
    else:
        email = sshKey["email"]
        params = {}
        params["git_name"] = email.split("@")[0]
        params["git_mail"] = email
        params["git_branch"] = "master"
        params["git_repository"] = sys.argv[2]
        targetPath = git_clone(params)
        print("clone ok. ->" + targetPath)

if sys.argv[1] == "deploy":
# os_onpath(workPath, deploy)    
    print("deploy")

