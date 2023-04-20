package com.example.batallademinijuegos.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.batallademinijuegos.R;
import com.example.batallademinijuegos.minijuegos.ActivityJuegoBolas;
import com.example.batallademinijuegos.minijuegos.ActivityJuegoGlobos;
import com.example.batallademinijuegos.minijuegos.ActivityJuegoPulsaciones;
import com.example.batallademinijuegos.minijuegos.ActivityJuegoReloj;
import com.example.batallademinijuegos.minijuegos.ActivityJuegoSonidos;

public class ActivitySeleccion extends AppCompatActivity {
    ImageButton botonGlobos;
    ImageButton botonBombas;
    ImageButton botonSonidos;
    ImageButton botonPulsaciones;
    ImageButton botonReloj;

    Button botonInstrucciones;
    Button botonPracticar;

    TextView textoTituloJuego;

    int juegoElegido = 0;

    MediaPlayer sonidoSeleccion;
    MediaPlayer sonidoBotones;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion);

        botonGlobos = findViewById(R.id.botonGlobos);
        botonBombas = findViewById(R.id.botonBombas);
        botonSonidos = findViewById(R.id.botonSonidos);
        botonPulsaciones = findViewById(R.id.botonPulsaciones);
        botonReloj = findViewById(R.id.botonReloj);

        botonInstrucciones = findViewById(R.id.botonInstrucciones);
        botonPracticar = findViewById(R.id.botonPracticar);
        textoTituloJuego = findViewById(R.id.textoTituloJuego);

        sonidoSeleccion = MediaPlayer.create(this, R.raw.sonido_seleccion);
        sonidoBotones = MediaPlayer.create(this, R.raw.sonido_botones);
    }

    //Botón juego de explotar los globos. Si se selecciona se deseleccionan los demás.
    public void botonGlobos(View view){
        sonidoSeleccion.start();
        if (juegoElegido == 1){
            juegoElegido = 0;
            botonGlobos.setBackground(hacerDrawable(R.drawable.color_morado));
            botonInstrucciones.setVisibility(View.INVISIBLE);
            botonPracticar.setVisibility(View.INVISIBLE);
            textoTituloJuego.setText("");
        }else {
            juegoElegido = 1;
            botonGlobos.setBackground(hacerDrawable(R.drawable.color_seleccion));
            botonInstrucciones.setVisibility(View.VISIBLE);
            botonPracticar.setVisibility(View.VISIBLE);
            textoTituloJuego.setText(R.string.titulo_globos);

            botonBombas.setBackground(hacerDrawable(R.drawable.color_morado));
            botonSonidos.setBackground(hacerDrawable(R.drawable.color_morado));
            botonPulsaciones.setBackground(hacerDrawable(R.drawable.color_morado));
            botonReloj.setBackground(hacerDrawable(R.drawable.color_morado));
        }
    }

    //Botón juego de encontrar la pareja. Si se selecciona se deseleccionan los demás.
    public void botonBombas(View view){
        sonidoSeleccion.start();
        if (juegoElegido == 2){
            juegoElegido = 0;
            botonBombas.setBackground(hacerDrawable(R.drawable.color_morado));
            botonInstrucciones.setVisibility(View.INVISIBLE);
            botonPracticar.setVisibility(View.INVISIBLE);
            textoTituloJuego.setText("");
        }else {
            juegoElegido = 2;
            botonBombas.setBackground(hacerDrawable(R.drawable.color_seleccion));
            botonInstrucciones.setVisibility(View.VISIBLE);
            botonPracticar.setVisibility(View.VISIBLE);
            textoTituloJuego.setText(R.string.titulo_bombas);

            botonGlobos.setBackground(hacerDrawable(R.drawable.color_morado));
            botonSonidos.setBackground(hacerDrawable(R.drawable.color_morado));
            botonPulsaciones.setBackground(hacerDrawable(R.drawable.color_morado));
            botonReloj.setBackground(hacerDrawable(R.drawable.color_morado));
        }
    }

    //Botón juego de encontrar el sonido. Si se selecciona se deseleccionan los demás.
    public void botonSonidos(View view){
        sonidoSeleccion.start();
        if (juegoElegido == 3){
            juegoElegido = 0;
            botonSonidos.setBackground(hacerDrawable(R.drawable.color_morado));
            botonInstrucciones.setVisibility(View.INVISIBLE);
            botonPracticar.setVisibility(View.INVISIBLE);
            textoTituloJuego.setText("");
        }else {
            juegoElegido = 3;
            botonSonidos.setBackground(hacerDrawable(R.drawable.color_seleccion));
            botonInstrucciones.setVisibility(View.VISIBLE);
            botonPracticar.setVisibility(View.VISIBLE);
            textoTituloJuego.setText(R.string.titulo_sonidos);

            botonGlobos.setBackground(hacerDrawable(R.drawable.color_morado));
            botonBombas.setBackground(hacerDrawable(R.drawable.color_morado));
            botonPulsaciones.setBackground(hacerDrawable(R.drawable.color_morado));
            botonReloj.setBackground(hacerDrawable(R.drawable.color_morado));
        }
    }

    //Botón juego de las pulsaciones. Si se selecciona se deseleccionan los demás.
    public void botonPulsaciones(View view){
        sonidoSeleccion.start();
        if (juegoElegido == 4){
            juegoElegido = 0;
            botonPulsaciones.setBackground(hacerDrawable(R.drawable.color_morado));
            botonInstrucciones.setVisibility(View.INVISIBLE);
            botonPracticar.setVisibility(View.INVISIBLE);
            textoTituloJuego.setText("");
        }else {
            juegoElegido = 4;
            botonPulsaciones.setBackground(hacerDrawable(R.drawable.color_seleccion));
            botonInstrucciones.setVisibility(View.VISIBLE);
            botonPracticar.setVisibility(View.VISIBLE);
            textoTituloJuego.setText(R.string.titulo_pulsaciones);

            botonGlobos.setBackground(hacerDrawable(R.drawable.color_morado));
            botonBombas.setBackground(hacerDrawable(R.drawable.color_morado));
            botonSonidos.setBackground(hacerDrawable(R.drawable.color_morado));
            botonReloj.setBackground(hacerDrawable(R.drawable.color_morado));
        }
    }

    //Botón juego del reloj. Si se selecciona se deseleccionan los demás.
    public void botonReloj(View view){
        sonidoSeleccion.start();
        if (juegoElegido == 5){
            juegoElegido = 0;
            botonReloj.setBackground(hacerDrawable(R.drawable.color_morado));
            botonInstrucciones.setVisibility(View.INVISIBLE);
            botonPracticar.setVisibility(View.INVISIBLE);
            textoTituloJuego.setText("");
        }else {
            juegoElegido = 5;
            botonReloj.setBackground(hacerDrawable(R.drawable.color_seleccion));
            botonInstrucciones.setVisibility(View.VISIBLE);
            botonPracticar.setVisibility(View.VISIBLE);
            textoTituloJuego.setText(R.string.titulo_reloj);

            botonGlobos.setBackground(hacerDrawable(R.drawable.color_morado));
            botonBombas.setBackground(hacerDrawable(R.drawable.color_morado));
            botonSonidos.setBackground(hacerDrawable(R.drawable.color_morado));
            botonPulsaciones.setBackground(hacerDrawable(R.drawable.color_morado));
        }
    }

    //Botón practicar. Comienza el juego seleccionado.
    public void practicar(View view){
        sonidoBotones.start();
        Intent i = null;
        switch (juegoElegido){
            case 1:
                i = new Intent(this, ActivityJuegoGlobos.class);
                break;
            case 2:
                i = new Intent(this, ActivityJuegoBolas.class);
                break;
            case 3:
                i = new Intent(this, ActivityJuegoSonidos.class);
                break;
            case 4:
                i = new Intent(this, ActivityJuegoPulsaciones.class);
                break;
            case 5:
                i = new Intent(this, ActivityJuegoReloj.class);
                break;
        }
        i.putExtra("batalla", false);
        startActivity(i);
    }

    //Botón instrucciones. Muestra cómo jugar del juego seleccionado.
    public void mostrarInstrucciones(View view){
        sonidoBotones.start();
        Intent i = new Intent(this, ActivityInstrucciones.class);
        switch (juegoElegido){
            case 1:
                i.putExtra("juego", 1);
                break;
            case 2:
                i.putExtra("juego", 2);
                break;
            case 3:
                i.putExtra("juego", 3);
                break;
            case 4:
                i.putExtra("juego", 4);
                break;
            case 5:
                i.putExtra("juego", 5);
                break;
        }
        startActivity(i);
    }

    //Método para hacer drawable una imagen.
    public Drawable hacerDrawable(int imagen){
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), imagen, null);

        return drawable;
    }
}