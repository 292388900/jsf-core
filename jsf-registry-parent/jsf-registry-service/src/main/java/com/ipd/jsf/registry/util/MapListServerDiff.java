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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.ipd.jsf.registry.domain.Server;

public class MapListServerDiff {
    private Map<String, List<Server>> onlyOnLeft;
    private Map<String, List<Server>> onlyOnRight;
    private Map<String, List<Server>> onBoth;
    
    public MapListServerDiff(Map<String, List<Server>> left, Map<String, List<Server>> right) {
        boolean switched = false;
        if(left.size() < right.size()){ // 做优化，比较大小，只遍历少的
            Map<String, List<Server>> tmp = left;
            left = right;
            right = tmp;
            switched = true;
        }

        Map<String, List<Server>> onlyOnLeftMap = new HashMap<String, List<Server>>();
        Map<String, List<Server>> onlyOnRightMap = new HashMap<String, List<Server>>(right);
        Map<String, List<Server>> onBothMap = new HashMap<String, List<Server>>();

        for (Map.Entry<String, List<Server>> entry : left.entrySet()) {
            String leftKey = entry.getKey();
            if (right.containsKey(leftKey)) {
                List<Server> leftValueList = entry.getValue();
                List<Server> rightValueList = onlyOnRightMap.get(leftKey);
                ListServerDiff listDiff = new ListServerDiff(leftValueList, rightValueList);
                List<Server> onlyOnleftValueList = listDiff.onlyOnLeft;
                List<Server> onlyOnRightValueList = listDiff.onlyOnRight;
                onlyOnLeftMap.put(leftKey, onlyOnleftValueList);
                onlyOnRightMap.put(leftKey, onlyOnRightValueList);
                if (!listDiff.onBoth.isEmpty()) {
                    onBothMap.put(leftKey, listDiff.onBoth);
                }
            } else {
                onlyOnLeftMap.put(leftKey, entry.getValue());
            }
        }
        this.onlyOnLeft = switched ? onlyOnRightMap : onlyOnLeftMap;
        this.onlyOnRight = switched ? onlyOnLeftMap : onlyOnRightMap;
        this.onBoth = onBothMap;
    }

    /**
     * @return the onlyOnLeft
     */
    public Map<String, List<Server>> getOnlyOnLeft() {
        return onlyOnLeft;
    }

    /**
     * @param onlyOnLeft the onlyOnLeft to set
     */
    public void setOnlyOnLeft(Map<String, List<Server>> onlyOnLeft) {
        this.onlyOnLeft = onlyOnLeft;
    }

    /**
     * @return the onlyOnRight
     */
    public Map<String, List<Server>> getOnlyOnRight() {
        return onlyOnRight;
    }

    /**
     * @param onlyOnRight the onlyOnRight to set
     */
    public void setOnlyOnRight(Map<String, List<Server>> onlyOnRight) {
        this.onlyOnRight = onlyOnRight;
    }

    /**
     * @return the onBoth
     */
    public Map<String, List<Server>> getOnBoth() {
        return onBoth;
    }

    /**
     * @param onBoth the onBoth to set
     */
    public void setOnBoth(Map<String, List<Server>> onBoth) {
        this.onBoth = onBoth;
    }

    class ListServerDiff {
        private List<Server> onlyOnLeft;
        private List<Server> onlyOnRight;
        private List<Server> onBoth;

        public ListServerDiff(List<Server> leftList, List<Server> rightList) {
            List<Server> onlyOnLeftList = new ArrayList<Server>();
            List<Server> onlyOnRightList = new ArrayList<Server>(rightList);
            List<Server> onBothList = new ArrayList<Server>();

            for (Server left : leftList) {
            	Server right = findServer(left.getUniqKey(), rightList);
            	if (right != null) {
            		onBothList.add(left);
            		//权重不一样，要做删除和新增的通知, 所以onlyLeftList中要添加server
            		if (left.getWeight() != right.getWeight()) {
            			onlyOnLeftList.add(left);
            		} else {  //如果权重一样，就从左右list中去掉
            			onlyOnLeftList.remove(left);
            			onlyOnRightList.remove(left);
            		}
                } else {
                    onlyOnLeftList.add(left);
                }
            }
            this.onlyOnLeft = onlyOnLeftList;
            this.onlyOnRight = onlyOnRightList;
            this.onBoth = onBothList;
        }

        private Server findServer(String uniqKey, List<Server> serverList) {
        	if (!CollectionUtils.isEmpty(serverList)) {
        		for (Server server : serverList) {
        			if (server != null && server.getUniqKey().equals(uniqKey)) {
        				return server;
        			}
        		}
        	}
        	return null;
        }
    }

    public static void main(String[] args) {

        Map<String, List<Server>> newMap = new HashMap<String, List<Server>>();
        newMap.put("aaa", new ArrayList<Server>());
        Server server = new Server();
        server.setUniqKey("1");
        newMap.get("aaa").add(server);
        server = new Server();
        server.setUniqKey("122222");
        newMap.get("aaa").add(server);
        server = new Server();
        server.setUniqKey("3333333");
        server.setWeight(100);
        newMap.get("aaa").add(server);
        server = new Server();
        server.setUniqKey("555555");
        newMap.get("aaa").add(server);
        
        newMap.put("bbb", new ArrayList<Server>());
        server = new Server();
        server.setUniqKey("111");
        newMap.get("bbb").add(server);
        server = new Server();
        server.setUniqKey("222");
        newMap.get("bbb").add(server);
        server = new Server();
        server.setUniqKey("333");
        newMap.get("bbb").add(server);
        server = new Server();
        server.setUniqKey("444");
        newMap.get("bbb").add(server);
        
        newMap.put("ccc", new ArrayList<Server>());
        server = new Server();
        server.setUniqKey("123333333");
        newMap.get("ccc").add(server);
        server = new Server();
        server.setUniqKey("456666666");
        newMap.get("ccc").add(server);
        
        Map<String, List<Server>> oldMap = new HashMap<String, List<Server>>();
        oldMap.put("aaa", new ArrayList<Server>());
        server = new Server();
        server.setUniqKey("66666666");
        oldMap.get("aaa").add(server);
        server = new Server();
        server.setUniqKey("122222");
        oldMap.get("aaa").add(server);
        server = new Server();
        server.setUniqKey("3333333");
        server.setWeight(101);
        oldMap.get("aaa").add(server);
        
        oldMap.put("bbb", new ArrayList<Server>());
        server = new Server();
        server.setUniqKey("111");
        oldMap.get("bbb").add(server);
        server = new Server();
        server.setUniqKey("222");
        oldMap.get("bbb").add(server);
        server = new Server();
        server.setUniqKey("333");
        oldMap.get("bbb").add(server);
        server = new Server();
        server.setUniqKey("444");
        oldMap.get("bbb").add(server);
        
        oldMap.put("ccc", new ArrayList<Server>());
        server = new Server();
        server.setUniqKey("12333344");
        oldMap.get("ccc").add(server);
        server = new Server();
        server.setUniqKey("456666666");
        oldMap.get("ccc").add(server);
        MapListServerDiff t = new MapListServerDiff(newMap, oldMap);
        System.out.println("only left :" + JSON.toJSONString(t.getOnlyOnLeft()));
        System.out.println("only right :" + JSON.toJSONString(t.getOnlyOnRight()));
        System.out.println("both :" + JSON.toJSONString(t.getOnBoth()));

    }

}
