FROM zycgit/environment
MAINTAINER ZhaoYongChun "zyc@hasor.net"

# install
RUN apt-get update && apt-get install -y --no-install-recommends \
                gnupg \
                expect \
                git \
        && rm -rf /var/lib/apt/lists/* \
        && rm -rf ~/.gnupg/

# work
#VOLUME /work
ENV WORK_HOME /work

# script
ADD . /usr/share/deployScript
RUN ln -s /usr/share/deployScript /script \
        && chmod 755 /script/*.sh \
        && chmod 755 /script/*.py \
        && ln -s /script/deploy.py /usr/bin/deploy

ENV passphrase "123456"
ENV default_branch "master"

ENTRYPOINT [ "/script/init_gnupg.py" ]
#CMD ["/script/deploy.py"]