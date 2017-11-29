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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ipd.jsf.common.enumtype.InstanceStatus;
import com.ipd.jsf.common.enumtype.SourceType;
import com.ipd.jsf.common.util.UniqkeyUtil;
import com.ipd.jsf.worker.domain.Client;
import com.ipd.jsf.worker.domain.Server;
import com.ipd.jsf.worker.service.common.Constants;
import com.ipd.jsf.worker.service.common.URL;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.util.Constants.ProtocolType;
import com.ipd.jsf.worker.domain.InterfaceInfo;

public class ConvertUtils {
    private static final Logger logger = LoggerFactory.getLogger(ConvertUtils.class);
    private static final String SAFPID_KEY = "safpid";

    private static final String SAFVERSION_KEY = "safversion";

    private static final String RANDOMPORT_KEY = "randomPort";

    private static final String DEF_APPPATH = "safDefAppPath";

    public static final String SPLITSTR_SEMICOLON = ";";

    private static final String SPLITSTR_COLON = ":";

    private static final byte STATUS_ONLINE = 1;
    
    private static final byte STATUS_ONLINEBUTNOTWORK = 0;
    
    private static final byte STATUS_OFFLINE = 2;

    private static final byte STATUS_OFFLINEBUTNOTWORK = 2;

    private static final byte STATUS_OTHER = -1;
    
    /**
	 * 2.0-server --> 1.0-url
	 * @param server
	 * @param intf
	 * @return
	 */
	public static String convertDBServer2URL(Server server, String intf, boolean needServerId){
        if (server == null) return null;
        Map<String, String> map = new HashMap<String, String>();
        String alias = server.getAlias();
//		int split = alias.lastIndexOf(Constants.SYN_GROUP_VERSION_SPLIT);
//		//判断最后一组是否为数字
//		if (split <= 0) {
//			//2.0只同步version
////			map.put(Constants.GROUP_KEY, "");
//			map.put(Constants.VERSION_KEY, alias);
//		} else {
//			map.put(Constants.GROUP_KEY, alias.substring(0, split));
//			if (!"".equals(alias.substring(split+1, alias.length()))) {
//				map.put(Constants.VERSION_KEY, alias.substring(split+1, alias.length()));
//			}
//		}

        map.put(Constants.VERSION_KEY, alias); // 把alias放入version中
        if (!(null == server.getSafVer() || server.getSafVer() <= 0)) {
            map.put(Constants.SAFVERSION_KEY, intToVersion(server.getSafVer()));
        } else {
            map.put(Constants.SAFVERSION_KEY, "");
        }

        if (!(null == server.getToken() || "".equals(server.getToken()))) {
            map.put(Constants.TOKEN_KEY, server.getToken());
        }

        // 1.0与2.0状态不完全对应，需要特别处理一下
        map.put(Constants.STATUS_KEY, String.valueOf(status(server.getStatus())[0]));
        map.put(Constants.WEIGHT_KEY, server.getWeight() == null ? "" : server.getWeight() + "");

        map.put(Constants.INTERFACE_KEY, server.getInterfaceName());
        if (needServerId) {
            map.put("id", server.getId() + "");
        }

        URL url = new URL(convertProtocolToString(server.getProtocol()), server.getIp(), server.getPort(), map);

        return url.toFullString();
	}

    public static String convertDBConsumer2URL(Client client, String intf) {
        if (client == null) return null;
        Map<String, String> map = new HashMap<String, String>();
    	String alias = client.getAlias();
//		int split = alias.lastIndexOf(Constants.SYN_GROUP_VERSION_SPLIT);
//		//判断最后一组是否为数字
//		
//		if (split <= 0) {
//			//2.0只同步version
////			map.put(Constants.GROUP_KEY, "");
//			map.put(Constants.VERSION_KEY, alias);
//		} else {
//			map.put(Constants.GROUP_KEY, alias.substring(0, split));
//			if (!"".equals(alias.substring(split+1, alias.length()))) {
//				map.put(Constants.VERSION_KEY, alias.substring(split+1, alias.length()));
//			}
//		}
    	map.put(Constants.VERSION_KEY, alias); //把alias放入version中
        if (client.getPid() >= 0) {
            map.put(SAFPID_KEY, client.getPid() + "");
        } else {
        	map.put(SAFPID_KEY, "");
        }
        
		if(client.getSafVer() > 0){
			map.put(Constants.SAFVERSION_KEY, intToVersion(client.getSafVer()));
		} else {
			map.put(Constants.SAFVERSION_KEY, "");
		}
        
        if (client.getStartTime() > 0) {
            map.put(Constants.TIMESTAMP_KEY, String.valueOf(client.getStartTime()));
        } else {
        	map.put(Constants.TIMESTAMP_KEY, "");
        }
        
        map.put(Constants.INTERFACE_KEY, intf);
        map.put(Constants.ID_KEY, client.getId() + "");
        
        URL url = new URL(ProtocolType.consumer.toString(), client.getIp(), 0, map);
        return url.toFullString();
    }

	/**
	 * 重构zk上的provider节点, zk 同步到db时使用
	 * @param provider : zk上面的节点
	 * @return
	 */
	public static String convertZKProvider2URL(String iface, String provider, byte status){//
		URL url = URL.valueOf(URL.decode(provider));
		if (url.getServiceInterface() == null || url.getServiceInterface().equals("")) return null;
		if (url.getServiceInterface().equals("/") && iface != null && !iface.isEmpty()) {
			url = url.setServiceInterface(iface);
		}
		Map<String, String> map = new HashMap<String, String>();
//		if(!(null == url.getParameter(Constants.GROUP_KEY) || "".equals(url.getParameter(Constants.GROUP_KEY)))){
//			map.put(Constants.GROUP_KEY, url.getParameter(Constants.GROUP_KEY));
//		}
//		if(!(null == url.getParameter(Constants.VERSION_KEY) || "".equals(url.getParameter(Constants.VERSION_KEY)))){
//			map.put(Constants.VERSION_KEY, url.getParameter(Constants.VERSION_KEY));
//		}
		String alias = null;
        if (!(null == url.getParameter(Constants.GROUP_KEY) || "".equals(url.getParameter(Constants.GROUP_KEY)))) {
            alias = url.getParameter(Constants.GROUP_KEY);
        }
        if (!(null == url.getParameter(Constants.VERSION_KEY) || "".equals(url.getParameter(Constants.VERSION_KEY)))) {//zk上的version不可能为空或者null
            if (alias == null || alias.equals("")) {
                alias = url.getParameter(Constants.VERSION_KEY);
            } else {
                alias = alias + SPLITSTR_COLON + url.getParameter(Constants.VERSION_KEY);
            }
        }
        map.put(Constants.VERSION_KEY, alias);   //把alias放入version中
		if(!(null == url.getParameter(SAFVERSION_KEY) || "".equals(url.getParameter(SAFVERSION_KEY)))){
			map.put(SAFVERSION_KEY, url.getParameter(SAFVERSION_KEY));
		}
		if(!(null == url.getParameter(Constants.TOKEN_KEY) || "".equals(url.getParameter(Constants.TOKEN_KEY)))){
			map.put(Constants.TOKEN_KEY, url.getParameter(Constants.TOKEN_KEY));
		}

        if (status >= 0 && status <= 3) {
			map.put(Constants.STATUS_KEY, String.valueOf(status));//需要状态对比,感知节点状态变化
        } else {
			map.put(Constants.STATUS_KEY, String.valueOf(STATUS_OTHER));//其他状态统一为-1
		}
		map.put(Constants.WEIGHT_KEY, url.getParameter(Constants.WEIGHT_KEY, ""));
		map.put(Constants.INTERFACE_KEY, url.getServiceInterface());
		URL result = new URL(url.getProtocol(), url.getIp(), url.getPort(), map);
		return result.toFullString();
	}

    /**
     * 重构zk上的provider节点, db 同步到zookeeper时使用
     * @param provider : zk上面的节点
     * @return
     */
    public static String filterZKProvider(String provider, byte status){
        URL url = URL.valueOf(URL.decode(provider));
        if (url.getServiceInterface() == null || url.getServiceInterface().equals("")) return null;
        Map<String, String> map = new HashMap<String, String>();
//      if(!(null == url.getParameter(Constants.GROUP_KEY) || "".equals(url.getParameter(Constants.GROUP_KEY)))){
//          map.put(Constants.GROUP_KEY, url.getParameter(Constants.GROUP_KEY));
//      }
//      if(!(null == url.getParameter(Constants.VERSION_KEY) || "".equals(url.getParameter(Constants.VERSION_KEY)))){
//          map.put(Constants.VERSION_KEY, url.getParameter(Constants.VERSION_KEY));
//      }
        if(!(null == url.getParameter(SAFVERSION_KEY) || "".equals(url.getParameter(SAFVERSION_KEY)))){
            map.put(SAFVERSION_KEY, url.getParameter(SAFVERSION_KEY));
        }
        String alias = null;
        if (!(null == url.getParameter(Constants.VERSION_KEY) || "".equals(url.getParameter(Constants.VERSION_KEY)))) {//zk上的version不可能为空或者null
            alias = url.getParameter(Constants.VERSION_KEY);
            if (alias != null) {
                String group = null;
                String version = null;
                if (alias.lastIndexOf(SPLITSTR_COLON) > 0) {
                    group = alias.substring(0, alias.lastIndexOf(SPLITSTR_COLON));
                    version = alias.substring(alias.lastIndexOf(SPLITSTR_COLON) + 1);
                } else {  //如果alias里不包含冒号, group为空, version置为alias
                    group = "";
                    version = alias;
                }
                map.put(Constants.GROUP_KEY, group);
                map.put(Constants.VERSION_KEY, version);
            }
        }
        if(!(null == url.getParameter(Constants.TOKEN_KEY) || "".equals(url.getParameter(Constants.TOKEN_KEY)))){
            map.put(Constants.TOKEN_KEY, url.getParameter(Constants.TOKEN_KEY));
        }
//
//        if (status >= 0 && status <= 3) {
//            map.put(STATUS, String.valueOf(status));//需要状态对比,感知节点状态变化
//        } else {
//            map.put(STATUS, String.valueOf(STATUS_OTHER));//其他状态统一为-1
//        }
        map.put(Constants.WEIGHT_KEY, url.getParameter(Constants.WEIGHT_KEY, ""));
        map.put(Constants.INTERFACE_KEY, url.getServiceInterface());
        URL result = new URL(url.getProtocol(), url.getIp(), url.getPort(), map);
        return result.toFullString();
    }

	/**
	 * 重构zk上consumer节点, zk 同步到db时使用
	 * @param intf
	 * @param consumer
	 * @param status
	 * @return
	 */
	public static String convertZKConsumer2String(String intf, String consumer, byte status){
		URL url = URL.valueOf(URL.decode(consumer));
		if (url.getServiceInterface() == null || url.getServiceInterface().equals("")) return null;
		Map<String, String> map = new HashMap<String, String>();
//		if(!(null == url.getParameter(Constants.GROUP_KEY) || "".equals(url.getParameter(Constants.GROUP_KEY)))){
//			map.put(Constants.GROUP_KEY, url.getParameter(Constants.GROUP_KEY));
//		}
		if(!(null == url.getParameter(SAFVERSION_KEY) || "".equals(url.getParameter(SAFVERSION_KEY)))){
			map.put(SAFVERSION_KEY, url.getParameter(SAFVERSION_KEY));
		}
		if(!(null == url.getParameter(SAFPID_KEY) || "".equals(url.getParameter(SAFPID_KEY)))){
			map.put(SAFPID_KEY, url.getParameter(SAFPID_KEY));
		}
//		if(!(null == url.getParameter(Constants.VERSION_KEY) || "".equals(url.getParameter(Constants.VERSION_KEY)))){
//			map.put(Constants.VERSION_KEY, url.getParameter(Constants.VERSION_KEY));
//		}
	    String alias = null;
        if (!(null == url.getParameter(Constants.GROUP_KEY) || "".equals(url.getParameter(Constants.GROUP_KEY)))) {
            alias = url.getParameter(Constants.GROUP_KEY);
        }
        if (!(null == url.getParameter(Constants.VERSION_KEY) || "".equals(url.getParameter(Constants.VERSION_KEY)))) {//zk上的version不可能为空或者null
            if (alias == null || alias.equals("")) {
                alias = url.getParameter(Constants.VERSION_KEY);
            } else {
                alias = alias + SPLITSTR_COLON + url.getParameter(Constants.VERSION_KEY);
            }
        }
        map.put(Constants.VERSION_KEY, alias);   //把alias放入version中
		if(!(null == url.getParameter(Constants.TIMESTAMP_KEY) || "".equals(url.getParameter(Constants.TIMESTAMP_KEY)))){
		    map.put(Constants.TIMESTAMP_KEY, url.getParameter(Constants.TIMESTAMP_KEY));
		}
		URL result = new URL(url.getProtocol(), url.getIp(), url.getPort(), map);
		return result.setServiceInterface(intf).toFullString();
	}
	

	private static Server convertServer(URL url, InstanceStatus status){
		if(url == null) return null;
		Server server = new Server();
		server.setAppPath(url.getPath() == null ? DEF_APPPATH : url.getPath());
//		String group = url.getParameter(Constants.GROUP_KEY, "");
//		String version = url.getParameter(Constants.VERSION_KEY, "");
//		server.setAlias(group + SPLITSTR_COLON + version);
		server.setAlias(url.getParameter(Constants.VERSION_KEY, ""));   //把version放入alias中
		server.setIp(url.getIp().replaceAll("\'", ""));
		server.setPort(url.getPort());
		server.setSafVer(versionToInt(url.getParameter(SAFVERSION_KEY,"0")));
		server.setToken(url.getParameter(Constants.TOKEN_KEY));
		server.setSrcType(SourceType.zookeeper.value());
		server.setStatus(status.value());//取全部状态的节点-07-26
		server.setRandom("true".equals(url.getParameter(RANDOMPORT_KEY)));
		server.setCreateTime(new java.util.Date());
		server.setUpdateTime(new java.util.Date());
		server.setProtocol(convertProtocolToInt(url.getProtocol()));
		server.setTimeout(url.getParameter("timeout", 5000));
		
		if(url.getParameter("id") != null) {
			try {
				server.setId(Integer.parseInt(url.getParameter("id")));
			} catch (NumberFormatException e) { }
		}
		if(url.getParameter(Constants.WEIGHT_KEY) != null){
			try {
				server.setWeight(Integer.parseInt(url.getParameter(Constants.WEIGHT_KEY)));
			} catch (NumberFormatException e) { 
			}
		}
		return server;
	}
	
	private static int versionToInt(String version) {
		int ret = 0;
		if(StringUtils.isBlank(version))  {
			return ret;
		} else {
			String[] temp = StringUtils.split(version, ".");
		    String intString = "";
			for (String s : temp) {
				intString += s;
			}
			try {
//			    String intString = version.replaceAll(".", "");
				ret = Integer.parseInt(intString);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		return ret;
	}
	
	private static String intToVersion(int version) {
		if (version > 0) {
			String ret = "";
			String sv = version + "";
			for (int i = 0; i< sv.length(); i++) {
				ret += sv.charAt(i) + ".";
			}
			return ret.substring(0, ret.length()-1);
			
		} else {
			return "";
		}
	}
	
	private static Client convertClient(URL url, InstanceStatus status){
		if(url == null) return null;
		Client client = new Client();
		client.setAppPath(url.getPath() == null ? DEF_APPPATH : url.getPath());
//		String group = url.getParameter(Constants.GROUP_KEY, "");
//		String version = url.getParameter(Constants.VERSION_KEY, "");
//		client.setAlias(group + Constants.SYN_GROUP_VERSION_SPLIT + version);
		client.setAlias(url.getParameter(Constants.VERSION_KEY, ""));   //把version放入alias中
		client.setIp(url.getIp());
		client.setSafVer(versionToInt(url.getParameter(SAFVERSION_KEY, "0")));
		client.setToken(url.getParameter(Constants.TOKEN_KEY));
		client.setSrcType(SourceType.zookeeper.value());
		client.setStatus(InstanceStatus.online.value());//Consumer只有在线状态
		client.setCreateTime(new java.util.Date());
        if (url.getParameter(Constants.TIMESTAMP_KEY) != null) {
            client.setStartTime(Long.parseLong(url.getParameter(Constants.TIMESTAMP_KEY, "0")));
            try {
                client.setUpdateTime(new java.util.Date(Long.parseLong(url.getParameter(Constants.TIMESTAMP_KEY))));
            } catch (Exception e) {
                client.setUpdateTime(new java.util.Date());
            }
        } else {
            client.setStartTime(new Date().getTime());
            client.setUpdateTime(new java.util.Date());
        }
		if(url.getParameter("id") != null) {
			try {
				client.setId(Integer.parseInt(url.getParameter("id")));
			} catch (NumberFormatException e) { }
		}
		if(url.getParameter(SAFPID_KEY) != null){
			try {
				client.setPid(Integer.parseInt(url.getParameter(SAFPID_KEY)));
			} catch (NumberFormatException e) { }
		}
		return client;
	}

    /**
     * 这里面都是由DB->url->Server对象，
     * @param urlMap
     * @return
     */
    public static List<Server> convertUrlServer(Map<String, byte[]> urlMap) {
        List<Server> servers = null;
        if (urlMap != null) {
            servers = new ArrayList<Server>();
            for (Entry<String, byte[]> entry : urlMap.entrySet()) {
                URL url = URL.valueOf(entry.getKey());
                Server server = ConvertUtils.convertServer(url, status(entry.getValue()));
                servers.add(server);
            }
        }
        return servers;
    }
    
    /**
     * 这里面都是由DB->url->client对象，
     * @param urlMap
     * @return
     */
    public static List<Client> convertUrlClient(Map<String, byte[]> urlMap) {
        List<Client> clients = null;
        if (urlMap != null) {
            clients = new ArrayList<Client>();
            for (Entry<String, byte[]> entry : urlMap.entrySet()) {
                URL url = URL.valueOf(entry.getKey());
                Client client = ConvertUtils.convertClient(url, status(entry.getValue()));
                clients.add(client);
            }
        }
        return clients;
    }

	/**
	 * 1.0与2.0的状态不一致，需要特别注意
	 * @param status
	 * @return
	 */
	public static InstanceStatus status(byte[] status){
		InstanceStatus is = InstanceStatus.deleted;
		if(status == null || status.length < 0) return is;
	    if(status[0] == STATUS_ONLINE) is = InstanceStatus.online;
	    else if(status[0] == STATUS_ONLINEBUTNOTWORK) is = InstanceStatus.onlineButNotWork;
	    else if(status[0] == STATUS_OFFLINE) is = InstanceStatus.offline;
	    else if(status[0] == STATUS_OFFLINEBUTNOTWORK) is = InstanceStatus.offlineAndNotWork;
		return is;
	}

	public static byte[] status(int status) {
	    byte[] result = new byte[1];
        if (status == InstanceStatus.onlineButNotWork.value()) {
            result[0] = STATUS_ONLINEBUTNOTWORK;
        } else if (status == InstanceStatus.offline.value()) {
            result[0] = STATUS_OFFLINE;
        } else if (status == InstanceStatus.online.value()) {
            result[0] = STATUS_ONLINE;
        } else if (status == InstanceStatus.offlineAndNotWork.value()) {
            result[0] = STATUS_OFFLINEBUTNOTWORK;
        } else {
            result[0] = STATUS_OTHER;
        }
	    return result;
	}

	/**
	 * 将当前的serverInfo的信息保存给server
	 * @param serverList
	 * @param serviceInfo
	 * @return
	 */
	public static List<Server> setInfoToServer(List<Server> serverList, InterfaceInfo ifaceInfo) {
	    List<Server> result = new ArrayList<Server>();
		if(serverList != null) {
			for(Server server : serverList) {
			    if (server.getProtocol() > 0) {   //从saf1中同步过来的节点中有106版本的错误节点，Protocol是0，要去掉
    				server.setInterfaceId(ifaceInfo.getInterfaceId());
    				server.setInterfaceName(ifaceInfo.getInterfaceName());
    				server.setUniqKey(UniqkeyUtil.getServerUniqueKey(server.getIp(), server.getPort(), 
    						server.getAlias(), server.getProtocol(), ifaceInfo.getInterfaceId()));
    				result.add(server);
			    } else {
			        logger.warn("protocol is 0. interface:{}, server:{}", ifaceInfo.getInterfaceName(), server.toString());
			    }
			}
		}
		return result;
	}

    public static List<Client> setInfoToClient(List<Client> clientList, InterfaceInfo ifaceInfo) {
        if (clientList != null) {
            for (Client client : clientList) {
                client.setInterfaceId(ifaceInfo.getInterfaceId());
                client.setInterfaceName(ifaceInfo.getInterfaceName());
        		client.setUniqKey(UniqkeyUtil.getClientUniqueKey(client.getIp(), client.getPid(), 
        				client.getAlias(), client.getProtocol(), client.getInterfaceId()));
            }
        }
        return clientList;
    }

    public static String getServerUniqueKey(URL url) {
        StringBuilder sb = new StringBuilder();
        sb.append(url.getIp()).append(SPLITSTR_SEMICOLON);
        sb.append(url.getPort()).append(SPLITSTR_SEMICOLON);
//        sb.append(url.getParameter(Constants.GROUP_KEY)).append(SPLITSTR_SEMICOLON);
        sb.append(url.getParameter(Constants.VERSION_KEY)).append(SPLITSTR_SEMICOLON);  //这里version就是alias
        sb.append(url.getProtocol()).append(SPLITSTR_SEMICOLON);
        sb.append(url.getServiceInterface());
        return sb.toString();
    }

	private static String convertProtocolToString(int protocol) {
	    return ProtocolType.valueOf(protocol).name();
	}

	private static int convertProtocolToInt(String protocol) {
	    //对1.0.7版本的协议做safdubbo处理
	    if (protocol.equals("safdubbo") || protocol.equals("mydubbo")) {
	        return ProtocolType.valueOf("dubbo").value();
	    } else if (protocol.equals("rest1")) {
            return ProtocolType.valueOf("rest").value();
        }
	    return ProtocolType.valueOf(protocol).value();
	}
}
