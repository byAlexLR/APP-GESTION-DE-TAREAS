package com.example.taskflow;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarioActivity extends AppCompatActivity {

    private RecyclerView rvCalendario;
    private CalendarioAdapter adapter;
    private List<Tarea> listaTareasVisual; // La lista que se ve en pantalla (filtrada por día)

    // Variables Fecha
    private Calendar fechaSeleccionadaCalendar;
    private TextView tvMesAnioSelector, tvFechaSeleccionadaBig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendario);

        // Inicializar vistas
        tvMesAnioSelector = findViewById(R.id.tvMesAnioSelector);
        tvFechaSeleccionadaBig = findViewById(R.id.tvFechaSeleccionada);
        LinearLayout btnSelectorFecha = findViewById(R.id.btnSelectorFecha);
        rvCalendario = findViewById(R.id.rvCalendario);
        rvCalendario.setLayoutManager(new LinearLayoutManager(this));

        // 1. Configurar Fecha Inicial (Hoy)
        fechaSeleccionadaCalendar = Calendar.getInstance();
        actualizarTextosFecha();

        // 2. Inicializar Lista y Adaptador
        listaTareasVisual = new ArrayList<>();

        adapter = new CalendarioAdapter(listaTareasVisual, new CalendarioAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Tarea tarea) {
                // Abrir editar
                Intent intent = new Intent(CalendarioActivity.this, CrearTareaActivity.class);
                intent.putExtra("TAREA_A_EDITAR", tarea);
                // Pasamos -1 o 0 porque ahora gestionamos por objeto en el repositorio
                intent.putExtra("POSICION_ORIGINAL", 0);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Tarea tarea) {
                // Borrar del repositorio GLOBAL
                Repositorio.tareasGlobales.remove(tarea);
                cargarTareasDelDia(); // Refrescar pantalla
                Toast.makeText(CalendarioActivity.this, "Tarea eliminada", Toast.LENGTH_SHORT).show();
            }

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
                Repositorio.tareasGlobales.add(copia);
                cargarTareasDelDia();
                Toast.makeText(CalendarioActivity.this, "Tarea duplicada", Toast.LENGTH_SHORT).show();
            }
        });
        rvCalendario.setAdapter(adapter);

        // 3. Cargar datos iniciales
        cargarTareasDelDia();

        // 4. Listeners
        btnSelectorFecha.setOnClickListener(v -> mostrarSelectorFecha());

        findViewById(R.id.fabAddCal).setOnClickListener(v -> {
            startActivity(new Intent(CalendarioActivity.this, CrearTareaActivity.class));
        });

        setupToolbarButtons();
    }

    // Se llama cada vez que volvemos a esta pantalla (ej: después de crear tarea)
    @Override
    protected void onResume() {
        super.onResume();
        cargarTareasDelDia();
    }

    // --- MÉTODOS CORE ---

    private void cargarTareasDelDia() {
        listaTareasVisual.clear();

        int diaSel = fechaSeleccionadaCalendar.get(Calendar.DAY_OF_MONTH);
        int mesSel = fechaSeleccionadaCalendar.get(Calendar.MONTH);
        int anioSel = fechaSeleccionadaCalendar.get(Calendar.YEAR);

        // Filtramos del repositorio global
        for (Tarea t : Repositorio.tareasGlobales) {
            if (t.getDia() == diaSel && t.getMes() == mesSel && t.getAnio() == anioSel) {
                listaTareasVisual.add(t);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void mostrarSelectorFecha() {
        int anio = fechaSeleccionadaCalendar.get(Calendar.YEAR);
        int mes = fechaSeleccionadaCalendar.get(Calendar.MONTH);
        int dia = fechaSeleccionadaCalendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, y, m, d) -> {
            fechaSeleccionadaCalendar.set(y, m, d);
            actualizarTextosFecha();
            cargarTareasDelDia(); // ¡Importante! Recargar lista al cambiar fecha
        }, anio, mes, dia).show();
    }

    private void actualizarTextosFecha() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        String t = sdf.format(fechaSeleccionadaCalendar.getTime());
        tvMesAnioSelector.setText(t.substring(0, 1).toUpperCase() + t.substring(1));

        SimpleDateFormat sdf2 = new SimpleDateFormat("dd MMMM yyyy", new Locale("es", "ES"));
        tvFechaSeleccionadaBig.setText(sdf2.format(fechaSeleccionadaCalendar.getTime()).toUpperCase());
    }

    // --- BARRA DE HERRAMIENTAS ---

    private void setupToolbarButtons() {
        findViewById(R.id.btnBuscar).setOnClickListener(v -> mostrarDialogoBusqueda());

        // Botón Calendario: Volver a Hoy
        findViewById(R.id.btnVerMes).setOnClickListener(v -> {
            fechaSeleccionadaCalendar = Calendar.getInstance();
            actualizarTextosFecha();
            cargarTareasDelDia();
            Toast.makeText(this, "Vuelto a Hoy", Toast.LENGTH_SHORT).show();
        });
    }

    private void mostrarDialogoBusqueda() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_buscar, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if(dialog.getWindow()!=null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        EditText et = view.findViewById(R.id.etBusqueda);
        view.findViewById(R.id.btnRealizarBusqueda).setOnClickListener(v -> {
            filtrarCalendario(et.getText().toString().trim().toLowerCase());
            dialog.dismiss();
        });
        view.findViewById(R.id.btnCancelarBusqueda).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void filtrarCalendario(String texto) {
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

        if (listaFiltrada.isEmpty()) {
            Toast.makeText(this, "No encontrado en este día", Toast.LENGTH_SHORT).show();
        } else {
            // Creamos un adaptador temporal solo para mostrar la búsqueda
            // (Nota: al volver a onResume o cambiar fecha se resetea)
            CalendarioAdapter adapterFiltro = new CalendarioAdapter(listaFiltrada, new CalendarioAdapter.OnItemClickListener() {
                @Override public void onEditClick(Tarea t) { startActivity(new Intent(CalendarioActivity.this, CrearTareaActivity.class).putExtra("TAREA_A_EDITAR", t)); }
                @Override public void onDeleteClick(Tarea t) { Repositorio.tareasGlobales.remove(t); cargarTareasDelDia(); }
                @Override public void onDuplicateClick(Tarea t) { Repositorio.tareasGlobales.add(t); cargarTareasDelDia(); }
            });
            rvCalendario.setAdapter(adapterFiltro);
        }
    }
}