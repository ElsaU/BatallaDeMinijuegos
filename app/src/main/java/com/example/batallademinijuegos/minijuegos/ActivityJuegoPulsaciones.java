package com.example.batallademinijuegos.minijuegos;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;

import com.example.batallademinijuegos.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;

public class ActivityJuegoPulsaciones extends AppCompatActivity{
    ConstraintLayout layout;
    MediaPlayer sonidoBeep;
    MediaPlayer sonidoSilbato;

    TextView tiempo;
    TextView textPuntos;
    TextView textoCronometro;
    Button botonSalir;
    Button botonPulsaciones;
    Guideline lineaLimite;

    int puntos = 0;

    CountDownTimer cronometro = null;
    CountDownTimer descuento = null;
    Handler handlerControl = null;

    FirebaseFirestore db;
    Boolean batalla;
    String nombreUsuario;
    String partidaId;
    String nombreJ1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_juego_pulsaciones);

        batalla = getIntent().getBooleanExtra("batalla", false);
        partidaId = getIntent().getStringExtra("partidaId");
        nombreJ1 = getIntent().getStringExtra("nombreJ1");

        db = FirebaseFirestore.getInstance();

        layout = findViewById(R.id.layout);

        sonidoBeep = MediaPlayer.create(this, R.raw.beep_tiempo);
        sonidoSilbato = MediaPlayer.create(this, R.raw.sonido_silbato);

        tiempo = findViewById(R.id.textTiempo);
        textPuntos = findViewById(R.id.textPuntos);
        textoCronometro = findViewById(R.id.textCronometro);
        botonSalir = findViewById(R.id.boton_salir);
        botonPulsaciones = findViewById(R.id.botonPulsaciones);
        lineaLimite = findViewById(R.id.lineaLimite);

        handlerControl = new Handler();

        if (batalla){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String email = user.getEmail();

            db.collection("usuarios").document(email).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    nombreUsuario = document.getString("nombreUsuario");
                                }
                            }
                        }
                    });
        }

        descuento();
    }

    //Cuenta atrás de 3 segundos para comenzar el juego
    public void descuento(){
        tiempo.setText("");
        tiempo.setTextSize(120);
        tiempo.setVisibility(View.VISIBLE);
        descuento = new CountDownTimer(5000, 1000) {
            public void onTick(long tiempoRestante) {
                switch ((int)tiempoRestante / 1000){
                    case 3:
                        sonidoBeep.start();
                        tiempo.setText(String.valueOf(tiempoRestante / 1000));
                        tiempo.setTextColor(Color.BLUE);
                        break;
                    case 2:
                        sonidoBeep.start();
                        tiempo.setText(String.valueOf(tiempoRestante / 1000));
                        tiempo.setTextColor(Color.WHITE);
                        break;
                    case 1:
                        sonidoBeep.start();
                        tiempo.setText(String.valueOf(tiempoRestante / 1000));
                        tiempo.setTextColor(Color.GREEN);
                        break;
                    case 0:
                        sonidoSilbato.start();
                        tiempo.setTextSize(60);
                        tiempo.setText(R.string.textoComienza);
                        tiempo.setTextColor(Color.YELLOW);

                        break;
                }
            }

            public void onFinish() {
                botonPulsaciones.setEnabled(true);
                botonPulsaciones.setVisibility(View.VISIBLE);
                botonSalir.setEnabled(true);
                tiempo.setVisibility(View.INVISIBLE);
                comenzarTiempo();
                guardarPosicionOriginal();
                if (batalla){
                    controlarSalidaOtroJugador();
                }
            }
        }.start();
    }

    //Cuenta atrás de 30 segundos. Es lo que dura el juego.
    public void comenzarTiempo(){
        cronometro = new CountDownTimer(31000, 1000) {

            public void onTick(long tiempoRestante) {
                if (((int)tiempoRestante / 1000) < 10){
                    textoCronometro.setText("00:0" + String.valueOf(tiempoRestante / 1000));
                }else{
                    textoCronometro.setText("00:" + String.valueOf(tiempoRestante / 1000));
                }

                switch ((int)tiempoRestante / 1000){
                    case 25:
                    case 20:
                    case 15:
                    case 12:
                    case 10:
                    case 8:
                    case 6:
                    case 5:
                    case 4:
                    case 3:
                    case 2:
                    case 1:
                        moverBoton();
                        break;
                    case 0:
                        sonidoSilbato.start();
                        botonPulsaciones.setEnabled(false);
                        break;
                }
            }

            public void onFinish() {
                if (batalla){
                    terminarBatalla();
                }else {
                    terminarPractica();
                }
            }

        }.start();
    }

    //Cada vez que se pulsa se suma 1 punto.
    public void pulsar(View view) {
        puntos = puntos + 1;
        textPuntos.setText(String.valueOf(puntos));
    }

    float originalX = 0;
    float originalY = 0;
    //Guarda la posición inicial del botón para volver a jugar.
    public void guardarPosicionOriginal(){
        originalX = botonPulsaciones.getX();
        originalY = botonPulsaciones.getY();
    }

    //Mueve el botón durante el juego a una posición aleatoria.
    public void moverBoton(){
        int posX;
        int posY;
        do {
            posX = posicionAleatoriaX();
        }while((posX + botonPulsaciones.getWidth()) > layout.getWidth());
        botonPulsaciones.setX(posX);
        do{
            posY = posicionAleatoriaY();
        }while((posY + botonPulsaciones.getHeight()) > layout.getHeight() || posY < lineaLimite.getY());
        botonPulsaciones.setY(posY);
    }

    //Posición aleatoria en el eje X para mover el botón.
    public int posicionAleatoriaX(){
        int aleatorio = (int)(Math.random()*layout.getWidth());
        return aleatorio;
    }

    //Posición aleatoria en el eje Y para mover el botón.
    public int posicionAleatoriaY(){
        int aleatorio = (int)(Math.random()*layout.getHeight());

        return aleatorio;
    }

    //Método que se ejecuta el terminar los 30 segundo de juego en modo PRACTICA. Muestra un alertDialog con los puntos obtenidos y la opción de salir o volver a jugar.
    public void terminarPractica(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final Context context = alert.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View v = inflater.inflate(R.layout.fin_juegos, null, false);

        final TextView puntuacion = v.findViewById(R.id.textoPuntosFinales);
        puntuacion.setText(String.valueOf(puntos));

        alert.setView(v).
                setCancelable(false);

        final AlertDialog dialog = alert.show();
        Button dialogButtonVolverAJugar = v.findViewById(R.id.botonVolverJugar);
        dialogButtonVolverAJugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelarHilos();
                puntos = 0;
                textPuntos.setText(String.valueOf(puntos));
                textoCronometro.setText("00:30");
                botonPulsaciones.setX(originalX);
                botonPulsaciones.setY(originalY);
                botonPulsaciones.setVisibility(View.INVISIBLE);

                botonSalir.setEnabled(false);
                descuento();
                dialog.dismiss();
            }
        });
        Button dialogButtonSalir = v.findViewById(R.id.botonSalir);
        dialogButtonSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelarHilos();
                dialog.dismiss();
                finish();
            }
        });
    }

    //Método que se ejecuta al terminar los 30 segundos en modo BATALLA. Guarda los puntos en la bd y termina.
    public void terminarBatalla(){
        cancelarHilos();
        HashMap<String, Object> dato = new HashMap<>();
        if (nombreJ1.equals(nombreUsuario)){
            dato.put("puntosJ1", puntos);
        }else {
            dato.put("puntosJ2", puntos);
        }

        db.collection("partidasActivas").document(partidaId).set(dato, SetOptions.merge());
        finish();
    }

    //Botón salir.
    public void salir(View view){
        cancelarHilos();
        if (batalla){
            abandonarBatalla();
        }else {
            finish();
        }
    }

    //Botón salir en modo BATALLA.
    public void abandonarBatalla(){
        db.collection("partidasActivas").document(partidaId).delete();
        cancelarHilos();
        this.finish();
    }

    //Controla si el otro jugador a salido de la batalla.
    public void controlarSalidaOtroJugador(){
        handlerControl.postDelayed(new Runnable() {
            public void run() {
                db.collection("partidasActivas").document(partidaId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (!document.exists()) {
                                        cancelarHilos();
                                        finish();
                                    }
                                }
                            }
                        });
                handlerControl.postDelayed(this, 500);
            }
        }, 500);
    }

    //Evita volver atrás con el botón del dispositivo.
    @Override
    public void onBackPressed() {

    }

    public void cancelarHilos(){
        if (cronometro != null){
            cronometro.cancel();
        }
        if (descuento != null){
            descuento.cancel();
        }
        if (handlerControl != null){
            handlerControl.removeCallbacksAndMessages(null);
        }
    }
}