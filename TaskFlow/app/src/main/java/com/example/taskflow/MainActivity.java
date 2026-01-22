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
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// Clase MainActivity que hereda de AppCompatActivity para la actividad principal
public class MainActivity extends AppCompatActivity {

    // Variables de la lista actual
    private TareaAdapter adapterHoy;
    private List<Tarea> listaHoy;

    // Lista de tareas FUTURAS
    private RecyclerView rvTareasManana;
    private TareaAdapter adapterManana;
    private List<Tarea> listaManana;
    private TextView tvTituloManana;

    // Lista de tareas COMPLETADAS
    private RecyclerView rvTareasCompletadas;
    private TareaAdapter adapterCompletadas;
    private List<Tarea> listaCompletadas;
    private TextView tvTituloCompletadas;

    // Launcher para crear o editar
    private final ActivityResultLauncher<Intent> launcherCrearTarea = registerForActivityResult(
            // Actividad para crear o editar
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Si el resultado es OK, vuelve a cargar las listas
                if (result.getResultCode() == Activity.RESULT_OK) {
                    cargarListasSeparadas();
                }
            });

    // Receptor para tareas por Bluetooth
    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        // Método que se ejecuta cuando se recibe un intent (tarea por Bluetooth)
        @Override
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

    // Método principal de la actividad
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configura la vista
        setContentView(R.layout.activity_main);

        // Si no hay tareas, crea una lista vacía
        if (Repositorio.tareasGlobales == null) {
            Repositorio.tareasGlobales = new ArrayList<>();
        }

        // Inicia el servicio de Bluetooth
        Intent serviceIntent = new Intent(this, BluetoothReceiver.class);
        startService(serviceIntent);

        // Configura las vistas
        inicializarVistas();
        cargarDatosPruebaSiVacio();
        cargarListasSeparadas();
        configurarListeners();
    }

    // Método para inicializar las vistas
    private void inicializarVistas() {
        // Lista de tareas Hoy
        RecyclerView rvTareasHoy = findViewById(R.id.rvTareasHoy);
        rvTareasHoy.setLayoutManager(new LinearLayoutManager(this));
        listaHoy = new ArrayList<>();
        adapterHoy = new TareaAdapter(listaHoy, crearListener());
        rvTareasHoy.setAdapter(adapterHoy);

        // Lista de tareas Futuras
        rvTareasManana = findViewById(R.id.rvTareasManana);
        rvTareasManana.setLayoutManager(new LinearLayoutManager(this));
        listaManana = new ArrayList<>();
        adapterManana = new TareaAdapter(listaManana, crearListener());
        rvTareasManana.setAdapter(adapterManana);

        // Lista de tareas Completadas
        rvTareasCompletadas = findViewById(R.id.rvTareasCompletadas);
        rvTareasCompletadas.setLayoutManager(new LinearLayoutManager(this));
        listaCompletadas = new ArrayList<>();
        adapterCompletadas = new TareaAdapter(listaCompletadas, crearListener());
        rvTareasCompletadas.setAdapter(adapterCompletadas);

        // Títulos
        tvTituloManana = findViewById(R.id.tvTituloManana);
        tvTituloCompletadas = findViewById(R.id.tvTituloCompletadas);
    }

    // Método para cargar datos de prueba si la lista está vacía
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

            // Tarea para HOY
            Repositorio.tareasGlobales.add(new Tarea(
                    "Bombardear la ULPGC",
                    d + " " + nombreMes + " " + a + " · 11:00 AM - 12:00 PM",
                    "...", "Las Palmas",
                    d, m, a,
                    11, 0, "AM", 12, 0, "PM", "30", "Min"
            ));

            // Tarea para MAÑANA
            Calendar tomorrow = (Calendar) hoy.clone();
            tomorrow.add(Calendar.DAY_OF_MONTH, 1);
            int dT = tomorrow.get(Calendar.DAY_OF_MONTH);
            int mT = tomorrow.get(Calendar.MONTH);
            int aT = tomorrow.get(Calendar.YEAR);

            Repositorio.tareasGlobales.add(new Tarea(
                    "Ir al Supermercado",
                    dT + " " + meses[mT] + " " + aT + " · 01:00 PM - 02:00 PM",
                    "...", "Mercadona",
                    dT, mT, aT,
                    1, 0, "PM", 2, 0, "PM", "1", "Hora"
            ));
        }
    }

    // Método para configurar los listeners
    private void configurarListeners() {
        // Botón para añadir tarea
        Button btnAnadir = findViewById(R.id.btnAnadir);
        btnAnadir.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CrearTareaActivity.class);
            intent.putExtra("POSICION_ORIGINAL", -1);
            launcherCrearTarea.launch(intent);
        });

        // Botón para calendario
        View btnCal = findViewById(R.id.btnCalendario);
        if (btnCal != null) btnCal.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CalendarioActivity.class)));

        // Botón para expandir/reducir lista de tareas futuras
        FloatingActionButton fab = findViewById(R.id.fabExpand);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                if (rvTareasManana.getVisibility() == View.VISIBLE) {
                    rvTareasManana.setVisibility(View.GONE);
                    tvTituloManana.setVisibility(View.GONE);
                } else {
                    rvTareasManana.setVisibility(View.VISIBLE);
                    tvTituloManana.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    // Método para mostrar el diálogo de confirmación
    private void mostrarDialogoAceptarTarea(Tarea tarea) {
        // Inflar el nuevo layout personalizado
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_aceptar_tarea, null);

        // Crear el diálogo con estilo Material transparente
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        // Configurar el diálogo
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Referenciar elementos del layout
        TextView tvNombre = view.findViewById(R.id.tvNombreTareaRecibida);
        TextView tvDetalles = view.findViewById(R.id.tvDetallesTareaRecibida);
        View btnAceptar = view.findViewById(R.id.btnAceptarTarea);
        TextView btnRechazar = view.findViewById(R.id.btnRechazarTarea);

        // Cargar datos de la tarea
        tvNombre.setText(tarea.getTitulo());
        String detalles = tarea.getFechaHora() + "\n" + tarea.getUbicacion();
        tvDetalles.setText(detalles);

        // Configurar listeners
        btnAceptar.setOnClickListener(v -> {
            Repositorio.tareasGlobales.add(tarea);
            cargarListasSeparadas();
            Toast.makeText(this, "Tarea añadida correctamente", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // Botón de rechazar
        btnRechazar.setOnClickListener(v -> dialog.dismiss());

        // Muestra el diálogo
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
        super.onPause();
        unregisterReceiver(btReceiver);
    }

    // --- LÓGICA DE SEPARACIÓN INTELIGENTE ---
    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    // Método para cargar las listas separadas
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

        // Busca la primera tarea futura
        Tarea primeraTareaFutura = null;

        // Recorre las tareas y las separa según su fecha
        for (Tarea t : Repositorio.tareasGlobales) {
            // Comprueba si está completada
            if (t.isCompletada()) {
                listaCompletadas.add(t);
                continue;
            }

            // Si es hoy, la añade a la lista de hoy
            if (t.getDia() == diaHoy && t.getMes() == mesHoy && t.getAnio() == anioHoy) {
                listaHoy.add(t);
            } else {
                // Si no es hoy, la añade a la lista de tareas futuras
                listaManana.add(t);
                if (primeraTareaFutura == null) primeraTareaFutura = t;
            }
        }

        // Ordena la lista de futuro para que la fecha más próxima salga primero
        listaManana.sort((t1, t2) -> {
            if (t1.getAnio() != t2.getAnio()) return t1.getAnio() - t2.getAnio();
            if (t1.getMes() != t2.getMes()) return t1.getMes() - t2.getMes();
            return t1.getDia() - t2.getDia();
        });

        if (!listaManana.isEmpty()) primeraTareaFutura = listaManana.get(0);

        // Detecta si hay tareas de diferentes días mezcladas
        boolean variosDiasDistintos = false;
        if (!listaManana.isEmpty()) {
            Tarea primera = listaManana.get(0);
            for (Tarea t : listaManana) {
                // Si la fecha cambia, hay varios días distintos
                if (t.getDia() != primera.getDia() || t.getMes() != primera.getMes()) {
                    variosDiasDistintos = true;
                    break;
                }
            }
        }

        // Actualiza el título de la sección de tareas futuras
        if (!listaManana.isEmpty() && primeraTareaFutura != null) {
            if (variosDiasDistintos) {
                // Título Genérico
                tvTituloManana.setText("PRÓXIMAS TAREAS (" + listaManana.size() + ")");
            } else {
                // Título con Fecha Específica
                String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
                String mesStr = (primeraTareaFutura.getMes() >= 0 && primeraTareaFutura.getMes() < 12) ? meses[primeraTareaFutura.getMes()] : "?";
                tvTituloManana.setText("TAREAS PARA EL " + primeraTareaFutura.getDia() + " " + mesStr.toUpperCase());
            }
        } else {
            // Lista vacía
            tvTituloManana.setText("PRÓXIMAS TAREAS");
        }

        // Controla la visibilidad de la sección de completadas
        int visibility = listaCompletadas.isEmpty() ? View.GONE : View.VISIBLE;
        tvTituloCompletadas.setVisibility(visibility);
        rvTareasCompletadas.setVisibility(visibility);

        // Actualiza los adaptadores
        adapterHoy.notifyDataSetChanged();
        adapterManana.notifyDataSetChanged();
        adapterCompletadas.notifyDataSetChanged();
    }

    // --- LISTENER (Para no repetir código) ---
    private TareaAdapter.OnItemClickListener crearListener() {
        // Listener para cada tarea
        return new TareaAdapter.OnItemClickListener() {
            @Override
            // Botón de editar
            public void onEditClick(Tarea t) {
                int index = Repositorio.tareasGlobales.indexOf(t);
                Intent intent = new Intent(MainActivity.this, CrearTareaActivity.class);
                intent.putExtra("TAREA_A_EDITAR", t);
                intent.putExtra("POSICION_ORIGINAL", index);
                launcherCrearTarea.launch(intent);
            }

            @Override
            // Botón de borrar
            public void onDeleteClick(Tarea t) {
                Repositorio.tareasGlobales.remove(t);
                cargarListasSeparadas();
                Toast.makeText(MainActivity.this, "Tarea eliminada", Toast.LENGTH_SHORT).show();
            }

            @Override
            // Botón de duplicar
            public void onDuplicateClick(Tarea t) {
                // Crea una nueva tarea con los mismos datos, añadiendo (Copia) al título
                Tarea copia = new Tarea(
                        t.getTitulo() + " (Copia)", t.getFechaHora(), t.getDescripcion(), t.getUbicacion(),
                        t.getDia(), t.getMes(), t.getAnio(),
                        t.getHoraInicio(), t.getMinInicio(), t.getAmPmInicio(),
                        t.getHoraFin(), t.getMinFin(), t.getAmPmFin(),
                        t.getNotifCantidad(), t.getNotifUnidad()
                );
                // Copia el estado
                copia.setCompletada(false);
                // Añade la nueva tarea a la lista
                Repositorio.tareasGlobales.add(copia);
                cargarListasSeparadas();
                Toast.makeText(MainActivity.this, "Tarea duplicada", Toast.LENGTH_SHORT).show();
            }

            @Override
            // Botón de completar
            public void onCompleteClick(Tarea t) {
                // Cambia el estado de la tarea
                t.setCompletada(!t.isCompletada());
                cargarListasSeparadas();
                // Muestra un mensaje de confirmación
                String mensaje = t.isCompletada() ? "Tarea completada" : "Tarea reactivada";
                Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_SHORT).show();
            }
        };
    }
}
