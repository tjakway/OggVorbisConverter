package com.jakway.util

import java.nio.file.{Files, Path}

/**
  * used to get a temp dir that supports atomic moves to the destination folder
 *
  * @param dstFolder
  */
class AtomicTempDirCreator(val dstFolder) {
  lazy val tmpDir: Path = {

    val tmpOutputFile = Files.createTempDirectory("conv", null)

    Files.createTempFile(tmpOutputFile, null, null)
  }

  private def atomicMoveSupported(src: Path, dst: Path): Boolean = {
    if(!src.toFile.isDirectory || !dst.toFile.isDirectory) {
      throw new RuntimeException("atomicMoveSupported should only be called with 2 directories")
    } else {
      val tmpFile = Files.createTempFile(src, null, null)
      tmpFile.toFile.deleteOnExit()
    }
  }

  def get(): Path = tmpDir

}
