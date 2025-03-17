// Adjusted `ZakazkyAdapter` to set text color based on the specified conditions.

package com.example.AINDilDoch

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.AINDilDoch.R

class ZakazkyAdapter(private val zakazky: MutableList<Zakazka>) :
    RecyclerView.Adapter<ZakazkyAdapter.ViewHolder>() {

    // ViewHolder to hold item views
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val datum_od: TextView = itemView.findViewById(R.id.datumOd)
        val datum_do: TextView = itemView.findViewById(R.id.datumDo)
        val reference: TextView = itemView.findViewById(R.id.reference)
        val nazev_subjektu: TextView = itemView.findViewById(R.id.nazevSubjektu)
    }

    // Inflates the item view and creates ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.zakazka_list_item, parent, false)
        return ViewHolder(view)
    }

    // Binds data to the item view at the given position
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val zakazka = zakazky[position]

        holder.datum_od.text = zakazka.datumOd
        holder.datum_do.text = zakazka.datumDo ?: "N/A"
        holder.reference.text = zakazka.reference
        holder.nazev_subjektu.text = zakazka.nazevSubjektu

        // Logic for text color based on conditions
        when {
            zakazka.datumOd.isNotEmpty() && zakazka.datumDo!!.isEmpty() && zakazka.nazevSubjektu == "Docházka" -> {
                holder.datum_od.setTextColor(Color.parseColor("#009900"))
                holder.datum_do.setTextColor(Color.parseColor("#009900"))
                holder.reference.setTextColor(Color.parseColor("#009900"))
                holder.nazev_subjektu.setTextColor(Color.parseColor("#009900"))
            }
            zakazka.datumOd.isNotEmpty() && zakazka.datumDo!!.isEmpty() && zakazka.nazevSubjektu != "Docházka" -> {
                holder.datum_od.setTextColor(Color.BLUE)
                holder.datum_do.setTextColor(Color.BLUE)
                holder.reference.setTextColor(Color.BLUE)
                holder.nazev_subjektu.setTextColor(Color.BLUE)
            }
            zakazka.datumOd.isNotEmpty() && zakazka.datumDo!!.isNotEmpty() -> {
                holder.datum_od.setTextColor(Color.parseColor("#CC0000"))
                holder.datum_do.setTextColor(Color.parseColor("#CC0000"))
                holder.reference.setTextColor(Color.parseColor("#CC0000"))
                holder.nazev_subjektu.setTextColor(Color.parseColor("#CC0000"))
            }
        }
    }

    // Returns the total number of items
    override fun getItemCount(): Int = zakazky.size

    // Clears all data from the list and notifies the adapter
    fun clearData() {
        zakazky.clear()
        notifyDataSetChanged()
    }
}