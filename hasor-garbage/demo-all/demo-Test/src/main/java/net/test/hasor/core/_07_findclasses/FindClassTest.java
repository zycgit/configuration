/*
 * Copyright 2008-2009 the original 赵永春(zyc@hasor.net).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.test.hasor.core._07_findclasses;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.core.Module;
import net.hasor.core.context.AbstractAppContext;
import org.junit.Test;
import org.more.builder.ReflectionToStringBuilder;
import org.more.builder.ToStringStyle;
import org.more.logger.LoggerHelper;
/**
 * 本示列演示如何通过Hasor扫描类。
 * @version : 2013-8-11
 * @author 赵永春 (zyc@hasor.net)
 */
public class FindClassTest {
    @Test
    public void findClassTest() throws IOException, URISyntaxException, InterruptedException {
        System.out.println("--->>findClassTest<<--");
        //1.创建一个标准的 Hasor 容器。
        AppContext appContext = Hasor.createAppContext();
        //
        // 请注意： Hasor默认扫描的范围是、net.*,com.*,org.*。 如要自定义扫描范围有两种办法：
        //         1.通过hasor-config.xml配置文件配置。
        //         2.通过 ((AbstractEnvironment)appContext.getEnvironment()).setSpanPackage(...); 方法修改
        //
        //1.查找所有Hasor模块（实现了Module接口的类）。
        Set<Class<?>> facesFeature = appContext.getEnvironment().findClass(Module.class);
        LoggerHelper.logInfo("find %s.", ReflectionToStringBuilder.toString(facesFeature, ToStringStyle.SIMPLE_STYLE));
        //2.查找AbstractAppContext的子类
        Set<Class<?>> subFeature = appContext.getEnvironment().findClass(AbstractAppContext.class);
        LoggerHelper.logInfo("find %s.", ReflectionToStringBuilder.toString(subFeature, ToStringStyle.SIMPLE_STYLE));
    }
}