package com.jakway.tools.drivers

import java.io.File
import java.nio.file.Files

import com.jakway.tools.MusicFileVisitor
import com.jakway.util.runner.{CheckCommandExists, Runner}
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success, Try}

object VlcDriver {
  val commandName = "vlc"

  val logger: Logger = LoggerFactory.getLogger(getClass())
}

class VlcDriver(onError: Throwable => Unit =
               MainDriver.logOnError(VlcDriver.logger))
  extends CheckCommandExists(VlcDriver.commandName) {


  private def conv(in: String, out: String): Unit = {
    val args = Seq(
      "-I", "dummy",
      "-vvv",
      in,
      "--sout",
      "#transcode{acodec=vorb}:standard{mux=ogg,dst=\"" +
        //replace a " with \"
        out.replaceAll("\"", "\\\\\"") +
        "\",access=file}",
      "vlc://quit"
    )


    Runner.run(VlcDriver.commandName, args).toTry match {
      case Failure(t: Throwable) => onError(t)
      case Success(value) => {}
    }
  }

  def run: (String, String) => Unit = conv _
}
