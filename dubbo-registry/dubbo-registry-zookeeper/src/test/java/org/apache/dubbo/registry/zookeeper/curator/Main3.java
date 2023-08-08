package org.apache.dubbo.registry.zookeeper.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @author aserendipper
 * @date 2023/6/28
 * @desc Curator连接状态监听器
 */
public class Main3 {

    public static void main(String[] args) {
        String zkAddress = "127.0.0.1:2181";
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkAddress, retryPolicy);
        client.start();
        //添加ConnectionStateListener监听器，监听连接状态
        client.getConnectionStateListenable().addListener((curatorFramework, connectionState) -> {
            switch (connectionState) {
                case CONNECTED:
                    //第一次成功连接到zookeeper时触发，只触发一次
                    System.out.println("CONNECTED");
                    break;
                case SUSPENDED:
                    //连接丢失时触发
                    System.out.println("SUSPENDED");
                    break;
                case RECONNECTED:
                    //重连成功时触发
                    System.out.println("RECONNECTED");
                    break;
                case LOST:
                    //当连接丢失，并且重试失败时触发，会话已过期
                    System.out.println("LOST");
                    break;
                case READ_ONLY:
                    //当连接处于只读模式时触发
                    System.out.println("READ_ONLY");
                    break;
                default:
            }
        });
    }
}
