 

### 使用Redis
添加依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```

不需要额外的配置，就可以注入`RedisTemplate`或`StringRedisTemplate`

```java
@Autowired
StringRedisTemplate stringRedisTemplate;
```

当然了，默认的`***Template`配置使用上和性能都不好，强烈建议进行额外的配置，见**RedisTemplate配置**



### RedisTemplate配置

```java
// 创建 redisTemplate Bean
@Bean
public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    // 我们为了自己开发方便，一般直接使用 <String, Object>
    RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
    template.setConnectionFactory(redisConnectionFactory);

    // String 的序列化
    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    // key采用String的序列化方式
    template.setKeySerializer(stringRedisSerializer);
    // hash的key也采用String的序列化方式
    template.setHashKeySerializer(stringRedisSerializer);

    // Json序列化配置
    RedisSerializer<Object> jackson2JsonRedisSerializer = redisSerializer();
    // value序列化方式采用jackson
    template.setValueSerializer(jackson2JsonRedisSerializer);
    // hash的value序列化方式采用jackson
    template.setHashValueSerializer(jackson2JsonRedisSerializer);
    template.afterPropertiesSet();
    return template;
}

//创建JSON序列化器
@Bean
@SuppressWarnings("deprecation")
public RedisSerializer<Object> redisSerializer() {
    // Json序列化配置
    Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
    return jackson2JsonRedisSerializer;
}
```





### RedisTemplate和StringRedisTemplate 

包含序列化(serialization)和连接管理(connection management)

使用 `RedisTemplate `读写 Redis，默认的序列化和反序列化使用的是 java 的实现，即`JdkSerializationRedisSerializer`，该算法会将数据序列化为字节数组后保存在Redis中，因此我们在Redis客户端查看数据的时候，显示的内容是字节数组，不具备可读性

而`StringRedisTemplate `使用的是`StringRedisSerializer`

`StringRedisTemplate`继承了`RedisTemplate `，在构造器中设置了序列化方法

```java
public class StringRedisTemplate extends RedisTemplate<String, String> {
	public StringRedisTemplate() {
		setKeySerializer(RedisSerializer.string());
		setValueSerializer(RedisSerializer.string());
		setHashKeySerializer(RedisSerializer.string());
		setHashValueSerializer(RedisSerializer.string());
	}
}
```





### Redis客户端

Redis客户端主要有两个：Jedis和Lettuce



### @CacheXXX相关

方法的执行结果自动更新到`Redis`，避免手动编写重复的操作`Redis`的语句

pom.xml添加配置

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

然后配置`RedisTemplate`和`RedisCacheManager`，设置序列化方法、默认缓存时间、Key格式（将两个冒号变成1个冒号）等。

最后在启动类上添加注解`@EnableCaching`即可，具体见代码子模块`caching`

#### 具体注解

- @Cacheable：一般使用在查询方法，根据`key`去`Redis`查询数据，如果查询到则不执行`mysql`的查询方法，否则执行方法，并且将结果缓存到`Redis`中
- @CachePut：一般使用在新增方法，更新Redis数据信息
- @CacheEvict：一般使用在删除方法上，删除Redis的数据信息

#### 注解中的属性设置

- cacheNames/value：缓存名称（必填），指定缓存的命名空间；
- key：用于设置在命名空间中的缓存key值，可以使用SpEL表达式定义；
- unless：条件符合则不缓存；
- condition：条件符合则缓存。

比如我们有下面的方法

```java
@Cacheable(value = "article", key = "result.id")
public Article findByName(String name) {
	return articleMapper.selectByName(name);
}
```

那么对于id=3的数据，对应Redis的Key为`article:3`

### 其他

使用于 Redis Repository 中 RedisTemplate 需要无事务