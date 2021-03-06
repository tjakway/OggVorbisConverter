name := "conv-music-ogg-vorbis"
version := "1.0"
scalaVersion := "2.12.2"

resolvers += Resolver.typesafeIvyRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= 
  Seq("org.slf4j" % "slf4j-parent" % "1.7.6",
      "ch.qos.logback"  %  "logback-classic"    % "1.2.1",
      "com.github.scopt" %% "scopt" % "3.5.0",

      //scalatest
      //see http://www.scalatest.org/install
      "org.scalatest" %% "scalatest" % "3.0.4" % "test",
      "org.scalactic" %% "scalactic" % "3.0.4" % "test"
      )


//set the main class
//see https://stackoverflow.com/questions/6467423/how-to-set-main-class-in-build
mainClass in Compile := Some("com.jakway.tools.ConvMusicOggVorbis")

//ignore anything named snippets.scala
excludeFilter in unmanagedSources := HiddenFileFilter || "snippets.scala"

//enable more warnings
scalacOptions in compile ++= Seq("-unchecked", "-deprecation", "-feature")
