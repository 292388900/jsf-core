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
package com.ipd.jsf.registry.callback;

import com.ipd.jsf.gd.transport.Callback;

/**
 * 实现增量provider的callback机制
 */
public class SubscribeCallback<T> implements ICallback<T> {
    //回调对象
    private transient Callback<T, String> callback;

    //客户端IP
    private String clientIp;

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public SubscribeCallback (Callback<T, String> callback, String clientIp) {
        this.callback = callback;
        this.clientIp = clientIp;
    }

    @Override
    public String notify(T result) {
        return callback.notify(result);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SubscribeCallback(");
        sb.append("clientIp:").append(this.clientIp);
        if (this.callback != null) {
        	sb.append(",callback:").append("exist");
        }
        sb.append(")");
        return sb.toString();
    }
}
