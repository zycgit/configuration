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
package net.hasor.security._.support;
import net.hasor.security.Token;
import net.hasor.security._.RoleIdentity;
import net.hasor.security._.RoleIdentityUtil;
/**
 * 抽象的{@link Token}接口。
 * @version : 2013-5-3
 * @author 赵永春 (zyc@byshell.org)
 */
public abstract class BaseUserInfo implements Token {
    @Override
    public boolean isGuest() {
        return false;
    }
    @Override
    public RoleIdentity getIdentity() {
        return RoleIdentityUtil.getTypeIdentity(this.getClass());
    }
}