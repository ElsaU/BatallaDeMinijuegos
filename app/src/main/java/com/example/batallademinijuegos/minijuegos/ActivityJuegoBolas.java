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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.batallademinijuegos.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;

public class ActivityJuegoBolas extends AppCompatActivity {
    TextView tiempo;
    TextView textPuntos;
    TextView textoCronometro;
    Button botonSalir;
    ImageView bomba1;
    ImageView bomba2;
    ImageView bomba3;
    ImageView bomba4;
    ImageView nave;
    ImageView meta;
    ImageButton botonMoverArriba;

    MediaPlayer sonidoBeep;
    MediaPlayer sonidoSilbato;
    MediaPlayer sonidoExplosion;

    FirebaseFirestore db;
    Boolean batalla;
    String nombreUsuario;
    String partidaId;
    String nombreJ1;

    CountDownTimer descuento = null;
    CountDownTimer cronometro = null;
    CountDownTimer cronometroEntreTiempo = null;
    Handler handler;
    Handler handlerControl;
    Handler handlerMeta;

    ConstraintLayout layout;

    int puntos;
    int ancho = 0;
    int alto = 0;
    int direccionX1 = 0;
    int direccionX2 = 0;
    int direccionX3 = 0;
    int direccionX4 = 0;
    float originalX = 0;
    float originalY = 0;
    Boolean inicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_juego_bolas);

        tiempo = findViewById(R.id.textTiempo);
        textPuntos = findViewById(R.id.textPuntos);
        textoCronometro = findViewById(R.id.textCronometro);
        botonSalir = findViewById(R.id.boton_salir);
        bomba1 = findViewById(R.id.imagenBomba1);
        bomba2 = findViewById(R.id.imagenBomba2);
        bomba3 = findViewById(R.id.imagenBomba3);
        bomba4 = findViewById(R.id.imagenBomba4);
        nave = findViewById(R.id.imagenNave);
        meta = findViewById(R.id.imagenMeta);
        botonMoverArriba = findViewById(R.id.botonMoverArriba);

        sonidoBeep = MediaPlayer.create(this, R.raw.beep_tiempo);
        sonidoSilbato = MediaPlayer.create(this, R.raw.sonido_silbato);
        sonidoExplosion = MediaPlayer.create(this, R.raw.sonido_explosion);

        batalla = getIntent().getBooleanExtra("batalla", false);
        partidaId = getIntent().getStringExtra("partidaId");
        nombreJ1 = getIntent().getStringExtra("nombreJ1");

        db = FirebaseFirestore.getInstance();
        layout = findViewById(R.id.constraintLayout);

        handler = new Handler();
        handlerControl = new Handler();
        handlerMeta = new Handler();

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

            HashMap<String, Object> hash = new HashMap<>();
            hash.put("estadoJ1", false);
            hash.put("estadoJ2", false);
            db.collection("juegoBombas").document(partidaId).set(hash, SetOptions.merge());
        }

        puntos = 100;
        textPuntos.setText(String.valueOf(puntos));
        inicio = true;
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
                        sonidoSilbato.start();
                        tiempo.setTextSize(60);
                        tiempo.setText(R.string.textoComienza);
                        tiempo.setTextColor(Color.YELLOW);

                        break;
                }
            }

            public void onFinish() {
                botonSalir.setEnabled(true);
                tiempo.setVisibility(View.INVISIBLE);
                bomba1.setVisibility(View.VISIBLE);
                bomba2.setVisibility(View.VISIBLE);
                bomba3.setVisibility(View.VISIBLE);
                bomba4.setVisibility(View.VISIBLE);
                botonMoverArriba.setVisibility(View.VISIBLE);
                nave.setVisibility(View.VISIBLE);
                meta.setVisibility(View.VISIBLE);
                if (batalla){
                    controlarSalidaOtroJugador();
                    controlarMetaOtroJugador();
                }
                ancho = layout.getWidth();
                alto = layout.getHeight();
                if (inicio){
                    originalX = nave.getX();
                    originalY = nave.getY();
                    inicio = false;
                }

                moverBombas();
                comenzarTiempo();
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
                    case 0:
                        sonidoSilbato.start();
                        botonMoverArriba.setEnabled(false);
                        break;
                }
            }

            public void onFinish() {
                handler.removeCallbacksAndMessages(null);
                if (batalla){
                    terminarBatalla(false);
                }else {
                    terminarPractica();
                }
            }

        }.start();
    }

    public void moverBombas(){
        direccionX1 = direccionAleatoriaX(1);
        direccionX2 = direccionAleatoriaX(2);
        direccionX3 = direccionAleatoriaX(3);
        direccionX4 = direccionAleatoriaX(4);

        handler.postDelayed(new Runnable() {
            public void run() {
                bomba1.setX(bomba1.getX()+direccionX1);
                direccionX1 = rebotar(bomba1, direccionX1);
                chocar(bomba1);

                bomba2.setX(bomba2.getX()+direccionX2);
                direccionX2 = rebotar(bomba2, direccionX2);
                chocar(bomba2);

                bomba3.setX(bomba3.getX()+direccionX3);
                direccionX3 = rebotar(bomba3, direccionX3);
                chocar(bomba3);

                bomba4.setX(bomba4.getX()+direccionX4);
                direccionX4 = rebotar(bomba4, direccionX4);
                chocar(bomba4);

                handler.postDelayed(this, 5);
            }
        }, 5);

    }

    //Método para mover la bomba en una dirección aleatoria del eje X.
    public int direccionAleatoriaX(int numBomba){
        int[] direccion;
        switch (numBomba){
            case 1:
                direccion = new int[]{-7, 7};
                break;
            case 2:
                direccion = new int[]{-8, 8};
                break;
            case 3:
                direccion = new int[]{-10, 10};
                break;
            case 4:
                direccion = new int[]{-13, 13};
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + numBomba);
        }
        int aleatorio = (int)(Math.random()*2);

        return direccion[aleatorio];
    }

    public int rebotar(ImageView bomba, int direccionX){
        if (bomba.getX() < 0 ||(bomba.getX()+bomba.getWidth()) > ancho){
            direccionX = -direccionX;
        }
        return direccionX;
    }

    public void moverNave(View view){
        nave.setY(nave.getY()-(bomba1.getHeight()/2));

        //Si pasas la meta
        if ((nave.getY()+nave.getHeight()) <= (meta.getY()+meta.getHeight())){
            cancelarHilos();
            sonidoSilbato.start();
            if (batalla){
                HashMap<String, Object> hash = new HashMap<>();
                if (nombreUsuario.equals(nombreJ1)){
                    hash.put("estadoJ1", true);
                }else {
                    hash.put("estadoJ2", true);
                }
                db.collection("juegoBombas").document(partidaId).set(hash, SetOptions.merge());
                entreTiempo(true);
            }else{
                entreTiempo(false);
            }
        }
    }

    //
    public void entreTiempo(final Boolean ganar){
        cronometroEntreTiempo = new CountDownTimer(2000, 1000) {
            public void onTick(long tiempoRestante) {

            }

            public void onFinish() {
                if (batalla){
                    if (ganar){
                        terminarBatalla(true);
                    }else {
                        terminarBatalla(false);
                    }
                }else {
                    terminarPractica();
                }

            }
        }.start();
    }

    public void chocar(ImageView bomba){
        if (nave.getY() <= (bomba.getY()+bomba.getHeight()) && nave.getY() >= bomba.getY() || (nave.getY()+nave.getHeight()) <= (bomba.getY()+nave.getHeight()) && (nave.getY()+nave.getHeight()) >= bomba.getY()){
            if (nave.getX() >= bomba.getX() && nave.getX() <= (bomba.getX()+bomba.getWidth()) || (nave.getX()+nave.getWidth()) >= bomba.getX() && (nave.getX()+nave.getWidth()) <= (bomba.getX()+bomba.getWidth())){
                sonidoExplosion.start();
                nave.setX(originalX);
                nave.setY(originalY);
                if (puntos != 0){
                    puntos = puntos - 10;
                    textPuntos.setText(String.valueOf(puntos));
                }
            }
        }
    }

    //Método que se ejecuta el terminar los 30 segundos de juego en modo PRACTICA. Muestra un alertDialog con los puntos obtenidos y la opción de salir o volver a jugar.
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
                puntos = 100;
                textPuntos.setText(String.valueOf(puntos));
                textoCronometro.setText("00:30");
                botonSalir.setEnabled(false);
                botonMoverArriba.setEnabled(true);
                bomba1.setVisibility(View.INVISIBLE);
                bomba2.setVisibility(View.INVISIBLE);
                bomba3.setVisibility(View.INVISIBLE);
                bomba4.setVisibility(View.INVISIBLE);
                nave.setVisibility(View.INVISIBLE);
                meta.setVisibility(View.INVISIBLE);
                botonMoverArriba.setVisibility(View.INVISIBLE);
                nave.setX(originalX);
                nave.setY(originalY);
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

    //Se ejecuta al terminar los 30 segundos de juego en modo BATALLA. Guarda los puntos en la bd y termina.
    public void terminarBatalla(Boolean ganar){
        cancelarHilos();
        if (!ganar){
            puntos = 0;
        }else {
            if (puntos == 0){
                puntos = 10;
            }
        }
        HashMap<String, Object> dato = new HashMap<>();
        if (nombreJ1.equals(nombreUsuario)){
            dato.put("puntosJ1", puntos);
        }else {
            dato.put("puntosJ2", puntos);
        }

        db.collection("partidasActivas").document(partidaId).set(dato, SetOptions.merge());
        if (nombreUsuario.equals(nombreJ1)){
            db.collection("juegoBombas").document(partidaId).delete();
        }
        finish();
    }

    //Controla si el otro jugador ha llegado a la meta.
    public void controlarMetaOtroJugador(){
        handlerMeta.postDelayed(new Runnable() {
            public void run() {
                db.collection("juegoBombas").document(partidaId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Boolean estado;
                                        if (nombreUsuario.equals(nombreJ1)){
                                            estado = (Boolean) document.get("estadoJ2");
                                        }else {
                                            estado = (Boolean) document.get("estadoJ1");
                                        }

                                        if (estado){
                                            cancelarHilos();
                                            sonidoSilbato.start();
                                            entreTiempo(false);
                                        }
                                    }
                                }
                            }
                        });
                handlerMeta.postDelayed(this, 500);
            }
        }, 500);
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
        db.collection("juegoBombas").document(partidaId).delete();
        this.finish();
    }

    //Controla si el otro jugador sale de la partida.
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
        if (handler != null){
            handler.removeCallbacksAndMessages(null);
        }
        if (handlerControl != null){
            handlerControl.removeCallbacksAndMessages(null);
        }
        if (handlerMeta != null){
            handlerMeta.removeCallbacksAndMessages(null);
        }
        if (cronometro != null){
            cronometro.cancel();
        }
        if (cronometroEntreTiempo != null){
            cronometroEntreTiempo.cancel();
        }
        if (descuento != null){
            descuento.cancel();
        }
    }
}