package com.example.batallademinijuegos.minijuegos;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

public class ActivityJuegoGlobos extends AppCompatActivity implements View.OnClickListener{
    ConstraintLayout layout;
    MediaPlayer sonidoExplotar;
    MediaPlayer sonidoBeep;
    MediaPlayer sonidoSilbato;
    MediaPlayer sonidoTicTac;

    TextView tiempo;
    TextView texto;
    TextView textPuntos;
    TextView textoCronometro;
    Button botonSalir;

    Boolean salir = false;
    int puntos;
    Boolean finJuego;
    int aumento;

    Handler handler= null;
    Handler handlerDesaparecer = null;
    Handler handlerControl = null;
    CountDownTimer cronometro = null;
    CountDownTimer descuento = null;

    FirebaseFirestore db;
    Boolean batalla;
    String nombreUsuario;
    String partidaId;
    String nombreJ1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_globos);

        batalla = getIntent().getBooleanExtra("batalla", false);
        partidaId = getIntent().getStringExtra("partidaId");
        nombreJ1 = getIntent().getStringExtra("nombreJ1");

        db = FirebaseFirestore.getInstance();

        layout = findViewById(R.id.layout);

        sonidoExplotar = MediaPlayer.create(this, R.raw.pinchar_globo);
        sonidoBeep = MediaPlayer.create(this, R.raw.beep_tiempo);
        sonidoSilbato = MediaPlayer.create(this, R.raw.sonido_silbato);
        sonidoTicTac = MediaPlayer.create(this, R.raw.tictac);

        tiempo = findViewById(R.id.textTiempo);
        texto = findViewById(R.id.textGlobos);
        textPuntos = findViewById(R.id.textPuntos);
        textoCronometro = findViewById(R.id.textCronometro);
        botonSalir = findViewById(R.id.boton_salir);

        handler = new Handler();
        handlerDesaparecer = new Handler();
        handlerControl = new Handler();

        textoCronometro.setText("00:30");
        finJuego = false;
        puntos = 0;
        aumento = 0;

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
        texto.setVisibility(View.VISIBLE);
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
                        texto.setVisibility(View.INVISIBLE);

                        finJuego = false;
                        sacarGlobos();
                        break;
                }
            }

            public void onFinish() {
                botonSalir.setEnabled(true);
                tiempo.setVisibility(View.INVISIBLE);
                comenzarTiempo();
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
                    case 10:
                        aumento = 750;
                        sacarGlobos();
                        break;
                    case 5:
                        sonidoTicTac.start();
                        break;
                    case 0:
                        sonidoSilbato.start();
                        break;
                }
            }

            public void onFinish() {
                finJuego = true;
                if (handler != null){
                    handler.removeCallbacksAndMessages(null);
                }
                if (batalla){
                    terminarBatalla();
                }else {
                    terminarPractica();
                }
            }

        }.start();
    }

    //Hilo para crear globos durante el juego.
    public void sacarGlobos(){
        handler.postDelayed(new Runnable() {
            public void run() {
                crearGlobo();
                crearGlobo();

                handler.postDelayed(this, 500);
            }
        }, 500);
    }

    //Crea imagen_globos aleatorios
    public void crearGlobo(){
        ImageView imagen = new ImageView(this);
        int color = colorAleatorio();

        imagen.setImageResource(color);
        imagen.setX(posicionAleatoria());
        imagen.setY(layout.getHeight()+imagen.getHeight());
        imagen.setOnClickListener(this);
        imagen.setId(color);
        layout.addView(imagen);

        mover(imagen);
        desaparecer(imagen);
    }

    //Método para mover los globos con una determinada velocidad.
    public void mover(ImageView imagen){
        int velocidad = 0;
        String color = saberColor(imagen);

        if (color.equals("black")){
            velocidad = 6000;
        }else if (color.equals("blue") || color.equals("red") || color.equals("orange")){
            velocidad = 4000;
        }else if (color.equals("green") || color.equals("purple")){
            velocidad = 3000;
        }else{
            velocidad = 2000;
        }

        ObjectAnimator animationX = ObjectAnimator.ofFloat(imagen, "translationX", direccionAleatoria());
        animationX.setDuration(velocidad - aumento);
        animationX.start();

        ObjectAnimator animationY = ObjectAnimator.ofFloat(imagen, "translationY", -1000);
        animationY.setDuration(velocidad - aumento);
        animationY.start();
    }

    //Método para eliminar los globos cuando salen de la pantalla.
    public void desaparecer(final ImageView imagen){
        handlerDesaparecer.postDelayed(new Runnable() {
            public void run() {
                if (imagen.getY() < -500){
                    layout.removeView(imagen);
                }
                handlerDesaparecer.postDelayed(this, 100);
            }
        }, 100);
    }

    //Método para colocar los globos en una posición aleatoria cuando salen.
    public int posicionAleatoria(){
        return (int)(Math.random()*layout.getWidth())+1;
    }

    //Método que da a los imagen_globos un color aleatorio.
    public int colorAleatorio(){
        int[] coloresGlobos = {R.drawable.black, R.drawable.blue, R.drawable.green, R.drawable.orange, R.drawable.purple,R.drawable.red, R.drawable.yellow, R.drawable.blue, R.drawable.green, R.drawable.orange, R.drawable.purple,R.drawable.red, R.drawable.yellow};
        int aleatorio = (int)(Math.random()*coloresGlobos.length);

        return coloresGlobos[aleatorio];
    }

    //Método para mover el globo en una dirección aleatoria (izquierda o derecha, siempre hacia arriba).
    public int direccionAleatoria(){
        int aleatorio = (int)(Math.random()*(layout.getWidth()+600)-300);

        return aleatorio;
    }

    //Método onClick de los globos. Al pulsar el globo, desaparece y se suman puntos.
    @Override
    public void onClick(View view) {
        ImageView imagen = (ImageView) view;
        String color = saberColor(imagen);

        sonidoExplotar.start();
        layout.removeView(imagen);

        //si finJuego = true no suma los puntos
        if (!finJuego) {
            if (color.equals("black")) {
                puntos = puntos - 3;
            } else if (color.equals("yellow")) {
                puntos = puntos + 5;
            } else if(color.equals(("green")) || color.equals("purple")) {
                puntos = puntos + 3;
            }else {
                puntos++;
            }
            textPuntos.setText(String.valueOf(puntos));
        }
    }

    //Método para saber el color del globo y así poder sumar sus puntos y darle una velocidad.
    public String saberColor(ImageView imagen){
        String[] partes = imagen.toString().split("/");
        String parte = partes[1];
        String color = parte.substring(0, parte.length()-1);

        return color;
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
                puntos = 0;
                textPuntos.setText(String.valueOf(puntos));
                textoCronometro.setText("00:30");
                aumento = 0;
                botonSalir.setEnabled(false);
                descuento();
                dialog.dismiss();
            }
        });
        Button dialogButtonSalir = v.findViewById(R.id.botonSalir);
        dialogButtonSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finJuego = false;
                aumento = 0;
                cancelarHilos();
                dialog.dismiss();
                finish();
            }
        });
    }

    //Se ejecuta al terminar los 30 segundos de juego en modo BATALLA. Guarda los puntos en la bd y termina.
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
        aumento = 0;

        if (batalla){
            salir = true;
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
                                        if (!salir) {
                                            cancelarHilos();
                                            finish();
                                        }
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
        if (handlerDesaparecer != null){
            handlerDesaparecer.removeCallbacksAndMessages(null);
        }
        if (handlerControl != null){
            handlerControl.removeCallbacksAndMessages(null);
        }
        if (cronometro != null){
            cronometro.cancel();
        }
        if (descuento != null){
            descuento.cancel();
        }
    }

}