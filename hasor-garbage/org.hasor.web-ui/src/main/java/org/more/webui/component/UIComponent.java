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
package org.more.webui.component;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.more.util.BeanUtils;
import org.more.webui.DataException;
import org.more.webui.component.support.NoState;
import org.more.webui.component.values.AbstractValueHolder;
import org.more.webui.component.values.ExpressionValueHolder;
import org.more.webui.component.values.MethodExpression;
import org.more.webui.component.values.StaticValueHolder;
import org.more.webui.context.ViewContext;
import org.more.webui.event.Event;
import org.more.webui.event.EventListener;
import org.more.webui.lifestyle.phase.InitView_Phase;
/**
 * <b>组建模型</b>：所有组件的根，这里拥有组件的所有关键方法。
 * <br><b>服务端事件</b>：OnLoadData
 * <br><b>渲染器</b>：无
* @version : 2011-8-4
* @author 赵永春 (zyc@byshell.org)
*/
public abstract class UIComponent {
    private String                           componentID   = null;
    private String                           componentPath = null;
    private UIComponent                      parent        = null;
    private List<UIComponent>                components    = new ArrayList<UIComponent>();
    private Map<Event, List<EventListener>>  listener      = new HashMap<Event, List<EventListener>>();
    private Map<String, AbstractValueHolder> propertys     = new HashMap<String, AbstractValueHolder>();
    private Map<String, Object>              atts          = new HashMap<String, Object>();
    /*-------------------------------------------------------------------------------get/set属性*/
    /**返回组件的ID*/
    public String getComponentID() {
        return componentID;
    }
    /**设置属性ID*/
    public void setComponentID(String componentID) {
        this.componentID = componentID;
    }
    /**通用属性表*/
    public static enum Propertys {
        /**客户端在请求之前进行的调用，返回false取消本次ajax请求（R）*/
        beforeScript,
        /**客户端脚本回调函数（R）*/
        afterScript,
        /**调用错误回调函数（R）*/
        errorScript,
        /**Ajax是否使用同步操作（R）*/
        async,
        /**表示是否渲染（-）*/
        render,
        /**表示是否渲染子组建（-）*/
        renderChildren,
        /**当发生事件OnLoadData时触发，该事件允许用户通过任意组建从服务端装载数据到客户端。（R）*/
        onLoadDataEL,
        /**发生事件时在URL后面携带的参数。（RW）*/
        ajaxParam,
    };
    /**子类可以通过该方法初始化组件。*/
    protected void initUIComponent(ViewContext viewContext) {
        /*设置属性默认值，当页面中有值被设置的时候这里设置的默认值就会失效*/
        this.setPropertyMetaValue(Propertys.beforeScript.name(), "true");
        this.setPropertyMetaValue(Propertys.afterScript.name(), null);
        this.setPropertyMetaValue(Propertys.errorScript.name(), null);
        this.setPropertyMetaValue(Propertys.async.name(), true);//默认使用异步操作事件
        this.setPropertyMetaValue(Propertys.render.name(), true);
        this.setPropertyMetaValue(Propertys.renderChildren.name(), true);
        this.setPropertyMetaValue(Propertys.onLoadDataEL.name(), null);
        this.setPropertyMetaValue(Propertys.ajaxParam.name(), null);
        this.addEventListener(Event.getEvent("OnLoadData"), new Event_OnLoadData());
    };
    public String getBeforeScript() {
        return this.getProperty(Propertys.beforeScript.name()).valueTo(String.class);
    }
    @NoState
    public void setBeforeScript(String beforeScript) {
        this.getProperty(Propertys.beforeScript.name()).value(beforeScript);
    }
    public String getAfterScript() {
        return this.getProperty(Propertys.afterScript.name()).valueTo(String.class);
    }
    @NoState
    public void setAfterScript(String afterScript) {
        this.getProperty(Propertys.afterScript.name()).value(afterScript);
    }
    public String getErrorScript() {
        return this.getProperty(Propertys.errorScript.name()).valueTo(String.class);
    }
    @NoState
    public void setErrorScript(String errorScript) {
        this.getProperty(Propertys.errorScript.name()).value(errorScript);
    }
    public boolean isAsync() {
        return this.getProperty(Propertys.async.name()).valueTo(Boolean.TYPE);
    }
    @NoState
    public void setAsync(boolean async) {
        this.getProperty(Propertys.async.name()).value(async);
    }
    /**返回一个boolean值，该值决定是否渲染该组件*/
    @NoState
    public boolean isRender() {
        return this.getProperty(Propertys.render.name()).valueTo(Boolean.TYPE);
    };
    /**设置一个boolean值，该值决定是否渲染该组件*/
    @NoState
    public void setRender(boolean isRender) {
        this.getProperty(Propertys.render.name()).value(isRender);
    };
    /**返回一个boolean值，该值决定是否渲染该组件的子组建。*/
    @NoState
    public boolean isRenderChildren() {
        return this.getProperty(Propertys.renderChildren.name()).valueTo(Boolean.TYPE);
    }
    /**设置一个boolean值，该值决定是否渲染该组件的子组建。*/
    @NoState
    public void setRenderChildren(boolean isRenderChildren) {
        this.getProperty(Propertys.renderChildren.name()).value(isRenderChildren);
    }
    /**当企图装载数据时EL调用表达式（如果配置）*/
    public String getOnLoadDataEL() {
        return this.getProperty(Propertys.onLoadDataEL.name()).valueTo(String.class);
    }
    /**当企图装载数据时EL调用表达式（如果配置）*/
    @NoState
    public void setOnLoadDataEL(String onLoadDataEL) {
        this.getProperty(Propertys.onLoadDataEL.name()).value(onLoadDataEL);
    }
    private MethodExpression loadDataExp = null;
    /**获取loadDataExp属性的{@link MethodExpression}对象。*/
    public MethodExpression getOnLoadDataExpression() {
        if (this.loadDataExp == null) {
            String loadDataExpString = this.getOnLoadDataEL();
            if (loadDataExpString == null || loadDataExpString.equals("")) {} else
                this.loadDataExp = new MethodExpression(loadDataExpString);
        }
        return this.loadDataExp;
    }
    /**发生事件时在URL后面携带的参数。（RW）*/
    public String getAjaxParam() {
        return this.getProperty(Propertys.ajaxParam.name()).valueTo(String.class);
    }
    /**发生事件时在URL后面携带的参数。（RW）*/
    public void setAjaxParam(String ajaxParam) {
        this.getProperty(Propertys.ajaxParam.name()).value(ajaxParam);
    }
    /*-------------------------------------------------------------------------------核心方法*/
    /**获取用于附加的属性的Map对象*/
    public Map<String, Object> getAtts() {
        return this.atts;
    };
    /**获取组建类型，每一个UI组建都应该具备一个独一无二的componentType，这个ID是用来表示组建类型。*/
    public abstract String getComponentType();
    /**获取组建在组建树中的位置格式为：/1/3/4/2 */
    public String getComponentPath() {
        if (this.componentPath == null) {
            StringBuffer buffer = new StringBuffer("/");
            UIComponent target = this;
            UIComponent targetParent = target.getParent();
            while (targetParent != null) {
                int index = targetParent.getChildren().indexOf(target);
                buffer.append(new StringBuffer(String.valueOf(index)).reverse());
                buffer.append('/');
                //
                target = targetParent;
                targetParent = target.getParent();
            }
            if (buffer.length() > 1)
                this.componentPath = buffer.deleteCharAt(0).reverse().toString();
            else
                this.componentPath = buffer.reverse().toString();
        }
        return this.componentPath;
    }
    /**获取一个可用的客户端ID*/
    public String getClientID(ViewContext viewContext) {
        if (this.getComponentID() != null)
            return getComponentID();
        else
            return "uiCID_" + viewContext.getComClientID(this);
    }
    public UIComponent getChildByPath(String componentPath) {
        if (componentPath == null || componentPath.equals("") == true)
            return null;
        String thisPath = this.getComponentPath();
        if (thisPath.equals(componentPath) == true)
            return this;//判断目标是否就是自己。
        if (componentPath.startsWith(thisPath) == false)
            return null;//排除要获取的目标不是自己孩子的情况。
        //
        String targetPath = componentPath.substring(thisPath.length());
        int firstSpan = targetPath.indexOf('/');
        {
            if (firstSpan == 0) {
                targetPath = targetPath.substring(1);
                firstSpan = targetPath.indexOf('/');
            }
        }
        //
        int index = -1;
        if (firstSpan == -1)
            index = Integer.parseInt(targetPath);
        else
            index = Integer.parseInt(targetPath.substring(0, firstSpan));
        //
        UIComponent comObject = this.getChildren().get(index);
        //
        if (comObject == null)
            return null;
        else
            return comObject.getChildByPath(componentPath);
    }
    /**在当前组件的子级中寻找某个特定ID的组件*/
    public UIComponent getChildByID(String componentID) {
        if (componentID == null)
            return null;
        if (this.getComponentID().equals(componentID) == true)
            return this;
        for (UIComponent component : this.components) {
            UIComponent com = component.getChildByID(componentID);
            if (com != null)
                return com;
        }
        return null;
    };
    /**获取一个int，该值表明当前组件中共有多少个子元素*/
    public int getChildCount() {
        return this.components.size();
    };
    /**获取一个元素集合，该集合是存放子组件的场所*/
    public List<UIComponent> getChildren() {
        return Collections.unmodifiableList(this.components);
    };
    /**获取一个组建列表该列表中包含了该组建以及该组建的所有子组建。*/
    public List<UIComponent> getALLChildren() {
        ArrayList<UIComponent> list = new ArrayList<UIComponent>();
        list.add(this);
        for (UIComponent uic : components)
            list.addAll(uic.getALLChildren());
        return list;
    };
    /**添加子组建*/
    public void addChildren(UIComponent componentItem) {
        componentItem.setParent(this);
        this.components.add(componentItem);
    };
    /**获取组建的父级。*/
    public UIComponent getParent() {
        return this.parent;
    };
    /**设置组建的父级别。*/
    private void setParent(UIComponent parent) {
        this.parent = parent;
    }
    /**获取保存属性的集合。*/
    public Map<String, AbstractValueHolder> getPropertys() {
        return this.propertys;
    };
    /**获取用于表示组件属性对象。*/
    public AbstractValueHolder getProperty(String propertyName) {
        AbstractValueHolder value = this.getPropertys().get(propertyName);
        if (value == null)
            return new StaticValueHolder();
        return value;
    };
    /**添加一个EL形式的组建。属性参数readString、writeString分别对应了业务组建的读写属性。*/
    public void setPropertyEL(String propertyName, String readString, String writeString) {
        AbstractValueHolder value = this.getPropertys().get(propertyName);
        ExpressionValueHolder elValueHolder = null;
        if (value == null || value instanceof ExpressionValueHolder == false)
            elValueHolder = new ExpressionValueHolder(readString, writeString);
        this.getPropertys().put(propertyName, elValueHolder);
    };
    /**该方法会将elString参数会作为readString和、writeString。*/
    public void setPropertyEL(String propertyName, String elString) {
        this.setPropertyEL(propertyName, elString, elString);
    };
    /**设置组建属性的值（该值的设置只会影响本次请求生命周期）。*/
    public void setProperty(String propertyName, Object newValue) {
        if (ViewContext.getCurrentViewContext().getPhaseID().equals(InitView_Phase.PhaseID) == true)
            throw new RuntimeException("请不要在InitView阶段使用该方法。");
        //
        AbstractValueHolder value = this.getPropertys().get(propertyName);
        if (value == null)
            value = new StaticValueHolder();
        value.value(newValue);
        this.getPropertys().put(propertyName, value);
    };
    /**设置组建属性的MetaValue值（该值可以作为属性在全部线程上的默认初始化值，真正意义上的默认值）。
     * 注意：在initUIComponent方法中使用该方法只会影响到那些未在页面中定义的属性。*/
    public void setPropertyMetaValue(String propertyName, Object newValue) {
        AbstractValueHolder value = this.getPropertys().get(propertyName);
        if (value == null)
            value = new StaticValueHolder(newValue);
        this.getPropertys().put(propertyName, value);
        //不处理init过程中的设置请求。
        ViewContext view = ViewContext.getCurrentViewContext();
        if (view != null && view.getPhaseID().equals(InitView_Phase.PhaseID) == false)
            value.setMetaValue(newValue);
    };
    /**将map中的属性全部安装到当前组建上*/
    public void setupPropertys(Map<String, Object> objMap) {
        if (objMap != null)
            for (String key : this.propertys.keySet())
                if (objMap.containsKey(key) == true) {
                    AbstractValueHolder vh = this.propertys.get(key);
                    Object newValue = objMap.get(key);
                    vh.value(newValue);
                }
    };
    /*-------------------------------------------------------------------------------生命周期*/
    /**组建被初始化标记*/
    private Boolean doInit = false;
    /**第1阶段，处理初始化阶段，该阶段负责初始化组件。*/
    public final void processInit(ViewContext viewContext) throws Throwable {
        if (this.doInit == false) {
            this.initUIComponent(viewContext);
            this.doInit = true;
        }
        /*重置属性，重置属性会保证每个生命周期内的属性值是由UI中定义的原始值。*/
        for (AbstractValueHolder vh : this.propertys.values())
            vh.reset();
        for (UIComponent com : this.components)
            com.processInit(viewContext);
    };
    /**第3阶段，将请求参数中与属性名一致的属性灌入属性上。*/
    public void processApplyRequest(ViewContext viewContext) throws Throwable {
        /*将请求参数中要求灌入的属性值灌入到属性上*/
        for (String key : this.propertys.keySet()) {
            /*被灌入的属性名，请求参数中必须是“componentID:attName”*/
            String[] newValues = viewContext.getHttpRequest().getParameterValues(this.getComponentPath() + ":" + key);
            if (newValues == null)
                continue;
            else if (newValues.length == 1)
                this.propertys.get(key).value(newValues[0]);
            else
                this.propertys.get(key).value(newValues);
        }
        for (UIComponent com : this.components)
            com.processApplyRequest(viewContext);
    };
    /**第4阶段，该阶段用于提供一组验证数据的合法性。*/
    public void processValidate(ViewContext viewContext) throws Throwable {
        for (UIComponent com : this.components)
            com.processValidate(viewContext);
    };
    /**第5阶段，将组件模型中的新值应用到，Bean*/
    public void processUpdate(ViewContext viewContext) throws Throwable {
        /*更新所有注册到propertys中的属性值*/
        for (String key : this.propertys.keySet()) {
            AbstractValueHolder vh = this.propertys.get(key);
            if (vh.isUpdate() == true)
                vh.updateModule(this, viewContext);
        }
        for (UIComponent com : this.components)
            com.processUpdate(viewContext);
    };
    /**第6阶段，处理Action动作和客户端回传的事件*/
    public void processApplication(ViewContext viewContext) throws Throwable {
        if (this.getComponentPath().equals(viewContext.getTargetPath()) == true) {
            /*处理客户端引发的事*/
            Event eventType = Event.getEvent(viewContext.getEvent());
            if (eventType != null)
                /**事件请求*/
                this.doEvent(eventType, viewContext);
        }
        for (UIComponent com : this.components)
            com.processApplication(viewContext);
    };
    /*-------------------------------------------------------------------------------事件响应*/
    /**执行事件*/
    protected void doEvent(Event eventType, ViewContext viewContext) throws Throwable {
        try {
            for (Event e : this.listener.keySet())
                if (e.equals(eventType) == true) {
                    List<EventListener> listeners = this.listener.get(eventType);
                    for (EventListener listener : listeners)
                        listener.onEvent(eventType, this, viewContext);
                }
        } catch (Exception e) {
            if (viewContext.isAjax() == true) {
                viewContext.sendError(e);
                //e.printStackTrace(System.err);
            } else
                throw e;
        }
    };
    /**添加一种类型事件的事件监听器。*/
    public void addEventListener(Event eventType, EventListener listener) {
        if (eventType == null || listener == null)
            return;
        List<EventListener> listeners = this.listener.get(eventType);
        if (listeners == null) {
            //            Log.debug("this event is first append, event = " + eventType + ", listener = " + listener);
            listeners = new ArrayList<EventListener>();
            this.listener.put(eventType, listeners);
        }
        //        Log.debug("add event listener, event = " + eventType + ", listener = " + listener);
        listeners.add(listener);
    };
    /*-------------------------------------------------------------------------------状态处理*/
    /**从状态数据中恢复组建状态*/
    public void restoreState(List<?> stateData) {
        //1.数据检查
        if (stateData == null)
            return;
        if (stateData.size() == 0)
            throw new DataException("WebUI无法重塑组件状态，在重塑组件[" + this.getComponentID() + "]组件发生数据丢失");
        //2.恢复自身数据
        Map<String, Object> mineState = (Map<String, Object>) stateData.get(0);
        for (String propName : mineState.keySet()) {
            /*排除错误*/
            if (propName == null)
                continue;
            /*ID属性不处理*/
            if (propName.toLowerCase().equals("id") == true)
                continue;
            /*处理注解*/
            Method rm = BeanUtils.getWriteMethod(propName, this.getClass());
            if (rm == null)
                continue;
            if (rm.getAnnotation(NoState.class) != null)
                continue;
            /*写入属性*/
            AbstractValueHolder vh = this.propertys.get(propName);
            if (vh != null)
                vh.value(mineState.get(propName));
        }
        //3.恢复子组件
        if (stateData.size() == 2) {
            Map<String, Object> childrenState = (Map<String, Object>) stateData.get(1);
            for (UIComponent com : components)
                com.restoreState((List<?>) childrenState.get(com.getComponentPath()));
        }
    };
    /**保存组建的当前状态，不包含子组建。*/
    public List<Object> saveStateOnlyMe() {
        //1.持久化自身的状态
        HashMap<String, Object> mineState = new HashMap<String, Object>();
        for (String propName : this.propertys.keySet()) {
            Method rm = BeanUtils.getReadMethod(propName, this.getClass());
            if (rm == null)
                continue;
            if (rm.getAnnotation(NoState.class) != null)
                continue;
            AbstractValueHolder vh = this.propertys.get(propName);
            mineState.put(propName, vh.value());
        }
        //3.返回持久化状态
        ArrayList<Object> array = new ArrayList<Object>();
        array.add(mineState);
        return array;
    };
    /**保存组建的当前状态，包含子组建。*/
    public List<Object> saveState() {
        //1.持久化自身的状态
        List<Object> array = this.saveStateOnlyMe();
        //2.持久化子组件的状态
        HashMap<String, Object> childrenState = new HashMap<String, Object>();
        for (UIComponent com : components)
            childrenState.put(com.getComponentPath(), com.saveState());
        //3.返回持久化状态
        array.add(childrenState);
        return array;
    };
};
/**负责处理OnLoadData事件的EL调用*/
class Event_OnLoadData implements EventListener {
    public void onEvent(Event event, UIComponent component, ViewContext viewContext) throws Throwable {
        MethodExpression e = component.getOnLoadDataExpression();
        if (e != null)
            viewContext.sendObject(e.execute(component, viewContext));
    }
}