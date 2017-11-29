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

import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.net.URL;

public class FileUtil {

	public static File getFileByClasspath(String fileName){
		URL url = FileUtil.class.getResource(fileName);
		File file =  url != null ? new File(url.getFile()) : null;
		return file;
	}
	
	public static String file2String(File file){
		FileReader reader = null;
		StringWriter writer = null;
		char[] cbuf = new char[1024];
		try {
			reader = new FileReader(file);
			writer = new StringWriter();
			int len = 0;
			while ((len = reader.read(cbuf)) != -1) {
				writer.write(cbuf, 0, len);
				
			}
			return writer.toString();
		} catch (Exception e) {
			// ignore error
			return "";
		}finally{
			try {
				if(reader != null){
					reader.close();
				}
				if(writer != null){
					writer.close();
				}
			} catch (Exception e2) {
			}
		}
	}
}
