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
package com.ipd.jsf.worker.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyFactory {
	
	private static Logger logger = LoggerFactory.getLogger(PropertyFactory.class);
	
	private static Properties properties = new Properties();
	
	public PropertyFactory(String ... resource){
		try {
			for (String fileName : resource) {
				InputStream inputStream = PropertyFactory.class.getClassLoader().getResourceAsStream(fileName);
				properties.load(inputStream);
				inputStream.close();
				
			}
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
		
	}

	public static Object getProperty(String key){
		return  properties.get(key);
	}
	public static Set<Object> getPropertyKeys(){
		return  properties.keySet();
	}
	public static int getProperty(String key, int defaultValue) {
		int result = defaultValue;
		try {
			if (properties.get(key) != null) {
				result = Integer.valueOf((String)properties.get(key));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	
	public static void setProperty(String key,Object value){
		properties.put(key, value);
	}
	
}
