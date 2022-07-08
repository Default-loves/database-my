Redis Lua常用命令

- EVAL script numkeys key [key ...] arg [arg ...] 执行 Lua 脚本。
- EVALSHA sha1 numkeys key [key ...] arg [arg ...] 执行 Lua 脚本。
- SCRIPT EXISTS script [script ...] 查看指定的脚本是否已经被保存在缓存当中。
- SCRIPT FLUSH 从脚本缓存中移除所有脚本。
- SCRIPT KILL 杀死当前正在运行的 Lua 脚本。
- SCRIPT LOAD script 将脚本 script 添加到脚本缓存中，但并不立即执行这个脚本。



### EVAL 

命令格式：

```shell
EVAL script numkeys key [key ...] arg [arg ...]
```

- `script`是第一个参数，为 Lua 5.1脚本；
- 第二个参数`numkeys`指定后续参数有几个 key；
- `key [key ...]`，是要操作的键，可以指定多个，在 Lua 脚本中通过`KEYS[1]`, `KEYS[2]`获取；
- `arg [arg ...]`，参数，在 Lua 脚本中通过`ARGV[1]`, `ARGV[2]`获取。

```shell
> eval "return ARGV[1]" 0 100
100

> eval "return {ARGV[1],ARGV[2]}" 0 100 101
100
101

> eval "return {KEYS[1],KEYS[2],ARGV[1]}" 2 key1 key2 first second
key1
key2
first

> set article:001 "abc"
OK
> get article:001
abc
> eval "return redis.call('get', 'article:001')" 0
abc
> eval "return redis.call('get', KEYS[1])" 1 article:001
abc
> eval "return redis.call('get', ARGV[1])" 0 article:001
abc
```



### EVALSHA 

```shell
# 将脚本加载到Redis服务器上，返回唯一标识
SCRIPT LOAD script 
# 通过evalsha调用唯一标识即可直接执行对应的脚本
EVALSHA sha1 numkeys key [key ...] arg [arg ...] 
```

```shell
> script load "return 'hello Friday'"
25b7c4ec13dfc1ae48b196bdf516841a17181283
> evalsha 25b7c4ec13dfc1ae48b196bdf516841a17181283 0
hello Friday
```



### 脚本的测试方法

1. 编写JUnit测试用例
2. 从 Redis 3.2 开始，内置了 Lua debugger（简称`LDB`）, 可以使用 Lua debugger 对 Lua 脚本进行调试。



### 其他

Redis Lua 中内置了 cjson 函数，用于 json 的编解码。



### 具体例子

```lua
-- KEY[1]: 用户防重领取记录
local userHashKey = KEYS[1];
-- KEY[2]: 运营预分配红包列表
local redPacketOperatingKey = KEYS[2];
-- KEY[3]: 用户红包领取记录 
local userAmountKey = KEYS[3];
-- KEY[4]: 用户编号
local userId = KEYS[4];
-- 返回的结果，json格式
local result = {};
-- 判断用户是否领取过 
if redis.call('hexists', userHashKey, userId) == 1 then
  result['code'] = '1'; 
  return cjson.encode(result);
else
   -- 从预分配红包中获取红包数据
   local redPacket = redis.call('rpop', redPacketOperatingKey);
   if redPacket
   then
      local data = cjson.decode(redPacket);
      -- 加入用户ID信息
      data['userId'] = userId; 
      -- 把用户编号放到去重的哈希，value设置为红包编号
      redis.call('hset', userHashKey, userId, data['redPacketId']);
      --  用户和红包放到已消费队列里
      redis.call('lpush', userAmountKey, cjson.encode(data));
      -- 组装成功返回值
      result['redPacketId'] = data['redPacketId'];
      result['code'] = '0';
      result['amount'] = data['amount'];
      return cjson.encode(result);
   else
      -- 抢红包失败
      result['code'] = '-1';
      return cjson.encode(result);
   end 
end
```

