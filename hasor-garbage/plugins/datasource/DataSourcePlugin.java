/*
 * Copyright 2002-2006 the original author or authors.
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
package net.hasor.plugins.datasource;
import java.util.List;
import javax.sql.DataSource;
import net.hasor.core.ApiBinder;
import net.hasor.core.Environment;
import net.hasor.core.Hasor;
import net.hasor.core.Settings;
import net.hasor.core.XmlNode;
import net.hasor.core.plugin.AbstractHasorPlugin;
import net.hasor.quick.plugin.Plugin;
import org.more.util.StringUtils;
/**
 * 
 * @version : 2013-10-8
 * @author 赵永春(zyc@hasor.net)
 */
@Plugin
public class DataSourcePlugin extends AbstractHasorPlugin {
    public void loadPlugin(ApiBinder apiBinder) throws Throwable {
        Environment env = apiBinder.getEnvironment();
        Settings settings = env.getSettings();
        //
        String defaultDS = settings.getString("hasor-jdbc.dataSourceSet.default");
        XmlNode[] dataSourceSet = settings.getXmlNodeArray("hasor-jdbc.dataSourceSet");
        if (dataSourceSet == null)
            return;
        //
        for (XmlNode dsSet : dataSourceSet) {
            List<XmlNode> dataSource = dsSet.getChildren("dataSource");
            for (XmlNode dsConfig : dataSource) {
                //1.DataSources
                String name = dsConfig.getAttribute("name");
                String dsFactoryClass = dsConfig.getAttribute("dsFactory");
                //
                Class<?> dsFactoryType = Thread.currentThread().getContextClassLoader().loadClass(dsFactoryClass);
                DataSourceFactory dsFactory = (DataSourceFactory) dsFactoryType.newInstance();
                //
                DataSource dataSourceObject = dsFactory.createDataSource(env, dsConfig);
                if (dataSourceObject == null) {
                    Hasor.logWarn("‘%s’ dataSource is null.", name);
                    continue;
                }
                Hasor.logInfo("‘%s’ dataSource is defined.", name);
                apiBinder.bindingType(name, DataSource.class).toInstance(dataSourceObject);/*Bind DataSource.*/
                apiBinder.bindingType(name, DataSourceFactory.class).toInstance(dsFactory);/*Bind Factory.*/
                //default
                if (StringUtils.equalsIgnoreCase(name, defaultDS)) {
                    apiBinder.bindingType(DataSource.class).toInstance(dataSourceObject);
                    Hasor.logInfo("‘%s’ dataSource is default.", name);
                }
            }
        }
    }
}