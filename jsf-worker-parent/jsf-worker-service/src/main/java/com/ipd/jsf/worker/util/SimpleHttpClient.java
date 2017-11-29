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
package com.ipd.jsf.worker.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

 
public class SimpleHttpClient {
	private static final int  CONNECT_TIMEOUT=2*1000; //2秒超时
    public static String get(String urlStr,String requestMethod) throws IOException{
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if ( requestMethod != null && !"".equals(requestMethod)){
            connection.setRequestMethod(requestMethod);
        }
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(CONNECT_TIMEOUT*20);
        connection.connect();
        InputStream in = null;
        if ( connection.getResponseCode() >= 400 ){
            in = connection.getErrorStream();
        } else {
            in = connection.getInputStream();
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        StringBuilder content = new StringBuilder();
        String tmp;
        while ( (tmp = bufferedReader.readLine()) != null ){
            content.append(tmp);
        }
        if ( in != null ){
            in.close();
        }
        bufferedReader.close();
        connection.disconnect();
        return content.toString();
    }


    public static String post(String urlStr,String content,String requestMethod,ContentType contentType) throws IOException{
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod(requestMethod);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Content-type",contentType.contentType);
        connection.connect();
        Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
        if ( content != null && !"".equals(content)){
            writer.write(content);
        }
        writer.flush();
        writer.close();
        InputStream in = null;
        if ( connection.getResponseCode() >= 400 ){
            in = connection.getErrorStream();
        } else {
            in = connection.getInputStream();
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        StringBuilder response = new StringBuilder();
        String tmp;
        while ( (tmp = bufferedReader.readLine()) != null ){
            response.append(tmp);
        }
        if ( in != null ){
            in.close();
        }
        bufferedReader.close();
        connection.disconnect();
        return response.toString();
    }

    public static enum ContentType{
        JSON("application/json");
        private String contentType;
        ContentType(String contentType) {
            this.contentType = contentType;
        }
    }

}
