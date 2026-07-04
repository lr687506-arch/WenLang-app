package com.example

import com.example.data.TranslationService
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {
  @Test
  fun testDeepL() {
    println("--- START DEEPL CONNECTION TEST ---")
    val result = TranslationService.testDeepLConnection()
    println(result)
    println("--- END DEEPL CONNECTION TEST ---")
  }
}
