package com.mebigfatguy.githublistener;

import java.io.Serializable;
import java.util.Comparator;

public class ItemCountComparator implements Comparator<ItemCount>, Serializable {

	private static final long serialVersionUID = 767673428110498992L;

	@Override
	public int compare(ItemCount o1, ItemCount o2) {
		long c1 = o1.getCount();
		long c2 = o2.getCount();
		
		if (c1 > c2)
			return -1;
		else if (c1 < c2)
			return 1;
		return 0;
	}
}
