package com.example.apka_zaliczeniowa

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.example.apka_zaliczeniowa.databinding.ActivityAddTransactionBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.app.DatePickerDialog
import android.widget.ArrayAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTransaction : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private var selectedDate: Long = System.currentTimeMillis()
    private val incomeCategories = listOf("Wynagrodzenie", "Świadczenia socjalne", "Darowizna", "Zysk z inwestycji", "Sprzedaż", "Inne przychody")
    private val expenseCategories = listOf("Jedzenie", "Transport", "Rozrywka", "Inne", "Opłaty", "Zakupy")
    private val typeList = listOf("Wydatek", "Przychód")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ------------------ TYP TRANSAKCJI + dynamiczne kategorie -------------------
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, typeList)
        binding.costOrNoInput.setAdapter(typeAdapter)

        binding.costOrNoInput.setOnItemClickListener { _, _, position, _ ->
            val selectedType = typeList[position]
            val newCategoryList = if (selectedType == "Przychód") incomeCategories else expenseCategories

            val newCategoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, newCategoryList)
            binding.categoryDropdown.setAdapter(newCategoryAdapter)
            binding.categoryDropdown.setText("") // resetuj po zmianie
        }

        // ------------------ Domyślne kategorie -------------------
        val defaultCategoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, expenseCategories)
        binding.categoryDropdown.setAdapter(defaultCategoryAdapter)

        // ------------------ WYBÓR DATY -------------------
        binding.datePickerInput.setText(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(selectedDate)))
        binding.datePickerInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.timeInMillis
                    binding.datePickerInput.setText(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(selectedDate)))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // ------------------ WALIDACJA -------------------
        binding.labelInput.addTextChangedListener {
            if (it?.isNotEmpty() == true) binding.labelLayout.error = null
        }
        binding.amountInput.addTextChangedListener {
            if (it?.isNotEmpty() == true) binding.amountLayout.error = null
        }

        // ------------------ DODAJ TRANSAKCJĘ -------------------
        binding.addBtn.setOnClickListener {
            val label = binding.labelInput.text.toString()
            val description = binding.descriptionInput.text.toString()
            val amount = binding.amountInput.text.toString().toDoubleOrNull()
            val category = binding.categoryDropdown.text.toString()
            val type = binding.costOrNoInput.text.toString()

            val isCost = when (type) {
                "Wydatek" -> true
                "Przychód" -> false
                else -> null
            }

            var isValid = true
            if (label.isEmpty()) {
                binding.labelLayout.error = "Wprowadź nazwę"
                isValid = false
            }
            if (amount == null) {
                binding.amountLayout.error = "Niepoprawna kwota"
                isValid = false
            }
            if (isCost == null) {
                binding.costOrNoLayout.error = "Wybierz typ transakcji"
                isValid = false
            }
            if (category.isEmpty()) {
                binding.categoryLayout.error = "Wybierz kategorię"
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            val finalAmount = if (isCost == true) -kotlin.math.abs(amount!!) else kotlin.math.abs(amount!!)

            val transaction = Transaction(
                id = 0,
                label = label,
                isCost = isCost!!,
                date = selectedDate,
                category = category,
                amount = finalAmount,
                description = description
            )

            insert(transaction)
        }

        // ------------------ ZAMKNIJ -------------------
        binding.closeBtn.setOnClickListener { finish() }
    }

    private fun insert(transaction: Transaction) {
        val db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "transactions"
        ).fallbackToDestructiveMigration().build()

        GlobalScope.launch {
            db.transactionDao().insertAll(transaction)
            runOnUiThread { finish() }
        }
    }
}