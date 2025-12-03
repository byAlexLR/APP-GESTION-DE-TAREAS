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

public class CalendarioAdapter extends RecyclerView.Adapter<CalendarioAdapter.HoraViewHolder> {

    private List<Tarea> listaTareasDelDia;
    private OnItemClickListener listener;

    // Interfaz para comunicar acciones a la Activity
    public interface OnItemClickListener {
        void onEditClick(Tarea tarea); // Pasamos la tarea directamente
        void onDeleteClick(Tarea tarea);
        void onDuplicateClick(Tarea tarea);
    }

    public CalendarioAdapter(List<Tarea> listaTareasDelDia, OnItemClickListener listener) {
        this.listaTareasDelDia = listaTareasDelDia;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HoraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendario_hora, parent, false);
        return new HoraViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HoraViewHolder holder, int position) {
        // 1. Formatear hora
        String horaTexto = formatearHora(position);
        holder.tvHora.setText(horaTexto);

        // 2. Buscar si hay tarea en esta hora
        Tarea tareaEncontrada = null;
        for (Tarea t : listaTareasDelDia) {
            if (t.getHoraInicio() == position) {
                tareaEncontrada = t;
                break;
            }
        }

        // 3. Mostrar/Ocultar tarjeta
        if (tareaEncontrada != null) {
            holder.cardTarea.setVisibility(View.VISIBLE);

            // Rellenar datos
            holder.tvTitulo.setText(tareaEncontrada.getTitulo());
            holder.tvDesc.setText(tareaEncontrada.getDescripcion());
            holder.tvUbi.setText(tareaEncontrada.getUbicacion());

            // Controlar Expansión
            boolean isExpanded = tareaEncontrada.isExpanded();
            holder.layoutDetalles.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            if (isExpanded) {
                holder.imgArrow.setImageResource(R.drawable.ic_arrow_up);
            } else {
                holder.imgArrow.setImageResource(R.drawable.ic_arrow_down);
            }

            // Click en tarjeta (Expansión)
            Tarea finalTarea = tareaEncontrada;
            holder.cardTarea.setOnClickListener(v -> {
                finalTarea.setExpanded(!finalTarea.isExpanded());
                notifyItemChanged(position);
            });

            // === BOTÓN EDITAR ===
            holder.btnEditar.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(finalTarea);
            });

            // === BOTÓN COMPARTIR (Gmail/WhatsApp) ===
            holder.btnCompartir.setOnClickListener(v -> {
                String asunto = "Tarea: " + finalTarea.getTitulo();
                String mensaje = "📅 *Fecha:* " + finalTarea.getFechaHora() + "\n" +
                        "📝 *Nota:* " + finalTarea.getDescripcion() + "\n" +
                        "📍 *Lugar:* " + finalTarea.getUbicacion();

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, asunto);
                intent.putExtra(Intent.EXTRA_TEXT, mensaje);
                v.getContext().startActivity(Intent.createChooser(intent, "Compartir tarea..."));
            });

            // === BOTÓN MENÚ (Borrar/Duplicar) ===
            holder.btnMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), holder.btnMenu);
                popup.getMenu().add("Duplicar");
                popup.getMenu().add("Eliminar");

                popup.setOnMenuItemClickListener(item -> {
                    if (listener == null) return false;
                    if (item.getTitle().equals("Duplicar")) {
                        listener.onDuplicateClick(finalTarea);
                        return true;
                    } else if (item.getTitle().equals("Eliminar")) {
                        listener.onDeleteClick(finalTarea);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });

        } else {
            // Hora vacía
            holder.cardTarea.setVisibility(View.INVISIBLE);
            holder.cardTarea.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() { return 24; }

    private String formatearHora(int hora) {
        if (hora == 0) return "12 AM";
        if (hora < 12) return hora + " AM";
        if (hora == 12) return "12 PM";
        return (hora - 12) + " PM";
    }

    public static class HoraViewHolder extends RecyclerView.ViewHolder {
        TextView tvHora, tvTitulo, tvDesc, tvUbi;
        CardView cardTarea;
        ImageView imgArrow, btnEditar, btnCompartir, btnMenu;
        LinearLayout layoutDetalles;

        public HoraViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHora = itemView.findViewById(R.id.tvHoraSlot);
            tvTitulo = itemView.findViewById(R.id.tvTituloTareaSlot);
            tvDesc = itemView.findViewById(R.id.tvDescSlot);
            tvUbi = itemView.findViewById(R.id.tvUbiSlot);
            cardTarea = itemView.findViewById(R.id.cardTareaSlot);
            imgArrow = itemView.findViewById(R.id.imgArrowSlot);
            layoutDetalles = itemView.findViewById(R.id.layoutDetallesSlot);

            // Botones del slot
            btnEditar = itemView.findViewById(R.id.btnEditarSlot);
            btnCompartir = itemView.findViewById(R.id.btnCompartirSlot);
            btnMenu = itemView.findViewById(R.id.btnMenuSlot);
        }
    }
}