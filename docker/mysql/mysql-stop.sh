#!/bin/bash
source env.sh

if docker ps|grep -v grep|grep $MYSQL_CONTAINER_NAME >/dev/null 2>&1; then
  echo "mysql stoping..."
  echo "mysql is stoped container ->" `docker stop $MYSQL_CONTAINER_NAME`
else
  echo "mysql is stoped."
fi
