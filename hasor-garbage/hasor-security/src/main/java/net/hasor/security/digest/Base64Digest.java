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
package net.hasor.security.digest;
import net.hasor.security.Digest;
import org.more.util.CommonCodeUtils.Base64;
/**
 * Base64编码。
 * @version : 2013-4-24
 * @author 赵永春 (zyc@byshell.org)
 */
public final class Base64Digest implements Digest {
    @Override
    public String encrypt(String strValue, String generateKey) throws Throwable {
        return Base64.base64Encode(strValue);
    }
    @Override
    public String decrypt(String strValue, String generateKey) throws Throwable {
        return Base64.base64Decode(strValue);
    };
}