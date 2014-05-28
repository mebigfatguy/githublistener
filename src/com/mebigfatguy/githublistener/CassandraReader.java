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

import com.datastax.driver.core.Session;
import com.mebigfatguy.githublistener.rest.ItemCount;

public class CassandraReader {

	private Session session;
	
	public CassandraReader(Session session) {
		this.session = session;
	}
	
	public ItemCount[] getTopProjectsByMonth() {
		ItemCount item = new ItemCount();
		item.setName("proj1");
		item.setCount(20);
		
		return new ItemCount[] { item };
	}

	public ItemCount[] getTopProjectsByWeek() {
		ItemCount item = new ItemCount();
		item.setName("proj1");
		item.setCount(20);
		
		return new ItemCount[] { item };
	}

	public ItemCount[] getTopProjectsByDay() {
		ItemCount item = new ItemCount();
		item.setName("proj1");
		item.setCount(20);
		
		return new ItemCount[] { item };
	}

	public ItemCount[] getTopUsersByMonth() {
		ItemCount item = new ItemCount();
		item.setName("user1");
		item.setCount(20);
		
		return new ItemCount[] { item };
	}

	public ItemCount[] getTopUsersByWeek() {
		ItemCount item = new ItemCount();
		item.setName("user1");
		item.setCount(20);
		
		return new ItemCount[] { item };
	}

	public ItemCount[] getTopUsersByDay() {
		ItemCount item = new ItemCount();
		item.setName("user1");
		item.setCount(20);
		
		return new ItemCount[] { item };
	}
}
