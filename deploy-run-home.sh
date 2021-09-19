if [ "$1" == '0' ]
then
  gradle clean build
fi
mkdir -p build/libs/src/main/resources
cp -r src/main/resources/ build/libs/src/main/resources

#cp application.ros.json build/libs
cp start-home.sh build/libs
cp redis-start-home.sh build/libs
cp stop.sh build/libs
cp redis.conf build/libs

cd build/libs/
zip application.zip -r game-chat-redis-1.0-SNAPSHOT.jar src/main/resources redis-start-home.sh redis.conf

if [ "$1" == '3' ]
then
  scp application.zip  psql:/tmp
  scp start-home.sh  psql:~/
  scp stop.sh  psql:~/
  ssh psql "bash ~/stop.sh"
  ssh psql "bash ~/start-home.sh aa"
fi

#rm application.zip