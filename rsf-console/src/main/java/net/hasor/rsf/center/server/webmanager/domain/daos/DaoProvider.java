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
package net.hasor.rsf.center.server.webmanager.domain.daos;
import net.hasor.core.Inject;
import net.hasor.rsf.center.server.core.daos.AppDODao;
import net.hasor.rsf.center.server.core.daos.ServiceInfoDODao;
import net.hasor.rsf.center.server.core.daos.ServiceJoinPortDODao;
import net.hasor.rsf.center.server.core.daos.TerminalDODao;
import net.hasor.rsf.center.server.webmanager.core.dao.AbstractDao;
import net.hasor.rsf.center.server.webmanager.core.dao.Dao;
/**
 * @version : 2015年6月30日
 * @author 赵永春(zyc@hasor.net)
 */
@Dao
public class DaoProvider extends AbstractDao<Object> {
    @Inject
    private AppDODao             appDao;
    @Inject
    private ServiceInfoDODao     serviceInfoDao;
    @Inject
    private ServiceJoinPortDODao serviceJoinPortDao;
    @Inject
    private TerminalDODao        terminalDao;
    //
    public AppDODao getAppDao() {
        return appDao;
    }
    public ServiceInfoDODao getServiceInfoDao() {
        return serviceInfoDao;
    }
    public ServiceJoinPortDODao getServiceJoinPortDao() {
        return serviceJoinPortDao;
    }
    public TerminalDODao getTerminalDao() {
        return terminalDao;
    }
}