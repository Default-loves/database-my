

### 参数变量

```mysql
查看变量值
show VARIABLES LIKE 'sql_mode';
select @@global.sql_mode;

设置变量值
set global sql_mode='xx_mode';
set @@global.sql_mode='xx_mode';
```



### 行转列、列转行

```mysql
SELECT name,
  MAX(CASE WHEN subject='语文' THEN score ELSE 0 END) AS "语文",
  MAX(CASE WHEN subject='数学' THEN score ELSE 0 END) AS "数学",
  MAX(CASE WHEN subject='英语' THEN score ELSE 0 END) AS "英语"
FROM student1
GROUP BY name

SELECT NAME,'语文' AS subject,MAX("语文") AS score
FROM student2 GROUP BY NAME
UNION
SELECT NAME,'数学' AS subject,MAX("数学") AS score
FROM student2 GROUP BY NAME
UNION
SELECT NAME,'英语' AS subject,MAX("英语") AS score
FROM student2 GROUP BY NAME

```





### 函数

```mysql
# IF(expr1,expr2,expr3)
# 如果expr1为TRUE，则为expr2，否则为expr3

select employee_id , IF(employee_id%2=1 and name not like 'M%', salary , 0) AS bonus 
from Employees 



# case()
# 枚举
update salary
set sex = (case sex
               when 'm' then 'f'
               else 'm'
           end);

update 班级表
set 班级 = (case 班级
                when 1 then 2
                when 2 then 1
                else 3
           end);


```



### 删除重复记录，但保留id最小的一条

```mysql
Person 表:
+----+------------------+
| id | email            |
+----+------------------+
| 1  | john@example.com |
| 2  | bob@example.com  |
| 3  | john@example.com |
+----+------------------+

delete a
from Person a, Person b
where a.email = b.email and a.id > b.id;
```



### 查询语句

对字符串进行拼接

```mysql
SELECT 
    GROUP_CONCAT(DISTINCT country
        ORDER BY country
        SEPARATOR ';')
FROM t_book
```

查找是否存在的SQL语句和Java代码

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

### 参数配置

每次事务的 redo log 都直接持久化到磁盘：`innodb_flush_log_at_trx_commit = 1`

每次事务的 bin log 都直接持久化到磁盘：`sync_binlog = 1`

change buffer 的大小最多只能占用 buffer pool 的 50%：`innodb_change_buffer_max_size= 50`

存储引擎线程并发上限，如果为0则表示无限制：`innodb_thread_concurrency`

### 新增和更新
```mysql
如果发现表的唯一索引存在相同的数据，那么就只会更新update，否则insert

例子：存在唯一索引a和b。如果表中已经存在a=apple，b=1的数据，那么就只会update。
对于insert，受影响的行值为1，对于update操作，则为2

INSERT INTO sys_parameters
(a, b, c,)
values
("apple", 1, 100)
ON DUPLICATE KEY UPDATE
 c=100

也可以使用
REPLACE INTO users (id,name,age) VALUES(123, ‘賈斯丁比伯‘, 22);
来达到类似的效果，但是其不好的地方在于如果是更新操作，那么其会删除原来的一行数据，再新增一行，而且自增id也会变化
```

### mysql Command
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

查看配置加载的顺序，即查看使用的哪个 my.cnf
mysql --help | grep 'Default options' -A 1


查看语句的执行情况
`explain MySQL语句`

强制优化器使用索引k
`force index(k)`

接下来的SQL语句都会记录到慢查询日志（slow log）中
`set long_query_time=0`

查看InnoDB存储引擎的状态
show engine innodb status

查看客户端的连接：
`show PROCESSLIST;`
```



### binlog相关

```mysql
查看binlog：
mysqlbinlog --no-defaults --database=ykt  --base64-output=decode-rows -v --start-datetime='2019-04-11 00:00:00' --stop-datetime='2021-07-17 15:00:00'  mysql-bin.000120 | less

查看binlog：
mysql > show binlog events in 'master.000001';

mysqlbinlog mysql-bin.00001 | more
```