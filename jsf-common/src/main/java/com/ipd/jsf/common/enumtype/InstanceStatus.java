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


public enum InstanceStatus {

	offline(0),            //死亡-红色
	online(1),             //在线-绿色
	@Deprecated
	onlineButNotWork(2),   //下线-黄色
	@Deprecated
	offlineAndNotWork(3),  //死亡下线-深黄色
	unreg(4),              //逻辑删除-反注册
	deleted(5);            //逻辑删除-扫描(也包括自动部署下线方法)

	private int value;
	
	private InstanceStatus(int status){
		this.value = status;
	}
	
    public Integer value() {
        return value;
    }

    public static InstanceStatus of(final Integer value) {
        if (value == null) {
            return null;
        }
        switch (value.intValue()) {
        case 0:
            return offline;
        case 1:
            return online;
        case 2:
            return onlineButNotWork;
        case 3:
            return offlineAndNotWork;
        case 4:
        	return unreg;
        case 5:
        	return deleted;
        }
        return null;
    }
	
}
