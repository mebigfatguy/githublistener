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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/statistics")
public class StatisticsResource {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String status() {
		return "Statistics REST resource is operational";
	}
	
	@GET
	@Path("/projects/month")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTopProjectsByMonth() {
		return "";
	}
	
	@GET
	@Path("/projects/week")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTopProjectsByWeek() {
		return "";
	}
	
	@GET
	@Path("/projects/day")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTopProjectsByDay() {
		return "";
	}
	
	@GET
	@Path("/users/month")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTopUsersByMonth() {
		return "";
	}
	
	@GET
	@Path("/users/week")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTopUsersByWeek() {
		return "";
	}
	
	@GET
	@Path("/users/day")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTopUsersByDay() {
		return "";
	}
}
