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
package com.ipd.testsaf;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author 360buy
 *
 */

public interface TestHelloService {

    public String echoStr(String str, String p) throws Exception;

    public String test(String str);

    public int testInt(int value);

    public double testDouble(double value);

    public float testFloat(float value);

    public boolean testBool(boolean value);

    public Date testDate(Date value);

    public long testLong(long value);

    public Map<String, String> testMap(Map<String, String> value);

    public List<String> testList(List<String> value);

//	public JSONObject perform(JSONObject a);
	
	
}
