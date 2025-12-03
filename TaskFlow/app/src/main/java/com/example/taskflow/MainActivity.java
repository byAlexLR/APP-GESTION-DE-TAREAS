package com.example.taskflow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
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

    // Listas y Adaptadores
    private RecyclerView rvTareasHoy;
    private TareaAdapter adapterHoy;
    private List<Tarea> listaHoy;

    private LinearLayout layoutManana;
    private RecyclerView rvTareasManana;
    private TareaAdapter adapterManana;
    private List<Tarea> listaManana;

    // --- LANZADOR QUE RECIBE LA RESPUESTA ---
    ActivityResultLauncher<Intent> launcherCrearTarea = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String titulo = data.getStringExtra("TITULO_NUEVO");
                            String fechaTexto = data.getStringExtra("FECHA_NUEVA");

                            // Recibimos la fecha exacta en números
                            int diaTarea = data.getIntExtra("DIA_EXACTO", 0);
                            int mesTarea = data.getIntExtra("MES_EXACTO", 0);
                            int anoTarea = data.getIntExtra("ANO_EXACTO", 0);

                            // Consultamos qué día es HOY en el sistema
                            Calendar hoy = Calendar.getInstance();
                            int diaHoy = hoy.get(Calendar.DAY_OF_MONTH);
                            int mesHoy = hoy.get(Calendar.MONTH);
                            int anoHoy = hoy.get(Calendar.YEAR);

                            // Comparamos
                            boolean esParaHoy = (diaTarea == diaHoy) && (mesTarea == mesHoy) && (anoTarea == anoHoy);

                            if (esParaHoy) {
                                // AÑADIR A LA LISTA DE HOY
                                listaHoy.add(new Tarea(titulo, fechaTexto));
                                adapterHoy.notifyDataSetChanged();
                            } else {
                                // AÑADIR A LA OTRA LISTA (FUTURO)
                                listaManana.add(new Tarea(titulo, fechaTexto));
                                adapterManana.notifyDataSetChanged();
                                layoutManana.setVisibility(View.VISIBLE);

                                // Cambiar el título "TAREAS PARA MAÑANA" por "TAREAS PARA EL [FECHA]"
                                TextView tvTituloAbajo = (TextView) layoutManana.getChildAt(0);
                                String nuevoTitulo = "TAREAS PARA EL " + diaTarea + "/" + (mesTarea + 1) + "/" + anoTarea;
                                tvTituloAbajo.setText(nuevoTitulo);
                            }
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Configurar Lista HOY
        rvTareasHoy = findViewById(R.id.rvTareasHoy);
        rvTareasHoy.setLayoutManager(new LinearLayoutManager(this));
        listaHoy = new ArrayList<>();
        // Datos de ejemplo
        listaHoy.add(new Tarea("Bombardear la ULPGC", "27/11/2025 · 11:00 am - 12:00 pm"));
        listaHoy.add(new Tarea("Ir al Supermercado", "27/11/2025 · 1:00 pm - 2:00 pm"));
        adapterHoy = new TareaAdapter(listaHoy);
        rvTareasHoy.setAdapter(adapterHoy);

        // 2. Configurar Lista FUTURO (Oculta al inicio)
        layoutManana = findViewById(R.id.layoutManana);
        rvTareasManana = findViewById(R.id.rvTareasManana);
        rvTareasManana.setLayoutManager(new LinearLayoutManager(this));
        listaManana = new ArrayList<>();
        adapterManana = new TareaAdapter(listaManana);
        rvTareasManana.setAdapter(adapterManana);

        // 3. Configurar Botones
        Button btnAnadir = findViewById(R.id.btnAnadir);
        FloatingActionButton fabExpand = findViewById(R.id.fabExpand);

        // Botón "AÑADIR" -> Abre la pantalla de crear
        btnAnadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CrearTareaActivity.class);
                launcherCrearTarea.launch(intent);
            }
        });

        // Botón "+" FLOTANTE -> Expande/Contrae la lista de abajo
        if (fabExpand != null) {
            fabExpand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (layoutManana.getVisibility() == View.GONE) {
                        layoutManana.setVisibility(View.VISIBLE);
                    } else {
                        layoutManana.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}