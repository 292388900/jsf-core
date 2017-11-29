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

public class BroadCastSend {
    /**
     * @param args
     * @throws UException
     */
    public static void main(String[] args) throws Exception {
        int port = 6789;
        String sendMessage = "adbzz";
        InetAddress inetAddress = InetAddress.getByName("228.5.6.255");
        DatagramPacket datagramPacket = new DatagramPacket(sendMessage.getBytes(), sendMessage.length(), inetAddress, port);
        MulticastSocket multicastSocket = new MulticastSocket();
        multicastSocket.send(datagramPacket);

    }
}
