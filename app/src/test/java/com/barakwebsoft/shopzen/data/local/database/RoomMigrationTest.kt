package com.barakwebsoft.shopzen.data.local.database

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.lang.reflect.Proxy

class RoomMigrationTest {

    private lateinit var schemaContent: String

    @Before
    fun setUp() {
        val schemaFile = listOf(
            File("schemas/com.barakwebsoft.shopzen.data.local.database.ShopZenDatabase/1.json"),
            File("app/schemas/com.barakwebsoft.shopzen.data.local.database.ShopZenDatabase/1.json")
        ).firstOrNull { it.exists() }

        assertNotNull("Schema JSON file 1.json must exist in schemas directory", schemaFile)
        schemaContent = schemaFile!!.readText()
    }

    @Test
    fun testSchemaVersion1File_formatAndDatabaseVersion() {
        assertTrue("Schema format version must be 1", schemaContent.contains("\"formatVersion\": 1"))
        assertTrue("Database version must be 1", schemaContent.contains("\"version\": 1"))
        assertTrue("Identity hash must exist", schemaContent.contains("\"identityHash\":"))
    }

    @Test
    fun testSchemaVersion1_containsAllTablesAndSql() {
        val expectedTables = listOf(
            "shopping_lists",
            "shopping_items",
            "categories",
            "shopping_history",
            "history_items"
        )
        for (table in expectedTables) {
            assertTrue("Schema must contain table $table", schemaContent.contains("\"tableName\": \"$table\""))
        }
        assertTrue("Schema must contain createSql template", schemaContent.contains("CREATE TABLE IF NOT EXISTS `\${TABLE_NAME}`"))
    }

    @Test
    fun testSchemaVersion1_foreignKeysAndCascadeRules() {
        // shopping_items -> shopping_lists (CASCADE)
        assertTrue(schemaContent.contains("\"table\": \"shopping_lists\""))
        assertTrue(schemaContent.contains("\"onDelete\": \"CASCADE\""))

        // shopping_items -> categories (SET NULL)
        assertTrue(schemaContent.contains("\"table\": \"categories\""))
        assertTrue(schemaContent.contains("\"onDelete\": \"SET NULL\""))

        // history_items -> shopping_history (CASCADE)
        assertTrue(schemaContent.contains("\"table\": \"shopping_history\""))
    }

    @Test
    fun testSchemaVersion1_indicesPresent() {
        val expectedIndices = listOf(
            "index_shopping_lists_status",
            "index_shopping_lists_updated_at",
            "index_shopping_items_list_id",
            "index_shopping_items_category_id",
            "index_shopping_items_list_id_sort_order",
            "index_categories_sort_order",
            "index_categories_name",
            "index_shopping_history_completed_at",
            "index_shopping_history_original_list_id",
            "index_history_items_history_id"
        )
        for (index in expectedIndices) {
            assertTrue("Schema must define index $index", schemaContent.contains("\"name\": \"$index\""))
        }
    }

    @Test
    fun testDatabaseConfiguration_exportsSchemaAndDisallowsDestructiveMigration() {
        val dbAnnotation = ShopZenDatabase::class.java.getAnnotation(Database::class.java)
        // Check database class definition in source to verify exportSchema and version
        val sourceFile = listOf(
            File("src/main/java/com/barakwebsoft/shopzen/data/local/database/ShopZenDatabase.kt"),
            File("app/src/main/java/com/barakwebsoft/shopzen/data/local/database/ShopZenDatabase.kt")
        ).firstOrNull { it.exists() }
        assertNotNull("ShopZenDatabase.kt source file must exist", sourceFile)

        val source = sourceFile!!.readText()
        assertTrue("ShopZenDatabase must specify version = 1", source.contains("version = 1"))
        assertTrue("ShopZenDatabase must export schema", source.contains("exportSchema = true"))
        assertTrue(
            "ShopZenDatabase must register explicit migrations",
            source.contains(".addMigrations(*Migrations.ALL_MIGRATIONS)")
        )
        assertFalse(
            "Production database MUST NOT use fallbackToDestructiveMigration()",
            source.contains("fallbackToDestructiveMigration")
        )
    }

    @Test
    fun testMigrationsRegistry_isDefinedForVersion1() {
        assertNotNull("Migrations.ALL_MIGRATIONS must not be null", Migrations.ALL_MIGRATIONS)
        assertEquals("Baseline V1 has 0 prior migrations", 0, Migrations.ALL_MIGRATIONS.size)
    }

    @Test
    fun testSimulatedForwardMigration_executesSqlStatementsCorrectly() {
        val executedStatements = mutableListOf<String>()

        // Create a simulated forward migration from V1 to V2
        val migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE shopping_lists ADD COLUMN notes TEXT DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_shopping_lists_notes ON shopping_lists(notes)")
            }
        }

        assertEquals(1, migration1To2.startVersion)
        assertEquals(2, migration1To2.endVersion)

        // Mock SupportSQLiteDatabase using dynamic proxy to record executed SQL
        val fakeDb = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedStatements.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        migration1To2.migrate(fakeDb)

        assertEquals(2, executedStatements.size)
        assertEquals(
            "ALTER TABLE shopping_lists ADD COLUMN notes TEXT DEFAULT NULL",
            executedStatements[0]
        )
        assertEquals(
            "CREATE INDEX IF NOT EXISTS index_shopping_lists_notes ON shopping_lists(notes)",
            executedStatements[1]
        )
    }
}
