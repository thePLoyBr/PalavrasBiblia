package br.com.quanticoapps.charadabiblica.views;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import br.com.quanticoapps.charadabiblica.R;

import static br.com.quanticoapps.charadabiblica.views.MainActivity.myRef;
import static br.com.quanticoapps.charadabiblica.views.SplashActivity.userConvertido;

public class ResultActivity extends AppCompatActivity implements RewardedVideoAdListener {

    private String palavrasCorretasFinal = "";
    private String palavrasPassadasFinal = "";
    private AdView adView;
    private RewardedVideoAd rewardedVideoAd;
    private TextView tvMoedasResult;
    private boolean isFinished = true;
    private int coinsDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_result);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        if (MainActivity.anuncios != "noAds") {
            rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(ResultActivity.this);
            rewardedVideoAd.setRewardedVideoAdListener(ResultActivity.this);
            loadRewardedVideoAd();

            if(adView == null) {
                adView = findViewById(R.id.adBannerResult);
                AdRequest adRequestResult = new AdRequest.Builder().build();
                adView.loadAd(adRequestResult);
            }
        }



        TextView points = findViewById(R.id.id_pontos);
        tvMoedasResult = findViewById(R.id.id_coins);
        ImageView ivMoedasResult = findViewById(R.id.iv_coin);
        TextView palavrasCorretas = findViewById(R.id.id_tv_palavrasCorretas);
        TextView palavrasPassadas = findViewById(R.id.id_tv_palavrasPassadas);
        palavrasCorretas.setMovementMethod(new ScrollingMovementMethod());
        palavrasPassadas.setMovementMethod(new ScrollingMovementMethod());

        ImageView btnPlay = findViewById(R.id.iv_btn_play);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ResultActivity.this, GameActivity.class));
            }
        });

        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            tvMoedasResult.setVisibility(View.VISIBLE);
            ivMoedasResult.setVisibility(View.VISIBLE);
        } else {
            tvMoedasResult.setVisibility(View.INVISIBLE);
            ivMoedasResult.setVisibility(View.INVISIBLE);
        }

        Bundle extra = getIntent().getExtras();

        if(extra != null) {
            int pontos = extra.getInt("pontos");
            points.setText(String.valueOf(pontos));

            //Palavras Corretas
            ArrayList<String> corretasLista = extra.getStringArrayList("palavrasCorretas");

            for (int i = 0; i < corretasLista.size(); i++) {
                palavrasCorretasFinal += "\n" + corretasLista.get(i);
                palavrasCorretas.setText(palavrasCorretasFinal);
            }

            //Palavras Passadas
            ArrayList<String> passadasLista = extra.getStringArrayList("palavrasPassadas");

            for (int i = 0; i < passadasLista.size(); i++) {
                palavrasPassadasFinal += "\n" + passadasLista.get(i);
                palavrasPassadas.setText(palavrasPassadasFinal);
            }
        }
        monitorDBResult();
    }

    private void monitorDBResult() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isFinished) {
                    coinsDB = Integer.parseInt(String.valueOf(dataSnapshot.child(userConvertido).child("coins").getValue()));
                    coinsDB += 5;
                    myRef.child(userConvertido).child("coins").setValue(coinsDB);
                    tvMoedasResult.setText(String.valueOf(coinsDB));
                    isFinished = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }

        if(rewardedVideoAd != null){
            rewardedVideoAd.pause(this);
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (adView != null) {
            adView.destroy();
        }

        if(rewardedVideoAd != null){
            rewardedVideoAd.destroy(this);
        }
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }

        if(rewardedVideoAd != null){
            rewardedVideoAd.destroy(this);
        }

        super.onDestroy();
    }

    private void loadRewardedVideoAd() {
        rewardedVideoAd.loadAd("ca-app-pub-8250381765915307/2501922021",
                new AdRequest.Builder().build());
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        final ImageView btnVideo = findViewById(R.id.id_btn_video);
        btnVideo.setVisibility(View.VISIBLE);
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rewardedVideoAd.show();
                btnVideo.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {

        myRef.child(userConvertido).child("coins").setValue(coinsDB + 20);
        tvMoedasResult.setText(String.valueOf(coinsDB + 20));
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        rewardedVideoAd.destroy(this);
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }
}

