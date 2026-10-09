package com.sabin.emicalculator

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherIconTest {
    private val res = File("src/main/res")
    private val manifest = File("src/main/AndroidManifest.xml").readText()

    @Test
    fun adaptiveIconsReferenceBackgroundAndForeground() {
        for (name in listOf("ic_launcher", "ic_launcher_round")) {
            val xml = File(res, "mipmap-anydpi-v26/$name.xml").readText()
            assertTrue(xml.contains("<adaptive-icon"))
            assertTrue(xml.contains("@drawable/ic_launcher_background"))
            assertTrue(xml.contains("@drawable/ic_launcher_foreground"))
        }
    }

    @Test
    fun backgroundIsIndigo() =
        assertTrue(File(res, "drawable/ic_launcher_background.xml").readText().contains("#3F51B5"))

    @Test
    fun foregroundIsWhite() =
        assertTrue(File(res, "drawable/ic_launcher_foreground.xml").readText().contains("#FFFFFF"))

    @Test
    fun manifestUsesMipmapIcons() {
        assertTrue(manifest.contains("""android:icon="@mipmap/ic_launcher""""))
        assertTrue(manifest.contains("""android:roundIcon="@mipmap/ic_launcher_round""""))
    }

    @Test
    fun placeholderRemoved() = assertFalse(File(res, "drawable/ic_launcher.xml").exists())
}
