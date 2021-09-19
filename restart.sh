
#这个会报错，用下面哪个
#ssh psql "ps -a | grep java | xargs kill -9"
ssh local "ps -ef | grep java | grep -v grep | awk '{print $2}' | xargs --no-run-if-empty kill"
ssh local "ps -ef | grep redis | grep -v grep | awk '{print $2}' | xargs --no-run-if-empty kill"
#ssh local "cd ~ && source /etc/profile && sh ~/test/run.sh \n"