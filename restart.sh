ssh local "ps -ef | grep java | grep -v grep | awk '{print $2}' | xargs --no-run-if-empty kill"
ssh local "ps -ef | grep redis | grep -v grep | awk '{print $2}' | xargs --no-run-if-empty kill"