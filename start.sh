#!/bin/bash
mvn_package_install() {
  #  echo -e "\033[47;30m 暂停所有服务再编译...此处出现 error 可忽略... \033[0m"
  #  for service_name in ${array_service[@]}; do
  #    cd $Project/isen-$service_name
  #    docker compose -f $dockerProfile pause
  #  done
  echo -e "\033[47;30m 编译最新代码中... \033[0m"
  cd $Project
  mvn package install -P $application
  #  echo -e "\033[47;30m 启动暂停的服务...此处出现 error 可忽略... \033[0m"
  #  for service_name in ${array_service[@]}; do
  #    cd $Project/isen-$service_name
  #    docker compose -f $dockerProfile unpause
  #  done
}
workdir_controller() {
  cd $Project/bot-controller
}
stop_docker-compose() {
  docker compose -f $dockerProfile down
}
start_docker-compose() {
  docker compose -f $dockerProfile up -d
}
restart_docker-compose() {
  docker compose -f $dockerProfile down
  docker compose -f $dockerProfile up -d
}
easy_use() {
  mvn_package_install
  echo -e "\033[47;30m 启动服务中: controller \033[0m"
  workdir_controller
  restart_docker-compose
}
array_service=("controller")
array_act=("start" "stop" "restart")
custom_use() {
  echo -e "\033[41;37m 启动/关闭/重启 = start/stop/restart 服务名称(controller) \033[0m"
  echo -n -e "\033[41;37m 请输入内容(格式: 操作 服务):  \033[0m"
  read service_act service_name
  if [ ! -n "$service_act" ] || [ ! -n "$service_name" ]; then
    echo -e "\033[31m 必须携带两个参数 \033[0m"
    custom_use
  elif [[ "${array_act[@]}" =~ "$service_act" ]] && [[ "${array_service[@]}" =~ "$service_name" ]]; then
    mvn_package_install
    cd $Project/isen-$service_name
    if [ "$service_act" = "start" ]; then
      echo -e "\033[47;30m 启动服务 $service_name \033[0m"
      restart_docker-compose
    elif [ "$service_act" = "stop" ]; then
      echo -e "\033[47;30m 停止服务 $service_name \033[0m"
      stop_docker-compose
    elif [ "$service_act" = "restart" ]; then
      echo -e "\033[47;30m 重启服务 $service_name \033[0m"
      restart_docker-compose
    fi
  else
    echo -e "\033[34m 输入的 操作/服务名称 错误 \033[0m"
    custom_use
  fi
}

export JAVA_HOME=$HOME/jdk-17.0.2
export PATH=$PATH:$JAVA_HOME/bin
export MAVEN_HOME=$HOME/apache-maven-3.8.5
export PATH=$PATH:$MAVEN_HOME/bin
export Project=$(
  cd $(dirname $0)
  pwd
)
cd $Project
echo -e "\033[47;30m 拉取最新项目代码 \033[0m"
git config --global pull.rebase true
git stash
git stash clear
git pull -f
#rm -rfv /home/ocd/emby_ocd/emby/webhook.sh
#cp webhook.sh /home/ocd/emby_ocd/emby/webhook.sh
echo -e "\033[47;30m 已同步最新项目代码 \033[0m"
echo -e "\033[41;37m 1.正式服配置 2.测试服配置 不选择默认测试服配置 \033[0m"
read application_type
export application=prod
export dockerProfile=docker-compose-prod.yml
if [ ! -n "$application_type" ] || [ "$application_type" = 2 ]; then
  export application=test
  export dockerProfile=docker-compose-test.yml
fi
echo -e "\033[41;37m 1.基础操作 2.自定义操作  不输入则默认基础操作 \033[0m"
read type
if [ ! -n "$type" ] || [ "$type" = 1 ]; then
  echo -e "\033[41;37m 请输入操作服务type 1.重启所有服务 \033[0m"
  read easy_type
  easy_use
  exit
elif [ "$type" = 2 ]; then
  custom_use
else
  echo -e "\033[31m 参数错误 \033[0m"
fi
