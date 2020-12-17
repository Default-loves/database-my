

#### 总述

explain是SQL提供的能够分析语句的执行情况，例如

```mysql
explain select id, name from book where create_date > XXX AND type = 3
```

explain的结果有以下几项：

![pic](F:\GithubMy\my\pic\explain语句的结果解释)

下面就针对一些重要的字段进行说明

#### select_type

语句执行的类型，通常来说我们推荐写多条简单的SQL语句，而不是一条复杂的SQL语句。

![图片](F:\GithubMy\my\pic\explain_select_type)

#### type

type 显示的是访问类型，是较为重要的一个指标，结果值从好到坏依次是：system > const > eq_ref > ref > fulltext > ref_or_null > index_merge > unique_subquery > index_subquery > range > index > ALL ，一般来说，得保证查询至少达到 range 级别，最好能达到 ref。

![图片](F:\GithubMy\my\pic\explain_type)

#### extra

其他说明

![图片](F:\GithubMy\my\pic\explain_extra)