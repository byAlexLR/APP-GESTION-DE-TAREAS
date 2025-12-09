package com.example.taskflow;

// Importación de librerías necesarias
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

    // Launcher para crear/editar
    ActivityResultLauncher<Intent> launcherCrearTarea = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    cargarListasSeparadas(); // Recargar tras crear/editar
                }
            });

    @Override
    // Método principal de la actividad
    protected void onCreate(Bundle savedInstanceState) {
        // Llamada al método onCreate de la superclase
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Lista de Hoy, con su adapter y layout
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

        // Título de los apartados de la lista de tareas
        tvTituloManana = findViewById(R.id.tvTituloManana);
        tvTituloCompletadas = findViewById(R.id.tvTituloCompletadas);

        // Datos iniciales de la lista de tareas
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
            String fechaHoyStr = d + " " + nombreMes + " " + a + " · 11:00 AM - 12:00 PM";

            // Añadir tarea a la lista (Tarea Ejemplo)
            Repositorio.tareasGlobales.add(new Tarea(
                    "Bombardear la ULPGC",
                    fechaHoyStr,
                    "...", "Las Palmas",
                    d, m, a,
                    11, 0, "AM", 12, 0, "PM", "30", "Min"
            ));

            // Tarea para MANANA
            String fechaMananaStr = (d+1) + " " + nombreMes + " " + a + " · 01:00 PM - 02:00 PM";

            // Añadir tarea a la lista (Tarea Ejemplo)
            Repositorio.tareasGlobales.add(new Tarea(
                    "Ir al Supermercado",
                    fechaMananaStr,
                    "...", "Mercadona",
                    d+1, m, a,
                    13, 0, "PM", 14, 0, "PM", "1", "Hora"
            ));
        }

        // Cargar listas separadas
        cargarListasSeparadas();

        // Botón para añadir tarea
        Button btnAnadir = findViewById(R.id.btnAnadir);
        btnAnadir.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CrearTareaActivity.class);
            intent.putExtra("POSICION_ORIGINAL", -1);
            launcherCrearTarea.launch(intent);
        });

        // Botón para calendario
        View btnCal = findViewById(R.id.btnCalendario);
        if(btnCal != null) btnCal.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CalendarioActivity.class)));

        // Botón para expandir/reducir lista de tareas futuras
        FloatingActionButton fab = findViewById(R.id.fabExpand);
        if(fab != null) {
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

    @Override
    // Método para volver a cargar las listas separadas tras volver a la actividad
    protected void onResume() {
        super.onResume();
        cargarListasSeparadas();
    }

    // --- LÓGICA DE SEPARACIÓN INTELIGENTE ---
    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    // Método para cargar las listas separadas
    private void cargarListasSeparadas() {
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
        if (listaCompletadas.isEmpty()) {
            tvTituloCompletadas.setVisibility(View.GONE);
            rvTareasCompletadas.setVisibility(View.GONE);
        } else {
            // Muestra la sección de completadas
            tvTituloCompletadas.setVisibility(View.VISIBLE);
            rvTareasCompletadas.setVisibility(View.VISIBLE);
        }

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