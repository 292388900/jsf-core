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
package com.ipd.jsf.worker.manager.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ipd.jsf.worker.log.dao.IfaceMethodDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ipd.jsf.worker.manager.IfaceMethodManager;

@Service
public class IfaceMethodManagerImpl implements IfaceMethodManager{
	
	@Autowired
    IfaceMethodDao ifaceMethodDao;
	
	@Override
	public List<String> findMethodNames(String ifaceName) {
		Set<String> result = new HashSet<String>();
		List<String> methods = (List<String>) ifaceMethodDao.findMethodNames(ifaceName);
		if(methods != null){
			for(String method : methods){
				result.addAll(Arrays.asList(method.split(",")));
			}
		}
		result.remove("");
		methods.clear();
		for(String m : result){
			if(StringUtils.hasText(m)){
				methods.add(m);
			}
		}
		return new ArrayList<String>(result);
	}
}
