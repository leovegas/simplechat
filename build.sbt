name := "SimpleWebChat"
 
version := "1.0" 
      
lazy val `simplewebchat` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

version := "2.8.x"

scalaVersion := "2.13.2"

//PlayKeys.devSettings += "akka.http.server.websocket.periodic-keep-alive-max-idle" -> "5 seconds"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test ,
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
  // https://mvnrepository.com/artifact/org.json4s/json4s-native
  "org.json4s" %% "json4s-native" % "3.7.0-M2",
  "org.json4s" %% "json4s-jackson" % "3.7.0-M2",
  "com.typesafe.play" %% "play-json" % "2.9.1",
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.typesafe.slick" %% "slick-codegen" % "3.3.3",
  "org.postgresql" % "postgresql" % "42.2.16",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
  "org.mindrot" % "jbcrypt" % "0.4")

libraryDependencies += "com.typesafe.play" %% "play-mailer" % "8.0.1"
libraryDependencies += "com.typesafe.play" %% "play-mailer-guice" % "8.0.1"


scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

      