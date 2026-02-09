package com.example.taskflow;

// Importa las líbrerias necesarias
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

// Clase MainActivity que hereda de AppCompatActivity para la actividad principal
public class MainActivity extends AppCompatActivity {

    // Variables de la lista actual
    private TareaAdapter adapterHoy, adapterManana, adapterCompletadas;
    private List<Tarea> listaHoy, listaManana, listaCompletadas;
    private TextView tvTituloManana, tvTituloCompletadas;

    // Receptor de broadcast para nuevas tareas vía Bluetooth
    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        // Método que se ejecuta cuando se recibe un intent (tarea por Bluetooth)
        public void onReceive(Context context, Intent intent) {
            // Si es una tarea por Bluetooth
            if (BluetoothReceiver.ACTION_NUEVA_TAREA.equals(intent.getAction())) {
                // Almacena la tarea
                Tarea tarea = (Tarea) intent.getSerializableExtra("TAREA_RECIBIDA");
                // Muestra el diálogo de confirmación
                if (tarea != null) {
                    mostrarDialogoAceptarTarea(tarea);
                }
            }
        }
    };

    // Lanzador de actividad para crear o editar tareas
    private final ActivityResultLauncher<Intent> launcherCrearTarea = registerForActivityResult(
            // Actividad para crear o editar
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Si el resultado es OK, vuelve a cargar las listas
                if (result.getResultCode() == Activity.RESULT_OK) {
                    cargarListasSeparadas();
                }
            }
    );

    @Override
    // 
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configura la vista de la actividad
        setContentView(R.layout.activity_main);

        // Configura las vistas y adaptadores
        vincularVistasYAdaptadores();
        cargarDatosPruebaSiVacio();
        configurarListeners();
        cargarListasSeparadas();
    }

    // Método para vincular vistas y adaptadores
    private void vincularVistasYAdaptadores() {
        // Configura el RecyclerView para las tareas de hoy
        RecyclerView rvTareasHoy = findViewById(R.id.rvTareasHoy);
        rvTareasHoy.setLayoutManager(new LinearLayoutManager(this));
        listaHoy = new ArrayList<>();
        adapterHoy = new TareaAdapter(listaHoy, crearListener());
        rvTareasHoy.setAdapter(adapterHoy);

        // Configura el RecyclerView para las tareas de mañana
        RecyclerView rvTareasManana = findViewById(R.id.rvTareasManana);
        rvTareasManana.setLayoutManager(new LinearLayoutManager(this));
        listaManana = new ArrayList<>();
        adapterManana = new TareaAdapter(listaManana, crearListener());
        rvTareasManana.setAdapter(adapterManana);

        // Configura el RecyclerView para las tareas completadas
        RecyclerView rvTareasCompletadas = findViewById(R.id.rvTareasCompletadas);
        rvTareasCompletadas.setLayoutManager(new LinearLayoutManager(this));
        listaCompletadas = new ArrayList<>();
        adapterCompletadas = new TareaAdapter(listaCompletadas, crearListener());
        rvTareasCompletadas.setAdapter(adapterCompletadas);

        // Vincula los TextViews de los títulos
        tvTituloManana = findViewById(R.id.tvTituloManana);
        tvTituloCompletadas = findViewById(R.id.tvTituloCompletadas);
    }

    // Método para cargar datos de prueba si la lista global está vacía
    private void cargarDatosPruebaSiVacio() {
        if (Repositorio.tareasGlobales.isEmpty()) {
            // Calcula la fecha actual
            Calendar hoy = Calendar.getInstance();
            int d = hoy.get(Calendar.DAY_OF_MONTH);
            int m = hoy.get(Calendar.MONTH);
            int a = hoy.get(Calendar.YEAR);

            // Array de meses auxiliar
            String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
           
            // Formatea el mes y día
            String nombreMes = meses[m];

            // Tarea 1: Hoy con Multimedia Completa para probar el Visualizador
            Tarea tarea1 = new Tarea(
                    "Prueba de Multimedia",
                    d + " " + nombreMes + " " + a + " · 09:00 AM - 10:00 AM",
                    "Esta tarea contiene todos los tipos de archivos para probar el nuevo visualizador.", "Laboratorio de Pruebas",
                    d, m, a,
                    9, 0, "AM", 10, 0, "AM", "5", "Minutos antes"
            );
            tarea1.setImagenUri("android.resource://" + getPackageName() + "/" + R.drawable.ic_taskflow_logo_background);
            tarea1.setVideoUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
            tarea1.setAudioUri("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3");
            tarea1.setColaboradores(new ArrayList<>(Arrays.asList("Tester Principal", "QA Engine")));
            Repositorio.tareasGlobales.add(tarea1);

            // Tarea 2: Hoy con Colaboradores
            Tarea tarea2 = new Tarea(
                    "Reunión de equipo TaskFlow",
                    d + " " + nombreMes + " " + a + " · 11:00 AM - 12:30 PM",
                    "Discutir avances de multimedia y nuevas funcionalidades.", "Oficina Central",
                    d, m, a,
                    11, 0, "AM", 12, 30, "PM", "15", "Minutos antes"
            );
            tarea2.setImagenUri("android.resource://" + getPackageName() + "/" + R.drawable.ic_taskflow_logo_background);
            tarea2.setColaboradores(new ArrayList<>(Arrays.asList("Juan Pérez", "María García")));
            Repositorio.tareasGlobales.add(tarea2);

            // Tarea para MAÑANA
            Calendar tomorrow = (Calendar) hoy.clone();
            tomorrow.add(Calendar.DAY_OF_MONTH, 1);
            int dT = tomorrow.get(Calendar.DAY_OF_MONTH);
            int mT = tomorrow.get(Calendar.MONTH);
            int aT = tomorrow.get(Calendar.YEAR);

            // Tarea 3: Mañana con Video
            Tarea tarea3 = new Tarea(
                    "Diseño de interfaz móvil",
                    dT + " " + meses[mT] + " " + aT + " · 04:00 PM - 06:00 PM",
                    "Finalizar prototipos de alta fidelidad.", "Estudio Creativo",
                    dT, mT, aT,
                    4, 0, "PM", 6, 0, "PM", "1", "Horas antes"
            );
            tarea3.setVideoUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4");
            Repositorio.tareasGlobales.add(tarea3);
        }
    }

    // Método para configurar los listeners de los botones
    private void configurarListeners() {
        // Botón para añadir tarea
        Button btnAnadir = findViewById(R.id.btnAnadir);
        // Listener para el botón de añadir tarea
        btnAnadir.setOnClickListener(v -> {
            // Se inicia la actividad para crear una nueva tarea
            Intent intent = new Intent(MainActivity.this, CrearTareaActivity.class);
            intent.putExtra("POSICION_ORIGINAL", -1);
            launcherCrearTarea.launch(intent);
        });

        // Botón para abrir el calendario
        View btnCal = findViewById(R.id.btnCalendario);
        // Listener para el botón del calendario
        if (btnCal != null) btnCal.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CalendarioActivity.class)));

        // Botón para expandir/colapsar las tareas de mañana
        FloatingActionButton fab = findViewById(R.id.fabExpand);
        // Listener para el botón flotante
        if (fab != null) {
            fab.setOnClickListener(v -> {
                // Alterna la visibilidad del RecyclerView y el título de las tareas de mañana
                RecyclerView rvManana = findViewById(R.id.rvTareasManana);
                if (rvManana.getVisibility() == View.VISIBLE) {
                    rvManana.setVisibility(View.GONE);
                    tvTituloManana.setVisibility(View.GONE);
                } else {
                    rvManana.setVisibility(View.VISIBLE);
                    tvTituloManana.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    // Método para mostrar el diálogo de aceptar tarea recibida por Bluetooth
    private void mostrarDialogoAceptarTarea(Tarea tarea) {
        // Infla el diseño del diálogo personalizado
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_aceptar_tarea, null);

        // Crea el AlertDialog con el diseño personalizado
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        // Configura el fondo transparente del diálogo
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Vincula las vistas del diálogo
        TextView tvNombre = view.findViewById(R.id.tvNombreTareaRecibida);
        TextView tvDetalles = view.findViewById(R.id.tvDetallesTareaRecibida);
        View btnAceptar = view.findViewById(R.id.btnAceptarTarea);
        TextView btnRechazar = view.findViewById(R.id.btnRechazarTarea);

        // Configura los datos de la tarea en el diálogo
        tvNombre.setText(tarea.getTitulo());
        String detalles = tarea.getFechaHora() + "\n" + tarea.getUbicacion();
        tvDetalles.setText(detalles);

        // Configura los listeners de los botones del diálogo
        btnAceptar.setOnClickListener(v -> {
            // Añade la tarea a la lista global y recarga las listas
            Repositorio.tareasGlobales.add(tarea);
            cargarListasSeparadas();
            Toast.makeText(this, "Tarea añadida correctamente", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // Listener para rechazar la tarea
        btnRechazar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    // Método para volver a cargar las listas separadas tras volver a la actividad
    protected void onResume() {
        super.onResume();
        // Preparar el receptor
        IntentFilter filter = new IntentFilter(BluetoothReceiver.ACTION_NUEVA_TAREA);

        // Registrar el receptor
        ContextCompat.registerReceiver(this, btReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        // Volver a cargar
        cargarListasSeparadas();
    }

    // Método para detener el receptor tras salir de la actividad
    @Override
    protected void onPause() {
        // Llama al método padre
        super.onPause();
        // Desregistrar el receptor
        unregisterReceiver(btReceiver);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    // Método para cargar las listas separadas de tareas
    private void cargarListasSeparadas() {
        // Si no hay tareas, no hace nada
        if (Repositorio.tareasGlobales == null) return;

        // Limpia las listas
        listaHoy.clear();
        listaManana.clear();
        listaCompletadas.clear();

        // Recorre las tareas y las separa según su fecha
        Calendar cal = Calendar.getInstance();
        int diaHoy = cal.get(Calendar.DAY_OF_MONTH);
        int mesHoy = cal.get(Calendar.MONTH);
        int anioHoy = cal.get(Calendar.YEAR);

        // Recorre las tareas y las separa según su fecha
        for (Tarea t : Repositorio.tareasGlobales) {
            // Comprueba si está completada
            if (t.isCompletada()) {
                // Añade a la lista de completadas
                listaCompletadas.add(t);
                continue;
            }

            // Comprueba si es para hoy o para mañana y las añade a la lista correspondiente
            if (t.getDia() == diaHoy && t.getMes() == mesHoy && t.getAnio() == anioHoy) {
                listaHoy.add(t);
            } else {
                listaManana.add(t);
            }
        }

        // Ordena las listas por fecha
        listaManana.sort((t1, t2) -> {
            // Ordena por año, mes y día
            if (t1.getAnio() != t2.getAnio()) return t1.getAnio() - t2.getAnio();
            if (t1.getMes() != t2.getMes()) return t1.getMes() - t2.getMes();
            return t1.getDia() - t2.getDia();
        });

        // Actualiza los títulos según las tareas presentes
        if (!listaManana.isEmpty()) {
            // Comprueba si hay varias fechas distintas
            Tarea primera = listaManana.get(0);
            boolean variosDiasDistintos = false;
            // Recorre las tareas para comprobar si hay varias fechas distintas
            for (Tarea t : listaManana) {
                // Si encuentra una fecha distinta, marca la variable y sale del bucle
                if (t.getDia() != primera.getDia() || t.getMes() != primera.getMes()) {
                    variosDiasDistintos = true;
                    break;
                }
            }

            // Actualiza el título según si hay varias fechas distintas o no
            if (variosDiasDistintos) {
                tvTituloManana.setText("PRÓXIMAS TAREAS (" + listaManana.size() + ")");
            } else {
                // Formatea el mes
                String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
                String mesStr = (primera.getMes() >= 0 && primera.getMes() < 12) ? meses[primera.getMes()] : "?";
                tvTituloManana.setText("TAREAS PARA EL " + primera.getDia() + " " + mesStr.toUpperCase());
            }
        } else {
            tvTituloManana.setText("PRÓXIMAS TAREAS");
        }

        // Controla la visibilidad de la sección de completadas
        int visibility = listaCompletadas.isEmpty() ? View.GONE : View.VISIBLE;
        tvTituloCompletadas.setVisibility(visibility);
        RecyclerView rvCompletadas = findViewById(R.id.rvTareasCompletadas);
        if (rvCompletadas != null) rvCompletadas.setVisibility(visibility);

        // Actualiza los adaptadores
        adapterHoy.notifyDataSetChanged();
        adapterManana.notifyDataSetChanged();
        adapterCompletadas.notifyDataSetChanged();
    }

    // Método para crear el listener del adaptador de tareas
    private TareaAdapter.OnItemClickListener crearListener() {
        // Devuelve una nueva instancia del listener
        return new TareaAdapter.OnItemClickListener() {
            @Override
            // Método para editar una tarea
            public void onEditClick(Tarea t) {
                // Obtiene la posición original de la tarea en la lista global
                int index = Repositorio.tareasGlobales.indexOf(t);
                Intent intent = new Intent(MainActivity.this, CrearTareaActivity.class);
                intent.putExtra("TAREA_A_EDITAR", t);
                intent.putExtra("POSICION_ORIGINAL", index);
                launcherCrearTarea.launch(intent);
            }

            @Override
            // Método para eliminar una tarea
            public void onDeleteClick(Tarea t) {
                // Elimina la tarea de la lista global y recarga las listas
                Repositorio.tareasGlobales.remove(t);
                cargarListasSeparadas();
                Toast.makeText(MainActivity.this, "Tarea eliminada", Toast.LENGTH_SHORT).show();
            }

            @Override
            // Método para duplicar una tarea
            public void onDuplicateClick(Tarea t) {
                // Crea una copia de la tarea y la añade a la lista global
                Tarea copia = new Tarea(
                        t.getTitulo() + " (Copia)", t.getFechaHora(), t.getDescripcion(), t.getUbicacion(),
                        t.getDia(), t.getMes(), t.getAnio(),
                        t.getHoraInicio(), t.getMinInicio(), t.getAmPmInicio(),
                        t.getHoraFin(), t.getMinFin(), t.getAmPmFin(),
                        t.getNotifCantidad(), t.getNotifUnidad()
                );
                copia.setCompletada(false);
                copia.setImagenUri(t.getImagenUri());
                copia.setVideoUri(t.getVideoUri());
                copia.setAudioUri(t.getAudioUri());
                copia.setColaboradores(new ArrayList<>(t.getColaboradores()));
                Repositorio.tareasGlobales.add(copia);
                cargarListasSeparadas();
                Toast.makeText(MainActivity.this, "Tarea duplicada", Toast.LENGTH_SHORT).show();
            }

            @Override
            // Método para completar o reactivar una tarea
            public void onCompleteClick(Tarea t) {
                // Cambia el estado de completada y recarga las listas
                t.setCompletada(!t.isCompletada());
                cargarListasSeparadas();
                // Muestra un mensaje según el nuevo estado
                String mensaje = t.isCompletada() ? "Tarea completada" : "Tarea reactivada";
                Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_SHORT).show();
            }
        };
    }
}
