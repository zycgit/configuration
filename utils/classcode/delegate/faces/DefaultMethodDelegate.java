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
package org.more.classcode.delegate.faces;
import org.more.util.BeanUtils;

import java.lang.reflect.Method;
/**
 * 代理方法的空实现。
 * @version 2010-9-3
 * @author 赵永春 (zyc@hasor.net)
 */
public class DefaultMethodDelegate implements MethodDelegate {
    public Object invoke(final Method callMethod, final Object target, final Object[] params) {
        Class<?> returnType = callMethod.getReturnType();
        return BeanUtils.getDefaultValue(returnType);
    }
}