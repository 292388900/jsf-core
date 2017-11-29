/**
 * Copyright 2004-2048 .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ipd.jsf.worker.thread.healthmonitor;

import com.ipd.jsf.gd.error.ClientTimeoutException;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.error.NoAliveProviderException;
import com.ipd.jsf.gd.error.RpcException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExceptionHandler {

    public static String getExceptionCode(Exception e) {
        if (e == null)
            return "00000";
        String message = e.getMessage();
        Throwable cause = e.getCause();
        Pattern pattern = null;
        if (e instanceof InitErrorException) {
            pattern = Pattern.compile("Build consumer proxy error");//com.ipd.jsf.gd.error.InitErrorException: Build consumer proxy error![网络不通、Connection refused: no further information: /10.12.166.18:40660]
            Matcher matcher11 = pattern.matcher(message);
            if (matcher11.find()) {
                //Caused by: com.ipd.jsf.gd.error.InitErrorException: The consumer is depend on alive provider and there is no alive provider, you can ignore it by <jsf:consumer check="false"> (default is false)
                if (cause != null && cause.getMessage().contains("The consumer is depend on alive provider and there is no alive provider"))
                    return "10001";
            }
            pattern = Pattern.compile("Init provider's transport error!");//com.ipd.jsf.gd.error.InitErrorException: Init provider's transport error!
            Matcher matcher12 = pattern.matcher(message);
            if (matcher12.find()) {
                return "10002";
            }
            return "19999";
        } else if (e instanceof RpcException) {
            pattern = Pattern.compile("Fail to pass the server auth check in server:");
            Matcher matcher21 = pattern.matcher(message);
            if (matcher21.find()) {
                return "20001";
            }
            pattern = Pattern.compile("Cannot found invoker of");
            Matcher matcher22 = pattern.matcher(message);
            if (matcher22.find()) {
                return "20002";
            }
            pattern = Pattern.compile("validateRegistry, JsfUrl is null.");
            Matcher matcher23 = pattern.matcher(message);
            if (matcher23.find()) {
                return "20003";
            }
            pattern = Pattern.compile("validateRegistry, JsfUrl->alias is not correct.");
            Matcher matcher24 = pattern.matcher(message);
            if (matcher24.find()) {
                return "20004";
            }
            pattern = Pattern.compile("validateRegistry, JsfUrl->iface is not correct. ");
            Matcher matcher25 = pattern.matcher(message);
            if (matcher25.find()) {
                return "20005";
            }
            pattern = Pattern.compile("validateRegistry, JsfUrl->ip is not correct. ");
            Matcher matcher26 = pattern.matcher(message);
            if (matcher26.find()) {
                return "20006";
            }
            pattern = Pattern.compile(" validateRegistry, JsfUrl->port is not correct.");
            Matcher matcher27 = pattern.matcher(message);
            if (matcher27.find()) {
                return "20007";
            }
            pattern = Pattern.compile("validateRegistry, JsfUrl->sttime is not correct.");
            Matcher matcher28 = pattern.matcher(message);
            if (matcher28.find()) {
                return "20008";
            }
            pattern = Pattern.compile("validateRegistry, JsfUrl->ip exceed register limit. 5分钟内超过访问次数上限，请稍候重试.");
            Matcher matcher29 = pattern.matcher(message);
            if (matcher29.find()) {
                return "20009";
            }
            //doRegister: " + jsfUrl.getIface() + "接口未注册，请登录JSF管理端录入接口，并提交审核。
            pattern = Pattern.compile("doRegister: [A-Za-z\\.]*[ ]*接口未注册，请登录JSF管理端录入接口，并提交审核。");
            Matcher matcher210 = pattern.matcher(message);
            if (matcher210.find()) {
                return "20010";
            }
            //com.ipd.jsf.gd.error.RpcException: Failed to call com.ipd.jsf.service.RegistryService.lookup on remote server after retry 4 times: [],等等
            pattern = Pattern.compile("Failed to call [A-Za-z\\.]*[ ]* on remote server");
            Matcher matcher211 = pattern.matcher(message);
            if (matcher211.find()) {
                //last exception is cause by:com.ipd.jsf.gd.error.ClientTimeoutException, message is: Waiting provider return response timeout.
                if (cause != null && cause instanceof ClientTimeoutException) {
                    return "20011";
                }
            }
            //注册中心注册失败，com.ipd.jsf.gd.error.RpcException: doRegister is failed.JsfUrl()
            pattern = Pattern.compile("doRegister is failed");
            Matcher matcher212 = pattern.matcher(message);
            if (matcher212.find()) {
                return "20012";
            }
            pattern = Pattern.compile("validateUnSubscribe JsfUrl is not correct.");
            Matcher matcher213 = pattern.matcher(message);
            if (matcher213.find()) {
                return "20013";
            }
            pattern = Pattern.compile("validateUnSubscribe JsfUrl->iface is not correct.");
            Matcher matcher214 = pattern.matcher(message);
            if (matcher214.find()) {
                return "20014";
            }
            pattern = Pattern.compile("validateUnSubscribe JsfUrl->insKey is not correct.");
            Matcher matcher215 = pattern.matcher(message);
            if (matcher215.find()) {
                return "20015";
            }
            pattern = Pattern.compile("Registry is not available now! Current registry address list is");
            Matcher matcher216 = pattern.matcher(message);
            if (matcher216.find()) {
                return "20016";
            }
            return "29999";
        } else if (e instanceof NoAliveProviderException) {
            return "30001";
        } else {
            pattern = Pattern.compile("lookup is failed.");
            Matcher matcher51 = pattern.matcher(message);
            if (matcher51.find()) {
                return "20051";
            }
            pattern = Pattern.compile("doUnSubscribe is error ,");
            Matcher matcher52 = pattern.matcher(message);
            if (matcher52.find()) {
                return "20052";
            }
            return "59999";
        }
    }

    public static String getErrorMessage(Throwable e) {
        StackTraceElement[] stackElements = e.getStackTrace();
        StringBuffer sb = new StringBuffer();
        if (stackElements != null) {
            sb.append(e.toString() + "\r\n");
            for (int i = 0; i < stackElements.length; i++) {
                sb.append("\tat ").append(stackElements[i]).append("\n");
            }
        } else {
            sb.append(e.getMessage());
        }
        return sb.toString();
    }

}