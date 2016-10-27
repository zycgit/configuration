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
package net.test.hasor.db._08_ar.dao;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;
import javax.sql.DataSource;
import net.hasor.db.orm.AbstractDao;
import net.hasor.db.orm.PageResult;
import net.hasor.db.orm.ar.dialect.SQLBuilderEnum;
import net.test.hasor.db._08_ar.entity.TB_User;
/**
 * 
 * @version : 2014年10月27日
 * @author 赵永春(zyc@hasor.net)
 */
public class UserDao extends AbstractDao<TB_User> {
    public UserDao(DataSource dataSource) {
        super(dataSource);
        this.setDialect(SQLBuilderEnum.MySql);
    }
    //
    public PageResult<TB_User> queryList1() throws SQLException {
        return this.listByExample(new TB_User());
    }
    public PageResult<TB_User> queryList2() throws SQLException {
        return this.queryBySQL("select * from TB_User");
    }
    public boolean insertUser(int i) throws SQLException {
        TB_User tbUser = new TB_User();
        tbUser.setUserUUID(UUID.randomUUID().toString());
        tbUser.setName(String.format("默认用户_%s", i));
        tbUser.setLoginName(String.format("acc_%s", i));
        tbUser.setLoginPassword(String.format("pwd_%s", i));
        tbUser.setEmail(String.format("autoUser_%s@hasor.net", i));
        tbUser.setRegisterTime(new Date());
        //
        return this.saveAsNew(tbUser);
    }
}