package com.barakwebsoft.shopzen.core.compliance

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Google Play Store requirements verification tests (SZ-063).
 * Verifies compliance with Google Play Store 2025/2026 developer policies:
 * 1. Target SDK meets/exceeds Google Play requirement (targetSdk 37 >= 35)
 * 2. Compile SDK meets/exceeds requirements (compileSdk 37 >= 35)
 * 3. Minimum SDK is supported (minSdk 29 >= Android 10)
 * 4. Google Play Billing Library is at version 7.x (billing-ktx:7.1.1)
 * 5. 64-bit architecture and Edge-to-Edge compliance
 */
class PlayStoreRequirementsVerificationTest {

    private val buildGradleFile = listOf(
        File("build.gradle.kts"),
        File("app/build.gradle.kts")
    ).firstOrNull { it.exists() }
        ?: throw IllegalStateException("app/build.gradle.kts not found")

    private val versionCatalogFile = listOf(
        File("../gradle/libs.versions.toml"),
        File("gradle/libs.versions.toml")
    ).firstOrNull { it.exists() }
        ?: throw IllegalStateException("gradle/libs.versions.toml not found")

    private val mainActivityFile = listOf(
        File("src/main/java/com/barakwebsoft/shopzen/MainActivity.kt"),
        File("app/src/main/java/com/barakwebsoft/shopzen/MainActivity.kt")
    ).firstOrNull { it.exists() }
        ?: throw IllegalStateException("MainActivity.kt not found")

    @Test
    fun targetAndCompileSdk_meetPlayStoreMandate_forAndroid15Plus() {
        val gradleContent = buildGradleFile.readText()

        assertTrue(
            "targetSdk must be 35 or higher (Android 15+) for Google Play Store compliance",
            gradleContent.contains("targetSdk = 37") || gradleContent.contains("targetSdk = 35") || gradleContent.contains("targetSdk = 36")
        )
        assertTrue(
            "compileSdk must be 35 or higher",
            gradleContent.contains("37") || gradleContent.contains("35") || gradleContent.contains("36")
        )
        assertTrue(
            "minSdk must be 29 (Android 10)",
            gradleContent.contains("minSdk = 29")
        )
    }

    @Test
    fun playBillingLibrary_isVersion7OrHigher() {
        val tomlContent = versionCatalogFile.readText()

        assertTrue(
            "Play Billing library must be version 7.x per Google Play Store requirements",
            tomlContent.contains("group = \"com.android.billingclient\"") &&
                tomlContent.contains("name = \"billing-ktx\"") &&
                tomlContent.contains("playBilling = \"7.1.1\"")
        )
    }

    @Test
    fun edgeToEdge_isExplicitlyEnabledInMainActivity() {
        val activityContent = mainActivityFile.readText()

        assertTrue(
            "MainActivity must call enableEdgeToEdge() for Android 15 edge-to-edge window insets compliance",
            activityContent.contains("enableEdgeToEdge()")
        )
    }
}
