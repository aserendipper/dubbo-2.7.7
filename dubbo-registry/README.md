## 模块功能
注册中心模块：基于注册中心下发地址的集群方式，以及对各种注册中心的抽象。

1、dubbo-register-api：抽象注册中心的注册和发现接口。  
2、其他模块实现dubbo-register-api，提供对应的注册中心实现。   
3、dubbo-register-default：对应Simple注册中心的实现。  