#!/bin/expect

spawn psql -U admin -d game
expect "*口令*"
send "123\n"
expect "*game*"

send "delete from g_message;\n delete from g_room;\n"
expect eof