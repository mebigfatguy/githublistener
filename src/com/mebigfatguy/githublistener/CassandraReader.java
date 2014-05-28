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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
		ResultSet rs = model.getSession().execute(model.getProjectsByMonthPS().bind());
		return sortTopItems(rs);
	}

	public ItemCount[] getTopProjectsByWeek() {
		ResultSet rs = model.getSession().execute(model.getProjectsByWeekPS().bind());
		return sortTopItems(rs);
	}

	public ItemCount[] getTopProjectsByDay() {
		ResultSet rs = model.getSession().execute(model.getProjectsByDayPS().bind());
		return sortTopItems(rs);
	}

	public ItemCount[] getTopUsersByMonth() {
		ResultSet rs = model.getSession().execute(model.getUsersByMonthPS().bind());
		return sortTopItems(rs);
	}

	public ItemCount[] getTopUsersByWeek() {
		ResultSet rs = model.getSession().execute(model.getUsersByWeekPS().bind());
		return sortTopItems(rs);
	}

	public ItemCount[] getTopUsersByDay() {
		ResultSet rs = model.getSession().execute(model.getUsersByDayPS().bind());
		return sortTopItems(rs);
	}
	
	public ItemCount[] sortTopItems(ResultSet rs) {
		List<ItemCount> items = new ArrayList<ItemCount>(ROWS_GUESSTIMATE);
		
		for (Row row : rs) {
			ItemCount item = new ItemCount(row.getString(0), row.getLong(1));
			items.add(item);
		}
		
		Collections.sort(items, COMPARATOR);
		List<ItemCount> subList = items.subList(0,  MAX_RESULTS);
		
		return subList.toArray(new ItemCount[subList.size()]);
	}

}
