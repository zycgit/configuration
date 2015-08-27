FROM java:7
MAINTAINER ZhaoYongChun "zyc@hasor.net"

# maven
ENV MAVEN_VERSION 3.3.3
RUN curl -fsSL http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share \
	&& mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven \
	&& ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME /usr/share/maven


# gnupg
RUN apt-get update \
	&& apt-get install -y gnupg expect git \
	&& mkdir ~/.gnupg

ADD . /usr/share/deployScript
RUN ln -s /usr/share/deployScript /script \
	&& chown 755 /script/*.sh


ENTRYPOINT [ "/script/init_gnupg.sh" , "userName" , "user@mail.cc" , "password" ]


# project
VOLUME /project_home

#CMD ["~/script/pre_gpg.sh"]
#	&& ln -s /usr/share/deployScript/init_gnupg.sh /etc/init.d/init_gnupg.sh
