

安装流程：

https://www.cnblogs.com/xsge/p/13827288.html



### 修改密码

安装完mysql后，登录mysql，修改密码发现修改不了，显示如下：

```mysql
mysql> SET PASSWORD FOR 'root'@'localhost' = PASSWORD('123456');
ERROR 1819 (HY000): Your password does not satisfy the current policy requirements
```

MySQL安装时默认安装了 [validate_password](http://dev.mysql.com/doc/refman/5.7/en/validate-password-plugin.html)，MySQL的密码策略比较复杂。这个插件要求密码至少包含一个大写字母，一个小写字母，一个数字和一个特殊字符，并且密码长度至少8个字符。过于简单的密码，不会被通过。

我们需要将策略修改掉，执行命令，表示密码只验证长度，长度最小为1

```mysql
set global validate_password_policy=0;
set global validate_password_length=1;
```

然后我们通过修改命令后就成功了

```mysql
mysql> SET PASSWORD FOR 'root'@'localhost' = PASSWORD('123456');
Query OK, 0 rows affected, 1 warning (0.00 sec)
```

### 远程访问

```shell
grant all on *.* to 'root'@'%' identified by'123456';
```

