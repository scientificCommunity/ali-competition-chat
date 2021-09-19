ps -ef | grep game-chat-redis-1.0-SNAPSHOT.jar | grep -v grep | awk '{print $2}' | xargs --no-run-if-empty kill
ps -ef | grep redis | grep -v grep | awk '{print $2}' | xargs --no-run-if-empty kill