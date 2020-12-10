

explain是SQL提供的能够分析语句的执行情况，例如

```mysql
explain select id, name from book where create_date > XXX AND type = 3
```

explain的结果有以下几项：

![pic](F:\GithubMy\my\pic\explain语句的结果解释)