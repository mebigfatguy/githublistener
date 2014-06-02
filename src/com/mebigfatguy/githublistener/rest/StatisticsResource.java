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

import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.kohsuke.github.GHEvent;

import com.mebigfatguy.githublistener.CassandraModel;
import com.mebigfatguy.githublistener.CassandraReader;

@Path("/statistics")
public class StatisticsResource {
	
	@Context 
	ServletContext context;
	
	@GET
	@Path("/weights")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<GHEvent, Long> getEventWeights() {
		return (Map<GHEvent, Long>) context.getAttribute("weights");
	}

	
	@GET
	@Path("/projects/month")
	@Produces(MediaType.APPLICATION_JSON)
	public Items getTopProjectsByMonth() {
		CassandraReader reader = new CassandraReader((CassandraModel) context.getAttribute("model"));
		
		return new Items("Projects", "Score By Month", reader.getTopProjectsByMonth());
	}
	
	@GET
	@Path("/projects/week")
	@Produces(MediaType.APPLICATION_JSON)
	public Items getTopProjectsByWeek() {
		CassandraReader reader = new CassandraReader((CassandraModel) context.getAttribute("model"));
		
		return new Items("Projects", "Score By Week", reader.getTopProjectsByWeek());
	}
	
	@GET
	@Path("/projects/day")
	@Produces(MediaType.APPLICATION_JSON)
	public Items getTopProjectsByDay() {
		CassandraReader reader = new CassandraReader((CassandraModel) context.getAttribute("model"));
		
		return new Items("Projects", "Score By Day", reader.getTopProjectsByDay());
	}
	
	@GET
	@Path("/users/month")
	@Produces(MediaType.APPLICATION_JSON)
	public Items getTopUsersByMonth() {
		CassandraReader reader = new CassandraReader((CassandraModel) context.getAttribute("model"));
		
		return new Items("Users", "Score By Month", reader.getTopUsersByMonth());
	}
	
	@GET
	@Path("/users/week")
	@Produces(MediaType.APPLICATION_JSON)
	public Items getTopUsersByWeek() {
		CassandraReader reader = new CassandraReader((CassandraModel) context.getAttribute("model"));
		
		return new Items("Users", "Score By Week", reader.getTopUsersByWeek());
	}
	
	@GET
	@Path("/users/day")
	@Produces(MediaType.APPLICATION_JSON)
	public Items getTopUsersByDay() {
		CassandraReader reader = new CassandraReader((CassandraModel) context.getAttribute("model"));
		
		return new Items("Users", "Score By Day", reader.getTopUsersByDay());
	}
}
