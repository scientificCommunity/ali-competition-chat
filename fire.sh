curl -i -X POST \
   -H "Content-Type:application/json" \
   -d \
'{
  "username":"xxx",
  "firstName":"xxx",
  "lastName":"k",
  "email":"3058@qq.com2",
  "phone":"15612",
  "password":"xxx"
}' \
 'http://localhost:8080/user'

curl -i -X GET \
 'http://localhost:8080/userLogin?username=xxx&password=xxx'