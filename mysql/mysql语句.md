

### 查询语句

对字符串进行拼接

```mysql
SELECT 
    GROUP_CONCAT(DISTINCT country
        ORDER BY country
        SEPARATOR ';')
FROM t_book
```





### 配置与系统语句



查看客户端的连接：`show PROCESSLIST;`

### 参数配置

每次事务的 redo log 都直接持久化到磁盘：`innodb_flush_log_at_trx_commit = 1`

每次事务的 bin log 都直接持久化到磁盘：`sync_binlog = 1`

change buffer 的大小最多只能占用 buffer pool 的 50%：`innodb_change_buffer_max_size= 50`

存储引擎线程并发上限，如果为0则表示无限制：`innodb_thread_concurrency`









### 查找是否存在的SQL语句和Java代码

```java
//在Service层进行是否存在的判断的时候，如下
Integer exist = xxDao.existXxxxByXxx(params);
if ( exist != NULL ) {
  //当存在时，执行这里的代码
} else {
  //当不存在时，执行这里的代码
}

// 使用如下的SQL，限制查找到1条就返回，并且返回1，能够有效提高性能
select 1 from students where ... limit 1;
```

### 新增和更新
```mysql
如果发现表的唯一索引存在相同的数据，那么就只会更新update，否则insert

例子：存在唯一索引a和b。如果表中已经存在a=apple，b=1的数据，那么就只会update。
如果行作為新記錄被插入，則受影響行的值為1；如果原有的記錄被更新，則受影響行的值為2。
INSERT INTO sys_parameters
(a, b, c,)
values
("apple", 1, 100)
ON DUPLICATE KEY UPDATE
 c=100

注意：也可以使用
REPLACE INTO users (id,name,age) VALUES(123, ‘賈斯丁比伯‘, 22);
来达到类似的效果，但是其不好的地方在于如果是更新操作，那么其会删除原来的一行数据，再新增一行，而且自增id也会变化
```

### Command
```mysql
// 创建数据库
mysql> create database db_example; 
// 创建用户
mysql> create user 'springuser'@'%' identified by '123456'; 
//授予权限-全部
mysql> grant all on db_example.* to 'springuser'@'%'; 
//撤销权限
mysql> revoke all on db_example.* from 'springuser'@'%';
//授予特定的权限
mysql> grant select, insert, delete, update on db_example.* to 'springuser'@'%';

查看表格的建表语句
mysql> show create table <tablename>

查看表的结构
mysql> DESC <tablename>;

删除表的所有数据
truncate table student;

查看MySQL的配置参数：
show variables like 'wait_timeout';

查看配置加载的顺序，即查看使用的哪个my.cnf
mysql --help | grep 'Default options' -A 1


查看语句的执行情况
`explain MySQL语句`

强制优化器使用索引k
`force index(k)`

接下来的SQL语句都会记录到慢查询日志（slow log）中
`set long_query_time=0`

查看InnoDB存储引擎的状态
show engine innodb status
```



### binlog

```mysql
查看binlog：
mysqlbinlog --no-defaults --database=ykt  --base64-output=decode-rows -v --start-datetime='2019-04-11 00:00:00' --stop-datetime='2021-07-17 15:00:00'  mysql-bin.000120 | less
```