

### 查询语句

对字符串进行拼接

```mysql
SELECT 
    GROUP_CONCAT(DISTINCT country
        ORDER BY country
        SEPARATOR ';')
FROM
```





### 配置与系统语句

查看MySQL的配置参数：`show variables like 'wait_timeout';`

查看客户端的连接：`show PROCESSLIST;`

### 参数配置

每次事务的 redo log 都直接持久化到磁盘：`innodb_flush_log_at_trx_commit = 1`

每次事务的 bin log 都直接持久化到磁盘：`sync_binlog = 1`

change buffer 的大小最多只能占用 buffer pool 的 50%：`innodb_change_buffer_max_size= 50`