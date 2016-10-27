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
import java.io.IOException;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import net.hasor.web.pipeline.PipelineContext;
import net.hasor.web.pipeline.ValveChain;
import net.hasor.web.pipeline.support.PipelineContextWrap;
/**
 * 
 * @version : 2014年10月21日
 * @author 赵永春(zyc@hasor.net)
 */
public abstract class FilterValve extends AbstractServletValve<Filter> {
    public FilterValve(int index, Map<String, String> initParams, String pattern, UriPatternMatcher patternMatcher) {
        super(index, initParams, pattern, patternMatcher);
    }
    //
    public void doValve(PipelineContext pipContext, ValveChain chain) throws IOException, ServletException {
        Filter filter = this.getTarget();
        if (filter != null) {
            FilterValveValveChain valveChain = new FilterValveValveChain(chain, pipContext);
            filter.doFilter(pipContext.getHttpRequest(), pipContext.getHttpResponse(), valveChain);
        }
    }
    private static class FilterValveValveChain implements FilterChain {
        private ValveChain      valveChain = null;
        private PipelineContext pipContext = null;
        public FilterValveValveChain(ValveChain valveChain, PipelineContext pipContext) {
            this.valveChain = valveChain;
            this.pipContext = pipContext;
        }
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            PipelineContextWrap wrap = new PipelineContextWrap(this.pipContext);
            wrap.setHttpRequest((HttpServletRequest) request);
            wrap.setHttpRequest((HttpServletRequest) response);
            this.valveChain.doNext(wrap);
        }
    }
}