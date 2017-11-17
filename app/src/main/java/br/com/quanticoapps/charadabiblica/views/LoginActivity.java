package br.com.quanticoapps.charadabiblica.views;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import br.com.quanticoapps.charadabiblica.R;
import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

import static br.com.quanticoapps.charadabiblica.views.SplashActivity.checkLogin;
import static br.com.quanticoapps.charadabiblica.views.SplashActivity.firebaseAuth;
import static br.com.quanticoapps.charadabiblica.views.SplashActivity.senha;
import static br.com.quanticoapps.charadabiblica.views.SplashActivity.user;
import static br.com.quanticoapps.charadabiblica.views.SplashActivity.userConvertido;

public class LoginActivity extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private TextView email;
    private TextView pass;
    private CircularProgressButton btnRegister;
    private TextView mensagemLogin;
    private CircularProgressButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_login);

        mensagemLogin = findViewById(R.id.tv_msg_login);
        email = findViewById(R.id.tv_email);
        pass = findViewById(R.id.tv_senha);
        btnRegister = findViewById(R.id.btn_register);
        mensagemLogin = findViewById(R.id.tv_mensagem_login);
        btnLogin = findViewById(R.id.loading_splash);

        if (user == "") {

            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    user = email.getText().toString();
                    senha = pass.getText().toString();

                    if(user.isEmpty() || senha.isEmpty()){
                        mensagemLogin.setText("Você precisa preencher Email e senha");
                    } else {
                        btnLogin.startAnimation();
                        mensagemLogin.setText("Fazendo login, aguarde uns segundos...");
                        signInEmail(user, senha);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (checkLogin()) {
                                    btnLogin.setEnabled(false);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        }
                                    }, 1000);
                                } else {
                                    btnLogin.revertAnimation();
                                    //mensagemLogin.setText("Usuário ou senha incorretos, tente novamente");
                                }
                            }
                        }, 5000);
                    }


                }


            });

            btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    user = email.getText().toString();
                    senha = pass.getText().toString();

                    if(user.isEmpty() || senha.isEmpty()){
                        mensagemLogin.setText("Você precisa preencher Email e senha");
                    } else {
                        registerEmail();
                        mensagemLogin.setText("Registrando usuário, aguarde uns segundos...");
                        btnRegister.startAnimation();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (checkLogin()) {

                                    btnRegister.setEnabled(false);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            createUserDb(userConvertido);
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        }
                                    }, 5000);
                                } else {
                                    mensagemLogin.setText("Falha ao criar usuário, verifique sua conexão de internet.");
                                    btnRegister.revertAnimation();
                                }
                            }
                        }, 5000);
                    }


                }
            });
        }
    }


    private void registerEmail() {
        firebaseAuth.createUserWithEmailAndPassword(user, senha)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            signInEmail(user, senha);
                        } else {
                            mensagemLogin.setText("Falha ao REGISTRAR usuário, verifique sua conexão de internet.");
                        }
                    }
                });

    }

    private String signInEmail(String user, String senha) {
        firebaseAuth.signInWithEmailAndPassword(user, senha)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            SplashActivity.checkLogin();
                        } else {
                            mensagemLogin.setText("Não foi possível fazer login, verifique usuário e senha");
                        }
                    }
                });
        return "logado";
    }

    private void createUserDb(String user) {
        if (myRef != null) {
            myRef.child(user).child("coins").setValue("0");
            for (int i = 1; i <= 17; i++) {
                myRef.child(user).child("pacotes").child(String.valueOf(i)).child("compras").setValue("false");
            }
        }
    }
}
