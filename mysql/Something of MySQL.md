如果redo log不小心设置得很小，那么看到的现象就是磁盘压力很小，但是数据库间歇性出现性能下跌，因为redo log经常写满，需要将redo log清空，因此经常性进行flush，磁盘操作频繁


### 分页
通常的做法是这样的：`LIMIT 10 OFFSET 2000`

不过`OFFSET`的效率是很低的，优化的办法是使用`id`字段，即`WHERE id > 2000 LIMIT 10`，达到的效果是一样的，效率却很高，不过请求mysql的时候需要传递之前查找到的最大id值

### utf8 和 utf8mp4
Mysql的`utf8`并不是真正的UTF-8，其每个字符最多位3字节，因此如果表中有Emoji表情，因为Emoji表情一个字符占用4字节，所以会导致错误。

utf8mp4是utf8的超集，能够支持一个字符编码为4字节，所以对于需要存放表情的需要使用utf8mp4

utf8mp4需要Mysql版本为5.5.3以上


### COLLATE
COLLATE指定了排序的规则，影响ORDER BY的排序结果、DISTINCT、HAVING、GROUP BY等操作

具体的COLLATE一般有两种，后缀为`_ci`，表示Case Insensitive，即大小写无关，对于"A"和"a"一视同仁；后缀为`_cs`，表示Case Sensitive，即大小写敏感的

可以通过命令`mysql> show collation`查看所有的COLLATE

对于CHARACTER=utf8mp4来说，通常使用的COLLATE有三种
- `utf8mb4_bin`：大小写敏感的，即区分大小写
- `utf8mb4_unicode_ci`：常用的
- `utf8mb4_general_ci`：对于使用中文和英文来说，和上者是一样的

COLLATE可以配置在database, table, field, select中，使用的优先级select > field > table > database
```mysql
CREATE DATABASE <db_name> DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE (
    ...
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE (
`field1` VARCHAR（64） CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
) ……

SELECT field1, field2 FROM table1 ORDER BY field1 COLLATE utf8mb4_unicode_ci;
```

### ROW FORMAT
配置行记录格式，数据库一行数据保存在Page中的形式，有以下几类：
- Antelope：：默认的文件格式
    - COMPACT
    - REDUNDANT
- Barracuda：新的文件格式
    - COMPRESSED
    - DYNAMIC

### 常用数据类型
- longtext：长文本
- timestamp：时间戳
- datetime：2018-06-22 12:20:59
- varchar(100)：变长的字符串，存储0~100个字符
- decimal：定点数类型，以字符串的形式保存。float和double由于是浮点数，会存在小数点的误差。如果对于数据的精度比较高，应该使用decimal

### Other
#### 创建或修改表
Mysql中如果表和表之间建立的外键约束，则无法删除表及修改表结构。

解决方法是在Mysql中取消外键约束:`SET FOREIGN_KEY_CHECKS=0;`，然后对表格进行修改或删除后重建，最后需要再设置外键约束: `SET FOREIGN_KEY_CHECKS=1;`

命令行程序mysql实际上是MySQL客户端，真正的MySQL服务器程序是mysqld，在后台运行。

### 表关系
- 一对多：可以添加外键约束，但是一般不添加，添加之后会对插入的数据内容做验证
- 多对多：通过配置一个中间表来存放多对多关系
- 一对一：对于字段比较多的表格做切分。或者将常用的字段和不常用的字段分开，提高查询效率


### 索引
由于数据在物理上只会保存一份，所以包含实际数据的聚簇索引只能有一个。InnoDB 会自动使用主键（唯一定义一条记录的单个或多个字段）作为聚簇索引的索引键。

对于非主键索引，为了实现快速搜索，引入了二级索引，也是B+树的结构，叶子节点存放的是主键。获得主键后再去聚簇索引中查找数据，这个过程叫做回表

如果联合索引实现了覆盖索引(联合索引已经包含了select所需要的字段)，那么直接在联合索引处就能够获取足够的信息，不需要回表(Using index)

联合索引只能匹配左边的列。比如创建了索引`(name, score)`，但是仅仅对score进行条件搜索`where score > 60`，是无法走这个索引的