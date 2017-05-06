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
package net.hasor.rsf.center.server.webmanager.domain.valid;
import org.more.bizcommon.ResultDO;
import net.hasor.core.AppContext;
import net.hasor.core.InjectMembers;
import net.hasor.plugins.valid.ValidDefine;
import net.hasor.plugins.valid.Validation;
import net.hasor.rsf.center.server.core.daos.DaoProvider;
import net.hasor.rsf.center.server.domain.entity.TerminalDO;
/**
 * @version : 2015年6月28日
 * @author 赵永春(zyc@hasor.net)
 */
@ValidDefine("Access")
public class AccessValid implements Validation, InjectMembers {
    private DaoProvider daoProvider;
    public void doInject(AppContext appContext) {
        this.daoProvider = appContext.getInstance(DaoProvider.class);
    }
    public ResultDO<String> doValidation(Object dataForm) {
        AccessInfo accInfo = (AccessInfo) dataForm;
        //
        String secretKey = "dddd";//SecretUtils.toSecretKey(accInfo);
        ResultDO<TerminalDO> terminalResultDO = daoProvider.getTerminalDao().queryTerminalByIDAndSecret(accInfo.getTerminalID(), secretKey);
        // if (!terminalResultDO.isSuccess() || terminalResultDO.getResult() == null) {
        // if (terminalResultDO.isSuccess() == false) {
        // return new ResultDO<String>().setSuccess(false).addMessage(terminalResultDO.getMessageList());
        // } else {
        // return new ResultDO<String>().setSuccess(false);
        // }
        // }
        // TODO Auto-generated method stub
        return new ResultDO<String>().setSuccess(true);
    }
}