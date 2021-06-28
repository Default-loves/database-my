

---
### 基础架构

![img](img/0d2070e8f84c4801adbfa03bda1f98d9.png)

#### 连接器
连接器用于和客户端建立连接，获取登陆用户的权限，维持和管理连接

对于长连接，由于查询过程中使用的临时内存放置在连接对象中，只有断开连接的时候才释放内存资源，所以如果连接长时间保持，则内存占用大，如果超过限制则会OOM，表现为MySQL异常重启了。对此，有两种解决方法：
1. 定期断开连接，或者程序里面判断如果操作了一个占用内存的大查询后，断开连接，之后要查询的时候重新连接
2. 在每次执行完一次大查询后，执行`mysql_reset_connection`来重新初始化连接，这个过程不需要重新获取权限和重连，只是将连接恢复到原始的状态

当客户端长时间无动静，连接器会断开连接，这个时间是通过参数`wait_timeout`控制的，默认是8小时

#### 查询缓存

查询缓存是对查询结果缓存起来，当下一次有相同的查询请求时，直接从缓存中获取结果返回，避免查询存储引擎，加快查询速度。

不过，不建议使用查询缓存，MySql8.0也将查询缓存的功能去掉了，原因是查询缓存的失效率很高，但凡有对一个表进行修改，都会将相关的缓存给清除掉



#### 分析器

包括词法分析，语法分析
#### 优化器
在表里面有多个索引的时候，决定使用哪个索引更快，或者在多表连接`join`的时候，决定表的连接顺序，优化器的目的是对于多个索引，找到执行代价最小的方案去执行，影响执行代价的元素包括：扫描行数、是否涉及到临时表和排序等
##### 索引的基数（cardinality）
一个索引上不同值的个数，更为索引的基数，一个索引的基数越高，说明该索引的区分度会更好，MySQL计算索引的基数是采用采样统计的方法，通过设置`innodb_stats_persistent`来选择存储索引统计的方式

扫描行数的判断如果出现偏差，可以使用`analyze table T`来重新统计索引信息

即使某一个二级索引的扫描行数更少，但是优化器会考虑回表的消耗，可能会采用扫描行数更高的主键索引

##### 优化器索引选择异常的处理
- 使用`force index`强制使用索引
- 修改SQL语句，引导MySQL使用我们期望的索引，例如索引异常的情况如下`order by b limit 1`改为`order by b,a limit 1`就能从使用索引b变为使用索引a
- 用`limit 10000`来让优化器意识到使用某些索引的代价更高
- 在某些场景下，新建一个更合适的索引，来提供给优化器做选择，或删除误用的索引

如果经常对数据进行增删，那么数据由于MVCC会存在多个数据版本，对于非主键索引，它看到的数据包括历史数据版本的，因此可能优化器可能会错误选择索引，而主键是直接按照表的行数来估计的，而表的行数，优化器直接用的是slow table status的值

#### 执行器
首先判断对表是否有执行的权限，如果没有操作权限就报错，否则调用存储引擎接口来进行操作，在数据库的慢查询日志中有`rows_examined`字段，表示了语句执行过程中执行器调用存储引擎接口的次数

#### 存储引擎

存储引擎负责数据的存储和读取，其架构模式是插件式的，支持InnoDB、MyISAM、Memory等多个存储引擎

### 版本

MySql 5.7.34 与 MySql 8.0.25，目前常用的还是5.7版本



### MySQL语句的分类

- DDL（Data Definition Language，数据库定义语言），包括create,drop,alter等
- DML(Data Manipulation Language，数据库操纵语言)，包括select,delete,update,insert等
- DCL(Data Control Language，数据控制语言)，包括grant,revoke
- TCL(Transaction Control Language，事务控制语言)，包括savepoint,rollback,set transaction




---

### 日志系统

#### WAL(Write-ahead logging，预写式日志）

是关系数据库中用来保证原子性和持久性的一系列技术，关键就是先写日志，再写磁盘，日志文件通常包括redo和undo信息，具备crash-safe

#### redo log

- 是物理日志，意思是记录的内容是更新操作对具体的数据页进行的操作
- 存在于InnoDB存储引擎中
- 由于在硬盘中对数据进行更新耗费时间很久，所以将更新操作记录在redo log中，通常在系统空闲的时候读取redo log进行磁盘写入,这个过程称为`flush`
- 大小是固定的，比如4个各2G的文件，循环写入redo log，因此当redo log满时，需要等待flush过程，此时系统不能更新数据
- crash-safe，在系统异常重启后，保证了数据的一致性，也就是说之前提交记录不会丢失

在内存中的数据更新之后，磁盘中的数据和内存中的数据不一致，我们称这个内存页为“脏页”，而一致的数据页，称为“干净页”

什么时候会进行flush？

- 当redo log满的时候，此时不能更新数据
- 当内存满的时候，需要将长久不用的数据页淘汰，如果淘汰的是脏页，那么就需要将脏页写入磁盘
- 当系统空闲的时候
- MySQL正常关闭的时候

##### 刷脏页的控制策略

参数`innodb_io_capacity`表示了主机IO的能力，这个值一般设置为磁盘的IOPS，而IOPS可以通过以下语句了解`fio -filename=$filename -direct=1 -iodepth1 -thread -rw=randrw -ioengine=psync -bs=16k -size=500M -numjobs=10 -runtime=10 -group_reporting -name=mytest`

InnoDB刷盘速度会考虑两个，一个是脏页比例，一个是redo log写盘速度，参数`innodb_max_dirty_pages_pct`是脏页比例上限，默认值为75%，InnoDB会根据当前的脏页比例算出一个0~100的值为K1，根据redo log写入序号跟checkpoint对应序号的差值算出一个0~100的值为K2，取较大值为R，之后引擎根据innodb_io_capacity定义的能力乘以R%来控制刷页的速度

脏页比例计算?

1. `select VARIABLE_VALUE into @a from global_status where VARIABLE_NAME='Innodb_buffer_pool_pages_dirty';`
2. `select VARIABLE_VALUE into @b from global_status where VARIABLE_NAME='Innodb_buffer_pool_pages_total';`
3. `select @a/@b;`

刷脏页的时候，如果脏页旁边也是脏页的话，如果参数`innodb_flush_neighbors`设置为1，那么连着的脏页会一起flush，而如果设置为0的话（默认），flush就只有一个脏页，在固态硬盘时代，IOPS已经不是瓶颈了，所以刷自己就好

#### binlog

- 是逻辑日志，当中的内容是更新操作对查找数据字段的值修改多少，多是SQL语句
- 存在于Server层中
- 主要用于存储全部的更新操作记录，用于重现历史时刻

**查看binlog**：`mysqlbinlog -vv mysql-bin.000001 `

#### 日志的记录过程

比如执行`update t_user set count = count+1 where id = 3`这条更新语句，执行过程如下：

1. 执行器调用存储引擎的接口获取id=3这一行数据，存储引擎通过主键索引直接在B树中获取到数据。如果数据在内存中，则直接返回内存中的数据，否则需要到磁盘中读入内存，再返回数据给执行器。
2. 执行器获取到数据后，更新数据，比如原来count=99，更新后count的值变为100，将更新后的数据行发送回存储引擎。
3. 存储引擎将数据行写入到内存，然后生成redo log，状态为prepare，通知执行器自身执行完了，随时可以提交事务。
4. 执行器生成binlog，写入磁盘，调用存储引擎的提交事务接口
5. 存储引擎提交事务，将redo log的状态更新为commit

------

### 事务

事务简单来说就是一组数据库操作，要么全部执行成功，要么全部执行失败

#### 事务隔离级别

- 读未提交（read uncommitted）
- 读提交（read committed）
- 可重复读（repeatable read）
- 串行化（serializable）

#### 事务隔离的实现

事务隔离的具体实现是使用视图（read-view），可重复读是在事务开始的时候创建视图，读提交是在sql语句执行前重新算出一个新的视图，读未提交直接返回记录的最新值，串行化是加锁

对数据行进行更新操作会产生一个回滚日志，能够获取到数据行的历史数据，而视图就指向了数据行的回滚日志，从而能够获取到不同版本的数据，当没有视图指向旧的回滚日志的时候，才会删除回滚日志

需要注意的是，长事务会导致大量的回滚日志滞留，空间消耗大

#### 视图

这儿说的视图是支持读提交和可重复读隔离级别实现的`consistent read view`, 是InnoDB在实现MVVC时用到的一致性视图，它没有物理结构，主要的作用就是事务执行期间用来定义“我能看到什么数据”

#### 快照

每一条行记录是有多个数据版本的，每一次事务的更新操作都会生成一个数据版本，并且把事务的`transaction id`赋值给这个数据版本的事务ID，记为`row trx_id`

##### 实现快照？

InnoDB利用了所有数据都有多个版本的特性，实现了秒级创建快照的能力。对于可重复读，在事务启动的时候，对于数据版本大于该事务的数据版本，不认就行，当然了，对于还没有提交的事务，自然也是不可见的。

> 在实现上，InnoDB为每个事务都构造了一个数组，用来保存在当前事务启动瞬间，活跃的所有事务ID，数组里面事务ID最小的记为低水位，当前系统里面已经创建过的事务ID最大值+1为高水位，这个视图数组和高水位，就组成了当前事务的一致性视图（read-view）

事务中的更新逻辑？

> 事务中的更新数据都是先读后写的，而这个读是当前读，即是读当前最新的值，如果写的过程发现数据被其他事务上了写锁，则阻塞等待

为什么表的结构不支持可重复读？

> 因为表结构没有对应的行数据，也没有`row trx_id`，因此只能遵循当前读的逻辑

selete语句如果加了锁，也是当前读：1.加读锁`select k from t where id=1 lock in share mode;`; 2.加写锁`select k from t where id=1 for update;`

每一条记录在修改的时候都会记录一条回滚日志`undo log`，可以通过回滚操作得到之前的值，当记录的回滚日志不被事务需要的时候会被删除，也就是说没有比这个回滚日志更早的read-view时候会被删除

对于长事务，意味着会保留大量的回滚日志，存在很老的事务视图，对于可能访问的数据，数据的回滚记录都需要保留

#### 事务的启动方式

1. 显示启动，`begin`或`start transaction`，提交`commit`，回滚`rollback`
2. `set autocommit=0`,意味着只执行一个`select`，事务就会启动，直到`commit`或者`rollback`,或者断开连接

建议将`autocommit`设置为1，通过显示语句启动事务

`commit work and chain`:在`autocommit`为1的情况下，则效果为提交事务并且自动启动下一个事务

`begin/start transaction`命令并不是一个事务的起点，在执行第一个操作InnoDB表语句时，事务才真正启动；而`start transaction with consistent snapshot`则是马上启动一个事务

#### 如何避免长事务

- 应用开发端
  1. 确认使用了`set autocommit=0`
  2. 确认是否有不必要的只读事务，即查询操作也加上了事务
  3. 业务连接数据库的时候，设置`SET MAX_EXCUTION_TIME`来控制每条语句执行的最长时间，避免单个语句意外执行太长时间
- 数据库端
  1. 监控`information_schema.Innodb_trx`表，设置长事务阈值，超过就报警
  2. Percona的pt-kill工具
  3. 在业务功能测试阶段输出所有的general_log，分析日志行为提前发现问题
  4. 设置`innodb_undo_tablespaces`值为2（或者更大），这样undo就不会使用系统表空间，即ibdata1，而是使用独立的表空间，一般名称为undo001,undo002，当出现长事务导致回滚段过大，方便清理



查询长事务，持续60s以上的事务：`select * from information_schema.innodb_trx where TIME_TO_SEC(timediff(now(),trx_started))>60`

------

### 索引

索引其实就是为了加速查找数据的速度，常用的数据结构包括散列表、有序数组和搜索树，MySQL使用B+数，是多叉的，N的值约为1200左右，借此极大降低了树的高度，减少了读取磁盘的次数，索引是在存储引擎进行实现的，以下讨论的都是InnoDB下的索引设计

#### 索引类型

主键索引也叫作聚簇索引，叶子节点存放的是整行的数据，非主键索引也叫作二级索引，叶子节点存放的是主键的值，意味着当使用二级索引的时候，只能找到主键的值，如果需要其他的键值，需要到主键索引中查找整行的数据，这个过程称为回表

覆盖索引，意思是在二级索引中就能够获得需要查找的数据字段，不需要回表，覆盖索引可以显著减少树的搜索次数

联合索引，B+树的索引结构，可以利用索引的“最左前缀”，来定位记录，联合索引字段的顺序，一般是最常用的字段放在最前面，可以少维护索引树，其次使用“索引下推”来减少回表的次数，最左前缀可以是联合索引的最左 N 个字段，也可以是字符串索引的最左 M 个字符

#### 关于主键索引

为什么要使用自增主键？

> `NOT NULL PRIMARY KEY AUTO_INCREMENT`，每次增加数据，都是追加在最后面，对于主键索引树不会有节点的分裂和合并，如果是使用业务字段做主键，因为不一定每次都是追加在最后面，会导致树节点的分裂和合并,则写入数据维护索引树的成本更高；
>
> 其次，二级索引存储的是主键的值，如果是使用业务字段做主键，如果业务字段类型为长字符串，那么则二级索引的叶子节点需要的存储空间更高

可以使用业务逻辑字段作为主键索引的场景：

> Key-Value场景。只有一个索引，并且该索引是唯一的索引。而如果使用了自增主键，那么就会需要搜索两棵索引树

#### 索引下推

MySQL 5.6 引入的索引下推优化（index condition pushdown)， 可以在索引遍历过程中，对索引中包含的字段先做判断，直接过滤掉不满足条件的记录，减少回表次数。

比如有联合索引`(name, sex)`，查询语句如下：

`select * from t_user where name like "徐%" and sex = 1`

对于无索引下推的情况，记录(徐小白, 2)这条记录会进行回表操作，获取整行的数据，然后判断set

而对于索引下推，由于有联合索引，记录(徐小白, 2)这条记录直接就可以判断不符合查询条件，因此无需回表



#### 索引的重建

索引可能因为页分裂等维护操作，导致结构有空缺，通过重建索引能够按顺序构建索引，使结构更紧凑，也节省空间。

非主键索引的重建可以直接删除索引，然后再添加

删除：`alter table T drop index k;`

添加：`alter table T add index(k);`

而主键索引，删除主键和创建主键都会将整个表重建，所以使用语句下面的一条语句即可：`alter table T engine=InnoDB`

#### 唯一索引和普通索引

主键是肯定需要保证唯一性的，所以这儿所说的是非主键索引

##### change buffer

当对数据进行更新的时候，如果数据页在内存中，则直接更新，如果不在内存中，则将更新操作缓存在`change buffer`中，在数据页由于查询被加载到内存之后，进行`merge`操作来更新数据，或者在空闲的时候，将更新操作写入磁盘从而更新磁盘中的数据信息

在查询数据的时候，唯一索引和普通索引的性能相差无几，而在更新数据的时候，对于唯一索引，所有的更新操作都需要判断操作是否违反唯一性约束，需要将数据页加载进内存，所以唯一索引没必要是用change buffer，因此只有普通索引需要使用change buffer

对于写多读少的业务，如果使用了change buffer+普通索引，能够大大减少访问磁盘的次数，数据的更新都累计在change buffer中，能够批量性地对数据进行更新；而对于写完之后马上读取数据的，change buffer就没有存在的必要了，反而需要维护change buffer而需要耗费资源，因此change buffer记录的变更越多，收益越大

change buffer VS redo log

> redo log 主要节省的是随机写磁盘的IO消耗（转为顺序写），而change buffer主要节省的是随机读磁盘的IO消耗

#### 怎么对字符串字段进行索引

1. 直接创建完整索引，如果字段长度过长，则很占用空间
2. 创建前缀索引，减少存储空间，但会增加扫描的行数，而且不能使用覆盖索引
3. 对于识别度比较低的字符串创建前缀索引，如身份证号，由于前面的字符都是一样的，所以可以存储字符串的倒序，再在倒序的基础上创建前缀索引，如此一来，该索引不支持范围查询
4. 创建hash字段索引，添加一个字段值为hash值，性能稳定，也是不能支持范围查询

前缀索引的长度？

```
select count(distinct email) as L from User;
select count(distinct left(email,4)) as L4,
count(distinct left(email,5)) as L5,
count(distinct left(email,6)) as L6,
count(distinct left(email,7)) as L7 from User;
```

reverse操作CPU消耗比Hash操作小，而hash字段索引几乎扫描行数可以稳定在1，而倒序索引的话扫描行数可能很多



### 全局锁、表级锁、行锁

#### 全局锁

全局锁的使用场景是，做全库逻辑备份，使用`Flush tables with read lock(FTWRL)`，而对于使用InnoDB引擎的表，建议使用`mysqldump`，使用参数`-single-transaction`，在导数据前会启动一个事务，来确保拿到一致性视图，由于MVCC的支持，该过程中数据是可以正常更新的

#### 表级锁

##### 表锁

表锁的语法是`lock table T read/write`，

##### 元数据锁（meta data lock, MDL）

元数据锁不需要显示使用，在访问表的时候会自动加上，对表做增删改查的时候会加上MDL读锁，对表做结构变更的时候会加上MDL写锁

如何安全地给表加字段？

> 首先检查是否有长事务，保证没有长事务存在，其次，对SQL语句设置等待时间，超过等待时间则放弃，不要阻塞后面的语句，通过重复执行SQL语句来实现业务

#### 行锁

在InnoDB引擎中，行锁是在需要的时候才加上去的，但是直到事务结束后才释放，这个就是两阶段锁协议，因此如果事务需要锁多各行，把最可能造成锁冲突的，最可能影响并发度的锁放到最后

行锁的实现？

> innodb行锁是通过锁索引记录来实现的，如果更新的列没有建立索引是会对扫描的数据逐行加锁的

当出现死锁的时候处理？

> 参数`innodb_deadlock_detect`一般设置为`on`，表示开启死锁检测功能，发现死锁后，主动回滚其中的一条事务，让其他事务得以执行

在高并发量的情况下，死锁检测需要耗费大量的CPU资源，对于性能问题解决如下

> 1. 如果能够确保业务中一定不会出现死锁，那么可以临时把死锁检测给关掉，但是存在风险
> 2. 在数据库服务端控制并发量，在中间件或者MySQL中，对于相同行的更新，在进入引擎之前排队
> 3. 将更新一行的逻辑变为多行，需要根据业务逻辑做详细设计，当某一部分记录超出临界值的时候需要做特殊处理

### information_schema

该库中包括了三个关于锁的表，依次如下：

- innodb_trx：记录了事务的相关信息
- innodb_locks：记录了当前锁的相关信息
- innodb_waits：记录了锁等待的对应关系

------

### 读写分离

在分布式环境下，为了保证DB的高可用性和负载均衡，借鉴Master-Slave的思想，将读数据的SQL语句发送到Slave库中，将写数据的SQL语句发送到Master库中，Master和Slave保持数据的一致性，如果Master库挂掉了，则Slave临时成为Master

由于不同的SQL语句发送给不同的服务器，对于Tomcat来说有点麻烦，所以在Tomcat和DB中间加入MySQL Proxy这个中间层处理SQL语句的分发



### 排序
order by操作

线程会在内存中开辟一个空间`sort buffer`用来排序，当`sort_buffer_size`大于要排序的数据量时，使用快排在内存中进行内部排序，如果小于那么会利用磁盘临时文件辅助排序，使用归并进行外部排序，

使用以下的方法，来确定一个排序语句是否使用了临时文件?

```
/* 打开 optimizer_trace，只对本线程有效 */
SET optimizer_trace='enabled=on'; 
/* @a 保存 Innodb_rows_read 的初始值 */
select VARIABLE_VALUE into @a from  performance_schema.session_status where variable_name = 'Innodb_rows_read';
/* 执行语句 */
select city, name,age from t where city='杭州' order by name limit 1000; 
/* 查看 OPTIMIZER_TRACE 输出 */
SELECT * FROM `information_schema`.`OPTIMIZER_TRACE`\G
/* @b 保存 Innodb_rows_read 的当前值 */
select VARIABLE_VALUE into @b from performance_schema.session_status where variable_name = 'Innodb_rows_read';
/* 计算 Innodb_rows_read 差值 */
select @b-@a;
```

`number_of_tep_files`指示使用了多少临时文件

#### 全字段排序

初始化sort_buffer，在二级索引中查找相应的数据，根据主键回表查找整行的数据，将数据放到sort buffer中，对sort buffer中的数据按照字段进行排序，将排好序的数据返回给客户端

#### rowid排序

如果一行的字段有很多的话，那么内存中能够放入的行数会很少，要分成为很多个临时的文件，排序的性能很差，因此可以设置
`SET max_length_for_sort_data = 16`,控制了用于排序的行数据的长度，如果超过了这个长度，那么进入sort buffer的字段只有主键和排序字段，最后排好序后需要进行回表查找完整的数据，所以此时的`select @b-@a`需要增加相应的行数

#### 全字段排序和rowid排序

- 对于InnoDB表来说，全字段排序能够减少磁盘的访问，因此会被优先选择
- 对于内存表来说，rowid排序的回表只是简单地根据数据行的位置，直接访问内存得到数据，并不会访问磁盘，因此使用rowid排序

#### 避免排序

- 如果有联合索引，那么可以避免排序，只需要在联合索引进行检索，回表查找相应数据按照顺序记录数据就可以(Using index condition)
- 如果联合索引实现了覆盖索引，那么直接在联合索引处就能够获取足够的信息，不需要回表(Using index)

#### 有覆盖索引的排序

> `select * from t where city in ("杭州","苏州") ordr by name limit 100;`，有覆盖索引(city,name)

- 执行 select * from t where city=“杭州” order by name limit 100; 这个语句是不需要排序的，客户端用一个长度为 100 的内存数组 A 保存结果。
- 执行 select * from t where city=“苏州” order by name limit 100; 用相同的方法，假设结果被存进了内存数组 B。
- 现在 A 和 B 是两个有序数组，然后你可以用归并排序的思想，得到 name 最小的前 100 值，就是我们需要的结果了。

### count操作

InnoDB执行count操作需要将数据从存储引擎一行行读取到Server层，然后累积计数，如果数据量大的话，那么执行时间会很久

#### 怎么解决？

##### 用缓存系统（如Redis）存储计数值

然而毕竟内存数据会丢失，所以需要将数据写入到磁盘中，不过即使Redis正常工作，还是可能存在不一致，因为Redis数据更新和数据表的数据插入毕竟是两个操作，不是原子的，如果有查询在这两个操作之间进行了查询那么返回的数据就是不一致的

##### 数据库表来保存计数值

由于InnoDB要支持事务，所以不能直接把count（）直接存储起来，然后查询的时间直接返回现成的，也正是因为事务，使用表格来存储计数值，能够解决数据不一致的问题...

#### 不同的count

优化器对count（*）进行了优化，不需要把全部字段取出来，不取值，速度更快

性能上

> count(*) = count(1) > count(主键id) > count(字段)

### delete

每个InnoDB表数据存储在一个以.ibd为后缀的文件中

InnoDB删除数据只是在数据上标记为删除，对于单个数据或者页数据为了效率都是可复用的，因此删除了之后磁盘空间大小是不会变化的，甚至插入数据会导致数据页的分裂，也会导致很多空洞产生

#### 重建表

对于数据的空洞，可以通过重建表来收缩表空间`alter table A engine=InnoDB`，这个过程就是在临时表中依次插入原表中的数据，然后将临时表替换为原表

重建表的过程，会按照页数据容量的15/16进行填充数据（1/16留给update使用）

#### Online DDL

在往临时表插入数据的过程中，如果有新的数据更新，那么会丢失，所以在生成临时文件的过程中，将所有对原表的操作记录在一个日志文件（row log）中，临时文件生成后，将日志文件的操作应用到临时表中，从而能够在重建表的时候对数据进行更新

alter在启动的时候需要获取MDL写锁，然后在真正拷贝数据的时候，就退化为MDL读锁了，从而能够支持数据的更新

- 重建表的语句完整是`alter table T engine=innodb, ALGORITHM=inplace;`，即是时Online的，而`ALGORITHM=copy;`的话不支持重建表同时更新
- DDL过程如果是Online的话，那么一定是inplace
- inplace的DDL，有可能不是Online的，添加全文索引（FULLTEXT index）和空间索引（SPATIAL index）就属于这种情况



### 随机数

- `order by rand()`使用了内存临时表(Using temporary)，内存临时表排序的时候是使用rowid排序方法
- `tmp_table_size`：内存临时表的大小，默认是16M，如果临时表大小超过了`tmp_table_size`，那么内存临时表就会转成磁盘临时表

#### 优先队列排序算法(堆排序)

当要排序的数据超过了tmp_table_size时，就不使用内存临时表了，如果limit a中a的值比较小，如果a的数据量不超过sort_buffer_size，那么会使用该算法，可以通过查看"filesort_priority_queue_optimization"中的"chosen字段是否为true来看是否使用了该算法

#### 取随机数的方法

```
取一个
mysql> select count(*) into @C from t;
set @Y = floor(@C * rand());
set @sql = concat("select * from t limit ", @Y, ",1");
prepare stmt from @sql;
execute stmt;
DEALLOCATE prepare stmt;
取三个
mysql> select count(*) into @C from t;
set @Y1 = floor(@C * rand());
set @Y2 = floor(@C * rand());
set @Y3 = floor(@C * rand());
set @M = max(@Y1,@Y2,@Y3)
set @N = min(@Y1,@Y2,@Y3)
select * from t limit N, M-N+1;
```

------

### 备份例子

```
set session transaction isolation level repeatable read;设置隔离级别
start transaction with consistent snapshop;得到一致性视图
savepoint sp;
show create table t1;
select * from t1;
rollback to savepoint sp;
继续对其他表重复上述过程
```

### 慢查询日志
slow log

#### 查看设置

- `show variables like 'slow_query%';`
- `show variables like 'long_query_time';`

#### 设置

- `set global slow_query_log='ON';`
- `set global slow_query_log_file='/usr/local/mysql/data/slow.log';`
- `set global long_query_time=1;`
- 重启客户端



### 执行很慢的原因

#### 查询长时间不返回

##### 等MDL锁

1. show processlist看到State是“Waiting for table metadata lock"
2. 查找相应的pid，`select blocking_pid from sys.schema_table_lock_waits;`
3. KILL PID

##### 等Flush

1. show processlist看到State是“Waiting for table flush"
2. 但是一般flush的操作是很快的，所以应该是flush被其他线程A给堵住了
3. 将A线程给杀死

##### 等行锁

1. mysql> select * from t where id=1 lock in share mode; 
2. 该命令需要加读锁，如果已经有一个事务在这行持有一个写锁，那么select语句就会被阻塞
3. 查找这行持有写锁的信息：mysql> select * from t sys.innodb_lock_waits where locked_table=`'test'.'t'`\G
4. 可以看到“blocking_pid"就是阻塞的pid
5. KILL PID

#### 查询慢

##### one

| session A                                      | session B                                 |
| ---------------------------------------------- | ----------------------------------------- |
| start transaction with consistent snapshot;    | ~                                         |
| ~                                              | update t set c=c+1 where id=1 //执行100次 |
| select * from t where id=1;                    |                                           |
| select * from t where id=1 lock in share mode; |                                           |

- lock in share mode是当前读，会直接读到c的最新值
- 而普通的是一致性读，需要从c最新的值开始，一直undo执行了100次后，将1返回，执行慢

##### two

- a字段定义为varchar(10),执行`mysql> select * from table_a where b='1234567890abcd';`
- mysql会将字符串截断为10个char的长度，如果表中N行，由于是select*，所以需要回表N次，但是每次查出整行，到server层一判断发现a字段不相同，最终结果返回空，如果N很大的话，那么花费的时间很多



### 避免对字段进行函数操作

对索引字段做函数操作，可能会破坏索引值的有序性，因此优化器就决定放弃走树搜索功能，查询速度很慢

#### 对字段做了函数操作

- select * from TABLE where id + 1 = 100;
- select * from TABLE where month(t) = 7;
- 解决方式是不对字段使用函数，而是使用范围

#### 隐式类型转换

- select * from TABLE where tradeid = 100;(tradeid为varchar) 
- 在sql中，字符串和数字作比较的话，是将字符串转换成为数字
- 上面的语句等价于select * from TABLE where CAST(tradeid as signed int) = 100

#### 隐式字符编码转换

1. `select d.* from tradelog l, trade_detail d where d.tradeid=l.tradeid and l.id=2;`
2. 由于表d的字符集是utf8，而表l的字符集是utf8mb4，因此上面对字段进行了函数操作
3. 优化方法
   - 把trade_detail表中的tradeid字段的字符集修改为utf8mb4： `alter table trade_detail modify tradeid varchar(32) CHARACTER SET     utf8mb4 default null;`
   - `select d.* from tradelog l , trade_detail d where d.tradeid=CONVERT(l.tradeid USING utf8) and l.id=2; `



### union all 和union

- 都是将两个select结果集进行合并，区别如下
- 对于有重复的行，union会进行去重，而union all不会；
- 对于有序的结果，union会重新进行排序，而union all只是简单拼接两个结果
- 性能的话当然是union all更快

### 反引号

mysql语句的表名和字段都会添加反引号(`\``)，目的是为了区分Mysql的保留字和普通字符，当使用了保留字作为字段的话不加反引号会报错，比如

```
create table `test` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `desc` varchar(255)
)
```

### Command

- `explain MySQL语句`：查看语句的执行情况
- `force index(k)`：强制优化器使用索引k
- `set long_query_time=0`：接下来的SQL语句都会记录到慢查询日志（slow log）中

### 优化

大小表join使用mapjoin
字段和值类型匹配
在关联之前，将条件过滤放到字句
查询sql中过滤掉脏数据
避免笛卡尔积 
先groupby 后在join
增加分区信息，减少sql解析时的内存占用

#### select

- 对于查询结果只有一条的，加上`limit 1`，这样找到结果后就不会再继续扫描
- 如果允许重复并且不要排序结果，使用union all而不是union

### Interesting SQL

- `update tbl_name set str_col="xxx" = "yyy"` 等同于 `update tbl_name set str_col=0`
- `select id,str_col from tbl_name where str_col="xxx" = "yyy";`


### 多表join
阿里巴巴java开发手册说，禁止超过3个表以上的join，而且需要确保索引存在。优化的方式是，拆分sql语句为多条简单的sql语句，整体上来看，多条简单sql语句效率会比一条多表join的sql语句要高