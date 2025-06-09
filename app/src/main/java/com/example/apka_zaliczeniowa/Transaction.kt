package com.example.apka_zaliczeniowa

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id:Int,
            val label: String,
            val isCost: Boolean,
            val date: Long,
            val category: String,
            val amount: Double,
            val description: String) {
}