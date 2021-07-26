### GEO

```shell
geoadd location 1 1 "apple"

geoadd location 3 4 "banana"

geoadd location 4 4 "durian"

计算距离，返回单位为km
geodist location apple banana km

给定经纬度，返回指定半径内的元素，即附近的人功能
GEORADIUS 

和GEORADIUS类似，不过不是给定随机的经纬度，而是使用已经存在的元素作为经纬度
GEORADIUSBYMEMBER 
```

