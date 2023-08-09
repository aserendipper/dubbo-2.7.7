## 功能模块
容器模块：是一个Standlone的容器，以简单的Main加载Spring启动，因为服务通常不需要Tomcat/JBoss等Web容器的特性，没必要用Web容器去加载服务。

1、dubbo-container-api：定义了com.alibaba.dubbo.container.Container接口，并提供加载所有容器启动的Main类。
2、实现dubbo-container-api：
* dubbo-container-spring：提供Spring容器的实现。
* dubbo-container-log4j：提供Log4j容器的实现。
* dubbo-container-logback：提供Logback容器的实现。