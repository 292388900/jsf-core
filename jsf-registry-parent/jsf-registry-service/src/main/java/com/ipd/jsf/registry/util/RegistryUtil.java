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
package com.ipd.jsf.registry.util;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.domain.Server;
import com.ipd.jsf.registry.domain.ServerAlias;
import com.ipd.jsf.service.RegistryHttpService;
import com.ipd.jsf.service.RegistryService;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.vo.ProviderUrl;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JSFContext;

/**
 * 工具类
 */
public class RegistryUtil {
    private static final Logger logger = LoggerFactory.getLogger(RegistryUtil.class);
    public static volatile boolean isDBOK = true;
    private static String registryIP = null;
    private static int registryPort = 40660;
    private static String registryIPPort = null;
    private static int pid = 0;
    private static final String registryIfaceName = RegistryService.class.getName();
    private static final String registryHttpIfaceName = RegistryHttpService.class.getName();
    private static final String OS = System.getProperty("os.name").toLowerCase();

    private static String[] FILTERSTR = {RegistryConstants.JSFVERSION};
    
    private static Pattern NORMAL_COLON = Pattern.compile("^[a-zA-Z0-9\\-\\_\\.:]+$");

    public static boolean isTest = false;
    //升级版本时，需要修改下检查的时间
    public static long checkVersionTime = 1433229456000L;
    public static int jsfVersionInt = Constants.JSF_VERSION;
    public static String jsfVersionString = Constants.JSF_BUILD_VERSION.substring(0, Constants.JSF_BUILD_VERSION.indexOf("_"));

    //是否开启整体的berkeleydb, 默认开启
    public static volatile boolean isOpenWholeBerkeleyDB = true;
    //是否开启provider的berkeleydb, 默认开启
    public static volatile boolean isOpenProviderBerkeleyDB = true;
    
    public static boolean isLinux() {
        return OS.indexOf("linux") >= 0;
    }

    private static String myAppName = null;

    /**
     * 获取当前进程的pid
     * @return
     */
    public static int getPid() {
        if (pid == 0) {
            pid = Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        }
        return pid;
    }

    /**
     * 获取本地机器的IP
     * @return
     */
    public static String getRegistryIP() {
        try {
            if (registryIP == null || registryIP.equals("")) {
                String localIp = NetUtils.getLocalAddress().getHostAddress();
                if (localIp != null && !localIp.equals("")) {
                    registryIP = localIp;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return registryIP;
    }

    /**
     * 获取本地机器的IP
     * @return
     */
    public static int getRegistryPort() {
        return registryPort;
    }

    /**
     * 获取本地机器的IP
     * @return
     */
    public static void setRegistryPort(int registryPort) {
        RegistryUtil.registryPort = registryPort;
    }

    /**
     * 获取本地机器的IP:PORT
     * @return
     */
    public static String getRegistryIPPort() {
        if (registryIPPort == null || registryIPPort.isEmpty()) {
            String ip = getRegistryIP();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(ip).append(RegistryConstants.SPLITSTR_COLON).append(RegistryUtil.registryPort);
            registryIPPort = stringBuilder.toString();
        }

        return registryIPPort;
    }

    /**
     * 获取系统时间
     * @return
     */
    public static long getSystemCurrentTime() {
        return System.currentTimeMillis();
    }

    public static String getRegistryIfaceName() {
        return registryIfaceName;
    }
    
    public static String getRegistryHttpIfaceName() {
    	return registryHttpIfaceName;
    }

    /**
     * 从map中取值，转为String
     * @param attrs
     * @param key
     * @return
     */
    public static String getValueFromMap(Map<String, String> attrs, String key, String defaultValue) {
        String value = defaultValue;
        if (attrs != null && attrs.get(key) != null) {
            value = attrs.get(key);
        }
        return value;
    }

    /**
     * 从map中取值，转为int型
     * @param attrs
     * @param key
     * @return
     */
    public static int getIntValueFromMap(Map<String, String> attrs, String key, int defaultValue) {
        int value = defaultValue;
        if (attrs != null && attrs.get(key) != null) {
            try {
                value = Integer.valueOf(attrs.get(key)).intValue();
            } catch (Exception e) {
                logger.error("convert error:" + key, e);
            }
        }
        return value;
    }

    /**
     * 从src中拷贝key和value
     * 白名单模式，包含关键字复制
     * 黑名单模式，排除关键字，其它复制
     * @param src
     * @param contains  true: 白名单，false: 黑名单
     * @param keys
     * @return
     */
    public static Map<String, String> copyEntries(Map<String, String> src, boolean contains, List<String> keys) {
        Map<String, String> dest = new HashMap<String, String>();
        if (contains) { // 白名单模式，包含关键字复制
            for (Entry<String, String> e : src.entrySet()) {
                if (keys.contains(e.getKey())) { // keys包含的复制过去
                    dest.put(e.getKey(), e.getValue());
                }
            }
        } else { // 黑名单模式，排除关键字，其它复制
            for (Entry<String, String> e : src.entrySet()) {
                if (!keys.contains(e.getKey())) { // keys不包含才复制过去
                    dest.put(e.getKey(), e.getValue());
                }
            }
        }
        return dest;
    }

    /**
     * 截取字符
     * @param str
     * @param length
     * @return
     */
    public static String limitString(String str, int length) {
        if (str != null && str.length() > length) {
            str = str.substring(0, length);
        }
        return str;
    }

    /**
     * 获取key值，key: alias:protocol
     * @param alias
     * @param protocol
     * @return
     */
    public static String getAliasProtocolKey(String alias, int protocol) {
        StringBuilder builder = new StringBuilder();
        builder.append(alias).append(RegistryConstants.SPLITSTR_COLON).append(protocol);
        return builder.toString();
    }

    /**
     * 获取接口下的alias对应的serverId
     * @param list
     * @param aliasType
     * @return
     */
    public static Map<Integer, Map<String, List<Integer>>> getMapFromServerAliasList(List<ServerAlias> list, byte aliasType) {
        Map<Integer, Map<String, List<Integer>>> result = new HashMap<Integer, Map<String, List<Integer>>>();
        if (list != null && !list.isEmpty()) {
            for (ServerAlias serverAlias : list) {
                if (serverAlias.getAliasType() == aliasType) {
                    if (result.get(serverAlias.getInterfaceId()) == null) {
                        result.put(serverAlias.getInterfaceId(), new HashMap<String, List<Integer>>());
                    }
                    if (result.get(serverAlias.getInterfaceId()).get(serverAlias.getTargetAlias()) == null) {
                        result.get(serverAlias.getInterfaceId()).put(serverAlias.getTargetAlias(), new ArrayList<Integer>());
                    }
                    result.get(serverAlias.getInterfaceId()).get(serverAlias.getTargetAlias()).add(serverAlias.getId());
                }
            }
        }
        return result;
    }

    /**
     * 从实例key中获取ip
     * @param insKey
     * @return
     */
    public static String getIpFormInsKey(String insKey) {
        String ip = null;
        try {
            if (insKey != null && !insKey.isEmpty()) {
                ip = insKey.substring(0, insKey.indexOf(RegistryConstants.SPLITSTR_UNDERLINE));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return ip == null ? "" : ip;
    }

    /**
     * 匹配正则表达式
     * @param expression
     * @param str
     * @return
     */
    public static boolean match(String expression, String str){
        Pattern p = Pattern.compile(expression);
        Matcher m = p.matcher(str);
        boolean b = m.matches();
        return b;
    }

    /**
     * 将list转为string
     * @param list
     * @return
     */
    public static String getStringFromList(List<String> list) {
        String result = "";
        StringBuilder builder = new StringBuilder();
        for (String str : list) {
            builder.append(str).append(RegistryConstants.SPLITSTR_COMMA);
        }
        if (builder.length() > 0) {
            result = builder.substring(0, builder.length() - 1);
        }
        return result;
    }

    /**
     * 取随机值
     * @return
     */
    public static int getRanNegativeInt() {
        Random dom = new Random();
        int temp = dom.nextInt();
        temp = Math.abs(temp);
        return -temp;
    }

    /**
     * 将map转为list
     * @param list
     * @return
     */
    public static Map<Integer, Server> getMapFromList(List<Server> list) {
        Map<Integer, Server> result = new HashMap<Integer, Server>();
        if (list != null && !list.isEmpty()) {
            for (Server server : list) {
                result.put(server.getId(), server);
            }
        }
        return result;
    }

    /**
     * 解析mapstring(由map.toString生成的string) 为map
     * 如果filterStr为null，不做过滤，如果filterStr有值，则只将filterStr中的key对应的mapString解析到map中
     * @param mapString
     * @param filterStr
     * @return
     */
    private static Map<String, String> getMapFromMapString(String mapString, String... filterStr) {
        Map<String, String> map = new HashMap<String, String>();
        if (mapString != null && mapString.length() > 2 && filterStr != null && filterStr.length > 0) {
            try {
                int beginIndex = -1;
                int endIndex = -1;
                String substr[] = null;
                String paramString = mapString.substring(1, mapString.length() - 1);
                for (String str : filterStr) {
                    beginIndex = paramString.indexOf(str);
                    if (beginIndex > -1) {
                        endIndex = paramString.substring(beginIndex).indexOf(RegistryConstants.SPLITSTR_COMMA);
                        if (endIndex == -1) {  //如果没有', ',说明是最后一个，就赋值全部长度
                            endIndex = paramString.length();
                        } else {
                        	endIndex += beginIndex;
                        }
                        substr = paramString.substring(beginIndex, endIndex).split(RegistryConstants.SPLITSTR_EQUALMARK);
                        map.put(substr[0], substr[1]);
                    }
                }
            } catch (Exception e) {
                logger.error("error:{}, mapstring:{}, filterStr:{}", e.getMessage(), mapString, filterStr);
            }
        }
        return map;
    }

    /**
     * 解析mapstring(由map.toString生成的string) ，根据filterStr找到特定的值
     * 如果filterStr为null，不做过滤，如果filterStr有值，则只将filterStr中的key对应的mapString解析到map中
     * @param mapString
     * @param filterStr
     * @return
     */
    public static String getIntValueFromMapString(String mapString, String filterStr) {
        String result = null;
        if (mapString != null && mapString.length() > 2 && filterStr != null && filterStr.length() > 0) {
            try {
                int beginIndex = -1;
                int endIndex = -1;
                String substr[] = null;
                String paramString = mapString.substring(1, mapString.length() - 1);
                beginIndex = paramString.indexOf(filterStr);
                if (beginIndex > -1) {
                    endIndex = paramString.substring(beginIndex).indexOf(RegistryConstants.SPLITSTR_COMMA) + beginIndex;
                    if (endIndex == -1) {  //如果没有', ',说明是最后一个，就赋值全部长度
                        endIndex = paramString.length();
                    }
                    substr = paramString.substring(beginIndex, endIndex).split(RegistryConstants.SPLITSTR_EQUALMARK);
                    result = substr[1];
                }
            } catch (Exception e) {
                logger.error("error:{}, mapstring:{}, filterStr:{}", e.getMessage(), mapString, filterStr);
            }
        }
        return result;
    }

    /**
     * 将map的string转换为map，并取出filterstr中包含的key对应的value
     * @param mapString
     * @return
     */
    public static Map<String, String> getAttrMap(String mapString) {
        return getMapFromMapString(mapString, FILTERSTR);
    }

    /**
     * 检查alias是否匹配
     * @param alias
     * @return
     */
    public static boolean checkAlias (String alias) {
        return NORMAL_COLON.matcher(alias).find();
    }

    /**
     * 将jsfUrl转为providerUrl
     * @param list
     * @return
     */
    public static List<ProviderUrl> getProviderUrlFromJsfUrl(List<JsfUrl> list) {
        List<ProviderUrl> result = new ArrayList<ProviderUrl>();
        if (list != null) {
            for (JsfUrl jsfUrl : list) {
                ProviderUrl providerUrl = new ProviderUrl();
                providerUrl.setIp(jsfUrl.getIp());
                providerUrl.setPort(jsfUrl.getPort());
                providerUrl.setAlias(jsfUrl.getAlias());
                providerUrl.setProtocol(jsfUrl.getProtocol());
                providerUrl.setAttrs(jsfUrl.getAttrs());
                result.add(providerUrl);
            }
        }
        return result;
    }

    public static String getMyAppName() {
        if (myAppName == null) {
            myAppName = (String)JSFContext.get(JSFContext.KEY_APPNAME);
        }
        return myAppName;
    }

    /**
     * 将jsfUrl转为providerUrl
     * @param list
     * @return
     */
    public static List<ProviderUrl> getProviderUrlFromJsfUrl4List(List<JsfUrl> list) {
        List<ProviderUrl> result = new ArrayList<ProviderUrl>();
        if (list != null) {
            for (JsfUrl jsfUrl : list) {
                ProviderUrl providerUrl = new ProviderUrl();
                providerUrl.setIp(jsfUrl.getIp());
                providerUrl.setPort(jsfUrl.getPort());
                providerUrl.setAlias(jsfUrl.getAlias());
                providerUrl.setProtocol(jsfUrl.getProtocol());
//                providerUrl.setAttrs(jsfUrl.getAttrs());
                result.add(providerUrl);
            }
        }
        return result;
    }

    public static String messageHandler(String message) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(message);
        stringBuffer.append(" [registry ip: ");
        stringBuffer.append(RegistryUtil.getRegistryIPPort());
        stringBuffer.append(" ]");
        return stringBuffer.toString();
    }

    public static void main(String[] args) {
//        String reg = "10.12.122.([1-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])";
//        String ip = "10.12.122.254";
//        Pattern pattern = Pattern.compile(reg);
//        Matcher matcher = pattern.matcher(ip);
//        if (matcher.matches()) {
//            System.out.println("yes");
//        } else {
//            System.out.println("no");
//        }
        
//        System.out.println(getMapFromMapString("{dynamic=true, crossLang=false, jsfVersion=1000, safVersion=210, language=java}", new String[]{"jsfVersion", "safVersion"}));
        System.out.println(getMapFromMapString("{app=jsf-check-worker, LANGUAGE=java, appId=10701, jsfVersion=1551}", new String[]{"jsfVersion"}));

        //        Map<Integer, JsfSerialization> map = new HashMap<Integer, JsfSerialization>();
//        JsfSerialization jsf = new JsfSerialization();
//        jsf.setList(new ArrayList<String>());
//        jsf.getList().add("msgpack");
//        jsf.getList().add("hessian");
//        JsfSerialization jsf2 = new JsfSerialization();
//        jsf2.setList(new ArrayList<String>());
//        jsf2.getList().add("msgpack");
//        jsf2.getList().add("hessian");
//        map.put(1010, jsf2);
//        map.put(1000, jsf);
//        System.out.println(JSON.toJSONString(map));
    }
}
