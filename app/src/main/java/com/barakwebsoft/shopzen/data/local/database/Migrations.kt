package com.barakwebsoft.shopzen.data.local.database

import androidx.room.migration.Migration

/**
 * Explicit Room database migrations registry.
 *
 * Requirements:
 * - All schema version bumps must provide explicit [Migration] implementations.
 * - Never use fallbackToDestructiveMigration() in production.
 * - Migrations must preserve existing user data, indices, and foreign key integrity.
 */
object Migrations {

    /**
     * Array of all production database migrations.
     * Database is currently at baseline Version 1.
     * Future version bumps (e.g., 1 -> 2) will be appended here.
     */
    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        // Future migrations will be registered here (e.g., MIGRATION_1_2)
    )
}
