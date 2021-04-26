# database-my



### mysql数据导出与导入

```mysql
# 导出数据库test中的表格t_user
mysqldump -uroot -p123456 test t_user > db.sql
# 导出数据库test所有数据
mysqldump -uroot -p123456 test > db.sql
# 不导出create语句
mysqldump -uroot -p123456 --no-create-info test > db.sql

# 使用导出的db.sql还原数据
mysql -uroot -p123456 text < db.sql
```

```mysql
# 数据筛选后导出
SELECT * INTO OUTFILE '/var/lib/mysql-files/0426.txt'
FIELDS TERMINATED BY ',' 
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
FROM t_user;

# 将导出的数据导入到表格t_user
LOAD DATA INFILE '/var/lib/mysql-files/0426.txt' 
INTO TABLE t_user 
FIELDS TERMINATED BY ','  
OPTIONALLY ENCLOSED BY '"'  
LINES TERMINATED BY '\n';

# 需要注意的是导出的文件路径需要在mysql secure_file_priv路径下，通过语句查看
show variables like '%secure%';
```

























