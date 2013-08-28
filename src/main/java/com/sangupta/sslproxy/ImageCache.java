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

import com.sangupta.jerry.http.WebResponse;

/**
 * A simple value object that keeps the necessary data in cache
 * that could be served to the clients.
 * 
 * @author sangupta
 *
 */
public class ImageCache {
	
	/**
	 * The response code received from the server
	 */
	public int responseCode;
	
	/**
	 * The content type header received from the server
	 */
	public String contentType;
	
	/**
	 * The actual bytes received from the server
	 */
	public byte[] bytes;
	
	/**
	 * Default constructor
	 * 
	 */
	public ImageCache() {
		
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param code
	 */
	public ImageCache(int code) {
		this.responseCode = code;
	}

	/**
	 * Construct an {@link ImageCache} object for the given {@link WebResponse} 
	 * object. The {@link WebResponse} argument should not be <code>null</code>.
	 * 
	 * @param response
	 */
	public ImageCache(WebResponse response) {
		this.responseCode = response.getResponseCode();
		this.contentType = response.getContentType();
		this.bytes = response.getBytes();
	}
	
}
