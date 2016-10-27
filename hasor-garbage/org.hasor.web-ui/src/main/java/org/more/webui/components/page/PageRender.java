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
import java.io.IOException;
import java.io.Writer;
import org.more.webui.components.page.PageCom.Mode;
import org.more.webui.context.ViewContext;
import org.more.webui.render.AbstractRender;
import org.more.webui.tag.TemplateBody;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;
/**
 * 
 * @version : 2012-5-18
 * @author 赵永春 (zyc@byshell.org)
 */
public class PageRender extends AbstractRender<PageCom> {
    @Override
    public String getClientType() {
        return "ui_Page";
    }
    @Override
    public String tagName(ViewContext viewContext, PageCom component) {
        return "ul";
    }
    /**无数据判断，有数据时始终返回true*/
    public boolean noData(boolean hasData, String noDateMode, String contains) {
        boolean doThis = true;
        if (hasData == false)
            if (noDateMode.contains(contains) == false)
                doThis = false;
        return doThis;
    }
    private void clearVar(ViewContext viewContext, TemplateBody arg3, PageCom component) throws TemplateModelException {
        component.renderMode = null;
        //
        viewContext.remove("isFirst");
        viewContext.remove("isLast");
        viewContext.remove("isCurrent");
        viewContext.remove("PageIndex");
        viewContext.remove("RowNum");
        viewContext.remove("RenderMode");//值可能是如下的几个：First, Prev, Item, Next, Last, NoDate
        viewContext.remove("PageLink");
        arg3.getEnvironment().setVariable("isFirst", null);
        arg3.getEnvironment().setVariable("isLast", null);
        arg3.getEnvironment().setVariable("isCurrent", null);
        arg3.getEnvironment().setVariable("PageIndex", null);
        arg3.getEnvironment().setVariable("RowNum", null);
        arg3.getEnvironment().setVariable("RenderMode", null);
        arg3.getEnvironment().setVariable("PageLink", null);
    }
    private void putFirstVar(ViewContext viewContext, TemplateBody arg3, Boolean isFirst) throws TemplateModelException {
        viewContext.put("isFirst", isFirst);
        //int indexPage = (Integer) DeepUnwrap.permissiveUnwrap(arg3.getEnvironment().getVariable("PageIndex"));
        arg3.getEnvironment().setVariable("isFirst", ObjectWrapper.DEFAULT_WRAPPER.wrap(isFirst));
    }
    private void putLastVar(ViewContext viewContext, TemplateBody arg3, Boolean isLast) throws TemplateModelException {
        viewContext.put("isLast", isLast);
        //int indexPage = (Integer) DeepUnwrap.permissiveUnwrap(arg3.getEnvironment().getVariable("PageIndex"));
        arg3.getEnvironment().setVariable("isLast", ObjectWrapper.DEFAULT_WRAPPER.wrap(isLast));
    }
    private void putCurrentVar(ViewContext viewContext, TemplateBody arg3, Boolean isCurrent) throws TemplateModelException {
        viewContext.put("isCurrent", isCurrent);
        //int indexPage = (Integer) DeepUnwrap.permissiveUnwrap(arg3.getEnvironment().getVariable("PageIndex"));
        arg3.getEnvironment().setVariable("isCurrent", ObjectWrapper.DEFAULT_WRAPPER.wrap(isCurrent));
    }
    private void putVar(ViewContext viewContext, TemplateBody arg3, Integer pageIndex, Integer rowNum, PageCom component, Mode renderMode) throws TemplateModelException {
        String pageLink = component.getPageLinkAsTemplate(viewContext);
        component.renderMode = renderMode;
        //
        viewContext.put("PageIndex", pageIndex);
        viewContext.put("RowNum", rowNum);
        viewContext.put("RenderMode", renderMode);
        viewContext.put("PageLink", pageLink);
        arg3.getEnvironment().setVariable("PageIndex", ObjectWrapper.DEFAULT_WRAPPER.wrap(pageIndex));
        arg3.getEnvironment().setVariable("RowNum", ObjectWrapper.DEFAULT_WRAPPER.wrap(rowNum));
        arg3.getEnvironment().setVariable("RenderMode", ObjectWrapper.DEFAULT_WRAPPER.wrap(renderMode));
        arg3.getEnvironment().setVariable("RenderMode", ObjectWrapper.DEFAULT_WRAPPER.wrap(pageLink));
    }
    @Override
    public void render(ViewContext viewContext, PageCom component, TemplateBody arg3, Writer writer) throws IOException, TemplateException {
        if (arg3 == null)
            return;
        /**起始号*/
        int startWith = component.getStartWith();
        /**总数*/
        int rowCount = component.getRowCount();
        /**页大小*/
        int pageSize = component.getPageSize();
        /**当前页码*/
        int current = component.getCurrentPage();
        /**可以分页的最大页码*/
        float _pageMod = (float) (rowCount - startWith) % (float) pageSize;
        float _pageMax = (float) (rowCount - startWith) / (float) pageSize;
        int maxPage = (_pageMod != 0) ? ((int) _pageMax) + 1 : (int) _pageMax;
        /**当没有数据时显示模式，可叠加（逗号分割）。F(首页按钮)、P(上一页按钮)、N(下一页按钮)、L（尾页按钮）、I(页码按钮)、T(显示ui_pNoDate标签内容)：注意I与T只能有一个生效*/
        String noDateMode = component.getNoDateMode();
        /**有无数据*/
        boolean hasData = rowCount - startWith > 0;
        //
        //
        //
        //1.First
        if (component.isShowFirst() == true && noData(hasData, noDateMode, "F") == true) {
            this.clearVar(viewContext, arg3, component);
            this.putCurrentVar(viewContext, arg3, current <= 0);
            this.putVar(viewContext, arg3, 0, startWith, component, Mode.First);
            arg3.render(writer);
        }
        //2.Prev
        if (component.isShowPrev() == true && noData(hasData, noDateMode, "P") == true) {
            int prevPage = current - 1;
            prevPage = (prevPage <= 0) ? 0 : prevPage;
            prevPage = (prevPage >= maxPage) ? (int) _pageMax - 1 : prevPage;
            int prevRow = prevPage * pageSize + startWith;
            prevRow = (prevRow < startWith) ? startWith : prevRow;
            prevRow = (prevRow >= rowCount) ? (prevPage * pageSize + startWith) : prevRow;
            //
            this.clearVar(viewContext, arg3, component);
            this.putFirstVar(viewContext, arg3, current <= 0);
            this.putVar(viewContext, arg3, prevPage, prevRow, component, Mode.Prev);
            arg3.render(writer);
        }
        //3.Item
        for (int i = 0; i < maxPage; i++) {
            int itemRow = i * pageSize + startWith;
            itemRow = (itemRow >= rowCount) ? (current * pageSize + startWith) : itemRow;
            //
            this.clearVar(viewContext, arg3, component);
            this.putCurrentVar(viewContext, arg3, current == i);
            this.putVar(viewContext, arg3, i, itemRow, component, Mode.Item);
            arg3.render(writer);
        }
        //4.NoDate
        if (hasData == false) {
            if (noDateMode.contains("I") == true) {
                /**无数据：页码*/
                this.clearVar(viewContext, arg3, component);
                this.putCurrentVar(viewContext, arg3, true);
                this.putVar(viewContext, arg3, 0, startWith, component, Mode.Item);
                arg3.render(writer);
            } else if (noDateMode.contains("T") == true) {
                /**无数据：标签*/
                this.clearVar(viewContext, arg3, component);
                this.putCurrentVar(viewContext, arg3, null);
                this.putVar(viewContext, arg3, null, null, component, Mode.NoDate);
                arg3.render(writer);
            }
        }
        //5.Next
        if (component.isShowNext() == true && noData(hasData, noDateMode, "N") == true) {
            int nextPage = current + 1;
            nextPage = (nextPage >= maxPage) ? (int) _pageMax : nextPage;
            nextPage = (nextPage <= 0) ? 1 : nextPage;
            int nextRow = nextPage * pageSize + startWith;
            nextRow = (nextRow >= rowCount) ? (current * pageSize + startWith) : nextRow;
            nextRow = (nextRow < startWith) ? startWith : nextRow;
            //
            this.clearVar(viewContext, arg3, component);
            this.putLastVar(viewContext, arg3, current >= (int) _pageMax);
            this.putVar(viewContext, arg3, nextPage, nextRow, component, Mode.Next);
            arg3.render(writer);
        }
        //5.Last
        if (component.isShowLast() == true && noData(hasData, noDateMode, "L") == true) {
            this.clearVar(viewContext, arg3, component);
            this.putCurrentVar(viewContext, arg3, current >= (int) _pageMax);
            this.putVar(viewContext, arg3, (int) _pageMax, (int) _pageMax * pageSize, component, Mode.Last);
            arg3.render(writer);
        }
    }
}