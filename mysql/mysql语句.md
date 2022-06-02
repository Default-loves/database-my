

### 常用命令

```mysql
# 创建数据库
mysql> create database db_example; 
# 创建用户
mysql> create user 'springuser'@'%' identified by '123456'; 
# 授予权限-全部
mysql> grant all on db_example.* to 'springuser'@'%'; 
# 撤销权限
mysql> revoke all on db_example.* from 'springuser'@'%';
# 授予特定的权限
mysql> grant select, insert, delete, update on db_example.* to 'springuser'@'%';

# 查看表格的建表语句
mysql> show create table <tablename>

# 查看表的结构
mysql> DESC <tablename>;

# 删除表的所有数据
truncate table student;


# 查看配置加载的顺序，即查看使用的哪个 my.cnf
mysql --help | grep 'Default options' -A 1

# 查看语句的执行情况
`explain MySQL语句`

# 强制优化器使用索引k
`force index(k)`

# 接下来的SQL语句都会记录到慢查询日志（slow log）中
`set long_query_time=0`

# 查看InnoDB存储引擎的状态
show engine innodb status

# 查看客户端的连接：
`show PROCESSLIST;`

# 查看配置参数
show VARIABLES LIKE 'sql_mode';
select @@global.sql_mode;

# 设置配置参数
set global sql_mode='xx_mode';
set @@global.sql_mode='xx_mode';

#查看数据文件所在的路径
show global variables like "%datadir%";
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
CREATE TABLE `Person`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(100)  DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  CHARACTER SET = utf8;

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

### 查找是否存在数据的SQL语句

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



### 新增重复数据的替换操作

如果发现表格的唯一索引存在相同的数据，那么就只会更新update，否则insert

例如，存在唯一索引`idx_a_b(a, b)`。如果表中已经存在a=apple，b=1的数据，那么就只会update。

如果最终是`insert`，那么返回的受影响的行值为1，如果是`update`操作，那么受影响的行值为2

```mysql
INSERT INTO sys_parameters(a, b, c,)
values("apple", 1, 100)
ON DUPLICATE KEY UPDATE
 c=100
```

也可以使用`replace`来达到类似的效果，和上述不同的是，`replace`操作会删除原来的一行数据，再新增一行，因此自增的主键id会变化

```mysql
REPLACE INTO users (id,name,age) VALUES(123, ‘賈斯丁比伯‘, 22);
```




### 参数配置

每次事务的 redo log 都直接持久化到磁盘：`innodb_flush_log_at_trx_commit = 1`

每次事务的 bin log 都直接持久化到磁盘：`sync_binlog = 1`

change buffer 的大小最多只能占用 buffer pool 的 50%：`innodb_change_buffer_max_size= 50`

存储引擎线程并发上限，如果为0则表示无限制：`innodb_thread_concurrency`



### binlog相关

```mysql
查看binlog：
mysqlbinlog --no-defaults --database=ykt  --base64-output=decode-rows -v --start-datetime='2019-04-11 00:00:00' --stop-datetime='2021-07-17 15:00:00'  mysql-bin.000120 | less

查看binlog：
mysql > show binlog events in 'master.000001';

mysqlbinlog mysql-bin.00001 | more
```



### 建表语句

```mysql
DROP TABLE IF EXISTS `mytest`;
CREATE TABLE `mytest` (
	  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增长id',
    `text` varchar(255) DEFAULT '' COMMENT '内容',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
		PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `ygj_upload`;
CREATE TABLE `ygj_upload` (
	  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增长id',
		`update_id` int(11) NOT NULL COMMENT '表的自增id',
    `type` varchar(255) NOT NULL COMMENT '类型',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
		PRIMARY KEY(id),
		KEY `update_id_type` (`update_id`, `type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '保存待上传到云管家的数据';
```

