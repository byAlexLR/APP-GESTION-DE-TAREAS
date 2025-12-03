package com.example.taskflow;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    private List<Tarea> listaTareas;
    private OnItemClickListener listener;

    // === INTERFAZ CORRECTA (Usa objetos Tarea para evitar errores de índice) ===
    public interface OnItemClickListener {
        void onEditClick(Tarea tarea);
        void onDeleteClick(Tarea tarea);
        void onDuplicateClick(Tarea tarea);
    }

    public TareaAdapter(List<Tarea> listaTareas, OnItemClickListener listener) {
        this.listaTareas = listaTareas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Asegúrate de que item_tarea.xml tiene la estructura completa (oculta y visible)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarea, parent, false);
        return new TareaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        Tarea tarea = listaTareas.get(position);

        // 1. RELLENAR DATOS
        holder.tvTitulo.setText(tarea.getTitulo());
        holder.tvFecha.setText(tarea.getFechaHora());
        holder.tvDescripcion.setText(tarea.getDescripcion());
        holder.tvUbicacion.setText(tarea.getUbicacion());

        // 2. CONTROLAR EXPANSIÓN (ACORDEÓN)
        boolean isExpanded = tarea.isExpanded();
        holder.layoutDetallesHidden.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        // Cambiar flecha visualmente
        if (isExpanded) {
            holder.imgArrowIcon.setImageResource(R.drawable.ic_arrow_up);
        } else {
            holder.imgArrowIcon.setImageResource(R.drawable.ic_arrow_down);
        }

        // Listener para expandir/contraer al tocar la tarjeta
        View.OnClickListener expandListener = v -> {
            tarea.setExpanded(!tarea.isExpanded());
            notifyItemChanged(holder.getAdapterPosition());
        };
        holder.itemView.setOnClickListener(expandListener);
        holder.cardArrow.setOnClickListener(expandListener);

        // 3. BOTÓN EDITAR
        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(tarea);
        });

        // 4. BOTÓN COMPARTIR (Intent al sistema)
        holder.btnCompartir.setOnClickListener(v -> {
            String asunto = "Tarea: " + tarea.getTitulo();
            String mensaje = "📅 Fecha: " + tarea.getFechaHora() + "\n" +
                    "📝 Nota: " + tarea.getDescripcion() + "\n" +
                    "📍 Lugar: " + tarea.getUbicacion();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, asunto);
            intent.putExtra(Intent.EXTRA_TEXT, mensaje);

            // Iniciar selector de apps (Gmail, WhatsApp...)
            v.getContext().startActivity(Intent.createChooser(intent, "Compartir tarea..."));
        });

        // 5. BOTÓN MENÚ (3 PUNTOS) -> BORRAR / DUPLICAR
        holder.btnMenuOpciones.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.btnMenuOpciones);
            popup.getMenu().add("Duplicar");
            popup.getMenu().add("Eliminar");

            popup.setOnMenuItemClickListener(item -> {
                if (listener == null) return false;

                if (item.getTitle().equals("Duplicar")) {
                    listener.onDuplicateClick(tarea);
                    return true;
                } else if (item.getTitle().equals("Eliminar")) {
                    listener.onDeleteClick(tarea);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return listaTareas.size();
    }

    // === VIEWHOLDER: CONECTA CON EL XML ===
    public static class TareaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvFecha, tvDescripcion, tvUbicacion;
        LinearLayout layoutDetallesHidden;
        View cardArrow;
        ImageView imgArrowIcon, btnEditar, btnCompartir, btnMenuOpciones;

        public TareaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTareaTitulo);
            tvFecha = itemView.findViewById(R.id.tvTareaFecha);

            // Parte oculta
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionBody);
            tvUbicacion = itemView.findViewById(R.id.tvUbicacionBody);
            layoutDetallesHidden = itemView.findViewById(R.id.layoutDetallesHidden);

            // Botones e iconos
            cardArrow = itemView.findViewById(R.id.cardArrow);
            imgArrowIcon = itemView.findViewById(R.id.imgArrowIcon);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnCompartir = itemView.findViewById(R.id.btnCompartir);
            btnMenuOpciones = itemView.findViewById(R.id.btnMenuOpciones);
        }
    }
}