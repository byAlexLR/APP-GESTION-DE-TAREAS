package com.example.taskflow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class CrearTareaActivity extends AppCompatActivity {

    private EditText etNombre;

    // Controles de fecha
    private Spinner spDiaInicio, spMesInicio, spAnoInicio, spHoraInicio, spMinInicio, spAmPmInicio;
    private Spinner spDiaFin, spMesFin, spAnoFin, spHoraFin, spMinFin, spAmPmFin;

    // Contenedores dinámicos
    private LinearLayout containerNotificaciones;
    private LinearLayout containerColaboradores;

    // Arrays de datos
    private final String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
    private final String[] anos = {"2025", "2026", "2027", "2028", "2029", "2030"};
    private final String[] unidadesTiempo = {"Minutos antes", "Horas antes", "Días antes", "Segundos antes"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_tarea);

        // 1. VINCULAR CONTROLES
        etNombre = findViewById(R.id.etNombre);

        // Inicio
        spDiaInicio = findViewById(R.id.spDiaInicio); spMesInicio = findViewById(R.id.spMesInicio);
        spAnoInicio = findViewById(R.id.spAnoInicio); spHoraInicio = findViewById(R.id.spHoraInicio);
        spMinInicio = findViewById(R.id.spMinInicio); spAmPmInicio = findViewById(R.id.spAmPmInicio);

        // Fin
        spDiaFin = findViewById(R.id.spDiaFin); spMesFin = findViewById(R.id.spMesFin);
        spAnoFin = findViewById(R.id.spAnoFin); spHoraFin = findViewById(R.id.spHoraFin);
        spMinFin = findViewById(R.id.spMinFin); spAmPmFin = findViewById(R.id.spAmPmFin);

        // Contenedores
        containerNotificaciones = findViewById(R.id.containerNotificaciones);
        containerColaboradores = findViewById(R.id.containerColaboradores);

        // 2. CONFIGURAR DATOS DE FECHAS
        setupSpinner(spMesInicio, meses); setupSpinner(spAnoInicio, anos);
        setupSpinner(spHoraInicio, generarNumeros(1, 12)); setupSpinner(spMinInicio, new String[]{"00", "15", "30", "45"});
        setupSpinner(spAmPmInicio, new String[]{"AM", "PM"});

        setupSpinner(spMesFin, meses); setupSpinner(spAnoFin, anos);
        setupSpinner(spHoraFin, generarNumeros(1, 12)); setupSpinner(spMinFin, new String[]{"00", "15", "30", "45"});
        setupSpinner(spAmPmFin, new String[]{"AM", "PM"});

        // 3. INICIALIZAR LA PRIMERA FILA DE NOTIFICACIÓN
        View primerFilaNotif = containerNotificaciones.getChildAt(0);
        if (primerFilaNotif != null) {
            Spinner spUnidad = primerFilaNotif.findViewById(R.id.spUnidad);
            setupSpinner(spUnidad, unidadesTiempo);
        }

        // 4. LISTENERS PARA FECHAS (Días dinámicos 28/30/31)
        configurarListenerFechas(spMesInicio, spAnoInicio, spDiaInicio);
        configurarListenerFechas(spMesFin, spAnoFin, spDiaFin);

        // 5. BOTONES "MÁS" (+)
        ImageView btnAddNotif = findViewById(R.id.btnAddNotif);
        btnAddNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarFilaNotificacion();
            }
        });

        ImageView btnAddColab = findViewById(R.id.btnAddColab);
        btnAddColab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarFilaColaborador();
            }
        });

        // 6. BOTÓN GUARDAR (CHECK)
        ImageView btnGuardar = findViewById(R.id.btnGuardar);
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarTarea();
            }
        });
    }

    // --- LÓGICA DE AÑADIR FILAS ---
    private void agregarFilaNotificacion() {
        View nuevaFila = getLayoutInflater().inflate(R.layout.item_notificacion, containerNotificaciones, false);
        Spinner spUnidad = nuevaFila.findViewById(R.id.spUnidad);
        setupSpinner(spUnidad, unidadesTiempo);
        containerNotificaciones.addView(nuevaFila);
    }

    private void agregarFilaColaborador() {
        View nuevaFila = getLayoutInflater().inflate(R.layout.item_colaborador, containerColaboradores, false);
        containerColaboradores.addView(nuevaFila);
    }

    // --- GUARDAR Y ENVIAR DATOS ---
    private void guardarTarea() {
        String nombre = etNombre.getText().toString();
        if (nombre.isEmpty()) nombre = "Nueva Tarea";

        // Texto visual de la fecha
        String fechaStr = spDiaInicio.getSelectedItem().toString() + " " +
                spMesInicio.getSelectedItem().toString() + " " +
                spAnoInicio.getSelectedItem().toString() + " · " +
                spHoraInicio.getSelectedItem().toString() + ":" +
                spMinInicio.getSelectedItem().toString() + " " +
                spAmPmInicio.getSelectedItem().toString() + " - " +
                spHoraFin.getSelectedItem().toString() + ":" +
                spMinFin.getSelectedItem().toString() + " " +
                spAmPmFin.getSelectedItem().toString();

        Intent resultado = new Intent();
        resultado.putExtra("TITULO_NUEVO", nombre);
        resultado.putExtra("FECHA_NUEVA", fechaStr);

        // --- NUEVO: ENVIAMOS LOS NÚMEROS EXACTOS PARA COMPARAR ---
        resultado.putExtra("DIA_EXACTO", Integer.parseInt(spDiaInicio.getSelectedItem().toString()));
        resultado.putExtra("MES_EXACTO", spMesInicio.getSelectedItemPosition()); // 0=Enero
        resultado.putExtra("ANO_EXACTO", Integer.parseInt(spAnoInicio.getSelectedItem().toString()));

        setResult(Activity.RESULT_OK, resultado);
        finish();
    }

    // --- AYUDANTES ---
    private void configurarListenerFechas(Spinner spMes, Spinner spAno, Spinner spDiaDestino) {
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actualizarDiasDelMes(spMes, spAno, spDiaDestino);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        spMes.setOnItemSelectedListener(listener);
        spAno.setOnItemSelectedListener(listener);
    }

    private void actualizarDiasDelMes(Spinner spMes, Spinner spAno, Spinner spDia) {
        int mesIndex = spMes.getSelectedItemPosition();
        int anio = Integer.parseInt(spAno.getSelectedItem().toString());
        int diasEnMes = 31;
        if (mesIndex == 3 || mesIndex == 5 || mesIndex == 8 || mesIndex == 10) diasEnMes = 30;
        else if (mesIndex == 1) diasEnMes = esBisiesto(anio) ? 29 : 28;

        String[] diasArray = generarNumeros(1, diasEnMes);
        int seleccionAnterior = spDia.getSelectedItemPosition();
        setupSpinner(spDia, diasArray);
        if (seleccionAnterior < diasArray.length) spDia.setSelection(seleccionAnterior);
        else spDia.setSelection(diasArray.length - 1);
    }

    private boolean esBisiesto(int anio) {
        return (anio % 4 == 0 && anio % 100 != 0) || (anio % 400 == 0);
    }

    private String[] generarNumeros(int inicio, int fin) {
        List<String> lista = new ArrayList<>();
        for (int i = inicio; i <= fin; i++) {
            lista.add(i < 10 ? "0" + i : String.valueOf(i));
        }
        return lista.toArray(new String[0]);
    }

    private void setupSpinner(Spinner spinner, String[] datos) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, datos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}