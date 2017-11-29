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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(NetUtils.class);

    public static final String LOCALHOST = "127.0.0.1";

    public static final String ANYHOST = "0.0.0.0";

    private static final int RND_PORT_START = 30000;
    
    private static final int RND_PORT_RANGE = 10000;
    
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    
    public static int getRandomPort() {
        return RND_PORT_START + RANDOM.nextInt(RND_PORT_RANGE);
    }

    public static int getAvailablePort() {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket();
            ss.bind(null);
            return ss.getLocalPort();
        } catch (IOException e) {
            return getRandomPort();
        } finally {
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
    public static int getAvailablePort(String host, int port) {
    	if (port <= 0) {
    		return getAvailablePort();
    	}
    	for(int i = port; i < 65535; i ++) {
    		ServerSocket ss = null;
            try {
        		ss = new ServerSocket();
        		ss.bind(new InetSocketAddress(host, i));
				logger.info("ip:{} port:{} is available", host, i);
        		return i;
            } catch (IOException e) {
				// continue
				logger.error("无法绑定服务到该地址[" + host + ":" + i
						+ "]，可能该地址的IP对应的网卡不可用或者端口已被占用，尝试下一个端口：{}", i, i + 1);
            } finally {
                if (ss != null) {
                    try {
                        ss.close();
                    } catch (IOException e) {
                    }
                }
            }
    	}
		throw new RuntimeException("无法绑定服务到该指定的IP" + host + "的任何端口，请检查配置");
    }

    private static final int MIN_PORT = 0;
    
    private static final int MAX_PORT = 65535;
    
    public static boolean isInvalidPort(int port){
        return port > MIN_PORT || port <= MAX_PORT;
    }

    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}\\:\\d{1,5}$");

    public static boolean isValidAddress(String address){
    	return ADDRESS_PATTERN.matcher(address).matches();
    }

    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");
    public static boolean isInvalidLocalHost(String host) {
        return host == null 
        			|| host.length() == 0
                    || host.equalsIgnoreCase("localhost")
                    || host.equals("0.0.0.0")
                    || (LOCAL_IP_PATTERN.matcher(host).matches());
    }
    
    public static boolean isValidLocalHost(String host) {
    	return ! isInvalidLocalHost(host);
    }

    public static InetSocketAddress getLocalSocketAddress(String host, int port) {
        return isInvalidLocalHost(host) ? 
        		new InetSocketAddress(port) : new InetSocketAddress(host, port);
    }

    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    private static boolean isValidAddress(InetAddress address) {
        if (address == null || address.isLoopbackAddress())
            return false;
        String name = address.getHostAddress();
        return (name != null 
                && ! ANYHOST.equals(name)
                && ! LOCALHOST.equals(name) 
                && IP_PATTERN.matcher(name).matches());
    }
    
    public static String getLocalHost(){
        InetAddress address = getLocalAddress();
        return address == null ? null : address.getHostAddress();
    }
    
    public static String getCurrentHost(){
    	InetAddress address = getLocalAddress0();
    	return address == null ? null : address.getHostAddress();
    }
    
    public static String filterLocalHost(String host) {
    	if (NetUtils.isInvalidLocalHost(host)) {
    		return NetUtils.getLocalHost();
    	}
    	return host;
    }
    
    private static volatile InetAddress LOCAL_ADDRESS = null;

    /**
     * 遍历本地网卡，返回第一个合理的IP。
     * 
     * @return 本地网卡IP
     */
    public static InetAddress getLocalAddress() {
    	if (LOCAL_ADDRESS != null)
    		return LOCAL_ADDRESS;
    	InetAddress localAddress = getLocalAddress0();
    	LOCAL_ADDRESS = localAddress;
    	return localAddress;
    }
    
    private static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Throwable e) {
            logger.warn("Failed to retriving ip address, " + e.getMessage(), e);
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = interfaces.nextElement();
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        if (addresses != null) {
                            while (addresses.hasMoreElements()) {
                                try {
                                    InetAddress address = addresses.nextElement();
                                    if (isValidAddress(address)) {
                                        return address;
                                    }
                                } catch (Throwable e) {
                                    logger.warn("Failed to retriving ip address, " + e.getMessage(), e);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        logger.warn("Failed to retriving ip address, " + e.getMessage(), e);
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn("Failed to retriving ip address, " + e.getMessage(), e);
        }
        logger.error("Could not get local host ip address, will use 127.0.0.1 instead.");
        return localAddress;
    }
    
    private static final Map<String, String> hostNameCache = new HashMap<String, String>(1000);

    public static String getHostName(String address) {
    	try {
    		int i = address.indexOf(':');
    		if (i > -1) {
    			address = address.substring(0, i);
    		}
    		String hostname = hostNameCache.get(address);
    		if (hostname != null && hostname.length() > 0) {
    			return hostname;
    		}
    		InetAddress inetAddress = InetAddress.getByName(address);
    		if (inetAddress != null) {
    			hostname = inetAddress.getHostName();
    			hostNameCache.put(address, hostname);
    			return hostname;
    		}
		} catch (Throwable e) {
			// ignore
		}
		return address;
    }
    
    public static String toAddressString(InetSocketAddress address) {
        return address.getAddress().getHostAddress() + ":" + address.getPort();
    }
    
    public static InetSocketAddress toAddress(String address) {
        int i = address.indexOf(':');
        String host;
        int port;
        if (i > -1) {
            host = address.substring(0, i);
            port = Integer.parseInt(address.substring(i + 1));
        } else {
            host = address;
            port = 0;
        }
        return new InetSocketAddress(host, port);
    }
    
    public static String toURL(String protocol, String host, int port, String path) {
		StringBuilder sb = new StringBuilder();
		sb.append(protocol).append("://");
		sb.append(host).append(':').append(port);
		if( path.charAt(0) != '/' )
			sb.append('/');
		sb.append(path);
		return sb.toString();
	}

	/**
	 * 本地多ip情况下、连一下注册中心地址得到本地IP地址
	 * 
	 * @param registryIp
	 *            注册中心地址
	 * @return 本地多ip情况下得到本地能连上注册中心的IP地址
	 */
	public static String getLocalHostByRegistry(String registryIp) {
		String host = null;
		if (registryIp != null && registryIp.length() > 0) {
			List<InetSocketAddress> addrs = getIpListByRegistry(registryIp);
			for (int i = 0; i < addrs.size(); i++) {
				host = getLocalHostBySocket(addrs.get(i));
				if (host != null && !NetUtils.isInvalidLocalHost(host)) {
					return host;
				}
			}
		}
		if (NetUtils.isInvalidLocalHost(host)) {
			host = NetUtils.getLocalHost();
		}
		return host;
	}

	/**
	 * 通过连接远程地址得到本机内网地址
	 * 
	 * @param remoteAddress
	 *            远程地址
	 * @return 本机内网地址
	 */
	public static String getLocalHostBySocket(InetSocketAddress remoteAddress) {
		String host = null;
		try {
			// 去连一下远程地址
			Socket socket = new Socket();
			try {
				socket.connect(remoteAddress, 1000);
				// 得到本地地址
				host = socket.getLocalAddress().getHostAddress();
			} finally {
				try {
					socket.close();
				} catch (Throwable e) {
				}
			}
		} catch (Exception e) {
			logger.warn("连接不到该地址 {}，请检查配置 {}", remoteAddress.toString(), e);
		}
		return host;
	}

	/**
	 * 解析注册中心地址配置为多个连接地址
	 * 
	 * @param registryIp
	 *            注册中心地址
	 * @return
	 */
	public static List<InetSocketAddress> getIpListByRegistry(String registryIp) {
		List<String[]> ips = new ArrayList<String[]>();
		String defaultPort = null;

		String[] srcIps = registryIp.split(",");
		for (String add : srcIps) {
			int a = add.indexOf("://");
			if (a > -1) {
				add = add.substring(a + 3); // 去掉协议头
			}
			String[] s1 = add.split(":");
			if (s1.length > 1) {
				if (defaultPort == null && s1[1] != null && s1[1].length() > 0) {
					defaultPort = s1[1];
				}
				ips.add(new String[] { s1[0], s1[1] }); // 得到ip和端口
			} else {
				ips.add(new String[] { s1[0], defaultPort });
			}
			continue;
		}

		List<InetSocketAddress> ads = new ArrayList<InetSocketAddress>();
		for (int j = 0; j < ips.size(); j++) {
			String[] ip = ips.get(j);
			try {
				InetSocketAddress address = new InetSocketAddress(ip[0],
						Integer.parseInt(ip[1] == null ? defaultPort : ip[1]));
				ads.add(address);
			} catch (Exception e) {
			}
		}

		return ads;
	}
	
	/**
	 * 转换地址为标准的“IP1:port1,IP2:port2,IP3:port3,”写法，兼容旧配置
	 * 
	 * @param registryIp
	 *            原来的地址字符串
	 * @return 标准的地址字符串
	 */
	public static String getIpStringByRegistry(String registryIp) {
		List<InetSocketAddress> addresses = getIpListByRegistry(registryIp);
		StringBuilder sb = new StringBuilder();
		
		for (InetSocketAddress address : addresses) {
			sb.append(address.getAddress().getHostAddress()).append(":")
					.append(address.getPort()).append(",");
		}
		return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
	}
}
