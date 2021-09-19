ssh psql "ps -ef | grep game-chat-1.0-SNAPSHOT.jar | grep -v grep | awk '{print $2}' | xargs --no-run-if-empty kill"
ssh psql "cd ~ && source /etc/profile && sh ~/run.sh \n"