

explain是SQL提供的能够分析语句的执行情况，例如

```mysql
explain select id, name from book where create_date > XXX AND type = 3
```

explain的结果有以下几项：

![pic](F:\GithubMy\my\pic\explain语句的结果解释)



#### select_type

![图片](F:\GithubMy\my\pic\explain_select_type)

#### type

type 显示的是访问类型，是较为重要的一个指标，结果值从好到坏依次是：system > const > eq_ref > ref > fulltext > ref_or_null > index_merge > unique_subquery > index_subquery > range > index > ALL ，一般来说，得保证查询至少达到 range 级别，最好能达到 ref。

![图片](F:\GithubMy\my\pic\explain_type)

#### extra

![图片](F:\GithubMy\my\pic\explain_extra)