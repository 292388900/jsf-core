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
package com.ipd.jsf.worker.service.telnet;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ipd.jsf.worker.service.impl.ScanNodeStatusHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:/spring/test-context.xml" })
public class TelnetTest {
	
	@Test
	public void test() {
		try {
			ScanNodeStatusHelper helper = new ScanNodeStatusHelper();
			
			//检查正常情况
			byte t = helper.doCheck("com.ipd.jsf.service.RegistryService", "192.168.150.121", 40660, "reg", 1);
			Assert.assertEquals(helper.TELNET_OK, t);

			//检查分组错误的情况
			t = helper.doCheck("com.ipd.jsf.service.RegistryService", "192.168.150.121", 40660, "reg1", 1);
			Assert.assertEquals(helper.TELNET_NOTEXIST, t);

			//检查端口错误的情况
			t = helper.doCheck("com.ipd.jsf.service.RegistryService", "192.168.150.121", 40661, "reg", 1);
			Assert.assertEquals(helper.TELNET_NOTCONNECT, t);

			//检查实例正确的情况（实例key可能会变化，需要修改下）
			boolean b = helper.checkInstanceKey("192.168.150.121", 40660, "192.168.150.121_10742_57088");
			Assert.assertEquals(true, b);

			//检查注册中心地址正确的情况
			helper.getRegistryList().add("192.168.150.121:40660");
			helper.getRegistryList().add("192.168.150.119:40660");
			b = helper.checkRegistry("192.168.150.121", 22000);
			Assert.assertEquals(true, b);

			//检查注册中心地址错误的情况
			helper.getRegistryList().clear();
			helper.getRegistryList().add("192.168.151.139:40660");
			helper.getRegistryList().add("192.168.151.142:40660");
			b = helper.checkRegistry("192.168.150.121", 22000);
			Assert.assertEquals(false, b);

			//检查jsf版本正确的情况
			String jsfVersion = helper.getJsfVersion("192.168.150.121", 40660);
			System.out.println(jsfVersion);
			//jsf版本有可能会变，如果变化，需要修改下面的jsf版本
			Assert.assertEquals("JSF1.2.2_201506150925", jsfVersion);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
