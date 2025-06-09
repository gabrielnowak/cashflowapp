package com.example.apka_zaliczeniowa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onTransactionClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionHolder>() {

    class TransactionHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.label)
        val amount: TextView = view.findViewById(R.id.amount)
        val container: View = view // Cały widok do kliknięcia
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_layout, parent, false)
        return TransactionHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        val transaction = transactions[position]
        val context = holder.amount.context

        // Formatowanie kwoty
        val formattedAmount = String.format("%.2f zł", kotlin.math.abs(transaction.amount))
        holder.amount.text = if (transaction.amount >= 0) "+$formattedAmount" else "-$formattedAmount"
        holder.amount.setTextColor(
            ContextCompat.getColor(
                context,
                if (transaction.amount >= 0) R.color.green else R.color.red
            )
        )

        holder.label.text = transaction.label

        // Kliknięcie w cały wiersz
        holder.container.setOnClickListener {
            onTransactionClick(transaction)
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun setData(transactions: List<Transaction>) {
        this.transactions = transactions
        notifyDataSetChanged()
    }
}