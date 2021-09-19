if [ "$1" == '0' ]; then
  cp application1.ros.json build/libs/application.ros.json
elif [ "$1" == '1' ]; then
  cp application-redis.ros.json build/libs/application.ros.json
elif [ "$1" == '2' ]; then
  cp application-redis1.ros.json build/libs/application.ros.json
else
  cp application.ros.json build/libs
fi

cd build/libs/
zip deploy_application.zip application.zip start.sh application.ros.json