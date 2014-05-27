package com.mebigfatguy.githublistener.rest;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class Items {

	private ItemCount[] itemCount;

	public ItemCount[] getItems() {
		if (itemCount == null)
			itemCount = new ItemCount[0];
		
		return itemCount;
	}

	public void setItems(ItemCount... items) {
		itemCount = items;
	}
	
	
}
