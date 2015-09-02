#!/bin/bash
source env.sh

if docker ps|grep -v grep|grep $MYSQL_CONTAINER_NAME >/dev/null 2>&1; then
  echo "connection to docker mysql..."
  docker exec -i -t $MYSQL_CONTAINER_NAME mysql -uroot -p$MYSQL_ROOT_PWD
else
  echo "mysql is stoped , you need start it."
fi
