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

package com.ipd.jsf.service.vo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Paging {
	private final static int DEFAULT_PAGE_SIZE = 10;
	private final static int DEFAULT_PAGE_INDEX = 1;
	
    /**
     * the total page number
     */
    private int totalPage;
    
    /**
     * the total record number
     */
    private int totalRecord;
    
    /**
     * the number of showed record for each page
     */
    private int pageSize = DEFAULT_PAGE_SIZE;
    
    /**
     * the index for current page which is being accessed
     */
    private int pageIndex = DEFAULT_PAGE_INDEX;
    
    /**
     * the offset which the data is started to selected from
     */
    private int pageOffset;

    /**
     * the index for previous page, the default value is 1
     */
    private int prevPage = 1;

    /**
     * the index for next page, the default value is 1
     */
    private int nextPage = 1;
    
    private boolean hasNextPage = false;

    private boolean hasPrevPage = false;

    /**
     * the result for query
     */
    private Collection result;
    
    /**
     * usual , params is a map or a parameterization object
     */
    private Map<String, Object> params = new HashMap<String, Object>();
    
    /**
     * for query
     */
    public static Paging getParamPaging(Integer pageIndex, Integer pageSize, Object params) {
    	Paging paging = new Paging();
    	
    	paging.setPageSize(pageSize != null ? pageSize : 0);
    	paging.setPageIndex(pageIndex != null ? pageIndex : 0);
//    	paging.setParams(params);
    	
    	return paging;
    }
    
    /**
     * for result
     */
    public static Paging getResultPaging(Paging paging, Integer totalRecord, Collection result) {
    	if(paging == null) 
    	    paging = new Paging();
    	
    	paging.setTotalRecord(totalRecord != null ? totalRecord : 0);
    	paging.setResult(result);
    	
    	return paging;
    }
    
    /**
     * <p> deal with paging by Collection. memory paging
     * @param source the data source
     * @param pageIndex the starting location
     * @param size the number for return
     * @return Paging
     */
	public static Paging pagingByList(List source, int pageIndex, int pageSize) {
		if(source == null)
			throw new IllegalArgumentException("the source must not be null!");
		Paging paging = new Paging();
		paging.setPageSize(pageSize);
		paging.setPageIndex(pageIndex);
		paging.setTotalRecord(source.size());
		
		List target = new ArrayList();
		for(int i = paging.getPageOffset(); i < paging.getEndIndex(); i++) {
			target.add(source.get(i));
		}
		paging.setResult(target);
		
		return paging;
	}
	
	private int getEndIndex() {
		int endIndex = this.getPageOffset() + this.getPageSize();
		return endIndex < totalRecord ? endIndex : totalRecord;
	}
    
	public int getPageOffset() {
		return (pageIndex - 1) * pageSize;
	}
    
    //getter and setter
	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage > 0 ? totalPage : 0;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
        this.totalRecord = totalRecord > 0 ? totalRecord : 0;
        this.setTotalPage((int) Math.ceil((double)totalRecord/pageSize));
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = (pageSize > 0) ? pageSize : DEFAULT_PAGE_SIZE;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex > 0 ? pageIndex : 1;
	}

	public int getPrevPage() {
		return prevPage;
	}

	public void setPrevPage(int prevPage) {
		this.prevPage = prevPage < 1 ? 1 : prevPage;
	}

	public int getNextPage() {
		return nextPage;
	}

	public void setNextPage(int nextPage) {
		this.nextPage = nextPage > this.totalPage ? this.totalPage : nextPage;
	}

	public boolean isHasNextPage() {
		hasNextPage = pageIndex < totalPage ? true : false;
		return hasNextPage;
	}

	public void setHasNextPage(boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
	}

	public boolean isHasPrevPage() {
		hasPrevPage = pageIndex > 1 ? true : false;
		return hasPrevPage;
	}

	public void setHasPrevPage(boolean hasPrevPage) {
		this.hasPrevPage = hasPrevPage;
	}

	public Collection getResult() {
		return result;
	}

	public void setResult(Collection result) {
		this.result = result;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
	
	@Override
	public String toString() {
		return "Paging [totalPage=" + totalPage + ", totalRecord="
				+ totalRecord + ", pageSize=" + pageSize + ", pageIndex="
				+ pageIndex + ", pageOffset=" + pageOffset + ", prevPage="
				+ prevPage + ", nextPage=" + nextPage + ", hasNextPage="
				+ hasNextPage + ", hasPrevPage=" + hasPrevPage + ", result="
				+ result + ", params=" + params + "]";
	}

}
