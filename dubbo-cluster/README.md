## 模块功能
集群模块：将多个服务提供方伪装为一个服务提供方，包括：负载均衡、集群容错、路由、分组聚合等。集群的地址列表可以是静态配置的，也可以是由注册中心下发(由dubbo-registry提供)。

1、容错
* com.alibaba.dubbo.rpc.cluster.Cluster接口 + com.alibaba.dubbo.rpc.cluster.support包。
* Cluster将Directory中的多个Invoker伪装为一个Invoker，对外提供服务，伪装过程包括容错逻辑，调用失败后，重试另一个。  

2、目录
* com.alibaba.dubbo.rpc.cluster.Directory接口 + com.alibaba.dubbo.rpc.cluster.directory包。
* Directory是Invoker的集合，可以把它看成List<Invoker>，但与List不同的是，它的值可能是动态变化的，比如注册中心推送变更。

3、路由
* com.alibaba.dubbo.rpc.cluster.Router接口 + com.alibaba.dubbo.rpc.cluster.router包。
* 负责从多个Invoker中按路由规则选出子集，比如读写分离、只读节点负载均衡等。

4、配置
* com.alibaba.dubbo.rpc.cluster.Configurator接口 + com.alibaba.dubbo.rpc.cluster.configurator包。

5、负载均衡
* com.alibaba.dubbo.rpc.cluster.LoadBalance接口 + com.alibaba.dubbo.rpc.cluster.loadbalance包。
* 负责从多个Invoker中选出具体的一个用于本次调用，选的过程包含了负载均衡算法，调用失败后，重选另一个。

6、合并结果
* com.alibaba.dubbo.rpc.cluster.Merger接口 + com.alibaba.dubbo.rpc.cluster.merger包。
* 合并返回结果，用于分组聚合。

