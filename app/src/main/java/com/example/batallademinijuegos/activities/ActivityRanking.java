package com.example.batallademinijuegos.activities;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.batallademinijuegos.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ActivityRanking extends AppCompatActivity {
    FirebaseFirestore db;
    ArrayList<String> listaNombres;
    ArrayList<Integer> listaDatos;

    TextView textCarga;
    ListView listNombres;
    ListView listDatos;
    Button botonPuntos;
    Button botonBatallas;

    MediaPlayer sonidoBotones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        textCarga = findViewById(R.id.textCarga);
        listNombres = findViewById(R.id.listViewNombres);
        listDatos = findViewById(R.id.listViewPuntos);
        botonPuntos = findViewById(R.id.botonRankingPuntos);
        botonBatallas = findViewById(R.id.botonRankingBatallas);

        listaNombres = new ArrayList<>();
        listaDatos = new ArrayList<>();

        db = FirebaseFirestore.getInstance();

        sonidoBotones = MediaPlayer.create(this, R.raw.sonido_botones);

        recogerDatos("puntuacionMasAlta");
    }

    //Busca los datos que se quieren mostrar. Puntuaciones o batallas ganadas
    public void recogerDatos(final String datos){
        db.collection("usuarios").orderBy(datos, Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                listaNombres.add(document.getString("nombreUsuario"));
                                listaDatos.add(Integer.parseInt(document.get(datos).toString()));
                            }
                            mostrarDatos();
                        }
                    }
                });
    }

    //Muestra los datos recogidos
    public void mostrarDatos(){
        textCarga.setVisibility(View.INVISIBLE);

        ArrayAdapter<String> arrayAdapterNombres;
        arrayAdapterNombres = new ArrayAdapter<>(this, R.layout.layout_lista, listaNombres);
        listNombres.setAdapter(arrayAdapterNombres);

        ArrayAdapter<Integer> arrayAdapterPuntos;
        arrayAdapterPuntos = new ArrayAdapter<>(this, R.layout.layout_lista, listaDatos);
        listDatos.setAdapter(arrayAdapterPuntos);
    }

    //Mostrar las puntuaciones de los usuarios.
    public void mostrarPuntos(View view){
        sonidoBotones.start();

        textCarga.setVisibility(View.VISIBLE);
        botonPuntos.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.color_n, null));
        botonBatallas.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.color_transparente, null));

        listaNombres.removeAll(listaNombres);
        listaDatos.removeAll(listaDatos);
        recogerDatos("puntuacionMasAlta");
    }

    //Mostrar las batallas ganadas de los usuarios.
    public void mostrarBatallas(View view) {
        sonidoBotones.start();

        textCarga.setVisibility(View.VISIBLE);
        botonBatallas.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.color_n, null));
        botonPuntos.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.color_transparente, null));

        listaNombres.removeAll(listaNombres);
        listaDatos.removeAll(listaDatos);
        recogerDatos("batallasGanadas");
    }
}