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
package com.ipd.jsf.registry.util;

import com.ipd.fastjson.JSON;
import com.ipd.fastjson.JSONObject;
import com.ipd.jsf.common.util.PropertyFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class HttpClientUtil {

	private final static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
	private static String token = null;
	
	static {
		try {
			token = String.valueOf(PropertyFactory.getProperty("deploy.app.token"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static String getAppNameByAppId(int appId) {
		String httpResult = null;
		String appName = null;
		CloseableHttpResponse response = null;
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			String url = "?id=" + appId+ "&token=" + token;
			HttpGet httpget = new HttpGet(url);
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).build();
			httpget.setConfig(requestConfig);
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				httpResult = EntityUtils.toString(entity);
				if (httpResult == null) {
					logger.warn("在自动部署未取到App, appId:{}", appId);
				} else {
					try {
						Map<String, JSONObject> map = JSON.parseObject(httpResult, Map.class);
						String code = String.valueOf(map.get("code"));
						if (code.equals("success")) {
							JSONObject data = map.get("data");
							JSONObject app = data.getJSONObject("app");
							if (app != null) {
								appName = app.getString("name");
								logger.info("appName: {}", appName);
							}
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (Exception e2) {
				logger.error(e2.getMessage(), e2);
			}
		}
		return appName;
	}


}
