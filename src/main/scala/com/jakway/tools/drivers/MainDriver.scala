package com.jakway.tools.drivers

import java.io.File
import java.nio.file.{CopyOption, Files, StandardCopyOption}

import com.jakway.tools.MusicFileVisitor
import com.jakway.util.{AtomicTempDirCreator, Util}
import com.jakway.util.runner.{CheckCommandExists, Runner}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
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

  val outputExtension = ".ogg"

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
        case (input, output) => execEachFile(onError)(input, output)
      }
    }

  }

  lazy val tmpDir = new AtomicTempDirCreator(outputDir.toPath).get()

  private def execEachFile(onError: Throwable => Unit)(in: String, out: String): Future[Unit] = {
    if(new File(out).exists()) {
      logger.info(s"Skipping $out, file already exists.")
      Future.successful()
    } else {
      Future {
        Try {
          //perform operations on a temporary file then atomically move it to the destination
          val tmpOutputFile = Files.createTempFile(tmpDir, "conv", null)
          vlcDriver.run(in, tmpOutputFile.toFile.getAbsolutePath)
          lltagDriver.run(in, tmpOutputFile.toFile.getAbsolutePath)

          Files.move(tmpOutputFile, new File(out).toPath, StandardCopyOption.ATOMIC_MOVE)
          logger.debug(s"Finished $out")
        } recover {
          case t: Throwable => onError(t)
        }
      }
    }
  }
}

