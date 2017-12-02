package com.jakway.tools

import com.jakway.tools.Runner.RunOutput
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Try

case class CommandNotFoundException(val commandName: String)
  extends RuntimeException(s"Could not find command `$commandName` on $$PATH")

object CheckCommandExists {
  def runOrThrow(logger: Logger)(commandName: String, args: Seq[String]): Try[Unit] = {
    Runner.run(commandName, args) match {
      case Left(RunOutput(_, _, stdout, stderr, exitCode)) => {
        logger.warn(s"Error while executing command `$commandName $args`, exit code: $exitCode" +
          s"stdout: $stdout, stderr: $stderr")
        throw CommandNotFoundException(commandName)
      }
      case Right(_) => {}
    }
  }
}

/**
  * ***WARNING: this will run the passed command!***
  * if this isn't OK, use alternative methods like manually searching $PATH
  * @param commandName
  */
class CheckCommandExists(val commandName: String) {
  val logger: Logger = LoggerFactory.getLogger(getClass())
  def runOrThrow = CheckCommandExists.runOrThrow(logger)

  //if any of the following succeed, the program exists
  runOrThrow(commandName, Seq("--help"))
    .recover {
      case _: Throwable => runOrThrow(commandName, Seq())
    }
    .recover {
      case _: Throwable => runOrThrow(commandName, Seq("-help"))
    }.get
}