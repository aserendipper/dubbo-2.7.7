package org.apache.dubbo.registry.zookeeper.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * @author aserendipper
 * @date 2023/6/28
 * @desc Curator异步API使用
 */
public class Main2 {

    public static void main(String[] args) throws Exception {
        String zkAddress = "127.0.0.1:2181";
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkAddress, retryPolicy);
        client.start();
        //添加CuratorListener监听器，针对不同的事件进行处理
        client.getCuratorListenable().addListener(new CuratorListener() {
            @Override
            public void eventReceived(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                switch (curatorEvent.getType()) {
                    case CREATE:
                        System.out.println("CREATE:" + curatorEvent.getPath());
                        break;
                    case DELETE:
                        System.out.println("DELETE:" + curatorEvent.getPath());
                        break;
                    case EXISTS:
                        System.out.println("EXISTS:" + curatorEvent.getPath());
                        break;
                    case GET_DATA:
                        System.out.println("GET_DATA:" + curatorEvent.getPath());
                        break;
                    case SET_DATA:
                        System.out.println("SET_DATA:" + curatorEvent.getPath());
                        break;
                    case CHILDREN:
                        System.out.println("CHILDREN:" + curatorEvent.getPath());
                        break;
                    default:
                }
            }
        });
        //注意：下面所有的操作都添加了inBackground()方法，转换为后台操作
        client.create().withMode(CreateMode.PERSISTENT).inBackground().forPath("/user", "test".getBytes());
        client.checkExists().inBackground().forPath("/user");
        client.setData().inBackground().forPath("/user", "setData-Test".getBytes());
        client.getData().inBackground().forPath("/user");
        for (int i = 0; i < 3; i++) {
            client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).inBackground().forPath("/user/child-" + i, String.valueOf(i).getBytes());
        }
        client.getChildren().inBackground().forPath("/user");
        //添加BackgroundCallback回调接口
        client.getChildren().inBackground(new BackgroundCallback() {
            @Override
            public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                System.out.println("in background:" + curatorEvent.getType() + "," + curatorEvent.getPath());
            }
        }).forPath("/user");
        client.delete().deletingChildrenIfNeeded().inBackground().forPath("/user");
        System.in.read();
    }
}
