package com.example.taskflow;

// Importa las líbrerias necesarias
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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

// Clase adaptador para la lista de tareas
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
    // Método para crear cada fila de la lista
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        // Obtiene la tarea actual y configura los datos en la vista
        Tarea tarea = listaTareas.get(position);
        Context context = holder.itemView.getContext();

        // Configura los datos de la tarea en la vista
        holder.tvTitulo.setText(tarea.getTitulo());
        holder.tvFecha.setText(tarea.getFechaHora());
        holder.tvDescripcion.setText(tarea.getDescripcion());
        holder.tvUbicacion.setText(tarea.getUbicacion());

        // Aplica o remueve el tachado según el estado de completada
        if (tarea.isCompletada()) {
            holder.tvTitulo.setPaintFlags(holder.tvTitulo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitulo.setPaintFlags(holder.tvTitulo.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Maneja la expansión y contracción de los detalles
        boolean isExpanded = tarea.isExpanded();
        holder.layoutDetallesHidden.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.imgArrowIcon.setImageResource(isExpanded ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);

        // Configura los archivos multimedia y colaboradores
        if (isExpanded) {
            boolean tieneMultimedia = false;

            // Imagen
            if (tarea.getImagenUri() != null && !tarea.getImagenUri().isEmpty()) {
                // Mostrar tarjeta de imagen
                holder.cardAdjuntoImagen.setVisibility(View.VISIBLE);
                holder.tvNombreImagen.setText(getFileName(tarea.getImagenUri()));
                holder.cardAdjuntoImagen.setOnClickListener(v -> abrirVisualizador(context, tarea.getImagenUri(), "image"));
                tieneMultimedia = true;
            } else {
                holder.cardAdjuntoImagen.setVisibility(View.GONE);
            }

            // Video
            if (tarea.getVideoUri() != null && !tarea.getVideoUri().isEmpty()) {
                // Mostrar tarjeta de video
                holder.cardAdjuntoVideo.setVisibility(View.VISIBLE);
                holder.tvNombreVideo.setText(getFileName(tarea.getVideoUri()));
                holder.cardAdjuntoVideo.setOnClickListener(v -> abrirVisualizador(context, tarea.getVideoUri(), "video"));
                tieneMultimedia = true;
            } else {
                holder.cardAdjuntoVideo.setVisibility(View.GONE);
            }

            // Audio
            if (tarea.getAudioUri() != null && !tarea.getAudioUri().isEmpty()) {
                // Mostrar tarjeta de audio
                holder.cardAdjuntoAudio.setVisibility(View.VISIBLE);
                holder.tvNombreAudio.setText(getFileName(tarea.getAudioUri()));
                holder.cardAdjuntoAudio.setOnClickListener(v -> abrirVisualizador(context, tarea.getAudioUri(), "audio"));
                tieneMultimedia = true;
            } else {
                holder.cardAdjuntoAudio.setVisibility(View.GONE);
            }

            // Mostrar u ocultar sección multimedia
            holder.lblMultimediaItem.setVisibility(tieneMultimedia ? View.VISIBLE : View.GONE);
            holder.scrollMultimedia.setVisibility(tieneMultimedia ? View.VISIBLE : View.GONE);

            // Colaboradores
            if (tarea.getColaboradores() != null && !tarea.getColaboradores().isEmpty()) {
                // Mostrar lista de colaboradores
                holder.lblColaboradoresItem.setVisibility(View.VISIBLE);
                holder.tvColaboradoresBody.setVisibility(View.VISIBLE);
                StringBuilder sb = new StringBuilder();
                // Construye la lista de colaboradores separados por comas
                for (int i = 0; i < tarea.getColaboradores().size(); i++) {
                    sb.append(tarea.getColaboradores().get(i));
                    if (i < tarea.getColaboradores().size() - 1) sb.append(", ");
                }
                holder.tvColaboradoresBody.setText(sb.toString());
            } else {
                // Ocultar sección de colaboradores si no hay
                holder.lblColaboradoresItem.setVisibility(View.GONE);
                holder.tvColaboradoresBody.setVisibility(View.GONE);
            }
        } else {
            // Ocultar multimedia y colaboradores si no está expandido
            holder.lblMultimediaItem.setVisibility(View.GONE);
            holder.scrollMultimedia.setVisibility(View.GONE);
            holder.lblColaboradoresItem.setVisibility(View.GONE);
            holder.tvColaboradoresBody.setVisibility(View.GONE);
        }

        // Configura los listeners de los botones y la expansión
        View.OnClickListener expandListener = v -> {
            // Alterna el estado de expansión y notifica el cambio
            tarea.setExpanded(!tarea.isExpanded());
            notifyItemChanged(holder.getBindingAdapterPosition());
        };

        // Listener para expandir/contraer detalles
        holder.itemView.setOnClickListener(expandListener);
        holder.cardArrow.setOnClickListener(expandListener);

        // Listeners para los botones de editar, compartir y menú de opciones
        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(tarea);
        });

        // Listener para el botón de compartir
        holder.btnCompartir.setOnClickListener(v -> mostrarDialogoCompartir(context, tarea));
        holder.btnMenuOpciones.setOnClickListener(v -> mostrarMenuOpciones(context, v, tarea));
    }

    // Método para obtener el nombre del archivo desde su URI
    private String getFileName(String uriString) {
        // Extrae el nombre del archivo de la URI
        if (uriString == null) return "Archivo";
        try {
            // Parsea la URI y obtiene el segmento final como nombre de archivo
            Uri uri = Uri.parse(uriString);
            // Obtiene la ruta del archivo
            String path = uri.getPath();
            // Extrae el nombre del archivo de la ruta
            if (path != null && path.contains("/")) {
                return path.substring(path.lastIndexOf('/') + 1);
            }
            // Si no se puede extraer, devuelve el último segmento de la URI
            return uri.getLastPathSegment() != null ? uri.getLastPathSegment() : "Archivo adjunto";
        } catch (Exception e) {
            return "Archivo adjunto";
        }
    }

    // Método para abrir el visualizador de multimedia
    private void abrirVisualizador(Context context, String uri, String tipo) {
        // Abre la actividad de visualización de multimedia con la URI y tipo especificados
        Intent intent = new Intent(context, VisualizadorActivity.class);
        intent.putExtra("URI", uri);
        intent.putExtra("TIPO", tipo);
        context.startActivity(intent);
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

    // Clase ViewHolder para mantener las referencias de las vistas
    public static class TareaViewHolder extends RecyclerView.ViewHolder {
        // Variables de la vista
        TextView tvTitulo, tvFecha, tvDescripcion, tvUbicacion, tvColaboradoresBody;
        LinearLayout layoutDetallesHidden;
        View cardArrow, scrollMultimedia;
        ImageView imgArrowIcon, btnEditar, btnCompartir, btnMenuOpciones;
        View cardAdjuntoImagen, cardAdjuntoVideo, cardAdjuntoAudio;
        TextView tvNombreImagen, tvNombreVideo, tvNombreAudio;
        TextView lblColaboradoresItem, lblMultimediaItem;

        // Constructor del ViewHolder
        public TareaViewHolder(@NonNull View itemView) {
            // Llama al constructor de la clase padre
            super(itemView);
            // Vincula las vistas con sus IDs
            tvTitulo = itemView.findViewById(R.id.tvTareaTitulo);
            tvFecha = itemView.findViewById(R.id.tvTareaFecha);
            
            // Vista oculta
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionBody);
            tvUbicacion = itemView.findViewById(R.id.tvUbicacionBody);
            tvColaboradoresBody = itemView.findViewById(R.id.tvColaboradoresBody);
            lblColaboradoresItem = itemView.findViewById(R.id.lblColaboradoresItem);
            lblMultimediaItem = itemView.findViewById(R.id.lblMultimediaItem);
            layoutDetallesHidden = itemView.findViewById(R.id.layoutDetallesHidden);
            
            // Multimedia
            scrollMultimedia = itemView.findViewById(R.id.scrollMultimedia);
            cardAdjuntoImagen = itemView.findViewById(R.id.cardAdjuntoImagen);
            tvNombreImagen = itemView.findViewById(R.id.tvNombreImagen);
            cardAdjuntoVideo = itemView.findViewById(R.id.cardAdjuntoVideo);
            tvNombreVideo = itemView.findViewById(R.id.tvNombreVideo);
            cardAdjuntoAudio = itemView.findViewById(R.id.cardAdjuntoAudio);
            tvNombreAudio = itemView.findViewById(R.id.tvNombreAudio);

            // Botones e iconos
            cardArrow = itemView.findViewById(R.id.cardArrow);
            imgArrowIcon = itemView.findViewById(R.id.imgArrowIcon);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnCompartir = itemView.findViewById(R.id.btnCompartir);
            btnMenuOpciones = itemView.findViewById(R.id.btnMenuOpciones);
        }
    }
}