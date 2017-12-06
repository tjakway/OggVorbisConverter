package com.jakway.tools.drivers

import java.io.File
import java.nio.file.Files

import com.jakway.tools.MusicFileVisitor
import com.jakway.util.Util
import com.jakway.util.runner.{CheckCommandExists, Runner}
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success, Try}

object MainDriver {
  def logOnError(logger: Logger)(t: Throwable): Unit =
    logger.error("Exception thrown", t)
}

class MainDriver(val inputDir: File, val outputDir: File) {
  case class ConversionException(msg: String)
    extends RuntimeException(msg)

  val logger: Logger = LoggerFactory.getLogger(getClass())
  def logOnError = MainDriver.logOnError(logger) _

  val vlcDriver = new VlcDriver()
  val lltagDriver = new LltagDriver(verbose = true)

  val outputExtension = ".opus"

  def getMusicInputFiles(): Try[Seq[String]] = Try {
    if(!inputDir.isDirectory) {
      throw ConversionException(s"$inputDir is not a directory")
    }

    val visitor = new MusicFileVisitor()
    Files.walkFileTree(inputDir.toPath, visitor)

    //accMusicFiles will be a Seq of absolute paths
    visitor.accMusicFiles
  }

  def mapInputToOutput(in: Seq[String]): Try[Map[String, String]] = Try {
    in.map { thisFile =>
      (thisFile, new File(outputDir,
        Util.removeFilenameExtension(new File(thisFile).getName) + outputExtension).getAbsolutePath)
    }.toMap
  }

  def run(onError: Throwable => Unit = logOnError): Unit = {

    for {
      musicFiles <- getMusicInputFiles()
      inputOutputMap <- mapInputToOutput(musicFiles)
    } yield {
      inputOutputMap.foreach {
        case (input, output) => Try {
          execEachFile(input, output)
        } recover {
          case t: Throwable => onError(t)
        }
      }
    }

  }

  private def execEachFile(in: String, out: String): Unit = {
    vlcDriver.run(in, out)
    lltagDriver.run(in, out)
  }
}

