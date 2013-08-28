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

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 * SslProxy allows to proxy to non-secure assets and serve them over
 * an SSL proxy.
 * 
 * It is all about making insecure assets look secure. This is an SSL 
 * image proxy to prevent mixed content warnings on secure pages.
 * 
 * It is based on the awesome <code>camo</code> project that Github 
 * uses to make its assets look secure. However, we only support the
 * URL format of <code>http://server/security-code/base-64-url</code>.
 * 
 * @author sangupta
 *
 */
public class SslProxy {
	
	private static final int HTTP_PORT = 80;
	
	private static final int SSL_PORT = 443;
	
	private static final int OUTPUT_BUFFER_SIZE = 32768;
	
    public static void main( String[] args ) throws Exception {
    	// create the thread pool
    	QueuedThreadPool threadPool = new QueuedThreadPool();
    	threadPool.setMinThreads(25);
    	threadPool.setMaxThreads(250);
    	
    	// create the server
    	Server server = new Server(threadPool);
    	
    	// http configuration
		HttpConfiguration httpConfig = new HttpConfiguration();
		httpConfig.setSecureScheme("https");
		httpConfig.setSecurePort(SSL_PORT);
		httpConfig.setOutputBufferSize(OUTPUT_BUFFER_SIZE);
		
		// http connector
		ServerConnector http = new ServerConnector(server,new HttpConnectionFactory(httpConfig));        
		http.setPort(HTTP_PORT);
		http.setIdleTimeout(30000);

		// SSL Context Factory for HTTPS and SPDY
		SslContextFactory sslContextFactory = new SslContextFactory();
		sslContextFactory.setKeyStorePath("c:/users/sangupta/.keystore");
		sslContextFactory.setKeyStorePassword("tomcat");
		sslContextFactory.setKeyManagerPassword("tomcat");
  
		// HTTPS Configuration
		HttpConfiguration https_config = new HttpConfiguration(httpConfig);
		https_config.addCustomizer(new SecureRequestCustomizer());
		  
		// HTTPS connector
		ServerConnector https = new ServerConnector(server, new SslConnectionFactory(sslContextFactory,"http/1.1"), new HttpConnectionFactory(https_config));
		https.setPort(SSL_PORT);
		https.setIdleTimeout(500000);

		// Set the connectors
		server.setConnectors(new Connector[] { http, https });

		// set the request handler
        server.setHandler(new ProxyHandler());
  
        // start the server
        server.start();
        server.join();
    }
    
}
