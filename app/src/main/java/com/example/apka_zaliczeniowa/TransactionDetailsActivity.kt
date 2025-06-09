package com.example.apka_zaliczeniowa
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.apka_zaliczeniowa.AppDatabase
import com.example.apka_zaliczeniowa.Transaction
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
class TransactionDetailsActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var transactionId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)

        db = Room.databaseBuilder(this, AppDatabase::class.java, "transactions")
            .fallbackToDestructiveMigration()
            .build()

        transactionId = intent.getIntExtra("TRANSACTION_ID", -1)

        if (transactionId == -1) {
            finish()
            return
        }

        val label = findViewById<TextView>(R.id.detailLabel)
        val amount = findViewById<TextView>(R.id.detailAmount)
        val category = findViewById<TextView>(R.id.detailCategory)
        val type = findViewById<TextView>(R.id.detailType)
        val date = findViewById<TextView>(R.id.detailDate)
        val description = findViewById<TextView>(R.id.detailDescription)
        val deleteBtn = findViewById<Button>(R.id.deleteBtn)
        val backBtn = findViewById<Button>(R.id.backBtn)

        GlobalScope.launch {
            val transaction = db.transactionDao().getById(transactionId)

            runOnUiThread {
                label.text = "Nazwa: ${transaction.label}"
                amount.text = "Kwota: ${transaction.amount} PLN"
                category.text = "Kategoria: ${transaction.category}"
                type.text = "Typ: ${if (transaction.isCost) "Wydatek" else "Przychód"}"
                date.text = "Data: ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(transaction.date))}"
                description.text = "Opis: ${transaction.description}"

                deleteBtn.setOnClickListener {
                    GlobalScope.launch {
                        db.transactionDao().delete(transaction)
                        runOnUiThread {
                            Toast.makeText(this@TransactionDetailsActivity, "Usunięto", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }

                backBtn.setOnClickListener { finish() }
            }
        }
    }
}