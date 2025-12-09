package com.example.taskflow;

// Importación de librerías necesarias
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Objects;

public class CalendarioAdapter extends RecyclerView.Adapter<CalendarioAdapter.HoraViewHolder> {

    // Variables
    private final List<Tarea> listaTareasDelDia;
    private final OnItemClickListener listener;

    // Interfaz para comunicar acciones a la Activity
    public interface OnItemClickListener {
        void onEditClick(Tarea tarea);
        void onDeleteClick(Tarea tarea);
        void onDuplicateClick(Tarea tarea);
        void onCompleteClick(Tarea tarea);
    }

    // Constructor
    public CalendarioAdapter(List<Tarea> listaTareasDelDia, OnItemClickListener listener) {
        this.listaTareasDelDia = listaTareasDelDia;
        this.listener = listener;
    }

    @NonNull
    @Override
    // Crea el ViewHolder
    public HoraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendario_hora, parent, false);
        return new HoraViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    // Configura el ViewHolder
    public void onBindViewHolder(@NonNull HoraViewHolder holder, int position) {
        // Formatea la hora
        holder.tvHora.setText(formatearHora(position));

        // Variables auxiliares
        Tarea tareaEncontrada = null;
        boolean esHoraInicio = false;
        boolean esUltimaHora = false;

        // Busca si hay una tarea en esa hora
        for (Tarea t : listaTareasDelDia) {
            // Variables con los datos de inicio y fin de la tarea
            int horaInicio24 = t.getHoraInicio24();
            int horaFin24 = t.getHoraFin24();

            // Comprueba si la hora de fin es menor que la de inicio
            if (horaInicio24 == horaFin24) {
                horaFin24 = horaInicio24 + 1;
            }

            if (horaFin24 < horaInicio24) horaFin24 = 24;

            // Comprueba si la hora actual está dentro del rango de inicio y fin de la tarea
            if (position >= horaInicio24 && position < horaFin24) {
                tareaEncontrada = t;
                esHoraInicio = (position == horaInicio24);
                esUltimaHora = (position == horaFin24 - 1);
                break;
            }
        }

        // Muestra la tarea o oculta la vista
        if (tareaEncontrada != null) {
            // Muestra la vista
            holder.cardTarea.setVisibility(View.VISIBLE);

            // Obtenemos los LayoutParams para modificar márgenes dinámicamente
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.cardTarea.getLayoutParams();
            final Tarea finalTarea = tareaEncontrada;

            if (esHoraInicio) {
                holder.layoutContenidoPrincipal.setVisibility(View.VISIBLE);

                // Márgenes
                params.topMargin = 4;
                params.bottomMargin = 0;

                // Estética
                holder.cardTarea.setRadius(16);
                holder.cardTarea.setCardElevation(0);

                // Ocultamos línea de fondo para fusionar con la siguiente
                holder.lineaSeparadora.setVisibility(View.GONE);

                // Datos
                holder.tvTitulo.setText(finalTarea.getTitulo());

                // Comprueba si está completada, y si lo está, la tacha
                if (finalTarea.isCompletada()) {
                    holder.tvTitulo.setPaintFlags(holder.tvTitulo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.tvTitulo.setAlpha(0.5f); // Opacidad reducida
                } else {
                    holder.tvTitulo.setPaintFlags(holder.tvTitulo.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    holder.tvTitulo.setAlpha(1.0f); // Opacidad normal
                }

                holder.tvUbi.setText(finalTarea.getUbicacion());
                holder.tvDesc.setText(generarTextoDescripcion(finalTarea));

                // Configurar lógica de botones y click
                configurarBotonesYExpansion(holder, finalTarea);

            } else {
                // Ocultamos la vista
                holder.layoutContenidoPrincipal.setVisibility(View.GONE);
                holder.layoutDetalles.setVisibility(View.GONE);

                // Limpiamos listeners antiguos
                holder.cardTarea.setOnClickListener(null);

                // Márgenes
                params.topMargin = 0;
                holder.cardTarea.setCardElevation(0);

                // Hacemos que esta parte "vacía" funcione igual que la principal
                holder.cardTarea.setOnClickListener(v -> {
                    finalTarea.setExpanded(!finalTarea.isExpanded());
                    notifyDataSetChanged();
                });

                // Si ocupa varios días
                if (esUltimaHora) {
                    params.bottomMargin = 4;
                    holder.cardTarea.setRadius(16);
                    holder.lineaSeparadora.setVisibility(View.VISIBLE);
                } else {
                    params.bottomMargin = 0;
                    holder.cardTarea.setRadius(0);
                    holder.lineaSeparadora.setVisibility(View.GONE);
                }
            }
            // Aplicamos los cambios
            holder.cardTarea.setCardBackgroundColor(android.graphics.Color.WHITE);
            holder.cardTarea.setLayoutParams(params);
        } else {
            // Ocultamos la vista
            holder.cardTarea.setVisibility(View.INVISIBLE);
            holder.cardTarea.setOnClickListener(null);
            holder.lineaSeparadora.setVisibility(View.VISIBLE);
        }
    }

    // Genera el texto de descripción con formato adecuado
    private String generarTextoDescripcion(Tarea tarea) {
        // Variables para insertar la hora con un formato específico
        String minIn = tarea.getMinInicio() < 10 ? "0" + tarea.getMinInicio() : String.valueOf(tarea.getMinInicio());
        String minOut = tarea.getMinFin() < 10 ? "0" + tarea.getMinFin() : String.valueOf(tarea.getMinFin());
        String rangoHoras = tarea.getHoraInicio() + ":" + minIn + " " + tarea.getAmPmInicio() +
                " - " +
                tarea.getHoraFin() + ":" + minOut + " " + tarea.getAmPmFin();

        // Si no hay descripción, solo muestra el rango de horas
        if (tarea.getDescripcion() != null && !tarea.getDescripcion().isEmpty()) {
            return rangoHoras + "\n\n" + tarea.getDescripcion();
        }
        return rangoHoras;
    }

    @SuppressLint("NotifyDataSetChanged")
    // Configura los botones y lógica de expansión
    private void configurarBotonesYExpansion(HoraViewHolder holder, Tarea tarea) {
        // Variable para saber si está expandida
        boolean isExpanded = tarea.isExpanded();
        // Expansión de la vista
        holder.layoutDetalles.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.imgArrow.setImageResource(isExpanded ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);

        // Click en tarjeta (Expansión)
        holder.cardTarea.setOnClickListener(v -> {
            tarea.setExpanded(!tarea.isExpanded());
            notifyDataSetChanged();
        });

        // Botones
        holder.btnEditar.setOnClickListener(v -> { if (listener != null) listener.onEditClick(tarea); }); // Editar tarea
        holder.btnCompartir.setOnClickListener(v -> compartirTarea(v.getContext(), tarea)); // Compartir tarea
        holder.btnMenu.setOnClickListener(v -> mostrarMenuOpciones(v, tarea)); // Mostrar menú de opciones
    }

    // Para no repetir código y compatir la tarea
    private void compartirTarea(Context context, Tarea tarea) {
        // Variables con formato de compartir la tarea
        String asunto = "Tarea: " + tarea.getTitulo();
        String mensaje = "📅 Fecha: " + tarea.getFechaHora() + "\n" +
                "📝 Nota: " + tarea.getDescripcion() + "\n" +
                "📍 Lugar: " + tarea.getUbicacion();

        // Intent para compartir
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, asunto);
        intent.putExtra(Intent.EXTRA_TEXT, mensaje);
        context.startActivity(Intent.createChooser(intent, "Compartir tarea..."));
    }

    // Menú de opciones
    private void mostrarMenuOpciones(View v, Tarea tarea) {
        // Listener para el menú
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        // Botones del menú
        if (tarea.isCompletada()) {
            popup.getMenu().add("Desmarcar como hecha");
        } else {
            popup.getMenu().add("Marcar como hecha");
        }
        popup.getMenu().add("Duplicar");
        popup.getMenu().add("Eliminar");
        // Muestra el menú
        popup.setOnMenuItemClickListener(item -> {
            if (listener == null) return false;
            if (Objects.equals(item.getTitle(), "Duplicar")) { listener.onDuplicateClick(tarea); return true; } // Duplica
            else if (Objects.equals(item.getTitle(), "Eliminar")) { listener.onDeleteClick(tarea); return true; } // Elimina
            else if (Objects.equals(item.getTitle(), "Marcar como hecha") || Objects.equals(item.getTitle(), "Desmarcar como hecha")) { listener.onCompleteClick(tarea); return true; } // Marcar como hecha
            return false;
        });
        popup.show();
    }

    @Override
    // Devuelve el número de elementos
    public int getItemCount() { return 24; }

    // Formatea la hora
    private String formatearHora(int hora) {
        if (hora == 0) return "12 AM";
        if (hora < 12) return hora + " AM";
        if (hora == 12) return "12 PM";
        return (hora - 12) + " PM";
    }

    // ViewHolder para la vista de cada elemento
    public static class HoraViewHolder extends RecyclerView.ViewHolder {
        // Variables de la vista
        TextView tvHora, tvTitulo, tvDesc, tvUbi;
        CardView cardTarea;
        ImageView imgArrow, btnEditar, btnCompartir, btnMenu;
        LinearLayout layoutDetalles, layoutContenidoPrincipal;
        View lineaSeparadora;

        // Constructor de la clase
        public HoraViewHolder(@NonNull View itemView) {
            // Llama al constructor de la clase padre
            super(itemView);
            // Asignamos las variables de la vista
            tvHora = itemView.findViewById(R.id.tvHoraSlot);
            tvTitulo = itemView.findViewById(R.id.tvTituloTareaSlot);
            tvDesc = itemView.findViewById(R.id.tvDescSlot);
            tvUbi = itemView.findViewById(R.id.tvUbiSlot);
            cardTarea = itemView.findViewById(R.id.cardTareaSlot);
            imgArrow = itemView.findViewById(R.id.imgArrowSlot);
            layoutDetalles = itemView.findViewById(R.id.layoutDetallesSlot);
            layoutContenidoPrincipal = itemView.findViewById(R.id.layoutContenedorInfo);
            lineaSeparadora = itemView.findViewById(R.id.lineaSeparadoraFondo);

            // Botones de la vista de la tarea (editar, compartir, menu)
            btnEditar = itemView.findViewById(R.id.btnEditarSlot);
            btnCompartir = itemView.findViewById(R.id.btnCompartirSlot);
            btnMenu = itemView.findViewById(R.id.btnMenuSlot);
        }
    }
}
