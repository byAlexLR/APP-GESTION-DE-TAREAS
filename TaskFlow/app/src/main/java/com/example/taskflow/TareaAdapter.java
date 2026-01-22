package com.example.taskflow;

// Importa las líbrerias necesarias
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

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
        Context context = holder.itemView.getContext();

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
        holder.imgArrowIcon.setImageResource(isExpanded ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);

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
        holder.btnCompartir.setOnClickListener(v -> mostrarDialogoCompartir(context, tarea));

        // Botón de menú de opciones
        holder.btnMenuOpciones.setOnClickListener(v -> mostrarMenuOpciones(context, v, tarea));
    }

    // Método para mostrar el diálogo de compartir interactivo y moderno
    private void mostrarDialogoCompartir(Context context, Tarea tarea) {
        // Inflamos el layout moderno
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_compartir, null);

        // Creamos el diálogo con estilo Material
        AlertDialog dialog = new AlertDialog.Builder(context).setView(view).create();

        // Fondo transparente para los bordes redondeados
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Vinculamos los nuevos contenedores interactivos
        LinearLayout btnTexto = view.findViewById(R.id.btnCompartirTexto);
        LinearLayout btnBluetooth = view.findViewById(R.id.btnCompartirBluetooth);
        TextView btnCancelar = view.findViewById(R.id.btnCancelarCompartir);

        // Acciones con feedback visual
        btnTexto.setOnClickListener(v -> {
            context.startActivity(Intent.createChooser(crearIntentCompartirTexto(context, tarea),
                    context.getString(R.string.compartir_tarea_chooser)));
            dialog.dismiss();
        });

        // Acciones para Bluetooth
        btnBluetooth.setOnClickListener(v -> {
            dialog.dismiss(); // Cierra el diálogo

            // Abre el nuevo BottomSheet
            if (context instanceof androidx.fragment.app.FragmentActivity) {
                BluetoothTransfer sheet = BluetoothTransfer.newInstance(tarea);
                sheet.show(((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager(), "BT");
            }
        });

        // Cancela el diálogo
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        // Muestra el diálogo
        dialog.show();
    }

    // Crea un Intent de envío de texto con los detalles de la tarea
    private Intent crearIntentCompartirTexto(Context context, Tarea tarea) {
        // Formatea el asunto y el cuerpo del mensaje usando recursos de strings
        String asunto = context.getString(R.string.formato_asunto_compartir, tarea.getTitulo());
        String mensaje = context.getString(R.string.formato_mensaje_compartir, tarea.getFechaHora(), tarea.getDescripcion(), tarea.getUbicacion());

        // Configura el intent de tipo SEND
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, asunto);
        intent.putExtra(Intent.EXTRA_TEXT, mensaje);
        return intent;
    }

    // Método para mostrar un menú emergente con acciones adicionales sobre la tarea
    private void mostrarMenuOpciones(Context context, View view, Tarea tarea) {
        // Crea el menú emergente
        PopupMenu popup = new PopupMenu(context, view);

        // Define el texto de la opción completar según el estado actual
        String menuCompletar = tarea.isCompletada() ?
                context.getString(R.string.menu_desmarcar_hecha) :
                context.getString(R.string.menu_marcar_hecha);

        // Añade las opciones al menú (Completar, Duplicar, Eliminar)
        popup.getMenu().add(0, 0, 0, menuCompletar);
        popup.getMenu().add(0, 1, 1, R.string.menu_duplicar);
        popup.getMenu().add(0, 2, 2, R.string.menu_eliminar);

        // Gestiona los clics en los elementos del menú llamando al listener
        popup.setOnMenuItemClickListener(item -> {
            if (listener == null) return false;
            // Llama al listener según la opción seleccionada
            switch (item.getItemId()) {
                case 0: listener.onCompleteClick(tarea); return true;
                case 1: listener.onDuplicateClick(tarea); return true;
                case 2: listener.onDeleteClick(tarea); return true;
            }
            return false;
        });
        popup.show();
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