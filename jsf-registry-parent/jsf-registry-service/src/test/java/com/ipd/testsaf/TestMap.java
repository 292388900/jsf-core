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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;


public class TestMap {

    public static Map<String, String> map = new HashMap<String, String>();
    /**
     * @param args
     */
    public static void main(String[] args) {
        int n = 2147483647;
        final Random r = new Random();
        Thread t = new Thread(new Runnable() {
            
            @Override
            public void run() {
                Random r = new Random();
                while(true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    synchronized (map) {
                        map.put("key" + r.nextInt(1000000), String.valueOf(r.nextInt(200000)));
                    }
                }
            }
        });
        t.start();
        
//        int i = 0;
//        while(i ++ < 10000) {
//            map.put("key" + r.nextInt(1000000), String.valueOf(r.nextInt(200000)));
//        }
        
        while (true) {
//            for (Map.Entry<String, String> entry : map.entrySet()) {
//                System.out.println(entry.getValue());
//            }
            Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                try {
                    Entry<String, String> entry = iterator.next();
                    System.out.println(entry.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
