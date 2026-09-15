package com.positivefinancial.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds budgets, goals, and recurring_items. Only ever ADDS new tables here —
 * none of the existing tables (accounts, transactions, categories,
 * credit_card_details) are touched, so a mistake in this migration can make
 * the app fail to open, but it cannot destroy data already on disk.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `budgets` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `categoryId` INTEGER NOT NULL,
                `monthlyLimit` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_budgets_categoryId` ON `budgets` (`categoryId`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `goals` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `goalType` TEXT NOT NULL,
                `targetAmount` INTEGER NOT NULL,
                `linkedAccountId` INTEGER NOT NULL,
                `targetDate` INTEGER,
                `iconKey` TEXT NOT NULL,
                `colorHex` TEXT NOT NULL,
                `isArchived` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`linkedAccountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_goals_linkedAccountId` ON `goals` (`linkedAccountId`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `recurring_items` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `amount` INTEGER NOT NULL,
                `accountId` INTEGER NOT NULL,
                `categoryId` INTEGER NOT NULL,
                `frequency` TEXT NOT NULL,
                `dayOfMonth` INTEGER NOT NULL,
                `monthOfYear` INTEGER NOT NULL,
                `autoCreateTransaction` INTEGER NOT NULL,
                `reminderDaysBefore` INTEGER NOT NULL,
                `isActive` INTEGER NOT NULL,
                `nextDueDate` INTEGER NOT NULL,
                `lastProcessedDate` INTEGER,
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_items_accountId` ON `recurring_items` (`accountId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_items_categoryId` ON `recurring_items` (`categoryId`)")
    }
}

/** Adds a column to recurring_items so a reminder fires once per cycle instead of every day. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `recurring_items` ADD COLUMN `reminderSentAt` INTEGER")
    }
}
