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
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

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
	private static final Object SCHEMA_LOCK = new Object();
	private static boolean isInitialized = false;
	
	private final ArrayBlockingQueue<GHEventInfo> eventQueue;
	private final Session session;
	private PreparedStatement addProjectEventPS;
	private PreparedStatement addUserEventPS;
	private PreparedStatement incProjectDayCountPS;
	private PreparedStatement incUserDayCountPS;
	
	public CassandraWriter(ArrayBlockingQueue<GHEventInfo> queue, Cluster cluster, int replicationFactor) {
		eventQueue = queue;
		session = cluster.connect();
		
		setUpSchema(replicationFactor);
		setUpStatements();
	}
	
	@Override
	public void run() {
		while (!Thread.interrupted()) {
			try {
				GHEventInfo event = eventQueue.take();
				Date date = event.getCreatedAt();
				String project = event.getRepository().getName();
				String user = event.getActorLogin();
				session.execute(addProjectEventPS.bind(project, user, date, event.getType().name()));
				session.execute(addUserEventPS.bind(user, project, date, event.getType().name()));

				Date day = new DateTime(date).withTimeAtStartOfDay().toDate();
				session.execute(incProjectDayCountPS.bind(project, day));
				session.execute(incUserDayCountPS.bind(user, day));
			
			} catch (IOException ioe) {
				LOGGER.error("Failed writing events to cassandra", ioe);
			} catch (InterruptedException ioe) {
				return;
			}
		}
	}
	
	private void setUpSchema(int replicationFactor) {
		synchronized (SCHEMA_LOCK) {
			if (!isInitialized) {
		        try {
		            session.execute(String.format("CREATE KEYSPACE github WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : %d }", replicationFactor));
		        } catch (AlreadyExistsException aee) {
		        } finally {
		            session.execute("use github");
		        }
	
		        try {
		            session.execute("CREATE TABLE github.project_events (project text, user text, date_time timestamp, type text, primary key(project, user))");
		            session.execute("CREATE TABLE github.user_events (user text, project text, date_time timestamp, type text, primary key(user, project))");
		            session.execute("CREATE TABLE github.project_day_counts (project text, date timestamp, count counter, primary key (project, date)) WITH CLUSTERING ORDER BY (date DESC)");
		            session.execute("CREATE TABLE github.user_day_counts (user text, date timestamp, count counter, primary key (user, date)) WITH CLUSTERING ORDER BY (date DESC)");
		        } catch (AlreadyExistsException aee) {
		        } catch (Exception e) {
		        	e.printStackTrace();
		        }
		        
		        isInitialized = true;
			}
		}
	}
	
	private void setUpStatements() {
        addProjectEventPS = session.prepare("insert into github.project_events (project, user, date_time, type) values (?,?,?,?)");
        addUserEventPS = session.prepare("insert into github.user_events (user, project, date_time, type) values (?,?,?,?)");
    	incProjectDayCountPS = session.prepare("update github.project_day_counts set count = count + 1 where project = ? and date = ?");	
    	incUserDayCountPS = session.prepare("update github.user_day_counts set count = count + 1 where user = ? and date = ?");
	}
}
