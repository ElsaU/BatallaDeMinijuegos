package com.example.batallademinijuegos.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.batallademinijuegos.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivityInicio extends AppCompatActivity {
    MediaPlayer sonidoBotones;

    //Theme.AppCompat.NoActionBar
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat_Light_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        sonidoBotones = MediaPlayer.create(this, R.raw.sonido_botones);
    }

    //Botón practicar. Inicia ActivitySeleccion.
    public void practicar(View view) {
        sonidoBotones.start();
        Intent i = new Intent(this, ActivitySeleccion.class);
        startActivity(i);
    }

    //Botón batalla. Si no hay ninguna sesión abierta inicia ActivityAuthentication. Si hay una sesión abierta inicia ActivityUsuario.
    public void batalla(View view) {
        sonidoBotones.start();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent i = new Intent(this, ActivityUsuario.class);
            startActivity(i);
        } else {
            Intent i = new Intent(this, ActivityAuthentication.class);
            startActivity(i);
        }
    }
}