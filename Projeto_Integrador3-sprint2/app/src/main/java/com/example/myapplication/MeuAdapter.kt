package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MeuAdapter(private val listaRiscos: List<Risco>) : RecyclerView.Adapter<MeuAdapter.RiscoViewHolder>() {

    class RiscoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.titulo_risco)
        val data: TextView = itemView.findViewById(R.id.data_risco)
        val local: TextView = itemView.findViewById(R.id.local_risco)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiscoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return RiscoViewHolder(view)
    }

    override fun onBindViewHolder(holder: RiscoViewHolder, position: Int) {
        val risco = listaRiscos[position]
        holder.titulo.text = risco.descricao
        holder.data.text = risco.data
        holder.local.text = risco.localReferencia
    }

    override fun getItemCount(): Int = listaRiscos.size
}