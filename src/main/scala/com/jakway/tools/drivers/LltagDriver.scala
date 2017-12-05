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

  val exiftoolDriver = new ExiftoolDriver(true)

  val tagRegexp: Regex = "(?u)[A-Z]=.+".r

  val commonOptions: Seq[String] = Seq(
    "--yes",
    "--spaces",
    "--ogg",
  )



  //lltag only works with MP3, Ogg Vorbis and FLAC
  val lltagCompatibleExtensions = Seq("mp3", "ogg", "flac")

  def hasLltagCompatibleExtension(in: String): Boolean =
    lltagCompatibleExtensions.exists(in.toLowerCase.endsWith(_))

  /**
    * read in the key-value pairs
    * @param in
    * @return
    */
  private def runLltagInput(in: String): Try[ProgramOutput] =
    Runner.run(programName, Seq("-S", in), true).toTry

  private def parseLltagInput(in: String, o: ProgramOutput): Seq[String] = o match {
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
    def getInput = {
      //run lltag if possible--
      //supports more tags and less things can go wrong feeding the same tool's
      //output to its input
      if(hasLltagCompatibleExtension(in)) {
        runLltagInput(in).map(parseLltagInput(in, _))
      } else {
        //otherwise run exiftool
        exiftoolDriver.run(in)
      }
    }

    for {
      input <- getInput
      _ <- runOutput(input, out)
    } yield { }
    logger.info(s"Copied tags $in -> $out")
  }
}

object ExiftoolDriver {
  val programName = "exiftool"

  val tagList: Map[String, String] = {
    Seq("Artist",
          "Title",
          "Album",
          "Date",
          "Comment",
          "Genre",
          "Number")
      .map(tag => (tag, tag.toUpperCase))
      .toMap + {
          //non-obvious mappings
          //exiftool has multiple ways to name the equivalent lltag value
         ("Discnumber" -> "NUMBER")
         ("Track Number" -> "NUMBER")
         ("Track" -> "NUMBER")
         ("Year" -> "DATE")
         ("Date/Time Original" -> "DATE")
      }
    }
}

/**
  * Only used by LltagDriver
  */
class ExiftoolDriver(val verbose: Boolean = false)
  extends CheckCommandExists(ExiftoolDriver.programName) {
  import ExiftoolDriver._

  case class ExiftoolDriverException(val msg: String)
    extends RuntimeException(msg)

  val logger: Logger = LoggerFactory.getLogger(getClass())

  val tagValueRegex: Regex = """(?U)(?<=\s*:)$""".r

  private def extractTagValue(line: String): String = {
    val matches = tagValueRegex.findAllMatchIn(line)
    if(matches.length != 1) {
      throw ExiftoolDriverException(s"Multiple matches in $line for ${tagValueRegex.pattern.pattern()}")
    } else {
      //get the first match
      matches.toSeq.head.group(0)
    }
  }

  private def extractTag(line: String): Option[(String, String)] = {
    tagList.foldLeft(None: Option[(String, String)]) {
      //if we haven't found a tag in this line yet,
      case (None, (exifTagName, lltagName)) => {
        //check if the current tag matches
        if(line.startsWith(exifTagName)) {
          //and if so extract the value
          Some(lltagName, extractTagValue(line))
        } else {
          None
        }
      }

        //sanity check--shouldn't match another tag once we've already extracted a tag value
      case (Some(tagValuePair), (exifTagName, _))
        if line.startsWith(exifTagName) => {
        logger.warn(s"exiftool output line $line matches multiple tags ($exifTagName this iteration), " +
          s"using $tagValuePair")
        Some(tagValuePair)
      }

        //otherwise skip this pair
      case (Some(x), _) => Some(x)
    }
  }

  def run(in: String): Try[Seq[String]] = {
    val res = Runner.run(programName, Seq(in), logOutput = true)
      .toTry
      //extract the tags from exiftool's stdout
      .map(_.stdout.lines.toSeq.flatMap(extractTag))
      //Try -> Seq[String, String] becomes
      //Try -> Seq[String]
      .map(_.map(v => s"${v._1}=${v._2}"))

    if(verbose) {
      res.foreach(tags => logger.debug(s"Collected tags $tags from $in"))
    }
    res
  }
}


