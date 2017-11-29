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

package com.ipd.jsf.util;

import java.security.MessageDigest;

public class MD5Util {

    public MD5Util() {
    }

    private static final String MD5(String s) {
        char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        try {
            byte[] e = s.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(e);
            byte[] md = mdInst.digest();
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;

            for(int i = 0; i < j; ++i) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 15];
                str[k++] = hexDigits[byte0 & 15];
            }

            return new String(str);
        } catch (Exception var10) {
            var10.printStackTrace();
            return null;
        }
    }

    public static String getSign(String appCode, String businessId, String requestTimestamp, String safetyKey, String params) {
        return MD5(appCode + businessId + requestTimestamp + safetyKey + params);
    }

    public static String getSign(String appCode, String businessId, Long requestTimestamp, String safetyKey, String params) {
        return MD5(appCode + businessId + requestTimestamp + safetyKey + params);
    }

    public static void main(String[] args) {
        System.out.println(MD5("ops2015-08-28 11:47:52.000JZ6MH0623DIU09LMC06Pbjsgp"));
        System.out.println(MD5("加密"));
    }
}
