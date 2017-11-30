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

public class RegistryUtil {
	/**
	 * @param mapString
	 * @param filterStr
     * @return
     */
	public static String getValueFromAttr(String mapString, String filterStr) {
		String result = null;
		if(mapString == null || mapString.length() == 0){
			return result;
		}
		try {
            int beginIndex = -1;
            int endIndex = -1;
            String substr[] = null;
            String paramString = mapString.substring(1, mapString.length() - 1);
            beginIndex = paramString.indexOf(filterStr);
            if (beginIndex > -1) {
                endIndex = paramString.substring(beginIndex).indexOf(",") + beginIndex;
                if (endIndex == -1) {  //如果没有', ',说明是最后一个，就赋值全部长度
                    endIndex = paramString.length();
                }
                substr = paramString.substring(beginIndex, endIndex).split("=");
            }
            if(substr == null || substr.length != 2){
            	return result;
            }
            return substr[1];
        } catch (Exception e) {
           e.printStackTrace();
        }
		return result;
	}
	
	public static String getValueFromDeslUrl(String descUrl, String filterStr){
		String result = null;
		if(descUrl == null || descUrl.length() == 0){
			return result;
		}
		try {
			descUrl = descUrl.replace("JsfUrl(", "").replace(")", "");
			int beginIndex = -1;
            int endIndex = -1;
            String substr[] = null;
            beginIndex = descUrl.indexOf(filterStr);
            if (beginIndex > -1) {
                endIndex = descUrl.substring(beginIndex).indexOf(",") + beginIndex;
                if (endIndex == -1) {  //如果没有', ',说明是最后一个，就赋值全部长度
                    endIndex = descUrl.length();
                }
                //TODO BUG
                substr = descUrl.substring(beginIndex, endIndex).split("=");
            }
            if(substr == null || substr.length != 2){
            	return result;
            }
            return substr[1];
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static void main(String[] args) {
		String desc = "JsfUrl(ip:127.0.0.1,port:0,pid:32199,interface:com.ipd.jsf.monitor.query.RegistryDataService,alias:monitorquery,protocol:1,timeout:0,random:false,startTime:1420707930047,insKey:127.0.0.1_32199_30047,dataversion:1419842061000,attrs:{apppath=/export/jsf/saf-admin-tomcat/bin, timestamp=1420707931617, jsfVersion=1000, re-reg=true, safVersion=210, language=java, consumer=1})";
		System.out.println(getValueFromDeslUrl(desc, "jsfVersion"));
	}
}
