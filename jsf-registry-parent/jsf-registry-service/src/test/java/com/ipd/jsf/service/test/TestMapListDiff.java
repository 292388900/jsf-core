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
package com.ipd.jsf.service.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.ipd.jsf.registry.util.MapListDiff;

public class TestMapListDiff {

    /**
     * @param args
     */
    public static void main(String[] args) {

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
        MapListDiff t = new MapListDiff(newMap, oldMap);
        System.out.println("new :" + JSON.toJSONString(t.getOnlyOnLeft()));
        System.out.println("old :" + JSON.toJSONString(t.getOnlyOnRight()));

    }

}
