#!/usr/bin/env python
import datetime
import os
import sys
#
def git_swapath(workDir, function):
    currentDir = os.getcwd()
    os.chdir(workDir)
    result = function()
    os.chdir(currentDir)
    return result
#
def git_clone(config):
    git_name = config["git_name"]
    git_mail = config["git_mail"]
    git_pwd = config["git_pwd"]
    git_branch = config["git_branch"]
    git_repository = config["git_repository"]
    os.system('init_gnupg.py "%s" "%s" "%s"' % (git_name, git_mail, git_pwd))
    if git_name != None and git_mail != None :
        print("git_name: %s, git_mail: %s" % (git_name , git_mail))
    #
    workDir = os.getenv('WORK_HOME') + "/" + datetime.datetime.now().strftime("%Y%m%d-%H%M%S-%f")
    print("workDir at -> " + workDir)
    result = os.system('git clone --branch %s --progress -v "%s" "%s"' % (git_branch, git_repository, workDir))
    if result != 0:
        print("clone error -> " + result)
        exit(result)  # exit
    return workDir
#
def git_configProject(workDir, config):
    git_name = config["git_name"]
    git_mail = config["git_mail"]
    git_account = config["git_account"]
    git_pwd = config["git_pwd"]
    def foo():
        os.system('git config user.name "%s"' % (git_name))
        os.system('git config user.email %s' % (git_mail))
        os.system('git config credential.helper store')
        #
        creditsStr = "https://%s:%s@git.oschina.net" % (git_account, git_pwd)
        credentialsStr = os.popen("cat ~/.git-credentials | grep '%s'" % (creditsStr)).read()
        if credentialsStr.strip() == "" :
            os.system('echo "%s" > ~/.git-credentials' % (creditsStr,))
    return git_swapath(workDir, foo)
#
def git_configMaven(config):
    ossrhStr = os.popen("cat $MAVEN_HOME/conf/settings.xml | grep '<id>ossrh</id>'").read()
    if ossrhStr.strip() == "" :
        maven_user = os.getenv('userMail')
        maven_pass = os.getenv('userMail')
        print("write ‘ossrh’ to settings.xml")
        os.system("sed -i '/<\/servers>/i\<server><id>ossrh</id><username>" + maven_user + "</username><password>" + maven_pass + "</password></server>' $MAVEN_HOME/conf/settings.xml")
    else:
        print("do not config maven.")
#
#
#
config = {}
if len(sys.argv) == 7:
    config["git_name"] = sys.argv[1]
    config["git_mail"] = sys.argv[2]
    config["git_account"] = sys.argv[3]
    config["git_pwd"] = sys.argv[4]
    config["git_branch"] = sys.argv[5]
    config["git_repository"] = sys.argv[6]
    print("form args -> 6")
elif len(sys.argv) == 5:
    config["git_name"] = os.getenv('userName')
    config["git_mail"] = os.getenv('userMail')
    config["git_account"] = sys.argv[1]
    config["git_pwd"] = sys.argv[2]
    config["git_branch"] = sys.argv[3]
    config["git_repository"] = sys.argv[4]
    print("form args -> 4")
elif len(sys.argv) == 4:
    config["git_name"] = os.getenv('userName')
    config["git_mail"] = os.getenv('userMail')
    config["git_account"] = sys.argv[1]
    config["git_pwd"] = sys.argv[2]
    config["git_branch"] = os.getenv('default_branch')
    if config["git_branch"] == None:
        config["git_branch"] = "master"
    config["git_repository"] = sys.argv[3]
    print("form args -> 3")
else:
    print("args error.")
    exit(1)
#
#
#
git_configMaven(config)
git_configProject(git_clone(config), config)
#
#
#
# git add .
# git commit -m "auto commit."
# git push
#
# 8-> deploy "zyc" "zyc@hasor.net" "zycgit" "password" "master" "https://git.oschina.net/zycgit/hasor-garbage.git"
# 4-> deploy "zycgit" "password" "master" "https://git.oschina.net/zycgit/hasor-garbage.git"
# 3-> deploy "zycgit" "password" "https://git.oschina.net/zycgit/hasor-garbage.git"
