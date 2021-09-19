# ali-competition-chat
[![chat on gitlab](https://img.shields.io/gitter/room/mampod/payment?logo=github)](https://github.com/scientificCommunity/blog-sample/issues)

阿里云天池举办的[web应用性能挑战赛](https://tianchi.aliyun.com/competition/entrance/531907/rankingList/1) 参赛作品。
# 其他版本
[redis](https://github.com/scientificCommunity/ali-competition-chat)

[PostgreSQL+消息顺序读写磁盘](https://github.com/scientificCommunity/ali-competition-chat/tree/feature/psql-read-disk)
# 关于比赛成绩
初赛360分,第一次提交排名为16，最终是在30名左右。后面因为骨折，错过了实名认证，导致成绩跟复赛资格被取消。

# 技术栈
采用`vertx`作为程序的基本框架，`PostgreSQL`提供数据持久化功能

# 快速开始
1. 先通过[脚本](scripts/init.sql)初始化数据库。

2. 数据库设置好了之后需要在项目根目录下创建`jooq.properties`文件，并配置数据库信息：
    ```yaml
    host=xxx
    port=5432
    username=xxx
    password=xxx
    database=xxx
    ```
3. 在[PostgreSqlVerticle](src/main/kotlin/org/baichuan/chat/db/verticle/PostgreSqlVerticle.kt)中配置连接信息

4. 运行`./gradlew clean build`

5. 通过[Main](src/main/kotlin/org/baichuan/chat/Main.kt)启动项目

# 关于比赛规则
三个考核标准(接口)：qps、时延、功能完整度
考核结果以分数作为唯一反馈。不提供运行程序的机器的各项指标
