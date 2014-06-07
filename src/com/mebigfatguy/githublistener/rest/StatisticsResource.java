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

import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.kohsuke.github.GHEvent;

import com.mebigfatguy.githublistener.Bundle;
import com.mebigfatguy.githublistener.CassandraModel;
import com.mebigfatguy.githublistener.CassandraReader;

@Path("/statistics")
public class StatisticsResource {
	
	@Context 
	ServletContext context;
	
	@GET
	@Path("/text")
	@Produces(MediaType.APPLICATION_JSON)
	public PageText getText(@Context HttpServletRequest request) {
		return new PageText(request.getLocale());
	}
	
	@GET
	@Path("/weights")
	@Produces(MediaType.APPLICATION_JSON)
	@SuppressWarnings("unchecked")
	public Map<GHEvent, Long> getEventWeights() {
		return (Map<GHEvent, Long>) context.getAttribute("weights");
	}

	
	@GET
	@Path("/projects/month")
	@Produces(MediaType.APPLICATION_JSON)
	public Items getTopProjectsByMonth(@Context HttpServletRequest request) {
		Locale locale = request.getLocale();
		
		CassandraReader reader = new CassandraReader((CassandraModel) context.getAttribute("model"));
		
		return new Items(Bundle.getString(locale, Bundle.Projects), Bundle.getString(locale,  Bundle.ScoreByMonth), reader.getTopProjectsByMonth());
	}
	
	@GET
	@Path("/projects/week")
	@Produces(MediaType.APPLICATION_JSON)
	public Items getTopProjectsByWeek(@Context HttpServletRequest request) {
		Locale locale = request.getLocale();
		
		CassandraReader reader = new CassandraReader((CassandraModel) context.getAttribute("model"));
		
		return new Items(Bundle.getString(locale, Bundle.Projects), Bundle.getString(locale, Bundle.ScoreByWeek), reader.getTopProjectsByWeek());
	}
	
	@GET
	@Path("/projects/day")
	@Produces(MediaType.APPLICATION_JSON)
	public Items getTopProjectsByDay(@Context HttpServletRequest request) {
		Locale locale = request.getLocale();
		
		CassandraReader reader = new CassandraReader((CassandraModel) context.getAttribute("model"));
		
		return new Items(Bundle.getString(locale, Bundle.Projects), Bundle.getString(locale, Bundle.ScoreByDay), reader.getTopProjectsByDay());
	}
	
	@GET
	@Path("/users/month")
	@Produces(MediaType.APPLICATION_JSON)
	public Items getTopUsersByMonth(@Context HttpServletRequest request) {
		Locale locale = request.getLocale();
		
		CassandraReader reader = new CassandraReader((CassandraModel) context.getAttribute("model"));
		
		return new Items(Bundle.getString(locale, Bundle.Users), Bundle.getString(locale,  Bundle.ScoreByMonth), reader.getTopUsersByMonth());
	}
	
	@GET
	@Path("/users/week")
	@Produces(MediaType.APPLICATION_JSON)
	public Items getTopUsersByWeek(@Context HttpServletRequest request) {
		Locale locale = request.getLocale();
		
		CassandraReader reader = new CassandraReader((CassandraModel) context.getAttribute("model"));
		
		return new Items(Bundle.getString(locale, Bundle.Users), Bundle.getString(locale, Bundle.ScoreByWeek), reader.getTopUsersByWeek());
	}
	
	@GET
	@Path("/users/day")
	@Produces(MediaType.APPLICATION_JSON)
	public Items getTopUsersByDay(@Context HttpServletRequest request) {
		Locale locale = request.getLocale();
		
		CassandraReader reader = new CassandraReader((CassandraModel) context.getAttribute("model"));
		
		return new Items(Bundle.getString(locale, Bundle.Users), Bundle.getString(locale, Bundle.ScoreByDay), reader.getTopUsersByDay());
	}
}
