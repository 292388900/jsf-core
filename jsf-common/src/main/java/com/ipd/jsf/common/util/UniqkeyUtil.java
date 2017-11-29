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

package com.ipd.jsf.common.util;

public class UniqkeyUtil {
    public final static String SPLITSTR_SEMICOLON = ";";
    public final static String SPLITSTR_UNDERLINE = "_";
    public final static int insKeyLimit = 8;

    public static String getServerUniqueKey(String ip, int port, String alias, int protocol, int interfaceId) {  // TODO  return "" 还是  throw Exception
        if (ip == null || ip.equals("") || interfaceId == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ip).append(SPLITSTR_SEMICOLON);
        sb.append(port).append(SPLITSTR_SEMICOLON);
        sb.append(alias).append(SPLITSTR_SEMICOLON);
        sb.append(protocol).append(SPLITSTR_SEMICOLON);
        sb.append(interfaceId);
        return sb.toString();
    }

    public static String getClientUniqueKey(String ip, int pid, String alias, int protocol, int interfaceId) {  // TODO  return "" 还是  throw Exception
        if (ip == null || ip.equals("") || interfaceId == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ip).append(SPLITSTR_SEMICOLON);
        sb.append(pid).append(SPLITSTR_SEMICOLON);
        sb.append(alias).append(SPLITSTR_SEMICOLON);
        sb.append(protocol).append(SPLITSTR_SEMICOLON);
        sb.append(interfaceId);
        return sb.toString();
    }
    
    public static String getAliasFromServerUniqKey(String uniqKey) {
    	return getParamFromUniqKey(uniqKey, 2);
    }

    private static String getParamFromUniqKey(String uniqKey, int index) {
    	String[] array = uniqKey.split(SPLITSTR_SEMICOLON);
    	if (array.length >= index) {
    		return array[index];
    	}
    	return null;
    }
    
    /**
     * 获取insKey
     * @param ip
     * @param pid
     * @param startTime
     * @return
     */
    public static String getInsKey(String ip, int pid, long startTime) {
        StringBuilder sb = new StringBuilder();
        sb.append(ip);
        sb.append(SPLITSTR_UNDERLINE);
        sb.append(pid);
        sb.append(SPLITSTR_UNDERLINE);
        if (String.valueOf(startTime).length() > insKeyLimit) {
            sb.append(String.valueOf(startTime).substring(insKeyLimit));
        } else {
            sb.append(String.valueOf(startTime).substring(0));
        }
        return sb.toString();
    }

    public static String getIpFromInsKey(String insKey) {
        return insKey.substring(0, insKey.indexOf(SPLITSTR_UNDERLINE));
    }

    public static int getPidFromInsKey(String insKey) {
        return Integer.parseInt(insKey.substring(insKey.indexOf(SPLITSTR_UNDERLINE) + 1, insKey.lastIndexOf(SPLITSTR_UNDERLINE)));
    }

    public static void main(String[] args) {
        System.out.println(getIpFromInsKey("10.12.122.28_2064_35709"));
        System.out.println(getPidFromInsKey("10.12.122.28_2064_35709"));
        System.out.println(getAliasFromServerUniqKey("125.32.0.2.;2356;chq;4;2451"));
    }
}
