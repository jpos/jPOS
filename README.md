## Build

    mvn package
    
Alternatively, if you want to skip the unit tests, you can:

    mvn -Dmaven.test.skip=true package

## Run

    cd jpos/target/jpos-x.x.x
    java -jar jpos-x.x.x.jar
    
    
## Create your own project

    mvn archetype:generate \
      -DarchetypeGroupId=org.jpos \
      -DarchetypeArtifactId=jpos-archetype \
      -DarchetypeVersion=1.8.6 \
      -DarchetypeRepository=http://jpos.org/maven

## Install locally

    mvn install
    
## Maven POM

    <repository>
      <id>jpos</id>
      <name>jPOS Central Repository</name>
      <url> http://jpos.org/maven </url>
      <layout>default</layout>
    </repository>

    <dependency>
      <groupId>org.jpos</groupId>
      <artifactId>jpos</artifactId>
      <version>1.8.6</version>
    </dependency>


----
See the [ChangeLog:](http://jpos.org/wiki/ChangeLog) or visit the [Resources](http://jpos.org/resources) page for additional information.


