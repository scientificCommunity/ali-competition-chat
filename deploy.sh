if [ "$1" == '0' ]
then
  gradle clean build
fi
mkdir -p build/libs/src/main/resources
cp -r src/main/resources/ build/libs/src/main/resources
cp -r scripts/ build/libs/

#cp application.ros.json build/libs
cp start.sh build/libs
cp fire.sh build/libs
cd build/libs/
zip application.zip -r game-chat-1.0-SNAPSHOT.jar src/main/resources db_init.sh init.sql fire.sh

if [ "$1" == '2' ]
then
  scp game-chat-1.0-SNAPSHOT.jar psql:~/
  scp table_init.sh psql:~/
  sh restart.sh
fi
#scp game-chat-1.0-SNAPSHOT.jar psql:~/

if [ "$1" == '3' ]
then
  scp application.zip  psql:/tmp
  scp start.sh  psql:~/
  ssh psql1 "bash ~/start.sh aa"
fi

#rm application.zip