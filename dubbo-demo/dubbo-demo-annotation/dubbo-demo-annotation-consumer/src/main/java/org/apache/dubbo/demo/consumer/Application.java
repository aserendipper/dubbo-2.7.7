/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.demo.consumer;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.apache.dubbo.demo.DemoService;
import org.apache.dubbo.demo.consumer.comp.DemoServiceComponent;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

public class Application {
    /**
     * In order to make sure multicast registry works, need to specify '-Djava.net.preferIPv4Stack=true' before
     * launch the application
     */
    public static void main(String[] args) {
        //使用注解的方式启动，加载ConsumerConfiguration配置类
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ConsumerConfiguration.class);
        context.start();
        //获取DemoServiceComponent实例
        DemoService service = context.getBean("demoServiceComponent", DemoServiceComponent.class);
        String hello = service.sayHello("world");
        System.out.println("result :" + hello);
    }

    //静态内部类，用于加载配置类
    @Configuration
    //启用Dubbo注解
    @EnableDubbo(scanBasePackages = "org.apache.dubbo.demo.consumer.comp")
    //加载配置文件
    @PropertySource("classpath:/spring/dubbo-consumer.properties")
    //扫描dubbo-demo-consumer-comp包下的组件
    @ComponentScan(value = {"org.apache.dubbo.demo.consumer.comp"})
    static class ConsumerConfiguration {

    }
}
