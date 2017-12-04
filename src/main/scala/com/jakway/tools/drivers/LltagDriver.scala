package com.jakway.tools.drivers

import java.io.ByteArrayInputStream

import com.jakway.util.InfiniteStringStream
import com.jakway.util.runner._
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex

object LltagDriver {
  val programName = "lltag"
}

class LltagDriver
  extends CheckCommandExists(LltagDriver.programName) {
  import LltagDriver._

  private val logger: Logger = LoggerFactory.getLogger(getClass())

  val tagRegexp: Regex = "(?u)[A-Z]=.+".r

  val commonOptions: Seq[String] = Seq(
    "--yes",
    "--spaces",
    "--ogg",
  )

  /**
    * read in the key-value pairs
    * @param in
    * @return
    */
  private def runInput(in: String): Try[ProgramOutput] =
    Runner.run(programName, Seq("-S", in), true).toTry

  private def parseInput(in: String, o: ProgramOutput): Seq[String] = o match {
    case x: ZeroExitCode => {
      //there's no tailOption
      Try(x.stdout
        .lines
        .toSeq
        .tail).toOption match {
        case None => {
          logger.warn(s"No key value pairs for $in")
          Seq()
        }
        case Some(kvLines) => {
          val kvPairs = kvLines
            .map(_.trim)
            //remove empty strings
            .filter(!_.isEmpty)
            //make sure they match KEY=VALUE
            .filter(thisLine => tagRegexp.findFirstIn(thisLine).nonEmpty)

          if(kvPairs.isEmpty) {
            logger.warn(s"No key value pairs for $in after filtering" +
              s", original program output: $o")
          }

          kvPairs
        }
      }

    }
    case x: NonzeroExitCode =>  {
      x.throwThis()
      Seq()
    }
  }

  private def runOutput(kvPairs: Seq[String], out: String): Try[Unit] = {
    if(kvPairs.isEmpty) {
      logger.info(s"Skipping tags for $out because non exist")
      Success({})
    }
    else {
      val args = commonOptions ++ kvPairs.flatMap {
        case thisPair => Seq("--tag", thisPair)
          //don't forget the output file
      } ++ Seq(out)

      //ignore the output
      Runner.run(programName, args, true).toTry.map(_ => {})
    }
  }

  def run(in: String, out: String): Unit = {
    for {
      o <- runInput(in)
      _ <- runOutput(parseInput(in, o), out)
    } yield { }
    logger.info(s"Copied tags $in -> $out")
  }
}


