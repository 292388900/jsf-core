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

package com.ipd.jsf.common.util;

import java.io.*;
import java.net.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafTelnetClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(SafTelnetClient.class);
    /**
     * socket函数
     */
    private Socket socket;

    /**
     * 换行符
     */
    private final static String LINE_SEPARATOR = System.getProperty("line.separator");
    /**
     * 默认读字符编码
     */
    private String readEncoding = "UTF-8";

    /**
     * 默认写字符编码
     */
    private String writeEncoding = "UTF-8";

    public SafTelnetClient() {
    }

    /**
     * DubboTelnetClient 构造
     *
     * @param ip   ip地址
     * @param port 端口
     * @throws UnknownHostException
     * @throws IOException
     */
    public SafTelnetClient(String ip, int port) throws UnknownHostException,
            IOException {
        this.socket = new Socket(ip, port);
    }

    /**
     * DubboTelnetClient 构造
     *
     * @param ip          ip地址
     * @param port        端口
     * @param connTimeout 连接超时时间超时时间
     * @throws UnknownHostException
     * @throws IOException
     */
    public SafTelnetClient(String ip, int port, int connTimeout)
            throws UnknownHostException, IOException {
        this(ip, port, connTimeout, 5000);
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    /**
     * DubboTelnetClient 构造
     *
     * @param ip          ip地址
     * @param port        端口
     * @param connTimeout 连接超时时间
     * @param soTimeout   读取超时时间
     * @throws UnknownHostException
     * @throws IOException
     */
    public SafTelnetClient(String ip, int port, int connTimeout, int soTimeout)
            throws UnknownHostException, IOException {
        try {
            this.socket = new Socket();
            socket.setSoTimeout(soTimeout);
            socket.connect(new InetSocketAddress(ip, port), connTimeout);
        } catch (Exception e) {
            LOGGER.error("连接" + ip + ":" + port + "异常" + e.getMessage(), e);
        }
    }

    public String[] init(String ip, int port, int connTimeout, int soTimeout) {
        String [] result = new String[2];
        result[0] = "0";
        try {
            this.socket = new Socket();
            socket.setSoTimeout(soTimeout);
            socket.connect(new InetSocketAddress(ip, port), connTimeout);
        } catch (Exception e) {
            result[0] = "1";
            if (e instanceof UnknownHostException) {
                result[1] = "未知主机异常";
            } else if (e instanceof SocketTimeoutException) {
                result[1] = "连接主机超时异常";
            } else if (e instanceof ConnectException) {
                result[1] = "连接主机拒绝异常";
            } else {
                result[1] = e.getMessage();
            }
            LOGGER.error("连接" + ip + ":" + port + "异常" + e.getMessage(), e);
        }
        return result;
    }

    /**
     * 发送消息
     *
     * @param msg   消息
     * @param times 读取次数（即遇到dubbo>结尾代表一次）
     * @return 返回消息
     * @throws IOException
     */
    public String send(String msg, int times) throws IOException {
        String result = "";
        InputStream in = null;
        OutputStream out = null;
        ByteArrayOutputStream baout = null;
        try {
            // 初始化通道
            in = socket.getInputStream();
            out = socket.getOutputStream();
            // 发送请求
            out.write(msg.getBytes(writeEncoding));
            out.write(LINE_SEPARATOR.getBytes(writeEncoding));
            out.flush();

            baout = new ByteArrayOutputStream();
            // 解析得到的响应
            StringBuffer sb = new StringBuffer();
            byte[] bs = new byte[1024];
            int len = 0;
            int i = 0;
            while (i < 1024 && (len = in.read(bs)) != -1) {
                //if (i > 1024) { // 防止无限循环 最多取 1M的数据
                //	break;
                //}
                String data = new String(bs, 0, len, readEncoding);
                baout.write(bs, 0, len);
                sb.append(data);

                String last = sb.substring(sb.length() - 6);
                if (last.endsWith("jsf>") || "dubbo>".equals(last) || "lnet> ".equals(last)) {
                    if (--times < 1) {
                        break; // 读到这个就断开连接返回
                    }
                }
                i++;
            }
            result = new String(baout.toByteArray(), readEncoding);
            return result;
        } catch (Exception e) {
            LOGGER.error("执行socket命令" + msg + "结束, 结果为" + e.getMessage(), e);
        } finally {
            if (baout != null) {
                baout.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        }
        return result;
    }

    /**
     * 发送消息
     *
     * @param msg   消息
     * @param times 读取次数（即遇到dubbo>结尾代表一次）
     * @return 返回消息
     * @throws IOException
     */
    public String[] sendMessage(String msg, int times) throws IOException {
        String [] result = new String[2];
        InputStream in = null;
        OutputStream out = null;
        ByteArrayOutputStream baout = null;
        result[0] = "0";
        try {
            // 初始化通道
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                result[0] = "1";
                result[1] = "获取Socket的输入输出流异常";
                LOGGER.error("执行socket命令" + msg + "结束, 结果为" + e.getMessage(), e);
                return result;
            }

            // 发送请求
            byte [] output = null;
            try {
                output = msg.getBytes(writeEncoding);
            } catch (UnsupportedEncodingException e) {
                result[0] = "1";
                result[1] = "解析编码输入参数异常,编码UTF-8";
                LOGGER.error("执行socket命令" + msg + "结束, 结果为" + e.getMessage(), e);
                return result;
            }
            try {
                out.write(output);
            } catch (IOException e) {
                result[0] = "1";
                result[1] = "发送输入参数异常,编码UTF-8";
                LOGGER.error("执行socket命令" + msg + "结束, 结果为" + e.getMessage(), e);
                return result;
            }
            try {
                output = LINE_SEPARATOR.getBytes(writeEncoding);
            } catch (UnsupportedEncodingException e) {
                result[0] = "1";
                result[1] = "解析编码输入参数异常,编码UTF-8";
                LOGGER.error("执行socket命令" + msg + "结束, 结果为" + e.getMessage(), e);
                return result;
            }
            try {
                out.write(output);
                out.flush();
            } catch (IOException e) {
                result[0] = "1";
                result[1] = "发送输入参数异常,编码UTF-8";
                LOGGER.error("执行socket命令" + msg + "结束, 结果为" + e.getMessage(), e);
                return result;
            }

            baout = new ByteArrayOutputStream();
            // 解析得到的响应
            StringBuffer sb = new StringBuffer();
            byte[] bs = new byte[1024];
            int len = 0;
            int i = 0;
            try {
                while (i < 1024 && (len = in.read(bs)) != -1) {
                    //if (i > 1024) { // 防止无限循环 最多取 1M的数据
                    //	break;
                    //}
                    String data = new String(bs, 0, len, readEncoding);
                    baout.write(bs, 0, len);
                    sb.append(data);

                    String last = sb.substring(sb.length() - 6);
                    if (last.endsWith("jsf>") || "dubbo>".equals(last) || "lnet> ".equals(last)) {
                        if (--times < 1) {
                            break; // 读到这个就断开连接返回
                        }
                    }
                    i++;
                }
            } catch (IOException e) {
                result[0] = "1";
                if (e instanceof UnsupportedEncodingException) {
                    result[1] = "解析编码返回结果异常,编码UTF-8";
                } else if (e instanceof SocketTimeoutException) {
                    result[1] = "从服务端读取返回结果超时异常";
                } else {
                    result[1] = "从服务端读取返回结果异常";
                }
                return result;
            }
            try {
                result[1] = new String(baout.toByteArray(), readEncoding);
            } catch (UnsupportedEncodingException e) {
                result[0] = "1";
                result[1] = "解析编码返回结果异常,编码UTF-8";
                return result;
            }
            return result;
        } catch (Exception e) {
            result[0] = "1";
            if (e instanceof SocketTimeoutException) {
                result[1] = "读取返回结果超时异常,编码UTF-8";
            } else {
                result[1] = e.getMessage();
            }
            LOGGER.error("执行socket命令" + msg + "结束, 结果为" + e.getMessage(), e);
        } finally {
            if (baout != null) {
                baout.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        }
        return result;
    }

    /**
     * 发送消息
     *
     * @param msgArray   消息
     * @param times 读取次数（即遇到dubbo>结尾代表一次）
     * @return 返回消息
     * @throws IOException
     */
    public String[] send(String msgArray[], int times) throws IOException {
        String result[] = new String[msgArray.length];
        InputStream in = null;
        OutputStream out = null;
        ByteArrayOutputStream baout = null;
        try {
            // 初始化通道
            in = socket.getInputStream();
            out = socket.getOutputStream();
            int tempTimes;
            for (int n = 0; n < msgArray.length; n++) {
                tempTimes = times;
                // 发送请求
                out.write(msgArray[n].getBytes(writeEncoding));
                out.write(LINE_SEPARATOR.getBytes(writeEncoding));
                out.flush();

                baout = new ByteArrayOutputStream();
                // 解析得到的响应
                StringBuffer sb = new StringBuffer();
                byte[] bs = new byte[1024];
                int len = 0;
                int i = 0;
                while (i < 1024 && (len = in.read(bs)) != -1) {
                    //if (i > 1024) { // 防止无限循环 最多取 1M的数据
                    //  break;
                    //}
                    String data = new String(bs, 0, len, readEncoding);
                    baout.write(bs, 0, len);
                    sb.append(data);

                    String last = sb.substring(sb.length() - 6);
                    if (last.endsWith("jsf>") || "dubbo>".equals(last) || "lnet> ".equals(last)) {
                        if (--tempTimes < 1) {
                            break; // 读到这个就断开连接返回
                        }
                    }
                    i++;
                }
                result[n] = new String(baout.toByteArray(), readEncoding);
            }
            return result;
        } catch (Exception e) {
            LOGGER.error("执行socket命令" + msgArray + "结束, 结果为" + e.getMessage(), e);
        } finally {
            if (baout != null) {
                baout.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        }
        return result;
    }

    /**
     * 发送消息
     *
     * @param msg 消息
     * @return 返回消息
     * @throws IOException
     */
    public String send(String msg) throws IOException {
        int time = 1;
        if (msg.startsWith("count")) {
            time = 2; // count命令会返回2次dubbo
        } else if (msg.startsWith("trace")) {
            String[] cmds = msg.split("\\s+"); // trace几次返回几次
            time = cmds.length > 2 ? Integer.valueOf(cmds[cmds.length - 1].trim()) : 1;
        }
        return send(msg, time);
    }

    public String[] sendMessage(String msg) throws IOException {
        int time = 1;
        if (msg.startsWith("count")) {
            time = 2; // count命令会返回2次dubbo
        } else if (msg.startsWith("trace")) {
            String[] cmds = msg.split("\\s+"); // trace几次返回几次
            time = cmds.length > 2 ? Integer.valueOf(cmds[cmds.length - 1].trim()) : 1;
        }
        return sendMessage(msg, time);
    }

    /**
     * 发送消息
     *
     * @param msg 消息
     * @return 返回消息
     * @throws IOException
     * @deprecated use {@link #send(String)}
     */
    public String sendOld(String msg) throws IOException {
        String result = "";
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            // 初始化通道
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 发送请求
            out.println(msg);
            out.flush();

            // 解析得到的响应
            StringBuffer sb = new StringBuffer();
            char[] cbuf = new char[10240];
            in.read(cbuf);
            /*try {
                Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
            sb.append(cbuf, 0, cbuf.length);
            result = sb.toString();
            return result;
        } finally {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        try {
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            // ignore
            // e.printStackTrace();
        }
    }

    /**
     * @return the readEncoding
     */
    public String getReadEncoding() {
        return readEncoding;
    }

    /**
     * @param readEncoding the readEncoding to set
     */
    public void setReadEncoding(String readEncoding) {
        this.readEncoding = readEncoding;
    }

    /**
     * @return the writeEncoding
     */
    public String getWriteEncoding() {
        return writeEncoding;
    }

    /**
     * @param writeEncoding the writeEncoding to set
     */
    public void setWriteEncoding(String writeEncoding) {
        this.writeEncoding = writeEncoding;
    }

    public static void main(String[] args) {
        String[] cmds = new String[]{"ls"};
//		String[] cmds = new String[] { "ls -l", "count com.ipd.testsaf.HelloService", "trace com.ipd.testsaf.HelloService"};
        SafTelnetClient client = null;
        long time0 = System.currentTimeMillis();
        try {
            for (int i = 0; i < cmds.length; i++) {
                client = new SafTelnetClient("1222.1212.12", 20880, 5000);
//				client = new DubboTelnetClient("10.12.120.121", 20880,5000);
                client.setReadEncoding("gbk");
                String res = client.send(cmds[i]);
                System.out.println("命令:" + cmds[i] + "   返回");
                System.out.println(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                client.close();
            }
        }
        long time1 = System.currentTimeMillis();
        System.out.println("耗时：" + (time1 - time0) + "ms");
    }
}
