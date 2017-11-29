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
package com.ipd.jsf.registry.service.test;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class BroadCastReciever {
    /**
     * @param args
     * @throws UnknownHostException
     */
    public static void main(String[] args) throws Exception {
        InetAddress group = InetAddress.getByName("228.5.6.7");
        MulticastSocket s = new MulticastSocket(6789);
        byte[] arb = new byte[1024];
        s.joinGroup(group);// 加入该组
        while (true) {
            DatagramPacket datagramPacket = new DatagramPacket(arb, arb.length);
            s.receive(datagramPacket);
            System.out.println(arb.length);
            System.out.println(new String(arb));
        }

    }
}
