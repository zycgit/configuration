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
package org.more.webui.context;
/**
 * bean管理器，该类继承Map接口接口后map会被加入到El上下文
 * @version : 2012-6-27
 * @author 赵永春 (zyc@byshell.org)
 */
public interface BeanManager {
    /**初始化。*/
    public void init(FacesConfig environment);
    /**获取需要的Bean对象。*/
    public <T> T getBean(Class<T> type);
}