package org.apache.dubbo.registry.zookeeper.curator;

import java.util.List;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

/**
 * @author aserendipper
 * @date 2023/6/28
 * @desc Curator基础API使用
 */
public class Main {

    public static void main(String[] args) throws Exception {
        //zookeeper集群地址，多个地址用逗号分隔
        String zkAddress = "127.0.0.1:2181";
        //重试策略，如果连接不上zookeeper，每隔1秒重试一次，最多重试3次
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        //创建Curator客户端
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkAddress, retryPolicy);
        client.start();
        //create()方法创建一个节点，withMode()方法指定节点类型，forPath()方法指定节点路径，最后一个参数是节点的数据
        String path = client.create().withMode(CreateMode.PERSISTENT).forPath("/user", "test".getBytes());
        System.out.println(path);
        //checkExists()方法检查节点是否存在，如果存在返回节点的状态信息，如果不存在返回null
        Stat stat = client.checkExists().forPath("/user");
        System.out.println(stat);
        //getData()方法获取节点的数据，返回byte[]类型的数据
        byte[] data = client.getData().forPath("/user");
        System.out.println(new String(data));
        //setData()方法更新节点的数据，返回Stat对象
        client.setData().forPath("/user", "data".getBytes());
        data = client.getData().forPath("/user");
        System.out.println(new String(data));
        //在/user节点下创建子节点，节点类型为EPHEMERAL_SEQUENTIAL，即临时顺序节点
        for (int i = 0; i < 3; i++) {
            client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/user/child-" + i, String.valueOf(i).getBytes());
        }
        //getChildren()方法获取子节点列表
        List<String> children = client.getChildren().forPath("/user");
        System.out.println(children);
        //delete()方法删除节点，deletingChildrenIfNeeded()方法指定如果存在子节点，同时删除子节点
        client.delete().deletingChildrenIfNeeded().forPath("/user");
    }
}
