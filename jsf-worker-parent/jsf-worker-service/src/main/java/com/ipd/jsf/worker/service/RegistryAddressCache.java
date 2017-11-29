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
package com.ipd.jsf.worker.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ipd.jsf.common.util.PropertyFactory;
import org.aspectj.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class RegistryAddressCache {
    /**
     * slf4j Logger for this class
     */
    private final static Logger logger = LoggerFactory.getLogger(RegistryAddressCache.class);


    static {
        new PropertyFactory("check.properties");
    }

    private LoadingCache<String, Map<String, String>> addressCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Integer.parseInt((String) PropertyFactory.getProperty("data.update.intervalTime")), TimeUnit.MINUTES)  // 20分钟重新计算加载一次
            .maximumSize(50)
            .build(new CacheLoader<String, Map<String, String>>() {
                public Map<String, String> load(String key) throws Exception {
                    return key.equals("registryAddress") ? setCheckAddress() : null;
                }
            });


    private Map<String, String> setCheckAddress() {
        Map<String, String> config = getAddressFromFile();
        return config;
    }

    private Map<String, String> getAddressFromFile() {
        Map<String, String> content = new HashMap<String, String>();
        try {
            String contentS = FileUtil.readAsString(new File((String) PropertyFactory.getProperty("registry.address.backup.filename")));
            String[] contentArray = contentS.split("。");
            for (String temp : contentArray) {
                String[] tempArray = temp.split("=");
                content.put(tempArray[0], tempArray[1]);
            }
            logger.info("read registry address from the local file success");
        } catch (Exception e) {
            logger.error("read registry address from the local file failure!" + e.getMessage(), e);
        }
        return content;
    }

    /**
     * @return
     */
    public Map<String, String> getRegistryAddress() {
        try {
            Map<String, String> address = addressCache.get("registryAddress");
            if (address != null) {
                return address;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

}