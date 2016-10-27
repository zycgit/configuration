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
import org.more.webui.component.support.UICom;
import org.more.webui.component.values.AbstractValueHolder;
import org.more.webui.context.ViewContext;
/**
 * 分页组建
 * @version : 2012-5-15
 * @author 赵永春 (zyc@byshell.org)
 */
@UICom(tagName = "ui_Page", renderType = PageRender.class)
public class PageCom extends UIComponent {
    /**属性表*/
    public static enum Propertys {
        /**是否显示【首页】按钮（RW）*/
        showFirst,
        /**是否显示【上一页】按钮（RW）*/
        showPrev,
        /**是否显示【页码】按钮（RW）*/
        showNum,
        /**是否显示【下一页】按钮（RW）*/
        showNext,
        /**是否显示【尾页】按钮（RW）*/
        showLast,
        /**开始的页码号（RW）*/
        startWith,
        /**页大小（RW）*/
        pageSize,
        /**当前页（RW）*/
        currentPage,
        /**记录总数（RW）*/
        rowCount,
        /**当没有数据时显示模式，可叠加（逗号分割）。B（分页大小设置）、G（页码输入框）、F(首页按钮)、P(上一页按钮)、N(下一页按钮)、L（尾页按钮）、I(页码按钮)、T(显示ui_pNoDate标签内容)：注意I与T只能有一个生效（RW）*/
        noDateMode,
        /**分页组建的连接（RW）*/
        pageLink,
    };
    @Override
    public String getComponentType() {
        return "ui_Page";
    }
    @Override
    protected void initUIComponent(ViewContext viewContext) {
        super.initUIComponent(viewContext);
        this.setPropertyMetaValue(Propertys.showFirst.name(), false);
        this.setPropertyMetaValue(Propertys.showPrev.name(), true);
        this.setPropertyMetaValue(Propertys.showNum.name(), true);
        this.setPropertyMetaValue(Propertys.showNext.name(), true);
        this.setPropertyMetaValue(Propertys.showLast.name(), false);
        this.setPropertyMetaValue(Propertys.startWith.name(), 0);
        this.setPropertyMetaValue(Propertys.pageSize.name(), 20);
        this.setPropertyMetaValue(Propertys.currentPage.name(), 0);
        this.setPropertyMetaValue(Propertys.rowCount.name(), 0);
        this.setPropertyMetaValue(Propertys.noDateMode.name(), "T");
    }
    /**是否显示【首页】按钮*/
    public boolean isShowFirst() {
        return this.getProperty(Propertys.showFirst.name()).valueTo(Boolean.TYPE);
    }
    /**是否显示【首页】按钮*/
    public void setShowFirst(boolean showFirst) {
        this.getProperty(Propertys.showFirst.name()).value(showFirst);
    }
    /**是否显示【上一页】按钮*/
    public boolean isShowPrev() {
        return this.getProperty(Propertys.showPrev.name()).valueTo(Boolean.TYPE);
    }
    /**是否显示【上一页】按钮*/
    public void setShowPrev(boolean showPrev) {
        this.getProperty(Propertys.showPrev.name()).value(showPrev);
    }
    /**是否显示【页码】按钮（RW）*/
    public boolean isShowNum() {
        return this.getProperty(Propertys.showNum.name()).valueTo(Boolean.TYPE);
    }
    /**是否显示【页码】按钮（RW）*/
    public void setShowNum(boolean showNum) {
        this.getProperty(Propertys.showNum.name()).value(showNum);
    }
    /**是否显示【下一页】按钮*/
    public boolean isShowNext() {
        return this.getProperty(Propertys.showNext.name()).valueTo(Boolean.TYPE);
    }
    /**是否显示【下一页】按钮*/
    public void setShowNext(boolean showNext) {
        this.getProperty(Propertys.showNext.name()).value(showNext);
    }
    /**是否显示【尾页】按钮*/
    public boolean isShowLast() {
        return this.getProperty(Propertys.showLast.name()).valueTo(Boolean.TYPE);
    }
    /**是否显示【尾页】按钮*/
    public void setShowLast(boolean showLast) {
        this.getProperty(Propertys.showLast.name()).value(showLast);
    }
    /**开始的页码号*/
    public int getStartWith() {
        return this.getProperty(Propertys.startWith.name()).valueTo(Integer.TYPE);
    }
    /**开始的页码号*/
    public void setStartWith(Integer startWith) {
        this.getProperty(Propertys.startWith.name()).value(startWith);
    }
    /**页大小*/
    public int getPageSize() {
        return this.getProperty(Propertys.pageSize.name()).valueTo(Integer.TYPE);
    }
    /**页大小*/
    public void setDataSource(Integer pageSize) {
        this.getProperty(Propertys.pageSize.name()).value(pageSize);
    }
    /**当前页码*/
    public int getCurrentPage() {
        return this.getProperty(Propertys.currentPage.name()).valueTo(Integer.TYPE);
    }
    /**当前页码*/
    public void setCurrentPage(int currentPage) {
        this.getProperty(Propertys.currentPage.name()).value(currentPage);
    }
    /**记录总数*/
    public int getRowCount() {
        return this.getProperty(Propertys.rowCount.name()).valueTo(Integer.TYPE);
    }
    /**记录总数*/
    public void setRowCount(int rowCount) {
        this.getProperty(Propertys.rowCount.name()).value(rowCount);
    }
    /**当没有数据时显示模式，可叠加（逗号分割）。F(首页按钮)、P(上一页按钮)、N(下一页按钮)、L（尾页按钮）、I(页码按钮)、T(显示ui_pNoDate标签内容)：注意I与T只能有一个生效*/
    public String getNoDateMode() {
        return this.getProperty(Propertys.noDateMode.name()).valueTo(String.class);
    }
    /**当没有数据时显示模式，可叠加（逗号分割）。F(首页按钮)、P(上一页按钮)、N(下一页按钮)、L（尾页按钮）、I(页码按钮)、T(显示ui_pNoDate标签内容)：注意I与T只能有一个生效*/
    public void setNoDateMode(String noDateMode) {
        this.getProperty(Propertys.noDateMode.name()).value(noDateMode);
    }
    /**分页组建的连接(使用EL解析)*/
    public String getPageLink() {
        return this.getProperty(Propertys.pageLink.name()).valueTo(String.class);
    }
    /**分页组建的连接(使用EL解析)*/
    public void setPageLink(String pageLink) {
        this.getProperty(Propertys.pageLink.name()).value(pageLink);
    }
    /**分页组建的连接(使用模板解析)*/
    public String getPageLinkAsTemplate(ViewContext viewContext) {
        AbstractValueHolder avh = this.getProperty(Propertys.pageLink.name());
        String metaValue = (String) avh.getMetaValue();
        try {
            return viewContext.processTemplateString(metaValue);
        } catch (Exception e) {
            return "javascript:alert('PageCom.getPageLinkAsTemplate()方法执行遇到错误。');";
        }
    }
    /*-------------------------------------------------------------------------------*/
    Mode renderMode = null;
    public static enum Mode {
        First, Prev, Item, Next, Last, NoDate
    }
}