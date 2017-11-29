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

package com.ipd.jsf.common.enumtype;

public enum IfacePropertyType {
	MONITOR(1, "MONITOR"),
	WORKER(2, "WORKER"),
	REGISTRY(3, "REGISTRY");

    private int value;
    
    private String  key;

    private IfacePropertyType(int value, String key) {
        this.value = value;
        this.key = key;
    }

    public Integer value() {
        return value;
    }
    
    public static IfacePropertyType getBykey(String key){
    	if(key.equals("MONITOR")){
    		return MONITOR;
    	}
    	if(key.equals("WORKER")){
    		return WORKER;
    	}
    	if(key.equals("REGISTRY")){
    	    return REGISTRY;
    	}
    	return null;
    }
    
	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

    
	public static IfacePropertyType valueOf(final Integer value) {
		switch (value.intValue()) {
		case 1:
			return MONITOR;
		case 2:
			return WORKER;
		case 3:
		    return REGISTRY;
		}
		return null;
    }
}
