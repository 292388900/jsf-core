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
package com.ipd.jsf.worker.util;

import java.util.ArrayList;
import java.util.List;

public class ListPaging<T> {
	private int itemLimit = 0;
	private int totalCount = 0;
	private int totalPage = 0;
	private int page = 0;
	private int startIndex = 0;
	private int endIndex = 0;
	private List<T> list = null;

    public ListPaging(List<T> list, int itemLimit) {
    	this.totalCount = list.size();
    	this.itemLimit = itemLimit;
    	this.totalPage = totalCount % itemLimit == 0 ? totalCount / itemLimit : totalCount / itemLimit + 1;
    	this.list = list;
    }

    public List<T> nextPageList() {
    	if (page < totalPage) {
            startIndex = page * itemLimit;
            endIndex = (page + 1) * itemLimit;
            if (endIndex > totalCount) endIndex = totalCount;
            page ++;
            return list.subList(startIndex, endIndex);
        }
    	return new ArrayList<T>();
    }

    public void reset() {
    	page = 0;
    	startIndex = 0;
    	endIndex = 0;
    }

    public int getTotalCount() {
    	return totalCount;
    }

	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}
    
    
}
