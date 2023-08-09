## 模块功能
远程通信模块：提供通用的客户端和服务端的通信功能。

1、dubbo-remoting-zookeeper：相当于Zookeeper Client,和Zookeeper Server通信的客户端。  
2、dubbo-remoting-api：定义了Dubbo Client和Dubbo Server的通信接口。  
3、实现dubbo-remoting-api：
* dubbo-remoting-grizzly：基于Grizzly框架实现的Dubbo Client和Dubbo Server的通信功能。
* dubbo-remoting-http：基于Jetty或Tomcat实现。
* dubbo-remoting-mina：基于Mina框架实现。
* dubbo-remoting-netty：基于Netty3实现。
* dubbo-remoting-netty4：基于Netty4实现。
* dubbo-remoting-p2p：基于P2P实现。

4、从最小化的角度看，只需要看
* dubbo-remoting-api + dubbo-remoting-netty4。
* dubbo-remoting-zookeeper。