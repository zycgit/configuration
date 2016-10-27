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
package org.more.webui.components.page;
import org.more.webui.component.UIComponent;
import org.more.webui.components.page.PageCom.Mode;
import org.more.webui.context.ViewContext;
/**
 * 分页组建，页码
 * @version : 2012-5-15
 * @author 赵永春 (zyc@byshell.org)
 */
public abstract class AbstractItem extends UIComponent {
    public String getPageLinkAsTemplate(ViewContext viewContext) {
        PageCom pageCom = (PageCom) this.getParent();
        return pageCom.getPageLinkAsTemplate(viewContext);
    }
    protected abstract Mode getRenderMode();
}