package com.example.taskflow;

// Importación de librerías necesarias
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.util.Log;

public class CrearTareaActivity extends AppCompatActivity {

    // Variables globales
    private EditText etNombre, etDesc, etUbicacion;
    private TextView tvTituloPantalla;

    // Spinners de las fechas
    private Spinner spDiaInicio, spMesInicio, spAnoInicio, spHoraInicio, spMinInicio, spAmPmInicio;
    private Spinner spDiaFin, spMesFin, spAnoFin, spHoraFin, spMinFin, spAmPmFin;

    // Contenedores de notificaciones y colaboradores
    private LinearLayout containerNotificaciones, containerColaboradores;

    // Variables auxiliares
    private int posicionOriginal = -1;
    private final String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
    private final String[] anos = {"2025", "2026", "2027", "2028", "2029", "2030"};
    private final String[] ampm = {"AM", "PM"};
    private final String[] unidadesTiempo = {"Minutos antes", "Horas antes", "Días antes", "Segundos antes"};

    @Override
    // Método principal de la actividad
    protected void onCreate(Bundle savedInstanceState) {
        // Llamada al método onCreate de la superclase
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_tarea);

        vincularVistas(); // Aquí rellenamos los datos
        configurarSpinners(); // Aquí configuramos los spinners
        configurarListeners(); // Aquí configuramos los listeners

        // Comprobamos si estamos editando o creando una tarea
        if (getIntent().hasExtra("TAREA_A_EDITAR")) {
            Tarea tarea = (Tarea) getIntent().getSerializableExtra("TAREA_A_EDITAR");
            posicionOriginal = getIntent().getIntExtra("POSICION_ORIGINAL", -1);
            if (tarea != null) cargarDatosTarea(tarea);
        } else {
            cargarFechaHoraActual(); // Aquí cargamos la fecha y hora actual
        }

        // Botón de guardar
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardarTarea());
    }

    // --- VINCULACIÓN DE VISTAS ---
    private void vincularVistas() {
        // Vistas de la actividad
        tvTituloPantalla = findViewById(R.id.tvTituloPantalla);
        etNombre = findViewById(R.id.etNombre);
        etDesc = findViewById(R.id.etDesc);
        etUbicacion = findViewById(R.id.etUbicacion);

        // Spinners de las fechas
        spDiaInicio = findViewById(R.id.spDiaInicio);
        spMesInicio = findViewById(R.id.spMesInicio);
        spAnoInicio = findViewById(R.id.spAnoInicio);
        spHoraInicio = findViewById(R.id.spHoraInicio);
        spMinInicio = findViewById(R.id.spMinInicio);
        spAmPmInicio = findViewById(R.id.spAmPmInicio);
        spDiaFin = findViewById(R.id.spDiaFin);
        spMesFin = findViewById(R.id.spMesFin);
        spAnoFin = findViewById(R.id.spAnoFin);
        spHoraFin = findViewById(R.id.spHoraFin);
        spMinFin = findViewById(R.id.spMinFin);
        spAmPmFin = findViewById(R.id.spAmPmFin);

        // Contenedores de notificaciones y colaboradores
        containerNotificaciones = findViewById(R.id.containerNotificaciones);
        containerColaboradores = findViewById(R.id.containerColaboradores);
    }

    // Método mejorado para generar números (Para las horas y minutos)
    private String[] generarNumeros(int inicio, int fin) {
        List<String> l = new ArrayList<>();
        for (int k = inicio; k <= fin; k++) {
            l.add(k < 10 ? "0" + k : String.valueOf(k));
        }
        return l.toArray(new String[0]);
    }

    // --- CONFIGURAR SPINNERS ---
    private void configurarSpinners() {
        // Generar los números para los spinners
        setupSpinner(spMesInicio, meses);
        setupSpinner(spAnoInicio, anos);
        setupSpinner(spHoraInicio, generarNumeros(1, 12));
        setupSpinner(spMinInicio, generarNumeros(0, 59));
        setupSpinner(spAmPmInicio, ampm);
        setupSpinner(spMesFin, meses);
        setupSpinner(spAnoFin, anos);
        setupSpinner(spHoraFin, generarNumeros(1, 12));
        setupSpinner(spMinFin, generarNumeros(0, 59));
        setupSpinner(spAmPmFin, ampm);

        // Busca la primera fila de notificaciones
        if (containerNotificaciones.getChildCount() > 0) {
            // Si hay filas, obtenemos la primera
            View primeraFila = containerNotificaciones.getChildAt(0);
            Spinner spUnidad = primeraFila.findViewById(R.id.spUnidad);
            // Si la hemos encontrado, la rellenamos
            if (spUnidad != null) {
                setupSpinner(spUnidad, unidadesTiempo);
            }
        }
        // Actualiza los días del mes
        actualizarDiasDelMes(spMesInicio, spAnoInicio, spDiaInicio);
        actualizarDiasDelMes(spMesFin, spAnoFin, spDiaFin);
    }

    // --- LISTENERS ---
    private void configurarListeners() {
        // Configuramos los listeners de las fechas
        configurarListenerFechas(spMesInicio, spAnoInicio, spDiaInicio);
        configurarListenerFechas(spMesFin, spAnoFin, spDiaFin);

        // Al pulsar + Notificación
        findViewById(R.id.btnAddNotif).setOnClickListener(v -> agregarFila(R.layout.item_notificacion, containerNotificaciones));

        // Al pulsar + Colaborador
        findViewById(R.id.btnAddColab).setOnClickListener(v -> agregarFila(R.layout.item_colaborador, containerColaboradores));
    }

    // Método genérico para añadir filas dinámicas
    private void agregarFila(int layoutId, LinearLayout container) {
        // Creamos la vista de la fila
        View view = getLayoutInflater().inflate(layoutId, container, false);

        // Si es una notificación, rellenamos el spinner de unidades
        if (layoutId == R.layout.item_notificacion) {
            Spinner spUnidad = view.findViewById(R.id.spUnidad);
            setupSpinner(spUnidad, unidadesTiempo);
        }
        // Añadimos la fila al contenedor
        container.addView(view);
    }

    // --- GUARDAR ---
    private void guardarTarea() {
        // Recoge el nombre de la tarea
        String nombre = etNombre.getText().toString().trim();
        // Comprobamos que el nombre no esté vacío
        if (nombre.isEmpty()) {
            Toast.makeText(this, "No se puede añadir una tarea sin título", Toast.LENGTH_SHORT).show();
            return;
        }

        // Recoge los datos de la tarea
        String desc = etDesc.getText().toString();
        String ubi = etUbicacion.getText().toString();

        // Recogemos los datos de las fechas
        int dia = 1, mes = 0, anio = 2025, horaIn = 0, minIn = 0, horaOut = 0, minOut = 0;
        String apIn = "AM", apOut = "AM";
        String notifCant = "30", notifUni = "Minutos antes";

        try {
            // Recoger datos de las fechas
            String diaStrSpinner = spDiaInicio.getSelectedItem().toString();
            dia = Integer.parseInt(diaStrSpinner);
            mes = spMesInicio.getSelectedItemPosition();
            anio = Integer.parseInt(spAnoInicio.getSelectedItem().toString());
            horaIn = Integer.parseInt(spHoraInicio.getSelectedItem().toString());
            minIn = Integer.parseInt(spMinInicio.getSelectedItem().toString());
            apIn = spAmPmInicio.getSelectedItem().toString();
            horaOut = Integer.parseInt(spHoraFin.getSelectedItem().toString());
            minOut = Integer.parseInt(spMinFin.getSelectedItem().toString());
            apOut = spAmPmFin.getSelectedItem().toString();

            // Recoge los datos de las notificaciones
            if (containerNotificaciones.getChildCount() > 0) {
                View fila = containerNotificaciones.getChildAt(0);
                EditText etCant = fila.findViewById(R.id.etCantidad);
                Spinner spUni = fila.findViewById(R.id.spUnidad);
                notifCant = etCant.getText().toString();
                notifUni = spUni.getSelectedItem().toString();
            }
            // Recoge las posibles excepciones
        } catch (Exception e) {
            Log.e("CrearTarea", "Error al parsear datos de los spinners", e);
        }

        // Construye la cadena de la fecha y la hora
        String fechaStr = dia + " " + meses[mes] + " " + anio + " · " +
                horaIn + ":" + (minIn < 10 ? "0" + minIn : minIn) + " " + apIn + " - " +
                horaOut + ":" + (minOut < 10 ? "0" + minOut : minOut) + " " + apOut;

        // Crea la nueva tarea
        Tarea nuevaTarea = new Tarea(nombre, fechaStr, desc, ubi, dia, mes, anio,
                horaIn, minIn, apIn, horaOut, minOut, apOut,
                notifCant, notifUni);

        // Si estamos editando, actualiza la tarea. Sino, la añade
        if (posicionOriginal != -1 && posicionOriginal < Repositorio.tareasGlobales.size()) {
            Tarea tareaAntigua = Repositorio.tareasGlobales.get(posicionOriginal);
            nuevaTarea.setCompletada(tareaAntigua.isCompletada()); // Copia el estado
            Repositorio.tareasGlobales.set(posicionOriginal, nuevaTarea);
        } else {
            Repositorio.tareasGlobales.add(nuevaTarea);
        }

        // Devolvemos la nueva tarea al MainActivity
        Intent resultado = new Intent();
        resultado.putExtra("TAREA_OBJETO", nuevaTarea);
        resultado.putExtra("POSICION_EDITADA", posicionOriginal);
        setResult(Activity.RESULT_OK, resultado);
        finish();
    }

    @SuppressLint("SetTextI18n")
    // Cargamos los datos de la tarea
    private void cargarDatosTarea(Tarea t) {
        // Rellena los datos
        tvTituloPantalla.setText("EDITAR TAREA");
        etNombre.setText(t.getTitulo());
        etDesc.setText(t.getDescripcion());
        etUbicacion.setText(t.getUbicacion());

        // Rellena la fecha y hora de inicio
        seleccionarSpinner(spMesInicio, meses[t.getMes()]);
        seleccionarSpinner(spAnoInicio, String.valueOf(t.getAnio()));
        seleccionarSpinner(spHoraInicio, t.getHoraInicio() < 10 ? "0" + t.getHoraInicio() : String.valueOf(t.getHoraInicio()));
        seleccionarSpinner(spMinInicio, t.getMinInicio() < 10 ? "0" + t.getMinInicio() : String.valueOf(t.getMinInicio()));
        seleccionarSpinner(spAmPmInicio, t.getAmPmInicio());

        // Rellena la fecha y hora de fin
        seleccionarSpinner(spHoraFin, t.getHoraFin() < 10 ? "0" + t.getHoraFin() : String.valueOf(t.getHoraFin()));
        seleccionarSpinner(spMinFin, t.getMinFin() < 10 ? "0" + t.getMinFin() : String.valueOf(t.getMinFin()));
        seleccionarSpinner(spAmPmFin, t.getAmPmFin());

        // Rellena las notificaciones
        if (containerNotificaciones.getChildCount() > 0) {
            View fila = containerNotificaciones.getChildAt(0);
            ((EditText) fila.findViewById(R.id.etCantidad)).setText(t.getNotifCantidad());
            seleccionarSpinner(fila.findViewById(R.id.spUnidad), t.getNotifUnidad());
        }

        // Rellena los días de inicio y fin
        spDiaInicio.post(() -> seleccionarSpinner(spDiaInicio, t.getDia() < 10 ? "0" + t.getDia() : String.valueOf(t.getDia())));
        spDiaFin.post(() -> seleccionarSpinner(spDiaFin, t.getDia() < 10 ? "0" + t.getDia() : String.valueOf(t.getDia())));
    }

    // Cargamos la fecha y hora actual
    private void cargarFechaHoraActual() {
        Calendar hoy = Calendar.getInstance();

        // Obtiene el día, mes y año actual, también la hora y minuto
        int dia = hoy.get(Calendar.DAY_OF_MONTH);
        int mes = hoy.get(Calendar.MONTH);
        int anio = hoy.get(Calendar.YEAR);
        int hora24 = hoy.get(Calendar.HOUR_OF_DAY);
        int minuto = hoy.get(Calendar.MINUTE);

        // Convierte la hora a AM/PM
        String amPmStr = (hora24 >= 12) ? "PM" : "AM";
        int hora12 = (hora24 > 12) ? hora24 - 12 : hora24;
        if (hora12 == 0) hora12 = 12; // Las 00:00 son las 12 AM

        // Selecciona en los Spinners de INICIO. Por defecto, el actual.
        seleccionarSpinner(spMesInicio, meses[mes]);
        seleccionarSpinner(spAnoInicio, String.valueOf(anio));

        // El día necesita actualizarse tras cambiar mes/año
        spDiaInicio.post(() -> seleccionarSpinner(spDiaInicio, dia < 10 ? "0" + dia : String.valueOf(dia)));

        // Selecciona en los Spinners de INICIO
        seleccionarSpinner(spHoraInicio, hora12 < 10 ? "0" + hora12 : String.valueOf(hora12));
        seleccionarSpinner(spMinInicio, minuto < 10 ? "0" + minuto : String.valueOf(minuto));
        seleccionarSpinner(spAmPmInicio, amPmStr);

        // Selecciona en los Spinners de FIN. Por defecto, 1 hora después.
        Calendar fin = (Calendar) hoy.clone();
        fin.add(Calendar.HOUR_OF_DAY, 1);

        // Obtiene el día, mes y año actual, también la hora y minuto
        int diaFin = fin.get(Calendar.DAY_OF_MONTH);
        int mesFin = fin.get(Calendar.MONTH);
        int anioFin = fin.get(Calendar.YEAR);
        int horaFin24 = fin.get(Calendar.HOUR_OF_DAY);
        int minFin = fin.get(Calendar.MINUTE);

        // Convierte la hora a AM/PM
        String amPmFinStr = (horaFin24 >= 12) ? "PM" : "AM";
        int horaFin12 = (horaFin24 > 12) ? horaFin24 - 12 : horaFin24;
        if (horaFin12 == 0) horaFin12 = 12;

        // Selecciona en los Spinners de FIN
        seleccionarSpinner(spMesFin, meses[mesFin]);
        seleccionarSpinner(spAnoFin, String.valueOf(anioFin));

        // El día necesita actualizarse tras cambiar mes/año
        spDiaFin.post(() -> seleccionarSpinner(spDiaFin, diaFin < 10 ? "0" + diaFin : String.valueOf(diaFin)));

        // Selecciona en los Spinners de FIN
        seleccionarSpinner(spHoraFin, horaFin12 < 10 ? "0" + horaFin12 : String.valueOf(horaFin12));
        seleccionarSpinner(spMinFin, minFin < 10 ? "0" + minFin : String.valueOf(minFin));
        seleccionarSpinner(spAmPmFin, amPmFinStr);
    }

    // --- HELPERS ---
    private void configurarListenerFechas(Spinner spMes, Spinner spAno, Spinner spDiaDestino) {
        // Listener para actualizar los días del mes al cambiar mes/año
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            // Cuando cambia el mes o el año
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                actualizarDiasDelMes(spMes, spAno, spDiaDestino);
            }

            @Override
            // Cuando no cambia nada
            public void onNothingSelected(AdapterView<?> p) {
            }
        };

        // Configuramos los listeners
        spMes.setOnItemSelectedListener(listener);
        spAno.setOnItemSelectedListener(listener);
    }

    private void actualizarDiasDelMes(Spinner spMes, Spinner spAno, Spinner spDia) {
        int mesIndex = spMes.getSelectedItemPosition();
        int anio = Integer.parseInt(spAno.getSelectedItem().toString());
        int dias = 31;
        if (mesIndex == 3 || mesIndex == 5 || mesIndex == 8 || mesIndex == 10) dias = 30;
        else if (mesIndex == 1)
            dias = ((anio % 4 == 0 && anio % 100 != 0) || (anio % 400 == 0)) ? 29 : 28;

        String[] diasArr = generarNumeros(dias);
        int prev = spDia.getSelectedItemPosition();
        setupSpinner(spDia, diasArr);
        spDia.setSelection(Math.min(prev, diasArr.length - 1));
    }

    private String[] generarNumeros(int f) {
        List<String> l = new ArrayList<>();
        for (int k = 1; k <= f; k++) l.add(k < 10 ? "0" + k : String.valueOf(k));
        return l.toArray(new String[0]);
    }

    // Método genérico para configurar un Spinner
    private void setupSpinner(Spinner s, String[] d) {
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, d);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(a);
    }

    @SuppressWarnings("unchecked")
    // Método genérico para seleccionar un valor en un Spinner
    private void seleccionarSpinner(Spinner s, String v) {
        // Obtiene el adaptador
        android.widget.SpinnerAdapter adapter = s.getAdapter();

        // Verifica si es instancia de ArrayAdapter antes de castear
        if (adapter instanceof ArrayAdapter) {
            ArrayAdapter<String> a = (ArrayAdapter<String>) adapter;

            // Busca la posición del valor en el adaptador
            for (int i = 0; i < a.getCount(); i++) {
                String item = a.getItem(i);
                if (item != null && item.equals(v)) {
                    s.setSelection(i);
                    break;
                }
            }
        }
    }
}