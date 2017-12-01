package com.jakway.tools

import java.io.File
import java.nio.file.Files

import com.jakway.tools.Runner.RunOutput

import scala.util.Try

class VlcDriver(val inputDir: File, val outputDir: File) {
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
      (thisFile, Util.removeFilenameExtension(new File(thisFile).getName) + outputExtension)
    }.toMap
  }

  def run(onError: Throwable => Unit): Unit = {

    def conv(in: String, out: String): Unit = {
      val args = Seq(
        "-I", "dummy",
        "-vvv",
        in,
        "--sout",
        s"#transcode{acodec=vorb}:standard{mux=ogg,dst=\"$out\",access=file}",
        "vlc://quit"
      )


      Runner.run("vlc", args) match {
        case Left(r: RunOutput) => onError(r)
        case Right(value) => {}
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
