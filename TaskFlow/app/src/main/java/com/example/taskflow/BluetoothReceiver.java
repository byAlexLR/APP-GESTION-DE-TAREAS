package com.example.taskflow;

// Importa las líbrerias necesarias
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.UUID;

// Clase que implementa el Service para la recepción de datos
public class BluetoothReceiver extends Service {
    // Variables globales
    private static final String TAG = "BT_Receiver";
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final String NAME = "TaskFlow_Transfer";

    // Variables para la transferencia de datos
    public static final String ACTION_NUEVA_TAREA = "com.example.taskflow.NUEVA_TAREA_BT";

    // Variables para el Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothServerSocket serverSocket;
    private Thread acceptThread;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Método que se ejecuta al crear el servicio
    @Override
    public void onCreate() {
        super.onCreate();
        // Inicializa el adaptador Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        startAccepting();
    }

    // Método para iniciar la recepción de datos
    private void startAccepting() {
        // Cancela el hilo actual si existe, y crea uno nuevo y lo ejecuta
        stopAccepting();
        acceptThread = new Thread(this::runAcceptLoop);
        acceptThread.start();
    }

    // Método para detener la recepción de datos
    private void stopAccepting() {
        try {
            // Cierra el socket si existe
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error cerrando el serverSocket", e);
        }
        // Se asegura de que el hilo esté detenido
        if (acceptThread != null) {
            acceptThread.interrupt();
            acceptThread = null;
        }
    }

    // Método para ejecutar el hilo de recepción de datos
    @SuppressLint("MissingPermission")
    private void runAcceptLoop() {
        // Verificar permisos en tiempo de ejecución para Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Faltan permisos de conexión Bluetooth");
                stopSelf();
                return;
            }
        }

        try {
            // El ServerSocket se abre una sola vez para escuchar múltiples conexiones
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);

            // Bucle infinito para escuchar conexiones entrantes
            while (!Thread.currentThread().isInterrupted()) {
                // Variable para el socket
                BluetoothSocket socket;
                try {
                    // El socket se crea cada vez que se acepta una conexión
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    // Si el socket falla, lo notificamos
                    if (!Thread.currentThread().isInterrupted()) {
                        Log.e(TAG, "El socket falló", e);
                    }
                    break;
                }

                // Si el socket es válido, procesamos la tarea
                if (socket != null) {
                    // Procesamos la tarea recibida
                    gestionarTareaRecibida(socket);
                }
            }
            // Si el hilo se interrumpe, lo notificamos
        } catch (IOException e) {
            Log.e(TAG, "No se pudo iniciar el servidor Bluetooth", e);
        } finally {
            // Al finalizar, detenemos la recepción
            stopAccepting();
        }
    }

    // Método para gestionar la tarea recibida
    private void gestionarTareaRecibida(BluetoothSocket socket) {
        // Intenta leer el objeto Tarea del socket
        try (BluetoothSocket s = socket;
             ObjectInputStream inputStream = new ObjectInputStream(s.getInputStream())) {

            // Si el objeto es válido, lo procesamos
            Tarea tarea = (Tarea) inputStream.readObject();
            // Si la tarea es válida, la notificamos
            if (tarea != null) {
                // Notificamos al MainActivity que se ha recibido una nueva tarea
                mainHandler.post(() -> {
                    // Envia la tarea a través de un Intent
                    Intent intent = new Intent(ACTION_NUEVA_TAREA);
                    intent.putExtra("TAREA_RECIBIDA", tarea);
                    intent.setPackage(getPackageName());
                    sendBroadcast(intent);
                });
            }
            // Si falla, lo notificamos
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error leyendo el objeto Tarea", e);
        }
    }

    // Método que se ejecuta al iniciar el servicio, para que el servicio no se detenga
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    // Método que se ejecuta al detener el servicio
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cancela el hilo de recepción
        stopAccepting();
    }

    // Método que se ejecuta al detener el servicio
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
