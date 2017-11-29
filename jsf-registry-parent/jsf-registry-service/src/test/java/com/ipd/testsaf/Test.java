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
package com.ipd.testsaf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public class Test {

    /**
     * @param args
     */
    public static void main(String[] args) {
//        List<Server> list = new ArrayList<Server>();
//        Server server = new Server();
//        for (int i = 0; i < 20; i ++) {
//            server = new Server();
//            server.setId(i);
//            list.add(server);
//        }
//        
//        Iterator<Server> iterator = list.iterator();
//        while (iterator.hasNext()) {
//            Server s = iterator.next();
//            if (s.getId() == 5) {
//                iterator.remove();
//            }
//        }
//        
//        System.out.println("size:" + list.size());
        
        Test t = new Test();
        Map<String, List<Integer>> newMap = new HashMap<String, List<Integer>>();
        newMap.put("aaa", new ArrayList<Integer>());
        newMap.get("aaa").add(1);
//        newMap.get("aaa").add(122222);
//        newMap.get("aaa").add(3333333);
//        newMap.get("aaa").add(555555);
//        newMap.put("bbb", new ArrayList<Integer>());
//        newMap.get("bbb").add(111);
//        newMap.get("bbb").add(222);
//        newMap.get("bbb").add(333);
//        newMap.get("bbb").add(444);
        newMap.put("ccc", new ArrayList<Integer>());
        newMap.get("ccc").add(123333333);
        newMap.get("ccc").add(456666666);
        Map<String, List<Integer>> oldMap = new HashMap<String, List<Integer>>();
        oldMap.put("aaa", new ArrayList<Integer>());
//        oldMap.get("aaa").add(66666666);
//        oldMap.get("aaa").add(122222);
//        oldMap.get("aaa").add(3333333);
        oldMap.put("bbb1", new ArrayList<Integer>());
        oldMap.get("bbb1").add(111);
        oldMap.get("bbb1").add(222);
        oldMap.get("bbb1").add(333);
        oldMap.get("bbb1").add(444);
//        oldMap.put("ccc", new ArrayList<Integer>());
//        oldMap.get("ccc").add(12333344);
//        oldMap.get("ccc").add(456666666);
        
        t.MapListDiff(newMap, oldMap);
        System.out.println("new :" + JSON.toJSONString(t.onlyOnLeft));
        System.out.println("old :" + JSON.toJSONString(t.onlyOnRight));
    }

    public Map<String, List<Integer>> onlyOnLeft;
    public Map<String, List<Integer>> onlyOnRight;
    
    public void MapListDiff(Map<String, List<Integer>> left, Map<String, List<Integer>> right) {
        boolean switched = false;
        if(left.size() < right.size()){ // 做优化，比较大小，只遍历少的
            Map<String, List<Integer>> tmp = left;
            left = right;
            right = tmp;
            switched = true;
        }

        Map<String, List<Integer>> onlyOnLeft = new HashMap<String, List<Integer>>();
        Map<String, List<Integer>> onlyOnRight = new HashMap<String, List<Integer>>(right);

        for (Map.Entry<String, List<Integer>> entry : left.entrySet()) {
            String leftKey = entry.getKey();
            List<Integer> leftValueList = entry.getValue();
            if (right.containsKey(leftKey)) {
                List<Integer> rightValueList = onlyOnRight.get(leftKey);
                if (leftValueList != null && rightValueList != null) {
                    Iterator<Integer> leftIterator = leftValueList.iterator();
                    while (leftIterator.hasNext()) {
                        Integer value = leftIterator.next();
                        if (rightValueList.contains(value)) {
                            //删除掉共有的
                            rightValueList.remove(value);
                            leftIterator.remove();
                        }
                    }
                    if (rightValueList.size() == 0) {
                        onlyOnRight.remove(leftKey);
                    }
                    if (leftValueList.size() != 0) {
                        onlyOnLeft.put(leftKey, leftValueList);
                    }
                }
            } else {
                onlyOnLeft.put(leftKey, leftValueList);
            }
        }
        this.onlyOnLeft = switched ? onlyOnRight : onlyOnLeft;
        this.onlyOnRight = switched ? onlyOnLeft : onlyOnRight;
    }

}
