package com.jakway.tools

import java.io.File

import scala.sys.process.{Process, ProcessLogger}

object Runner {

  case class RunOutput(progName: String, args: Seq[String],
                       stdout: String, stderr: String, exitCode: Int)
    extends RuntimeException(s"Error while running $progName with args $args\nstdout: $stdout\nstderr: $stderr")

  def run(progName: String, args: Seq[String], cwd: Option[File] = None): Either[RunOutput, Unit]
  = {
    var stdout = ""
    var stderr = ""

    def appendStdout(line: String): Unit = {
      stdout = stdout + line + "\n"
    }
    def appendStderr(line: String): Unit = {
      stderr = stderr + line + "\n"
    }
    val processLogger = ProcessLogger(appendStdout, appendStderr)

    //don't connect stdin
    val exitCode = Process(Seq(progName) ++ args, cwd)
      .run(processLogger, false)
      //block until it returns
      .exitValue()

    if(exitCode == 0) {
      Right(Unit)
    } else {
      Left(RunOutput(progName, args, stdout, stderr, exitCode))
    }
  }
}
