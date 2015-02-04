package ideaplugin

import sbt._
import sbt.Keys._

object Keys {
  lazy val ideaVersion = SettingKey[String]("idea-version",
    "Version of Intellij IDEA to build against")
  lazy val ideaBaseDirectory = SettingKey[File]("idea-base-directory",
    "Directory where downloaded IDEA is unpacked")
  lazy val ideaPlugins = SettingKey[Seq[String]]("idea-plugins",
    "List of Intellij IDEA plugins this project depends on")

  lazy val ideaMainJars = TaskKey[Classpath]("idea-main-jars",
    "Classpath containing main IDEA jars")
  lazy val ideaPluginJars = TaskKey[Classpath]("idea-plugin-jars",
    "Classpath containing jars of IDEA plugins")
  lazy val ideaFullJars = TaskKey[Classpath]("idea-full-jars",
    "Concatenation of idea-main-jars and idea-community-jars")

  lazy val updateIdea = TaskKey[Unit]("update-idea",
    "Download Intellij IDEA binaries and sources for specified version")

  lazy val ideaPluginSettings: Seq[Setting[_]] = Seq(
    ideaBaseDirectory := baseDirectory.value / "idea",
    ideaPlugins       := Seq.empty,

    ideaMainJars   := (ideaBaseDirectory.value / ideaVersion.value / "lib" * "*.jar").classpath,
    ideaPluginJars := {
      val dirs = ideaPlugins.value.foldLeft(PathFinder.empty){ (paths, plugin) =>
        paths +++ (ideaBaseDirectory.value / ideaVersion.value / "plugins" / plugin / "lib")
      }
      (dirs * (globFilter("*.jar") -- "*asm*.jar")).classpath
    },
    ideaFullJars := ideaMainJars.value ++ ideaPluginJars.value,
    unmanagedJars in Compile ++= ideaFullJars.value,

    updateIdea <<= (ideaBaseDirectory, ideaVersion, streams).map(Tasks.updateIdea)
  )

}