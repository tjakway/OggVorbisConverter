package com.jakway.tools.drivers

import java.io.File
import java.nio.file.Files

import com.jakway.tools.{MusicFileVisitor, Util}
import com.jakway.util.runner.{CheckCommandExists, Runner}

import scala.util.{Failure, Success, Try}

object VlcDriver {
  val commandName = "vlc"
}

class VlcDriver(val inputDir: File, val outputDir: File)
  extends CheckCommandExists(VlcDriver.commandName) {
  case class VlcDriverException(m: String) extends RuntimeException(m)

  val outputExtension = ".ogg"

  def getMusicInputFiles(): Try[Seq[String]] = Try {
    if(!inputDir.isDirectory) {
      throw VlcDriverException(s"$inputDir is not a directory")
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

  def printOnError(t: Throwable) = println(Util.throwableToString(t))
  def run(onError: Throwable => Unit = printOnError): Unit = {

    def conv(in: String, out: String): Unit = {
      val args = Seq(
        "-I", "dummy",
        "-vvv",
        in,
        "--sout",
        "#transcode{acodec=vorb}:standard{mux=ogg,dst=\"" + out + "\",access=file}",
        "vlc://quit"
      )


      Runner.run(VlcDriver.commandName, args).toTry match {
        case Failure(t: Throwable) => onError(t)
        case Success(value) => {}
      }
    }

    for {
      musicFiles <- getMusicInputFiles()
      inputOutputMap <- mapInputToOutput(musicFiles)
    } yield {
      inputOutputMap.foreach {
        case (input, output) => Try {
          conv(input, output)
        } recover {
          case t: Throwable => onError(t)
        }
      }
    }

  }
}
