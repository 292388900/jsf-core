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

import com.alibaba.fastjson.JSON;

public class MapListDiff {
    private Map<String, List<Integer>> onlyOnLeft;
    private Map<String, List<Integer>> onlyOnRight;
    private Map<String, List<Integer>> onBoth;
    
    public MapListDiff(Map<String, List<Integer>> left, Map<String, List<Integer>> right) {
        boolean switched = false;
        if(left.size() < right.size()){ // 做优化，比较大小，只遍历少的
            Map<String, List<Integer>> tmp = left;
            left = right;
            right = tmp;
            switched = true;
        }

        Map<String, List<Integer>> onlyOnLeftMap = new HashMap<String, List<Integer>>();
        Map<String, List<Integer>> onlyOnRightMap = new HashMap<String, List<Integer>>(right);
        Map<String, List<Integer>> onBothMap = new HashMap<String, List<Integer>>();

        for (Map.Entry<String, List<Integer>> entry : left.entrySet()) {
            String leftKey = entry.getKey();
            if (right.containsKey(leftKey)) {
                List<Integer> leftValueList = entry.getValue();
                List<Integer> rightValueList = onlyOnRightMap.get(leftKey);
                ListDiff listDiff = new ListDiff(leftValueList, rightValueList);
                List<Integer> onlyOnleftValueList = listDiff.onlyOnLeft;
                List<Integer> onlyOnRightValueList = listDiff.onlyOnRight;
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
    public Map<String, List<Integer>> getOnlyOnLeft() {
        return onlyOnLeft;
    }

    /**
     * @param onlyOnLeft the onlyOnLeft to set
     */
    public void setOnlyOnLeft(Map<String, List<Integer>> onlyOnLeft) {
        this.onlyOnLeft = onlyOnLeft;
    }

    /**
     * @return the onlyOnRight
     */
    public Map<String, List<Integer>> getOnlyOnRight() {
        return onlyOnRight;
    }

    /**
     * @param onlyOnRight the onlyOnRight to set
     */
    public void setOnlyOnRight(Map<String, List<Integer>> onlyOnRight) {
        this.onlyOnRight = onlyOnRight;
    }

    /**
     * @return the onBoth
     */
    public Map<String, List<Integer>> getOnBoth() {
        return onBoth;
    }

    /**
     * @param onBoth the onBoth to set
     */
    public void setOnBoth(Map<String, List<Integer>> onBoth) {
        this.onBoth = onBoth;
    }

    class ListDiff {
        private List<Integer> onlyOnLeft;
        private List<Integer> onlyOnRight;
        private List<Integer> onBoth;
        
        

        public ListDiff(List<Integer> left, List<Integer> right) {
            boolean switched = false;
            if(left.size() < right.size()){ // 做优化，比较大小，只遍历少的
                List<Integer> tmp = left;
                left = right;
                right = tmp;
                switched = true;
            }

            List<Integer> onlyOnLeftList = new ArrayList<Integer>();
            List<Integer> onlyOnRightList = new ArrayList<Integer>(right);
            List<Integer> onBothList = new ArrayList<Integer>();

            for (Integer leftId : left) {
                if (right.contains(leftId)) {
                    onBothList.add(leftId);
                    onlyOnLeftList.remove(leftId);
                    onlyOnRightList.remove(leftId);
                } else {
                    onlyOnLeftList.add(leftId);
                }
            }
            this.onlyOnLeft = switched ? onlyOnRightList : onlyOnLeftList;
            this.onlyOnRight = switched ? onlyOnLeftList : onlyOnRightList;
            this.onBoth = onBothList;
        }
    }

    public static void main(String[] args) {

        Map<String, List<Integer>> newMap = new HashMap<String, List<Integer>>();
        newMap.put("aaa", new ArrayList<Integer>());
        newMap.get("aaa").add(1);
        newMap.get("aaa").add(122222);
        newMap.get("aaa").add(3333333);
        newMap.get("aaa").add(555555);
        newMap.put("bbb", new ArrayList<Integer>());
        newMap.get("bbb").add(111);
        newMap.get("bbb").add(222);
        newMap.get("bbb").add(333);
        newMap.get("bbb").add(444);
        newMap.put("ccc", new ArrayList<Integer>());
        newMap.get("ccc").add(123333333);
        newMap.get("ccc").add(456666666);
        Map<String, List<Integer>> oldMap = new HashMap<String, List<Integer>>();
        oldMap.put("aaa", new ArrayList<Integer>());
        oldMap.get("aaa").add(66666666);
        oldMap.get("aaa").add(122222);
        oldMap.get("aaa").add(3333333);
        oldMap.put("bbb1", new ArrayList<Integer>());
        oldMap.get("bbb1").add(111);
        oldMap.get("bbb1").add(222);
        oldMap.get("bbb1").add(333);
        oldMap.get("bbb1").add(444);
        oldMap.put("ccc", new ArrayList<Integer>());
        oldMap.get("ccc").add(12333344);
        oldMap.get("ccc").add(456666666);
        MapListDiff t = new MapListDiff(newMap, oldMap);
        System.out.println("new :" + JSON.toJSONString(t.getOnlyOnLeft()));
        System.out.println("old :" + JSON.toJSONString(t.getOnlyOnRight()));
        System.out.println("both :" + JSON.toJSONString(t.getOnBoth()));

    }

}
