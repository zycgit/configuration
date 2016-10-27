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
package org.dev.toos.constcode.model.group;
import java.io.InputStream;
import org.dev.toos.constcode.data.ConstDao;
import org.dev.toos.constcode.data.VarDao;
import org.dev.toos.constcode.data.xml.XmlConstDao;
import org.dev.toos.constcode.data.xml.XmlVarDao;
import org.dev.toos.constcode.model.ConstGroup;
import org.dev.toos.internal.util.Message;
/**
 * 
 * @version : 2013-2-2
 * @author 赵永春 (zyc@byshell.org) 
 */
public class JARSourceConstCodeGroup extends ConstGroup {
    private InputStream inStream = null;
    private XmlConstDao constDao = null;
    private XmlVarDao   varDao   = null;
    //
    //
    public JARSourceConstCodeGroup(String name, InputStream constSource) {
        super(FromType.JAR);
        this.inStream = constSource;
        this.setName(name);
        this.setReadOnly(true);
    }
    @Override
    protected void initGroup() {
        try {
            this.constDao = new XmlConstDao(this.inStream);
            this.varDao = new XmlVarDao(this.constDao);
        } catch (Exception e) {
            Message.errorInfo("Load ‘" + this.getName() + "’ Resource Error.", e);
        }
    }
    @Override
    protected ConstDao getConstDao() {
        return this.constDao;
    }
    @Override
    protected VarDao getVarDao() {
        return this.varDao;
    }
}