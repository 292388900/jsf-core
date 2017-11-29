/**
 * 
 */
package com.ipd.jsf.zookeeper.cache;
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
public class PathNode {
	
	private String path;
	
	private Byte[] data;
	
	
	public PathNode(String path,Byte[] bytes){
		this.path = path;
		this.data = bytes;
	}
	
	

	public String getPath() {
		return path;
	}





	public void setPath(String path) {
		this.path = path;
	}





	public Byte[] getData() {
		return data;
	}





	public void setData(Byte[] data) {
		this.data = data;
	}





	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
