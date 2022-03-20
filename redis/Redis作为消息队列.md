

Redis使用**LIST**数据结构，从左边添加数据，右边消费数据，满足先进先出，实现消息队列

生产者添加数据：LPUSH

```shell
LPUSH key element[element...]

LPUSH my-queue apple banana water
```

消费者消费数据：RPOP

```shell
RPOP key

> RPOP my-queue
"apple"
> RPOP my-queue
"banana"
```



需要注意的是，Redis的LIST使用RPOP需要我们程序用一个线程死循环去不断获取，因为该方法是非阻塞的，当队列为空的时候不会阻塞，而是返回空

因此为了避免死循环消耗系统资源，我们使用阻塞的消费方法**BRPOP**，参数0表示无限期等待

```shell
BRPOP my-queue 0
```



### 消息可靠性

如果消费者调用了`BRPOP`获取了数据，不过还没来得及处理数据就宕机了，那么该数据将丢失。

Redis提供了RPOPLPUSH、BRPOPLPUSH（阻塞）来解决，具体的操作是从队列弹出消息后，将消息又添加到其他的队列，这两个操作是原子性的

因此如果数据没有处理服务器宕机了，我们可以备份队列中获取到数据，处理完消息后再删除掉备份队列中的消息即可

```shell
LPUSH my-queue apple banana water
# my-queue-back 为备份队列
BRPOPLPUSH my-queue my-queue-back
```

