
### 查找是否存在的SQL语句

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
如果发现表格的唯一索引存在相同的数据，那么就只会更新update，否则insert

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
mysql> create database db_example; -- Creates the new database
mysql> create user 'springuser'@'%' identified by '123456'; -- Creates the user
//授予权限
mysql> grant all on db_example.* to 'springuser'@'%'; -- Gives all privileges to the new user on the newly created database
//撤销权限
mysql> revoke all on db_example.* from 'springuser'@'%';
//授予特定的权限
mysql> grant select, insert, delete, update on db_example.* to 'springuser'@'%';

查看表格的建表语句
mysql> show create table <tablename>

查看表的结构
mysql> DESC <tablename>;


```

删除表的所有数据：`truncate table student;`
