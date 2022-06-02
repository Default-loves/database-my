

### 使用docker安装与启动mongo

```shell
1. 安装
docker pull mongo:latest

2. 启动
docker run --name mongo -p 27017:27017 -v D:/myDocker/docker-data/mongo:/data/db -e MONGO_INITDB_ROOT_USERNAME=admin -e MONGO_INITDB_ROOT_PASSWORD=admin -d mongo



```



### 开启身份验证

```shell
docker run -itd --name mongo -p 27017:27017 mongo --auth

--auth：需要密码才能访问容器服务。
```

下面进入mongo中添加用户和密码

```shell
docker exec -it mongo mongo admin

use admin
# 创建一个名为 maxsu，密码为 pwd123 的用户。
>  db.createUser(
     {
       user:"maxsu",
       pwd:"pwd123",
       roles:[{role:"root",db:"admin"}]
     }
  )
# 尝试使用上面创建的用户信息进行连接。
> db.auth('maxsu', 'pwd123')
# 在数据库test创建test/123456用户
> use test
> db.createUser({ user: 'test', pwd: '123321132', roles: [{ role: "readWrite", db: "test" }] });
```

### 连接

```shell
mongo -u maxsu -p  --authenticationDatabase admin
```



### 用户角色

1. 数据库用户角色：read、readWrite;

2. 数据库管理角色：dbAdmin、dbOwner、userAdmin；
3. 集群管理角色：clusterAdmin、clusterManager、clusterMonitor、hostManager；
4. 备份恢复角色：backup、restore；
5. 所有数据库角色：readAnyDatabase、readWriteAnyDatabase、userAdminAnyDatabase、dbAdminAnyDatabase
6. 超级用户角色：root 
// 这里还有几个角色间接或直接提供了系统超级用户的访问（dbOwner 、userAdmin、userAdminAnyDatabase）
7. 内部角色：__system

具体角色：

Read：允许用户读取指定数据库
readWrite：允许用户读写指定数据库
dbAdmin：允许用户在指定数据库中执行管理函数，如索引创建、删除，查看统计或访问system.profile
userAdmin：允许用户向system.users集合写入，可以找指定数据库里创建、删除和管理用户
clusterAdmin：只在admin数据库中可用，赋予用户所有分片和复制集相关函数的管理权限。
readAnyDatabase：只在admin数据库中可用，赋予用户所有数据库的读权限
readWriteAnyDatabase：只在admin数据库中可用，赋予用户所有数据库的读写权限
userAdminAnyDatabase：只在admin数据库中可用，赋予用户所有数据库的userAdmin权限
dbAdminAnyDatabase：只在admin数据库中可用，赋予用户所有数据库的dbAdmin权限。
root：只在admin数据库中可用。超级账号，超级权限。

### 常用命令

```shell
# 列出所有用户
db.getUsers()
```

