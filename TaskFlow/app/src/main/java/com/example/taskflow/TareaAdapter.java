package com.example.taskflow;

import android.content.Intent; // <--- IMPORTANTE: Se añade este import
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    private List<Tarea> listaTareas;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
        void onDuplicateClick(int position);
    }

    public TareaAdapter(List<Tarea> listaTareas, OnItemClickListener listener) {
        this.listaTareas = listaTareas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarea, parent, false);
        return new TareaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        Tarea tarea = listaTareas.get(position);

        // 1. Rellenar datos visuales
        holder.tvTitulo.setText(tarea.getTitulo());
        holder.tvFecha.setText(tarea.getFechaHora());
        holder.tvDescripcion.setText(tarea.getDescripcion());
        holder.tvUbicacion.setText(tarea.getUbicacion());

        // 2. Controlar la expansión (Acordeón)
        boolean isExpanded = tarea.isExpanded();
        holder.layoutDetallesHidden.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        if (isExpanded) {
            holder.imgArrowIcon.setImageResource(R.drawable.ic_arrow_up);
        } else {
            holder.imgArrowIcon.setImageResource(R.drawable.ic_arrow_down);
        }

        // Listener para expandir
        View.OnClickListener expandListener = v -> {
            tarea.setExpanded(!tarea.isExpanded());
            notifyItemChanged(holder.getAdapterPosition());
        };
        holder.itemView.setOnClickListener(expandListener);
        holder.cardArrow.setOnClickListener(expandListener);

        // 3. Botón Editar
        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(position);
        });

        // === 4. BOTÓN COMPARTIR (GMAIL) ===
        holder.btnCompartir.setOnClickListener(v -> {
            // Construimos el mensaje con todos los datos
            String asunto = "Tarea compartida: " + tarea.getTitulo();
            String mensaje = "Hola, te comparto esta tarea de TaskFlow:\n\n" +
                    "📌 *Tarea:* " + tarea.getTitulo() + "\n" +
                    "📅 *Fecha:* " + tarea.getFechaHora() + "\n" +
                    "📝 *Descripción:* " + tarea.getDescripcion() + "\n" +
                    "📍 *Ubicación:* " + tarea.getUbicacion();

            // Creamos el Intent para compartir
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain"); // Indicamos que vamos a enviar texto
            intent.putExtra(Intent.EXTRA_SUBJECT, asunto); // Asunto del correo
            intent.putExtra(Intent.EXTRA_TEXT, mensaje);   // Cuerpo del mensaje

            // Lanzamos el menú del sistema (saldrá Gmail, WhatsApp, etc.)
            v.getContext().startActivity(Intent.createChooser(intent, "Compartir vía..."));
        });

        // 5. Botón Menú (3 puntos)
        holder.btnMenuOpciones.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.btnMenuOpciones);
            popup.getMenu().add("Duplicar");
            popup.getMenu().add("Eliminar");

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (listener == null) return false;
                    if (item.getTitle().equals("Duplicar")) {
                        listener.onDuplicateClick(position);
                        return true;
                    } else if (item.getTitle().equals("Eliminar")) {
                        listener.onDeleteClick(position);
                        return true;
                    }
                    return false;
                }
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() { return listaTareas.size(); }

    public static class TareaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvFecha, tvDescripcion, tvUbicacion;
        LinearLayout layoutDetallesHidden;
        View cardArrow;
        ImageView imgArrowIcon, btnEditar, btnCompartir, btnMenuOpciones;

        public TareaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTareaTitulo);
            tvFecha = itemView.findViewById(R.id.tvTareaFecha);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionBody);
            tvUbicacion = itemView.findViewById(R.id.tvUbicacionBody);
            layoutDetallesHidden = itemView.findViewById(R.id.layoutDetallesHidden);
            cardArrow = itemView.findViewById(R.id.cardArrow);
            imgArrowIcon = itemView.findViewById(R.id.imgArrowIcon);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnCompartir = itemView.findViewById(R.id.btnCompartir);
            btnMenuOpciones = itemView.findViewById(R.id.btnMenuOpciones);
        }
    }
}