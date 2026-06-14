package com.example.lofinotes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotasAdapter(
    private var listaNotas: List<Map<String, Any>>,
    private val onNotaClick: (Map<String, Any>) -> Unit
) : RecyclerView.Adapter<NotasAdapter.NotaViewHolder>() {

    class NotaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitulo: TextView = view.findViewById(R.id.itemTitulo)
        val tvContenido: TextView = view.findViewById(R.id.itemContenido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_nota, parent, false)
        return NotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotaViewHolder, position: Int) {
        val nota = listaNotas[position]
        holder.tvTitulo.text = nota["titulo"] as? String ?: ""
        holder.tvContenido.text = nota["contenido"] as? String ?: ""

        holder.itemView.setOnClickListener { onNotaClick(nota) }
    }

    override fun getItemCount(): Int = listaNotas.size

    fun actualizarLista(nuevaLista: List<Map<String, Any>>) {
        listaNotas = nuevaLista
        notifyDataSetChanged()
    }
}