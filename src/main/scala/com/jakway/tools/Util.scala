package com.jakway.tools

import java.io.{PrintWriter, StringWriter}

object Util {
  def removeFilenameExtension(fname: String): String = {
    fname.substring(0, fname.lastIndexOf('.'))
  }
  /**
    * see https://stackoverflow.com/questions/1149703/how-can-i-convert-a-stack-trace-to-a-string
    * @return
    */
  def throwableToString(t: Throwable, maxLines: Int = 15) = {
    val sw: StringWriter = new StringWriter()
    val pw: PrintWriter  = new PrintWriter(sw)
    t.printStackTrace(pw)
    sw.toString().lines.take(maxLines).mkString("\n") // stack trace as a string
  }
}
