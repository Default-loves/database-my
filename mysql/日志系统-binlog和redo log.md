### redo log

- 是物理日志，意思是记录的内容是更新操作对具体的数据页进行的操作
- 存在于InnoDB存储引擎中
- 由于在硬盘中对数据进行更新耗费时间很久，所以将更新操作记录在redo log中，此时内存中的数据和磁盘中的数据不一致，内存中数据所在的页称为“脏页”，会根据策略将内存数据写入到磁盘，这个过程称为`flush`
- 大小是固定的，比如4个各2G的文件，循环写入redo log，因此当redo log满时，需要等待flush过程，此时系统不能更新数据
- crash-safe，在系统异常重启后，会自动应用磁盘中的`redo log`文件，数据恢复到崩溃的前一刻，保证了数据的一致性，也就是说之前提交记录不会丢失

在内存中的数据更新之后，磁盘中的数据和内存中的数据不一致，我们称这个内存页为“脏页”，而一致的数据页，称为“干净页”

什么时候会进行flush？

- 当redo log满的时候，此时系统不能更新数据，导致系统处于不可用的状态
- 当内存满的时候，需要将长久不用的数据页淘汰，如果淘汰的是脏页，那么就需要将脏页写入磁盘
- 当系统空闲的时候
- MySQL正常关闭的时候

#### 参数innodb_flush_log_at_trx_commit

innodb_flush_log_at_trx_commit={0|1|2} 。指定何时将事务日志刷到磁盘，默认为1，这个也是建议设置成1。

-  0表示每秒将"log buffer"同步到"os buffer"且从"os buffer"刷到磁盘日志文件中。 
- 1表示每事务提交都将"log buffer"同步到"os buffer"且从"os buffer"刷到磁盘日志文件中。 
- 2表示每事务提交都将"log buffer"同步到"os buffer"但每秒才从"os buffer"刷到磁盘日志文件中。

#### 刷脏页的控制策略

MySql需要获取机器的性能，从而控制刷脏页的速度，参数`innodb_io_capacity`表示了主机IO的能力，这个值一般设置为磁盘的IOPS，而IOPS可以通过以下语句了解`fio -filename=$filename -direct=1 -iodepth1 -thread -rw=randrw -ioengine=psync -bs=16k -size=500M -numjobs=10 -runtime=10 -group_reporting -name=mytest`

InnoDB刷脏页的速度会考虑两个因素，一个是脏页比例，一个是redo log写盘速度，参数`innodb_max_dirty_pages_pct`是脏页比例上限，默认值为75%，InnoDB会根据当前的脏页比例算出一个0~100的值为K1，根据redo log写入序号跟checkpoint对应序号的差值算出一个0~100的值为K2，取较大值为R，之后引擎根据innodb_io_capacity定义的能力乘以R%来控制刷脏页页的速度

脏页比例计算：

```mysql
1. `select VARIABLE_VALUE into @a from global_status where VARIABLE_NAME='Innodb_buffer_pool_pages_dirty';`
2. `select VARIABLE_VALUE into @b from global_status where VARIABLE_NAME='Innodb_buffer_pool_pages_total';`
3. `select @a/@b;`
```

当查询操作的时候，由于内存不足需要flush掉历史脏页，会导致查询操作更慢。而且，如果脏页旁边也是脏页的话，且参数`innodb_flush_neighbors`设置为1，那么连着的脏页会一起flush，而如果设置为0的话（默认），flush就只有一个脏页，在固态硬盘时代，IOPS已经不是瓶颈了，所以刷自己就好，8.0版本参数`innodb_flush_neighbors`就已经默认是0了

### binlog

- 是逻辑日志，当中的内容是更新操作对查找数据字段的值修改多少，多是SQL语句
- 存在于Server层中
- 主要用于存储全部的更新操作记录，用于重现历史时刻，常用的是先通过全量备份恢复数据到某一天（每日一备份），然后应用binlog恢复数据到某一个时间点
- 通常会将binlog同步给其他存储系统，实现数据的同步
- sync_binlog 这个参数设置成 1 的时候，表示每次事务的 binlog 都持久化到磁盘。这个参数建议设置成 1，这样可以保证 MySQL 异常重启之后 binlog 不丢失。

**查看binlog**：`mysqlbinlog -vv mysql-bin.000001 `

binlog的格式：

- statement：记录的是原始的sql语句，在主备同步的时候，可能会导致主备不一致
- row：记录的是完整的数据修改内容，大小比statement大。这也是建议设置的，同时也方便误操作后修复数据
- mix：上面两者的混合。

### 日志的记录过程

比如执行`update t_user set count = count+1 where id = 3`这条更新语句，执行过程如下：

1. 执行器调用存储引擎的接口获取id=3这一行数据，存储引擎通过主键索引直接在B树中获取到数据。如果数据在内存中，则直接返回内存中的数据，否则需要到磁盘中读入内存，再返回数据给执行器。
2. 执行器获取到数据后，更新数据，比如原来count=99，更新后count的值变为100，将更新后的数据行发送回存储引擎。
3. 存储引擎将数据行写入到内存，然后生成redo log，状态为prepare，通知执行器自身执行完了，随时可以提交事务。
4. 执行器生成binlog，写入磁盘，调用存储引擎的提交事务接口
5. 存储引擎提交事务，将redo log的状态更新为commit

由于binlog和redo log是两个日志系统，因此我们需要进行上述的“两阶段提交”来保证两个日志系统数据的一致性

