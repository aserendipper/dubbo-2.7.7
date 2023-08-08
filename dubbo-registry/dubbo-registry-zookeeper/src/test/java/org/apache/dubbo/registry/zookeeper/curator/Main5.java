package org.apache.dubbo.registry.zookeeper.curator;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @author aserendipper
 * @date 2023/6/28
 * @desc Curator中的三种Watcher
 */
public class Main5 {

    public static void main(String[] args) throws Exception {
        String zkAddress = "127.0.0.1:2181";
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkAddress, retryPolicy);
        client.start();
        //创建NodeCache，监听"/user"节点
        NodeCache nodeCache = new NodeCache(client, "/user");
        //start()方法有个boolean类型的参数，默认是false
        //如果设置为true，那么NodeCache在第一次启动的时候就会立刻从ZooKeeper上读取对应节点的数据内容，并保存在Cache中
        nodeCache.start(true);
        if (nodeCache.getCurrentData() != null) {
            System.out.println("NodeCache节点初始化数据为：" + new String(nodeCache.getCurrentData().getData()));
        } else {
            System.out.println("NodeCache节点数据为空");
        }
        //添加监听器
        nodeCache.getListenable().addListener(() -> {
            String data = new String(nodeCache.getCurrentData().getData());
            System.out.println("NodeCache节点路径：" + nodeCache.getCurrentData().getPath() + "，节点数据为：" + data);
        });
        //创建PathChildrenCache，监听"/user"节点
        PathChildrenCache childrenCache = new PathChildrenCache(client, "/user", true);
        //StartMode指定的初始化的模式
        //BUILD_INITIAL_CACHE：同步初始化
        //POST_INITIALIZED_EVENT：异步初始化，初始化之后会触发事件
        //NORMAL：普通异步初始化
        childrenCache.start(StartMode.BUILD_INITIAL_CACHE);
        List<ChildData> children = childrenCache.getCurrentData();
        System.out.println("获取子节点列表：");
        //如果是BUILD_INITIAL_CACHE模式，那么在启动时就会同步初始化，此时getCurrentData()返回的结果不为空
        for (ChildData child : children) {
            System.out.println(new String(child.getData()));
        }
        childrenCache.getListenable().addListener((curatorFramework, pathChildrenCacheEvent) -> {
            System.out.println(LocalDateTime.now() + "，监听到事件：" + pathChildrenCacheEvent.getType());
            if (pathChildrenCacheEvent.getType().equals(Type.INITIALIZED)) {
                System.out.println("PathChildrenCache:子节点初始化成功...");
            } else if (pathChildrenCacheEvent.getType().equals(Type.CHILD_ADDED)) {
                System.out.println("PathChildrenCache添加子节点：" + pathChildrenCacheEvent.getData().getPath());
                System.out.println("PathChildrenCache子节点数据：" + new String(pathChildrenCacheEvent.getData().getData()));
            } else if (pathChildrenCacheEvent.getType().equals(Type.CHILD_REMOVED)) {
                System.out.println("PathChildrenCache删除子节点：" + pathChildrenCacheEvent.getData().getPath());
            } else if (pathChildrenCacheEvent.getType().equals(Type.CHILD_UPDATED)) {
                System.out.println("PathChildrenCache修改子节点路径：" + pathChildrenCacheEvent.getData().getPath());
                System.out.println("PathChildrenCache修改子节点数据：" + new String(pathChildrenCacheEvent.getData().getData()));
            }
        });
        //创建TreeCache，监听"/user"节点
        TreeCache treeCache = TreeCache.newBuilder(client, "/user").setCacheData(true).build();
        treeCache.getListenable().addListener((curatorFramework, treeCacheEvent) -> {
            if (treeCacheEvent.getData() != null) {
                System.out.println("TreeCache,type=" + treeCacheEvent.getType() + " path=" + treeCacheEvent.getData().getPath() + " data=" + new String(treeCacheEvent.getData().getData()));
            } else {
                System.out.println("TreeCache,type=" + treeCacheEvent.getType());
            }
        });
        treeCache.start();
        System.in.read();

        Proxy.newProxyInstance(Main5.class.getClassLoader(), new Class[]{}, (proxy, method, args1) -> {
            System.out.println("proxy");
            return null;
        });
    }
}
