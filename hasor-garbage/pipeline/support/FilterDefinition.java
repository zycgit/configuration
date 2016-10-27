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
package net.hasor.web.pipeline.support;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import net.hasor.core.BindInfo;
import net.hasor.web.WebAppContext;
import net.hasor.web.pipeline.valves.FilterValve;
import net.hasor.web.pipeline.valves.UriPatternMatcher;
import org.more.util.Iterators;
/**
 * 
 * @version : 2013-4-11
 * @author 赵永春 (zyc@hasor.net)
 */
class FilterDefinition extends FilterValve {
    private BindInfo<Filter> filterRegister = null;
    private Filter           filterInstance = null;
    //
    public FilterDefinition(final int index, final String pattern, final UriPatternMatcher uriPatternMatcher, final BindInfo<Filter> filterRegister, final Map<String, String> initParams) {
        super(index, initParams, pattern, uriPatternMatcher);
        this.filterRegister = filterRegister;
    }
    protected Filter getTarget() throws ServletException {
        if (this.filterInstance != null) {
            return this.filterInstance;
        }
        //
        final Map<String, String> initParams = this.getInitParams();
        this.filterInstance = this.getAppContext().getInstance(this.filterRegister);
        this.filterInstance.init(new FilterConfig() {
            public String getFilterName() {
                return (filterInstance == null ? filterRegister : filterInstance).toString();
            }
            public ServletContext getServletContext() {
                return getAppContext().getServletContext();
            }
            public String getInitParameter(final String s) {
                return initParams.get(s);
            }
            public Enumeration<String> getInitParameterNames() {
                return Iterators.asEnumeration(initParams.keySet().iterator());
            }
        });
        return this.filterInstance;
    }
    //
    public void init(WebAppContext appContext, Map<String, String> filterConfig) throws ServletException {
        super.init(appContext);
        //
        if (filterConfig != null) {
            Map<String, String> thisConfig = this.getInitParams();
            for (Entry<String, String> ent : filterConfig.entrySet()) {
                String key = ent.getKey();
                if (!thisConfig.containsKey(key)) {
                    thisConfig.put(key, ent.getValue());
                }
            }
        }
        //
        this.getTarget();
    }
    public void destroy() {
        if (this.filterInstance == null) {
            return;
        }
        this.filterInstance.destroy();
    }
}