

* [总述](#总述)
* [应用场景](#应用场景)
* [过期时间](#过期时间)
* [数据淘汰策略](#数据淘汰策略)
* [持久化](#持久化)
	* [RDB持久化](#rdb持久化)
	* [AOF持久化](#aof持久化)
* [缓存失效](#缓存失效)
* [Sentinel](#sentinel)
* [分片](#分片)
* [缓存雪崩](#缓存雪崩)
* [缓存穿透](#缓存穿透)
* [主从同步](#主从同步)
* [使用](#使用)


### 总述
- Redis是速度很快的非关系型内存KV存储，可以存储键(String)值(字符串、列表、集合、散列表、有序集合)之间的映射
- Redis可以将内存中的数据持久化到硬盘中，使用复制来扩展读性能，使用分片来扩展写性能。
- 不像Tomcat有线程池，Redis只有几个甚至一个线程，因为和内存打交道，速度很快，没有多线程的必要，使用IO多路复用来监听客户端的大量请求；
- Redis6.0 引入多线程，主要是为了提高网络 IO 读写性能。但是 Redis 的多线程只是在网络数据的读写这类耗时操作上使用了， 执行命令仍然是单线程顺序执行，因此不需要担心线程安全问题。

值的数据类型：
- String：字符串
- List：列表
- Set：无序集合
- Hash：包含键值对的无序散列表
- ZSet：有序集合
- Bitmaps：位图
- HyperLogLog ：基数统计，统计一个集合中不重复的元素个数

---
### 应用场景
1. 计数器：Redis的读写性能很高，适合存储频繁读写的计数量，字符串作为计数器，`INCRBY key 1`，将key储存的值增加1
2. 缓存：放置热点数据到内存中，数据和缓存的操作时序是，先淘汰缓存，在写数据库，再淘汰缓存，第二次淘汰缓存是避免写数据库期间，有新的缓存添加
3. 查找表：和缓存类似，也是利用了Redis快速的查找特性，例如保存DNS记录，查找表的内容不能失效，失效时间需要设置永久有效
4. 消息队列：List是一个双向链表，但最好还是使用Kafka
5. 分布式锁：分布式系统协调不同机器进程的执行
6. 排行榜：ZSet可以实现有序性操作，通过zscore指令获取指定元素的权重，通过zrank指令获取指定元素的正向排名(从小到大)
7. 好友关系：使用Set的并集、交集、差集等，实现共同好友、共同爱好等功能；
8. 用户是否登陆过：使用bitmaps判断
9. session共享：使用redis保存session信息

---
### 过期时间
Redis可以为每个键设置过期时间，当键过期，会自动删除键值

而对于散列表这种数据结构，只能为整个键(整个散列表)设置过期时间，而不能为散列表中的单个键值设置过期时间

#### 判断过期
Redis用一个过期字典表(Hash表)来保存数据的过期时间，Key为数据的Key，Value为过期时间(毫秒精确的UNIX时间戳)

#### 过期数据的处理
1. 惰性删除：在有客户端查找过期数据的时候，才删除过期数据，对CPU更友好，但是可能导致大量过期数据堆积在内存中；
2. 定期删除：每隔一段时间就抽取部分过期数据进行删除，对内存更加友好

Redis的做法是：惰性删除配合上定期删除

### 数据淘汰策略
可以设置内存最大使用量，当内存使用量超出时，会实行数据淘汰策略，Redis中有6中淘汰策略

不淘汰、任意淘汰、最近最少使用淘汰、从已设置过期时间中任意淘汰、从已设置过期时间中选择将要过期的淘汰、从已设置过期时间中选择最近最少使用的淘汰

---
### 持久化
Redis是内存型数据库，为了防止在断电的时候数据不会丢失，需要将内存中的数据持久化到磁盘中

#### RDB持久化
将某个时间点的所有数据存放到磁盘中

可以将快照复制到其他服务器从而创建具有相同数据的服务器副本

如果系统发生故障，那么就只是丢失最后一次创建快照之后的数据

#### AOF持久化
将Redis执行的写命令添加到AOF(Appdend Only File)的末尾，

使用AOF持久化需要设置同步选项，确保写命令同步到磁盘文件上的时机。底层上是将数据放置到缓冲区，而由操作系统决定何时同步到磁盘中

---
### 缓存失效
对于增、删服务器导致的缓存失效的问题，可以使用一致性Hash算法或者Hash slot来解决
- 一致性Hash算法：一个圆圈上面有很多的点，每一个Hash值对应一个点，Redis服务器也对应当中的一点，数据存在在顺时针最邻近的服务器上，Redis服务器映射到圈中的点也可以是虚拟的，就是一个Redis服务器可以对应多个点，从而缓解部分Redis服务器的压力
- Hash Slot：共有16384个槽，每个Redis服务器负责部分的槽，而客户端是无法知道数据是放在哪个Redis服务器的，所以发送请求的对象可以是任何一个Redis服务器，Redis服务器通过通信来确定数据位于哪个服务器上

---
### Sentinel
- 为主节点A添加从节点A1，当A下线后，集群自动设置A1为新的主节点，代替下线的A
- 哨兵可以监听集群中的服务器，并在主服务器进入下线状态时，能够从服务器中选举出新的主服务器

### 分片
一个 Redis 集群包含 16384 个哈希槽（hash slot）， 数据库中的每个键都属于这 16384 个哈希槽的其中一个， 集群使用公式 CRC16(key) % 16384 来计算键 key 属于哪个槽， 其中 CRC16(key)语句用于计算键 key 的 CRC16 校验和 。集群中的每个节点负责处理一部分哈希槽。




### 主从同步
1. 从服务器向主服务器发送SYNC指令，当主服务器接收到指令后，就会调用BGSAVE指令来创建一个子进程专门进行数据持久化工作，将数据写入RDB文件中，在数据持久化期间，主服务器会将执行的写命令缓存在内存中
2. 在BGSAVE指令完成后，主服务器将RDB文件发送给从服务器，从服务器将文件保存到磁盘中，再读取到内存中。之后主服务器将这段时间缓存的写命令以redis协议的格式发送给从服务器

当从服务器断开重连之后，会进行增量同步
1. 主服务器会在内存维护一个缓冲区，保存着要发送给从服务器的数据
2. 从服务器将希望同步的主服务器ID和希望请求的数据的偏移位置发送出去
3. 主服务器确认消息后会检查请求的偏移位置是否在缓冲区中，如果在的话向从服务器发送增量内容


### 使用
- Redis没有关系型数据库中表这一个概念将同种类型的数据存放在一起，而是使用命名空间的方式实现这个功能。键名的前面部分存储命名空间，后面部分的内容存储ID，中间使用“：”进行分隔，例如键名为article:123456
- `keys *`：显示所有的key
- `hgetall KEY_NAME`：查看某个KEY下面的所有数据



### 数据一致性

对于写请求，一般是使用策略**Cache Aside Pattern**，即先更新DB数据，然后删除Redis数据

由于Redis不可用导致删除Redis数据失败，会导致DB数据和Redis数据不一致，如果此时有客户端查询则会直接返回Redis的数据，为脏数据

可以采取的措施是，当删除Redis数据失败的时候，重试一定的次数，如果重试次数用尽后，将数据写入到单独的队列（to_be_delete）中，等待Redis可用的时候，消费队列（to_be_delete）执行删除操作