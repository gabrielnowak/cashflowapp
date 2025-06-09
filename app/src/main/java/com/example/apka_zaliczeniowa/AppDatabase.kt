package com.example.apka_zaliczeniowa

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = arrayOf(Transaction::class), version = 3)
abstract class AppDatabase : RoomDatabase(){
    abstract fun transactionDao():TransactionDao
}