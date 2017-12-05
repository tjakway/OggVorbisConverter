package com.jakway.tools.tests

import com.jakway.tools.drivers.ExiftoolDriver
import org.scalatest.FlatSpec

import scala.util.matching.Regex

class TestRegex extends FlatSpec {
  lazy val exifRegex = new ExiftoolDriver().tagValueRegex


  /**
    * Tests that rgx(key) produces value
    * @param rgx
    * @param items
    * @return
    */
  def assertRegexMatches(items: Map[String, String])(rgx: Regex) = {
    items.map {
      case (key, value) => {
        val matches = rgx.findAllMatchIn(key)

        //our use case is simple:
        //only getting the first group of the first match
        assert(matches.length == 1)
        assert(matches.toSeq.head.group(0).trim == value)
      }
    }
  }

  def assertMatchesTitles = assertRegexMatches( Map {
    "Title                           : Happy Jack" -> "Happy Jack"
    "Title                           : Longview" -> "Longview"
    "Title                           : At Least I Have Nothing" ->
        "At Least I Have Nothing"
  }) _

  "ExiftoolDriver's Regex" should "match titles" in {
    assertMatchesTitles(exifRegex)
  }
}
