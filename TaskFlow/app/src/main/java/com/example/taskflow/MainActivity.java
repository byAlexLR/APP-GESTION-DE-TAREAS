package com.example.taskflow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvTareasHoy;
    private TareaAdapter adapterHoy;
    private List<Tarea> listaHoy;

    ActivityResultLauncher<Intent> launcherCrearTarea = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        if (data.hasExtra("TAREA_OBJETO")) {
                            Tarea tareaRecibida = (Tarea) data.getSerializableExtra("TAREA_OBJETO");
                            int posicionEditada = data.getIntExtra("POSICION_EDITADA", -1);

                            if (tareaRecibida != null) {
                                if (posicionEditada != -1 && posicionEditada < listaHoy.size()) {
                                    listaHoy.set(posicionEditada, tareaRecibida);
                                    adapterHoy.notifyItemChanged(posicionEditada);
                                } else {
                                    listaHoy.add(tareaRecibida);
                                    adapterHoy.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvTareasHoy = findViewById(R.id.rvTareasHoy);
        rvTareasHoy.setLayoutManager(new LinearLayoutManager(this));
        listaHoy = new ArrayList<>();

        // Datos de prueba
        listaHoy.add(new Tarea("Bombardear la ULPGC", "27 Ene 2025 · 11:00 AM", "Descripción...", "Las Palmas", 27, 0, 2025, 11, 0, "AM", 12, 0, "PM"));
        listaHoy.add(new Tarea("Ir al Supermercado", "27 Ene 2025 · 01:00 PM", "Leche y pan", "Mercadona", 27, 0, 2025, 1, 0, "PM", 2, 0, "PM"));

        // === CONFIGURAR EL ADAPTADOR CON LAS 3 ACCIONES ===
        adapterHoy = new TareaAdapter(listaHoy, new TareaAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                editarTarea(listaHoy.get(position), position);
            }

            @Override
            public void onDeleteClick(int position) {
                // Lógica de borrar
                listaHoy.remove(position);
                adapterHoy.notifyItemRemoved(position);
                // Ajustar rangos por si acaso se descuadran índices
                adapterHoy.notifyItemRangeChanged(position, listaHoy.size());
                Toast.makeText(MainActivity.this, "Tarea eliminada", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDuplicateClick(int position) {
                // Lógica de duplicar
                Tarea original = listaHoy.get(position);

                // Creamos una copia exacta usando todos los datos
                Tarea copia = new Tarea(
                        original.getTitulo() + " (Copia)", // Cambiamos el nombre
                        original.getFechaHora(),
                        original.getDescripcion(),
                        original.getUbicacion(),
                        original.getDia(), original.getMes(), original.getAnio(),
                        original.getHoraInicio(), original.getMinInicio(), original.getAmPmInicio(),
                        original.getHoraFin(), original.getMinFin(), original.getAmPmFin()
                );

                // Añadimos justo debajo de la original
                listaHoy.add(position + 1, copia);
                adapterHoy.notifyItemInserted(position + 1);
            }
        });

        rvTareasHoy.setAdapter(adapterHoy);

        Button btnAnadir = findViewById(R.id.btnAnadir);
        btnAnadir.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CrearTareaActivity.class);
            launcherCrearTarea.launch(intent);
        });
    }

    private void editarTarea(Tarea tarea, int posicion) {
        Intent intent = new Intent(MainActivity.this, CrearTareaActivity.class);
        intent.putExtra("TAREA_A_EDITAR", tarea);
        intent.putExtra("POSICION_ORIGINAL", posicion);
        launcherCrearTarea.launch(intent);
    }
}