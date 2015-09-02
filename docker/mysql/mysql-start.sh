#!/bin/bash
source env.sh

if docker ps|grep -v grep|grep $MYSQL_CONTAINER_NAME >/dev/null 2>&1; then
  echo "mysql is started."
  exit 0;
fi

echo "starting mysql..."

if docker ps -a|grep -v grep|grep $MYSQL_CONTAINER_NAME >/dev/null 2>&1; then
  CMD="docker start $MYSQL_CONTAINER_NAME"
else
  VARS="-v $MYSQL_CONFIG_DIR:/etc/mysql/conf.d -v $MYSQL_DATA_DIR:/var/lib/mysql"
  CMD="docker run --name $MYSQL_CONTAINER_NAME $VARS -e MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PWD -d -p 3306:$MYSQL_PORT mysql:5.7.7"
fi

echo 'execute =' $CMD
echo "docker start mysql container ->" `$CMD`

if docker ps|grep -v grep|grep $MYSQL_CONTAINER_NAME >/dev/null 2>&1; then
  echo "mysql Start successf."
else
  echo "mysql Start failure."
fi
