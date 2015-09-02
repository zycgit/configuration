#!/bin/bash
#-------------------------------------------------------------------------------
#
#
#
#
#-------------------------------------------------------------------------------

#root password
#MYSQL_ROOT_PWD=root
MYSQL_ROOT_PWD=123

#docker container name
MYSQL_CONTAINER_NAME=store-mysql

#export port
MYSQL_PORT=3306

#my.cfg dir
#MYSQL_CONFIG_DIR=~/mysql/cfg
MYSQL_CONFIG_DIR=/home/admin/data/mysql_cfg

#data dir
#MYSQL_DATA_DIR=~/mysql/data
MYSQL_DATA_DIR=/home/admin/data/mysql


#-------------------------------------------------------------------------------
#
function start_mysql(){
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
}

function stop_mysql(){
  if docker ps|grep -v grep|grep $MYSQL_CONTAINER_NAME >/dev/null 2>&1; then
    echo "mysql stoping..."
    echo "mysql is stoped container ->" `docker stop $MYSQL_CONTAINER_NAME`
    else
    echo "mysql is stoped."
  fi
}

function client_mysql(){
  if docker ps|grep -v grep|grep $MYSQL_CONTAINER_NAME >/dev/null 2>&1; then
    echo "connection to docker mysql..."
    docker exec -i -t $MYSQL_CONTAINER_NAME mysql -uroot -p$MYSQL_ROOT_PWD
  else
    echo "mysql is stoped , you need start it."
  fi
}
#-------------------------------------------------------------------------------
#


echo $1

#start_mysql
#stop_mysql
#client_mysql 