package com.jakway.util

import java.io.ByteArrayInputStream

class InfiniteStringStream(val str: String, val charset: String = "UTF-8") extends java.io.InputStream {
  val stream = new ByteArrayInputStream(str.getBytes(charset))

  override def read(): Int = {
    stream.read() match {
      case -1 => {
        //restart the stream whenever it runs out
        stream.reset()
        read()
      }
      case x => x
    }
  }
}
