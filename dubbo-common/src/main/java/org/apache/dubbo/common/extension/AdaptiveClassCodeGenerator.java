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
package org.apache.dubbo.common.extension;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.utils.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Code generator for Adaptive class
 */
public class AdaptiveClassCodeGenerator {

    private static final Logger logger = LoggerFactory.getLogger(AdaptiveClassCodeGenerator.class);

    private static final String CLASSNAME_INVOCATION = "org.apache.dubbo.rpc.Invocation";

    private static final String CODE_PACKAGE = "package %s;\n";

    private static final String CODE_IMPORTS = "import %s;\n";

    private static final String CODE_CLASS_DECLARATION = "public class %s$Adaptive implements %s {\n";

    private static final String CODE_METHOD_DECLARATION = "public %s %s(%s) %s {\n%s}\n";

    private static final String CODE_METHOD_ARGUMENT = "%s arg%d";

    private static final String CODE_METHOD_THROWS = "throws %s";

    private static final String CODE_UNSUPPORTED = "throw new UnsupportedOperationException(\"The method %s of interface %s is not adaptive method!\");\n";

    private static final String CODE_URL_NULL_CHECK = "if (arg%d == null) throw new IllegalArgumentException(\"url == null\");\n%s url = arg%d;\n";

    private static final String CODE_EXT_NAME_ASSIGNMENT = "String extName = %s;\n";

    private static final String CODE_EXT_NAME_NULL_CHECK = "if(extName == null) "
                    + "throw new IllegalStateException(\"Failed to get extension (%s) name from url (\" + url.toString() + \") use keys(%s)\");\n";

    private static final String CODE_INVOCATION_ARGUMENT_NULL_CHECK = "if (arg%d == null) throw new IllegalArgumentException(\"invocation == null\"); "
                    + "String methodName = arg%d.getMethodName();\n";


    private static final String CODE_EXTENSION_ASSIGNMENT = "%s extension = (%<s)%s.getExtensionLoader(%s.class).getExtension(extName);\n";

    private static final String CODE_EXTENSION_METHOD_INVOKE_ARGUMENT = "arg%d";

    private final Class<?> type;

    private String defaultExtName;

    public AdaptiveClassCodeGenerator(Class<?> type, String defaultExtName) {
        this.type = type;
        this.defaultExtName = defaultExtName;
    }

    /**
     * test if given type has at least one method annotated with <code>Adaptive</code>
     */
    private boolean hasAdaptiveMethod() {
        return Arrays.stream(type.getMethods()).anyMatch(m -> m.isAnnotationPresent(Adaptive.class));
    }

    /**
     * generate and return class code
     */
    public String generate() {
        //判断目标接口是否有方法标注了adaptive注解，如果没有则抛出异常
        // no need to generate adaptive class since there's no adaptive method found.
        if (!hasAdaptiveMethod()) {
            throw new IllegalStateException("No adaptive method exist on extension " + type.getName() + ", refuse to create the adaptive class!");
        }

        StringBuilder code = new StringBuilder();
        //生成package代码：package + type所在包名，
        //例如package com.alibaba.dubbo.rpc;
        code.append(generatePackageInfo());
        //生成import信息，这里只引入了ExtensionLoader类，其他的类都是通过全限定名字符串拼接的方式引入的
        //import + ExtensionLoader全限定名
        //例如import com.alibaba.dubbo.common.extension.ExtensionLoader;
        code.append(generateImports());
        //生成类代码：public class + type简单类名 + $Adaptive + implements + type全限定名 + {
        //例如public class Protocol$Adaptive implements com.alibaba.dubbo.rpc.Protocol {
        code.append(generateClassDeclaration());

        Method[] methods = type.getMethods();
        //为所有方法生成实现代码
        for (Method method : methods) {
            code.append(generateMethod(method));
        }
        // 生成类结束符号
        code.append("}");

        if (logger.isDebugEnabled()) {
            logger.debug(code.toString());
        }
        //返回生成的class代码
        return code.toString();
    }

    /**
     * generate package info
     */
    private String generatePackageInfo() {
        return String.format(CODE_PACKAGE, type.getPackage().getName());
    }

    /**
     * generate imports
     */
    private String generateImports() {
        return String.format(CODE_IMPORTS, ExtensionLoader.class.getName());
    }

    /**
     * generate class declaration
     */
    private String generateClassDeclaration() {
        return String.format(CODE_CLASS_DECLARATION, type.getSimpleName(), type.getCanonicalName());
    }

    /**
     * generate method not annotated with Adaptive with throwing unsupported exception
     */
    private String generateUnsupported(Method method) {
        return String.format(CODE_UNSUPPORTED, method, type.getName());
    }

    /**
     * get index of parameter with type URL
     */
    private int getUrlTypeIndex(Method method) {
        int urlTypeIndex = -1;
        Class<?>[] pts = method.getParameterTypes();
        for (int i = 0; i < pts.length; ++i) {
            if (pts[i].equals(URL.class)) {
                urlTypeIndex = i;
                break;
            }
        }
        return urlTypeIndex;
    }

    /**
     * generate method declaration
     */
    private String generateMethod(Method method) {
        //生成返回值信息
        String methodReturnType = method.getReturnType().getCanonicalName();
        //生成方法名信息
        String methodName = method.getName();
        //生成方法体信息
        String methodContent = generateMethodContent(method);
        //生成方法参数信息
        String methodArgs = generateMethodArguments(method);
        //生成方法抛出异常信息
        String methodThrows = generateMethodThrows(method);
        return String.format(CODE_METHOD_DECLARATION, methodReturnType, methodName, methodArgs, methodThrows, methodContent);
    }

    /**
     * generate method arguments
     */
    private String generateMethodArguments(Method method) {
        Class<?>[] pts = method.getParameterTypes();
        return IntStream.range(0, pts.length)
                        .mapToObj(i -> String.format(CODE_METHOD_ARGUMENT, pts[i].getCanonicalName(), i))
                        .collect(Collectors.joining(", "));
    }

    /**
     * generate method throws
     */
    private String generateMethodThrows(Method method) {
        Class<?>[] ets = method.getExceptionTypes();
        if (ets.length > 0) {
            String list = Arrays.stream(ets).map(Class::getCanonicalName).collect(Collectors.joining(", "));
            return String.format(CODE_METHOD_THROWS, list);
        } else {
            return "";
        }
    }

    /**
     * generate method URL argument null check
     */
    private String generateUrlNullCheck(int index) {
        return String.format(CODE_URL_NULL_CHECK, index, URL.class.getName(), index);
    }

    /**
     * generate method content
     */
    private String  generateMethodContent(Method method) {
        //获取方法上标注的Adaptive注解，dubbo会使用该注解的值作为key从URL中获取扩展名，例如dubbo://127.0.0.1:20880/com.alibaba.dubbo.demo.DemoService?proxy=jdk
        Adaptive adaptiveAnnotation = method.getAnnotation(Adaptive.class);
        StringBuilder code = new StringBuilder(512);
        //dubbo不会为没有标注Adaptive注解的方法生成实现代码，而是直接抛出异常
        if (adaptiveAnnotation == null) {
            return generateUnsupported(method);
        } else {
            //获取方法参数中类型为URL的参数的下标，如果没有则返回-1
            int urlTypeIndex = getUrlTypeIndex(method);

            // found parameter in URL type
            //urlTypeIndex != -1表示方法参数中有类型为URL的参数
            if (urlTypeIndex != -1) {
                // Null Point check
                //为url生成null检查代码，例如if (arg0 == null) throw new IllegalArgumentException("url == null")
                //为url类型参数生成赋值代码，例如URL arg0 = arg1;
                code.append(generateUrlNullCheck(urlTypeIndex));
            } else {
                // did not find parameter in URL type
                //如果方法参数中没有类型为URL的参数，则生成间接获取URL的代码，例如URL url = argN.getURL();
                code.append(generateUrlAssignmentIndirectly(method));
            }
            //获取方法上的Adaptive注解的值，例如@Adaptive({"protocol"})，则value为["protocol"]
            //如果Adaptive注解的值为null，做额外处理，例如把LoadBalance处理成load.balance
            String[] value = getMethodAdaptiveValue(adaptiveAnnotation);
            //检测方法参数中是否有Invocation类型的参数
            boolean hasInvocation = hasInvocationArgument(method);
            //生成Invocation类型参数为null的代码，例如if (arg2 == null) throw new IllegalArgumentException("invocation == null");
            code.append(generateInvocationArgumentNullCheck(method));
            //生成获取扩展名的代码
            //例如String extName = ( url.getProtocol() == null ? "dubbo" : url.getProtocol() )
            //例如String extName = url.getMethodParameter(methodName, "loadbalance", "random");
            //例如String extName = url.getParameter("client", url.getParameter("transporter", "netty"))
            code.append(generateExtNameAssignment(value, hasInvocation));
            // check extName == null?
            //生成扩展名为null的代码，例如if (arg3 == null) throw new IllegalStateException("Fail to get extension");
            code.append(generateExtNameNullCheck(value));
            //生成扩展名获取代码，例如Protocol extension = (Protocol)ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(extName)
            code.append(generateExtensionAssignment());

            // return statement
            //生成目标方法调用代码，例如return extension.refer(arg0, arg1)
            code.append(generateReturnAndInvocation(method));
        }

        return code.toString();
    }

    /**
     * generate code for variable extName null check
     */
    private String generateExtNameNullCheck(String[] value) {
        return String.format(CODE_EXT_NAME_NULL_CHECK, type.getName(), Arrays.toString(value));
    }

    /**
     * generate extName assigment code
     */
    private String generateExtNameAssignment(String[] value, boolean hasInvocation) {
        // TODO: refactor it
        String getNameCode = null;
        //defaultExtName为@SPI注解的值，如果没有则为null
        //从后向前遍历@Adaptive注解的值，例如@Adaptive({"protocol", "cluster"})，则value为["protocol", "cluster"]
        //如果value[i]不为protocol，则生成获取扩展名的代码，例如String extName = url.getMethodParameter(methodName, "cluster", "failover");
        //如果value[i]为protocol，则生成获取扩展名的代码，例如String extName = ( url.getProtocol() == null ? "dubbo" : url.getProtocol() )
        for (int i = value.length - 1; i >= 0; --i) {
            //当i为最后一个元素时
            if (i == value.length - 1) {
                if (null != defaultExtName) {
                    //protocol是url的一部分，可通过getProtocol方法获取，其他的则是从URL参数中获取，两种类型获取方式不同
                    if (!"protocol".equals(value[i])) {
                        //hasInvocation表示方法参数中是否有Invocation类型的参数
                        if (hasInvocation) {
                            //生成的代码功能等价于下面的代码：
                            //url.getMethodParameter(methodName, value[i], defaultExtName)
                            //以loadbalance接口的select方法为例，生成的代码如下：
                            //url.getMethodParameter(methodName, "loadbalance", "random")
                            getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                        } else {
                            //生成的代码功能等价于下面的代码：
                            //url.getParameter(value[i], defaultExtName)
                            getNameCode = String.format("url.getParameter(\"%s\", \"%s\")", value[i], defaultExtName);
                        }
                    } else {
                        //生成的代码功能等价于下面的代码：
                        //url.getProtocol() == null ? "dubbo" : url.getProtocol()
                        getNameCode = String.format("( url.getProtocol() == null ? \"%s\" : url.getProtocol() )", defaultExtName);
                    }
                } else {
                    //默认扩展名称为空
                    if (!"protocol".equals(value[i])) {
                        if (hasInvocation) {
                            getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                        } else {
                            getNameCode = String.format("url.getParameter(\"%s\")", value[i]);
                        }
                    } else {
                        getNameCode = "url.getProtocol()";
                    }
                }
            } else {
                if (!"protocol".equals(value[i])) {
                    if (hasInvocation) {
                        getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                    } else {
                        getNameCode = String.format("url.getParameter(\"%s\", %s)", value[i], getNameCode);
                    }
                } else {
                    getNameCode = String.format("url.getProtocol() == null ? (%s) : url.getProtocol()", getNameCode);
                }
            }
        }
        //生成getNameCode判空代码
        return String.format(CODE_EXT_NAME_ASSIGNMENT, getNameCode);
    }

    /**
     * @return
     */
    private String generateExtensionAssignment() {
        return String.format(CODE_EXTENSION_ASSIGNMENT, type.getName(), ExtensionLoader.class.getSimpleName(), type.getName());
    }

    /**
     * generate method invocation statement and return it if necessary
     */
    private String generateReturnAndInvocation(Method method) {
        //判断方法是否有返回值
        String returnStatement = method.getReturnType().equals(void.class) ? "" : "return ";

        String args = IntStream.range(0, method.getParameters().length)
                .mapToObj(i -> String.format(CODE_EXTENSION_METHOD_INVOKE_ARGUMENT, i))
                .collect(Collectors.joining(", "));

        return returnStatement + String.format("extension.%s(%s);\n", method.getName(), args);
    }

    /**
     * test if method has argument of type <code>Invocation</code>
     */
    private boolean hasInvocationArgument(Method method) {
        Class<?>[] pts = method.getParameterTypes();
        return Arrays.stream(pts).anyMatch(p -> CLASSNAME_INVOCATION.equals(p.getName()));
    }

    /**
     * generate code to test argument of type <code>Invocation</code> is null
     */
    private String generateInvocationArgumentNullCheck(Method method) {
        Class<?>[] pts = method.getParameterTypes();
        return IntStream.range(0, pts.length).filter(i -> CLASSNAME_INVOCATION.equals(pts[i].getName()))
                        .mapToObj(i -> String.format(CODE_INVOCATION_ARGUMENT_NULL_CHECK, i, i))
                        .findFirst().orElse("");
    }

    /**
     * get value of adaptive annotation or if empty return splitted simple name
     */
    private String[] getMethodAdaptiveValue(Adaptive adaptiveAnnotation) {
        String[] value = adaptiveAnnotation.value();
        // value is not set, use the value generated from class name as the key
        if (value.length == 0) {
            String splitName = StringUtils.camelToSplitName(type.getSimpleName(), ".");
            value = new String[]{splitName};
        }
        return value;
    }

    /**
     * get parameter with type <code>URL</code> from method parameter:
     * <p>
     * test if parameter has method which returns type <code>URL</code>
     * <p>
     * if not found, throws IllegalStateException
     */
    private String generateUrlAssignmentIndirectly(Method method) {
        //获取方法的参数类型列表
        Class<?>[] pts = method.getParameterTypes();

        Map<String, Integer> getterReturnUrl = new HashMap<>();
        // find URL getter method
        for (int i = 0; i < pts.length; ++i) {
            //遍历某一类型参数的所有方法，找到可返回url的get方法
            for (Method m : pts[i].getMethods()) {
                String name = m.getName();
                //1、方法名以get开头，或者方法名长度大于3
                //2、方法是public的
                //3、方法不是static的
                //4、方法的参数数量为0
                //5、方法的返回值是URL类型
                if ((name.startsWith("get") || name.length() > 3)
                        && Modifier.isPublic(m.getModifiers())
                        && !Modifier.isStatic(m.getModifiers())
                        && m.getParameterTypes().length == 0
                        && m.getReturnType() == URL.class) {
                    getterReturnUrl.put(name, i);
                }
            }
        }

        if (getterReturnUrl.size() <= 0) {
            // getter method not found, throw
            //所有参数中没有可返回URL的get方法，抛出异常
            throw new IllegalStateException("Failed to create adaptive class for interface " + type.getName()
                    + ": not found url parameter or url attribute in parameters of method " + method.getName());
        }

        Integer index = getterReturnUrl.get("getUrl");
        if (index != null) {
            return generateGetUrlNullCheck(index, pts[index], "getUrl");
        } else {
            Map.Entry<String, Integer> entry = getterReturnUrl.entrySet().iterator().next();
            return generateGetUrlNullCheck(entry.getValue(), pts[entry.getValue()], entry.getKey());
        }
    }

    /**
     * 1, test if argi is null
     * 2, test if argi.getXX() returns null
     * 3, assign url with argi.getXX()
     */
    private String generateGetUrlNullCheck(int index, Class<?> type, String method) {
        // Null point check
        StringBuilder code = new StringBuilder();
        //生成arg参数为空的判断
        code.append(String.format("if (arg%d == null) throw new IllegalArgumentException(\"%s argument == null\");\n",
                index, type.getName()));
        //生成arg参数的get方法返回值为空的判断
        code.append(String.format("if (arg%d.%s() == null) throw new IllegalArgumentException(\"%s argument %s() == null\");\n",
                index, method, type.getName(), method));
        //生成url赋值语句，例如：URL url = argN.getUrl();
        code.append(String.format("%s url = arg%d.%s();\n", URL.class.getName(), index, method));
        return code.toString();
    }

}
