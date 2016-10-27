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
package org.more.webui.freemarker.loader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.more.webui.freemarker.loader.mto.AbstractTemplateObject;
import org.more.webui.freemarker.loader.mto.ClassPath_TemplateObject;
import org.more.webui.freemarker.loader.mto.File_TemplateObject;
import org.more.webui.freemarker.loader.mto.String_TemplateObject;
import org.more.webui.freemarker.loader.mto.URL_TemplateObject;
import org.more.webui.resource.IResourceLoader;
/**
 * 处理配置文件中添加的模板。
 * @version : 2011-9-14
 * @author 赵永春 (zyc@byshell.org) 
 */
public class ConfigTemplateLoader implements ITemplateLoader, IResourceLoader {
    private Map<String, AbstractTemplateObject> objectMap = null;
    //
    public ConfigTemplateLoader() {
        this.objectMap = new HashMap<String, AbstractTemplateObject>();
    };
    public String getType() {
        return this.getClass().getSimpleName();
    }
    /**将classpath中的一个资源地址作为模板内容添加到装载器中。*/
    public void addTemplate(String name, String classPath) {
        this.objectMap.put(name, new ClassPath_TemplateObject(classPath, Thread.currentThread().getContextClassLoader()));
    };
    /**将{@link File}地址作为模板内容添加到装载器中。*/
    public void addTemplate(String name, File filePath) {
        this.objectMap.put(name, new File_TemplateObject(filePath));
    };
    /**将{@link URL}地址作为模板内容添加到装载器中。*/
    public void addTemplate(String name, URL urlPath) {
        this.objectMap.put(name, new URL_TemplateObject(urlPath));
    };
    /**将字符串作为模板内容添加到装载器中。*/
    public void addTemplateAsString(String name, String templateString) {
        this.objectMap.put(name, new String_TemplateObject(templateString));
    }
    public Object findTemplateSource(String arg0) throws IOException {
        if (this.objectMap.containsKey(arg0) == false)
            return null;
        return this.objectMap.get(arg0);
    }
    public long getLastModified(Object arg0) {
        AbstractTemplateObject mto = (AbstractTemplateObject) arg0;
        return mto.lastModified();
    }
    public Reader getReader(Object arg0, String encoding) throws IOException {
        if (arg0 instanceof AbstractTemplateObject == false)
            return null;
        AbstractTemplateObject mto = (AbstractTemplateObject) arg0;
        mto.openObject();
        return mto.getReader(encoding);
    }
    public void closeTemplateSource(Object arg0) throws IOException {
        if (arg0 instanceof AbstractTemplateObject == true)
            ((AbstractTemplateObject) arg0).closeObject();
    }
    public URL getResource(String resourcePath) throws IOException {
        if (this.objectMap.containsKey(resourcePath) == false)
            return null;
        return new URL("template-mto://" + resourcePath);
    }
    public InputStream getResourceAsStream(String resourcePath) throws IOException {
        if (this.objectMap.containsKey(resourcePath) == false)
            return null;
        return this.objectMap.get(resourcePath).getInputStream();
    }
    public void resetState() {
        this.objectMap.clear();
    }
}