package org.apache.dubbo.registry.zookeeper.curator;

import java.util.List;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Op.Create;
import org.apache.zookeeper.WatchedEvent;

/**
 * @author aserendipper
 * @date 2023/6/28
 * @desc Curator添加Watcher
 */
public class Main4 {

    public static void main(String[] args) throws Exception {
        String zkAddress = "127.0.0.1:2181";
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkAddress, retryPolicy);
        client.start();
        client.create().withMode(CreateMode.PERSISTENT).forPath("/user", "test".getBytes());
        //通过usingWatcher()方法添加一个Watcher
        List<String> children = client.getChildren().usingWatcher(new CuratorWatcher() {
            @Override
            public void process(WatchedEvent watchedEvent) throws Exception {
                System.out.println(watchedEvent.getType() + "," + watchedEvent.getPath());
            }
        }).forPath("/user");
        System.out.println(children);
        System.in.read();
    }
}
