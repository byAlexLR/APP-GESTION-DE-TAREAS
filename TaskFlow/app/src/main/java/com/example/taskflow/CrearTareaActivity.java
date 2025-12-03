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
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class CrearTareaActivity extends AppCompatActivity {

    // Vistas
    private EditText etNombre, etDesc, etUbicacion;
    private TextView tvTituloPantalla; // El texto de arriba

    // Spinners Inicio
    private Spinner spDiaInicio, spMesInicio, spAnoInicio, spHoraInicio, spMinInicio, spAmPmInicio;
    // Spinners Fin
    private Spinner spDiaFin, spMesFin, spAnoFin, spHoraFin, spMinFin, spAmPmFin;

    private LinearLayout containerNotificaciones, containerColaboradores;

    // Variables de control
    private int posicionOriginal = -1; // -1 = Nueva, >=0 = Editar
    private final String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
    private final String[] anos = {"2025", "2026", "2027", "2028", "2029", "2030"};
    private final String[] mins = {"00", "15", "30", "45"};
    private final String[] ampm = {"AM", "PM"};
    private final String[] unidadesTiempo = {"Minutos antes", "Horas antes", "Días antes"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_tarea);

        vincularVistas();
        configurarSpinners();
        configurarListeners();

        // === LÓGICA DE EDICIÓN ===
        // Verificamos si el MainActivity nos mandó una tarea para editar
        if (getIntent().hasExtra("TAREA_A_EDITAR")) {
            Tarea tareaRecibida = (Tarea) getIntent().getSerializableExtra("TAREA_A_EDITAR");
            posicionOriginal = getIntent().getIntExtra("POSICION_ORIGINAL", -1);

            if (tareaRecibida != null) {
                cargarDatosParaEditar(tareaRecibida);
            }
        }

        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardarTarea());
    }

    // --- AQUÍ OCURRE LA MAGIA DE RELLENAR LOS DATOS ---
    private void cargarDatosParaEditar(Tarea t) {
        // 1. Cambiar el título de la pantalla
        tvTituloPantalla.setText("EDITAR TAREA");

        // 2. Rellenar textos básicos
        etNombre.setText(t.getTitulo());
        etDesc.setText(t.getDescripcion());
        etUbicacion.setText(t.getUbicacion());

        // 3. Seleccionar los valores en los Spinners de INICIO
        // Usamos .getMes() porque guardamos el índice (0=Ene, 1=Feb...)
        spMesInicio.setSelection(t.getMes());
        seleccionarTextoSpinner(spAnoInicio, String.valueOf(t.getAnio()));

        // Convertimos los números a String con formato (ej: 9 -> "09")
        String horaInStr = t.getHoraInicio() < 10 ? "0" + t.getHoraInicio() : String.valueOf(t.getHoraInicio());
        seleccionarTextoSpinner(spHoraInicio, horaInStr);

        String minInStr = t.getMinInicio() < 10 ? "0" + t.getMinInicio() : String.valueOf(t.getMinInicio());
        seleccionarTextoSpinner(spMinInicio, minInStr);

        seleccionarTextoSpinner(spAmPmInicio, t.getAmPmInicio());

        // 4. Seleccionar los valores en los Spinners de FIN
        String horaOutStr = t.getHoraFin() < 10 ? "0" + t.getHoraFin() : String.valueOf(t.getHoraFin());
        seleccionarTextoSpinner(spHoraFin, horaOutStr);

        String minOutStr = t.getMinFin() < 10 ? "0" + t.getMinFin() : String.valueOf(t.getMinFin());
        seleccionarTextoSpinner(spMinFin, minOutStr);

        seleccionarTextoSpinner(spAmPmFin, t.getAmPmFin());

        // 5. Truco para el DÍA: Como los días dependen del mes, hay que esperar un poquito a que se carguen
        spDiaInicio.post(() -> {
            String diaStr = t.getDia() < 10 ? "0" + t.getDia() : String.valueOf(t.getDia());
            seleccionarTextoSpinner(spDiaInicio, diaStr);
            // Asumimos mismo día para fin por ahora
            seleccionarTextoSpinner(spDiaFin, diaStr);
        });
    }

    private void guardarTarea() {
        String nombre = etNombre.getText().toString();
        if (nombre.isEmpty()) nombre = "Sin Título";
        String desc = etDesc.getText().toString();
        String ubi = etUbicacion.getText().toString();

        // Valores por defecto
        int dia = 1, mes = 0, anio = 2025;
        int horaIn = 1, minIn = 0, horaOut = 1, minOut = 0;
        String apIn = "AM", apOut = "AM";

        try {
            dia = Integer.parseInt(spDiaInicio.getSelectedItem().toString());
            mes = spMesInicio.getSelectedItemPosition();
            anio = Integer.parseInt(spAnoInicio.getSelectedItem().toString());

            horaIn = Integer.parseInt(spHoraInicio.getSelectedItem().toString());
            minIn = Integer.parseInt(spMinInicio.getSelectedItem().toString());
            apIn = spAmPmInicio.getSelectedItem().toString();

            horaOut = Integer.parseInt(spHoraFin.getSelectedItem().toString());
            minOut = Integer.parseInt(spMinFin.getSelectedItem().toString());
            apOut = spAmPmFin.getSelectedItem().toString();
        } catch (Exception e) { e.printStackTrace(); }

        String fechaStr = dia + " " + meses[mes] + " " + anio + " · " +
                horaIn + ":" + (minIn < 10 ? "0"+minIn : minIn) + " " + apIn + " - " +
                horaOut + ":" + (minOut < 10 ? "0"+minOut : minOut) + " " + apOut;

        // Creamos la tarea actualizada
        Tarea nuevaTarea = new Tarea(nombre, fechaStr, desc, ubi, dia, mes, anio, horaIn, minIn, apIn, horaOut, minOut, apOut);

        Intent resultado = new Intent();
        resultado.putExtra("TAREA_OBJETO", nuevaTarea);
        resultado.putExtra("POSICION_EDITADA", posicionOriginal); // Esto le dice al Main qué posición actualizar

        setResult(Activity.RESULT_OK, resultado);
        finish();
    }

    // --- Helpers y Configuración ---
    private void seleccionarTextoSpinner(Spinner spinner, String texto) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equals(texto)) {
                    spinner.setSelection(i);
                    return;
                }
            }
        }
    }

    private void vincularVistas() {
        tvTituloPantalla = findViewById(R.id.tvTituloPantalla);
        etNombre = findViewById(R.id.etNombre);
        etDesc = findViewById(R.id.etDesc);
        etUbicacion = findViewById(R.id.etUbicacion);

        spDiaInicio = findViewById(R.id.spDiaInicio); spMesInicio = findViewById(R.id.spMesInicio);
        spAnoInicio = findViewById(R.id.spAnoInicio); spHoraInicio = findViewById(R.id.spHoraInicio);
        spMinInicio = findViewById(R.id.spMinInicio); spAmPmInicio = findViewById(R.id.spAmPmInicio);

        spDiaFin = findViewById(R.id.spDiaFin); spMesFin = findViewById(R.id.spMesFin);
        spAnoFin = findViewById(R.id.spAnoFin); spHoraFin = findViewById(R.id.spHoraFin);
        spMinFin = findViewById(R.id.spMinFin); spAmPmFin = findViewById(R.id.spAmPmFin);

        containerNotificaciones = findViewById(R.id.containerNotificaciones);
        containerColaboradores = findViewById(R.id.containerColaboradores);
    }

    private void configurarSpinners() {
        setupSpinner(spMesInicio, meses); setupSpinner(spAnoInicio, anos);
        setupSpinner(spHoraInicio, generarNumeros(1, 12)); setupSpinner(spMinInicio, mins); setupSpinner(spAmPmInicio, ampm);
        setupSpinner(spMesFin, meses); setupSpinner(spAnoFin, anos);
        setupSpinner(spHoraFin, generarNumeros(1, 12)); setupSpinner(spMinFin, mins); setupSpinner(spAmPmFin, ampm);

        actualizarDiasDelMes(spMesInicio, spAnoInicio, spDiaInicio);
        actualizarDiasDelMes(spMesFin, spAnoFin, spDiaFin);
    }

    private void configurarListeners() {
        configurarListenerFechas(spMesInicio, spAnoInicio, spDiaInicio);
        configurarListenerFechas(spMesFin, spAnoFin, spDiaFin);
        findViewById(R.id.btnAddNotif).setOnClickListener(v -> agregarFila(R.layout.item_notificacion, containerNotificaciones));
        findViewById(R.id.btnAddColab).setOnClickListener(v -> agregarFila(R.layout.item_colaborador, containerColaboradores));
    }

    private void agregarFila(int layoutId, LinearLayout container) {
        View view = getLayoutInflater().inflate(layoutId, container, false);
        if (layoutId == R.layout.item_notificacion) {
            setupSpinner(view.findViewById(R.id.spUnidad), unidadesTiempo);
        }
        container.addView(view);
    }

    private void configurarListenerFechas(Spinner spMes, Spinner spAno, Spinner spDiaDestino) {
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { actualizarDiasDelMes(spMes, spAno, spDiaDestino); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        };
        spMes.setOnItemSelectedListener(listener); spAno.setOnItemSelectedListener(listener);
    }

    private void actualizarDiasDelMes(Spinner spMes, Spinner spAno, Spinner spDia) {
        int mesIndex = spMes.getSelectedItemPosition();
        int anio = Integer.parseInt(spAno.getSelectedItem().toString());
        int dias = 31;
        if (mesIndex == 3 || mesIndex == 5 || mesIndex == 8 || mesIndex == 10) dias = 30;
        else if (mesIndex == 1) dias = ((anio % 4 == 0 && anio % 100 != 0) || (anio % 400 == 0)) ? 29 : 28;

        String[] diasArr = generarNumeros(1, dias);
        int prev = spDia.getSelectedItemPosition();
        setupSpinner(spDia, diasArr);
        spDia.setSelection(Math.min(prev, diasArr.length - 1));
    }

    private String[] generarNumeros(int i, int f) {
        List<String> l = new ArrayList<>();
        for (int k = i; k <= f; k++) l.add(k < 10 ? "0" + k : String.valueOf(k));
        return l.toArray(new String[0]);
    }

    private void setupSpinner(Spinner s, String[] d) {
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, d);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(a);
    }
}