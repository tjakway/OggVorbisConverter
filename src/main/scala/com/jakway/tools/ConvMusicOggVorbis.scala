package com.jakway.tools

import java.io.{File, IOException, PrintWriter, StringWriter}
import java.nio.file.{FileVisitResult, Files, Path}
import java.nio.file.attribute.BasicFileAttributes

import org.slf4j
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success, Try}

case class Args(inputDir: File, outputDir: File)

object Util {
  def removeFilenameExtension(fname: String): String = {
    fname.substring(0, fname.lastIndexOf('.'))
  }
  /**
    * see https://stackoverflow.com/questions/1149703/how-can-i-convert-a-stack-trace-to-a-string
    * @return
    */
  def throwableToString(t: Throwable, maxLines: Int = 15) = {
    val sw: StringWriter = new StringWriter()
    val pw: PrintWriter  = new PrintWriter(sw)
    t.printStackTrace(pw)
    sw.toString().lines.take(maxLines).mkString("\n") // stack trace as a string
  }
}






class ArgChecks(val args: Args) {
  val logger: Logger = LoggerFactory.getLogger(getClass())

  def sameFile() = args match {
    case Args(inputDir, outputDir) if inputDir == outputDir => {
      logger.warn("Warning: input and output directories are the same.")
    }
  }
}

object ConvMusicOggVorbis {
  def main(args: Array[String]): Unit = {

  }
}