package com.sabin.emicalculator

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NepaliStringsTest {
    private fun load(dir: String): Map<String, String> {
        val file = listOf("src/main/res/$dir/strings.xml", "app/src/main/res/$dir/strings.xml").map(::File).first { it.exists() }
        return Regex("""<string name="([^"]+)"([^>]*)>(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(file.readText())
            .filterNot { it.groupValues[2].contains("translatable=\"false\"") }
            .associate { it.groupValues[1] to it.groupValues[3] }
    }

    @Test fun everyDefaultStringHasNepaliTranslation() {
        val en = load("values")
        val ne = load("values-ne")
        assertTrue(en.size > 20)
        assertEquals(emptySet<String>(), en.keys - ne.keys)
        assertEquals(emptySet<String>(), ne.keys - en.keys)
        assertTrue(ne.filterValues { it.isBlank() }.isEmpty())
    }

    @Test fun nepaliTranslationsAreNotCopiesOfEnglish() {
        val en = load("values")
        val same = load("values-ne").filter { (k, v) -> en[k] == v && k != "app_name" }
        assertTrue("Untranslated: ${same.keys}", same.size <= 3)
    }

    @Test fun placeholdersMatch() {
        val en = load("values")
        val ph = Regex("%\\d\\$[sd]")
        load("values-ne").forEach { (k, v) ->
            assertEquals(k, ph.findAll(en[k]!!).map { it.value }.toSet(), ph.findAll(v).map { it.value }.toSet())
        }
    }
}
