name := """GeoTxtWeb"""

version := "3.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)

libraryDependencies ++= Seq(
 "edu.stanford.nlp" % "stanford-corenlp" % "3.9.1",
 "edu.illinois.cs.cogcomp" % "illinois-ner" % "4.0.3",
 "org.apache.opennlp" % "opennlp-tools" % "1.8.4",
 "uk.ac.gate" % "gate-core" % "8.4.1",
 "org.postgresql" % "postgresql" % "42.2.1",
 "org.apache.solr" % "solr-solrj" % "6.6.0",
 "com.googlecode.json-simple" % "json-simple" % "1.1.1",
 "org.twitter4j" % "twitter4j-core" % "4.0.6",
 "org.twitter4j" % "twitter4j-stream" % "4.0.6",
 "org.apache.commons" % "commons-csv" % "1.5",
 "de.julielab" % "aliasi-lingpipe" % "4.1.0",
 "commons-httpclient" % "commons-httpclient" % "3.1",
 "org.apache.directory.studio" % "org.apache.commons.collections" % "3.2.1",
 "com.bericotech" % "clavin" % "2.1.0",
 "edu.mit.ll" % "mitie" % "0.8" 
)

resolvers += "CogcompSoftware" at "http://cogcomp.cs.illinois.edu/m2repo/"