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
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.hasor.web.pipeline.PipelineContext;
/**
 * 
 * @version : 2014年10月21日
 * @author 赵永春(zyc@hasor.net)
 */
public class PipelineContextWrap implements PipelineContext {
    private PipelineContext     pipContext   = null;
    private HttpServletRequest  httpRequest  = null;
    private HttpServletResponse httpResponse = null;
    //
    public PipelineContextWrap(PipelineContext pipContext) {
        this.pipContext = pipContext;
    }
    public ServletContext getServletContext() {
        return this.pipContext.getServletContext();
    }
    public HttpServletRequest getHttpRequest() {
        if (this.httpRequest != null)
            return this.httpRequest;
        return this.pipContext.getHttpRequest();
    }
    public void setHttpRequest(HttpServletRequest httpRequest) {
        this.httpRequest = httpRequest;
    }
    public HttpServletResponse getHttpResponse() {
        if (this.httpResponse != null)
            return this.httpResponse;
        return this.pipContext.getHttpResponse();
    }
    public void setHttpResponse(HttpServletResponse httpResponse) {
        this.httpResponse = httpResponse;
    }
}