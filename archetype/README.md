In order to generate a jPOS application based on this archetype, you need to: 

    mvn install 

then, on another directory call:

    mvn -U archetype:generate -DarchetypeCatalog=local -Dfilter=jpos-archetype

alternatively, you can pull the dependencies from jPOS' Maven repository using:

    mvn -U archetype:generate -DarchetypeRepository=http://jpos.org/maven -Dfilter=jpos-archetype 


