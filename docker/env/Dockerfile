#version 0.0.1
#
FROM java:7
MAINTAINER ZhaoYongChun "zyc@byshell.org"

# maven
ENV MAVEN_VERSION 3.3.3
RUN curl -fsSL http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share \
	&& mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven \
	&& ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME /usr/share/maven

# gnupg
RUN apt-get update
RUN apt-get install -y gnupg
RUN apt-get install -y expect
RUN apt-get install -y pacman

ADD . ~/script
cat /dev/urandom

ENV relName
ENV relName
ENV relName

ONBUILD ~/script/pre_gpg.sh expect gpg --gen-key


# deploy
RUN PWD=curl http://files.byshell.org/settings/password.key
RUN curl -fsSL http://files.byshell.org/settings/ssh.zip >> ~/ssh.zip
RUN curl -fsSL http://files.byshell.org/settings/gnupg.zip >> ~/gnupg.zip
RUN unzip -P"$PWD" -d ~/.gnupg ~/gnupg.zip
RUN unzip -P"$PWD" -d ~/.ssh ~/ssh.zip

RUN wget http://files.byshell.org/script/deploy.sh \
	&& mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven \
	 




CMD ["mvn"]
