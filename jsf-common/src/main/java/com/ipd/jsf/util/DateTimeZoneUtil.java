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

package com.ipd.jsf.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.common.util.PropertyFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 时区转换工具
 * 如果当前服务器时区与目标时区不一致，需要进行时区和时间转换，转为目标时区和时间
 * 转换的对象包括心跳时间，接口版本号的时间，server和client表的时间，app和appins表的时间，扫描接口版本号的查询时间
 */
public class DateTimeZoneUtil {
	private static Logger logger = LoggerFactory.getLogger(DateTimeZoneUtil.class);
	//是否关闭时区转换，默认是关闭的-true, 可配置的
	private static boolean forceClose = true;
	private static boolean convertTimeZoneFlag = false;
	//目标时区ID
	public static String targetTimeZoneID = "GMT+08:00";
	//本地时区对象
	public static TimeZone localTimeZone = TimeZone.getDefault();
	//目标时区对象
	public static TimeZone targetTimeZone = null;

	static {
		try {
			forceClose = Boolean.valueOf(PropertyFactory.getProperty("timezone.convert.forceclose", "true")).booleanValue();
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
		try {
			if (forceClose) {
				convertTimeZoneFlag = false;
			} else {
				//如果目标时区ID为空，目标时区ID则取当前服务器的时区
				if (targetTimeZoneID == null || targetTimeZoneID.isEmpty()) {
					targetTimeZoneID = TimeZone.getDefault().getID();
				}
				//设置目标时区
				targetTimeZone = TimeZone.getTimeZone(targetTimeZoneID);
				//如果目标时区与当前时区不一致，则进行时区转换
				if (TimeZone.getDefault().getRawOffset() != TimeZone.getTimeZone(targetTimeZoneID).getRawOffset()) {
					convertTimeZoneFlag = true;
				}
			}
			logger.info("-------------forceClose:{}, convertTimeZoneFlag:{}", forceClose, convertTimeZoneFlag);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 根据时区计算时间
	 * 将当地时间减去当地时区的偏移量，获得标准格林尼治时间，再加上目标时区的偏移量，然后获得目标时区的时间
	 * @return
	 */
	public static Date getTargetTime() {
		if (convertTimeZoneFlag) {
			return convertToTargetDate(System.currentTimeMillis());
		} else {
			return new Date();
		}
	}

	/**
	 * 根据时区计算时间
	 * 将当地时间减去当地时区的偏移量，获得标准格林尼治时间，然后再加上目标时区的偏移量，获得目标时区的时间
	 * @return
	 */
	public static Date getTargetTime(long time) {
		if (convertTimeZoneFlag) {
			return convertToTargetDate(time);
		} else {
			return new Date(time);
		}
	}

	private static Date convertToTargetDate(long time) {
		long targetTime = time - localTimeZone.getRawOffset() + targetTimeZone.getRawOffset();
		return new Date(targetTime);
	}

	public static long getTimeZone(){
		Calendar calendar = Calendar.getInstance();
		long timeInMillis = calendar.getTimeInMillis();
		long requestTimestamp = timeInMillis - TimeZone.getDefault().getRawOffset();
		return requestTimestamp;
	}

	public static String getTime(){
		SimpleDateFormat detailFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String timestamp = detailFormat.format(Calendar.getInstance().getTime());
		return timestamp;
	}
}
