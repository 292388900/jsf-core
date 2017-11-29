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

package com.ipd.jsf.zookeeper.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.ipd.jsf.zookeeper.IZkChildListener;
import com.ipd.jsf.zookeeper.IZkDataListener;
import com.ipd.jsf.zookeeper.common.Constants;
import com.ipd.jsf.zookeeper.common.URL;
import com.ipd.jsf.zookeeper.IZkStateListener;
import com.ipd.jsf.zookeeper.ZkClient;
import com.ipd.jsf.zookeeper.common.StringUtils;

import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @date 2012-12-02 取得zookeeper数据，延迟数秒取得数据；
 *       zookeeper不支持修改节点路径——采用如下机制来保证：状态写子节点同时写父节点来触发通知 注意并发写：带上版本
 *       上下线时序问题：后发生的事件先记录了；
 * @update 2013-04-20 将最新几秒的事件保存在queue中，采用定时更新的方法，保证时序的正确性
 * @update 2013-08-21 完善异常处理机制
 */
public class PathCache {

	private static final Logger logger = LoggerFactory.getLogger(PathCache.class);
	

	private ZkClient client;

	String path;
	
	Integer pathDataVersion = 0;//数据版本
	
	Integer pathChildVersion = 0;//字节点版本
	
	Long updateTime = System.currentTimeMillis();
	

	private boolean watchData = false;

	private Map<String, Byte[]> datas = new ConcurrentHashMap<String, Byte[]>();

	private static final String OFFLINESET = "OFFLINESET";
	private static final String ONLINESET = "ONLINESET";
	
	private volatile KeeperState oldState = null;

	private Map<ChildrenEvent, List<PathCacheListener>> listenerMap = new ConcurrentHashMap<ChildrenEvent, List<PathCacheListener>>();
	
	//先入队列，再定时从队列中取，保证处理时序的正确性
	//private LinkedBlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>(5000);

	//private ScheduledExecutorService eventHandleServices =  Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("PathCache-eventHandle "));

	public PathCache(final ZkClient client2, final String path2, final Boolean watchData2) {
		this.client = client2;
		this.path = path2;
		this.watchData = watchData2;
		//this.delay = _delay;
		listenerMap.put(ChildrenEvent.nodeChanged,
				new ArrayList<PathCacheListener>());
		listenerMap.put(ChildrenEvent.nodeAdded,
				new ArrayList<PathCacheListener>());
		listenerMap.put(ChildrenEvent.nodeDeleted,
				new ArrayList<PathCacheListener>());
		
		
		
		
		if(pathListener == null) pathListener = new IZkChildListener(){

			@Override
			public void handleChildChange(String parentPath,
					List<String> currentChilds) throws Exception {
				logger.info("ChildChange:{}",parentPath);
				Event childEvent = new Event();
				try {
					childEvent.setEventType(EventType.childrenChanged);
					childEvent.setChildrens(currentChilds);
					tagNotify(childEvent);
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
                    throw e;
				}
				
				
			}};
		
		if(dataListener == null)dataListener = new IZkDataListener(){

			@Override
			public void handleDataChange(String dataPath, Object data)
					throws Exception {
				try {
					logger.info("data changed Path:{}",dataPath);
					Event dataEvent = new Event();
					dataEvent.setEventType(EventType.dataChanged);
					//tagNotify(dataEvent);
				} catch (Exception e) {
					logger.error("Error when handle Data Change! ",e);
                    throw e;
				}
			}

			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
				try {
					Event dataEvent = new Event();
					dataEvent.setEventType(EventType.nodeDeleted);
					//tagNotify(dataEvent);
					
				} catch (Exception e) {
					logger.error("Error when handle Data Delete! ",e);
                    throw e;
				}
				
			}
			
		};
		
		if(stateListener == null) stateListener = new IZkStateListener(){

			@Override
			public void handleStateChanged(KeeperState state) throws Exception {
				try {
					logger.info("SAF PathCache State:{}",state);
					if((oldState == KeeperState.Disconnected ||oldState == KeeperState.Expired) && (state == KeeperState.SyncConnected || state == KeeperState.ConnectedReadOnly)){
						logger.info("need reconnect:oldState:{},newState:{}",oldState,state);
						reconnect();
					}
					oldState = state;
				} catch (Exception e) {
					logger.warn("Error when handle State Changed! ",e);
                    throw e;
				}
				
			}

			@Override
			public void handleNewSession() throws Exception {
				logger.info("SAF PathCache New Session event received!");
				
			}
			
		};
		
		/*
		 * 定时取回本路径的版本--数据版本 字节点版本
		 */
		VersionChecker.addVerionCheck(this);

	}
	
	/*
	 * 检查是否需要重新链接
	 */
	protected void checkNeedReconnect() {
		reconnect();//每次检查时都主动reconnect
		
		
	}

	/*
	 * 
	 * 重连上时重新注册
	 */
	protected void reconnect() {
		logger.debug("SAF PathCache for Path {} ReWatch for reconnected!",path);
		
		client.subscribeChildChanges(path, pathListener );
		if(watchData) client.subscribeDataChanges(path, dataListener );
		
	}

	IZkChildListener pathListener = null;
	
	IZkDataListener dataListener = null;
	
	IZkStateListener stateListener = null;

	/**
	 * 重新设置下zkClient
	 * 
	 * @param newZkClient
	 *            新的zkclient对象
	 */
	public void resetZkClient(ZkClient newZkClient) {
		if (client == newZkClient) {
			return;
		} else {
			if (newZkClient.isAvailable()) {
				this.client = newZkClient; // 切换到新的
				datas = getChildrenData(getData());
				client.subscribeStateChanges(stateListener);
				client.subscribeChildChanges(path, pathListener);
				if (watchData) {
					client.subscribeDataChanges(path, dataListener);
				}
			}
		}
	}
	/*
	 * 
	 */
	public void start() {
		datas = getChildrenData(getData());
		
		client.subscribeStateChanges(stateListener);
		client.subscribeChildChanges(path, pathListener );
		if(watchData)client.subscribeDataChanges(path, dataListener );
		
		Stat stat = getPathVersion(path);
		setPathVersion(stat, System.currentTimeMillis());//给version赋值
	}


    private void setPathVersion(Stat stat, Long timestamp) {

        this.pathDataVersion = stat.getVersion();
        this.pathChildVersion = stat.getCversion();
        this.updateTime = timestamp;


    }

    /*
     *取得远程版本号 有失败可能
     */
	private Stat getPathVersion(String path){
		Stat stat = null;
		try {
			stat = client.getVersion(path);
		} catch (Throwable e) {
			logger.error("Error when getting version for path {}",path);
			logger.error(e.getMessage(),e);
			throw new RuntimeException(e);
		}
		return stat;
	}
	
	

	/*
	 * 在此完成通知
	 * 如通知失败，将通过versionChecker定时来进行重试
	 *
	 */
	void tagNotify(Event event) {
		// process node 在此生成相应map
		Map<String, Byte[]> nodeWithData  = null;
		Map<String, Byte[]> oldNodeData = this.datas;
		try {
			if(!client.exists(path)){
				logger.error("path {} not exist!!",path);
				throw new RuntimeException("Path not exist!");
			}
		} catch (Exception e1) {
			logger.error(e1.getMessage(),e1);
		}
		
		Stat stat = getPathVersion(path);
		Long timestamp = System.currentTimeMillis();
		
		switch (event.getEventType()) {
		case dataChanged:
            List<PathCacheListener> listeners = listenerMap
                    .get(ChildrenEvent.nodeChanged);
            for (PathCacheListener listener : listeners) {
                try {
                    List<PathNode> nodeLists_ = toNodeList(nodeWithData);
                    if((nodeLists_ == null || nodeLists_.size() == 0) && this.path.endsWith(Constants.PROVIDERS_CATEGORY)){
                        logger.error("nodeChanged, node list is null or size is zero ! parent path: "+ path);
                    }
                    listener.processEvent(path, nodeLists_);
                } catch (Throwable e) {
                    logger.error("Error when handle nodeChanged-pathCache",e);
                    throw new RuntimeException("error when handle tagnotify!");
                }
            }
            break;
		case nodeDeleted:
			List<String> nodes = getData();
//			nodeWithData = getChildrenData(nodes);
            List<PathCacheListener> deleteListeners = listenerMap
                    .get(ChildrenEvent.nodeDeleted);
            if (deleteListeners.size() > 0 ) {
//			for (String node : offlineNode) {
//				offlineNodeMap.put(node, oldNodeData.get(node));
//			}
                for (PathCacheListener deleteListener : deleteListeners) {
                    try {
                        deleteListener.processEvent(path, null);
                    } catch (Throwable e) {
                        logger.error("Error when handle deleteListener-pathCache",e);
                        throw new RuntimeException("error when handle tagnotify!");
                    }
                }
            }
			break;
		case childrenChanged:
//			nodeWithData = getChildrenData(event.getChildrens());
            List<PathCacheListener> addListeners = listenerMap
                    .get(ChildrenEvent.nodeAdded);
            if (addListeners.size() > 0) {
//			for (String node : onlineNode) {
//				onlineNodeMap.put(node, nodeWithData.get(node));
//			}
                for (PathCacheListener addListener : addListeners) {
                    try {
                        addListener.processEvent(path, null);
                    } catch (Throwable e3) {
                        logger.error("Error when handle addListener-pathCache",e3);
                        throw new RuntimeException("error when handle tagnotify!");
                    }
                }
            }
			break;
		default:
			logger.warn("No event for:" + event.getEventType());
			break;
		}
		setPathVersion(stat,timestamp);//给version赋值
		this.datas = nodeWithData;
	}
	

	public void addNodeChangedListener(PathCacheListener listener) {
		List<PathCacheListener> listeners = listenerMap
				.get(ChildrenEvent.nodeChanged);
		listeners.add(listener);
	}

	public void addNodeDeletedListener(PathCacheListener listener) {
		List<PathCacheListener> listeners = listenerMap
				.get(ChildrenEvent.nodeDeleted);
		listeners.add(listener);
	}

	public void addNodeAddedListener(PathCacheListener listener) {
		List<PathCacheListener> listeners = listenerMap
				.get(ChildrenEvent.nodeAdded);
		listeners.add(listener);
	}

	/*
	 * 取所有的子节点及其数据
	 */
	List<String> getData() {
		List<String> strs = null;
		try {
			strs = client.watchForChilds(path,false);// 直接取，不走watch
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("get Zookeeper Data RuntimeException Path:"+path);
		}
		
		return strs;

	}

	/*
	 * 把相应的data map转换为Node List
	 */
	private List<PathNode> toNodeList(Map<String, Byte[]> data) {
		List<PathNode> nodes = new ArrayList<PathNode>(); 
		Set<String> strKeys = data.keySet();
		for (String key : strKeys) {
			PathNode tempNode = new PathNode(key, data.get(key));
			nodes.add(tempNode);
		}
		return nodes;
	}
	
	/*
	 * 把缓存的数据转化为标准nodeList
	 */
	public List<PathNode> getCurrentData() {
		return toNodeList(this.datas);
	}

	/*
	 * get child data 从provider节点上读取配置值
	 */
    private Map<String, Byte[]> getChildrenData(List<String> childrens) {

        final Map<String, Byte[]> tempMap = new ConcurrentHashMap<String, Byte[]>();
        Map<String, String> keyMap = new ConcurrentHashMap<String, String>();

        if (childrens == null  && path.endsWith(Constants.PROVIDERS_CATEGORY)) {
            logger.warn("Path do NOT exist or No children node . path: {}", path);
            throw new RuntimeException(" Dirctory can not be empty...:Path:" + path);

        }

        //在此读取provider节点上的数据并处理：path
        if (path.endsWith(Constants.PROVIDERS_CATEGORY)) {
            byte[] dataByte = client.readData(path, true);
            for (final String node : childrens) {
                final String childPath = path + "/" + node;
                byte[] initData = new byte[]{1};
                Byte[] dataB = toByteArray(initData);
                tempMap.put(node, dataB);

                try {
                    URL url = urlCache.get(node);
                    String key2 = url.getIp() + ":" + url.getPort() + ":" + url.getParameter(Constants.GROUP_KEY, " ") + ":" + url.getParameter(Constants.VERSION_KEY, " ");
                    keyMap.put(key2, node);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);//只纪录，不影响正常运行
                }
            }
            handleNodeData(tempMap, keyMap, dataByte);
        }
        return tempMap;
    }

	/**
	 * @param tempMap
	 * @param keyMap
	 * @param dataByte
	 */
	private void handleNodeData(final Map<String, Byte[]> tempMap,
			Map<String, String> keyMap, byte[] dataByte) {
		if(dataByte != null && dataByte.length > 1){
			String nodesStr = null;
			nodesStr = new String(dataByte,Charset.forName("utf-8"));
			if(nodesStr == null || nodesStr.trim().length() == 0){
				logger.info("此节点没有下线节点值配置: "+path);
			}else{
				String[] nodes = nodesStr.split(";");
				for(String nodeValue : nodes){
					String[] params = nodeValue.split(":");//IP: port:group:version;IP: port:group:version;如group verison为空则空白处理" "（一个空格）
					if(params.length < 4 || params.length > 4){
						logger.error("node内容不对，数组长度不为4 请检查:"+nodeValue);
						continue;
					}
					
					try {
						String keyIns = StringUtils.join(params, ":");
						String nodeStr = keyMap.get(keyIns);
						if (nodeStr == null) {
							continue;
						}
						URL nodeUrl = urlCache.get(nodeStr);
						if(compareUrl(params,nodeUrl)){
							Byte[] offLine = new Byte[]{0};
							tempMap.put(nodeStr,offLine);
						}
					} catch (Exception e) {
						logger.error("SAF error when handle node value :"+nodeValue);
						logger.error(e.getMessage(),e);//只纪录，不影响正常运行
						
					}
					
				}
			}
		}
	}
	

	//缓存URL对象，线程安全
	LoadingCache<String, URL> urlCache = CacheBuilder.newBuilder()
		       .maximumSize(10000)
		       .expireAfterWrite(10, TimeUnit.HOURS)
		       //.removalListener(MY_LISTENER)
		       .build(
		           new CacheLoader<String, URL>() {
		             public URL load(String node) throws Exception {
		            	 URL temp = URL.valueOf(URL.decode(node));
		               return temp;
		             }
		           });
	
	/*
	 * 比较IP地址等值是否相等
	 * 
	 */
	private boolean compareUrl(String[] params, URL url) {
		String ip = url.getIp();
		String port = Integer.toString(url.getPort());
		String group = url.getParameter(Constants.GROUP_KEY, " ");
		String version = url.getParameter(Constants.VERSION_KEY, " ");
		return params[0].equals(ip) && params[1].equals(port) && params[2].equals(group) && params[3].equals(version);
	}

	
	
	private Byte[] toByteArray(byte[] data){
		
		if(data == null||data.length == 0){
			return new Byte[]{ };//默认值
			
		}
		Byte[] dataB = new Byte[]{};
		List<Byte> b1 = new ArrayList<Byte>();
		for(int i= 0;i<data.length;i++){
			b1.add(data[i]);
		}
		dataB = b1.toArray(dataB);
		return dataB;
	}

	/*
	 * 计算不同的Map
	 */
	private static Map<String, Set<String>> diffMap(Map<String, Byte[]> oldNodeData,
			Map<String, Byte[]> nodeWithData) {
		Set<String> oldChildNode = oldNodeData.keySet();
		Set<String> newChildNode = nodeWithData.keySet();
		SetView<String> view1 = Sets.difference(oldChildNode, newChildNode);// 下线的
		SetView<String> view2 = Sets.difference(newChildNode, oldChildNode);// 上线的
		Map<String, Set<String>> resultMap = new ConcurrentHashMap<String, Set<String>>();
		Set<String> offlineSet = new HashSet<String>();
		Set<String> onlineSet = new HashSet<String>();
		long before = System.currentTimeMillis();
		offlineSet = view1.copyInto(offlineSet);
		onlineSet = view2.copyInto(onlineSet);
		long after = System.currentTimeMillis();
		
		//Long[] longList = new Long[]{after-before,new Long(onlineSet.size()),new Long(offlineSet.size())};
		//logger.debug("SAF diff set spend time:{},online node MAP size:{} offlien node MAP size:{}",longList);
		resultMap.put(PathCache.OFFLINESET, offlineSet);
		resultMap.put(PathCache.ONLINESET, onlineSet);
		return resultMap;

	}
	


	public void close() {
		
		logger.warn("PathCache for Path:{} begin to close!", path);
		VersionChecker.removeVerionCheck(this);
	}
	/*
	 * 李鑫 2013-04-28 context 关闭时才执行这个
	 */
	public static void destroyFetcher(){
		//
		logger.warn("release dataFetcher pool Resources!");
		VersionChecker.close();
	}

	enum ChildrenEvent {

		nodeChanged(0), nodeDeleted(1), nodeAdded(2);

		private final int value;

		/**
		 * 采用自定义属性的构造方法
		 */
		ChildrenEvent(int value) {
			this.value = value;
		}

		/**
		 * 得到对应值
		 * 
		 * @return the value
		 */
		public int getValue() {
			return value;
		}

	}

}

enum EventType{
	childrenChanged,
	nodeDeleted,
	dataChanged;	
}

class Event{
	
	private EventType eventType;
	private String path;
	private List<String> childrens;
	
	public EventType getEventType() {
		return eventType;
	}
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public List<String> getChildrens() {
		return childrens;
	}
	public void setChildrens(List<String> childrens) {
		this.childrens = childrens;
	}
}
