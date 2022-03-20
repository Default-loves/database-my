
### 下载
- https://dev.mysql.com/downloads/mysql/
- Download MySQL Community Server

### 配置
- 将MySQL/bin目录添加到环境变量PATH中
- 在MySQL目录下添加my.ini
```
[mysql]
# 设置mysql客户端默认字符集
default-character-set=utf8
 
[mysqld]
# 设置3306端口
port = 3306
# 设置mysql的安装目录
basedir=F:\Tool\mysql-5.7.34-winx64
# 设置 mysql数据库的数据的存放目录，MySQL 8+ 不需要以下配置，系统自己生成即可，否则有可能报错
datadir=F:\Tool\mysql-5.7.34-winx64\data
# 允许最大连接数
max_connections=200
# 服务端使用的字符集默认为8比特编码的latin1字符集
character-set-server=utf8
# 创建新表时将使用的默认存储引擎
default-storage-engine=INNODB
```

注意：如果`datadir`配置的目录存在的话，那么需要删除掉，否则后面安装过程会报错

### 安装

- 以管理员身份启动CMD
- `mysqld -install`
- 切换到MySQL目录，执行`mysqld --initialize --user=root --console`，结果包含了初始密码
```
...
2018-04-20T02:35:05.464644Z 5 [Note] [MY-010454] [Server] A temporary password is generated for
root@localhost:APWCY5ws&hjQ
...
```
- `net start mysql`
- `mysql -u root -p123456`
- 修改密码：
    - 8.0版本之前：`mysql> SET PASSWORD FOR 'root'@'localhost' = PASSWORD('123456');`
    - 8.0版本之后：`alter user 'root'@'localhost' identified by '123456';`


### 已经安装，需要卸载后再安装
1. 运行services.msc，停止MySql服务。也可以通过`net stop mysql`来达到相同的效果
2. 运行regedit，删除注册表信息
```
HKEY_LOCAL_MACHINE/SYSTEM/ControlSet001/Services/Eventlog/Applications/MySQL
HKEY_LOCAL_MACHINE/SYSTEM/ControlSet002/Services/Eventlog/Applications/MySQL
HKEY_LOCAL_MACHINE/SYSTEM/CurrentControlSet/Services/Eventlog/Applications/MySQL
```
3. 以管理员身份运行cmd，执行`mysqld remove`
4. 按照配置和安装流程进行安装