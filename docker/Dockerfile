#version 0.0.1
#
FROM maven:3.3.3-jdk-7
MAINTAINER ZhaoYongChun "zyc@byshell.org"

ENV PWD=fubing220106


RUN mkdir ~/workspace
RUN cd ~/workspace
RUN wget http://files.byshell.org/settings/gnupg.zip
RUN wget http://files.byshell.org/settings/ssh.zip
RUN unzip -P"${PWD}" -d ~/.gnupg ./gnupg.zip
RUN unzip -P"${PWD}" -d ~/.ssh ./ssh.zip

RUN git clone https://git.oschina.net/zycgit/hasor.git ~/git_project

  
#RUN wget http://files.byshell.org/script/deploy.sh
#RUN chmod 755 deploy.sh 

#EXPOSE 80
