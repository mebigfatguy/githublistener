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
package com.mebigfatguy.githublistener;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.kohsuke.github.GHEventInfo;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.extras.OkHttpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.okhttp.OkHttpClient;

public class EventPoller implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventPoller.class);
	private static final int POLL_TIME = (60 * 60 * 1000) / 6000;
	
	private final ArrayBlockingQueue<GHEventInfo> eventQueue;
	private final GitHub github;
	
	public EventPoller(ArrayBlockingQueue<GHEventInfo> queue, String userName, String authToken) throws IOException {
		eventQueue = queue;

		github = GitHub.connect(userName, authToken);
		github.setConnector(new OkHttpConnector(new OkHttpClient()));
	}
	
	@Override
	public void run() {
		while (!Thread.interrupted()) {
			try {
				List<GHEventInfo> currentEvents = github.getEvents();
				eventQueue.addAll(currentEvents);
				Thread.sleep(POLL_TIME);
			} catch (InterruptedException e) {
				return;
			} catch (IOException ioe) {
				LOGGER.error("Failed fetching events from github", ioe);
			}
		}
	}
}
