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

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sangupta.jerry.encoder.Base64Encoder;
import com.sangupta.jerry.http.WebInvoker;
import com.sangupta.jerry.http.WebResponse;
import com.sangupta.jerry.util.DateUtils;
import com.sangupta.jerry.util.ResponseUtils;

/**
 * The proxy handler request.
 * 
 * @author sangupta
 *
 */
public class ProxyHandler extends AbstractHandler {
	
	/**
	 * The secret used to sign and generate the HMAC signature
	 */
	static final String KEY_STRING = "9d2f3250-0fda-11e3-8ffd-0800200c9a66";
	
	/**
	 * The signing instance to use
	 */
	static Mac mac;
	
	static {
		SecretKeySpec key = new SecretKeySpec(KEY_STRING.getBytes(), "HmacSHA1");
		try {
			mac = Mac.getInstance("HmacSHA1");
			mac.init(key);
		} catch(InvalidKeyException e) {
			e.printStackTrace();
		} catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Cache loader to be used.
	 * 
	 */
	private static final CacheLoader<String, ImageCache> CACHE_LOADER = new CacheLoader<String, ImageCache>() {
		
		public ImageCache load(String url) {
			ImageCache imageCache = proxyRequest(url);
			if(imageCache == null) {
				imageCache = NOT_FOUND;
			}
			
			return imageCache;
		}
		
	};
	
	/**
	 * The image cache that is used to reduce the number of actual proxy requests.
	 * 
	 */
	private static final LoadingCache<String, ImageCache> CACHE = CacheBuilder
																.newBuilder()
																.concurrencyLevel(8)
																.expireAfterAccess(6, TimeUnit.HOURS)
																.recordStats()
																.build(CACHE_LOADER);

	/**
	 * The 404 object that should be used to reduce the amount of garbage that
	 * is generated.
	 * 
	 */
	private static final ImageCache NOT_FOUND = new ImageCache(404);
	
	/**
	 * Whether we need to allow non-image requests to be proxied or not.
	 * 
	 */
	private static final boolean DISALLOW_NON_IMAGE_REQUESTS = true;

	/**
	 * Handle the request.
	 * 
	 */
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String uri = request.getRequestURI();
		if("/favicon.ico".equalsIgnoreCase(uri)) {
			response.setStatus(404); // return a 404
			return;
		}
		
		if("/stats".equalsIgnoreCase(uri)) {
			baseRequest.setHandled(true);
			displayServerStats(response);
			return;
		}
		
		uri = requestGenuine(uri);
		if(uri == null) {
			response.setStatus(403); // unauthorized request
			return;
		}
		
		serveRequest(uri, response);
		baseRequest.setHandled(true);
	}

	/**
	 * Display the server stats
	 * 
	 */
	private void displayServerStats(HttpServletResponse response) throws IOException {
		response.setContentType("text/plain");
		response.setStatus(200);
		response.getWriter().write(CACHE.stats().toString());
	}

	/**
	 * Check if the request is purely genuine or not. Returns <code>null</code> if
	 * the request is not genuine, the actual URL to hit if genuine.
	 * 
	 * @param uri
	 * @return
	 */
	private String requestGenuine(String uri) {
		System.out.println(uri);
		String[] tokens = uri.split("/"); // this will return us 3 tokens - 1st empty - 2nd secret hash - 3rd the url
		if(tokens.length != 3) {
			return null;
		}
		
		String hmac = tokens[1];
		String url = tokens[2];
		
		// decode the URL
		url = new String(Base64Encoder.decode(url.getBytes()));
		
		// match the hmac
		if(!hmac.equals(getHMAC(url))) {
			return null;
		}
		
		return url;
	}

	/**
	 * Serve the request from the memory cache or proxy the request accordingly.
	 * 
	 * @param uri
	 * @param response
	 * @throws IOException
	 */
	private void serveRequest(String uri, HttpServletResponse response) throws IOException {
		ImageCache imageCache = CACHE.getUnchecked(uri);   
		
		// copy everything
		response.setStatus(imageCache.responseCode);
		response.setContentType(imageCache.contentType);
		response.setHeader("Connection", "Keep-Alive");
        ResponseUtils.setCacheHeaders(response, DateUtils.ONE_YEAR);
        
		response.setHeader("X-Content-Type-Options", "nosniff");
        
		response.setContentLength(imageCache.bytes.length);
        response.getOutputStream().write(imageCache.bytes);
        
	}

	/**
	 * Proxy request to this URL and return it back as an {@link ImageCache}
	 * object that we can save for ourselves.
	 * 
	 * @param uri
	 * @return
	 */
	private static ImageCache proxyRequest(final String url) {
		// if all well
		// go ahead and fetch the response from the internet
		WebResponse webResponse = WebInvoker.getResponse(url);
		if(webResponse == null) {
			return NOT_FOUND;
		}
		
		if(!webResponse.isSuccess()) {
			return NOT_FOUND;
		}
		
		// set handled
		if(DISALLOW_NON_IMAGE_REQUESTS) {
			if(!webResponse.getContentType().startsWith("image")) {
				return NOT_FOUND;
			}
		}
		
		return new ImageCache(webResponse);
	}

	/**
	 * 
	 * @param signable
	 * @return
	 */
	static String getHMAC(String signable) {
		byte[] bytes = mac.doFinal(signable.getBytes(Charset.forName("UTF8")));
		return Base64Encoder.encodeToString(bytes, false);
	}
	
}
