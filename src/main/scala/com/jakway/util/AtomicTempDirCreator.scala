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

  val tmpDir: Path = {

    lazy val first = Try(Files.createTempDirectory(prefix))
    lazy val second = Try(Files.createTempDirectory(dstFolder.getParent(), prefix))

    if(first.map(atomicMoveSupported(_, dstFolder)).isSuccess) {
      first.get
    } else {
      first.map(Files.deleteIfExists(_))
      if(second.map(atomicMoveSupported(_, dstFolder)).isSuccess) {

        second.map(_.toFile.deleteOnExit())
        second.get
      } else {
        throw AtomicTempDirCreatorException("Could not create a temporary directory that can atomically " +
          s"move files to $dstFolder")
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
