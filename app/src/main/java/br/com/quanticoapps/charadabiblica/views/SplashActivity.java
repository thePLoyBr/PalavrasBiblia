package br.com.quanticoapps.charadabiblica.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import br.com.quanticoapps.charadabiblica.R;
import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class SplashActivity extends AppCompatActivity {

    public static FirebaseAuth firebaseAuth;
    public static String user;
    public static String userConvertido;
    public static String senha;
    private CircularProgressButton loadingSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        loadingSplash = findViewById(R.id.loading_splash);
        loadingSplash.startAnimation();

        firebaseAuth = FirebaseAuth.getInstance();
        //firebaseAuth.signOut();


        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            checkLogin();
        } else {
        }


        //??????????????alteracoes BANCO + SHARED/////////
/*       sharedPreferences.edit().clear().commit();
        Log.i("listaShared", String.valueOf(sharedPreferences.getAll()));
        for(int i = 1; i <= 17; i++){
            myRef.child(userConvertido).child("pacotes").child(String.valueOf(i)).child("compras").setValue("false");
        }

        }*/

///////////////////////////////////////////////////////////////

        if(user == "") {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 4000);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 4000);
        }
    }

    public static boolean checkLogin() {
        if (firebaseAuth.getCurrentUser() != null) {
            setUser(firebaseAuth.getCurrentUser().getEmail());
            setUserConvertido(userConvertido = user.replace(".",""));
            Log.i("CONVERTIDO!", userConvertido);
            return true;
        } else {
            user = "";
            return false;
        }
    }

    private static void setUser(String user) {
        SplashActivity.user = user;
    }

    private static void setUserConvertido(String userConvertido) {
        SplashActivity.userConvertido = userConvertido;
    }
}
