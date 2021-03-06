package com.jakway.tools

import java.io.File

import com.jakway.tools.drivers.{MainDriver, VlcDriver}
import org.slf4j.{Logger, LoggerFactory}

case class Args(inputDir: File, outputDir: File)


class ArgChecks(val args: Args) {
  val logger: Logger = LoggerFactory.getLogger(getClass())

  def sameFile() = args match {
    case Args(inputDir, outputDir) if inputDir == outputDir => {
      logger.warn("Warning: input and output directories are the same.")
    }
    case _ => {}
  }

  def apply() = sameFile
}

object ConvMusicOggVorbis {
  def main(args: Array[String]): Unit = {
    if(args.length != 2) {
      System.err.println("Usage: [input-dir] [output-dir]")
      System.exit(1)
    } else {
      val convArgs = Args(new File(args(0)), new File(args(1)))
      new ArgChecks(convArgs).apply()

      new MainDriver(convArgs.inputDir, convArgs.outputDir)
        .run()
    }
  }
}