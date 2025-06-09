package com.example.apka_zaliczeniowa

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.apka_zaliczeniowa.Transaction

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    fun getById(id: Int): Transaction

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllSorted(): List<Transaction>

    @Insert
    fun insertAll(vararg transaction: Transaction)

    @Delete
    fun delete(transaction: Transaction)

    @Update
    fun update (vararg transaction: Transaction)
}