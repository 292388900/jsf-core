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

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestMapMultiThread {
    private static ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>();

    /**
     * @param args
     */
    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(100);
        while(true) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        int i = new Random().nextInt(10000);
                        map.put("abc" + i, String.valueOf(i));
                    }
                }
            });
            for (Map.Entry<String, String> entry : map.entrySet()) {
                try {
                    String value = entry.getKey() + ":" + entry.getValue();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
