#!/bin/sh
# shellcheck disable=SC2039
if [[ $# != 1 ]]; then
   echo "USAGE $0 option<deploy | run>"
   exit 1
fi
option=$1
echo $1
zip_file_name="application.zip"
app_file_name="game-chat-1.0-SNAPSHOT.jar"

# shellcheck disable=SC2120
# shellcheck disable=SC2112
function deploy() {
  cd /tmp || exit
  #解压
  unzip -o "${zip_file_name}" -d ~/
  cd ~ || exit
  # +x and execute
  chmod +x "${app_file_name}"
  #初始化db
  expect db_init.sh
}

# shellcheck disable=SC2120
# shellcheck disable=SC2112
function run() {
  echo "start app time: $(date "+%Y-%m-%d %H:%M:%S")" >> ~/run_function.log
  cd ~ || exit
  # 等待db初始化
  sleep 20
  #重启后报java: command not found？
  source /etc/profile
  # 请务必使用后台运行的方式
  java -Xmx6g -Xms6g -Xmn4g -jar ./"${app_file_name}" &> start.log &

  sleep 40
#  sh fire.sh
  echo "end app time: $(date "+%Y-%m-%d %H:%M:%S")" >> ~/run_function.log
  echo "------------------------------------------" >> ~/run_function.log
}

# shellcheck disable=SC2166
if [ "$option" == 'deploy' ]
then
  deploy
elif [ "$option" == 'run' ]
then
  run
else
  deploy
  run
fi