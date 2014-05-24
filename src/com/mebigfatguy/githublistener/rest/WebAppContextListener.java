/** githublistener - an github events recording using cassandra. 
  * Copyright 2014 MeBigFatGuy.com 
  * Copyright 2014 Dave Brosius 
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0 
  * 
  * Unless required by applicable law or agreed to in writing, 
  * software distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and limitations 
  * under the License. 
  */ 
package com.mebigfatguy.githublistener.rest;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.kohsuke.github.GHEventInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.mebigfatguy.githublistener.CassandraWriter;
import com.mebigfatguy.githublistener.EventPoller;

public class WebAppContextListener implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebAppContextListener.class);
	
	private ExecutorService pool = null;
	private ArrayBlockingQueue<GHEventInfo> queue;
	private Cluster cluster;

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		try {
			Context ic = new InitialContext();
			Context envContext = (Context)ic.lookup("java:/comp/env");
			
			String endPoints = (String) envContext.lookup("endpoints");
			cluster = new Cluster.Builder().addContactPoints(endPoints.split(",")).build();
			
			int numWriters = (Integer) envContext.lookup("numwriters");
			pool = Executors.newFixedThreadPool(numWriters + 1);
			
			queue = new ArrayBlockingQueue<GHEventInfo>(10000);
			
			String userName = (String) envContext.lookup("username");
			String authToken = (String) envContext.lookup("authtoken");
			EventPoller ep = new EventPoller(queue, userName, authToken);
			pool.submit(ep);
			
			int replicationFactor = (Integer) envContext.lookup("replicationfactor");
			
			for (int i = 0; i < numWriters; i++) {
				CassandraWriter cw = new CassandraWriter(queue, cluster, replicationFactor);
				pool.submit(cw);
			}	
		} catch (NamingException ne) {
			LOGGER.error("Failed looking up environment properties through jndi", ne);
		} catch (IOException ioe) {
			LOGGER.error("Failed to initialize event poller", ioe);
		}
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		try {
			pool.shutdown();
			cluster.close();
			queue.clear();
		} finally {
			pool = null;
			cluster = null;
			queue = null;
		}
	}
}
