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
package org.apache.dubbo.demo.provider;

import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

public class Application {
    
    public static void main(String[] args) throws Exception {
        //使用AnnotationConfigApplicationContext初始化spring容器
        //从ProviderConfiguration.class中加载spring配置
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ProviderConfiguration.class);
        //启动spring容器
        context.start();
        //阻塞main线程
        System.in.read();
    }

    @Configuration
    //使用@EnableDubbo注解开启基于注解的dubbo功能，@EnableDubbo注解指定包下的Bean都会被扫描，并做Dubbo服务暴露出去
    @EnableDubbo(scanBasePackages = "org.apache.dubbo.demo.provider")
    //使用@PropertySource注解加载properties文件
    @PropertySource("classpath:/spring/dubbo-provider.properties")
    static class ProviderConfiguration {
        //使用@Bean注解初始化RegistryConfig对象
        //相当于dubbo-provider.xml中的<dubbo:registry address="zookeeper://127.0.0.1:2181" />
        @Bean
        public RegistryConfig registryConfig() {
            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setAddress("zookeeper://127.0.0.1:2181");
            return registryConfig;
        }
    }
}
