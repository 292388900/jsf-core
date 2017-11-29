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
package com.ipd.jsf.worker.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 当前系统运行的环境
 */
public class EnvironmentHelper {

    private static Logger logger = LoggerFactory.getLogger(EnvironmentHelper.class);

    private static boolean isMainEnvironment = true;//默认主站
    private static boolean isJcloudEnvironment = true;//是否是公有云环境
    private static boolean isForeignEnvironment = false;//是否是国外

    private static boolean isOnlineEnvironment = false;//默认测试环境

    static {
        String environment = PropertyUtil.getProperties("environment");//部署环境
        if (environment != null && !"".equals(environment)) {
            switch (environment) {
                case "main":
                    isMainEnvironment = true;
                    isJcloudEnvironment = false;
                    isForeignEnvironment = false;
                    logger.info("isMainEnvironment:" + isMainEnvironment);
                    break;
                case "jcloud":
                    isJcloudEnvironment = true;
                    isMainEnvironment = false;
                    isForeignEnvironment = false;
                    logger.info("isJcloudEnvironment:" + isJcloudEnvironment);
                    break;
                case "foreign":
                    isForeignEnvironment = true;
                    isMainEnvironment = false;
                    isForeignEnvironment = false;
                    logger.info("isForeignEnvironment:" + isForeignEnvironment);
                    break;
                default:
                    logger.info("deploy environment unknown");
            }
        }
        String online = PropertyUtil.getProperties("event.online");//是否线上环境
        if(online != null && !online.isEmpty() && Boolean.valueOf(online)){
            isOnlineEnvironment = true;
        }
    }

    public static boolean isMainEnvironment() {
        return isMainEnvironment;
    }

    //判断是否是公有云环境。true为公有云环境
    public static boolean isJcloudEnvironment() {
        return isJcloudEnvironment;
    }

    public static boolean isForeignEnvironment() {
        return isForeignEnvironment;
    }

    public static boolean isOnlineEnvironment() {
        return isOnlineEnvironment;
    }

}
