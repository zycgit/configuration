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
package org.more.webui.resource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
/**
 * 给定资源路径装载该资源对象。
 * @version : 2011-9-14
 * @author 赵永春 (zyc@byshell.org) 
 */
public interface IResourceLoader {
    public URL getResource(String resourcePath) throws IOException;
    /**装载指定资源。*/
    public InputStream getResourceAsStream(String resourcePath) throws IOException;
}