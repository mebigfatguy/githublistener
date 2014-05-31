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
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHEventInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mebigfatguy.githublistener.CassandraModel;
import com.mebigfatguy.githublistener.CassandraWriter;
import com.mebigfatguy.githublistener.EventPoller;

public class WebAppContextListener implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebAppContextListener.class);
	
	private ArrayBlockingQueue<GHEventInfo> queue;
	private CassandraModel model;

	@Override
	public void contextInitialized(ServletContextEvent event) {
		try {
			Context ic = new InitialContext();
			Context envContext = (Context)ic.lookup("java:/comp/env");
			
			String endPoints = (String) envContext.lookup("endpoints");
			Integer replicationFactor = (Integer) envContext.lookup("replicationfactor");
			
			model = new CassandraModel(endPoints.split(","), replicationFactor.intValue());
			event.getServletContext().setAttribute("model", model);
			
			queue = new ArrayBlockingQueue<GHEventInfo>(10000);
			
			String userName = (String) envContext.lookup("username");
			String authToken = (String) envContext.lookup("authtoken");
			EventPoller ep = new EventPoller(queue, userName, authToken);
			startDaemonThread(ep, "Event Poller");
			
			Map<GHEvent, Long> eventWeights = buildEventWeights((String) envContext.lookup("eventweights"));
			
			int numWriters = (Integer) envContext.lookup("numwriters");
			for (int i = 0; i < numWriters; i++) {
				CassandraWriter cw = new CassandraWriter(queue, model, eventWeights);
				startDaemonThread(cw,  "Cassandra Writer " + (i + 1));
			}
		} catch (NamingException ne) {
			LOGGER.error("Failed looking up environment properties through jndi", ne);
		} catch (IOException ioe) {
			LOGGER.error("Failed to initialize event poller", ioe);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		try {
			model.close();
			queue.clear();
		} finally {
			model = null;
			queue = null;
		}
	}
	
	private void startDaemonThread(Runnable r, String name) {
		Thread t = new Thread(r);
		t.setDaemon(true);
		t.setName(name);
		t.start();
	}
	
	private Map<GHEvent, Long> buildEventWeights(String eventWeights) {
		
		
		Map<GHEvent, Long> weights = new EnumMap<>(GHEvent.class);
		Long one = Long.valueOf(1);
		for (GHEvent event : GHEvent.values()) {
			weights.put(event, one);
		}
		
		if ((eventWeights != null) && (!eventWeights.isEmpty())) {
			try {
				eventWeights = eventWeights.replace("'", "\"");
				ObjectMapper mapper = new ObjectMapper();
				JsonFactory factory = mapper.getJsonFactory();
				JsonParser jp = factory.createJsonParser(eventWeights);
				JsonNode ewJson = mapper.readTree(jp);
				
				Iterator<Map.Entry<String, JsonNode>> it = ewJson.getFields();
				while (it.hasNext()) {
					Map.Entry<String, JsonNode> entry = it.next();
					GHEvent event = GHEvent.valueOf(entry.getKey().toUpperCase());
					if (event != null) {
						weights.put(event, Long.valueOf(entry.getValue().getLongValue()));
					}
				}
			} catch (Exception e) {
				LOGGER.error("Failed parsing the event weights property {}", eventWeights);
			}
		}
		
		return weights;
	}
}
