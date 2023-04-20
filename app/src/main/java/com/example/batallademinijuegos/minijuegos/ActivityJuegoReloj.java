package com.example.batallademinijuegos.minijuegos;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.batallademinijuegos.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;

import static java.lang.Thread.sleep;

public class ActivityJuegoReloj extends AppCompatActivity {
    MediaPlayer sonidoBeep;
    MediaPlayer sonidoSilbato;
    MediaPlayer sonidoAcierto;

    TextView tiempo;
    TextView textPuntos;
    TextView textoCronometro;
    Button botonSalir;
    TextView textTaparCronometro1;
    TextView textTaparCronometro2;
    Button botonParar;
    TextView textTiempoBuscar;
    TextView textComparar;
    TextView textRival;
    TextView textTiempoRival;
    TextView textoPararEn;

    int tiempoAleatorio = 0;
    int puntos = 0;
    int cont = 0;
    int contEntreTiempo;
    Boolean tapado = false;

    Boolean estado = false;
    int segOtroJugador = -1;
    int milisegOtroJugador = -1;
    int diferenciaSeg = 0;
    long diferenciaMilis = 0;

    CountDownTimer cronometroEntreTiempo = null;
    CountDownTimer descuento = null;
    Handler handlerControl = null;
    Handler handler = null;
    Handler handlerPuntos = null;
    Handler handlerTiempo = null;

    FirebaseFirestore db;
    Boolean batalla;
    String nombreUsuario;
    String partidaId;
    String nombreJ1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_juego_reloj);

        batalla = getIntent().getBooleanExtra("batalla", false);
        partidaId = getIntent().getStringExtra("partidaId");
        nombreJ1 = getIntent().getStringExtra("nombreJ1");

        db = FirebaseFirestore.getInstance();

        sonidoBeep = MediaPlayer.create(this, R.raw.beep_tiempo);
        sonidoSilbato = MediaPlayer.create(this, R.raw.sonido_silbato);
        sonidoAcierto = MediaPlayer.create(this, R.raw.sonido_correcto);

        tiempo = findViewById(R.id.textTiempo);
        textPuntos = findViewById(R.id.textPuntos);
        textoCronometro = findViewById(R.id.textCronometro);
        botonSalir = findViewById(R.id.boton_salir);
        textTaparCronometro1 = findViewById(R.id.textTaparCronometro1);
        textTaparCronometro2 = findViewById(R.id.textTaparCronometro2);
        botonParar = findViewById(R.id.botonParar);
        textTiempoBuscar = findViewById(R.id.textTiempoBuscar);
        textComparar = findViewById(R.id.textComparar);
        textRival = findViewById(R.id.textRival);
        textTiempoRival = findViewById(R.id.textTiempoRival);
        textoPararEn = findViewById(R.id.textoPararEn);

        handlerControl = new Handler();
        handler= new Handler();
        handlerPuntos = new Handler();
        handlerTiempo = new Handler();

        cont = 0;
        contEntreTiempo = 0;

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

                                    if (nombreUsuario.equals(nombreJ1)){
                                        tiempoAleatorio();
                                    }else {
                                        buscarTiempo();
                                    }
                                }
                            }
                        }
                    });
        }else {
            tiempoAleatorio();
        }
        descuento();
    }

    //El jugador 1 selecciona un tiempo aleatorio entre 7 y 12 segundos. Lo guarda en la bd para que el jugador 2 lo busque.
    public void tiempoAleatorio(){
        tiempoAleatorio = (int)(Math.random()*(12-7))+7;
        textTiempoBuscar.setText(tiempoAleatorio + ".00");

        if (batalla){
            HashMap<String, Object> hash = new HashMap<>();
            hash.put("tiempo", tiempoAleatorio);
            hash.put("estadoJ1", false);
            hash.put("estadoJ2", false);
            db.collection("juegoReloj").document(partidaId).set(hash, SetOptions.merge());
        }
    }

    //El jugador 2 busca el tiempo aleatorio elegido.
    public void buscarTiempo(){
        handlerTiempo.postDelayed(new Runnable() {
            public void run() {
                db.collection("juegoReloj").document(partidaId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        tiempoAleatorio = Integer.parseInt(String.valueOf(document.get("tiempo")));
                                        textTiempoBuscar.setText(tiempoAleatorio + ".00");
                                    }
                                }
                            }
                        });
                handlerTiempo.postDelayed(this, 10);
            }
        }, 10);
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
                botonSalir.setEnabled(true);
                botonParar.setVisibility(View.VISIBLE);
                botonParar.setEnabled(true);
                tiempo.setVisibility(View.INVISIBLE);
                textTiempoBuscar.setVisibility(View.VISIBLE);
                textoPararEn.setVisibility(View.VISIBLE);
                textoCronometro.setVisibility(View.VISIBLE);
                textRival.setVisibility(View.INVISIBLE);
                textTiempoRival.setVisibility(View.INVISIBLE);
                comenzarTiempo();

                if (batalla){
                    controlarSalidaOtroJugador();
                }
            }
        }.start();
    }

    Boolean parar = false;
    long milisegundos1;
    long milisegundos2;
    long miliseg = 0;
    int segundos = 0;
    //Comienza el cronómetro hasta que se puse el botón de parar.
    public void comenzarTiempo(){
        parar = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                milisegundos1 = System.currentTimeMillis();
                while(!parar) {
                    milisegundos2 = System.currentTimeMillis();
                    long diferencia = (milisegundos2 - milisegundos1);
                    if (diferencia >= 1){
                        miliseg++;
                    }
                    if (miliseg>=10){
                        segundos++;
                        miliseg = 0;
                    }

                    try {
                        sleep(99);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (segundos < 10){
                        textoCronometro.setText(segundos + "." + miliseg);
                    }else {
                        textoCronometro.setText(segundos + "." + miliseg);
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (segundos == 2 && miliseg == 0){
                                taparCronometro();
                            }
                            if (segundos == (tiempoAleatorio + 5) && miliseg == 0){
                                miliseg++;
                                pararTiempo();
                            }
                        }
                    });
                }
            }
        }).start();
    }

    //Tapa el cronómetro para no ver por dónde va contando.
    public void taparCronometro(){
        tapado = true;
        textTaparCronometro1.setVisibility(View.VISIBLE);
        Animation animacion1 = AnimationUtils.loadAnimation(this, R.anim.movimiento_hacia_derecha);
        textTaparCronometro1.setAnimation(animacion1);
        textTaparCronometro2.setVisibility(View.VISIBLE);
        Animation animacion2 = AnimationUtils.loadAnimation(this, R.anim.movimiento_hacia_izquierda);
        textTaparCronometro2.setAnimation(animacion2);
    }

    //Botón parar. En batalla guarda el tiempo en la bd y va a comparar. En práctica suma puntos.
    public void parar(View view){
        parar = true;
        handler.removeCallbacksAndMessages(null);
        textTaparCronometro1.setVisibility(View.INVISIBLE);
        textTaparCronometro2.setVisibility(View.INVISIBLE);
        botonParar.setEnabled(false);

        if (batalla){
            textComparar.setVisibility(View.VISIBLE);
            HashMap<String, Object> hash = new HashMap<>();
            if (nombreUsuario.equals(nombreJ1)){
                hash.put("segJ1", segundos);
                hash.put("milisecJ1", miliseg);
                hash.put("estadoJ1", true);
            }else {
                hash.put("segJ2", segundos);
                hash.put("milisecJ2", miliseg);
                hash.put("estadoJ2", true);
            }
            db.collection("juegoReloj").document(partidaId).set(hash, SetOptions.merge());
            compararPuntos();
        }else {
            if (segundos == tiempoAleatorio && miliseg == 0){
                sonidoAcierto.start();
                puntos = puntos + 100;
            }else if (segundos >= tiempoAleatorio){
                puntos = puntos - 20;
            }else {
                puntos = puntos + 50;
            }

            textPuntos.setText(String.valueOf(puntos));
            entreTiempo();
        }
    }

    //Se ejecuta cuando han pasado 5 segundos más del tiempo indicado. Para el cronómetro, guarda el tiempo y sigue.
    public void pararTiempo(){
        parar = true;
        handler.removeCallbacksAndMessages(null);
        textTaparCronometro1.setVisibility(View.INVISIBLE);
        textTaparCronometro2.setVisibility(View.INVISIBLE);
        botonParar.setEnabled(false);

        if (batalla){
            HashMap<String, Object> hash = new HashMap<>();
            if (nombreUsuario.equals(nombreJ1)){
                hash.put("segJ1", segundos);
                hash.put("milisecJ1", miliseg);
                hash.put("estadoJ1", true);
            }else {
                hash.put("segJ2", segundos);
                hash.put("milisecJ2", miliseg);
                hash.put("estadoJ2", true);
            }
            db.collection("juegoReloj").document(partidaId).set(hash, SetOptions.merge());
            entreTiempo();
        }else{
            entreTiempo();
        }
    }

    //Método para parar entre las veces que se juegan. Al jugar 3 veces se acaba.
    public void entreTiempo(){
        cronometroEntreTiempo = new CountDownTimer(2000, 1000) {
            public void onTick(long tiempoRestante) {

            }

            public void onFinish() {
                botonParar.setVisibility(View.INVISIBLE);
                botonSalir.setEnabled(false);
                textoCronometro.setVisibility(View.INVISIBLE);
                textTaparCronometro1.setVisibility(View.INVISIBLE);
                textTaparCronometro2.setVisibility(View.INVISIBLE);

                cont++;
                if (cont < 3){
                    segundos = 0;
                    miliseg = 0;
                    textoCronometro.setText(miliseg + "." + miliseg);
                    textTiempoBuscar.setVisibility(View.INVISIBLE);
                    textoPararEn.setVisibility(View.INVISIBLE);
                    if (batalla){
                        if (nombreUsuario.equals(nombreJ1)){
                            tiempoAleatorio();
                        }else {
                            buscarTiempo();
                        }
                    }else {
                        tiempoAleatorio();
                    }

                    descuento();
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

    //Busca si el otro jugador ya ha guardado el tiempo, lo compara y suma puntos.
    public void compararPuntos(){
        contEntreTiempo = 0;
        handlerPuntos.postDelayed(new Runnable() {
            public void run() {
                db.collection("juegoReloj").document(partidaId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            if (nombreUsuario.equals(nombreJ1)){
                                                estado = (Boolean)document.get("estadoJ2");
                                            }else {
                                                estado = (Boolean)document.get("estadoJ1");
                                            }
                                            if (estado){
                                                handlerPuntos.removeCallbacksAndMessages(null);

                                                if (nombreUsuario.equals(nombreJ1)){
                                                    segOtroJugador = Integer.parseInt(String.valueOf(document.get("segJ2")));
                                                    milisegOtroJugador = Integer.parseInt(String.valueOf(document.get("milisecJ2")));
                                                }else {
                                                    segOtroJugador = Integer.parseInt(String.valueOf(document.get("segJ1")));
                                                    milisegOtroJugador = Integer.parseInt(String.valueOf(document.get("milisecJ1")));
                                                }

                                                textTiempoRival.setText(segOtroJugador + "." + milisegOtroJugador);
                                                textRival.setVisibility(View.VISIBLE);
                                                textTiempoRival.setVisibility(View.VISIBLE);

                                                if (segundos == tiempoAleatorio && miliseg == 0){
                                                    sonidoAcierto.start();
                                                    puntos = puntos + 100;
                                                }else if (segundos >= tiempoAleatorio){
                                                    puntos = puntos - 20;
                                                }else {
                                                    if (segOtroJugador != -1 && milisegOtroJugador != -1) {

                                                        if (segOtroJugador == tiempoAleatorio && milisegOtroJugador == 0) {

                                                        }else if (segOtroJugador == (tiempoAleatorio+5) && milisegOtroJugador == 1){
                                                            puntos = puntos + 50;
                                                        }else if (segOtroJugador >= tiempoAleatorio) {
                                                            puntos = puntos + 70;
                                                        }else {
                                                            diferenciaSeg = segundos - segOtroJugador;
                                                            diferenciaMilis = miliseg - milisegOtroJugador;

                                                            if (diferenciaSeg > 0) {
                                                                puntos = puntos + 50;
                                                            }else if (diferenciaSeg == 0 && diferenciaMilis == 0) {
                                                                puntos = puntos + 25;
                                                            }else if (diferenciaSeg == 0 && diferenciaMilis > 0){
                                                                puntos = puntos + 50;
                                                            }
                                                        }
                                                    }
                                                }
                                                if (contEntreTiempo == 0){
                                                    textComparar.setVisibility(View.INVISIBLE);
                                                    textPuntos.setText(String.valueOf(puntos));
                                                    contEntreTiempo++;
                                                    entreTiempo();
                                                }
                                            }
                                        }
                                    }
                                }
                        });
                handlerPuntos.postDelayed(this, 500);
            }
        }, 500);
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
                textPuntos.setText(String.valueOf(puntos));
                segundos = 0;
                miliseg = 0;
                textoCronometro.setText(segundos + "." + miliseg);
                cont = 0;
                parar = false;
                botonSalir.setEnabled(false);
                botonParar.setVisibility(View.INVISIBLE);
                if (batalla){
                    if (nombreUsuario.equals(nombreJ1)){
                        tiempoAleatorio();
                    }else {
                        buscarTiempo();
                    }
                }else {
                    tiempoAleatorio();
                }
                descuento();
                dialog.dismiss();
            }
        });
        Button dialogButtonSalir = v.findViewById(R.id.botonSalir);
        dialogButtonSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelarHilos();
                parar = true;
                dialog.dismiss();
                finish();
            }
        });
    }

    //Método al terminar las 3 partidas en modo BATALLA. Guarda los puntos en la bd y termina.
    public void terminarBatalla(){
        cancelarHilos();
        HashMap<String, Object> dato = new HashMap<>();
        if (nombreUsuario.equals(nombreJ1)){
            dato.put("puntosJ1", puntos);
        }else {
            dato.put("puntosJ2", puntos);
        }

        db.collection("partidasActivas").document(partidaId).set(dato, SetOptions.merge());
        db.collection("juegoReloj").document(partidaId).delete();
        finish();
    }

    //Botón salir.
    public void salir(View view){
        parar = true;
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
        db.collection("juegoReloj").document(partidaId).delete();
        parar = true;
        cancelarHilos();
        this.finish();
    }

    //Controla si el otro jugador a salido de la partida.
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
                                        parar = true;
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
        if (cronometroEntreTiempo != null){
            cronometroEntreTiempo.cancel();
        }
        if (descuento != null){
            descuento.cancel();
        }
        if (handlerControl != null){
            handlerControl.removeCallbacksAndMessages(null);
        }

        if (handler != null){
            handler.removeCallbacksAndMessages(null);
        }
        if (handlerPuntos != null){
            handlerPuntos.removeCallbacksAndMessages(null);
        }
        if (handlerTiempo != null){
            handlerTiempo.removeCallbacksAndMessages(null);
        }
    }
}