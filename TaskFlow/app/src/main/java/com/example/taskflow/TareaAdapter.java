package com.example.taskflow;

// Importación de librerías necesarias
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
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Objects;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {
    // Variables de la lista y listener
    private final List<Tarea> listaTareas;
    private final OnItemClickListener listener;

    // --- LISTENER ---
    public interface OnItemClickListener {
        // Métodos para cada botón
        void onEditClick(Tarea tarea);
        void onDeleteClick(Tarea tarea);
        void onDuplicateClick(Tarea tarea);
        void onCompleteClick(Tarea tarea);
    }

    // Constructor de la clase
    public TareaAdapter(List<Tarea> listaTareas, OnItemClickListener listener) {
        this.listaTareas = listaTareas;
        this.listener = listener;
    }

    @NonNull
    @Override
    // Método para crear cada fila de la lista
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Crea la vista de la fila
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarea, parent, false);
        return new TareaViewHolder(view);
    }

    @Override
    // Método para rellenar cada fila de la lista
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        // Recoge la tarea de la lista
        Tarea tarea = listaTareas.get(position);

        // Rellena los campos
        holder.tvTitulo.setText(tarea.getTitulo());
        holder.tvFecha.setText(tarea.getFechaHora());
        holder.tvDescripcion.setText(tarea.getDescripcion());
        holder.tvUbicacion.setText(tarea.getUbicacion());

        // Comprueba si la tarea está completada, y si lo está, la tacha
        if (tarea.isCompletada()) {
            holder.tvTitulo.setPaintFlags(holder.tvTitulo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitulo.setPaintFlags(holder.tvTitulo.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Expansión de la vista
        boolean isExpanded = tarea.isExpanded();
        holder.layoutDetallesHidden.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        // Cambia el icono de la flecha según el estado
        if (isExpanded) {
            holder.imgArrowIcon.setImageResource(R.drawable.ic_arrow_up);
        } else {
            holder.imgArrowIcon.setImageResource(R.drawable.ic_arrow_down);
        }

        // Listener para expandir/contraer al tocar la tarjeta
        View.OnClickListener expandListener = v -> {
            // Cambia el estado de la tarea
            tarea.setExpanded(!tarea.isExpanded());

            // Actualiza la vista
            notifyItemChanged(holder.getBindingAdapterPosition());
        };

        // Eventos de click en la tarjeta
        holder.itemView.setOnClickListener(expandListener);
        holder.cardArrow.setOnClickListener(expandListener);


        // Botón de editar
        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(tarea);
        });

        // Botón de compartir
        holder.btnCompartir.setOnClickListener(v -> {
            // Variables con formato de compartir la tarea
            String asunto = "Tarea: " + tarea.getTitulo();
            String mensaje = "📅 Fecha: " + tarea.getFechaHora() + "\n" +
                    "📝 Nota: " + tarea.getDescripcion() + "\n" +
                    "📍 Lugar: " + tarea.getUbicacion();

            // Llama al intent para compartir
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, asunto);
            intent.putExtra(Intent.EXTRA_TEXT, mensaje);

            // Inicia el selector de apps
            v.getContext().startActivity(Intent.createChooser(intent, "Compartir tarea..."));
        });

        // Botón de menú de opciones
        holder.btnMenuOpciones.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.btnMenuOpciones);
            
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

                // Comprueba el título del botón pulsado
                String titulo = Objects.requireNonNull(item.getTitle()).toString();
                switch (titulo) {
                    case "Duplicar":
                        listener.onDuplicateClick(tarea);
                        return true;
                    case "Eliminar":
                        listener.onDeleteClick(tarea);
                        return true;
                    case "Marcar como hecha":
                    case "Desmarcar como hecha":
                        listener.onCompleteClick(tarea);
                        return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    // Devuelve el número de elementos
    public int getItemCount() {
        return listaTareas.size();
    }

    // --- VIEWHOLDER ---
    public static class TareaViewHolder extends RecyclerView.ViewHolder {
        // Variables de la vista
        TextView tvTitulo, tvFecha, tvDescripcion, tvUbicacion;
        LinearLayout layoutDetallesHidden;
        View cardArrow;
        ImageView imgArrowIcon, btnEditar, btnCompartir, btnMenuOpciones;

        // Constructor de la clase
        public TareaViewHolder(@NonNull View itemView) {
            // Llama al constructor de la clase padre
            super(itemView);
            // Asigna las variables de la vista
            tvTitulo = itemView.findViewById(R.id.tvTareaTitulo);
            tvFecha = itemView.findViewById(R.id.tvTareaFecha);

            // Vista oculta
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