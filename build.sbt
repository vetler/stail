name := "sshlog"

version := "1.0"

scalaVersion := "2.9.0"

// mainClass := Some("SSHLog")

seq(webSettings :_*)

libraryDependencies ++= Seq(
        "org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "jetty",
        "ch.qos.logback" % "logback-classic" % "0.9.26",
        "javax.servlet" % "servlet-api" % "2.5" % "provided->default"
)

libraryDependencies += "org.fusesource.scalate" % "scalate-core" % "1.5.0"
