/** githublistener - an github events recording using cassandra. 
 * Copyright 2014-2015 MeBigFatGuy.com 
 * Copyright 2014-2015 Dave Brosius 
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AlreadyExistsException;

public class CassandraModel {
	private static final Logger LOGGER = LoggerFactory.getLogger(CassandraModel.class);
	
	Cluster cluster;
	private Session session;
	private PreparedStatement addBatchEventPS;
	private PreparedStatement incBatchCountPS;
	private PreparedStatement projectsByMonthPS;
	private PreparedStatement projectsByWeekPS;
	private PreparedStatement projectsByDayPS;
	private PreparedStatement usersByMonthPS;
	private PreparedStatement usersByWeekPS;
	private PreparedStatement usersByDayPS;
	
	
	public CassandraModel(String[] endPoints, Integer replicationFactor) {
		
		cluster = new Cluster.Builder().addContactPoints(endPoints).build();
		session = cluster.connect();

		setUpSchema(replicationFactor);
		setUpStatements();
	}
	
	public void close() {
		try {
			session.close();
			cluster.close();
		} catch (Exception e) {
			LOGGER.error("Failed closing cassandra cluster", e);
		}
	}
	
	public Session getSession() {
		return session;
	}

	public PreparedStatement getBatchEventPS() {
		return addBatchEventPS;
	}
	
	public PreparedStatement getBatchCountPS() {
		return incBatchCountPS;
	}
	
	public PreparedStatement getProjectsByMonthPS() {
		return projectsByMonthPS;
	}
	
	public PreparedStatement getProjectsByWeekPS() {
		return projectsByWeekPS;
	}
	
	public PreparedStatement getProjectsByDayPS() {
		return projectsByDayPS;
	}
	
	public PreparedStatement getUsersByMonthPS() {
		return usersByMonthPS;
	}
	
	public PreparedStatement getUsersByWeekPS() {
		return usersByWeekPS;
	}
	
	public PreparedStatement getUsersByDayPS() {
		return usersByDayPS;
	}

	
	private void setUpSchema(Integer replicationFactor) {

        try {
            session.execute(String.format("CREATE KEYSPACE github WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : %d }", replicationFactor));
        } catch (AlreadyExistsException aee) {
        	//an expected event, this is good
        } finally {
            session.execute("use github");
        }

        try {
            session.execute("CREATE TABLE github.project_events (project text, user text, date_time timestamp, type text, primary key(project, user, date_time)) WITH CLUSTERING ORDER BY (user ASC, date_time desc)");
            session.execute("CREATE TABLE github.user_events (user text, project text, date_time timestamp, type text, primary key(user, project, date_time)) WITH CLUSTERING ORDER BY (project ASC, date_time desc)");
            session.execute("CREATE TABLE github.project_day_counts (project text, date timestamp, count counter, primary key (date, project))");
            session.execute("CREATE TABLE github.user_day_counts (user text, date timestamp, count counter, primary key (date, user))");
            session.execute("CREATE TABLE github.project_week_counts (project text, date timestamp, count counter, primary key (date, project))");
            session.execute("CREATE TABLE github.user_week_counts (user text, date timestamp, count counter, primary key (date, user))");
            session.execute("CREATE TABLE github.project_month_counts (project text, date timestamp, count counter, primary key (date, project))");
            session.execute("CREATE TABLE github.user_month_counts (user text, date timestamp, count counter, primary key (date, user))");
        } catch (AlreadyExistsException aee) {
        	//an expected event, this is good
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
									"update github.project_day_counts set count = count + ? where project = ? and date = ?"	+
									"update github.user_day_counts set count = count + ? where user = ? and date = ?" +
									"update github.project_week_counts set count = count + ? where project = ? and date = ?" +
									"update github.user_week_counts set count = count + ? where user = ? and date = ?" +
									"update github.project_month_counts set count = count + ? where project = ? and date = ?" +
									"update github.user_month_counts set count = count + ? where user = ? and date = ?" +
									"APPLY BATCH"
									);
		
		projectsByMonthPS = session.prepare("select project, count from project_month_counts where date = ?");
		projectsByWeekPS = session.prepare("select project, count from project_week_counts where date = ?");
		projectsByDayPS = session.prepare("select project, count from project_day_counts where date = ?");
		usersByMonthPS = session.prepare("select user, count from user_month_counts where date = ?");
		usersByWeekPS = session.prepare("select user, count from user_week_counts where date = ?");
		usersByDayPS = session.prepare("select user, count from user_day_counts where date = ?");
	}
}
