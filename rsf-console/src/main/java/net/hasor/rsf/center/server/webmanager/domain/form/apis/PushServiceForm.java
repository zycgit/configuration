/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.rsf.center.server.webmanager.domain.form.apis;
import net.hasor.plugins.restful.api.ReqParam;
import net.hasor.rsf.center.server.webmanager.domain.valid.AccessInfo;
/**
 * @version : 2015年6月11日
 * @author 赵永春(zyc@hasor.net)
 */
public class PushServiceForm implements AccessInfo {
    @ReqParam("Terminal_ID")
    private String terminalID;
    @ReqParam("Terminal_AccessKey")
    private String accessKey;
    //
    @ReqParam("Service_BindID")
    private String bindID;
    @ReqParam("Service_BindName")
    private String bindName;
    @ReqParam("Service_BindGroup")
    private String bindGroup;
    @ReqParam("Service_BindVersion")
    private String bindVersion;
    @ReqParam("Service_BindType")
    private String bindType;
    //
    @ReqParam("Service_ClientTimeout")
    private String clientTimeout;
    @ReqParam("Service_SerializeType")
    private String serializeType;
    @ReqParam("Service_Persona")
    private String persona;
    //
    public String getTerminalID() {
        return terminalID;
    }
    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }
    public String getAccessKey() {
        return accessKey;
    }
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }
    public String getBindID() {
        return bindID;
    }
    public void setBindID(String bindID) {
        this.bindID = bindID;
    }
    public String getBindName() {
        return bindName;
    }
    public void setBindName(String bindName) {
        this.bindName = bindName;
    }
    public String getBindGroup() {
        return bindGroup;
    }
    public void setBindGroup(String bindGroup) {
        this.bindGroup = bindGroup;
    }
    public String getBindVersion() {
        return bindVersion;
    }
    public void setBindVersion(String bindVersion) {
        this.bindVersion = bindVersion;
    }
    public String getBindType() {
        return bindType;
    }
    public void setBindType(String bindType) {
        this.bindType = bindType;
    }
    public String getClientTimeout() {
        return clientTimeout;
    }
    public void setClientTimeout(String clientTimeout) {
        this.clientTimeout = clientTimeout;
    }
    public String getSerializeType() {
        return serializeType;
    }
    public void setSerializeType(String serializeType) {
        this.serializeType = serializeType;
    }
    public String getPersona() {
        return persona;
    }
    public void setPersona(String persona) {
        this.persona = persona;
    }
}