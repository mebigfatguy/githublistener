githublistener
==============

A simple web app that will show most active user and projects on github by day, week and month.

This is a work in progress.

Uses Cassandra as the events store.

the web app uses jndi to pull various settings from java:/comp/env so you will have to set these up in the container of your choice. In Tomcat for instance you would create a file called

githublistener.xml

in ${TOMCAT_HOME}/conf/Catalina/localhost

with the contents

{code}
<Context docBase="/home/dave/dev/githublistener/target/war">

	<Environment name="username" value="mebigfatguy" type="java.lang.String" override="false"/>
	<Environment name="authtoken" value="1a805105835ea70a478d0d36231ecaba0c040a8f" type="java.lang.String" override="false"/>
	<Environment name="numwriters" value="10" type="java.lang.Integer" override="false"/>
	<Environment name="endpoints" value="192.168.1.100,192.168.1.200" type="java.lang.String" override="false"/>
	<Environment name="replicationfactor" value="2" type="java.lang.Integer" override="false"/>


</Context>
{code}

Or with the settings you would like.
