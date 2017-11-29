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
package com.ipd.jsf.worker.service.test.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Test {

    /**
     * @param args
     */
    public static void main(String[] args) {
        List<Integer> list1 = new ArrayList<Integer>();
        list1.add(1);
        list1.add(57777);
        list1.add(888888800);
        list1.add(10000);
        list1.add(10000000);
        list1.add(499999);
        List<Integer> list2 = new ArrayList<Integer>();
        list2.add(1);
        list2.add(57777);
        list2.add(888888800);
        
        for (Integer value : list1) {
            if (list2.contains(value)) {
                System.out.println(value);
            }
        }
        
        
        List<Integer> list3 = new ArrayList<Integer>();
        list3.add(1);
        list3.add(57777);
        list3.add(888888800);
        list3.add(10000);
        list3.add(10000000);
        list3.add(10000);
        list3.add(499999);
        Set<Integer> set = new HashSet<Integer>(list3);
        System.out.println("111:"+set);
        
        System.out.println(new Date(1));
    }

}
