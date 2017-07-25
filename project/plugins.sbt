logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.16")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.9.0")

addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.1.1")

//wartremoverErrors ++= Warts.allBut(Wart.Any, Wart.Nothing, Wart.Serializable)

//wartremoverWarnings += Wart.Nothing

//wartremoverWarnings ++= Seq(Wart.Any, Wart.Serializable)




