package com.jakway.tools.tests

import com.jakway.tools.{CheckCommandExists, CommandNotFoundException}
import org.scalatest.FlatSpec

class TestCheckCommandExists extends FlatSpec {
  "CheckCommandExists" should "throw for a command that doesn't exist" in {
    //some command that clearly doesn't exist
    val randCommandName = "dfklkqdjjnsnnneeeerw"

    assertThrows[CommandNotFoundException](new CheckCommandExists(randCommandName))
  }

}