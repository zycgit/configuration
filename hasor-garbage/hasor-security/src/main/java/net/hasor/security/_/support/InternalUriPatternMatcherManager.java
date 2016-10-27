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
import java.util.List;
import net.hasor.plugins.secweb.UriPatternMatcher;
import org.hasor.context.AppContext;
import org.more.util.StringUtils;
/**
 * 
 * @version : 2013-4-25
 * @author 赵永春 (zyc@byshell.org)
 */
class InternalUriPatternMatcherManager {
    private SecuritySettings securitySettings = null;
    //
    public void initManager(AppContext appContext) {
        this.securitySettings = appContext.getInstance(SecuritySettings.class);
    }
    public UriPatternMatcher getUriMatcher(String requestPath) {
        if (StringUtils.isBlank(requestPath) == true)
            return null;
        requestPath = requestPath.toLowerCase();
        //1.检查排除配置
        List<UriPatternMatcher> excludeRules = this.securitySettings.getRulesExcludeList();
        for (UriPatternMatcher urlPattern : excludeRules) {
            if (requestPath.startsWith(urlPattern.getRequestURI()) == true)
                return urlPattern;
        }
        //2.检查包含配置
        List<UriPatternMatcher> includeRules = this.securitySettings.getRulesIncludeList();
        for (UriPatternMatcher urlPattern : includeRules) {
            if (requestPath.startsWith(urlPattern.getRequestURI()) == true)
                return urlPattern;
        }
        return null;
    }
    public void destroyManager(AppContext appContext) {};
}