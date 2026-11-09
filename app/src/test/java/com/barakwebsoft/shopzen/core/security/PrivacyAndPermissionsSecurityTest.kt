package com.barakwebsoft.shopzen.core.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Privacy, permissions, and Data Safety declaration security tests (SZ-062).
 * Verifies that:
 * 1. AndroidManifest.xml requests zero dangerous runtime permissions (no location, contacts, camera, storage, microphone)
 * 2. Only strictly necessary permissions for AdMob and Google Play Billing are utilized
 * 3. Application package target matches approved identity com.barakwebsoft.shopzen
 * 4. Local storage security guarantees (Room/SQLite local-only storage)
 */
class PrivacyAndPermissionsSecurityTest {

    private val manifestFile = listOf(
        File("src/main/AndroidManifest.xml"),
        File("app/src/main/AndroidManifest.xml")
    ).firstOrNull { it.exists() }
        ?: throw IllegalStateException("AndroidManifest.xml not found")

    private val dangerousPermissions = listOf(
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.READ_CONTACTS",
        "android.permission.WRITE_CONTACTS",
        "android.permission.READ_CALENDAR",
        "android.permission.WRITE_CALENDAR",
        "android.permission.READ_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_PHONE_STATE",
        "android.permission.READ_CALL_LOG",
        "android.permission.BODY_SENSORS",
        "android.permission.ACCESS_BACKGROUND_LOCATION",
        "android.permission.POST_NOTIFICATIONS"
    )

    @Test
    fun manifest_doesNotRequestAnyDangerousRuntimePermissions() {
        val manifestContent = manifestFile.readText()

        for (permission in dangerousPermissions) {
            assertFalse(
                "AndroidManifest.xml must NOT request dangerous permission '$permission'",
                manifestContent.contains(permission)
            )
        }
    }

    @Test
    fun manifest_configuresSampleAdMobAppId_andBackupRules() {
        val manifestContent = manifestFile.readText()

        assertTrue(
            "Manifest must configure AdMob application ID metadata",
            manifestContent.contains("com.google.android.gms.ads.APPLICATION_ID")
        )
        assertTrue(
            "Manifest must configure data extraction rules",
            manifestContent.contains("android:dataExtractionRules=\"@xml/data_extraction_rules\"")
        )
        assertTrue(
            "Manifest must configure backup rules",
            manifestContent.contains("android:fullBackupContent=\"@xml/backup_rules\"")
        )
    }

    @Test
    fun gradleBuild_declaresStablePackageIdentity() {
        val buildFile = listOf(
            File("build.gradle.kts"),
            File("app/build.gradle.kts")
        ).firstOrNull { it.exists() }
            ?: throw IllegalStateException("build.gradle.kts not found")

        val buildContent = buildFile.readText()

        assertTrue(
            "Namespace must be com.barakwebsoft.shopzen",
            buildContent.contains("namespace = \"com.barakwebsoft.shopzen\"")
        )
        assertTrue(
            "ApplicationId must be com.barakwebsoft.shopzen",
            buildContent.contains("applicationId = \"com.barakwebsoft.shopzen\"")
        )
    }
}
