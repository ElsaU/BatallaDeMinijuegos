package com.example.batallademinijuegos.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.batallademinijuegos.R;
import com.example.batallademinijuegos.minijuegos.ActivityJuegoBolas;
import com.example.batallademinijuegos.minijuegos.ActivityJuegoGlobos;
import com.example.batallademinijuegos.minijuegos.ActivityJuegoPulsaciones;
import com.example.batallademinijuegos.minijuegos.ActivityJuegoReloj;
import com.example.batallademinijuegos.minijuegos.ActivityJuegoSonidos;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.annotation.Nullable;

public class ActivityBatalla extends AppCompatActivity {
    TextView textNombreJ1;
    TextView textNombreJ2;
    TextView textCronometro;
    TextView textComenzando;
    TextView puntos1;
    TextView puntos2;
    TextView puntos3;
    TextView puntos4;
    TextView puntos5;
    TextView puntos6;
    ImageView imagenJuego1;
    ImageView imagenJuego2;
    ImageView imagenJuego3;
    TextView pTotales1;
    TextView pTotales2;
    TextView textGanarPerder;
    Button botonTerminar;

    FirebaseFirestore db;
    DocumentReference docRef;

    Handler handler= null;
    Handler handler2 = null;

    String email = "";
    String nombreUsuario = "";
    int puntuacionMax = 0;
    String nombreJ1 = "";
    String nombreJ2 = "";
    String partidaId;
    Boolean salir = false;
    int p1 = 0;
    int p2 = 0;
    int puntosFinalesEsteJugador = 0;
    int puntosFinalesOtroJugador = 0;
    int vecesJugadas = -1;
    Boolean estado = false;

    ArrayList<Integer> listaTodosLosJuegos = new ArrayList<>();
    ArrayList<Integer> listaJuegosAJugar = new ArrayList<>();

    CountDownTimer descuento = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batalla);

        partidaId = getIntent().getStringExtra("partidaID");

        textNombreJ1 = findViewById(R.id.textNombreJ1);
        textNombreJ2 = findViewById(R.id.textNombreJ2);
        textCronometro = findViewById(R.id.textCronom);
        textComenzando = findViewById(R.id.textComenzando);
        puntos1 = findViewById(R.id.textPuntos1);
        puntos2 = findViewById(R.id.textPuntos2);
        puntos3 = findViewById(R.id.textPuntos3);
        puntos4 = findViewById(R.id.textPuntos4);
        puntos5 = findViewById(R.id.textPuntos5);
        puntos6 = findViewById(R.id.textPuntos6);
        imagenJuego1 = findViewById(R.id.imageViewJuego1);
        imagenJuego2 = findViewById(R.id.imageViewJuego2);
        imagenJuego3 = findViewById(R.id.imageViewJuego3);
        pTotales1 = findViewById(R.id.textPTotal1);
        pTotales2 = findViewById(R.id.textPTotal2);
        textGanarPerder = findViewById(R.id.textGanarPerder);
        botonTerminar = findViewById(R.id.botonTerminar);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        email = user.getEmail();

        handler = new Handler();
        handler2 = new Handler();

        db.collection("usuarios").document(email).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                nombreUsuario = document.getString("nombreUsuario");
                                puntuacionMax = Integer.parseInt(String.valueOf(document.get("puntuacionMasAlta")));

                            }
                        }
                    }
                }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                colocarNombres();
            }
        });

        controlarSalidaOtroJugador();
        mostrarPuntuaciones();

    }

    //Coloca los nombres de los jugadores. El jugador local siempre se muestra a la izquierda.
    public void colocarNombres(){
        db.collection("partidasActivas").document(partidaId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                nombreJ1 = document.get("jugador1").toString();
                                nombreJ2 = document.get("jugador2").toString();

                                if (nombreUsuario.equals(nombreJ1)) {
                                    textNombreJ1.setText(nombreJ1);
                                    textNombreJ2.setText(nombreJ2);
                                } else {
                                    textNombreJ1.setText(nombreJ2);
                                    textNombreJ2.setText(nombreJ1);
                                }

                                HashMap<String, Object> hash = new HashMap<>();
                                if (nombreUsuario.equals(nombreJ1)){
                                    elegirJuegos();
                                    colocarImagenesJuegos();
                                    hash.put("estadoJ1", true);
                                    db.collection("partidasActivas").document(partidaId).set(hash, SetOptions.merge());
                                }else {
                                    hash.put("estadoJ2", true);
                                    db.collection("partidasActivas").document(partidaId).set(hash, SetOptions.merge());
                                }
                                comprobarEstadoOtroJugador();
                            }
                        }
                    }
                });
    }

    //Controla si el otro jugador sale de la partida.
    public void controlarSalidaOtroJugador(){
        handler.postDelayed(new Runnable() {
            public void run() {
                db.collection("partidasActivas").document(partidaId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (!document.exists()) {
                                        if (!salir){
                                            if (descuento != null){
                                                descuento.cancel();
                                            }
                                            if (handler != null){
                                                handler.removeCallbacksAndMessages(null);
                                            }
                                            if (handler2 != null){
                                                handler2.removeCallbacksAndMessages(null);
                                            }
                                            cancelarBatalla();
                                        }
                                    }
                                }
                            }
                        });
                handler.postDelayed(this, 500);
            }
        }, 500);
    }

    //Abre un alertDialog si el otro jugador ha salido de la partida. Se acaba la partida sin guardar resultados.
    public void cancelarBatalla(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final Context context = alert.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View v = inflater.inflate(R.layout.batalla_cancelada, null, false);
        alert.setView(v).
                setCancelable(false);

        final AlertDialog dialog = alert.show();
        Button dialogButtonVolverAJugar = v.findViewById(R.id.botonAceptar);
        dialogButtonVolverAJugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
    }

    //Tiempo en descuento para comenzar los minijuegos. Al final de la batalla muestra el ganador.
    public void descuento(final boolean fin, final int tiempo){
        handler2.removeCallbacksAndMessages(null);
        textComenzando.setText(R.string.etiq_cargar_minijuego);

        if (nombreUsuario.equals(nombreJ2)){
            buscarJuegos();
        }

        descuento = new CountDownTimer(tiempo, 1000) {
            public void onTick(long tiempoRestante) {
                if (!fin){
                    textCronometro.setText("00:0" + tiempoRestante / 1000);
                }
            }

            public void onFinish() {
                if (!fin){
                    jugar();
                }else {
                    terminarBatalla();
                }
            }
        }.start();
    }

    //El jugador 1 elige los minijuegos aleatoriamente, se guardan en la bd.
    public void elegirJuegos(){
        listaTodosLosJuegos.add(0);
        listaTodosLosJuegos.add(1);
        listaTodosLosJuegos.add(2);
        listaTodosLosJuegos.add(3);
        listaTodosLosJuegos.add(4);
        Collections.shuffle(listaTodosLosJuegos);

        listaJuegosAJugar.add(listaTodosLosJuegos.get(0));
        listaJuegosAJugar.add(listaTodosLosJuegos.get(1));
        listaJuegosAJugar.add(listaTodosLosJuegos.get(2));

        HashMap<String, Object> hash = new HashMap<>();
        hash.put("juegos", listaJuegosAJugar);
        hash.put("estadoJ1", true);
        db.collection("partidasActivas").document(partidaId).set(hash, SetOptions.merge());
    }

    //Muestra las imagenes de los juegos que se van a jugar durante la partida.
    public void colocarImagenesJuegos(){
        for (int i=0; i<listaJuegosAJugar.size(); i++){
            switch (i){
                case 0:
                    if (listaJuegosAJugar.get(i) == 0){
                        imagenJuego1.setImageResource(R.drawable.imagen_globos);
                    }else if (listaJuegosAJugar.get(i) == 1){
                        imagenJuego1.setImageResource(R.drawable.imagen_sonidos);
                    }else if (listaJuegosAJugar.get(i) == 2){
                        imagenJuego1.setImageResource(R.drawable.imagen_pulsaciones);
                    }else if (listaJuegosAJugar.get(i) == 3){
                        imagenJuego1.setImageResource(R.drawable.imagen_reloj);
                    }else if (listaJuegosAJugar.get(i) == 4){
                        imagenJuego1.setImageResource(R.drawable.imagen_bombas);
                    }
                    break;
                case 1:
                    if (listaJuegosAJugar.get(i) == 0){
                        imagenJuego2.setImageResource(R.drawable.imagen_globos);
                    }else if (listaJuegosAJugar.get(i) == 1){
                        imagenJuego2.setImageResource(R.drawable.imagen_sonidos);
                    }else if (listaJuegosAJugar.get(i) == 2){
                        imagenJuego2.setImageResource(R.drawable.imagen_pulsaciones);
                    }else if (listaJuegosAJugar.get(i) == 3){
                        imagenJuego2.setImageResource(R.drawable.imagen_reloj);
                    }else if (listaJuegosAJugar.get(i) == 4){
                        imagenJuego2.setImageResource(R.drawable.imagen_bombas);
                    }
                    break;
                case 2:
                    if (listaJuegosAJugar.get(i) == 0){
                        imagenJuego3.setImageResource(R.drawable.imagen_globos);
                    }else if (listaJuegosAJugar.get(i) == 1){
                        imagenJuego3.setImageResource(R.drawable.imagen_sonidos);
                    }else if (listaJuegosAJugar.get(i) == 2){
                        imagenJuego3.setImageResource(R.drawable.imagen_pulsaciones);
                    }else if (listaJuegosAJugar.get(i) == 3){
                        imagenJuego3.setImageResource(R.drawable.imagen_reloj);
                    }else if (listaJuegosAJugar.get(i) == 4){
                        imagenJuego3.setImageResource(R.drawable.imagen_bombas);
                    }
                    break;
            }
        }
    }

    //El jugador 2 busca los juegos seleccionados aleatoriamente por el jugador 1.
    ArrayList<Integer> lista;
    public void buscarJuegos(){
        db.collection("partidasActivas").document(partidaId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                if (document.get("juegos") != null){
                                    lista = (ArrayList<Integer>) document.get("juegos");

                                    for (int i = 0; i<lista.size(); i++){
                                        listaJuegosAJugar.add(Integer.parseInt(String.valueOf(lista.get(i))));
                                        colocarImagenesJuegos();
                                    }
                                }
                            }
                        }
                    }
                });
    }

    //Comienzan los minijuegos en el orden aleatorio seleccionado.
    public void jugar(){
        HashMap<String, Object> hash = new HashMap<>();
        if (nombreUsuario.equals(nombreJ1)){
            hash.put("estadoJ1", false);
            db.collection("partidasActivas").document(partidaId).set(hash, SetOptions.merge());
        }else {
            hash.put("estadoJ2", false);
            db.collection("partidasActivas").document(partidaId).set(hash, SetOptions.merge());
        }

        Intent i = null;

        if (vecesJugadas >= 0){
            switch (listaJuegosAJugar.get(vecesJugadas)){
                case 0:
                    i= new Intent(this, ActivityJuegoGlobos.class);
                    break;
                case 1:
                    i = new Intent(this, ActivityJuegoSonidos.class);
                    break;
                case 2:
                    i = new Intent(this, ActivityJuegoPulsaciones.class);
                    break;
                case 3:
                    i = new Intent(this, ActivityJuegoReloj.class);
                    break;
                case 4:
                    i = new Intent(this, ActivityJuegoBolas.class);
                    break;
            }

            i.putExtra("batalla", true);
            i.putExtra("partidaId", partidaId);
            i.putExtra("nombreJ1", nombreJ1);
            startActivity(i);
        }
    }

    //Comprobación de que el otro jugador está al mismo tiempo.
    public void comprobarEstadoOtroJugador(){
        handler2.postDelayed(new Runnable() {
            public void run() {
                db.collection("partidasActivas").document(partidaId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        if (nombreUsuario.equals(nombreJ1)){
                                            estado = (Boolean) document.get("estadoJ2");
                                        }else {
                                            estado = (Boolean) document.get("estadoJ1");
                                        }

                                        if (estado){
                                            descuento(false, 6000);
                                        }
                                    }
                                }
                            }
                        });
                handler2.postDelayed(this, 500);
            }
        }, 500);
    }

    //Busca las puntuaciones guardadas en la bd en cada minijuego y las muestra. Según pasan los minijuegos se van sumando.
    public void mostrarPuntuaciones(){
        final DocumentReference docRef = db.collection("partidasActivas").document(partidaId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    alert("Ha ocurrido un error.");
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    db.collection("partidasActivas").document(partidaId).get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            if ((Boolean)document.get("estadoJ1") == true && (Boolean)document.get("estadoJ2") == true) {
                                                p1 = Integer.parseInt(document.get("puntosJ1").toString());
                                                p2 = Integer.parseInt(document.get("puntosJ2").toString());

                                                switch (vecesJugadas) {
                                                    case 1:
                                                        if (nombreUsuario.equals(nombreJ1)) {
                                                            puntos1.setText(String.valueOf(p1));
                                                            puntos2.setText(String.valueOf(p2));
                                                        } else {
                                                            puntos1.setText(String.valueOf(p2));
                                                            puntos2.setText(String.valueOf(p1));
                                                        }
                                                        puntosFinalesEsteJugador = Integer.parseInt((String) puntos1.getText());
                                                        puntosFinalesOtroJugador = Integer.parseInt((String) puntos2.getText());

                                                        break;
                                                    case 2:
                                                        if (nombreUsuario.equals(nombreJ1)) {
                                                            puntos3.setText(String.valueOf(p1));
                                                            puntos4.setText(String.valueOf(p2));
                                                        } else {
                                                            puntos3.setText(String.valueOf(p2));
                                                            puntos4.setText(String.valueOf(p1));
                                                        }

                                                        puntosFinalesEsteJugador = Integer.parseInt((String) puntos1.getText()) + Integer.parseInt((String) puntos3.getText());
                                                        puntosFinalesOtroJugador = Integer.parseInt((String) puntos2.getText()) + Integer.parseInt((String) puntos4.getText());

                                                        break;
                                                    case 3:
                                                        if (nombreUsuario.equals(nombreJ1)) {
                                                            puntos5.setText(String.valueOf(p1));
                                                            puntos6.setText(String.valueOf(p2));
                                                        } else {
                                                            puntos5.setText(String.valueOf(p2));
                                                            puntos6.setText(String.valueOf(p1));
                                                        }

                                                        puntosFinalesEsteJugador = Integer.parseInt((String) puntos1.getText()) + Integer.parseInt((String) puntos3.getText()) + Integer.parseInt((String) puntos5.getText());
                                                        puntosFinalesOtroJugador = Integer.parseInt((String) puntos2.getText()) + Integer.parseInt((String) puntos4.getText()) + Integer.parseInt((String) puntos6.getText());
                                                        break;
                                                }
                                                pTotales1.setText(String.valueOf(puntosFinalesEsteJugador));
                                                pTotales2.setText(String.valueOf(puntosFinalesOtroJugador));
                                            }
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }

    //Método para crear alerts.
    public void alert(String mensaje){
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    int intervalo = 2000;
    long tiempoPrimerClick = 0;
    int click = 0;
    //Método para tener que pulsar dos veces atrás para salir.
    @Override
    public void onBackPressed(){
        click++;

        if (click == 1){
            tiempoPrimerClick = System.currentTimeMillis();
            alert("Vuelve a pulsar para salir.");
        }else {
            if ((System.currentTimeMillis() - tiempoPrimerClick) < intervalo){
                db.collection("partidasActivas").document(partidaId).delete();
                if (descuento != null){
                    descuento.cancel();
                }
                if (handler != null){
                    handler.removeCallbacksAndMessages(null);
                }
                if (handler2 != null){
                    handler2.removeCallbacksAndMessages(null);
                }
                salir = true;
                this.finish();
            }else {
                click = 0;
            }
        }
    }

    //Se ejecuta cuando se han jugado los 3 minijuegos de la batalla. Muestra al ganador.
    public void terminarBatalla(){
        handler.removeCallbacksAndMessages(null);
        textComenzando.setVisibility(View.INVISIBLE);
        textGanarPerder.setVisibility(View.VISIBLE);
        botonTerminar.setVisibility(View.VISIBLE);

        puntosFinalesEsteJugador = Integer.valueOf(pTotales1.getText().toString());
        puntosFinalesOtroJugador = Integer.parseInt(pTotales2.getText().toString());

        final HashMap<String, Object> hash = new HashMap<>();
        if (puntosFinalesEsteJugador > puntuacionMax){
            hash.put("puntuacionMasAlta", puntosFinalesEsteJugador);
        }

        if (puntosFinalesEsteJugador > puntosFinalesOtroJugador){
            db.collection("usuarios").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            int batallas = Integer.parseInt(String.valueOf(document.get("batallasGanadas")));
                            batallas++;
                            hash.put("batallasGanadas", batallas);
                            db.collection("usuarios").document(email).set(hash, SetOptions.merge());
                        }
                    }
                }
            });

            textGanarPerder.setText(R.string.textoGanarBatalla);
        }else if (puntosFinalesEsteJugador < puntosFinalesOtroJugador){
            textGanarPerder.setText(R.string.textoPerderBatalla);
        }else {
            textGanarPerder.setText(R.string.textoEmpate);
        }
    }

    //Botón terminar. Borra la partida y sale a la actividad del usuario.
    public void fin(View view){
        db.collection("partidasActivas").document(partidaId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                db.collection("partidasActivas").document(partidaId).delete();
                            }
                            finish();
                        }
                    }
                });
    }

    //Se ejecuta al acabar cada minijuego.
    @Override
    public void onResume() {
        super.onResume();

        if (vecesJugadas < 2 && vecesJugadas != -1){
            db.collection("partidasActivas").document(partidaId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            controlarSalidaOtroJugador();
                            mostrarPuntuaciones();
                            descuento(false, 6000);

                            HashMap<String, Object> hash = new HashMap<>();
                            if (nombreUsuario.equals(nombreJ1)){
                                hash.put("estadoJ1", true);
                                db.collection("partidasActivas").document(partidaId).set(hash, SetOptions.merge());
                            }else {
                                hash.put("estadoJ2", true);
                                db.collection("partidasActivas").document(partidaId).set(hash, SetOptions.merge());
                            }
                        }else {
                            if (descuento != null){
                                descuento.cancel();
                            }
                            if (handler != null){
                                handler.removeCallbacksAndMessages(null);
                            }
                            if (handler2 != null){
                                handler2.removeCallbacksAndMessages(null);
                            }
                            cancelarBatalla();
                        }
                    }
                }
            });
        }else if (vecesJugadas == 2){
            HashMap<String, Object> hash = new HashMap<>();
            if (nombreUsuario.equals(nombreJ1)){
                hash.put("estadoJ1", true);
                db.collection("partidasActivas").document(partidaId).set(hash, SetOptions.merge());
            }else {
                hash.put("estadoJ2", true);
                db.collection("partidasActivas").document(partidaId).set(hash, SetOptions.merge());
            }
            descuento(true, 4000);
        }
        vecesJugadas++;

    }
}