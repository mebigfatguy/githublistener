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
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

import org.joda.time.DateTime;
import org.kohsuke.github.GHEventInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;

public class CassandraWriter implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CassandraWriter.class);
	
	private final ArrayBlockingQueue<GHEventInfo> queue;
	private CassandraModel model;
	
	public CassandraWriter(ArrayBlockingQueue<GHEventInfo> queue, CassandraModel model) {
		this.queue = queue;
		this.model = model;
	}
	
	@Override
	public void run() {
		while (!Thread.interrupted()) {
			try {
				GHEventInfo event = queue.take();
				Date date = event.getCreatedAt();
				String project = event.getRepository().getName();
				String user = event.getActorLogin();
				LOGGER.debug("Writing event for project {} and user {}", project, user);
				
				String eventType = (event.getType() == null) ? "" : event.getType().name();
				model.getSession().execute(model.getBatchEventPS().bind(project, user, date, eventType, user, project, date, eventType));

				Date day = new DateTime(date).withTimeAtStartOfDay().toDate();
				Date week = new DateTime(day).withDayOfWeek(Calendar.SUNDAY).toDate();
				Date month = new DateTime(week).withDayOfMonth(1).toDate();
				
				model.getSession().execute(model.getBatchCountPS().bind(project, day, user, day, project, week, user, week, project, month, user, month));
			
			} catch (IOException ioe) {
				LOGGER.error("Failed writing events to cassandra", ioe);
			} catch (NoHostAvailableException nhae) {
				LOGGER.error("Failed to find a cassandra host to write to", nhae);
			} catch (DriverException de) {
				LOGGER.error("Failed writing event to cassandra", de);
			} catch (InterruptedException ioe) {
				return;
			}
		}
	}
}
