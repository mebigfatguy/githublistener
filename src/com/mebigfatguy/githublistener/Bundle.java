package com.mebigfatguy.githublistener;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public enum Bundle {

	Users,
	Projects,
	ScoreByDay,
	ScoreByWeek,
	ScoreByMonth;
	
	public static String getString(Locale locale, Bundle key) {
		
		ResourceBundle bundle = ResourceBundle.getBundle("com/mebigfatguy/githublistener/bundle", locale);
		return bundle.getString(key.name());
	}
}
