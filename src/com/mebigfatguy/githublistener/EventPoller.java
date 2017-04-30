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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.kohsuke.github.GHEventInfo;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.extras.OkHttpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

public class EventPoller implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventPoller.class);
    private static final long POLL_TIME = (60L * 60L * 1000L) / 6000L;
    private static final long FAILURE_SLEEP_TIME = 10L * 1000L;

    private final ArrayBlockingQueue<GHEventInfo> eventQueue;
    private final GitHub github;

    public EventPoller(ArrayBlockingQueue<GHEventInfo> queue, String userName, String authToken) throws IOException {
        eventQueue = queue;

        github = GitHub.connect(userName, authToken);
        github.setConnector(new OkHttpConnector(new OkUrlFactory(new OkHttpClient())));
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                LOGGER.debug("Attempting to pull events from github");
                List<GHEventInfo> currentEvents = github.getEvents();
                eventQueue.addAll(currentEvents);
                LOGGER.debug("Event queue size is {}", Integer.valueOf(eventQueue.size()));
                Thread.sleep(POLL_TIME);
            } catch (InterruptedException e) {
                return;
            } catch (IOException ioe) {
                LOGGER.error("Failed fetching events from github", ioe);
            } catch (IllegalStateException ise) {
                LOGGER.error("Failed queueing events from github - queue full", ise);
                try {
                    Thread.sleep(FAILURE_SLEEP_TIME);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
}
