package com.jakway.util.runner

import java.io.{File, IOException}

import scala.sys.process.{Process, ProcessLogger}
import scala.util.Try

object Runner {

  sealed trait RunOutput {
    val progName: String
    val args: Seq[String]

    //equivalent to Try.get
    def get(): ProgramOutput = this match {
      case p: ProgramOutput => p
      case e: ExceptionOnRun => throw e
    }

    def toTry: Try[ProgramOutput] = Try(get())
  }

  class ProgramOutput(override val progName: String, override val args: Seq[String],
                      val stdout: String, val stderr: String, val exitCode: Int)
    extends RuntimeException(s"Error while running $progName with args" +
      s" $args\nstdout: $stdout\nstderr: $stderr") with RunOutput

  case class BadExitCode(override val progName: String, override val args: Seq[String],
                         override val stdout: String, override val stderr: String,
                         override val exitCode: Int)
    extends ProgramOutput(progName, args, stdout, stderr, exitCode)
  {

  }

  //used to represent success
  case class ZeroExitCode(override val progName: String, override val args: Seq[String],
                         override val stdout: String, override val stderr: String,
                         override val exitCode: Int)
    extends ProgramOutput(progName, args, stdout, stderr, exitCode)

  class ExceptionOnRun(override val progName: String, override val args: Seq[String],
                       val e: Exception)
    extends RuntimeException(e) with RunOutput

  case class IOExceptionOnRun(override val progName: String, override val args: Seq[String],
                              e: IOException)
    extends ExceptionOnRun(progName, args, e)

  def run(progName: String, args: Seq[String], cwd: Option[File] = None): RunOutput
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

    val exitCode = Try {
    //don't connect stdin
    Process(Seq(progName) ++ args, cwd)
      .run(processLogger, false)
      //block until it returns
      .exitValue()
    } recover {
      case e: IOException =>
    }


    if(exitCode == 0) {
      Right(Unit)
    } else {
      Left(RunOutput(progName, args, stdout, stderr, exitCode))
    }
  }
}
