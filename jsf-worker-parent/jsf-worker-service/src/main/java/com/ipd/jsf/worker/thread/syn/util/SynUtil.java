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
package com.ipd.jsf.worker.thread.syn.util;

import java.util.List;

import com.ipd.jsf.worker.service.common.URL;

public class SynUtil {
    public static final byte[] useful = new byte[] { 1 };

    public static final String SPLITSTR_SLASH = "/";

    public static final String SAF_SERVICE = "/saf_service";

    public static final String PROVIDERS = "/providers";

    public static final String CONSUMERS = "/consumers";

    public static final String REDIS = "redis";

    public static final String ZK = "zk";

    public static final String FROM_DB = "&source=db";

    public static final String FROM_DB_ENCODE = URL.encode(FROM_DB);

    /**
     * 存在相同的unique，且比当前safVersion更加先进的节点时，过滤掉当前的节点
     * @param providerList
     * @param provider
     * @return
     */
    public static boolean isBestVersion(List<String> providerList, String provider) {
        if (providerList.size() == 1) return true;
        for (String temp : providerList) {
            URL tu = URL.valueOf(URL.decode(temp));
            URL pu = URL.valueOf(URL.decode(provider));
            if (ConvertUtils.getServerUniqueKey(tu).equals(ConvertUtils.getServerUniqueKey(pu))) {
                // 判断safVersion
                String tuSV = tu.getParameter("safversion", "");
                String puSV = pu.getParameter("safversion", "");
                if (tuSV.compareTo(puSV) > 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
