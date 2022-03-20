

### 为什么会有Redis碎片？

1. Redis向操作系统申请内存的时候，可能会申请更多的内存，比如Redis需要24字节的内存，其向操作系统申请32字节的内存空间，具体要看内存分配器的行为，默认使用 jemalloc
2. 频繁修改Redis的数据，主要是删除数据，Redis不会马上释放内存给操作系统



### 怎么查看Redis内存碎片？

通过命令`info memory`查看

![image-20220309225408120](C:\Users\default-dog\Desktop\image\image-20220309225408120.png)

Redis 内存碎片率的计算公式：

`mem_fragmentation_ratio` （内存碎片率）= `used_memory_rss` (操作系统实际分配给 Redis 的物理内存空间大小)/ `used_memory`(Redis 内存分配器为了存储数据实际申请使用的内存空间大小)

通常情况下，我们认为 `mem_fragmentation_ratio > 1.5` 的话才需要清理内存碎片。



### 清理

Redis4.0-RC3 版本以后自带了内存整理，可以避免内存碎片率过大的问题。

需要执行命令：`config set activedefrag yes`

具体什么时候清理需要通过下面两个参数控制：

```shell
# 内存碎片占用空间达到 500mb 的时候开始清理
config set active-defrag-ignore-bytes 500mb
# 内存碎片率大于 1.5 的时候开始清理
config set active-defrag-threshold-lower 50
```

由于Redis碎片清理操作可能会影响Redis性能，我们可以通过下面的参数控制：

```shell
# 内存碎片清理所占用 CPU 时间的比例不低于 20%
config set active-defrag-cycle-min 20
# 内存碎片清理所占用 CPU 时间的比例不高于 50%
config set active-defrag-cycle-max 50
```

对于Redis集群，由于Redis重启就可以做到内存碎片重新整理，可以考虑将内存碎片率高的节点设置为从节点，然后重启实例后再提供正常使用。

