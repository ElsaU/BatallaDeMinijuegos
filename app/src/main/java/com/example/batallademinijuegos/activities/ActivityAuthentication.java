package com.example.batallademinijuegos.activities;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.batallademinijuegos.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class ActivityAuthentication extends AppCompatActivity {
    EditText email;
    EditText contra;
    TextView contraOlvidada;
    TextView textoCargando;

    MediaPlayer sonidoBotones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        email = findViewById(R.id.textEmail);
        contra = findViewById(R.id.textContrasenia);
        contraOlvidada = findViewById(R.id.textContraOlvidada);
        textoCargando = findViewById(R.id.textCargando);

        sonidoBotones = MediaPlayer.create(this, R.raw.sonido_botones);

        //OnClick para 'He olvidado la contraseña'.
        contraOlvidada.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                restablecerContra();
            }
        });
    }

    //Botón registrar. Comprueba que los campos no estén vacíos. Para firebase La contraseña debe tener más de 5 caracteres.
    //Firebase comprueba si ya existe una cuenta con ese email.
    public void registrar(View view){
        sonidoBotones.start();

        //Ocultar teclado
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(contra.getWindowToken(), 0);

        textoCargando.setVisibility(View.VISIBLE);
        if (!email.getText().toString().isEmpty() && !contra.getText().toString().isEmpty()){
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.getText().toString(), contra.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                textoCargando.setVisibility(View.INVISIBLE);
                                iniciarActivity(true);
                                finish();
                            }else {
                                if (contra.getText().toString().length() < 6) {
                                    textoCargando.setVisibility(View.INVISIBLE);
                                    alert("Introduce una contraseña más larga.");
                                }else {
                                    textoCargando.setVisibility(View.INVISIBLE);
                                    alert("Error al registrar el usuario.");
                                }
                            }
                        }
                    });
        }else {
            textoCargando.setVisibility(View.INVISIBLE);
            alert("Introduce los datos.");
        }
    }

    //Botón acceder. Comprueba que los campos no estén vacíos.
    //Firebase comprueba que exista la cuenta con ese email y contraseña.
    public void acceder(View view){
        sonidoBotones.start();

        //Ocultar teclado
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(contra.getWindowToken(), 0);

        textoCargando.setVisibility(View.VISIBLE);
        if (!email.getText().toString().isEmpty() && !contra.getText().toString().isEmpty()){
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email.getText().toString(), contra.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        textoCargando.setVisibility(View.INVISIBLE);
                        iniciarActivity(false);
                        finish();
                    }else {
                        textoCargando.setVisibility(View.INVISIBLE);
                        alert("Email o contraseña incorrecto.");
                    }
                }
            });
        }else {
            textoCargando.setVisibility(View.INVISIBLE);
            alert("Introduce los datos.");
        }
    }

    //Método para reestablecer la contraseña. Comprueba que el email no está vacío.
    //Firebase se encarga de enviar un email de reseteo de contraseña.
    public void restablecerContra(){
        if (!email.getText().toString().isEmpty()){
            FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        alert("Email de recuperación de contraseña enviado.");
                    }else {
                        alert("Error al enviar el email de recuperación.");
                    }
                }
            });
        }else {
            alert("Introduce el email");
        }
    }

    //Método para hacer toast ya que no es posible hacerlos dentro de los métodos de Firebase.
    public void alert(String mensaje){
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    //Método para iniciar otras activities ya que no es posible hacerlo dentro de los métodos de Firebase.
    public void iniciarActivity(boolean nuevo){
        Intent i = new Intent(this, ActivityUsuario.class);
        i.putExtra("nuevoUsuario", nuevo);
        startActivity(i);
    }
}