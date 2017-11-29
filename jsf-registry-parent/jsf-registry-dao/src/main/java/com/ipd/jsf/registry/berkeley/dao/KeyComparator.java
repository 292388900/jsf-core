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
package com.ipd.jsf.registry.berkeley.dao;

import java.io.UnsupportedEncodingException;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyComparator implements Comparator<byte[]> {
	
	private static final Logger logger = LoggerFactory.getLogger(KeyComparator.class);

	@Override
	public int compare(byte[] o1, byte[] o2) {
		try {
			String value1 = new String((byte[])o1, "utf-8");
			String value2 = new String((byte[])o2, "utf-8");
//			System.out.println(value1);
//			System.out.println(value2);
			long num1 = Long.valueOf(value1.substring(value1.lastIndexOf("_") + 1));
			long num2 = Long.valueOf(value2.substring(value2.lastIndexOf("_") + 1));
			return (int)(num1 - num2);
		} catch (UnsupportedEncodingException e) {
			logger.error("the key format is undefined!", e);
		}
		return 0;
	}

}
