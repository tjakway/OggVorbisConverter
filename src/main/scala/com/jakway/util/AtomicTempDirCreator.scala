package com.jakway.util

import java.nio.file.{Files, Path}

import scala.util.Try

/**
  * used to get a temp dir that supports atomic moves to the destination folder
 *
  * @param dstFolder
  */
class AtomicTempDirCreator(val dstFolder: Path) {
  val prefix: String = "conv"

  case class AtomicTempDirCreatorException(msg: String) extends RuntimeException(msg)
  lazy val tmpDir: Path = {

    lazy val first = Files.createTempDirectory(prefix, null, null)
    lazy val second = Files.createTempDirectory(dstFolder.getParent(), prefix, null)

    if(atomicMoveSupported(first, dstFolder)) {
      first
    } else {
      Files.deleteIfExists(first)
      if(!atomicMoveSupported(second, dstFolder)) {
        throw AtomicTempDirCreatorException("Could not create a temporary directory that can atomically" +
          s"move files to $dstFolder")
      } else {
        second.toFile.deleteOnExit()
        second
      }
    }
  }


  private def atomicMoveSupported(src: Path, dst: Path): Boolean = {
    if(!src.toFile.isDirectory || !dst.toFile.isDirectory) {
      throw new RuntimeException("atomicMoveSupported should only be called with 2 directories")
    } else {
      val tmpFile = Files.createTempFile(src, null, null)
      tmpFile.toFile.deleteOnExit()

      val r = Try {
        import java.nio.file.StandardCopyOption._
        val res: Path = Files.move(tmpFile, dst, ATOMIC_MOVE)

        Files.deleteIfExists(res)
      } recover {
        case t: Throwable =>  {
          Files.deleteIfExists(tmpFile)
          false
        }
      }

      r.get
    }
  }

  def get(): Path = tmpDir

}
