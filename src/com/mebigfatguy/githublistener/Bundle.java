package com.mebigfatguy.githublistener;

import java.util.Locale;
import java.util.ResourceBundle;

public enum Bundle {

	Title,
	Description,
	ProjectsByMonth,
	ProjectsByWeek,
	ProjectsByDay,
	UsersByMonth,
	UsersByWeek,
	UsersByDay,
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
