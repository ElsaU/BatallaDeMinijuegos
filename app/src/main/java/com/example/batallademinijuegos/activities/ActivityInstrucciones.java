package com.example.batallademinijuegos.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.batallademinijuegos.R;

import java.io.InputStream;
import java.util.Scanner;

public class ActivityInstrucciones extends AppCompatActivity {
    TextView texto;
    TextView titulo;
    int juego;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrucciones);

        texto = findViewById(R.id.textInstrucciones);
        titulo = findViewById(R.id.textTitulo);

        juego = getIntent().getIntExtra("juego", 0);

        //Selección del fichero del juego seleccionado
        InputStream input = null;
        switch (juego){
            case 1:
                titulo.setText(R.string.titulo_globos);
                input = getResources().openRawResource(R.raw.instrucciones_globos);
                break;
            case 2:
                titulo.setText(R.string.titulo_bombas);
                input = getResources().openRawResource(R.raw.instrucciones_bombas);
                break;
            case 3:
                titulo.setText(R.string.titulo_sonidos);
                input = getResources().openRawResource(R.raw.instrucciones_sonidos);
                break;
            case 4:
                titulo.setText(R.string.titulo_pulsaciones);
                input = getResources().openRawResource(R.raw.instrucciones_pulsaciones);
                break;
            case 5:
                titulo.setText(R.string.titulo_reloj);
                input = getResources().openRawResource(R.raw.instrucciones_reloj);
                break;

        }
        //Lee el fichero y muestra las instrucciones del juego seleccionado.
        Scanner sc = new Scanner(input);
        while (sc.hasNextLine()) {
            String linea = sc.nextLine();
            texto.append(linea+"\n");
        }
    }
}