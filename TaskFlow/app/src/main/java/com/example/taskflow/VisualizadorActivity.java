package com.example.taskflow;

// Importa las líbrerias necesarias
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.Locale;

// Clase principal para la Visualización
public class VisualizadorActivity extends AppCompatActivity {

    private static final String TAG = "VisualizadorActivity";

    // Componentes Multimedia
    private MediaPlayer mediaPlayer;
    private VideoView visorVideo;

    // Controles de UI para Audio
    private FloatingActionButton btnPlayPauseAudio;
    private SeekBar seekBarAudio;
    private TextView tvTiempoActual, tvTiempoTotal;
    private CardView cardVisorAudioContainer;

    // Controles de UI para Video
    private FloatingActionButton btnPlayPauseVideo;
    private SeekBar seekBarVideo;
    private TextView tvVideoTiempoActual, tvVideoTiempoTotal;
    private CardView cardVisorVideoControls;

    // Lógica de actualización de UI
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBar;
    private Runnable updateSeekBarVideo;

    @Override
    // Método principal
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizador);

        initViews();

        // Recuperamos los datos del Intent
        String uriStr = getIntent().getStringExtra("URI");
        String tipo = getIntent().getStringExtra("TIPO");

        // Cargamos el contenido según el tipo
        if (isValidUri(uriStr)) {
            Uri uri = Uri.parse(uriStr);
            cargarContenido(uri, tipo, uriStr);
        } else {
            Log.e(TAG, "URI nula o inválida recibida");
            finish();
        }
    }

    // Lógica de inicialización de vistas
    private void initViews() {
        visorVideo = findViewById(R.id.visorVideo);

        // Controles de UI para Audio
        cardVisorAudioContainer = findViewById(R.id.cardVisorAudioContainer);
        btnPlayPauseAudio = findViewById(R.id.btnPlayPauseAudio);
        seekBarAudio = findViewById(R.id.seekBarAudio);
        tvTiempoActual = findViewById(R.id.tvTiempoActual);
        tvTiempoTotal = findViewById(R.id.tvTiempoTotal);

        // Controles de UI para Video
        cardVisorVideoControls = findViewById(R.id.cardVisorVideoControls);
        btnPlayPauseVideo = findViewById(R.id.btnPlayPauseVideo);
        seekBarVideo = findViewById(R.id.seekBarVideo);
        tvVideoTiempoActual = findViewById(R.id.tvVideoTiempoActual);
        tvVideoTiempoTotal = findViewById(R.id.tvVideoTiempoTotal);

        // Botón Cerrar
        View btnCerrar = findViewById(R.id.btnCerrarVisor);
        View btnCerrarCard = findViewById(R.id.btnCerrarVisorCard);

        // Manejador de clics
        View.OnClickListener closeListener = v -> finish();
        btnCerrar.setOnClickListener(closeListener);
        btnCerrarCard.setOnClickListener(closeListener);
    }

    // Lógica de carga de contenido
    private void cargarContenido(Uri uri, String tipo, String uriStr) {
        ImageView visorImagen = findViewById(R.id.visorImagen);

        // Cargamos el contenido según el tipo
        if ("image".equals(tipo)) {
            visorImagen.setVisibility(View.VISIBLE);
            visorImagen.setImageURI(uri);

        } else if ("video".equals(tipo)) {
            setupVideo(uri);

        } else if ("audio".equals(tipo)) {
            setupAudioUI(uri, uriStr);
        }
    }

    // Si es un video, lo cargamos en el VideoView
    private void setupVideo(Uri uri) {
        // Configuramos el VideoView
        visorVideo.setVisibility(View.VISIBLE);
        cardVisorVideoControls.setVisibility(View.VISIBLE);
        visorVideo.setVideoURI(uri);

        // Configuramos el Listener para reproducir el video
        visorVideo.setOnPreparedListener(mp -> {
            tvVideoTiempoTotal.setText(formatTime(visorVideo.getDuration()));
            seekBarVideo.setMax(visorVideo.getDuration());
            visorVideo.start();
            btnPlayPauseVideo.setImageResource(android.R.drawable.ic_media_pause);
            startSeekBarUpdateVideo();
        });

        // Configuramos el Listener para cuando termine el video
        visorVideo.setOnCompletionListener(mp -> {
            btnPlayPauseVideo.setImageResource(android.R.drawable.ic_media_play);
            seekBarVideo.setProgress(0);
            tvVideoTiempoActual.setText(R.string._00_00);
            stopSeekBarUpdateVideo();
        });

        // Configuramos el Listener para cuando haya un error
        visorVideo.setOnErrorListener((mp, what, extra) -> {
            Log.e(TAG, "Error reproduciendo video: " + what);
            return true;
        });

        // Configurar botones de control
        btnPlayPauseVideo.setOnClickListener(v -> toggleVideoPlayback());

        // Configuramos el Listener para la SeekBar
        seekBarVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            // Actualizamos la posición en el video
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    visorVideo.seekTo(progress);
                    tvVideoTiempoActual.setText(formatTime(progress));
                }
            }

            @Override
            // No hacemos nada en estos métodos
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // Lógica para reproducir o pausar el video
    private void toggleVideoPlayback() {
        // Si está reproduciendo, lo paramos, si no, lo reproducimos
        if (visorVideo.isPlaying()) {
            visorVideo.pause();
            btnPlayPauseVideo.setImageResource(android.R.drawable.ic_media_play);
            stopSeekBarUpdateVideo();
        } else {
            visorVideo.start();
            btnPlayPauseVideo.setImageResource(android.R.drawable.ic_media_pause);
            startSeekBarUpdateVideo();
        }
    }

    // Lógica para iniciar la actualización de la SeekBar
    private void startSeekBarUpdateVideo() {
        // Paramos la actualización de la SeekBar
        stopSeekBarUpdateVideo();
        // Actualizamos la posición en el video
        updateSeekBarVideo = new Runnable() {
            @Override
            public void run() {
                // Si el VideoView está reproduciendo, actualizamos la SeekBar
                if (visorVideo != null && visorVideo.isPlaying()) {
                    int currentPos = visorVideo.getCurrentPosition();
                    seekBarVideo.setProgress(currentPos);
                    tvVideoTiempoActual.setText(formatTime(currentPos));
                    handler.postDelayed(this, 500);
                }
            }
        };
        // Iniciamos la actualización
        handler.post(updateSeekBarVideo);
    }

    // Lógica para detener la actualización de la SeekBar
    private void stopSeekBarUpdateVideo() {
        // Paramos la actualización de la SeekBar
        if (updateSeekBarVideo != null) {
            handler.removeCallbacks(updateSeekBarVideo);
        }
    }

    // Si es un audio, lo cargamos en el UI
    private void setupAudioUI(Uri uri, String uriStr) {
        // Hacemos visible el contenedor de Audio
        if (cardVisorAudioContainer != null) {
            cardVisorAudioContainer.setVisibility(View.VISIBLE);
        }

        // Configuramos el nombre del audio
        TextView tvNombreAudio = findViewById(R.id.tvNombreAudio);
        tvNombreAudio.setText(getFileName(uriStr));

        // Preparamos el MediaPlayer
        prepararMediaPlayer(uri);
    }

    // Lógica de preparación del MediaPlayer
    private void prepararMediaPlayer(Uri uri) {
        // Configuramos el MediaPlayer
        mediaPlayer = new MediaPlayer();
        try {
            // Preparamos el MediaPlayer
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.prepareAsync();

            // Configuramos el Listener para cuando esté listo
            mediaPlayer.setOnPreparedListener(mp -> {
                tvTiempoTotal.setText(formatTime(mp.getDuration()));
                seekBarAudio.setMax(mp.getDuration());
                btnPlayPauseAudio.setEnabled(true);
                setupAudioControls();
            });

            // Configuramos el Listener para cuando termine
            mediaPlayer.setOnCompletionListener(mp -> {
                btnPlayPauseAudio.setImageResource(android.R.drawable.ic_media_play);
                btnPlayPauseAudio.setContentDescription(getString(R.string.desc_reproducir));
                seekBarAudio.setProgress(0);
                tvTiempoActual.setText(R.string._00_00);
                stopSeekBarUpdate();
            });
            // Capturamos errores
        } catch (IOException e) {
            Log.e(TAG, "Error preparando audio", e);
        }
    }

    // Lógica para configurar los controles de Audio
    private void setupAudioControls() {
        // Configuramos los controles de Audio
        btnPlayPauseAudio.setOnClickListener(v -> toggleAudioPlayback());

        // Configuramos el Listener para la SeekBar (actualización visual)
        seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            // Actualizamos la posición en el audio
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    tvTiempoActual.setText(formatTime(progress));
                }
            }

            // No hacemos nada en estos métodos
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // Lógica para reproducir o pausar el audio
    private void toggleAudioPlayback() {
        // Si el MediaPlayer no está creado, no hacemos nada
        if (mediaPlayer == null) return;

        // Si está reproduciendo, lo paramos, si no, lo reproducimos
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPauseAudio.setImageResource(android.R.drawable.ic_media_play);
            btnPlayPauseAudio.setContentDescription(getString(R.string.desc_reproducir));
            stopSeekBarUpdate();
        } else {
            mediaPlayer.start();
            btnPlayPauseAudio.setImageResource(android.R.drawable.ic_media_pause);
            btnPlayPauseAudio.setContentDescription(getString(R.string.desc_pausar));
            startSeekBarUpdate();
        }
    }

    // Lógica para iniciar la actualización de la SeekBar
    private void startSeekBarUpdate() {
        // Paramos la actualización de la SeekBar
        stopSeekBarUpdate();
        // Actualizamos la posición en el audio
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                // Si el MediaPlayer está reproduciendo, actualizamos la SeekBar
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPos = mediaPlayer.getCurrentPosition();
                    seekBarAudio.setProgress(currentPos);
                    tvTiempoActual.setText(formatTime(currentPos));
                    handler.postDelayed(this, 500);
                }
            }
        };
        // Iniciamos la actualización
        handler.post(updateSeekBar);
    }

    // Lógica para detener la actualización de la SeekBar
    private void stopSeekBarUpdate() {
        // Paramos la actualización de la SeekBar
        if (updateSeekBar != null) {
            handler.removeCallbacks(updateSeekBar);
        }
    }

    // Lógica para formatear el tiempo
    private String formatTime(int ms) {
        int minutes = (ms / 1000) / 60; // Convertimos a minutos
        int seconds = (ms / 1000) % 60; // Convertimos a segundos
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds); // Formato 00:00
    }

    // Lógica para obtener el nombre del archivo
    private String getFileName(String uriString) {
        try {
            // Intentamos obtener el nombre del archivo
            Uri uri = Uri.parse(uriString);
            // Obtenemos el path del URI
            String path = uri.getPath();
            // Si el path es válido, lo retornamos
            if (path != null && path.contains("/")) {
                return path.substring(path.lastIndexOf('/') + 1);
            }
            // Si no, retornamos el último segmento del URI
            return uri.getLastPathSegment() != null ? uri.getLastPathSegment() : "Audio";
        } catch (Exception e) {
            return "Audio";
        }
    }

    // Lógica para verificar si la URI es válida
    private boolean isValidUri(String uriStr) {
        return uriStr != null && !uriStr.isEmpty(); // Si la URI no es nula o vacía, es válida
    }

    // Lógica de ciclo de vida de Activity
    @Override
    protected void onPause() {
        super.onPause();
        // Si el MediaPlayer está reproduciendo, lo pausamos
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPauseAudio.setImageResource(android.R.drawable.ic_media_play);
            btnPlayPauseAudio.setContentDescription(getString(R.string.desc_reproducir));
            stopSeekBarUpdate();
        }
        // Si el VideoView está reproduciendo, lo pausamos
        if (visorVideo != null && visorVideo.isPlaying()) {
            visorVideo.pause();
            btnPlayPauseVideo.setImageResource(android.R.drawable.ic_media_play);
            stopSeekBarUpdateVideo();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberamos los recursos del MediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopSeekBarUpdate();
        stopSeekBarUpdateVideo();
    }
}