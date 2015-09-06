FROM python:3.4.3
MAINTAINER ZhaoYongChun "zyc@hasor.net"

# install
RUN apt-get update && apt-get install -y --no-install-recommends \
                ca-certificates \
                curl \
                wget \
                unzip \
                gnupg \
                expect \
                git

# java7
ENV LANG C.UTF-8
RUN apt-get install -y openjdk-7-jre-headless="7u79-2.5.6-1~deb8u1" \
        && rm -rf /var/lib/apt/lists/*
ENV JAVA_HOME /usr/lib/jvm/java-7-openjdk-amd64

# maven
ENV MAVEN_VERSION 3.3.3
RUN curl -fsSL http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share \
        && mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven \
        && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_REPO /repo
VOLUME /repo
RUN sed -i '/<!-- localRepository/i\<localRepository>$MAVEN_REPO</localRepository>' $MAVEN_HOME/conf/settings.xml

CMD ["/bin/bash"]