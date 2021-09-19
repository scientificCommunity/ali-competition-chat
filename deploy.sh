if [ "$1" == '0' ]
then
  gradle clean build
fi
mkdir -p build/libs/src/main/resources
cp -r src/main/resources/ build/libs/src/main/resources

#cp application.ros.json build/libs
cp start.sh build/libs
cp redis-start.sh build/libs
cp redis.conf build/libs

cd build/libs/
zip application.zip -r game-chat-redis-1.0-SNAPSHOT.jar src/main/resources redis-start.sh redis.conf

if [ "$1" == '3' ]
then
  scp application.zip  local:/tmp
  scp start.sh  local:~/
  ssh local "bash ~/start.sh aa"
fi

#rm application.zip