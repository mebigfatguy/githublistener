githublistener
==============

A simple web app that shows most active user and projects on github by day, week and month.

Uses Cassandra as the events store, using the data-stax cql driver.

the web app uses jndi to pull various settings from java:/comp/env so you will have to set these up in the container of your choice. In Tomcat for instance you would create a file called

    githublistener.xml

in ${TOMCAT_HOME}/conf/Catalina/localhost

with the contents

    <Context docBase="/home/dave/dev/githublistener/target/war">

        <Environment name="username" value="yourusername" type="java.lang.String" override="false"/>
        <Environment name="authtoken" value="1111111yourauthtoken111111111111" type="java.lang.String" override="false"/>
        <Environment name="numwriters" value="10" type="java.lang.Integer" override="false"/>
        <Environment name="endpoints" value="192.168.1.100,192.168.1.200" type="java.lang.String" override="false"/>
        <Environment name="replicationfactor" value="2" type="java.lang.Integer" override="false"/>
        <Environment name="eventweights" value="{'PUSH' : 2}" type="java.lang.String" override="false"/>
    </Context>

Or with the settings you would like.

Only the options username and authtoken are required.

If not set the other options default to:

    numwriters        : 10
    endpoints         : 127.0.0.1
    replicationfactor : 1
    eventweights      : 1 for all values


Pointing your browser then at

http://localhost:8008/githublistener will give you the statistics page that you can pull the information from.

The Github api seems a bit flaky with regards to pulling events, sometimes it flows normally, sometimes it appears the call hangs up a bit on their servers. Not sure what is going on there.
