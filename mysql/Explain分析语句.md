

我们可以通过`mysql`的`explain`命令来分析语句的执行情况：

```mysql
mysql> explain select count(*) from park_car_out;
+----+-------------+--------------+------------+-------+---------------+-----------------+---------+------+------+----------+-------------+
| id | select_type | table        | partitions | type  | possible_keys | key             | key_len | ref  | rows | filtered | Extra       |
+----+-------------+--------------+------------+-------+---------------+-----------------+---------+------+------+----------+-------------+
|  1 | SIMPLE      | park_car_out | NULL       | index | NULL          | carOut_cardType | 5       | NULL |  277 |   100.00 | Using index |
+----+-------------+--------------+------------+-------+---------------+-----------------+---------+------+------+----------+-------------+
1 row in set, 1 warning (0.00 sec)
```



每个字段的含义如下：

![explain列含义](G:\GithubMy\my\database-my\mysql\img\explain列含义.png)





### type

type是访问类型，需要关注，从好到坏排序是：system > const > eq_ref > ref > fulltext > ref_or_null > index_merge > unique_subquery > index_subquery > range > index > ALL

![explain列含义-type](G:\GithubMy\my\database-my\mysql\img\explain列含义-type.png)



### Extra
Extra是语句查询的详细信息·

![explain列含义-Extra](G:\GithubMy\my\database-my\mysql\img\explain列含义-Extra.png)