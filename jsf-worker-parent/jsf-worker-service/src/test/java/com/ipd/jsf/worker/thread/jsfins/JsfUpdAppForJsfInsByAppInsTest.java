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
package com.ipd.jsf.worker.thread.jsfins;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:/spring/test-context.xml" })
public class JsfUpdAppForJsfInsByAppInsTest {

	@Resource(name = "jsfUpdAppForJsfInsByAppInsWorker", type = JsfUpdAppForJsfInsByAppIns.class)
	private JsfUpdAppForJsfInsByAppIns worker;

	@Test
	public void test() {

		try {
			worker.init();
			worker.run();
			synchronized (this) {
				this.wait();
			}
			// worker.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
