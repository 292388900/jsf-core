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
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestList {

    /**
     * @param args
     */
    public static void main(String[] args) {
        final List<Integer> list = new ArrayList<Integer>(52);
        
        ExecutorService service = Executors.newFixedThreadPool(50);
        
        int i = 0;
        while (i++ < 50) {
            service.execute(new Runnable() {
                
                @Override
                public void run() {
//                    while (true) {
//                        try {
//                            Thread.sleep(10);
//                        } catch (InterruptedException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
                        Random r = new Random();
                        list.add(r.nextInt(30));
//                    }
                }
            });
        }
        
        while(true) {
            System.out.println(list.size());
        }
    }

}
