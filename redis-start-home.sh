cp /root/redis.conf /var/lib/redis/redis-6.2.5/
nohup /var/lib/redis/redis-6.2.5/src/redis-server /var/lib/redis/redis-6.2.5/redis.conf >/var/lib/redis/redis.log &