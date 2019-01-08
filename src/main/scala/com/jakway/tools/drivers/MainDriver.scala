package com.jakway.tools.drivers

import java.io.File
import java.nio.file.{CopyOption, Files, StandardCopyOption, Path}

import com.jakway.tools.MusicFileVisitor
import com.jakway.util.Util
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

  private def execEachFile(onError: Throwable => Unit)(in: String, out: String): Future[Unit] = {
    if(new File(out).exists()) {
      logger.info(s"Skipping $out, file already exists.")
      Future.successful()
    } else {
      Future {
        Try {
          //perform operations on a temporary file then atomically move it to the destination
          val tmpOutputFile = Files.createTempFile("conv", null)
          vlcDriver.run(in, tmpOutputFile.toFile.getAbsolutePath)
          lltagDriver.run(in, tmpOutputFile.toFile.getAbsolutePath)

          tryAtomicMove(tmpOutputFile, new File(out).toPath)
          logger.debug(s"Finished $out")
        } recover {
          case t: Throwable => onError(t)
        }
      }
    }
  }

  private def tryAtomicMove(from: Path, to: Path): Unit = {
     try {
       Files.move(from, to, StandardCopyOption.ATOMIC_MOVE)
     } catch {
       case e: java.nio.file.AtomicMoveNotSupportedException => {
         logger.warn(s"Could not atomically move $from to $to")

         //retry the move
         Files.move(from, to)
       }
     }
  }
}

