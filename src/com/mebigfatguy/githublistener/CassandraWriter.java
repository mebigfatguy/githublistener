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
import java.util.concurrent.atomic.AtomicBoolean;

import org.joda.time.DateTime;
import org.kohsuke.github.GHEventInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AlreadyExistsException;

public class CassandraWriter implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CassandraWriter.class);
	private static final Object LOCK = new Object();
	private static boolean IS_INITIALIZED = false;
	private static Session session;
	private static PreparedStatement addBatchEventPS;
	private static PreparedStatement incProjectDayCountPS;
	private static PreparedStatement incUserDayCountPS;
	private static PreparedStatement incProjectWeekCountPS;
	private static PreparedStatement incUserWeekCountPS;
	
	private final ArrayBlockingQueue<GHEventInfo> eventQueue;
	
	public CassandraWriter(ArrayBlockingQueue<GHEventInfo> queue, Cluster cluster, int replicationFactor) {
		eventQueue = queue;
		
		synchronized(LOCK) {
			if (!IS_INITIALIZED) {
				session = cluster.connect();
				setUpSchema(replicationFactor);
				setUpStatements();
				IS_INITIALIZED = true;
			}
		}
	}
	
	@Override
	public void run() {
		while (!Thread.interrupted()) {
			try {
				GHEventInfo event = eventQueue.take();
				Date date = event.getCreatedAt();
				String project = event.getRepository().getName();
				String user = event.getActorLogin();
				String eventType = (event.getType() == null) ? "" : event.getType().name();
				session.execute(addBatchEventPS.bind(project, user, date, eventType, user, project, date, eventType));

				Date day = new DateTime(date).withTimeAtStartOfDay().toDate();
				session.execute(incProjectDayCountPS.bind(project, day));
				session.execute(incUserDayCountPS.bind(user, day));
				
				Date week = new DateTime(day).withDayOfWeek(Calendar.SUNDAY).toDate();
				session.execute(incProjectWeekCountPS.bind(project, week));
				session.execute(incUserWeekCountPS.bind(user, week));
			
			} catch (IOException ioe) {
				LOGGER.error("Failed writing events to cassandra", ioe);
			} catch (InterruptedException ioe) {
				return;
			}
		}
	}
	
	private void setUpSchema(int replicationFactor) {

        try {
            session.execute(String.format("CREATE KEYSPACE github WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : %d }", replicationFactor));
        } catch (AlreadyExistsException aee) {
        } finally {
            session.execute("use github");
        }

        try {
            session.execute("CREATE TABLE github.project_events (project text, user text, date_time timestamp, type text, primary key(project, user))");
            session.execute("CREATE TABLE github.user_events (user text, project text, date_time timestamp, type text, primary key(user, project))");
            session.execute("CREATE TABLE github.project_day_counts (project text, date timestamp, count counter, primary key (date, project))");
            session.execute("CREATE TABLE github.user_day_counts (user text, date timestamp, count counter, primary key (date, user))");
            session.execute("CREATE TABLE github.project_week_counts (project text, date timestamp, count counter, primary key (date, project))");
            session.execute("CREATE TABLE github.user_week_counts (user text, date timestamp, count counter, primary key (date, user))");
        } catch (AlreadyExistsException aee) {
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	private void setUpStatements() {
		addBatchEventPS = session.prepare(
									"BEGIN BATCH " +
									"insert into github.project_events (project, user, date_time, type) values (?,?,?,?) " +
									"insert into github.user_events (user, project, date_time, type) values (?,?,?,?)" +
									"APPLY BATCH"
									);

		incProjectDayCountPS = session.prepare("update github.project_day_counts set count = count + 1 where project = ? and date = ?");	
    	incUserDayCountPS = session.prepare("update github.user_day_counts set count = count + 1 where user = ? and date = ?");
    	incProjectWeekCountPS = session.prepare("update github.project_week_counts set count = count + 1 where project = ? and date = ?");	
    	incUserWeekCountPS = session.prepare("update github.user_week_counts set count = count + 1 where user = ? and date = ?");

	}
}
