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
package com.ipd.jsf.registry.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.registry.Provider;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.Constants.ProtocolType;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.service.SubscribeService;
import com.ipd.jsf.registry.service.helper.SubscribeHelper;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.vo.JsfUrl;

public class LocalConsumerConfig<T> extends ConsumerConfig<T> {
	private static final long serialVersionUID = 4048298864125288234L;
	private long dataVersion = 0;
	private SubscribeService subscribeService;
	private SubscribeHelper subscribeHelper;

	public LocalConsumerConfig(SubscribeService subscribeService, SubscribeHelper subscribeHelper) {
		this.subscribeService = subscribeService;
		this.subscribeHelper = subscribeHelper;
	}

	/**
	 * 更新provider列表
	 * @param list
	 * @throws Exception 
	 */
	public void updateProvider() throws Exception {
		long dataVersion = subscribeHelper.getInterfaceDataVersion(this.interfaceId, this.alias);
        List<JsfUrl> tempList = getProviderList();
		if (this.dataVersion != dataVersion) {
			super.providerListener.updateProvider(getProvidersFromUrls(tempList));
			this.dataVersion = dataVersion;
		}
	}

	private List<JsfUrl> getProviderList() throws Exception {
    	return subscribeService.subscribe(
    			this.interfaceId, this.alias,
                ProtocolType.jsf.value(),
                String.valueOf(Constants.JSF_VERSION),
                Constants.DEFAULT_CODEC_TYPE.name(),
                null,
                RegistryUtil.getRegistryIP(), null, 0);
    }

	/**
	 * 采用直连
	 * @throws Exception
	 */
	public void setUrl() throws Exception {
        setUrl(convertUrlFromProviderList(getProviderList()));
	}

	/**
	 * 转换url列表为provider列表
	 * @param urlList
	 * @return
	 */
	private List<Provider> getProvidersFromUrls(List<JsfUrl> urlList) {
		List<Provider> providerList = new ArrayList<Provider>();
		if (urlList != null && !urlList.isEmpty()) {
			for (JsfUrl url : urlList) {
				providerList.add(getProviderFromUrl(url));
			}
		}
		return providerList;
	}

	/**
	 * 转换url为provider
	 * @param urlList
	 * @return
	 */
	private Provider getProviderFromUrl(JsfUrl url) {
		Provider provider = new Provider();
		provider.setAlias(url.getAlias());
		provider.setInterfaceId(url.getIface());
		provider.setIp(url.getIp());
		provider.setPort(url.getPort());
		provider.setProtocolType(Constants.ProtocolType.valueOf(url.getProtocol()));
		provider.setWeight(RegistryUtil.getIntValueFromMap(url.getAttrs(), RegistryConstants.WEIGHT, 100));
		return provider;
	}

    private String convertUrlFromProviderList(List<JsfUrl> tempList) throws Exception {
        StringBuilder url = new StringBuilder();
        if (!CollectionUtils.isEmpty(tempList)) {
            for (JsfUrl jsfUrl : tempList) {
                if (url.length() > 0) {
                    url.append(",");
                }
                url.append(jsfUrl.getIp()).append(":").append(jsfUrl.getPort());
            }
        } else {
        	throw new Exception("no provider");
        }
        return url.toString();
    }
}
