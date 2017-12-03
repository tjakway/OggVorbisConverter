package com.jakway.tools.drivers

import com.jakway.util.runner.CheckCommandExists

object LltagDriver {
  val programName = "lltag"
}

class LltagDriver
  extends CheckCommandExists(LltagDriver.programName) {

  def run(in: String, out: String): Unit = {

  }

}
