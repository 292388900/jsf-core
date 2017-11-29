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


import java.util.HashMap;
import java.util.Map;

public enum ComputerRoom {
	
    Default(0),
	B28(1),
	YiZhuang(2),
	HuangCun(3),
	Yongfeng(4),
	PCI(5),
	M6(6),
    Langfang(7),
    HongKong(8),
    Indonesia(9),
    Guangzhou(10),
    Majuqiao(11),
    Huabei(12),
    Huadong(13);

	private int value; 

	private ComputerRoom(int value){
		this.value = value;
	}

    public Integer value() {
        return value;
    }
    
    public static ComputerRoom of(final Integer value) {
        if (value == null) {
            return null;
        }
        switch (value.intValue()) {
        case 0:
            return Default;
        case 1:
            return B28;
        case 2:
            return YiZhuang;
        case 3:
            return HuangCun;
        case 4:
            return Yongfeng;
        case 5:
            return PCI;
        case 6:
            return M6;
        case 7:
        	return Langfang;
        case 8:
        	return HongKong;
        case 9:
        	return Indonesia;
        case 10:
        	return Guangzhou;
        case 11:
        	return Majuqiao;
        case 12:
        	return Huabei;
        case 13:
        	return Huadong;
        }
        return null;
    }

    public static Map<Integer, String> getAllRoom() {
        Map<Integer, String> result = new HashMap<Integer, String>();
        for (ComputerRoom room : ComputerRoom.values()) {
            result.put(room.value(),room+"");
        }
        return result;
    }
}
