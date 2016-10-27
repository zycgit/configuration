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
package net.hasor.web.pipeline.valves;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import net.hasor.web.WebAppContext;
/**
 * Abstract implementation for all servlet module bindings
 * @version : 2013-4-12
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractServletValve<T> extends AbstractValve {
    private int                       index      = 0;
    private final Map<String, String> initParams;
    private final String              pattern;
    private final UriPatternMatcher   patternMatcher;
    private WebAppContext             appContext = null;
    //
    public AbstractServletValve(final int index, final Map<String, String> initParams, final String pattern, final UriPatternMatcher patternMatcher) {
        this.index = index;
        this.initParams = new HashMap<String, String>(initParams);
        this.pattern = pattern;
        this.patternMatcher = patternMatcher;
    }
    public int getIndex() {
        return this.index;
    }
    /** Returns any context params supplied when creating the binding. */
    public Map<String, String> getInitParams() {
        return this.initParams;
    }
    /** Returns the pattern used to match against the binding. */
    public String getPattern() {
        return this.pattern;
    }
    /** Returns the pattern type that this binding was created with. */
    public UriPatternType getUriPatternType() {
        return this.patternMatcher.getPatternType();
    }
    /** Returns true if the given URI will match this binding. */
    public boolean matchesUri(final String uri) {
        return this.patternMatcher.matches(uri);
    }
    /**init.*/
    public void init(WebAppContext appContext) {
        this.appContext = appContext;
    }
    /**获取{@link WebAppContext}对象。
     * @see #init(WebAppContext)*/
    protected WebAppContext getAppContext() {
        return appContext;
    }
    public String toString() {
        return String.format("type %s pattern=%s ,initParams=%s ,uriPatternType=%s",//
                this.getClass(), this.getPattern(), this.getInitParams(), this.getUriPatternType());
    }
    protected abstract T getTarget() throws ServletException;
}