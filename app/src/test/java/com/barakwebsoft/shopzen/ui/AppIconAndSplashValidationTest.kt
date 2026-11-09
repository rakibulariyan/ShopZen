package com.barakwebsoft.shopzen.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

/**
 * Validates the design assets, configuration, and implementation for ShopZen's
 * adaptive launcher icon, monochrome themed icon, fallback mipmaps, Google Play listing asset,
 * and official Android SplashScreen API configuration.
 */
class AppIconAndSplashValidationTest {

    private val projectRoot: File = listOf(
        File("."),
        File("..")
    ).firstOrNull { File(it, "gradlew.bat").exists() || File(it, "settings.gradle.kts").exists() }
        ?.canonicalFile ?: File(".").canonicalFile

    @Test
    fun testAdaptiveLauncherIconsExistAndIncludeMonochrome() {
        val launcherXml = File(projectRoot, "app/src/main/res/mipmap-anydpi/ic_launcher.xml")
        val launcherRoundXml = File(projectRoot, "app/src/main/res/mipmap-anydpi/ic_launcher_round.xml")

        assertTrue("ic_launcher.xml must exist in mipmap-anydpi (path: ${launcherXml.absolutePath})", launcherXml.exists())
        assertTrue("ic_launcher_round.xml must exist in mipmap-anydpi", launcherRoundXml.exists())

        val launcherContent = launcherXml.readText()
        assertTrue("ic_launcher must be an adaptive-icon", launcherContent.contains("<adaptive-icon"))
        assertTrue("ic_launcher must declare background", launcherContent.contains("android:drawable=\"@drawable/ic_launcher_background\""))
        assertTrue("ic_launcher must declare foreground", launcherContent.contains("android:drawable=\"@drawable/ic_launcher_foreground\""))
        assertTrue("ic_launcher must declare monochrome icon for Android 13+ dynamic theme", launcherContent.contains("<monochrome"))

        val roundContent = launcherRoundXml.readText()
        assertTrue("ic_launcher_round must be an adaptive-icon", roundContent.contains("<adaptive-icon"))
        assertTrue("ic_launcher_round must declare foreground", roundContent.contains("android:drawable=\"@drawable/ic_launcher_foreground\""))
        assertTrue("ic_launcher_round must declare monochrome", roundContent.contains("<monochrome"))
    }

    @Test
    fun testVectorDrawablesExistAndHaveCorrectBrandTokens() {
        val bgXml = File(projectRoot, "app/src/main/res/drawable/ic_launcher_background.xml")
        val fgXml = File(projectRoot, "app/src/main/res/drawable/ic_launcher_foreground.xml")
        val monoXml = File(projectRoot, "app/src/main/res/drawable/ic_launcher_monochrome.xml")
        val splashXml = File(projectRoot, "app/src/main/res/drawable/ic_splash_icon.xml")

        assertTrue("ic_launcher_background.xml must exist", bgXml.exists())
        assertTrue("ic_launcher_foreground.xml must exist", fgXml.exists())
        assertTrue("ic_launcher_monochrome.xml must exist", monoXml.exists())
        assertTrue("ic_splash_icon.xml must exist", splashXml.exists())

        val bgContent = bgXml.readText()
        assertTrue("Background gradient must include Primary Green #16845B", bgContent.contains("#16845B"))
        assertTrue("Background gradient must include Dark Green #105C43", bgContent.contains("#105C43"))

        val fgContent = fgXml.readText()
        assertTrue("Foreground viewport must be 108dp wide", fgContent.contains("android:viewportWidth=\"108\""))
        assertTrue("Foreground viewport must be 108dp high", fgContent.contains("android:viewportHeight=\"108\""))
        assertTrue("Foreground checkmark must use Accent Green #B8E986", fgContent.contains("#B8E986"))

        val splashContent = splashXml.readText()
        assertTrue("Splash icon viewport must be 288dp wide for standard splash icon", splashContent.contains("android:viewportWidth=\"288\""))
        assertTrue("Splash icon viewport must be 288dp high", splashContent.contains("android:viewportHeight=\"288\""))
        assertTrue("Splash icon emblem must use ShopZen brand colors", splashContent.contains("#16845B") && splashContent.contains("#B8E986"))
    }

    @Test
    fun testFallbackPngMipmapsExistAcrossAllStandardDensities() {
        val densities = listOf("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi")
        val expectedSizes = mapOf(
            "mdpi" to 48,
            "hdpi" to 72,
            "xhdpi" to 96,
            "xxhdpi" to 144,
            "xxxhdpi" to 192
        )

        for (density in densities) {
            val launcherPng = File(projectRoot, "app/src/main/res/mipmap-$density/ic_launcher.png")
            val roundPng = File(projectRoot, "app/src/main/res/mipmap-$density/ic_launcher_round.png")

            assertTrue("ic_launcher.png must exist for $density", launcherPng.exists())
            assertTrue("ic_launcher_round.png must exist for $density", roundPng.exists())

            val img = ImageIO.read(launcherPng)
            val expected = expectedSizes[density]!!
            assertEquals("ic_launcher.png width for $density", expected, img.width)
            assertEquals("ic_launcher.png height for $density", expected, img.height)

            val roundImg = ImageIO.read(roundPng)
            assertEquals("ic_launcher_round.png width for $density", expected, roundImg.width)
            assertEquals("ic_launcher_round.png height for $density", expected, roundImg.height)
        }
    }

    @Test
    fun testPlayStore512IconExistsAndHasExactDimensions() {
        val storeIcon = File(projectRoot, "store/icon/shopzen_play_store_icon_512.png")
        val docsIcon = File(projectRoot, "docs/assets/icon/shopzen_play_store_icon_512.png")

        assertTrue("Play Store icon asset must exist in store/icon/", storeIcon.exists())
        assertTrue("Play Store icon asset must exist in docs/assets/icon/", docsIcon.exists())

        val img = ImageIO.read(storeIcon)
        assertEquals("Play Store icon width must be 512px", 512, img.width)
        assertEquals("Play Store icon height must be 512px", 512, img.height)
    }

    @Test
    fun testSplashThemeConfigurationInLightAndDarkThemes() {
        val lightThemes = File(projectRoot, "app/src/main/res/values/themes.xml").readText()
        val darkThemes = File(projectRoot, "app/src/main/res/values-night/themes.xml").readText()

        assertTrue("Light theme must define Theme.ShopZen.Starting", lightThemes.contains("name=\"Theme.ShopZen.Starting\""))
        assertTrue("Light theme must inherit from Theme.SplashScreen", lightThemes.contains("parent=\"Theme.SplashScreen\""))
        assertTrue("Light theme must specify windowSplashScreenBackground", lightThemes.contains("name=\"windowSplashScreenBackground\""))
        assertTrue("Light theme must specify windowSplashScreenAnimatedIcon", lightThemes.contains("name=\"windowSplashScreenAnimatedIcon\""))
        assertTrue("Light theme must specify postSplashScreenTheme", lightThemes.contains("name=\"postSplashScreenTheme\""))

        assertTrue("Dark theme must define Theme.ShopZen.Starting", darkThemes.contains("name=\"Theme.ShopZen.Starting\""))
        assertTrue("Dark theme must inherit from Theme.SplashScreen", darkThemes.contains("parent=\"Theme.SplashScreen\""))
        assertTrue("Dark theme must specify dark splash background", darkThemes.contains("name=\"windowSplashScreenBackground\">@color/splash_background_dark</item>"))
    }

    @Test
    fun testAndroidManifestUsesSplashScreenTheme() {
        val manifest = File(projectRoot, "app/src/main/AndroidManifest.xml").readText()
        assertTrue("MainActivity must declare Theme.ShopZen.Starting as theme", manifest.contains("android:theme=\"@style/Theme.ShopZen.Starting\""))
    }
}
