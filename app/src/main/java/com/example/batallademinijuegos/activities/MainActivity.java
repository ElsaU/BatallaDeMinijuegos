package com.example.batallademinijuegos.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.batallademinijuegos.R;

public class MainActivity extends AppCompatActivity {
    ImageView imagenBatalla;
    ImageView imagenDe;
    ImageView imagenMinijuegos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat_Light_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imagenBatalla = findViewById(R.id.imageViewBatalla);
        imagenDe = findViewById(R.id.imageViewDe);
        imagenMinijuegos = findViewById(R.id.imageViewMinijuegos);

        //Animaciones al abrir la aplicación.
        Animation animacion1 = AnimationUtils.loadAnimation(this, R.anim.movimiento_hacia_abajo);
        Animation animacion2 = AnimationUtils.loadAnimation(this, R.anim.movimiento_hacia_izquierda);
        Animation animacion3 = AnimationUtils.loadAnimation(this, R.anim.aparecer);
        imagenBatalla.setAnimation(animacion1);
        imagenDe.setAnimation(animacion3);
        imagenMinijuegos.setAnimation(animacion2);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mostrarActivity();
                finish();
            }
        }, 3000);
    }

    //Muestra la actividad de inicio.
    public void mostrarActivity() {
        Intent i = new Intent(this, ActivityInicio.class);
        startActivity(i);
    }

}