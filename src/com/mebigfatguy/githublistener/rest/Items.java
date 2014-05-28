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

import javax.xml.bind.annotation.XmlRootElement;

import com.mebigfatguy.githublistener.ItemCount;

@XmlRootElement()
public class Items {

	private String groupName;
	private String countName;
	private ItemCount[] itemCount;
	
	public Items() {
	}
	
	public Items(String groupName, String countName, ItemCount...itemCount) {
		this.groupName = groupName;
		this.countName = countName;
		this.itemCount = itemCount;
	}
	
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getCountName() {
		return countName;
	}

	public void setCountName(String countName) {
		this.countName = countName;
	}

	public ItemCount[] getItems() {
		if (itemCount == null)
			itemCount = new ItemCount[0];
		
		return itemCount;
	}

	public void setItems(ItemCount... items) {
		itemCount = items;
	}
	
	
}
