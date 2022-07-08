InfluxDB 提供的 tag 功能，可以为每一个指标设置多个标签，并且 tag 有索引，可以对 tag 进行条件搜索或分组。但是，tag 只能保存有限的、可枚举的标签，不能保存 URL 等信息，否则可能会出现high series cardinality 问题，导致占用大量内存，甚至是 OOM。你可以点击这里，查看 series 和内存占用的关系。对于 InfluxDB，我们无法把 URL 这种原始数据保存到数据库中，只能把数据进行归类，形成有限的 tag 进行保存。

