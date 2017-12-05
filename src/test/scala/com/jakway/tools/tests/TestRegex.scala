package com.jakway.tools.tests

import com.jakway.tools.drivers.ExiftoolDriver
import org.scalatest.FlatSpec

import scala.util.matching.Regex

class TestRegex extends FlatSpec {

  /**
    * Tests that rgx(key) produces value
    * @param items
    * @return
    */
  def assertRegexMatches(items: Map[String, String]) = {
    val exiftoolDriver = new ExiftoolDriver()

    items.map {
      case (key, value) => {
        assert(exiftoolDriver.extractTagValue(key) == value)
      }
    }
  }

  "ExiftoolDriver's Regex" should "match titles" in {
    assertRegexMatches( Map {
        "Title                           : Happy Jack" -> "Happy Jack"
        "Title                           : Longview" -> "Longview"
        "Title                           : At Least I Have Nothing" ->
            "At Least I Have Nothing"
      })
  }
}
