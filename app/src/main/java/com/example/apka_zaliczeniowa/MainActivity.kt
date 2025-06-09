package com.example.apka_zaliczeniowa

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.apka_zaliczeniowa.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var transactions: List<Transaction>
    private lateinit var deletedTransaction: Transaction

    private var fromDate: Long = 0
    private var toDate: Long = Long.MAX_VALUE
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Inicjalizacja bazy danych
        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "transactions"
        ).fallbackToDestructiveMigration().build()

        // Domyślny zakres dat: pierwszy i ostatni dzień miesiąca
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        fromDate = calendar.timeInMillis
        binding.fromDate.text = dateFormat.format(Date(fromDate))

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        toDate = calendar.timeInMillis
        binding.toDate.text = dateFormat.format(Date(toDate))

        // Konfiguracja filtrowania po kliknięciu na daty
        binding.fromDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(y, m, d, 0, 0, 0)
                fromDate = calendar.timeInMillis
                binding.fromDate.text = dateFormat.format(Date(fromDate))
                fetchFiltered()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.toDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(y, m, d, 23, 59, 59)
                toDate = calendar.timeInMillis
                binding.toDate.text = dateFormat.format(Date(toDate))
                fetchFiltered()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Adapter i layout manager
        transactions = listOf()
        transactionAdapter = TransactionAdapter(transactions) { transaction ->
            val intent = Intent(this, TransactionDetailsActivity::class.java)
            intent.putExtra("TRANSACTION_ID", transaction.id)
            startActivity(intent)
        }

        binding.recyclerview.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        // Swipe to delete
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                deleteTransaction(transactions[vh.adapterPosition])
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.recyclerview)

        // Przycisk dodawania
        binding.addBtn.setOnClickListener {
            val intent = Intent(this, AddTransaction::class.java)
            startActivity(intent)
        }

        // Wczytaj dane po uruchomieniu
        fetchFiltered()
    }

    override fun onResume() {
        super.onResume()
        fetchFiltered()
    }

    private fun fetchFiltered() {
        GlobalScope.launch {
            val allSorted = db.transactionDao().getAllSorted()
            transactions = allSorted.filter {
                it.date in fromDate..toDate }
            runOnUiThread {
                updateDashboard()
                transactionAdapter.setData(transactions)
            }
        }
    }

    private fun updateDashboard() {
        val totalAmount = transactions.sumOf { it.amount }
        val budgetAmount = transactions.filter { it.amount > 0 }.sumOf { it.amount }
        val expenseAmount = totalAmount - budgetAmount

        binding.balance.text = "PLN %.2f".format(totalAmount)
        binding.budget.text = "PLN %.2f".format(budgetAmount)
        binding.expense.text = "PLN %.2f".format(expenseAmount)
    }

    private fun deleteTransaction(transaction: Transaction) {
        deletedTransaction = transaction
        GlobalScope.launch {
            db.transactionDao().delete(transaction)
            fetchFiltered()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_summary -> {
                Toast.makeText(this, "Zestawienia (todo)", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_premium -> {
                Toast.makeText(this, "Kup wersję premium (todo)", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_authors -> {
                Toast.makeText(this, "Autorzy: (todo)", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
