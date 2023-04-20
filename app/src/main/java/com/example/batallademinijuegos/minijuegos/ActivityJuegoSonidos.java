package com.example.batallademinijuegos.minijuegos;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.batallademinijuegos.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivityJuegoSonidos extends AppCompatActivity {
    TextView tiempo;
    TextView textPuntos;
    TextView textoCronometro;
    Button botonSalir;
    ImageButton botonOpc1;
    ImageButton botonOpc2;

    MediaPlayer sonidoBeep;
    MediaPlayer sonidoSilbato;
    MediaPlayer sonidoRespuestaCorrecta;
    MediaPlayer sonidoRespuestaIncorrecta;
    MediaPlayer sonidoDefinitivo;

    CountDownTimer descuento = null;
    CountDownTimer cronometro = null;
    CountDownTimer cronometroEntreTiempo = null;
    Handler handlerControl = null;

    FirebaseFirestore db;
    Boolean batalla;
    String nombreUsuario;
    String partidaId;
    String nombreJ1;

    ArrayList<String> listaSonidos;
    int[] sonidoViolin;
    int[] sonidoClarinete;
    int[] sonidoTransporte;
    int sonidoCorrecto;
    int sonidoAleatorio;

    long tiempoRespuesta;
    long tiempoRestante2;
    int cont = 0;
    int puntos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_juego_sonidos);

        batalla = getIntent().getBooleanExtra("batalla", false);
        partidaId = getIntent().getStringExtra("partidaId");
        nombreJ1 = getIntent().getStringExtra("nombreJ1");

        db = FirebaseFirestore.getInstance();

        sonidoBeep = MediaPlayer.create(this, R.raw.beep_tiempo);
        sonidoSilbato = MediaPlayer.create(this, R.raw.sonido_silbato);
        sonidoRespuestaCorrecta = MediaPlayer.create(this, R.raw.sonido_correcto);
        sonidoRespuestaIncorrecta = MediaPlayer.create(this, R.raw.sonido_incorrecto);

        tiempo = findViewById(R.id.textTiempo);
        textPuntos = findViewById(R.id.textPuntos);
        textoCronometro = findViewById(R.id.textCronometro);
        botonSalir = findViewById(R.id.boton_salir);
        botonOpc1 = findViewById(R.id.botonOpc1);
        botonOpc2 = findViewById(R.id.botonOpc2);

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

        listaSonidos = new ArrayList<>();
        listaSonidos.add("violin");
        listaSonidos.add("clarinete");
        listaSonidos.add("transporte");

        sonidoViolin = new int[]{R.raw.sonido_violin, R.raw.sonido_gaviota};
        sonidoClarinete = new int[]{R.raw.sonido_clarinete, R.raw.sonido_pato};
        sonidoTransporte = new int[]{R.raw.sonido_barco, R.raw.sonido_tren};

        cont = 0;
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
                        tiempo.setTextColor(Color.RED);
                        break;
                    case 1:
                        sonidoBeep.start();
                        tiempo.setText(String.valueOf(tiempoRestante / 1000));
                        tiempo.setTextColor(Color.GREEN);
                        break;
                    case 0:
                        tiempo.setTextSize(60);
                        tiempo.setText(R.string.textoEscucha);
                        tiempo.setTextColor(Color.YELLOW);
                        break;
                }
            }

            public void onFinish() {
                botonSalir.setEnabled(true);
                tiempo.setVisibility(View.INVISIBLE);

                botonOpc1.setVisibility(View.VISIBLE);
                botonOpc2.setVisibility(View.VISIBLE);

                tiempoCronometro();
                comenzarJuego();

                if (batalla){
                    controlarSalidaOtroJugador();
                }
            }
        }.start();
    }

    //Tiempo para elegir lo que suena.
    public void tiempoCronometro(){
        cronometro = new CountDownTimer(4000, 1000) {

            public void onTick(long tiempoRestante) {
                textoCronometro.setText("00:0" + String.valueOf(tiempoRestante / 1000));
                tiempoRestante2 = tiempoRestante;
            }

            public void onFinish() {
                botonOpc1.setVisibility(View.INVISIBLE);
                botonOpc2.setVisibility(View.INVISIBLE);
                botonOpc1.setEnabled(true);
                botonOpc2.setEnabled(true);

                entreTiempo();
            }
        }.start();
    }

    //Tiempo entre las veces jugadas. Se juega 3 veces.
    public void entreTiempo(){
        cronometroEntreTiempo = new CountDownTimer(1000, 1000) {
            public void onTick(long tiempoRestante) {

            }

            public void onFinish() {
                botonOpc1.setBackground(hacerDrawable(R.drawable.color_azul));
                botonOpc2.setBackground(hacerDrawable(R.drawable.color_azul));
                botonSalir.setEnabled(false);

                cont++;
                if (cont < 3){
                    botonOpc1.setVisibility(View.VISIBLE);
                    botonOpc2.setVisibility(View.VISIBLE);

                    tiempoCronometro();
                    comenzarJuego();
                }else {
                    if (batalla){
                        terminarBatalla();
                    }else {
                        terminarPractica();
                    }
                }
            }
        }.start();
    }

    //Elige el sonido aleatorio que va a sonar y muestra las imagenes correspondientes. Empieza el sonido.
    public void comenzarJuego(){
        String opcion = elegirSonido();
        sonidoAleatorio = (int)(Math.random()*2);

        switch (opcion){
            case "violin":
                sonidoCorrecto = sonidoViolin[sonidoAleatorio];
                sonidoDefinitivo = MediaPlayer.create(this, sonidoCorrecto);

                botonOpc1.setForeground(hacerDrawable(R.drawable.s_violin));
                botonOpc2.setForeground(hacerDrawable(R.drawable.s_gaviota));

                break;
            case "clarinete":
                sonidoCorrecto = sonidoClarinete[sonidoAleatorio];
                sonidoDefinitivo = MediaPlayer.create(this, sonidoCorrecto);

                botonOpc1.setForeground(hacerDrawable(R.drawable.s_clarinete));
                botonOpc2.setForeground(hacerDrawable(R.drawable.s_pato));

                break;
            case "transporte":
                sonidoCorrecto = sonidoTransporte[sonidoAleatorio];
                sonidoDefinitivo = MediaPlayer.create(this, sonidoCorrecto);

                botonOpc1.setForeground(hacerDrawable(R.drawable.s_barco));
                botonOpc2.setForeground(hacerDrawable(R.drawable.s_tren));

                break;
        }
        sonidoDefinitivo.start();
    }

    //Elige el sonido aleatoriamente.
    public String elegirSonido(){
        int aleatorio = (int)(Math.random()*listaSonidos.size());
        String opcion = listaSonidos.get(aleatorio);
        listaSonidos.remove(aleatorio);
        return opcion;
    }

    //Botón de la carta de la izquierda.
    public void clickBoton1(View view){
        botonOpc1.setEnabled(false);
        botonOpc2.setEnabled(false);

        if (sonidoAleatorio == 0){
            sonidoRespuestaCorrecta.start();
            botonOpc1.setBackground(hacerDrawable(R.drawable.color_verde));

            tiempoRespuesta = tiempoRestante2;
            ponerPuntuacion(true);
        }else{
            sonidoRespuestaIncorrecta.start();
            botonOpc1.setBackground(hacerDrawable(R.drawable.color_rojo));

            ponerPuntuacion(false);
        }
    }

    //Botón de la carta de la derecha.
    public void clickBoton2(View view){
        botonOpc1.setEnabled(false);
        botonOpc2.setEnabled(false);

        if (sonidoAleatorio == 0){
            sonidoRespuestaIncorrecta.start();
            botonOpc2.setBackground(hacerDrawable(R.drawable.color_rojo));

            ponerPuntuacion(false);
        }else{
            sonidoRespuestaCorrecta.start();
            botonOpc2.setBackground(hacerDrawable(R.drawable.color_verde));

            tiempoRespuesta = tiempoRestante2;
            ponerPuntuacion(true);
        }
    }

    //Puntua según lo que se ha tardado en darle a la carta.
    public void ponerPuntuacion(Boolean acierto){
        if (!acierto){
            puntos = puntos - 20;
        }else {
            if (tiempoRespuesta > 3500) {
                puntos = puntos + 75;
            }else if (tiempoRespuesta > 3000){
                puntos = puntos + 50;
            }else if (tiempoRespuesta > 2500){
                puntos = puntos + 40;
            }else if (tiempoRespuesta > 2000){
                puntos = puntos + 30;
            }else if (tiempoRespuesta > 1000){
                puntos = puntos + 20;
            }else if (tiempoRespuesta > 0){
                puntos = puntos + 10;
            }
        }

        textPuntos.setText(String.valueOf(puntos));
    }

    //Método al terminar las 3 partidas en modo PRACTICA. Muestra un alertDialog con los puntos obtenidos y la opción de salir o volver a jugar.
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
                cont = 0;
                listaSonidos.add("violin");
                listaSonidos.add("clarinete");
                listaSonidos.add("transporte");

                botonSalir.setEnabled(false);
                textPuntos.setText(String.valueOf(puntos));
                textoCronometro.setText("00:03");

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

    //Método al terminar las 3 partidas en modo BATALLA. Guarda los puntos en la bd y termina.
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
        sonidoDefinitivo.stop();
        cancelarHilos();

        if (batalla){
            abandonarBatalla();
        }else {
            finish();
        }
    }

    //Botón salir en modo BATALLA.
    public void abandonarBatalla(){
        cancelarHilos();
        db.collection("partidasActivas").document(partidaId).delete();
        if (handlerControl != null){
            handlerControl.removeCallbacksAndMessages(null);
        }
        if (cronometro != null){
            cronometro.cancel();
        }
        if (descuento != null){
            descuento.cancel();
        }
        this.finish();
    }

    //Controla si el otro jugador ha salido de la partida.
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

    //Hace drawable las imagenes.
    public Drawable hacerDrawable(int imagen){
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), imagen, null);

        return drawable;
    }

    //Evita volver atrás con el botón del dispositivo.
    @Override
    public void onBackPressed() {

    }

    public void cancelarHilos(){
        if (cronometro != null){
            cronometro.cancel();
        }
        if (cronometroEntreTiempo != null){
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