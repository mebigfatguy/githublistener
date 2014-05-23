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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;

public class CassandraWriter implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CassandraWriter.class);
	private static final Object LOCK = new Object();
	private static boolean IS_INITIALIZED = false;
	private static Session session;
	private static PreparedStatement addBatchEventPS;
	private static PreparedStatement incBatchCountPS;
	
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
				Date week = new DateTime(day).withDayOfWeek(Calendar.SUNDAY).toDate();
				Date month = new DateTime(week).withDayOfMonth(1).toDate();
				
				session.execute(incBatchCountPS.bind(project, day, user, day, project, week, user, week, project, month, user, month));
			
			} catch (IOException ioe) {
				LOGGER.error("Failed writing events to cassandra", ioe);
			} catch (NoHostAvailableException nhae) {
				LOGGER.error("Failed to find a cassandra host to write to", nhae);
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
            session.execute("CREATE TABLE github.project_month_counts (project text, date timestamp, count counter, primary key (date, project))");
            session.execute("CREATE TABLE github.user_month_counts (user text, date timestamp, count counter, primary key (date, user))");
        } catch (AlreadyExistsException aee) {
        } catch (Exception e) {
        	LOGGER.error("Failed creating tables for github events", e);
        }
	}
	
	private void setUpStatements() {
		addBatchEventPS = session.prepare(
									"BEGIN BATCH " +
									"insert into github.project_events (project, user, date_time, type) values (?,?,?,?) " +
									"insert into github.user_events (user, project, date_time, type) values (?,?,?,?)" +
									"APPLY BATCH"
									);
		
		incBatchCountPS = session.prepare(
									"BEGIN COUNTER BATCH " + 
									"update github.project_day_counts set count = count + 1 where project = ? and date = ?"	+
									"update github.user_day_counts set count = count + 1 where user = ? and date = ?" +
									"update github.project_week_counts set count = count + 1 where project = ? and date = ?" +
									"update github.user_week_counts set count = count + 1 where user = ? and date = ?" +
									"update github.project_month_counts set count = count + 1 where project = ? and date = ?" +
									"update github.user_month_counts set count = count + 1 where user = ? and date = ?" +
									"APPLY BATCH"
									);
	}
}
