package com.example.taskflow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
//import androidx.activity.result.ActivityResult;
//import androidx.activity.result.ActivityResultCallback;
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

    private TareaAdapter adapterHoy;
    private List<Tarea> listaHoy;

    // Lista de FUTURO
    private RecyclerView rvTareasManana;
    private TareaAdapter adapterManana;
    private List<Tarea> listaManana;
    private TextView tvTituloManana; // Para poner "TAREAS PARA LA [FECHA]"

    ActivityResultLauncher<Intent> launcherCrearTarea = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    cargarListasSeparadas(); // Recargar tras crear/editar
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. VINCULAR VISTAS Y ADAPTADORES
        // Lista de HOY
        RecyclerView rvTareasHoy = findViewById(R.id.rvTareasHoy);
        rvTareasHoy.setLayoutManager(new LinearLayoutManager(this));
        listaHoy = new ArrayList<>();
        adapterHoy = new TareaAdapter(listaHoy, crearListener());
        rvTareasHoy.setAdapter(adapterHoy);

        rvTareasManana = findViewById(R.id.rvTareasManana);
        rvTareasManana.setLayoutManager(new LinearLayoutManager(this));
        listaManana = new ArrayList<>();
        adapterManana = new TareaAdapter(listaManana, crearListener());
        rvTareasManana.setAdapter(adapterManana);

        tvTituloManana = findViewById(R.id.tvTituloManana);

        // 2. DATOS INICIALES (Para que veas las dos secciones al inicio)
        if (Repositorio.tareasGlobales.isEmpty()) {
            Calendar hoy = Calendar.getInstance();
            int d = hoy.get(Calendar.DAY_OF_MONTH);
            int m = hoy.get(Calendar.MONTH);
            int a = hoy.get(Calendar.YEAR);

            // Array de meses auxiliar para que coincida con CrearTareaActivity
            String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

            // Formateamos el mes y día para que se vean bonitos (ej: "05 Ene")
            String nombreMes = meses[m];

            // Tarea para HOY (Formato IDÉNTICO a CrearTareaActivity)
            String fechaHoyStr = d + " " + nombreMes + " " + a + " · 11:00 AM - 12:00 PM";

            Repositorio.tareasGlobales.add(new Tarea(
                    "Bombardear la ULPGC",
                    fechaHoyStr, // Usamos el string formateado
                    "...", "Las Palmas",
                    d, m, a,
                    11, 0, "AM", 12, 0, "PM", "30", "Min"
            ));

            String fechaMananaStr = (d+1) + " " + nombreMes + " " + a + " · 01:00 PM - 02:00 PM";

            Repositorio.tareasGlobales.add(new Tarea(
                    "Ir al Supermercado",
                    fechaMananaStr,
                    "...", "Mercadona",
                    d+1, m, a,
                    13, 0, "PM", 14, 0, "PM", "1", "Hora"
            ));
        }

        cargarListasSeparadas();

        // 3. BOTONES
        Button btnAnadir = findViewById(R.id.btnAnadir);
        btnAnadir.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CrearTareaActivity.class);
            intent.putExtra("POSICION_ORIGINAL", -1);
            launcherCrearTarea.launch(intent);
        });

        View btnCal = findViewById(R.id.btnCalendario);
        if(btnCal != null) btnCal.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CalendarioActivity.class)));

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
    protected void onResume() {
        super.onResume();
        cargarListasSeparadas(); // Asegura que la lista está actualizada al volver
    }

    // --- LÓGICA DE SEPARACIÓN INTELIGENTE ---
    private void cargarListasSeparadas() {
        listaHoy.clear();
        listaManana.clear();

        Calendar cal = Calendar.getInstance();
        int diaHoy = cal.get(Calendar.DAY_OF_MONTH);
        int mesHoy = cal.get(Calendar.MONTH);
        int anioHoy = cal.get(Calendar.YEAR);

        Tarea primeraTareaFutura = null;

        for (Tarea t : Repositorio.tareasGlobales) {
            // ¿Es hoy?
            if (t.getDia() == diaHoy && t.getMes() == mesHoy && t.getAnio() == anioHoy) {
                listaHoy.add(t);
            } else {
                // Si no es hoy, va a la lista de abajo (Futuro)
                listaManana.add(t);
                if (primeraTareaFutura == null) primeraTareaFutura = t;
            }
        }

        // Ordenar la lista de futuro para que la fecha más próxima salga primero
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            listaManana.sort((t1, t2) -> {
                if (t1.getAnio() != t2.getAnio()) return t1.getAnio() - t2.getAnio();
                if (t1.getMes() != t2.getMes()) return t1.getMes() - t2.getMes();
                return t1.getDia() - t2.getDia();
            });
            if (!listaManana.isEmpty()) primeraTareaFutura = listaManana.get(0);
        }

        // Detecta si hay tareas de diferentes días mezcladas
        boolean variosDiasDistintos = false;
        if (!listaManana.isEmpty()) {
            Tarea primera = listaManana.get(0);
            for (Tarea t : listaManana) {
                // Si alguna tarea no coincide en día o mes con la primera, son fechas mixtas
                if (t.getDia() != primera.getDia() || t.getMes() != primera.getMes()) {
                    variosDiasDistintos = true;
                    break;
                }
            }
        }

        // Actualiza el título de la sección de futuro
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

        adapterHoy.notifyDataSetChanged();
        adapterManana.notifyDataSetChanged();
    }

    // --- LISTENER (Para no repetir código) ---
    private TareaAdapter.OnItemClickListener crearListener() {
        return new TareaAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Tarea t) {
                int index = Repositorio.tareasGlobales.indexOf(t);
                Intent intent = new Intent(MainActivity.this, CrearTareaActivity.class);
                intent.putExtra("TAREA_A_EDITAR", t);
                intent.putExtra("POSICION_ORIGINAL", index);
                launcherCrearTarea.launch(intent);
            }

            @Override
            public void onDeleteClick(Tarea t) {
                Repositorio.tareasGlobales.remove(t);
                cargarListasSeparadas();
                Toast.makeText(MainActivity.this, "Tarea eliminada", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDuplicateClick(Tarea t) {
                // Creamos copia
                Tarea copia = new Tarea(
                        t.getTitulo() + " (Copia)", t.getFechaHora(), t.getDescripcion(), t.getUbicacion(),
                        t.getDia(), t.getMes(), t.getAnio(),
                        t.getHoraInicio(), t.getMinInicio(), t.getAmPmInicio(),
                        t.getHoraFin(), t.getMinFin(), t.getAmPmFin(),
                        t.getNotifCantidad(), t.getNotifUnidad()
                );
                Repositorio.tareasGlobales.add(copia);
                cargarListasSeparadas();
                Toast.makeText(MainActivity.this, "Tarea duplicada", Toast.LENGTH_SHORT).show();
            }
        };
    }
}