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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

public class CassandraReader {
	
	private static int ROWS_GUESSTIMATE = 1000;
	private static int MAX_RESULTS = 25;
	private static ItemCountComparator COMPARATOR = new ItemCountComparator();

	private CassandraModel model;
	
	public CassandraReader(CassandraModel model) {
		this.model = model;
	}
	
	public ItemCount[] getTopProjectsByMonth() {
		Date month = new DateTime().withDayOfMonth(1).withTimeAtStartOfDay().toDate();		
		ResultSet rs = model.getSession().execute(model.getProjectsByMonthPS().bind(month));
		return sortTopItems(rs);
	}

	public ItemCount[] getTopProjectsByWeek() {
		Date week = new DateTime().withDayOfWeek(Calendar.SUNDAY).withTimeAtStartOfDay().toDate();		
		ResultSet rs = model.getSession().execute(model.getProjectsByWeekPS().bind(week));
		return sortTopItems(rs);
	}

	public ItemCount[] getTopProjectsByDay() {
		Date day = new DateTime().withTimeAtStartOfDay().toDate();	
		ResultSet rs = model.getSession().execute(model.getProjectsByDayPS().bind(day));
		return sortTopItems(rs);
	}

	public ItemCount[] getTopUsersByMonth() {
		Date month = new DateTime().withDayOfMonth(1).withTimeAtStartOfDay().toDate();		
		ResultSet rs = model.getSession().execute(model.getUsersByMonthPS().bind(month));
		return sortTopItems(rs);
	}

	public ItemCount[] getTopUsersByWeek() {
		Date week = new DateTime().withDayOfWeek(Calendar.SUNDAY).withTimeAtStartOfDay().toDate();		
		ResultSet rs = model.getSession().execute(model.getUsersByWeekPS().bind(week));
		return sortTopItems(rs);
	}

	public ItemCount[] getTopUsersByDay() {
		Date day = new DateTime().withTimeAtStartOfDay().toDate();	
		ResultSet rs = model.getSession().execute(model.getUsersByDayPS().bind(day));
		return sortTopItems(rs);
	}
	
	private ItemCount[] sortTopItems(ResultSet rs) {
		List<ItemCount> items = new ArrayList<ItemCount>(ROWS_GUESSTIMATE);
		
		for (Row row : rs) {
			ItemCount item = new ItemCount(row.getString(0), row.getLong(1));
			items.add(item);
		}
		
		Collections.sort(items, COMPARATOR);
		List<ItemCount> subList = (items.size() > MAX_RESULTS) ? items.subList(0,  MAX_RESULTS) : items;
		
		return subList.toArray(new ItemCount[subList.size()]);
	}

}
