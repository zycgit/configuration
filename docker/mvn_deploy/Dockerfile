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
        && chown 755 /script/*.py \
        && ln -s /script/deploy.py /usr/bin/deploy

ENTRYPOINT [ "/script/init_gnupg.py" , "userName" , "user@mail.cc" , "password" ]
CMD ["/script/deploy.py"]