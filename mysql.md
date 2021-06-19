



对字符串进行拼接

```mysql
SELECT 
    GROUP_CONCAT(DISTINCT country
        ORDER BY country
        SEPARATOR ';')
FROM
```

