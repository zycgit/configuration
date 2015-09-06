FROM zycgit/environment
MAINTAINER ZhaoYongChun "zyc@hasor.net"

# install
RUN apt-get update && apt-get install -y --no-install-recommends \
                gnupg \
                expect \
                git
        && rm -rf /var/lib/apt/lists/*

# work
#VOLUME /work
ENV WORK_HOME /work

# script
ADD . /usr/share/deployScript
RUN ln -s /usr/share/deployScript /script \
        && chown 755 /script/*.sh \
        && ln -s /script/deploy.sh /usr/bin/deploy

ENTRYPOINT [ "/script/init_gnupg.sh" , "userName" , "user@mail.cc" , "password" ]
CMD ["/script/deploy.sh"]