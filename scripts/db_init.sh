#!/bin/expect

set fid [open init.sql r]
set sql [read $fid]

# 建库
spawn createdb -U admin -O admin game
expect "*口令*"
send "123\n"

# 等待数据库创建完成
sleep 2

spawn psql -U admin -d game
expect "*口令*"
send "123\n"
expect "*game*"

#执行sql
send $sql"\n"
# 退出psql终端
expect eof