package com.example.taskflow;

// Importa las líbrerias necesarias
import android.annotation.SuppressLint;
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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Objects;

// Clase que implementa el adaptador para la lista de tareas
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
            Context context = holder.itemView.getContext();

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

                // Configurar lógica de botones y expansion
                boolean isExpanded = finalTarea.isExpanded();
                // Expansión de la vista
                holder.layoutDetalles.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                holder.imgArrow.setImageResource(isExpanded ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);

                // Configura los archivos multimedia y colaboradores si está expandido
                if (isExpanded) {
                    boolean tieneMultimedia = false;

                    // Imagen
                    if (finalTarea.getImagenUri() != null && !finalTarea.getImagenUri().isEmpty()) {
                        if(holder.cardAdjuntoImagen != null) {
                            holder.cardAdjuntoImagen.setVisibility(View.VISIBLE);
                            holder.tvNombreImagen.setText(getFileName(finalTarea.getImagenUri()));
                            holder.cardAdjuntoImagen.setOnClickListener(v -> abrirVisualizador(context, finalTarea.getImagenUri(), "image"));
                            tieneMultimedia = true;
                        }
                    } else if(holder.cardAdjuntoImagen != null) {
                        holder.cardAdjuntoImagen.setVisibility(View.GONE);
                    }

                    // Video
                    if (finalTarea.getVideoUri() != null && !finalTarea.getVideoUri().isEmpty()) {
                        if(holder.cardAdjuntoVideo != null) {
                            holder.cardAdjuntoVideo.setVisibility(View.VISIBLE);
                            holder.tvNombreVideo.setText(getFileName(finalTarea.getVideoUri()));
                            holder.cardAdjuntoVideo.setOnClickListener(v -> abrirVisualizador(context, finalTarea.getVideoUri(), "video"));
                            tieneMultimedia = true;
                        }
                    } else if(holder.cardAdjuntoVideo != null) {
                        holder.cardAdjuntoVideo.setVisibility(View.GONE);
                    }

                    // Audio
                    if (finalTarea.getAudioUri() != null && !finalTarea.getAudioUri().isEmpty()) {
                        if(holder.cardAdjuntoAudio != null) {
                            holder.cardAdjuntoAudio.setVisibility(View.VISIBLE);
                            holder.tvNombreAudio.setText(getFileName(finalTarea.getAudioUri()));
                            holder.cardAdjuntoAudio.setOnClickListener(v -> abrirVisualizador(context, finalTarea.getAudioUri(), "audio"));
                            tieneMultimedia = true;
                        }
                    } else if(holder.cardAdjuntoAudio != null) {
                        holder.cardAdjuntoAudio.setVisibility(View.GONE);
                    }

                    // Mostrar u ocultar sección multimedia
                    if(holder.lblMultimediaItem != null) holder.lblMultimediaItem.setVisibility(tieneMultimedia ? View.VISIBLE : View.GONE);
                    if(holder.scrollMultimedia != null) holder.scrollMultimedia.setVisibility(tieneMultimedia ? View.VISIBLE : View.GONE);

                    // Colaboradores
                    if (finalTarea.getColaboradores() != null && !finalTarea.getColaboradores().isEmpty()) {
                        // Mostrar lista de colaboradores
                        if(holder.lblColaboradoresItem != null) holder.lblColaboradoresItem.setVisibility(View.VISIBLE);
                        if(holder.tvColaboradoresBody != null) {
                            holder.tvColaboradoresBody.setVisibility(View.VISIBLE);
                            StringBuilder sb = new StringBuilder();
                            // Construye la lista de colaboradores separados por comas
                            for (int i = 0; i < finalTarea.getColaboradores().size(); i++) {
                                sb.append(finalTarea.getColaboradores().get(i));
                                if (i < finalTarea.getColaboradores().size() - 1) sb.append(", ");
                            }
                            holder.tvColaboradoresBody.setText(sb.toString());
                        }
                    } else {
                        // Ocultar sección de colaboradores si no hay
                        if(holder.lblColaboradoresItem != null) holder.lblColaboradoresItem.setVisibility(View.GONE);
                        if(holder.tvColaboradoresBody != null) holder.tvColaboradoresBody.setVisibility(View.GONE);
                    }
                } else {
                    // Ocultar multimedia y colaboradores si no está expandido
                    if(holder.lblMultimediaItem != null) holder.lblMultimediaItem.setVisibility(View.GONE);
                    if(holder.scrollMultimedia != null) holder.scrollMultimedia.setVisibility(View.GONE);
                    if(holder.lblColaboradoresItem != null) holder.lblColaboradoresItem.setVisibility(View.GONE);
                    if(holder.tvColaboradoresBody != null) holder.tvColaboradoresBody.setVisibility(View.GONE);
                }

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
            holder.cardTarea.setCardBackgroundColor(Color.WHITE);
            holder.cardTarea.setLayoutParams(params);
        } else {
            // Ocultamos la vista
            holder.cardTarea.setVisibility(View.INVISIBLE);
            holder.cardTarea.setOnClickListener(null);
            lineaSeparadoraAMostrar(holder);
        }
    }

    // Muestra la línea de separación
    private void lineaSeparadoraAMostrar(HoraViewHolder holder) {
        holder.lineaSeparadora.setVisibility(View.VISIBLE);
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
        // Click en tarjeta (Expansión)
        holder.cardTarea.setOnClickListener(v -> {
            tarea.setExpanded(!tarea.isExpanded());
            notifyDataSetChanged();
        });

        // Botones
        holder.btnEditar.setOnClickListener(v -> { if (listener != null) listener.onEditClick(tarea); }); // Editar tarea
        holder.btnCompartir.setOnClickListener(v -> mostrarDialogoCompartir(v.getContext(), tarea)); // Compartir tarea
        holder.btnMenu.setOnClickListener(v -> mostrarMenuOpciones(v, tarea)); // Mostrar menú de opciones
    }

    // Método para mostrar el diálogo de compartir
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
        // Botones del menú
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

        // Variables nuevas para multimedia y colaboradores
        TextView lblMultimediaItem, lblColaboradoresItem, tvColaboradoresBody;
        View scrollMultimedia;
        CardView cardAdjuntoImagen, cardAdjuntoVideo, cardAdjuntoAudio;
        TextView tvNombreImagen, tvNombreVideo, tvNombreAudio;

        // Constructor de la clase
        public HoraViewHolder(@NonNull View itemView) {
            // Llama al constructor de la clase padre
            super(itemView);
            // Asigna las variables de la vista
            tvHora = itemView.findViewById(R.id.tvHoraSlot);
            tvTitulo = itemView.findViewById(R.id.tvTituloTareaSlot);

            // Vista oculta
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

            // Vinculación Multimedia y Colaboradores
            lblMultimediaItem = itemView.findViewById(R.id.lblMultimediaItem);
            scrollMultimedia = itemView.findViewById(R.id.scrollMultimedia);
            cardAdjuntoImagen = itemView.findViewById(R.id.cardAdjuntoImagen);
            tvNombreImagen = itemView.findViewById(R.id.tvNombreImagen);
            cardAdjuntoVideo = itemView.findViewById(R.id.cardAdjuntoVideo);
            tvNombreVideo = itemView.findViewById(R.id.tvNombreVideo);
            cardAdjuntoAudio = itemView.findViewById(R.id.cardAdjuntoAudio);
            tvNombreAudio = itemView.findViewById(R.id.tvNombreAudio);

            // Vista de colaboradores
            lblColaboradoresItem = itemView.findViewById(R.id.lblColaboradoresItem);
            tvColaboradoresBody = itemView.findViewById(R.id.tvColaboradoresBody);
        }
    }
}