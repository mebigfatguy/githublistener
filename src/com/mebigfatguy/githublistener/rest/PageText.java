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

import com.mebigfatguy.githublistener.Bundle;

public class PageText {
	
	private Locale locale;
	
	public PageText(Locale pageLocale) {
		locale = pageLocale;
	}
	
	public String getTitle() {
		return Bundle.getString(locale, Bundle.Title);
	}
	
	public String getDescription() {
		return Bundle.getString(locale, Bundle.Description);
	}
}
