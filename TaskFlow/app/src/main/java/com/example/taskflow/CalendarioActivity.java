package com.example.taskflow;

// Importa las líbrerias necesarias
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// Clase que implementa la actividad del calendario
public class CalendarioActivity extends AppCompatActivity {

    // Variables
    private RecyclerView rvCalendario;
    private CalendarioAdapter adapter;
    private List<Tarea> listaTareasVisual;
    private Calendar fechaSeleccionadaCalendar;
        private TextView tvMesAnioSelector, tvFechaSeleccionadaBig;
    private CalendarioAdapter.OnItemClickListener listenerAcciones;

    // Métodos de ciclo de vida
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendario);

        // Inicializa vistas
        tvMesAnioSelector = findViewById(R.id.tvMesAnioSelector);
        tvFechaSeleccionadaBig = findViewById(R.id.tvFechaSeleccionada);
        LinearLayout btnSelectorFecha = findViewById(R.id.btnSelectorFecha);
        rvCalendario = findViewById(R.id.rvCalendario);
        rvCalendario.setLayoutManager(new LinearLayoutManager(this));

        // Configura la fecha inicial
        fechaSeleccionadaCalendar = Calendar.getInstance();
        actualizarTextosFecha();

        // Inicializa la lista visual y el adaptador
        listaTareasVisual = new ArrayList<>();
        listenerAcciones = new CalendarioAdapter.OnItemClickListener() {
            // Implementamos los métodos de la interfaz
            // Botón de editar
            @Override
            public void onEditClick(Tarea tarea) {
                Intent intent = new Intent(CalendarioActivity.this, CrearTareaActivity.class);
                intent.putExtra("TAREA_A_EDITAR", tarea);

                // Busca el índice real en la lista global
                int indiceReal = Repositorio.tareasGlobales.indexOf(tarea);
                intent.putExtra("POSICION_ORIGINAL", indiceReal);

                startActivity(intent);
            }

            // Botón de eliminar
            @Override
            public void onDeleteClick(Tarea tarea) {
                // Eliminar en repositorio GLOBAL
                Repositorio.tareasGlobales.remove(tarea);
                cargarTareasDelDia();
                Toast.makeText(CalendarioActivity.this, "Tarea eliminada", Toast.LENGTH_SHORT).show();
            }

            // Botón de duplicar
            @Override
            public void onDuplicateClick(Tarea tarea) {
                // Duplicar en repositorio GLOBAL
                Tarea copia = new Tarea(
                        tarea.getTitulo() + " (Copia)",
                        tarea.getFechaHora(),
                        tarea.getDescripcion(),
                        tarea.getUbicacion(),
                        tarea.getDia(), tarea.getMes(), tarea.getAnio(),
                        tarea.getHoraInicio(), tarea.getMinInicio(), tarea.getAmPmInicio(),
                        tarea.getHoraFin(), tarea.getMinFin(), tarea.getAmPmFin(),
                        tarea.getNotifCantidad(), tarea.getNotifUnidad()
                );
                // Añade la copia a la lista
                Repositorio.tareasGlobales.add(copia);
                cargarTareasDelDia();
                Toast.makeText(CalendarioActivity.this, "Tarea duplicada", Toast.LENGTH_SHORT).show();
            }

            @Override
            // Botón de completar
            public void onCompleteClick(Tarea t) {
                // Cambia el estado de la tarea
                t.setCompletada(!t.isCompletada());
                cargarTareasDelDia();
                // Muestra un mensaje de confirmación
                String mensaje = t.isCompletada() ? "Tarea completada" : "Tarea reactivada";
                Toast.makeText(CalendarioActivity.this, mensaje, Toast.LENGTH_SHORT).show();
            }
        };
        // Configuramos el adaptador
        adapter = new CalendarioAdapter(listaTareasVisual, listenerAcciones);
        rvCalendario.setAdapter(adapter);

        // Carga los datos iniciales
        cargarTareasDelDia();

        // Configura el botón para mostrar el selector de fecha
        btnSelectorFecha.setOnClickListener(v -> mostrarSelectorFecha());

        // Botón Agregar Tarea
        findViewById(R.id.fabAddCal).setOnClickListener(v -> startActivity(new Intent(CalendarioActivity.this, CrearTareaActivity.class)));

        setupToolbarButtons();
    }

    // Se llama cada vez que volvemos a esta pantalla, después de crear una tarea
    @Override
    protected void onResume() {
        super.onResume();
        cargarTareasDelDia();
    }

    // Método para cargar las tareas del día
    @SuppressLint("NotifyDataSetChanged")
    private void cargarTareasDelDia() {
        // Limpiamos la lista visual
        listaTareasVisual.clear();

        // Variables de fecha
        int diaSel = fechaSeleccionadaCalendar.get(Calendar.DAY_OF_MONTH);
        int mesSel = fechaSeleccionadaCalendar.get(Calendar.MONTH);
        int anioSel = fechaSeleccionadaCalendar.get(Calendar.YEAR);

        // Filtramos del repositorio global
        for (Tarea t : Repositorio.tareasGlobales) {
            if (t.getDia() == diaSel && t.getMes() == mesSel && t.getAnio() == anioSel) {
                listaTareasVisual.add(t);
            }
        }

        // Vuelve a configurar el adaptador con la nueva lista
        if (rvCalendario.getAdapter() != adapter) {
            rvCalendario.setAdapter(adapter);
        }

        adapter.notifyDataSetChanged();
    }

    // Muestra el selector de fecha
    private void mostrarSelectorFecha() {
        // Variables de fecha
        int anio = fechaSeleccionadaCalendar.get(Calendar.YEAR);
        int mes = fechaSeleccionadaCalendar.get(Calendar.MONTH);
        int dia = fechaSeleccionadaCalendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, y, m, d) -> {
            fechaSeleccionadaCalendar.set(y, m, d);
            actualizarTextosFecha();
            cargarTareasDelDia(); // ¡Importante! Recargar lista al cambiar fecha
        }, anio, mes, dia).show();
    }

    @SuppressLint("SetTextI18n")
    // Actualiza los textos del selector de fecha
    private void actualizarTextosFecha() {
        // Actualiza el texto del selector de fecha, con el formato Español
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        String t = sdf.format(fechaSeleccionadaCalendar.getTime());

        // Colocamos la primera letra en mayúscula si no lo está
        if (!t.isEmpty()) {
            tvMesAnioSelector.setText(t.substring(0, 1).toUpperCase() + t.substring(1));
        } else {
            tvMesAnioSelector.setText(t);
        }

        // Actualiza el texto de la fecha seleccionada
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd MMMM yyyy", new Locale("es", "ES"));
        tvFechaSeleccionadaBig.setText(sdf2.format(fechaSeleccionadaCalendar.getTime()).toUpperCase());
    }

    // Configura los botones de la barra de herramientas
    private void setupToolbarButtons() {
        findViewById(R.id.btnBuscar).setOnClickListener(v -> mostrarDialogoBusqueda());

        // Botón Calendario: Volver a Hoy
        findViewById(R.id.btnVerMes).setOnClickListener(v -> {
            fechaSeleccionadaCalendar = Calendar.getInstance();
            actualizarTextosFecha();
            cargarTareasDelDia();
            Toast.makeText(this, "Has vuelto al día actual", Toast.LENGTH_SHORT).show();
        });
    }

    // Muestra el diálogo de búsqueda
    private void mostrarDialogoBusqueda() {
        // Configuramos el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_buscar, null);
        // Colocamos el layout en el diálogo
        builder.setView(view);
        // Construimos el diálogo
        AlertDialog dialog = builder.create();
        // Fondo transparente para los bordes redondeados
        if(dialog.getWindow()!=null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // Configuramos el diálogo
        EditText et = view.findViewById(R.id.etBusqueda);
        // Botón de búsqueda
        view.findViewById(R.id.btnRealizarBusqueda).setOnClickListener(v -> {
            filtrarCalendario(et.getText().toString().trim().toLowerCase());
            dialog.dismiss();
        });
        // Botón de cancelar
        view.findViewById(R.id.btnCancelarBusqueda).setOnClickListener(v -> dialog.dismiss());
        // Mostramos el diálogo
        dialog.show();
    }

    // Filtra la lista de tareas según el texto ingresado en el diálogo de búsqueda
    private void filtrarCalendario(String texto) {
        // Si no hay texto, cargamos las tareas del día
        if (texto.isEmpty()) {
            cargarTareasDelDia();
            return;
        }

        // Filtramos sobre la lista visual actual (las del día)
        List<Tarea> listaFiltrada = new ArrayList<>();
        for (Tarea t : listaTareasVisual) {
            if (t.getTitulo().toLowerCase().contains(texto)) {
                listaFiltrada.add(t);
            }
        }

        // Si no hay resultados, mostramos un mensaje, sino, mostramos la lista filtrada
        if (listaFiltrada.isEmpty()) {
            Toast.makeText(this, "No se ha encontrado en este día.", Toast.LENGTH_SHORT).show();
        } else {CalendarioAdapter adapterFiltro = new CalendarioAdapter(listaFiltrada, listenerAcciones);
            rvCalendario.setAdapter(adapterFiltro);

            // Mejora el Scroll
            Tarea primerResultado = listaFiltrada.get(0);
            int horaInicio24 = primerResultado.getHoraInicio24(); // Usa el método de Tarea

            // Hace un scroll suave hasta la hora
            rvCalendario.scrollToPosition(horaInicio24);
        }
    }
}