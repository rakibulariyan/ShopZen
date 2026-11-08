package com.barakwebsoft.shopzen.data.local

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.UUID
import java.util.regex.Pattern

/**
 * End-to-end SQLite integration tests for ShopZen local data foundation (SZ-015).
 * Validates SQLite schema DDL exported by Room v1, foreign key constraints (CASCADE, SET NULL),
 * transaction atomicity and rollbacks, CRUD operations, and cross-session file persistence.
 */
class RoomSqliteIntegrationTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var connection: Connection

    @Before
    fun setUp() {
        connection = createConnection("jdbc:sqlite::memory:")
        initSchema(connection)
    }

    @After
    fun tearDown() {
        if (!connection.isClosed) {
            connection.close()
        }
    }

    private fun createConnection(url: String): Connection {
        val conn = DriverManager.getConnection(url)
        conn.createStatement().use { stmt ->
            // In SQLite, foreign key enforcement must be explicitly enabled per connection
            stmt.execute("PRAGMA foreign_keys = ON;")
        }
        return conn
    }

    private fun initSchema(conn: Connection) {
        val schemaFile = listOf(
            File("schemas/com.barakwebsoft.shopzen.data.local.database.ShopZenDatabase/1.json"),
            File("app/schemas/com.barakwebsoft.shopzen.data.local.database.ShopZenDatabase/1.json")
        ).firstOrNull { it.exists() }
            ?: throw IllegalStateException("Room schema 1.json not found")

        val text = schemaFile.readText()
        val entityPattern = Pattern.compile("\"tableName\":\\s*\"([^\"]+)\"([\\s\\S]*?)(?=\"tableName\"|\"setupQueries\"|\\Z)")
        val createSqlPattern = Pattern.compile("\"createSql\":\\s*\"([^\"]+)\"")
        val matcher = entityPattern.matcher(text)

        conn.createStatement().use { stmt ->
            while (matcher.find()) {
                val tableName = matcher.group(1)!!
                val block = matcher.group(2)!!
                val sqlMatcher = createSqlPattern.matcher(block)
                while (sqlMatcher.find()) {
                    val rawSql = sqlMatcher.group(1)!!
                    val sql = rawSql.replace("\${TABLE_NAME}", tableName)
                    stmt.execute(sql)
                }
            }
        }
    }

    // =========================================================================
    // 1. Schema Initialization Tests
    // =========================================================================

    @Test
    fun schema_allTablesAndIndicesAreCreated() {
        val expectedTables = setOf(
            "shopping_lists",
            "shopping_items",
            "categories",
            "shopping_history",
            "history_items"
        )

        val actualTables = mutableSetOf<String>()
        connection.createStatement().use { stmt ->
            val rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';")
            while (rs.next()) {
                actualTables.add(rs.getString("name"))
            }
        }

        assertEquals("All Room entities must exist in SQLite", expectedTables, actualTables)

        // Verify foreign keys pragma is active
        connection.createStatement().use { stmt ->
            val rs = stmt.executeQuery("PRAGMA foreign_keys;")
            assertTrue("PRAGMA foreign_keys must be enabled", rs.next() && rs.getInt(1) == 1)
        }
    }

    // =========================================================================
    // 2. Foreign Key Constraint & Cascade Deletion Tests
    // =========================================================================

    @Test
    fun foreignKey_shoppingItem_rejectsOrphanInsert() {
        val orphanListId = UUID.randomUUID().toString()
        val itemId = UUID.randomUUID().toString()

        try {
            connection.prepareStatement(
                "INSERT INTO shopping_items (id, list_id, name, quantity, unit, is_purchased, sort_order, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);"
            ).use { stmt ->
                stmt.setString(1, itemId)
                stmt.setString(2, orphanListId)
                stmt.setString(3, "Milk")
                stmt.setString(4, "1")
                stmt.setString(5, "bottle")
                stmt.setInt(6, 0)
                stmt.setInt(7, 0)
                stmt.setLong(8, System.currentTimeMillis())
                stmt.setLong(9, System.currentTimeMillis())
                stmt.executeUpdate()
            }
            fail("Expected SQLException for FOREIGN KEY constraint failure when list_id does not exist")
        } catch (e: SQLException) {
            assertTrue("Exception must be foreign key constraint violation", e.message?.contains("FOREIGN KEY") == true)
        }
    }

    @Test
    fun foreignKey_shoppingItem_cascadeDeleteWhenListIsDeleted() {
        val listId = insertShoppingList("Groceries", "USD")
        insertShoppingItem(listId, "Apples", "3", "pcs", 150L)
        insertShoppingItem(listId, "Bread", "1", "loaf", 250L)
        insertShoppingItem(listId, "Milk", "2", "cartons", 300L)

        assertEquals(3, countRows("shopping_items", "list_id = '$listId'"))

        // Delete parent shopping list
        connection.createStatement().use { stmt ->
            stmt.executeUpdate("DELETE FROM shopping_lists WHERE id = '$listId';")
        }

        assertEquals(0, countRows("shopping_lists", "id = '$listId'"))
        // Cascade delete must clean up all child shopping items
        assertEquals("Child items must be automatically deleted via CASCADE", 0, countRows("shopping_items", "list_id = '$listId'"))
    }

    @Test
    fun foreignKey_category_setNullOnItemWhenCategoryIsDeleted() {
        val categoryId = insertCategory("Dairy", "dairy_icon", 1)
        val listId = insertShoppingList("Weekly", "USD")
        val itemId = insertShoppingItem(listId, "Cheese", "1", "block", 500L, categoryId = categoryId)

        // Verify category is linked
        connection.createStatement().use { stmt ->
            val rs = stmt.executeQuery("SELECT category_id FROM shopping_items WHERE id = '$itemId';")
            assertTrue(rs.next())
            assertEquals(categoryId, rs.getString("category_id"))
        }

        // Delete the category
        connection.createStatement().use { stmt ->
            stmt.executeUpdate("DELETE FROM categories WHERE id = '$categoryId';")
        }

        assertEquals(0, countRows("categories", "id = '$categoryId'"))

        // The item must still exist, with category_id set to NULL
        connection.createStatement().use { stmt ->
            val rs = stmt.executeQuery("SELECT category_id FROM shopping_items WHERE id = '$itemId';")
            assertTrue("Item must still exist after category deletion", rs.next())
            assertNull("Item category_id must be set to NULL via SET NULL rule", rs.getString("category_id"))
        }
    }

    @Test
    fun foreignKey_historyItem_rejectsOrphanInsert() {
        val orphanHistoryId = UUID.randomUUID().toString()
        val itemId = UUID.randomUUID().toString()

        try {
            connection.prepareStatement(
                "INSERT INTO history_items (id, history_id, item_name, quantity, unit, is_purchased) " +
                    "VALUES (?, ?, ?, ?, ?, ?);"
            ).use { stmt ->
                stmt.setString(1, itemId)
                stmt.setString(2, orphanHistoryId)
                stmt.setString(3, "Snapshot Apples")
                stmt.setString(4, "2")
                stmt.setString(5, "kg")
                stmt.setInt(6, 1)
                stmt.executeUpdate()
            }
            fail("Expected SQLException for FOREIGN KEY constraint failure when history_id does not exist")
        } catch (e: SQLException) {
            assertTrue("Exception must be foreign key constraint violation", e.message?.contains("FOREIGN KEY") == true)
        }
    }

    @Test
    fun foreignKey_historyItem_cascadeDeleteWhenHistoryIsDeleted() {
        val historyId = insertHistory("Weekly Market", "USD", 5000L, 4500L, 2, 0)
        insertHistoryItem(historyId, "Apples", "2", "kg", 200L, 400L, true)
        insertHistoryItem(historyId, "Bread", "1", "loaf", 250L, 250L, true)

        assertEquals(2, countRows("history_items", "history_id = '$historyId'"))

        // Delete history header
        connection.createStatement().use { stmt ->
            stmt.executeUpdate("DELETE FROM shopping_history WHERE id = '$historyId';")
        }

        assertEquals(0, countRows("shopping_history", "id = '$historyId'"))
        assertEquals("History items must be cascade deleted", 0, countRows("history_items", "history_id = '$historyId'"))
    }

    // =========================================================================
    // 3. CRUD Operations Tests
    // =========================================================================

    @Test
    fun crud_shoppingList_insertQueryUpdateDelete() {
        val listId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        // 1. Insert
        connection.prepareStatement(
            "INSERT INTO shopping_lists (id, name, description, currency_code, budget_minor, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?);"
        ).use { stmt ->
            stmt.setString(1, listId)
            stmt.setString(2, "Weekend BBQ")
            stmt.setString(3, "BBQ supplies and meat")
            stmt.setString(4, "EUR")
            stmt.setLong(5, 7500L)
            stmt.setString(6, "ACTIVE")
            stmt.setLong(7, now)
            stmt.setLong(8, now)
            stmt.executeUpdate()
        }

        // 2. Query
        connection.prepareStatement("SELECT * FROM shopping_lists WHERE id = ?;").use { stmt ->
            stmt.setString(1, listId)
            val rs = stmt.executeQuery()
            assertTrue(rs.next())
            assertEquals("Weekend BBQ", rs.getString("name"))
            assertEquals("BBQ supplies and meat", rs.getString("description"))
            assertEquals("EUR", rs.getString("currency_code"))
            assertEquals(7500L, rs.getLong("budget_minor"))
            assertEquals("ACTIVE", rs.getString("status"))
        }

        // 3. Update
        val updatedTime = now + 1000
        connection.prepareStatement(
            "UPDATE shopping_lists SET name = ?, budget_minor = ?, updated_at = ? WHERE id = ?;"
        ).use { stmt ->
            stmt.setString(1, "Summer BBQ")
            stmt.setLong(2, 9000L)
            stmt.setLong(3, updatedTime)
            stmt.setString(4, listId)
            stmt.executeUpdate()
        }

        connection.prepareStatement("SELECT name, budget_minor, updated_at FROM shopping_lists WHERE id = ?;").use { stmt ->
            stmt.setString(1, listId)
            val rs = stmt.executeQuery()
            assertTrue(rs.next())
            assertEquals("Summer BBQ", rs.getString("name"))
            assertEquals(9000L, rs.getLong("budget_minor"))
            assertEquals(updatedTime, rs.getLong("updated_at"))
        }

        // 4. Delete
        connection.prepareStatement("DELETE FROM shopping_lists WHERE id = ?;").use { stmt ->
            stmt.setString(1, listId)
            stmt.executeUpdate()
        }
        assertEquals(0, countRows("shopping_lists", "id = '$listId'"))
    }

    @Test
    fun crud_shoppingItem_exactValuesAndOrdering() {
        val listId = insertShoppingList("Items Test", "USD")

        // Insert items with distinct sort_orders and exact string quantities
        val id1 = insertShoppingItem(listId, "Item C", "1.75", "kg", 350L, sortOrder = 2)
        val id2 = insertShoppingItem(listId, "Item A", "0.5", "liter", 200L, sortOrder = 0)
        val id3 = insertShoppingItem(listId, "Item B", "10", "pcs", 100L, sortOrder = 1)

        // Query ordered by sort_order ASC
        val orderedNames = mutableListOf<String>()
        val quantities = mutableListOf<String>()
        connection.prepareStatement(
            "SELECT name, quantity FROM shopping_items WHERE list_id = ? ORDER BY sort_order ASC;"
        ).use { stmt ->
            stmt.setString(1, listId)
            val rs = stmt.executeQuery()
            while (rs.next()) {
                orderedNames.add(rs.getString("name"))
                quantities.add(rs.getString("quantity"))
            }
        }

        assertEquals(listOf("Item A", "Item B", "Item C"), orderedNames)
        assertEquals(listOf("0.5", "10", "1.75"), quantities)

        // Update purchase state and actual unit price
        connection.prepareStatement(
            "UPDATE shopping_items SET is_purchased = 1, actual_unit_price_minor = ? WHERE id = ?;"
        ).use { stmt ->
            stmt.setLong(1, 190L)
            stmt.setString(2, id2)
            stmt.executeUpdate()
        }

        connection.prepareStatement("SELECT is_purchased, actual_unit_price_minor FROM shopping_items WHERE id = ?;").use { stmt ->
            stmt.setString(1, id2)
            val rs = stmt.executeQuery()
            assertTrue(rs.next())
            assertEquals(1, rs.getInt("is_purchased"))
            assertEquals(190L, rs.getLong("actual_unit_price_minor"))
        }
    }

    @Test
    fun crud_categories_seedAndReorder() {
        // Seed default categories
        val defaultCategories = listOf(
            Triple("cat-1", "Produce", 0),
            Triple("cat-2", "Dairy", 1),
            Triple("cat-3", "Bakery", 2),
            Triple("cat-4", "Meat & Seafood", 3)
        )

        connection.prepareStatement(
            "INSERT INTO categories (id, name, icon_name, is_default, sort_order) VALUES (?, ?, 'icon', 1, ?);"
        ).use { stmt ->
            for (cat in defaultCategories) {
                stmt.setString(1, cat.first)
                stmt.setString(2, cat.second)
                stmt.setInt(3, cat.third)
                stmt.addBatch()
            }
            stmt.executeBatch()
        }

        assertEquals(4, countRows("categories"))

        // Query sorted by sort_order
        val categories = mutableListOf<String>()
        connection.createStatement().use { stmt ->
            val rs = stmt.executeQuery("SELECT name FROM categories ORDER BY sort_order ASC;")
            while (rs.next()) {
                categories.add(rs.getString("name"))
            }
        }
        assertEquals(listOf("Produce", "Dairy", "Bakery", "Meat & Seafood"), categories)
    }

    // =========================================================================
    // 4. Transaction Atomicity & Rollback Tests
    // =========================================================================

    @Test
    fun transaction_atomicArchiveList_commitsSuccessfully() {
        val listId = insertShoppingList("Party Groceries", "USD", 10000L)
        insertShoppingItem(listId, "Soda", "4", "bottles", 200L, isPurchased = true, actualPrice = 200L)
        insertShoppingItem(listId, "Chips", "2", "bags", 300L, isPurchased = true, actualPrice = 350L)
        insertShoppingItem(listId, "Napkins", "1", "pack", 150L, isPurchased = false)

        val historyId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        // Execute archive transaction atomically
        connection.autoCommit = false
        try {
            // 1. Insert history row
            connection.prepareStatement(
                "INSERT INTO shopping_history (id, original_list_id, list_name, currency_code, budget_minor, total_spent_minor, purchased_count, unpriced_purchased_count, completed_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);"
            ).use { stmt ->
                stmt.setString(1, historyId)
                stmt.setString(2, listId)
                stmt.setString(3, "Party Groceries")
                stmt.setString(4, "USD")
                stmt.setLong(5, 10000L)
                stmt.setLong(6, 1500L) // (4*200) + (2*350)
                stmt.setInt(7, 2)
                stmt.setInt(8, 0)
                stmt.setLong(9, now)
                stmt.executeUpdate()
            }

            // 2. Insert snapshot items
            connection.prepareStatement(
                "INSERT INTO history_items (id, history_id, item_name, quantity, unit, actual_unit_price_minor, line_total_minor, is_purchased) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?);"
            ).use { stmt ->
                // Item 1
                stmt.setString(1, UUID.randomUUID().toString())
                stmt.setString(2, historyId)
                stmt.setString(3, "Soda")
                stmt.setString(4, "4")
                stmt.setString(5, "bottles")
                stmt.setLong(6, 200L)
                stmt.setLong(7, 800L)
                stmt.setInt(8, 1)
                stmt.addBatch()

                // Item 2
                stmt.setString(1, UUID.randomUUID().toString())
                stmt.setString(2, historyId)
                stmt.setString(3, "Chips")
                stmt.setString(4, "2")
                stmt.setString(5, "bags")
                stmt.setLong(6, 350L)
                stmt.setLong(7, 700L)
                stmt.setInt(8, 1)
                stmt.addBatch()

                stmt.executeBatch()
            }

            // 3. Mark shopping list as ARCHIVED
            connection.prepareStatement(
                "UPDATE shopping_lists SET status = 'ARCHIVED', completed_at = ?, updated_at = ? WHERE id = ?;"
            ).use { stmt ->
                stmt.setLong(1, now)
                stmt.setLong(2, now)
                stmt.setString(3, listId)
                stmt.executeUpdate()
            }

            connection.commit()
        } catch (e: Exception) {
            connection.rollback()
            fail("Archive transaction failed: ${e.message}")
        } finally {
            connection.autoCommit = true
        }

        // Verify history row exists
        assertEquals(1, countRows("shopping_history", "id = '$historyId'"))
        assertEquals(2, countRows("history_items", "history_id = '$historyId'"))

        // Verify list is ARCHIVED
        connection.prepareStatement("SELECT status, completed_at FROM shopping_lists WHERE id = ?;").use { stmt ->
            stmt.setString(1, listId)
            val rs = stmt.executeQuery()
            assertTrue(rs.next())
            assertEquals("ARCHIVED", rs.getString("status"))
            assertEquals(now, rs.getLong("completed_at"))
        }

        // Active list items must remain intact for reference
        assertEquals(3, countRows("shopping_items", "list_id = '$listId'"))
    }

    @Test
    fun transaction_atomicArchiveList_rollsBackOnFailure() {
        val listId = insertShoppingList("Rollback List", "USD")
        insertShoppingItem(listId, "Item 1", "1", "pcs", 100L)

        val historyId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        connection.autoCommit = false
        var exceptionThrown = false

        try {
            // 1. Insert history header
            connection.prepareStatement(
                "INSERT INTO shopping_history (id, original_list_id, list_name, currency_code, budget_minor, total_spent_minor, purchased_count, unpriced_purchased_count, completed_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);"
            ).use { stmt ->
                stmt.setString(1, historyId)
                stmt.setString(2, listId)
                stmt.setString(3, "Rollback List")
                stmt.setString(4, "USD")
                stmt.setNull(5, java.sql.Types.BIGINT)
                stmt.setLong(6, 100L)
                stmt.setInt(7, 1)
                stmt.setInt(8, 0)
                stmt.setLong(9, now)
                stmt.executeUpdate()
            }

            // 2. Deliberate failure: insert invalid history item with invalid foreign key
            connection.prepareStatement(
                "INSERT INTO history_items (id, history_id, item_name, quantity, unit, is_purchased) " +
                    "VALUES (?, ?, ?, ?, ?, ?);"
            ).use { stmt ->
                stmt.setString(1, UUID.randomUUID().toString())
                stmt.setString(2, "non-existent-history-id-trigger-foreign-key-error")
                stmt.setString(3, "Failed Item")
                stmt.setString(4, "1")
                stmt.setString(5, "pcs")
                stmt.setInt(6, 1)
                stmt.executeUpdate()
            }

            // 3. Mark list as archived (should not reach here)
            connection.prepareStatement("UPDATE shopping_lists SET status = 'ARCHIVED' WHERE id = ?;").use { stmt ->
                stmt.setString(1, listId)
                stmt.executeUpdate()
            }

            connection.commit()
        } catch (e: SQLException) {
            connection.rollback()
            exceptionThrown = true
        } finally {
            connection.autoCommit = true
        }

        assertTrue("Transaction must throw SQLException and rollback", exceptionThrown)

        // Verify NO orphan history row exists
        assertEquals("History row must be rolled back", 0, countRows("shopping_history", "id = '$historyId'"))
        assertEquals("History items must be rolled back", 0, countRows("history_items", "history_id = '$historyId'"))

        // Verify list status is STILL ACTIVE (not archived)
        connection.prepareStatement("SELECT status FROM shopping_lists WHERE id = ?;").use { stmt ->
            stmt.setString(1, listId)
            val rs = stmt.executeQuery()
            assertTrue(rs.next())
            assertEquals("List must remain ACTIVE after transaction rollback", "ACTIVE", rs.getString("status"))
        }
    }

    // =========================================================================
    // 5. Database Persistence Tests
    // =========================================================================

    @Test
    fun persistence_dataSurvivesDatabaseCloseAndReopen() {
        val dbFile = File(tempFolder.root, "shopzen_persist_test.db")
        val jdbcUrl = "jdbc:sqlite:${dbFile.absolutePath}"

        val listId = UUID.randomUUID().toString()
        val itemId = UUID.randomUUID().toString()

        // Session 1: Create schema, insert data, and close connection
        createConnection(jdbcUrl).use { conn1 ->
            initSchema(conn1)

            conn1.prepareStatement(
                "INSERT INTO shopping_lists (id, name, currency_code, budget_minor, status, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?);"
            ).use { stmt ->
                stmt.setString(1, listId)
                stmt.setString(2, "Persistent List")
                stmt.setString(3, "USD")
                stmt.setLong(4, 5000L)
                stmt.setString(5, "ACTIVE")
                stmt.setLong(6, 1000L)
                stmt.setLong(7, 1000L)
                stmt.executeUpdate()
            }

            conn1.prepareStatement(
                "INSERT INTO shopping_items (id, list_id, name, quantity, unit, estimated_unit_price_minor, is_purchased, sort_order, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
            ).use { stmt ->
                stmt.setString(1, itemId)
                stmt.setString(2, listId)
                stmt.setString(3, "Persistent Oats")
                stmt.setString(4, "2")
                stmt.setString(5, "kg")
                stmt.setLong(6, 450L)
                stmt.setInt(7, 0)
                stmt.setInt(8, 0)
                stmt.setLong(9, 1000L)
                stmt.setLong(10, 1000L)
                stmt.executeUpdate()
            }
        }

        assertTrue("Database file must exist on disk", dbFile.exists() && dbFile.length() > 0)

        // Session 2: Open brand new connection to the file and verify persistence
        createConnection(jdbcUrl).use { conn2 ->
            // Query list
            conn2.prepareStatement("SELECT name, budget_minor, status FROM shopping_lists WHERE id = ?;").use { stmt ->
                stmt.setString(1, listId)
                val rs = stmt.executeQuery()
                assertTrue("Persisted list must be found in new connection", rs.next())
                assertEquals("Persistent List", rs.getString("name"))
                assertEquals(5000L, rs.getLong("budget_minor"))
                assertEquals("ACTIVE", rs.getString("status"))
            }

            // Query item
            conn2.prepareStatement("SELECT name, quantity, estimated_unit_price_minor FROM shopping_items WHERE id = ?;").use { stmt ->
                stmt.setString(1, itemId)
                val rs = stmt.executeQuery()
                assertTrue("Persisted item must be found in new connection", rs.next())
                assertEquals("Persistent Oats", rs.getString("name"))
                assertEquals("2", rs.getString("quantity"))
                assertEquals(450L, rs.getLong("estimated_unit_price_minor"))
            }
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private fun insertShoppingList(
        name: String,
        currencyCode: String,
        budgetMinor: Long? = null,
        status: String = "ACTIVE"
    ): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        connection.prepareStatement(
            "INSERT INTO shopping_lists (id, name, currency_code, budget_minor, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);"
        ).use { stmt ->
            stmt.setString(1, id)
            stmt.setString(2, name)
            stmt.setString(3, currencyCode)
            if (budgetMinor != null) stmt.setLong(4, budgetMinor) else stmt.setNull(4, java.sql.Types.BIGINT)
            stmt.setString(5, status)
            stmt.setLong(6, now)
            stmt.setLong(7, now)
            stmt.executeUpdate()
        }
        return id
    }

    private fun insertShoppingItem(
        listId: String,
        name: String,
        quantity: String,
        unit: String,
        estimatedPrice: Long? = null,
        actualPrice: Long? = null,
        isPurchased: Boolean = false,
        categoryId: String? = null,
        sortOrder: Int = 0
    ): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        connection.prepareStatement(
            "INSERT INTO shopping_items (id, list_id, name, quantity, unit, category_id, estimated_unit_price_minor, actual_unit_price_minor, is_purchased, sort_order, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
        ).use { stmt ->
            stmt.setString(1, id)
            stmt.setString(2, listId)
            stmt.setString(3, name)
            stmt.setString(4, quantity)
            stmt.setString(5, unit)
            if (categoryId != null) stmt.setString(6, categoryId) else stmt.setNull(6, java.sql.Types.VARCHAR)
            if (estimatedPrice != null) stmt.setLong(7, estimatedPrice) else stmt.setNull(7, java.sql.Types.BIGINT)
            if (actualPrice != null) stmt.setLong(8, actualPrice) else stmt.setNull(8, java.sql.Types.BIGINT)
            stmt.setInt(9, if (isPurchased) 1 else 0)
            stmt.setInt(10, sortOrder)
            stmt.setLong(11, now)
            stmt.setLong(12, now)
            stmt.executeUpdate()
        }
        return id
    }

    private fun insertCategory(name: String, iconName: String, sortOrder: Int): String {
        val id = UUID.randomUUID().toString()
        connection.prepareStatement(
            "INSERT INTO categories (id, name, icon_name, is_default, sort_order) VALUES (?, ?, ?, 0, ?);"
        ).use { stmt ->
            stmt.setString(1, id)
            stmt.setString(2, name)
            stmt.setString(3, iconName)
            stmt.setInt(4, sortOrder)
            stmt.executeUpdate()
        }
        return id
    }

    private fun insertHistory(
        listName: String,
        currencyCode: String,
        budgetMinor: Long?,
        totalSpentMinor: Long,
        purchasedCount: Int,
        unpricedCount: Int
    ): String {
        val id = UUID.randomUUID().toString()
        connection.prepareStatement(
            "INSERT INTO shopping_history (id, list_name, currency_code, budget_minor, total_spent_minor, purchased_count, unpriced_purchased_count, completed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?);"
        ).use { stmt ->
            stmt.setString(1, id)
            stmt.setString(2, listName)
            stmt.setString(3, currencyCode)
            if (budgetMinor != null) stmt.setLong(4, budgetMinor) else stmt.setNull(4, java.sql.Types.BIGINT)
            stmt.setLong(5, totalSpentMinor)
            stmt.setInt(6, purchasedCount)
            stmt.setInt(7, unpricedCount)
            stmt.setLong(8, System.currentTimeMillis())
            stmt.executeUpdate()
        }
        return id
    }

    private fun insertHistoryItem(
        historyId: String,
        name: String,
        quantity: String,
        unit: String,
        actualPrice: Long?,
        lineTotal: Long?,
        isPurchased: Boolean
    ): String {
        val id = UUID.randomUUID().toString()
        connection.prepareStatement(
            "INSERT INTO history_items (id, history_id, item_name, quantity, unit, actual_unit_price_minor, line_total_minor, is_purchased) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?);"
        ).use { stmt ->
            stmt.setString(1, id)
            stmt.setString(2, historyId)
            stmt.setString(3, name)
            stmt.setString(4, quantity)
            stmt.setString(5, unit)
            if (actualPrice != null) stmt.setLong(6, actualPrice) else stmt.setNull(6, java.sql.Types.BIGINT)
            if (lineTotal != null) stmt.setLong(7, lineTotal) else stmt.setNull(7, java.sql.Types.BIGINT)
            stmt.setInt(8, if (isPurchased) 1 else 0)
            stmt.executeUpdate()
        }
        return id
    }

    private fun countRows(table: String, whereClause: String? = null): Int {
        val sql = if (whereClause != null) "SELECT COUNT(*) FROM $table WHERE $whereClause;" else "SELECT COUNT(*) FROM $table;"
        connection.createStatement().use { stmt ->
            val rs = stmt.executeQuery(sql)
            return if (rs.next()) rs.getInt(1) else 0
        }
    }
}

