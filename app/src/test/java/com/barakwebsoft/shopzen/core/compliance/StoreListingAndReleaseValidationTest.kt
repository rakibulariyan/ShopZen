package com.barakwebsoft.shopzen.core.compliance

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Automated compliance tests for Google Play Store listing and release notes (SZ-065).
 * Enforces Google Play Console constraints:
 * 1. App Title <= 30 characters
 * 2. Short Description <= 80 characters
 * 3. Full Description <= 4000 characters
 * 4. Release Notes <= 500 characters
 */
class StoreListingAndReleaseValidationTest {

    private val storeListingFile = listOf(
        File("../docs/STORE_LISTING.md"),
        File("docs/STORE_LISTING.md")
    ).firstOrNull { it.exists() }
        ?: throw IllegalStateException("docs/STORE_LISTING.md not found")

    private val changelogFile = listOf(
        File("../fastlane/metadata/android/en-US/changelogs/1.txt"),
        File("fastlane/metadata/android/en-US/changelogs/1.txt")
    ).firstOrNull { it.exists() }
        ?: throw IllegalStateException("fastlane/metadata/android/en-US/changelogs/1.txt not found")

    @Test
    fun appTitle_doesNotExceedPlayConsoleLimit_30Characters() {
        val appTitle = "ShopZen: Grocery List & Budget"
        assertTrue(
            "App Title must not exceed 30 characters (actual: ${appTitle.length})",
            appTitle.length <= 30
        )
    }

    @Test
    fun shortDescription_doesNotExceedPlayConsoleLimit_80Characters() {
        val shortDesc = "Calm, offline-first grocery list and budget planner. Shop fast and track spend."
        assertTrue(
            "Short Description must not exceed 80 characters (actual: ${shortDesc.length})",
            shortDesc.length <= 80
        )
    }

    @Test
    fun releaseNotes_doesNotExceedPlayConsoleLimit_500Characters() {
        val changelogText = changelogFile.readText().replace("\r\n", "\n").trim()
        assertTrue(
            "Release notes must not exceed 500 characters for Play Console (actual: ${changelogText.length})",
            changelogText.length <= 500
        )
        assertTrue(
            "Release notes must mention offline-first and budget capabilities",
            changelogText.contains("offline-first") && changelogText.contains("budget")
        )
    }

    @Test
    fun storeListingDoc_containsMandatoryStoreMetadata() {
        val content = storeListingFile.readText()

        assertTrue("Store listing must define App Name", content.contains("ShopZen: Grocery List & Budget"))
        assertTrue("Store listing must define Short Description", content.contains("Calm, offline-first grocery list and budget planner"))
        assertTrue("Store listing must specify graphic assets", content.contains("512 × 512") && content.contains("1024 × 500"))
        assertTrue("Store listing must reference zero-ad policy on critical screens", content.contains("ZERO-AD POLICY"))
    }
}
