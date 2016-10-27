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
package org.more.webui.freemarker.parser;
import org.more.webui.component.UIComponent;
import org.more.webui.context.ViewContext;
import freemarker.core.TemplateElement;
/**
 * freemarker模板元素块钩子。
 * @version : 2012-5-14
 * @author 赵永春 (zyc@byshell.org)
 */
public interface ElementHook {
    /**开始处理遇到的模板标签*/
    public UIComponent beginAtBlcok(TemplateScanner scanner, TemplateElement e, UIComponent parent, ViewContext viewContext) throws ElementHookException;
    /**处理遇到的模板标签结束*/
    public void endAtBlcok(TemplateScanner scanner, TemplateElement e, UIComponent parent, ViewContext viewContext) throws ElementHookException;
}