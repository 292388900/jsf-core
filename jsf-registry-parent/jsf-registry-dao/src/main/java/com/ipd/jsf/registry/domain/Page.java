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
package com.ipd.jsf.registry.domain;

import java.util.List;
import java.util.Map;

public class Page<T> {
	
	List<T> results;
	
	private int totalCount;
	
	private int itemPerPage = 20;
	
	private int totalPage = 0;
	
	private int currentPage = 1;
	
	private int requestPage;
	
	private Map<String,String> conditionMap;//查询条件以key－value方式存放

	public List<T> getResults() {
		return results;
	}

	public void setResults(List<T> results) {
		this.results = results;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getItemPerPage() {
		return itemPerPage;
	}

	public void setItemPerPage(int itemPerPage) {
		this.itemPerPage = itemPerPage;
	}

	public int getTotalPage() {
	    if (totalPage == 0) {
            if (totalCount % itemPerPage == 0) {
                totalPage = totalCount / itemPerPage;
            } else {
                totalPage = totalCount / itemPerPage + 1;
            }
	    }
        return totalPage;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getRequestPage() {
		return requestPage;
	}

	public void setRequestPage(int requestPage) {
		this.requestPage = requestPage;
	}

	public Map<String, String> getConditionMap() {
		return conditionMap;
	}

	public void setConditionMap(Map<String, String> conditionMap) {
		this.conditionMap = conditionMap;
	}

    public int getOffset() {
        return (this.currentPage - 1) * itemPerPage;
    }

    public int getLimit() {
        return itemPerPage;
    }
}
