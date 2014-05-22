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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.kohsuke.github.GHEventInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;

public class GitHubListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GitHubListener.class);
	
	private static final String USERNAME = "username";
	private static final String AUTHTOKEN = "authtoken";
    private static final String ENDPOINTS = "endpoints";
    private static final String NUM_WRITERS = "numwriters";
    private static final String RF = "rc";
    
    private static final ArrayBlockingQueue<GHEventInfo> eventQueue = new ArrayBlockingQueue<>(10000);

    private static String userName;
    private static String authToken;
    private static String[] endPoints;
    private static int numWriters;
    private static int replicationFactor;

	public static void main(String... args) {
		try {
			
			parseOptions(args);
			
			Cluster cluster = new Cluster.Builder().addContactPoints(endPoints).build();		
			
			Thread[] writers = new Thread[numWriters];
			for (int i = 0; i < numWriters; i++) {
				writers[i] = new Thread(new CassandraWriter(eventQueue, cluster, replicationFactor));
				writers[i].setName("Cassandra Writer #" + i + 1);
				writers[i].setDaemon(true);
				writers[i].start();
			}
			
			Runnable eventPoller = new EventPoller(eventQueue, userName, authToken);
			Thread ept = new Thread(eventPoller);
			ept.setName("Event Poller");
			ept.setDaemon(true);
			ept.start();
			
			System.out.println("Type enter to exit");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			br.readLine();
			
			synchronized(ept) {
				try {
					ept.interrupt();
					ept.join();
				} catch (InterruptedException ie) {
				}
			}
			
			for (int i = 0; i < numWriters; i++) {
				synchronized(writers[i]) {
					try {
						writers[i].interrupt();
						writers[i].join();
					} catch (InterruptedException ie) {
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("Failed to initialize connection to github", e);
		} catch (ParseException e) {
			LOGGER.error("Failed to parse command line arguments", e);
		}
	}
	
	public static void parseOptions(String...args) throws ParseException {
        Options options = createOptions();
        CommandLineParser parser = new GnuParser();
        CommandLine cmdLine = parser.parse(options, args);
        
        userName = cmdLine.getOptionValue(USERNAME);
        authToken = cmdLine.getOptionValue(AUTHTOKEN);
        endPoints = cmdLine.getOptionValues(ENDPOINTS);
        if (endPoints == null) {
        	endPoints = new String[] {"127.0.0.1"};
        }
        
        String nw = cmdLine.getOptionValue(NUM_WRITERS);
        try {
        	numWriters = Integer.valueOf(nw);
        } catch (Exception e) {
        	numWriters = Runtime.getRuntime().availableProcessors();
        }
        
        String rf = cmdLine.getOptionValue(RF);
        try {
        	replicationFactor = Integer.valueOf(rf);
        } catch (Exception e) {
        	replicationFactor = 1;
        }
	}
	
    private static Options createOptions() {
        Options options = new Options();

        Option option = new Option(USERNAME, true, "github user name");
        option.setRequired(true);
        options.addOption(option);
        
        option = new Option(AUTHTOKEN, true, "github auth token");
        option.setRequired(true);
        options.addOption(option);
        
        option = new Option(ENDPOINTS, true, "space separated list of cassandra server server/ports");
        option.setOptionalArg(true);
        option.setRequired(false);
        option.setArgs(100);
        options.addOption(option);
    
        option = new Option(NUM_WRITERS, true, "number of cassandra writers[default=1]");
        option.setRequired(false);
        options.addOption(option);
        
        option = new Option(RF, true, "replication factor[default=1]");
        option.setRequired(false);
        options.addOption(option);

        return options;
    }
}
