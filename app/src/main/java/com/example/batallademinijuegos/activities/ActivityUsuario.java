package com.example.batallademinijuegos.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.example.batallademinijuegos.clases.Partida;
import com.example.batallademinijuegos.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.UUID;

public class ActivityUsuario extends AppCompatActivity {
    Button botonBuscar;
    Button botonClasificacion;
    TextView textoBuscar;
    TextView textCarga;
    TextView textPuntos;
    TextView textoPuntuacion;
    MenuItem editNombreUsuario;
    MenuItem modCon;
    MenuItem cerrarSesion;
    MenuItem eliminarCuenta;

    FirebaseAuth auth;
    Toolbar toolbar;
    FirebaseFirestore db;

    Handler handler;

    Boolean nuevoUsuario = false;
    String email;
    String nombreUsuario = null;
    int puntuacionMasAlta;

    int puntos;

    String j1 = null;
    String partId = null;
    String partidaIdCreada = null;
    Boolean encontrado = false;
    Boolean buscar = true;
    Boolean disponible = true;

    MediaPlayer sonidoBotones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);

        botonBuscar = findViewById(R.id.botonBuscar);
        botonClasificacion = findViewById(R.id.botonClasificaion);
        textoBuscar = findViewById(R.id.textBuscar);
        textCarga = findViewById(R.id.textCarga);
        textPuntos = findViewById(R.id.textPuntos);
        textoPuntuacion = findViewById(R.id.textoPuntuacion);

        sonidoBotones = MediaPlayer.create(this, R.raw.sonido_botones);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.setTitle("");
        nuevoUsuario = getIntent().getBooleanExtra("nuevoUsuario", false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        email = user.getEmail();
        db = FirebaseFirestore.getInstance();

        handler = new Handler();

        if (nuevoUsuario){
            editarNombre(false);
        }else {
            cambiarNombreUsuario();
        }
    }

    //Busca el nombre de usuario en la bd. Si existe el usuario lo visualiza, si no inicia la actividad de autenticar.
    public void cambiarNombreUsuario(){
        db.collection("usuarios").document(email).get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            nombreUsuario = document.getString("nombreUsuario");
                            visualizarNombreUsuario(nombreUsuario);
                        }else {
                            volverAAutenticar();
                        }
                    }
                }
            });
    }

    //Inicia la actividad de autenticar si no se ha encontrado el usuario actual.
    public void volverAAutenticar(){
        Intent i = new Intent(this, ActivityAuthentication.class);
        startActivity(i);
        finish();
    }

    //Muestra el nombre de usuario.
    public void visualizarNombreUsuario(String nombre){
        this.setTitle(nombre);
        textCarga.setVisibility(View.INVISIBLE);
        textoPuntuacion.setVisibility(View.VISIBLE);
        botonBuscar.setVisibility(View.VISIBLE);
        botonClasificacion.setVisibility(View.VISIBLE);

        visualizarPuntos();

        editNombreUsuario.setVisible(true);
        modCon.setVisible(true);
        cerrarSesion.setVisible(true);
        eliminarCuenta.setVisible(true);
    }

    //Busca y muestra la puntuación más alta obtenida del usuario.
    public void visualizarPuntos(){
        textPuntos.setVisibility(View.VISIBLE);

        db.collection("usuarios").document(email).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                puntos = Integer.parseInt(document.get("puntuacionMasAlta").toString());
                                textPuntos.setText(String.valueOf(puntos));
                            }
                        }
                    }
                });
    }

    //Muestra el menú.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_usuario, menu);

        editNombreUsuario = menu.findItem(R.id.editNombreUsuario);
        modCon = menu.findItem(R.id.modCon);
        cerrarSesion = menu.findItem(R.id.cerrarSesion);
        eliminarCuenta = menu.findItem(R.id.eliminarCuenta);
        return super.onCreateOptionsMenu(menu);
    }

    AlertDialog dialog;
    TextView textCargando;
    //Opciones del menú.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Opción de modificar la contraseña. Muestra un nuevo layout como un alertDialog para introducir las contraseñas.
            case R.id.modCon:
                final AlertDialog.Builder alert = new AlertDialog.Builder(this);
                final Context context = alert.getContext();
                final LayoutInflater inflater = LayoutInflater.from(context);

                final View v = inflater.inflate(R.layout.layout_mod_contrasenia, null, false);

                final EditText contraActual = v.findViewById(R.id.textContraActual);
                final EditText contraNueva1 = v.findViewById(R.id.textContraNueva);
                final EditText contraNueva2 = v.findViewById(R.id.textContraNueva2);
                textCargando = v.findViewById(R.id.textCargando);
                textCargando.setVisibility(View.INVISIBLE);

                alert.setView(v);
                dialog = alert.show();

                //Botón cancelar. Se cierra el alertDialog.
                Button dialogButtonCancelar = v.findViewById(R.id.botonCancelar);
                dialogButtonCancelar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                //Botón aceptar. Comprueba que no estén los campos vacios y llama al método para modificar la contraseña.
                Button dialogButtonAceptar = (Button) v.findViewById(R.id.botonAceptar);
                dialogButtonAceptar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!contraActual.getText().toString().isEmpty() && !contraNueva1.getText().toString().isEmpty() && !contraNueva2.getText().toString().isEmpty()){
                            textCargando.setVisibility(View.VISIBLE);
                            modificarContra(contraActual.getText().toString(), contraNueva1.getText().toString(), contraNueva2.getText().toString());
                        }else {
                            alert("Introduce todos los datos.");
                        }

                        //Ocultar teclado
                        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(contraNueva2.getWindowToken(), 0);
                    }
                });

                return true;

                //Opción de cerrar sesión. Firebase hace un signOut y vuelve a la pantalla anterior.
            case R.id.cerrarSesion:
                auth.getInstance().signOut();
                onBackPressed();
                return true;

                //Opción para eliminar la cuenta. Muestra un alertDialog para confirmar. Firebase elimina la cuenta.
            case R.id.eliminarCuenta:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle("CONFIRMAR");
                builder.setMessage("¿Estás seguro de que quieres eliminar la cuenta?");
                builder.setPositiveButton("Sí",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().getCurrentUser().delete();
                                db.collection("usuarios").document(email).delete();
                                Toast.makeText(ActivityUsuario.this, "Cuenta eliminada.", Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog alertD = builder.create();
                alertD.show();

                return true;
            case R.id.editNombreUsuario:
                editarNombre(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    //1. Cojo el email del usuario actual.
    //2. Se autentica el usuario con el email y la contraseña actual introducida.
    //3. Se actualiza la contraseña si coincide la confirmación.
    public void modificarContra(final String contraActual, final String contraNueva1, final String contraNueva2){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user.getEmail();

        AuthCredential credential = EmailAuthProvider.getCredential(email, contraActual);
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            if (contraNueva1.equals(contraNueva2)){
                                user.updatePassword(contraNueva1)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    textCargando.setVisibility(View.INVISIBLE);
                                                    dialog.dismiss();
                                                    alert("Contraseña cambiada con éxito.");
                                                }else {
                                                    if (contraNueva1.length() < 6){
                                                        textCargando.setVisibility(View.INVISIBLE);
                                                        alert("Introduce una contraseña más larga.");
                                                    }else {
                                                        textCargando.setVisibility(View.INVISIBLE);
                                                        alert("Ha ocurrido un error al actualizar la contraseña.");
                                                    }
                                                }
                                            }
                                        });
                            }else {
                                textCargando.setVisibility(View.INVISIBLE);
                                alert("Las contraseñas no coinciden.");
                            }
                        }else {
                            textCargando.setVisibility(View.INVISIBLE);
                            alert("Ha habido un error al cambiar la contraseña.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        textCargando.setVisibility(View.INVISIBLE);
                        alert("Ha habido un error al cambiar la contraseña.");
                    }
                });;
    }

    //Abre un alert para introducir un nombre de usuario a los nuevos jugadores. Los nombres de usuario no pueden repetirse.
    public void editarNombre(Boolean cancelable){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final Context context = alert.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View v = inflater.inflate(R.layout.layout_nombreusuario, null, false);

        final EditText nombre = v.findViewById(R.id.nombreUsuario);

        alert.setView(v);
        final AlertDialog dialo = alert.setCancelable(cancelable).show();

        Button buttonAceptar = v.findViewById(R.id.botonAceptar);
        buttonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nombre.getText().toString().isEmpty()){
                    alert("Introduce un nombre.");
                }else if(nombre.getText().toString().length() > 8){
                    alert("El nombre no puede tener más de 8 caracteres.");
                }else {
                    final String n = nombre.getText().toString().replace(" ", "");

                    db.collection("usuarios")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            if (document.get("nombreUsuario").toString().equals(n)){
                                                disponible = false;
                                                break;
                                            }else {
                                                disponible = true;
                                            }
                                        }

                                        if (disponible){
                                            HashMap<String, Object> hash = new HashMap<>();
                                            if (nuevoUsuario){
                                                hash.put("email", email);
                                                hash.put("nombreUsuario", n);
                                                hash.put("puntuacionMasAlta", puntuacionMasAlta);
                                                hash.put("batallasGanadas", puntos);
                                            }else {
                                                hash.put("nombreUsuario", n);
                                            }

                                            db.collection("usuarios").document(email).set(hash, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    alert("Nombre de usuario guardado correctamente");
                                                    cambiarNombreUsuario();
                                                }
                                            });
                                            dialo.dismiss();
                                        }else {
                                            alert("Nombre no disponible.");
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }

    //Método para mostrar toast ya que no es posible hacerlo en los métodos de Firebase.
    public void alert(String mensaje){
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    //Método para buscar partida. Si hay alguna disponible se une, si no espera a que alguien se una. Si cancela la busqueda se borra la partida en espera.
    public void buscarPartida(View view){
        sonidoBotones.start();

        if (buscar){
            botonBuscar.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.color_rojo, null));
            botonBuscar.setText(R.string.boton_cancelar_busqueda);
            textoBuscar.setText(R.string.etiq_conectando);
            textoBuscar.setVisibility(View.VISIBLE);
            botonClasificacion.setEnabled(false);
            editNombreUsuario.setEnabled(false);
            modCon.setEnabled(false);
            cerrarSesion.setEnabled(false);
            eliminarCuenta.setEnabled(false);

            db.collection("partidasEnEspera")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    j1 = document.get("jugador1").toString();
                                    partId = document.getId();

                                    if (document.get("jugador2").equals("") && !j1.equals(nombreUsuario)){
                                        db.collection("partidasEnEspera").document(partId).delete();
                                        Partida partida = new Partida(partId, j1, nombreUsuario);
                                        db.collection("partidasActivas").document(partId).set(partida);
                                        encontrado = true;

                                        botonBuscar.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.color_azul, null));
                                        botonBuscar.setText(R.string.boton_buscar);
                                        textoBuscar.setVisibility(View.INVISIBLE);
                                        mostrarActivity(partId);
                                        break;
                                    }
                                }
                                if (!encontrado){
                                    partidaIdCreada = UUID.randomUUID().toString();

                                    Partida partida = new Partida(partidaIdCreada, nombreUsuario, "");
                                    db.collection("partidasEnEspera").document(partidaIdCreada).set(partida);

                                    esperar(partidaIdCreada);
                                }
                            }
                        }
                    });
            buscar = false;
        }else {
            botonBuscar.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.color_azul, null));
            botonBuscar.setText(R.string.boton_buscar);
            textoBuscar.setVisibility(View.INVISIBLE);
            botonClasificacion.setEnabled(true);
            editNombreUsuario.setEnabled(true);
            modCon.setEnabled(true);
            cerrarSesion.setEnabled(true);
            eliminarCuenta.setEnabled(true);

            db.collection("partidasEnEspera")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    j1 = document.get("jugador1").toString();
                                    partId = document.getId();

                                    if (j1.equals(nombreUsuario)){
                                        db.collection("partidasEnEspera").document(partId).delete();
                                    }
                                }
                            }
                        }
                    });
            buscar = true;
        }
    }

    //Método para esperar a que alguien se una a la partida.
    public void esperar(final String partidaIdCreada){
        handler.postDelayed(new Runnable() {
            public void run() {
                db.collection("partidasActivas").document(partidaIdCreada).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        if (handler != null){
                                            handler.removeCallbacksAndMessages(null);
                                        }
                                        botonBuscar.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.color_azul, null));
                                        botonBuscar.setText(R.string.boton_buscar);
                                        textoBuscar.setVisibility(View.INVISIBLE);
                                        mostrarActivity(partidaIdCreada);
                                    } else {
                                        textoBuscar.setText(R.string.etiq_buscando);
                                    }
                                }
                            }
                        });
                handler.postDelayed(this, 500);
            }
        }, 500);
    }

    //Método para cambiar de actividad.
    public void mostrarActivity(String partida){
        Intent i = new Intent(this, ActivityBatalla.class);
        i.putExtra("partidaID", partida);
        startActivity(i);
    }

    //Botón para mostrar la clasificación de puntuaciones o batallas ganadas.
    public void mostrarClasificacion(View view){
        sonidoBotones.start();

        Intent i = new Intent(this, ActivityRanking.class);
        startActivity(i);
    }

    int contResume = 0;
    @Override
    public void onResume() {
        super.onResume();
        visualizarPuntos();
        buscar = true;
        if (contResume != 0){
            botonClasificacion.setEnabled(true);
            editNombreUsuario.setEnabled(true);
            modCon.setEnabled(true);
            cerrarSesion.setEnabled(true);
            eliminarCuenta.setEnabled(true);
        }
        contResume++;
    }

}