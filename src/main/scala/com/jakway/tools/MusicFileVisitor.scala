package com.jakway.tools

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, Path}

import org.slf4j.{Logger, LoggerFactory}

class MusicFileVisitor extends java.nio.file.FileVisitor[Path] {
  import scala.collection.mutable
  val accMusicFiles: mutable.ArrayBuffer[String] = mutable.ArrayBuffer.empty[String]

  case class MusicFileVisitorException(msg: String) extends RuntimeException(msg)
  val logger: Logger = LoggerFactory.getLogger(getClass())

  override def postVisitDirectory(t: Path, e: IOException): FileVisitResult = FileVisitResult.CONTINUE
  override def preVisitDirectory(t: Path, basicFileAttributes: BasicFileAttributes): FileVisitResult = FileVisitResult.CONTINUE

  override def visitFileFailed(t: Path, e: IOException): FileVisitResult = {
    logger.warn(s"MusicFileVisitor.visitFileFailed called for ${t.toFile.getAbsolutePath} with IOException ${Util.throwableToString(e)}")

    //stop walking the tree
    FileVisitResult.TERMINATE
  }

  override def visitFile(t: Path, basicFileAttributes: BasicFileAttributes): FileVisitResult = {
    //if it's a music file, add it to our list
    if(isMusicFile(t) && !t.toFile.isDirectory) {
      accMusicFiles += (t.toFile.getAbsolutePath)
      FileVisitResult.CONTINUE
    }
    else {
      FileVisitResult.CONTINUE
    }
  }

  def isMusicFile(p: Path): Boolean = {
    //TODO: make this a config parameter
    val musicExtensions = Seq("mp3", "ogg", "flac", "m4a", "mp2", "opus", "m4v")

    //check if it has any of the predetermined music extensions
    musicExtensions.exists(ext => p.toString.toLowerCase.endsWith("." + ext))
  }
}
