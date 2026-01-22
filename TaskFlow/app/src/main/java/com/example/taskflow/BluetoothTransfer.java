package com.example.taskflow;

// Importa las líbrerias necesarias
import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

// Importa las clases necesarias
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Clase que implementa el BottomSheetDialogFragment para la transferencia de datos
public class BluetoothTransfer extends BottomSheetDialogFragment {

    // Variables globales
    private Tarea tareaAEnviar;
    private final ArrayList<String> deviceNames = new ArrayList<>();
    private final ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    // Variables para la vista
    private TextView tvStatus, tvEmpty;
    private ProgressBar progressBar;

    // Variables para el Bluetooth
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    // Receptor para el escaneo de dispositivos
    private final BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        // Método llamado cuando se encuentra un dispositivo
        public void onReceive(Context context, Intent intent) {
            // Verifica si el intent es del tipo ACTION_FOUND
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                // Agrega el dispositivo a la lista
                agregarDispositivo(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
            }
        }
    };

    // Launcher para solicitar permisos de ubicación y Bluetooth
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            // Registra el resultado de la solicitud de permisos
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                // Verifica si todos los permisos fueron concedidos, si no, muestra un mensaje
                if (!result.containsValue(false)) iniciarEscaneo();
                else {
                    Toast.makeText(getContext(), R.string.permisos_denegados, Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            });

    // Método para crear una nueva instancia del BottomSheet
    public static BluetoothTransfer newInstance(Tarea tarea) {
        // Crea una nueva instancia del BottomSheet
        BluetoothTransfer f = new BluetoothTransfer();
        // Pasa la tarea como argumento
        Bundle args = new Bundle();
        args.putSerializable("TAREA", tarea);
        f.setArguments(args);
        return f;
    }

    // Método para crear la vista del BottomSheet
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup cont, @Nullable Bundle s) {
        // Inflar el layout del BottomSheet, para poder acceder a sus elementos
        return inflater.inflate(R.layout.item_bluetooth, cont, false);
    }

    // Método para inicializar la vista del BottomSheet
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Obtener la tarea a enviar a través de los argumentos
        if (getArguments() != null) tareaAEnviar = (Tarea) getArguments().getSerializable("TAREA");

        // Inicializar los elementos de la vista
        tvStatus = view.findViewById(R.id.tvSheetStatus);
        tvEmpty = view.findViewById(R.id.tvEmptyState);
        progressBar = view.findViewById(R.id.progressBarSheet);
        view.findViewById(R.id.btnSheetCancel).setOnClickListener(v -> dismiss());

        // Configurar el adaptador para la lista de dispositivos
        ListView listView = view.findViewById(R.id.lvSheetDevices);
        adapter = new ArrayAdapter<>(requireContext(), R.layout.item_bluetooth_device, deviceNames);
        listView.setAdapter(adapter);

        // Configurar el listener para el clic en un dispositivo
        listView.setOnItemClickListener((p, v, pos, id) -> enviarTarea(devices.get(pos)));

        // Verificar y solicitar permisos si es necesario
        checkPermissionsAndStart();
    }

    // Método para verificar y solicitar permisos de ubicación y Bluetooth
    private void checkPermissionsAndStart() {
        // Lista de permisos a solicitar
        ArrayList<String> perms = new ArrayList<>();
        // Comprueba si el dispone de Android 12 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Comprueba si el usuario ha denegado los permisos
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
                perms.add(Manifest.permission.BLUETOOTH_SCAN);
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                perms.add(Manifest.permission.BLUETOOTH_CONNECT);
            // Comprueba si el usuario ha denegado los permisos
        } else if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // Si hay permisos a solicitar, los solicita
        if (!perms.isEmpty()) requestPermissionLauncher.launch(perms.toArray(new String[0]));
        else iniciarEscaneo();
    }

    // Método para iniciar el escaneo de dispositivos
    @SuppressLint("MissingPermission")
    private void iniciarEscaneo() {
        // Verifica si el Bluetooth está habilitado
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(getContext(), R.string.bt_desactivado, Toast.LENGTH_SHORT).show();
            return;
        }

        // Limpia las listas de dispositivos y lo notifica al adaptador
        deviceNames.clear(); devices.clear(); adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(View.GONE); progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText(R.string.bt_buscando_dispositivos);

        // Intenta buscar dispositivos y registra el receptor
        try {
            // Busca dispositivos emparejados
            for (BluetoothDevice d : bluetoothAdapter.getBondedDevices()) agregarDispositivo(d);
            // Busca dispositivos no emparejados
            if (bluetoothAdapter.isDiscovering()) bluetoothAdapter.cancelDiscovery();
            // Inicia el escaneo
            bluetoothAdapter.startDiscovery();
            // Registra el receptor
            requireContext().registerReceiver(scanReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            // Captura las posibles excepciones
        } catch (Exception e) {
            Log.e("BT", "Error scan", e);
        }
    }

    // Método para agregar un dispositivo a la lista
    @SuppressLint("MissingPermission")
    private void agregarDispositivo(BluetoothDevice device) {
        // Verifica si el dispositivo ya está en la lista y lo agrega si no está
        if (device != null && device.getName() != null && !devices.contains(device)) {
            devices.add(device); // Agrega el dispositivo a la lista
            deviceNames.add(device.getName()); // Agrega el nombre del dispositivo a la lista
            adapter.notifyDataSetChanged(); // Notifica al adaptador que los datos han cambiado
        }
    }

    // Método para enviar la tarea a un dispositivo
    @SuppressLint("MissingPermission")
    private void enviarTarea(BluetoothDevice device) {
        // Detener el escaneo y notificar al usuario
        detenerEscaneo();
        String devName = device.getName();
        // Actualizar el estado de la vista
        tvStatus.setText(getString(R.string.bt_conectando_con, devName));

        // Crear un hilo para enviar la tarea
        executor.execute(() -> {
            // Intenta conectarse al dispositivo y enviar la tarea
            try (BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MY_UUID)) {
                // Conecta al dispositivo
                socket.connect();

                // Actualiza el estado de la vista
                mainHandler.post(() -> tvStatus.setText(R.string.bt_enviando_datos));
                // Agrega un objeto de salida para enviar la tarea
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject(tareaAEnviar); // Envía la tarea
                outputStream.flush(); // Actualiza el flujo de datos

                // Actualiza el estado de la vista
                mainHandler.post(() -> {
                    Toast.makeText(getContext(), getString(R.string.bt_completado, devName), Toast.LENGTH_LONG).show();
                    dismiss();
                });
                // Captura las posibles excepciones
            } catch (IOException e) {
                Log.e("BT", "Error: ", e);
                // Actualiza el estado de la vista
                mainHandler.post(() -> {
                    // Actualiza el estado de la vista
                    tvStatus.setText(R.string.bt_error_reintentar);
                    // Muestra un mensaje de error
                    Toast.makeText(getContext(), getString(R.string.bt_error_envio, e.getMessage()), Toast.LENGTH_SHORT).show();
                    // Oculta la barra de progreso
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    // Método para detener el escaneo de dispositivos
    @SuppressLint("MissingPermission")
    private void detenerEscaneo() {
        try {
            // Cancela el escaneo y desregistra el receptor
            if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) bluetoothAdapter.cancelDiscovery();
            requireContext().unregisterReceiver(scanReceiver);
        } catch (Exception e) {
            Log.e("BT", "Error: ", e);
        }
    }

    // Método que destruye la vista
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Detiene el escaneo al destruir la vista
        detenerEscaneo();
    }
}