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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

public class AppDateUtils {
	
	public static final String FORMAT_DATE_DOT="yyyy.MM.dd"; 
	public static final String FORMAT_DATE="yyyy-MM-dd"; 
	public static final String FORMAT_FULL="yyyy-MM-dd HH:mm:ss"; 
	public static final String FORMAT_FULL_TZ="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"; 

	public static String getDateNow() {
		return getDateByFormat(FORMAT_DATE);
	}
	public static String getDateNowDot() {
		return getDateByFormat(FORMAT_DATE_DOT);
	}
	
	public static String getDateByFormat(String fmt) {
		DateFormat df=new SimpleDateFormat(fmt);
		Calendar cal=Calendar.getInstance();
		return df.format(cal.getTime());
	}
	
	public static Date getDateFromFormat(String fmt, String dateString) {
		Date date = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat(fmt);
		try {
			date =  dateFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	public static String formatDate(String fmt,Date date) {
		if (StringUtils.isBlank(fmt)) {
			fmt=FORMAT_FULL;
		}
		DateFormat df=new SimpleDateFormat(fmt);
		return df.format(date);
	}
}
