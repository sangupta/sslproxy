/**
 *
 * sslproxy - A Java-based SSL reverse proxy to serve non-secure assets securely.
 * Copyright (c) 2013, Sandeep Gupta
 * 
 * http://www.sangupta/projects/sslproxy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.sangupta.sslproxy;

import com.sangupta.jerry.encoder.Base64Encoder;


/**
 * Unit test for simple App.
 * 
 * @author sangupta
 */
public class SslProxyTest {
	
	public static void main(String[] args) {
		String url = "http://google.com/favicon.ico";
		String hmac = ProxyHandler.getHMAC(url);
		String hit = hmac + "/" + Base64Encoder.encodeToString(url.getBytes(), false);
		System.out.println("Hit the url: " + hit);
	}
	
}