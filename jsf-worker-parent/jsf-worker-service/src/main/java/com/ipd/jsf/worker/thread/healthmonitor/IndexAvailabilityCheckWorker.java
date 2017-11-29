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
package com.ipd.jsf.worker.thread.healthmonitor;

import com.ipd.jsf.common.util.PropertyFactory;
import com.ipd.jsf.common.util.SafTelnetClient;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JsonUtils;
import com.ipd.jsf.gd.util.StringUtils;
import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.util.WorkerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Title： <br>
 * Description：检查寻址服务<br>
 */
public class IndexAvailabilityCheckWorker extends SingleWorker {

    private static final Logger logger = LoggerFactory.getLogger(IndexAvailabilityCheckWorker.class);

    private static int connectTimes = 3;
    private static long alarmTime = System.currentTimeMillis();

    static {
        new PropertyFactory("check.properties");
    }

    @Override
    public void init() {
    }

    @Override
    public boolean run() {
        logger.info("run the worker of checking the availability of index server starting......");
        try {
            StringBuilder sb = new StringBuilder(256);
            sb.append("http://");
            sb.append((String) PropertyFactory.getProperty("indexAdress"));
            sb.append("/addrs?sv=").append(Constants.DEFAULT_SAF_VERSION);
            sb.append("&p=").append(Constants.REGISTRY_PROTOCOL_JSF).append("&lan=java");
            sb.append("&app=").append((String) PropertyFactory.getProperty("app.name"));
            sb.append("&appId=").append((String) PropertyFactory.getProperty("app.id"));

            String uri = sb.toString();
            logger.info("Get address from index, the url is {}", uri);

            String result = null;
            for (int i = 0; i < connectTimes; i++) { // 试connectTimes次
                try {
                    result = httpGet(uri);
                } catch (Exception e) {
                    //logger.error(i+",alarmTime:"+alarmTime.getTime()+",now:{},httpGet error ",System.currentTimeMillis(),e.getMessage());
                    if (i == connectTimes - 1 && isAlarm()) {
                        //报警
                        alarmTime = System.currentTimeMillis();
                        dealAlarm(null, uri, e);
                    }
                }
                if (result != null) {
                    break;
                }
                try {
                    Thread.sleep(Integer.valueOf((String) PropertyFactory.getProperty("interrupted.time")));
                } catch (InterruptedException e) {
                }
            }
            if (result == null) {
                return false;
            }
            // result为json格式，需要解析
            String registryAddress = null;
            try {
                Map map = JsonUtils.parseObject(result, Map.class);
                registryAddress = (String) map.get("address");
                if (!StringUtils.isEmpty(registryAddress)) {
                    logger.info("return registry address is:{}", registryAddress);
                    if (telnetConnect(registryAddress)) {
                        logger.info("telnet address from index server finish, and exists valid registry address.");
                    } else {
                        dealAlarm("no exists valid registry address from index server.", registryAddress, null);
                        return false;
                    }
                } else {
                    dealAlarm("Get address from index server null!", uri, null);
                    return false;
                }
            } catch (Exception e) {
                dealAlarm("Get illegal json result from index server!", uri, null);
            }
            logger.info("finish index availability check. return registry address is:{}", registryAddress);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * index服务是否需要报警：如果已在alarmIntervalTime内报过警，则不报警
     *
     * @return
     */
    private boolean isAlarm() {
        long nowTime = System.currentTimeMillis();
        String alarmIntervalTime = (String) PropertyFactory.getProperty("alarm.index.intervalTime");
        if (WorkerUtil.getDateUtil(nowTime, alarmTime) < Integer.parseInt(alarmIntervalTime)) {
            return false;
        }
        return true;
    }

    private boolean telnetConnect(String registryAddress) {
        boolean isConnect = false;
        String[] addressArray = registryAddress.split(",");
        for (String temp : addressArray) {
            String[] tempArray = temp.split(":");
            try {
                if (isAlive(tempArray[0], Integer.parseInt(tempArray[1]))) {
                    isConnect = true;
                    break;
                }
            } catch (Exception e) {
                logger.error(tempArray + "," + e.getMessage(), e);
            }
        }
        return isConnect;
    }

    private boolean isAlive(String ip, int port) {
        boolean alive = false;
        try {
            alive = doCheck(ip, port);
            if (!alive) {
                for (int i = 0; i < connectTimes; i++) {
                    alive = doCheck(ip, port);
                    if (alive) {
                        return true;
                    }
                    try {
                        Thread.sleep(Integer.valueOf((String) PropertyFactory.getProperty("interrupted.time")));
                    } catch (InterruptedException e) {
                    }
                }
            }
        } finally {
            return alive;
        }
    }

    private boolean doCheck(String ip, int port) {
        SafTelnetClient client = null;
        try {
            client = new SafTelnetClient(ip, port, 5000, 5000);
            if (client.isConnected()) {
                String string = client.send("ls");
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Object[] objects = new Object[]{ip, port};
            logger.error("连接{}:{}不通!", objects);
            return false;
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 报警
     *
     * @param message
     * @param uri
     * @param e
     */
    private void dealAlarm(String message, String uri, Exception e) {
        StringBuffer sbf = new StringBuffer("[JSF]index check:");
        if (message == null) {
            message = "Get address from index server error!";
        }
        sbf.append(message).append(" url is:[").append(uri).append("].");
        if (e != null) {
            sbf.append("Exception:[").append(e.getMessage()).append("].");
            sbf.append("from ip:").append(WorkerUtil.getWorkerIP()).append(",报警时间:").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
            logger.error("umpKey:" + (String) PropertyFactory.getProperty("ump.index.check.key") + "," + sbf.toString(), e);
        } else {
            sbf.append("from ip:").append(WorkerUtil.getWorkerIP()).append(",报警时间:").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
            logger.error("umpKey:" + (String) PropertyFactory.getProperty("ump.index.check.key") + "," + sbf.toString());
        }
    }

    /**
     @Override
     public String getWorkerType() {
        return "indexCheckWorker";
     }
     */

    public String getIntervalTime() {
        return (String) PropertyFactory.getProperty("index.check.intervalTime");
    }

    private static String httpGet(String uri) throws Exception {
        HttpURLConnection con = null;
        String result = null;
        try {
            URL url = new URL(uri);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(3000);
            con.setReadTimeout(1000);
            con.setDoOutput(false); // post改为true
            con.setDoInput(true);
            con.setUseCaches(false);
            //con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Content-Type", "text/plain");
            con.connect();
            //con.getOutputStream().write("哈哈哈哈".getBytes("UTF-8"));
            int code = con.getResponseCode();
            if (code == 200) {
                // 读取返回内容
                StringBuffer buffer = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        con.getInputStream(), "UTF-8"));
                String temp;
                while ((temp = br.readLine()) != null) {
                    buffer.append(temp);
                    buffer.append("\n");
                }
                result = buffer.toString().trim();
            } else {
                logger.warn(con.getResponseMessage());
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        con.getErrorStream(), "UTF-8"));
                StringBuffer buffer = new StringBuffer();
                String temp;
                while ((temp = br.readLine()) != null) {
                    buffer.append(temp);
                    buffer.append("\n");
                }
                logger.warn(buffer.toString());
            }
        } catch (Exception e) {
            logger.info("Get address from index error! Error message is {}:{}", e.getClass().getName(), e.getMessage());
            throw new Exception(e.getClass().getName() + "," + e.getMessage());
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return result;
    }

}