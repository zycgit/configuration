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
package net.hasor.security._.support.process;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.hasor.plugins.secweb.SecurityDispatcher;
import net.hasor.plugins.secweb.SecurityForward;
import net.hasor.security.SecurityException;
import net.hasor.security._.AuthSession;
import net.hasor.security._.SecurityContext;
import org.hasor.Hasor;
/**
 * {@link LogoutProcess}接口默认实现。
 * @version : 2013-5-8
 * @author 赵永春 (zyc@byshell.org)
 */
public class LogoutProcess extends AbstractProcess {
    /**处理登出请求*/
    public SecurityForward processLogout(SecurityContext secContext, HttpServletRequest request, HttpServletResponse response) throws SecurityException, ServletException, IOException {
        String reqPath = request.getRequestURI().substring(request.getContextPath().length());
        SecurityDispatcher dispatcher = secContext.getDispatcher(reqPath);
        AuthSession[] authSessions = secContext.getCurrentAuthSession();
        for (AuthSession authSession : authSessions) {
            /*将所有已登入的会话全部登出*/
            if (authSession.isLogin() == false)
                continue;
            String userCode = authSession.getUserObject().getUserCode();
            try {
                authSession.doLogout();/*退出会话*/
                Hasor.info("logout OK. userCode=%s , at SessionID= %s", userCode, authSession.getSessionID());
                return dispatcher.forwardLogout();
            } catch (SecurityException e) {
                Hasor.info("logout failure! userCode=%s , at SessionID= %s", userCode, authSession.getSessionID());
                return dispatcher.forwardFailure(e);
            }
        }
        return dispatcher.forwardLogout();
    }
}