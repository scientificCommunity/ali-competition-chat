# game-chat-redis
阿里云天池举办的[web应用性能挑战赛](https://tianchi.aliyun.com/competition/entrance/531907/rankingList/1) 参赛作品。

# 关于比赛成绩
初赛360分,第一次提交排名为16，最终是在30名左右。后面因为骨折，错过了实名认证，导致成绩跟复赛资格被取消。

# 技术栈
采用vertx作为程序的基本框架，redis提供持久化功能以及为数据提供内存缓存功能

# 快速开始
本服务依赖redis，redis的相关配置在[RedisFactory](src/main/kotlin/org/baichuan/chat/service/RedisFactory.kt)。redis设置好了之后即可通过[Main](src/main/kotlin/org/baichuan/chat/Main.kt)启动程序

# 关于比赛规则
三个考核标准(接口)：qps、时延、功能完整度
考核结果以分数作为唯一反馈。不提供运行程序的机器的各项指标
