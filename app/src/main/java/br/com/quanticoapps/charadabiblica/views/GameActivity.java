package br.com.quanticoapps.charadabiblica.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Random;

import br.com.quanticoapps.charadabiblica.R;

import static br.com.quanticoapps.charadabiblica.views.MainActivity.myRef;
import static br.com.quanticoapps.charadabiblica.views.MainActivity.ARQUIVO_PREFERENCIA;
import static br.com.quanticoapps.charadabiblica.views.MainActivity.listaPalavrasInicial;
import static br.com.quanticoapps.charadabiblica.views.SplashActivity.userConvertido;

public class GameActivity extends AppCompatActivity implements SensorEventListener {

    private TextView tvWords;
    private TextView tvTimer;
    private boolean countdown = false;
    private int score = 0;
    private boolean palavraGerada = false;
    private boolean tempoAtivado = false;
    private ArrayList<String> palavrasCorretas = new ArrayList<>();
    private ArrayList<String> palavrasPassadas = new ArrayList<>();
    private View view;
    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayerContagens;
    private AdView adView;
    float standardGravity;
    float thresholdGraqvity;
    private SensorManager mySensorManager;
    private Sensor myGravitySensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_game);


        if (MainActivity.anuncios != "noAds") {
            adView = findViewById(R.id.adBannerGame);
            AdRequest adRequestGame = new AdRequest.Builder().build();
            adView.loadAd(adRequestGame);
        }

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        tvWords = findViewById(R.id.id_word);
        tvTimer = findViewById(R.id.id_timer);


        standardGravity = SensorManager.STANDARD_GRAVITY;
        thresholdGraqvity = standardGravity / 2;

        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        myGravitySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        mySensorManager.registerListener(this, myGravitySensor, SensorManager.SENSOR_DELAY_GAME);

        view = findViewById(R.id.id_view_game);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor source = event.sensor;

        float z = event.values[2];
        BigDecimal zBD = BigDecimal.valueOf(z).setScale(1, RoundingMode.HALF_EVEN);

        if (source.getType() == Sensor.TYPE_GRAVITY) {

            if (zBD.floatValue() >= 6 && palavraGerada) {
                palavraGerada = false;
                view.setBackgroundColor(Color.parseColor("#e74c3c"));

                releasePlayer();
                mediaPlayer = MediaPlayer.create(GameActivity.this, R.raw.beeperror);
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }

                palavrasPassadas.add(tvWords.getText().toString());
                tvWords.setText("PASSOU!");

            }

            if (zBD.floatValue() <= -5 && palavraGerada) {
                palavraGerada = false;
                view.setBackgroundColor(Color.parseColor("#1abc9c"));

                releasePlayer();
                mediaPlayer = MediaPlayer.create(GameActivity.this, R.raw.correct);
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }

                palavrasCorretas.add(tvWords.getText().toString());
                score += 1;
                tvWords.setText("ACERTOU!");
            }

            if (zBD.floatValue() >= -4 && zBD.floatValue() <= 4) {
                view.setBackgroundColor(Color.parseColor("#FF9B59B6"));
                if (!countdown) {
                    contagemInicial();
                }

                if (tempoAtivado && countdown && !palavraGerada) {
                    gerarPalavra();
                }
            }
        }
    }

    private void gerarPalavra() {

        if (MainActivity.listaPalavrasInicial.size() > 1) {
            Random randomico = new Random();
            int numAleatorio = randomico.nextInt(listaPalavrasInicial.size());
            tvWords.setText(listaPalavrasInicial.get(numAleatorio).toUpperCase());
            listaPalavrasInicial.remove(numAleatorio);
            palavraGerada = true;
        } else {
            adicionaPacoteInicial();
            verificaPacotesOffline();
        }
    }


    private void contagemInicial() {
        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                countdown = true;
                tvWords.setText("PREPARE-SE: \n" + millisUntilFinished / 1000);
                releasePlayer();
                mediaPlayer = MediaPlayer.create(GameActivity.this, R.raw.beepcountdown2);
                mediaPlayer.start();
            }

            public void onFinish() {
                releasePlayer();
                mediaPlayer = MediaPlayer.create(GameActivity.this, R.raw.correct);
                mediaPlayer.start();
                contarTempo();
            }
        }.start();
    }

    private void contarTempo() {
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                tempoAtivado = true;
                tvTimer.setVisibility(View.VISIBLE);
                tvTimer.setText("" + millisUntilFinished / 1000);

                if (millisUntilFinished / 1000 < 10) {
                    releasePlayerContagens();
                    mediaPlayerContagens = MediaPlayer.create(GameActivity.this, R.raw.beepcountdown2);
                    mediaPlayerContagens.start();

                }
            }

            public void onFinish() {
                countdown = true;
                mySensorManager.unregisterListener(GameActivity.this);
                releasePlayer();
                mediaPlayer = MediaPlayer.create(GameActivity.this, R.raw.gameover);
                mediaPlayer.start();
                tvWords.setText("GAME OVER");
                tvTimer.setText("");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(GameActivity.this, ResultActivity.class);
                        intent.putExtra("palavrasCorretas", palavrasCorretas);
                        intent.putExtra("palavrasPassadas", palavrasPassadas);
                        intent.putExtra("pontos", score);
                        startActivity(intent);
                    }
                },3000);
            }
        }.start();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onStop() {
        releasePlayer();

        if (mediaPlayerContagens != null) {
            mediaPlayerContagens.stop();
            mediaPlayerContagens.release();
            mediaPlayerContagens = null;
        }

        if (adView != null) {
            adView.destroy();
        }
        super.onStop();

    }

    @Override
    protected void onPause() {

        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        releasePlayer();

        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "Você não pode voltar a partir desse ponto", Toast.LENGTH_LONG).show();
    }

    private void verificaPacotesOffline() {
        SharedPreferences sharedPreferences = getSharedPreferences(ARQUIVO_PREFERENCIA, 0);
        for (int i = 1; i <= 16; i++) {
            if (sharedPreferences.contains(String.valueOf(i))) {
                adicionaPacoteOffline(i);
            }
        }
    }

    private void adicionaPacoteOffline(int numPacote) {

        switch (numPacote) {
            case 1:
                listaPalavrasInicial.add(0, "Batalha");
                listaPalavrasInicial.add(0, "Busca");
                listaPalavrasInicial.add(0, "Cavaleiros");
                listaPalavrasInicial.add(0, "Confiança");
                listaPalavrasInicial.add(0, "Conhecimento");
                listaPalavrasInicial.add(0, "Derrota");
                listaPalavrasInicial.add(0, "Desânimo");
                listaPalavrasInicial.add(0, "Descanso");
                listaPalavrasInicial.add(0, "Egito");
                listaPalavrasInicial.add(0, "Estrelas");
                listaPalavrasInicial.add(0, "Forte");
                listaPalavrasInicial.add(0, "Fracos");
                listaPalavrasInicial.add(0, "Fraqueza");
                listaPalavrasInicial.add(0, "História");
                listaPalavrasInicial.add(0, "Mãe");
                listaPalavrasInicial.add(0, "Mensagem");
                listaPalavrasInicial.add(0, "Mentira");
                listaPalavrasInicial.add(0, "Multidões");
                listaPalavrasInicial.add(0, "Noé");
                listaPalavrasInicial.add(0, "Trono");
                listaPalavrasInicial.add(0, "1 Coríntios");
                listaPalavrasInicial.add(0, "1 Crônicas");
                listaPalavrasInicial.add(0, "Adoração");
                listaPalavrasInicial.add(0, "Feridas");
                listaPalavrasInicial.add(0, "Fermento");
                listaPalavrasInicial.add(0, "Glória");
                listaPalavrasInicial.add(0, "Inveja");
                listaPalavrasInicial.add(0, "Invisível");
                listaPalavrasInicial.add(0, "Jó");
                listaPalavrasInicial.add(0, "Raquel");
                listaPalavrasInicial.add(0, "Selo");
                listaPalavrasInicial.add(0, "Sonda");
                listaPalavrasInicial.add(0, "Tentação");
                listaPalavrasInicial.add(0, "Tomé");
                listaPalavrasInicial.add(0, "Torre de Babel");
                listaPalavrasInicial.add(0, "Transgressões");
                listaPalavrasInicial.add(0, "Tribulação");
                listaPalavrasInicial.add(0, "Tropa");
                listaPalavrasInicial.add(0, "Túnica");
                listaPalavrasInicial.add(0, "Vacas gordas");
                listaPalavrasInicial.add(0, "Ananias");
                listaPalavrasInicial.add(0, "Ascensão");
                listaPalavrasInicial.add(0, "Aser");
                listaPalavrasInicial.add(0, "Labão");
                listaPalavrasInicial.add(0, "Lagar");
                listaPalavrasInicial.add(0, "Porta dos céus");
                listaPalavrasInicial.add(0, "Porta Estreita");
                listaPalavrasInicial.add(0, "Proclamar");
                listaPalavrasInicial.add(0, "Prodígio");
                listaPalavrasInicial.add(0, "Rei dos Reis");
                break;

            case 2:
                listaPalavrasInicial.add(0, "1 João");
                listaPalavrasInicial.add(0, "1 Pedro");
                listaPalavrasInicial.add(0, "1 Reis");
                listaPalavrasInicial.add(0, "1 Samuel");
                listaPalavrasInicial.add(0, "Adorar");
                listaPalavrasInicial.add(0, "Adultério");
                listaPalavrasInicial.add(0, "Festa");
                listaPalavrasInicial.add(0, "João");
                listaPalavrasInicial.add(0, "João Batista");
                listaPalavrasInicial.add(0, "Lágrimas");
                listaPalavrasInicial.add(0, "Lamentações");
                listaPalavrasInicial.add(0, "Lâmpada");
                listaPalavrasInicial.add(0, "Oração");
                listaPalavrasInicial.add(0, "Pedro");
                listaPalavrasInicial.add(0, "Profecia");
                listaPalavrasInicial.add(0, "Profeta");
                listaPalavrasInicial.add(0, "Rãs");
                listaPalavrasInicial.add(0, "Rebanho");
                listaPalavrasInicial.add(0, "Riqueza");
                listaPalavrasInicial.add(0, "Último");
                listaPalavrasInicial.add(0, "André");
                listaPalavrasInicial.add(0, "Angústia");
                listaPalavrasInicial.add(0, "Assembleia");
                listaPalavrasInicial.add(0, "Bate-Seba");
                listaPalavrasInicial.add(0, "Cavalos de Fogo");
                listaPalavrasInicial.add(0, "Conquista");
                listaPalavrasInicial.add(0, "Consagração");
                listaPalavrasInicial.add(0, "Descendentes");
                listaPalavrasInicial.add(0, "Eunuco");
                listaPalavrasInicial.add(0, "Fraternal");
                listaPalavrasInicial.add(0, "Fronteira");
                listaPalavrasInicial.add(0, "Holocausto");
                listaPalavrasInicial.add(0, "Magos");
                listaPalavrasInicial.add(0, "Majestade");
                listaPalavrasInicial.add(0, "Multiplicação");
                listaPalavrasInicial.add(0, "Possível");
                listaPalavrasInicial.add(0, "Reino");
                listaPalavrasInicial.add(0, "Vacas magras");
                listaPalavrasInicial.add(0, "Vento");
                listaPalavrasInicial.add(0, "Ventre");
                listaPalavrasInicial.add(0, "Cafarnaum");
                listaPalavrasInicial.add(0, "Eira");
                listaPalavrasInicial.add(0, "Gólgota");
                listaPalavrasInicial.add(0, "Invocar");
                listaPalavrasInicial.add(0, "Meribá");
                listaPalavrasInicial.add(0, "Noemi");
                listaPalavrasInicial.add(0, "Santidade");
                listaPalavrasInicial.add(0, "Serpente de Bronze");
                listaPalavrasInicial.add(0, "Sinédrio");
                listaPalavrasInicial.add(0, "Síria");
                break;

            case 3:
                listaPalavrasInicial.add(0, "Animais");
                listaPalavrasInicial.add(0, "Anjo");
                listaPalavrasInicial.add(0, "Batismo");
                listaPalavrasInicial.add(0, "Belém");
                listaPalavrasInicial.add(0, "Bem");
                listaPalavrasInicial.add(0, "Caim");
                listaPalavrasInicial.add(0, "Cajado");
                listaPalavrasInicial.add(0, "Caverna");
                listaPalavrasInicial.add(0, "Cegos");
                listaPalavrasInicial.add(0, "Ceia");
                listaPalavrasInicial.add(0, "Conselho");
                listaPalavrasInicial.add(0, "Consolador");
                listaPalavrasInicial.add(0, "Desejo");
                listaPalavrasInicial.add(0, "Deserto");
                listaPalavrasInicial.add(0, "Desobediência");
                listaPalavrasInicial.add(0, "Fruto");
                listaPalavrasInicial.add(0, "Golias");
                listaPalavrasInicial.add(0, "Homem");
                listaPalavrasInicial.add(0, "Ira");
                listaPalavrasInicial.add(0, "Urso");
                listaPalavrasInicial.add(0, "1 Tessalonicenses");
                listaPalavrasInicial.add(0, "1 Timóteo");
                listaPalavrasInicial.add(0, "2 Coríntios");
                listaPalavrasInicial.add(0, "2 Crônicas");
                listaPalavrasInicial.add(0, "Adversários");
                listaPalavrasInicial.add(0, "Advertência");
                listaPalavrasInicial.add(0, "Eli");
                listaPalavrasInicial.add(0, "Lança");
                listaPalavrasInicial.add(0, "Lavradores");
                listaPalavrasInicial.add(0, "Lázaro");
                listaPalavrasInicial.add(0, "Leal");
                listaPalavrasInicial.add(0, "Novilho");
                listaPalavrasInicial.add(0, "Orgulho");
                listaPalavrasInicial.add(0, "Pensamentos");
                listaPalavrasInicial.add(0, "Profetizar");
                listaPalavrasInicial.add(0, "Rebeca");
                listaPalavrasInicial.add(0, "Rebelde");
                listaPalavrasInicial.add(0, "Rebelião");
                listaPalavrasInicial.add(0, "Soberano");
                listaPalavrasInicial.add(0, "Sobrecarregados");
                listaPalavrasInicial.add(0, "Assíria");
                listaPalavrasInicial.add(0, "Êutico");
                listaPalavrasInicial.add(0, "Festa das Cabanas");
                listaPalavrasInicial.add(0, "Joel");
                listaPalavrasInicial.add(0, "Malaquias");
                listaPalavrasInicial.add(0, "Postes Sagrados");
                listaPalavrasInicial.add(0, "Potifar");
                listaPalavrasInicial.add(0, "Reino dos Céus");
                listaPalavrasInicial.add(0, "Reis Magos");
                listaPalavrasInicial.add(0, "Renovo");
                break;

            case 4:
                listaPalavrasInicial.add(0, "2 João");
                listaPalavrasInicial.add(0, "2 Pedro");
                listaPalavrasInicial.add(0, "2 Reis");
                listaPalavrasInicial.add(0, "2 Samuel");
                listaPalavrasInicial.add(0, "Ataque");
                listaPalavrasInicial.add(0, "Elias");
                listaPalavrasInicial.add(0, "Eliseu");
                listaPalavrasInicial.add(0, "Emanuel");
                listaPalavrasInicial.add(0, "Eva");
                listaPalavrasInicial.add(0, "Evangelho");
                listaPalavrasInicial.add(0, "Leão");
                listaPalavrasInicial.add(0, "Leis");
                listaPalavrasInicial.add(0, "Maldade");
                listaPalavrasInicial.add(0, "Maldição");
                listaPalavrasInicial.add(0, "Maldito");
                listaPalavrasInicial.add(0, "Maná");
                listaPalavrasInicial.add(0, "Messias");
                listaPalavrasInicial.add(0, "Mestre");
                listaPalavrasInicial.add(0, "Mundo");
                listaPalavrasInicial.add(0, "Vale");
                listaPalavrasInicial.add(0, "Antepassados");
                listaPalavrasInicial.add(0, "Bem aventurado");
                listaPalavrasInicial.add(0, "Calabouço");
                listaPalavrasInicial.add(0, "Calebe");
                listaPalavrasInicial.add(0, "Celeiros");
                listaPalavrasInicial.add(0, "Consolo");
                listaPalavrasInicial.add(0, "Conspiração");
                listaPalavrasInicial.add(0, "Constranger");
                listaPalavrasInicial.add(0, "Fidelidade");
                listaPalavrasInicial.add(0, "Fruto do Espírito");
                listaPalavrasInicial.add(0, "Gomorra");
                listaPalavrasInicial.add(0, "Homicídio");
                listaPalavrasInicial.add(0, "Honestidade");
                listaPalavrasInicial.add(0, "Honra");
                listaPalavrasInicial.add(0, "Isabel");
                listaPalavrasInicial.add(0, "Isaías");
                listaPalavrasInicial.add(0, "Joio");
                listaPalavrasInicial.add(0, "Repouso");
                listaPalavrasInicial.add(0, "Repreensão");
                listaPalavrasInicial.add(0, "Resgate");
                listaPalavrasInicial.add(0, "Afligido");
                listaPalavrasInicial.add(0, "Ageu");
                listaPalavrasInicial.add(0, "Despojos");
                listaPalavrasInicial.add(0, "Oséias");
                listaPalavrasInicial.add(0, "Pentecoste");
                listaPalavrasInicial.add(0, "Recenseamento");
                listaPalavrasInicial.add(0, "Sumo Sacerdote");
                listaPalavrasInicial.add(0, "Sunamita");
                listaPalavrasInicial.add(0, "Tamareira");
                listaPalavrasInicial.add(0, "Tanque de Betesda");
                break;

            case 5:
                listaPalavrasInicial.add(0, "Água");
                listaPalavrasInicial.add(0, "Águias");
                listaPalavrasInicial.add(0, "Benção");
                listaPalavrasInicial.add(0, "Cálice");
                listaPalavrasInicial.add(0, "Calvário");
                listaPalavrasInicial.add(0, "Cama");
                listaPalavrasInicial.add(0, "Caminho");
                listaPalavrasInicial.add(0, "Deus");
                listaPalavrasInicial.add(0, "Fiel");
                listaPalavrasInicial.add(0, "Fuga");
                listaPalavrasInicial.add(0, "Isaque");
                listaPalavrasInicial.add(0, "Jonas");
                listaPalavrasInicial.add(0, "Novo Testamento");
                listaPalavrasInicial.add(0, "Ouro");
                listaPalavrasInicial.add(0, "Ovelha");
                listaPalavrasInicial.add(0, "Povo");
                listaPalavrasInicial.add(0, "Promessa");
                listaPalavrasInicial.add(0, "Rocha");
                listaPalavrasInicial.add(0, "Romanos");
                listaPalavrasInicial.add(0, "Valor");
                listaPalavrasInicial.add(0, "2 Tessalonicenses");
                listaPalavrasInicial.add(0, "2 Timóteo");
                listaPalavrasInicial.add(0, "Atento");
                listaPalavrasInicial.add(0, "Emaús");
                listaPalavrasInicial.add(0, "Embaixadores");
                listaPalavrasInicial.add(0, "Evangelistas");
                listaPalavrasInicial.add(0, "Exaltado");
                listaPalavrasInicial.add(0, "Mandamento");
                listaPalavrasInicial.add(0, "Muralhas");
                listaPalavrasInicial.add(0, "Pequeninos");
                listaPalavrasInicial.add(0, "Recompensa");
                listaPalavrasInicial.add(0, "Reconciliação");
                listaPalavrasInicial.add(0, "Reconstrução");
                listaPalavrasInicial.add(0, "Recursos");
                listaPalavrasInicial.add(0, "Respeitável");
                listaPalavrasInicial.add(0, "Súplica");
                listaPalavrasInicial.add(0, "Verbo");
                listaPalavrasInicial.add(0, "Vergonha");
                listaPalavrasInicial.add(0, "Visão");
                listaPalavrasInicial.add(0, "Voto");
                listaPalavrasInicial.add(0, "Anticristo");
                listaPalavrasInicial.add(0, "Centurião");
                listaPalavrasInicial.add(0, "Contrito");
                listaPalavrasInicial.add(0, "Gósen");
                listaPalavrasInicial.add(0, "Horebe");
                listaPalavrasInicial.add(0, "Leite e mel");
                listaPalavrasInicial.add(0, "Midianitas");
                listaPalavrasInicial.add(0, "Terra Natal");
                listaPalavrasInicial.add(0, "Tesouro Escondido");
                listaPalavrasInicial.add(0, "Tosquiadores");
                break;

            case 6:
                listaPalavrasInicial.add(0, "3 João");
                listaPalavrasInicial.add(0, "Antigo Testamento");
                listaPalavrasInicial.add(0, "Atos");
                listaPalavrasInicial.add(0, "Céu");
                listaPalavrasInicial.add(0, "Chamado");
                listaPalavrasInicial.add(0, "Chuva");
                listaPalavrasInicial.add(0, "Cidade");
                listaPalavrasInicial.add(0, "Exército");
                listaPalavrasInicial.add(0, "Governador");
                listaPalavrasInicial.add(0, "Governo");
                listaPalavrasInicial.add(0, "Graça");
                listaPalavrasInicial.add(0, "Grande");
                listaPalavrasInicial.add(0, "Lepra");
                listaPalavrasInicial.add(0, "Milagre");
                listaPalavrasInicial.add(0, "Músicos");
                listaPalavrasInicial.add(0, "Perdão");
                listaPalavrasInicial.add(0, "Perfeito");
                listaPalavrasInicial.add(0, "Rede");
                listaPalavrasInicial.add(0, "Sagrado");
                listaPalavrasInicial.add(0, "Vara");
                listaPalavrasInicial.add(0, "Ajoelhar");
                listaPalavrasInicial.add(0, "Canaã");
                listaPalavrasInicial.add(0, "Conversão");
                listaPalavrasInicial.add(0, "Cooperadores");
                listaPalavrasInicial.add(0, "Copeiro");
                listaPalavrasInicial.add(0, "Deus Forte");
                listaPalavrasInicial.add(0, "Figueira");
                listaPalavrasInicial.add(0, "Filemom");
                listaPalavrasInicial.add(0, "Funda");
                listaPalavrasInicial.add(0, "Fundamento");
                listaPalavrasInicial.add(0, "Humilde");
                listaPalavrasInicial.add(0, "Humilhar");
                listaPalavrasInicial.add(0, "Idolatria");
                listaPalavrasInicial.add(0, "Ismael");
                listaPalavrasInicial.add(0, "Números");
                listaPalavrasInicial.add(0, "Paciência");
                listaPalavrasInicial.add(0, "Povo santo");
                listaPalavrasInicial.add(0, "Testemunha");
                listaPalavrasInicial.add(0, "Testemunho");
                listaPalavrasInicial.add(0, "Tiago");
                listaPalavrasInicial.add(0, "Benção da Primogenitura");
                listaPalavrasInicial.add(0, "Emboscada");
                listaPalavrasInicial.add(0, "Jônatas");
                listaPalavrasInicial.add(0, "Mandrágoras");
                listaPalavrasInicial.add(0, "Propiciação");
                listaPalavrasInicial.add(0, "Resplandecer");
                listaPalavrasInicial.add(0, "Rúben");
                listaPalavrasInicial.add(0, "Transfiguração");
                listaPalavrasInicial.add(0, "Umbrais");
                listaPalavrasInicial.add(0, "Vanglória");
                break;

            case 7:
                listaPalavrasInicial.add(0, "Ajuda");
                listaPalavrasInicial.add(0, "Alegria");
                listaPalavrasInicial.add(0, "Canção");
                listaPalavrasInicial.add(0, "Coração");
                listaPalavrasInicial.add(0, "Coragem");
                listaPalavrasInicial.add(0, "Cordeiro");
                listaPalavrasInicial.add(0, "Coroa");
                listaPalavrasInicial.add(0, "Deuses");
                listaPalavrasInicial.add(0, "Filho");
                listaPalavrasInicial.add(0, "Ídolo");
                listaPalavrasInicial.add(0, "Imitadores");
                listaPalavrasInicial.add(0, "José");
                listaPalavrasInicial.add(0, "Manjedoura");
                listaPalavrasInicial.add(0, "Paciente");
                listaPalavrasInicial.add(0, "Padeiro");
                listaPalavrasInicial.add(0, "Pragas");
                listaPalavrasInicial.add(0, "Praia");
                listaPalavrasInicial.add(0, "Ressurreição");
                listaPalavrasInicial.add(0, "Sal");
                listaPalavrasInicial.add(0, "Voltar");
                listaPalavrasInicial.add(0, "Aba");
                listaPalavrasInicial.add(0, "Abandonado");
                listaPalavrasInicial.add(0, "Anunciar");
                listaPalavrasInicial.add(0, "Apedrejar");
                listaPalavrasInicial.add(0, "Autoridade");
                listaPalavrasInicial.add(0, "Bendito");
                listaPalavrasInicial.add(0, "Benjamim");
                listaPalavrasInicial.add(0, "Cidade de Davi");
                listaPalavrasInicial.add(0, "Endemoniado");
                listaPalavrasInicial.add(0, "Enfermidades");
                listaPalavrasInicial.add(0, "Enoque");
                listaPalavrasInicial.add(0, "Exilados");
                listaPalavrasInicial.add(0, "Exílio");
                listaPalavrasInicial.add(0, "Granizo");
                listaPalavrasInicial.add(0, "Leproso");
                listaPalavrasInicial.add(0, "Ministério");
                listaPalavrasInicial.add(0, "Ministro");
                listaPalavrasInicial.add(0, "Pérola");
                listaPalavrasInicial.add(0, "Propósito");
                listaPalavrasInicial.add(0, "Prosperar");
                listaPalavrasInicial.add(0, "Gade");
                listaPalavrasInicial.add(0, "Ismaelitas");
                listaPalavrasInicial.add(0, "Naamã");
                listaPalavrasInicial.add(0, "Obadias");
                listaPalavrasInicial.add(0, "Vara de Arão");
                listaPalavrasInicial.add(0, "Vestes sagradas");
                listaPalavrasInicial.add(0, "Vida Eterna");
                listaPalavrasInicial.add(0, "Zacarias");
                listaPalavrasInicial.add(0, "Zebulom");
                listaPalavrasInicial.add(0, "Zeloso");
                break;

            case 8:
                listaPalavrasInicial.add(0, "Abel");
                listaPalavrasInicial.add(0, "Apocalipse");
                listaPalavrasInicial.add(0, "Azeite");
                listaPalavrasInicial.add(0, "Besta");
                listaPalavrasInicial.add(0, "Cinzas");
                listaPalavrasInicial.add(0, "Ensino");
                listaPalavrasInicial.add(0, "Entendimento");
                listaPalavrasInicial.add(0, "Entrega");
                listaPalavrasInicial.add(0, "Êxodo");
                listaPalavrasInicial.add(0, "Israel");
                listaPalavrasInicial.add(0, "Israelitas");
                listaPalavrasInicial.add(0, "Proteção");
                listaPalavrasInicial.add(0, "Provas");
                listaPalavrasInicial.add(0, "Provérbios");
                listaPalavrasInicial.add(0, "Salmos");
                listaPalavrasInicial.add(0, "Salomão");
                listaPalavrasInicial.add(0, "Salvação");
                listaPalavrasInicial.add(0, "Salvador");
                listaPalavrasInicial.add(0, "Santo");
                listaPalavrasInicial.add(0, "Viúva");
                listaPalavrasInicial.add(0, "Aleluia");
                listaPalavrasInicial.add(0, "Alfa");
                listaPalavrasInicial.add(0, "Algema");
                listaPalavrasInicial.add(0, "Candeia");
                listaPalavrasInicial.add(0, "Candelabro");
                listaPalavrasInicial.add(0, "Coroação");
                listaPalavrasInicial.add(0, "Deuteronômio");
                listaPalavrasInicial.add(0, "Dez Mandamentos");
                listaPalavrasInicial.add(0, "Diácono");
                listaPalavrasInicial.add(0, "Filho da promessa");
                listaPalavrasInicial.add(0, "Filho do Homem");
                listaPalavrasInicial.add(0, "Gafanhotos");
                listaPalavrasInicial.add(0, "Gálatas");
                listaPalavrasInicial.add(0, "Josué");
                listaPalavrasInicial.add(0, "Mansidão");
                listaPalavrasInicial.add(0, "Manto");
                listaPalavrasInicial.add(0, "Mar da Galileia");
                listaPalavrasInicial.add(0, "Mar Vermelho");
                listaPalavrasInicial.add(0, "Maravilhas");
                listaPalavrasInicial.add(0, "Nação");
                listaPalavrasInicial.add(0, "Grão de Mostarda");
                listaPalavrasInicial.add(0, "Imoralidade");
                listaPalavrasInicial.add(0, "Levi");
                listaPalavrasInicial.add(0, "Miquéias");
                listaPalavrasInicial.add(0, "Miriã");
                listaPalavrasInicial.add(0, "Pães sem fermento");
                listaPalavrasInicial.add(0, "Pagãos");
                listaPalavrasInicial.add(0, "Pai da Eternidade");
                listaPalavrasInicial.add(0, "Perseguição");
                listaPalavrasInicial.add(0, "Prato de lentilhas");
                break;

            case 9:
                listaPalavrasInicial.add(0, "Aliança");
                listaPalavrasInicial.add(0, "Cansado");
                listaPalavrasInicial.add(0, "Corpo");
                listaPalavrasInicial.add(0, "Dificuldades");
                listaPalavrasInicial.add(0, "Digno");
                listaPalavrasInicial.add(0, "Dilúvio");
                listaPalavrasInicial.add(0, "Filho Pródigo");
                listaPalavrasInicial.add(0, "Imortal");
                listaPalavrasInicial.add(0, "Palavra");
                listaPalavrasInicial.add(0, "Semeador");
                listaPalavrasInicial.add(0, "Semente");
                listaPalavrasInicial.add(0, "Senhor");
                listaPalavrasInicial.add(0, "Servo");
                listaPalavrasInicial.add(0, "Sete");
                listaPalavrasInicial.add(0, "Socorro");
                listaPalavrasInicial.add(0, "Sonhador");
                listaPalavrasInicial.add(0, "Sonho");
                listaPalavrasInicial.add(0, "Surdos");
                listaPalavrasInicial.add(0, "Temor");
                listaPalavrasInicial.add(0, "Vitória");
                listaPalavrasInicial.add(0, "Baal");
                listaPalavrasInicial.add(0, "Babilônia");
                listaPalavrasInicial.add(0, "Betel");
                listaPalavrasInicial.add(0, "Bezerro de Ouro");
                listaPalavrasInicial.add(0, "Circuncisão");
                listaPalavrasInicial.add(0, "Clamor");
                listaPalavrasInicial.add(0, "Codornizes");
                listaPalavrasInicial.add(0, "Exortação");
                listaPalavrasInicial.add(0, "Gratidão");
                listaPalavrasInicial.add(0, "Levitas");
                listaPalavrasInicial.add(0, "Levítico");
                listaPalavrasInicial.add(0, "Mirra");
                listaPalavrasInicial.add(0, "Misericórdia");
                listaPalavrasInicial.add(0, "Obediência");
                listaPalavrasInicial.add(0, "Obras");
                listaPalavrasInicial.add(0, "Obreiro");
                listaPalavrasInicial.add(0, "Perseverança");
                listaPalavrasInicial.add(0, "Perverso");
                listaPalavrasInicial.add(0, "Prazeres");
                listaPalavrasInicial.add(0, "Provisão");
                listaPalavrasInicial.add(0, "Abigail");
                listaPalavrasInicial.add(0, "Apolo");
                listaPalavrasInicial.add(0, "Apostasia");
                listaPalavrasInicial.add(0, "Ervas amargas");
                listaPalavrasInicial.add(0, "Galileia");
                listaPalavrasInicial.add(0, "Gamaliel");
                listaPalavrasInicial.add(0, "Issacar");
                listaPalavrasInicial.add(0, "Jubileu");
                listaPalavrasInicial.add(0, "Maravilhoso Conselheiro");
                listaPalavrasInicial.add(0, "Naftali");
                break;

            case 10:
                listaPalavrasInicial.add(0, "Abraão");
                listaPalavrasInicial.add(0, "Apóstolos");
                listaPalavrasInicial.add(0, "Bíblia");
                listaPalavrasInicial.add(0, "Colheita");
                listaPalavrasInicial.add(0, "Esaú");
                listaPalavrasInicial.add(0, "Escolhido");
                listaPalavrasInicial.add(0, "Escravos");
                listaPalavrasInicial.add(0, "Grávida");
                listaPalavrasInicial.add(0, "Gregos");
                listaPalavrasInicial.add(0, "Guerra");
                listaPalavrasInicial.add(0, "Jacó");
                listaPalavrasInicial.add(0, "Jardim do Éden");
                listaPalavrasInicial.add(0, "Marcos");
                listaPalavrasInicial.add(0, "Maria");
                listaPalavrasInicial.add(0, "Missão");
                listaPalavrasInicial.add(0, "Nascimento");
                listaPalavrasInicial.add(0, "Pescadores");
                listaPalavrasInicial.add(0, "Tempestade");
                listaPalavrasInicial.add(0, "Templo");
                listaPalavrasInicial.add(0, "Virgem");
                listaPalavrasInicial.add(0, "Alicerce");
                listaPalavrasInicial.add(0, "Alma");
                listaPalavrasInicial.add(0, "Cantares de Salomão");
                listaPalavrasInicial.add(0, "Cântico");
                listaPalavrasInicial.add(0, "Corvo");
                listaPalavrasInicial.add(0, "Cova");
                listaPalavrasInicial.add(0, "Coxos");
                listaPalavrasInicial.add(0, "Filhos de Deus");
                listaPalavrasInicial.add(0, "Filipe");
                listaPalavrasInicial.add(0, "Filipenses");
                listaPalavrasInicial.add(0, "Filisteus");
                listaPalavrasInicial.add(0, "Gêmeos");
                listaPalavrasInicial.add(0, "Impiedade");
                listaPalavrasInicial.add(0, "Ímpio");
                listaPalavrasInicial.add(0, "Impossível");
                listaPalavrasInicial.add(0, "Incenso");
                listaPalavrasInicial.add(0, "Incredulidade");
                listaPalavrasInicial.add(0, "Judá");
                listaPalavrasInicial.add(0, "Judas ");
                listaPalavrasInicial.add(0, "Palha");
                listaPalavrasInicial.add(0, "Balaão");
                listaPalavrasInicial.add(0, "Diná");
                listaPalavrasInicial.add(0, "Expiação");
                listaPalavrasInicial.add(0, "Lia");
                listaPalavrasInicial.add(0, "Obstinados");
                listaPalavrasInicial.add(0, "Predecessor");
                listaPalavrasInicial.add(0, "Predição");
                listaPalavrasInicial.add(0, "Restauração");
                listaPalavrasInicial.add(0, "Retidão");
                listaPalavrasInicial.add(0, "Santuário");
                break;

            case 11:
                listaPalavrasInicial.add(0, "Altar");
                listaPalavrasInicial.add(0, "Baleia");
                listaPalavrasInicial.add(0, "Capacete");
                listaPalavrasInicial.add(0, "Crente");
                listaPalavrasInicial.add(0, "Crer");
                listaPalavrasInicial.add(0, "Criação");
                listaPalavrasInicial.add(0, "Criança");
                listaPalavrasInicial.add(0, "Cristãos");
                listaPalavrasInicial.add(0, "Cristo");
                listaPalavrasInicial.add(0, "Crucificação");
                listaPalavrasInicial.add(0, "Cruz");
                listaPalavrasInicial.add(0, "Culpa");
                listaPalavrasInicial.add(0, "Firme");
                listaPalavrasInicial.add(0, "Flecha");
                listaPalavrasInicial.add(0, "Infiel");
                listaPalavrasInicial.add(0, "Inimigos");
                listaPalavrasInicial.add(0, "Judas Iscariotes");
                listaPalavrasInicial.add(0, "Pão");
                listaPalavrasInicial.add(0, "Pregação");
                listaPalavrasInicial.add(0, "Vinho");
                listaPalavrasInicial.add(0, "Abrigo");
                listaPalavrasInicial.add(0, "Arão");
                listaPalavrasInicial.add(0, "Bispo");
                listaPalavrasInicial.add(0, "Colossenses");
                listaPalavrasInicial.add(0, "Coluna de Fogo");
                listaPalavrasInicial.add(0, "Coluna de Nuvem");
                listaPalavrasInicial.add(0, "Discernimento");
                listaPalavrasInicial.add(0, "Disciplina");
                listaPalavrasInicial.add(0, "Escudeiro");
                listaPalavrasInicial.add(0, "Ezequiel");
                listaPalavrasInicial.add(0, "Guerreiro");
                listaPalavrasInicial.add(0, "Liberdade");
                listaPalavrasInicial.add(0, "Libertador");
                listaPalavrasInicial.add(0, "Liderança");
                listaPalavrasInicial.add(0, "Líderes");
                listaPalavrasInicial.add(0, "Maria Madalena");
                listaPalavrasInicial.add(0, "Marta");
                listaPalavrasInicial.add(0, "Moderado");
                listaPalavrasInicial.add(0, "Ofensa");
                listaPalavrasInicial.add(0, "Prudente");
                listaPalavrasInicial.add(0, "Genealogia");
                listaPalavrasInicial.add(0, "Jefté");
                listaPalavrasInicial.add(0, "Natã");
                listaPalavrasInicial.add(0, "Pilatos");
                listaPalavrasInicial.add(0, "Senhor dos Exércitos");
                listaPalavrasInicial.add(0, "Senhor dos Senhores");
                listaPalavrasInicial.add(0, "Silas");
                listaPalavrasInicial.add(0, "Simão");
                listaPalavrasInicial.add(0, "Simeão");
                listaPalavrasInicial.add(0, "Sinagoga");
                break;

            case 12:
                listaPalavrasInicial.add(0, "Arca");
                listaPalavrasInicial.add(0, "Discípulos");
                listaPalavrasInicial.add(0, "Dívida");
                listaPalavrasInicial.add(0, "Dízimo");
                listaPalavrasInicial.add(0, "Doença");
                listaPalavrasInicial.add(0, "Escudo");
                listaPalavrasInicial.add(0, "Fama");
                listaPalavrasInicial.add(0, "Família");
                listaPalavrasInicial.add(0, "Faraó");
                listaPalavrasInicial.add(0, "Mateus");
                listaPalavrasInicial.add(0, "Moisés");
                listaPalavrasInicial.add(0, "Oferta");
                listaPalavrasInicial.add(0, "Piolhos");
                listaPalavrasInicial.add(0, "Planos");
                listaPalavrasInicial.add(0, "Reto");
                listaPalavrasInicial.add(0, "Retorno");
                listaPalavrasInicial.add(0, "Tempo");
                listaPalavrasInicial.add(0, "Tenda");
                listaPalavrasInicial.add(0, "Terremoto");
                listaPalavrasInicial.add(0, "Vida");
                listaPalavrasInicial.add(0, "Banquete");
                listaPalavrasInicial.add(0, "Capitão da Guarda");
                listaPalavrasInicial.add(0, "Caráter");
                listaPalavrasInicial.add(0, "Culpado");
                listaPalavrasInicial.add(0, "Generosidade");
                listaPalavrasInicial.add(0, "Jejum");
                listaPalavrasInicial.add(0, "Jeremias");
                listaPalavrasInicial.add(0, "Naufrágio");
                listaPalavrasInicial.add(0, "Parábola");
                listaPalavrasInicial.add(0, "Paralítico");
                listaPalavrasInicial.add(0, "Presbíteros");
                listaPalavrasInicial.add(0, "Publicano");
                listaPalavrasInicial.add(0, "Redenção");
                listaPalavrasInicial.add(0, "Rute");
                listaPalavrasInicial.add(0, "Salvo");
                listaPalavrasInicial.add(0, "Sara");
                listaPalavrasInicial.add(0, "Sarça");
                listaPalavrasInicial.add(0, "Sensato");
                listaPalavrasInicial.add(0, "Sodoma");
                listaPalavrasInicial.add(0, "Submissão");
                listaPalavrasInicial.add(0, "Acabe");
                listaPalavrasInicial.add(0, "Amabilidade");
                listaPalavrasInicial.add(0, "Blasfêmia");
                listaPalavrasInicial.add(0, "Coluna Sagrada");
                listaPalavrasInicial.add(0, "Fogo Consumidor");
                listaPalavrasInicial.add(0, "Habacuque");
                listaPalavrasInicial.add(0, "Iniquidade");
                listaPalavrasInicial.add(0, "Judeia");
                listaPalavrasInicial.add(0, "Linha de Batalha");
                listaPalavrasInicial.add(0, "Livramento");
                break;

            case 13:
                listaPalavrasInicial.add(0, "Acampamento");
                listaPalavrasInicial.add(0, "Amar");
                listaPalavrasInicial.add(0, "Barco");
                listaPalavrasInicial.add(0, "Carcereiro");
                listaPalavrasInicial.add(0, "Culto");
                listaPalavrasInicial.add(0, "Cura");
                listaPalavrasInicial.add(0, "Gênesis");
                listaPalavrasInicial.add(0, "Inocente");
                listaPalavrasInicial.add(0, "Páscoa");
                listaPalavrasInicial.add(0, "Passos");
                listaPalavrasInicial.add(0, "Pastor");
                listaPalavrasInicial.add(0, "Pastor de ovelhas");
                listaPalavrasInicial.add(0, "Presença");
                listaPalavrasInicial.add(0, "Presente");
                listaPalavrasInicial.add(0, "Presos");
                listaPalavrasInicial.add(0, "Primeiro");
                listaPalavrasInicial.add(0, "Pureza");
                listaPalavrasInicial.add(0, "Redentor");
                listaPalavrasInicial.add(0, "Sábado");
                listaPalavrasInicial.add(0, "VERDADEIRO");
                listaPalavrasInicial.add(0, "Arca da aliança");
                listaPalavrasInicial.add(0, "Boas Novas");
                listaPalavrasInicial.add(0, "Comandante");
                listaPalavrasInicial.add(0, "Domínio Próprio");
                listaPalavrasInicial.add(0, "Esdras");
                listaPalavrasInicial.add(0, "Fardo");
                listaPalavrasInicial.add(0, "Fariseus");
                listaPalavrasInicial.add(0, "Fôlego");
                listaPalavrasInicial.add(0, "Habitação");
                listaPalavrasInicial.add(0, "Judeus");
                listaPalavrasInicial.add(0, "Jugo");
                listaPalavrasInicial.add(0, "Ló");
                listaPalavrasInicial.add(0, "Oleiro");
                listaPalavrasInicial.add(0, "Plantações");
                listaPalavrasInicial.add(0, "Sustento");
                listaPalavrasInicial.add(0, "Tabernáculo");
                listaPalavrasInicial.add(0, "Timóteo");
                listaPalavrasInicial.add(0, "Ungir");
                listaPalavrasInicial.add(0, "Único");
                listaPalavrasInicial.add(0, "Unidade");
                listaPalavrasInicial.add(0, "Jericó");
                listaPalavrasInicial.add(0, "Matusalém");
                listaPalavrasInicial.add(0, "Monte das Oliveiras");
                listaPalavrasInicial.add(0, "Monte Sinai");
                listaPalavrasInicial.add(0, "Moriá");
                listaPalavrasInicial.add(0, "Naum");
                listaPalavrasInicial.add(0, "Nazaré");
                listaPalavrasInicial.add(0, "Nazireu");
                listaPalavrasInicial.add(0, "Retribuir");
                listaPalavrasInicial.add(0, "Samaria");
                break;

            case 14:
                listaPalavrasInicial.add(0, "Arco");
                listaPalavrasInicial.add(0, "Arco-íris");
                listaPalavrasInicial.add(0, "Armadura");
                listaPalavrasInicial.add(0, "Armas");
                listaPalavrasInicial.add(0, "Compaixão");
                listaPalavrasInicial.add(0, "Comunhão");
                listaPalavrasInicial.add(0, "Comunidade");
                listaPalavrasInicial.add(0, "Dons");
                listaPalavrasInicial.add(0, "Espada");
                listaPalavrasInicial.add(0, "Esperança");
                listaPalavrasInicial.add(0, "Esperar");
                listaPalavrasInicial.add(0, "Fé");
                listaPalavrasInicial.add(0, "Fome");
                listaPalavrasInicial.add(0, "Juiz");
                listaPalavrasInicial.add(0, "Juízes");
                listaPalavrasInicial.add(0, "Louvor");
                listaPalavrasInicial.add(0, "Lucas");
                listaPalavrasInicial.add(0, "Morte");
                listaPalavrasInicial.add(0, "Óleo");
                listaPalavrasInicial.add(0, "Verdade");
                listaPalavrasInicial.add(0, "Ação de graças");
                listaPalavrasInicial.add(0, "Amém");
                listaPalavrasInicial.add(0, "Insensato");
                listaPalavrasInicial.add(0, "Instruções");
                listaPalavrasInicial.add(0, "Jerusalém");
                listaPalavrasInicial.add(0, "Mediador");
                listaPalavrasInicial.add(0, "Meditar");
                listaPalavrasInicial.add(0, "Necessitado");
                listaPalavrasInicial.add(0, "Pátio");
                listaPalavrasInicial.add(0, "Primogênito");
                listaPalavrasInicial.add(0, "Purificação");
                listaPalavrasInicial.add(0, "Refinar");
                listaPalavrasInicial.add(0, "Refúgio");
                listaPalavrasInicial.add(0, "Samaritana");
                listaPalavrasInicial.add(0, "Véu");
                listaPalavrasInicial.add(0, "Videira");
                listaPalavrasInicial.add(0, "Vigília");
                listaPalavrasInicial.add(0, "Vigor");
                listaPalavrasInicial.add(0, "Vingança");
                listaPalavrasInicial.add(0, "Zaqueu");
                listaPalavrasInicial.add(0, "Barnabé");
                listaPalavrasInicial.add(0, "Boaz");
                listaPalavrasInicial.add(0, "Carmelo");
                listaPalavrasInicial.add(0, "Cutelo");
                listaPalavrasInicial.add(0, "Dã");
                listaPalavrasInicial.add(0, "Getsêmani");
                listaPalavrasInicial.add(0, "Hagar");
                listaPalavrasInicial.add(0, "Plenitude");
                listaPalavrasInicial.add(0, "Sarepta");
                listaPalavrasInicial.add(0, "Sentinela");
                break;

            case 15:
                listaPalavrasInicial.add(0, "Amigos");
                listaPalavrasInicial.add(0, "Amor");
                listaPalavrasInicial.add(0, "Harpa");
                listaPalavrasInicial.add(0, "Hebreus");
                listaPalavrasInicial.add(0, "Herança");
                listaPalavrasInicial.add(0, "Instrumentos");
                listaPalavrasInicial.add(0, "Medo");
                listaPalavrasInicial.add(0, "Paulo");
                listaPalavrasInicial.add(0, "Paz");
                listaPalavrasInicial.add(0, "Pecado");
                listaPalavrasInicial.add(0, "Poço");
                listaPalavrasInicial.add(0, "Poder");
                listaPalavrasInicial.add(0, "Príncipe");
                listaPalavrasInicial.add(0, "Puro");
                listaPalavrasInicial.add(0, "Rio");
                listaPalavrasInicial.add(0, "Sabedoria");
                listaPalavrasInicial.add(0, "Sábio");
                listaPalavrasInicial.add(0, "Sacerdote");
                listaPalavrasInicial.add(0, "Samuel");
                listaPalavrasInicial.add(0, "Vencedores");
                listaPalavrasInicial.add(0, "Arrependimento");
                listaPalavrasInicial.add(0, "Barrabás");
                listaPalavrasInicial.add(0, "Bonança");
                listaPalavrasInicial.add(0, "Carros de Guerra");
                listaPalavrasInicial.add(0, "Concubina");
                listaPalavrasInicial.add(0, "Dádiva");
                listaPalavrasInicial.add(0, "Dalila");
                listaPalavrasInicial.add(0, "Damasco");
                listaPalavrasInicial.add(0, "Doutrina");
                listaPalavrasInicial.add(0, "Espias");
                listaPalavrasInicial.add(0, "Feitiçaria");
                listaPalavrasInicial.add(0, "Forca");
                listaPalavrasInicial.add(0, "Gideão");
                listaPalavrasInicial.add(0, "Julgamento");
                listaPalavrasInicial.add(0, "Jumenta");
                listaPalavrasInicial.add(0, "Moscas");
                listaPalavrasInicial.add(0, "Saul");
                listaPalavrasInicial.add(0, "Sepulcro");
                listaPalavrasInicial.add(0, "Botija");
                listaPalavrasInicial.add(0, "Cativos");
                listaPalavrasInicial.add(0, "Acepção");
                listaPalavrasInicial.add(0, "Jessé");
                listaPalavrasInicial.add(0, "Lugar Santo");
                listaPalavrasInicial.add(0, "Neemias");
                listaPalavrasInicial.add(0, "Óleo de alegria");
                listaPalavrasInicial.add(0, "Regozijo");
                listaPalavrasInicial.add(0, "Sinal miraculoso");
                listaPalavrasInicial.add(0, "Sofonias");
                listaPalavrasInicial.add(0, "Tábuas da Aliança");
                listaPalavrasInicial.add(0, "Tadeu");
                break;

            case 16:
                listaPalavrasInicial.add(0, "Barro");
                listaPalavrasInicial.add(0, "Bondoso");
                listaPalavrasInicial.add(0, "Carta");
                listaPalavrasInicial.add(0, "Casamento");
                listaPalavrasInicial.add(0, "Castigo");
                listaPalavrasInicial.add(0, "Condenação");
                listaPalavrasInicial.add(0, "Danças");
                listaPalavrasInicial.add(0, "Daniel");
                listaPalavrasInicial.add(0, "Davi");
                listaPalavrasInicial.add(0, "Espírito");
                listaPalavrasInicial.add(0, "Espírito Santo");
                listaPalavrasInicial.add(0, "Forças");
                listaPalavrasInicial.add(0, "Gigantes");
                listaPalavrasInicial.add(0, "Jesus");
                listaPalavrasInicial.add(0, "Lutar");
                listaPalavrasInicial.add(0, "Luz");
                listaPalavrasInicial.add(0, "Mudo");
                listaPalavrasInicial.add(0, "Mulher");
                listaPalavrasInicial.add(0, "Olhos");
                listaPalavrasInicial.add(0, "Vaso");
                listaPalavrasInicial.add(0, "Açoites");
                listaPalavrasInicial.add(0, "Aconselhar");
                listaPalavrasInicial.add(0, "Herdeiro");
                listaPalavrasInicial.add(0, "Integridade");
                listaPalavrasInicial.add(0, "Íntegro");
                listaPalavrasInicial.add(0, "Intercessão");
                listaPalavrasInicial.add(0, "Nicodemos");
                listaPalavrasInicial.add(0, "Pecador");
                listaPalavrasInicial.add(0, "Pedido");
                listaPalavrasInicial.add(0, "Poderoso");
                listaPalavrasInicial.add(0, "Príncipe da Paz");
                listaPalavrasInicial.add(0, "Quebrantado");
                listaPalavrasInicial.add(0, "Queda");
                listaPalavrasInicial.add(0, "Querubim");
                listaPalavrasInicial.add(0, "Sacrifício");
                listaPalavrasInicial.add(0, "Conduta");
                listaPalavrasInicial.add(0, "Confessar");
                listaPalavrasInicial.add(0, "Débora");
                listaPalavrasInicial.add(0, "Decretos");
                listaPalavrasInicial.add(0, "Dedicado");
                listaPalavrasInicial.add(0, "Amós");
                listaPalavrasInicial.add(0, "Árvore da Vida");
                listaPalavrasInicial.add(0, "Dracma");
                listaPalavrasInicial.add(0, "Feixes");
                listaPalavrasInicial.add(0, "Juramento");
                listaPalavrasInicial.add(0, "Mefibosete");
                listaPalavrasInicial.add(0, "Melquisedeque");
                listaPalavrasInicial.add(0, "Rio Eufrates");
                listaPalavrasInicial.add(0, "Sepultura");
                listaPalavrasInicial.add(0, "Tenda da Congregação");
                break;
        }
    }

    private void verificaPacotesOnline() {
        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (int i = 1; i <= 17; i++) {
                    String posicao = String.valueOf(dataSnapshot.child(userConvertido).child("pacotes").child(String.valueOf(i)).child("compras").getValue());
                    if (posicao.equals("comprado")) {
                        adicionaPacoteOnline(i);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void adicionaPacoteOnline(int numPacote) {

        switch (numPacote) {
            case 1:
                listaPalavrasInicial.add(0, "Batalha");
                listaPalavrasInicial.add(0, "Busca");
                listaPalavrasInicial.add(0, "Cavaleiros");
                listaPalavrasInicial.add(0, "Confiança");
                listaPalavrasInicial.add(0, "Conhecimento");
                listaPalavrasInicial.add(0, "Derrota");
                listaPalavrasInicial.add(0, "Desânimo");
                listaPalavrasInicial.add(0, "Descanso");
                listaPalavrasInicial.add(0, "Egito");
                listaPalavrasInicial.add(0, "Estrelas");
                listaPalavrasInicial.add(0, "Forte");
                listaPalavrasInicial.add(0, "Fracos");
                listaPalavrasInicial.add(0, "Fraqueza");
                listaPalavrasInicial.add(0, "História");
                listaPalavrasInicial.add(0, "Mãe");
                listaPalavrasInicial.add(0, "Mensagem");
                listaPalavrasInicial.add(0, "Mentira");
                listaPalavrasInicial.add(0, "Multidões");
                listaPalavrasInicial.add(0, "Noé");
                listaPalavrasInicial.add(0, "Trono");
                listaPalavrasInicial.add(0, "1 Coríntios");
                listaPalavrasInicial.add(0, "1 Crônicas");
                listaPalavrasInicial.add(0, "Adoração");
                listaPalavrasInicial.add(0, "Feridas");
                listaPalavrasInicial.add(0, "Fermento");
                listaPalavrasInicial.add(0, "Glória");
                listaPalavrasInicial.add(0, "Inveja");
                listaPalavrasInicial.add(0, "Invisível");
                listaPalavrasInicial.add(0, "Jó");
                listaPalavrasInicial.add(0, "Raquel");
                listaPalavrasInicial.add(0, "Selo");
                listaPalavrasInicial.add(0, "Sonda");
                listaPalavrasInicial.add(0, "Tentação");
                listaPalavrasInicial.add(0, "Tomé");
                listaPalavrasInicial.add(0, "Torre de Babel");
                listaPalavrasInicial.add(0, "Transgressões");
                listaPalavrasInicial.add(0, "Tribulação");
                listaPalavrasInicial.add(0, "Tropa");
                listaPalavrasInicial.add(0, "Túnica");
                listaPalavrasInicial.add(0, "Vacas gordas");
                listaPalavrasInicial.add(0, "Ananias");
                listaPalavrasInicial.add(0, "Ascensão");
                listaPalavrasInicial.add(0, "Aser");
                listaPalavrasInicial.add(0, "Labão");
                listaPalavrasInicial.add(0, "Lagar");
                listaPalavrasInicial.add(0, "Porta dos céus");
                listaPalavrasInicial.add(0, "Porta Estreita");
                listaPalavrasInicial.add(0, "Proclamar");
                listaPalavrasInicial.add(0, "Prodígio");
                listaPalavrasInicial.add(0, "Rei dos Reis");
                break;

            case 2:
                listaPalavrasInicial.add(0, "1 João");
                listaPalavrasInicial.add(0, "1 Pedro");
                listaPalavrasInicial.add(0, "1 Reis");
                listaPalavrasInicial.add(0, "1 Samuel");
                listaPalavrasInicial.add(0, "Adorar");
                listaPalavrasInicial.add(0, "Adultério");
                listaPalavrasInicial.add(0, "Festa");
                listaPalavrasInicial.add(0, "João");
                listaPalavrasInicial.add(0, "João Batista");
                listaPalavrasInicial.add(0, "Lágrimas");
                listaPalavrasInicial.add(0, "Lamentações");
                listaPalavrasInicial.add(0, "Lâmpada");
                listaPalavrasInicial.add(0, "Oração");
                listaPalavrasInicial.add(0, "Pedro");
                listaPalavrasInicial.add(0, "Profecia");
                listaPalavrasInicial.add(0, "Profeta");
                listaPalavrasInicial.add(0, "Rãs");
                listaPalavrasInicial.add(0, "Rebanho");
                listaPalavrasInicial.add(0, "Riqueza");
                listaPalavrasInicial.add(0, "Último");
                listaPalavrasInicial.add(0, "André");
                listaPalavrasInicial.add(0, "Angústia");
                listaPalavrasInicial.add(0, "Assembleia");
                listaPalavrasInicial.add(0, "Bate-Seba");
                listaPalavrasInicial.add(0, "Cavalos de Fogo");
                listaPalavrasInicial.add(0, "Conquista");
                listaPalavrasInicial.add(0, "Consagração");
                listaPalavrasInicial.add(0, "Descendentes");
                listaPalavrasInicial.add(0, "Eunuco");
                listaPalavrasInicial.add(0, "Fraternal");
                listaPalavrasInicial.add(0, "Fronteira");
                listaPalavrasInicial.add(0, "Holocausto");
                listaPalavrasInicial.add(0, "Magos");
                listaPalavrasInicial.add(0, "Majestade");
                listaPalavrasInicial.add(0, "Multiplicação");
                listaPalavrasInicial.add(0, "Possível");
                listaPalavrasInicial.add(0, "Reino");
                listaPalavrasInicial.add(0, "Vacas magras");
                listaPalavrasInicial.add(0, "Vento");
                listaPalavrasInicial.add(0, "Ventre");
                listaPalavrasInicial.add(0, "Cafarnaum");
                listaPalavrasInicial.add(0, "Eira");
                listaPalavrasInicial.add(0, "Gólgota");
                listaPalavrasInicial.add(0, "Invocar");
                listaPalavrasInicial.add(0, "Meribá");
                listaPalavrasInicial.add(0, "Noemi");
                listaPalavrasInicial.add(0, "Santidade");
                listaPalavrasInicial.add(0, "Serpente de Bronze");
                listaPalavrasInicial.add(0, "Sinédrio");
                listaPalavrasInicial.add(0, "Síria");
                break;

            case 3:
                listaPalavrasInicial.add(0, "Animais");
                listaPalavrasInicial.add(0, "Anjo");
                listaPalavrasInicial.add(0, "Batismo");
                listaPalavrasInicial.add(0, "Belém");
                listaPalavrasInicial.add(0, "Bem");
                listaPalavrasInicial.add(0, "Caim");
                listaPalavrasInicial.add(0, "Cajado");
                listaPalavrasInicial.add(0, "Caverna");
                listaPalavrasInicial.add(0, "Cegos");
                listaPalavrasInicial.add(0, "Ceia");
                listaPalavrasInicial.add(0, "Conselho");
                listaPalavrasInicial.add(0, "Consolador");
                listaPalavrasInicial.add(0, "Desejo");
                listaPalavrasInicial.add(0, "Deserto");
                listaPalavrasInicial.add(0, "Desobediência");
                listaPalavrasInicial.add(0, "Fruto");
                listaPalavrasInicial.add(0, "Golias");
                listaPalavrasInicial.add(0, "Homem");
                listaPalavrasInicial.add(0, "Ira");
                listaPalavrasInicial.add(0, "Urso");
                listaPalavrasInicial.add(0, "1 Tessalonicenses");
                listaPalavrasInicial.add(0, "1 Timóteo");
                listaPalavrasInicial.add(0, "2 Coríntios");
                listaPalavrasInicial.add(0, "2 Crônicas");
                listaPalavrasInicial.add(0, "Adversários");
                listaPalavrasInicial.add(0, "Advertência");
                listaPalavrasInicial.add(0, "Eli");
                listaPalavrasInicial.add(0, "Lança");
                listaPalavrasInicial.add(0, "Lavradores");
                listaPalavrasInicial.add(0, "Lázaro");
                listaPalavrasInicial.add(0, "Leal");
                listaPalavrasInicial.add(0, "Novilho");
                listaPalavrasInicial.add(0, "Orgulho");
                listaPalavrasInicial.add(0, "Pensamentos");
                listaPalavrasInicial.add(0, "Profetizar");
                listaPalavrasInicial.add(0, "Rebeca");
                listaPalavrasInicial.add(0, "Rebelde");
                listaPalavrasInicial.add(0, "Rebelião");
                listaPalavrasInicial.add(0, "Soberano");
                listaPalavrasInicial.add(0, "Sobrecarregados");
                listaPalavrasInicial.add(0, "Assíria");
                listaPalavrasInicial.add(0, "Êutico");
                listaPalavrasInicial.add(0, "Festa das Cabanas");
                listaPalavrasInicial.add(0, "Joel");
                listaPalavrasInicial.add(0, "Malaquias");
                listaPalavrasInicial.add(0, "Postes Sagrados");
                listaPalavrasInicial.add(0, "Potifar");
                listaPalavrasInicial.add(0, "Reino dos Céus");
                listaPalavrasInicial.add(0, "Reis Magos");
                listaPalavrasInicial.add(0, "Renovo");
                break;

            case 4:
                listaPalavrasInicial.add(0, "2 João");
                listaPalavrasInicial.add(0, "2 Pedro");
                listaPalavrasInicial.add(0, "2 Reis");
                listaPalavrasInicial.add(0, "2 Samuel");
                listaPalavrasInicial.add(0, "Ataque");
                listaPalavrasInicial.add(0, "Elias");
                listaPalavrasInicial.add(0, "Eliseu");
                listaPalavrasInicial.add(0, "Emanuel");
                listaPalavrasInicial.add(0, "Eva");
                listaPalavrasInicial.add(0, "Evangelho");
                listaPalavrasInicial.add(0, "Leão");
                listaPalavrasInicial.add(0, "Leis");
                listaPalavrasInicial.add(0, "Maldade");
                listaPalavrasInicial.add(0, "Maldição");
                listaPalavrasInicial.add(0, "Maldito");
                listaPalavrasInicial.add(0, "Maná");
                listaPalavrasInicial.add(0, "Messias");
                listaPalavrasInicial.add(0, "Mestre");
                listaPalavrasInicial.add(0, "Mundo");
                listaPalavrasInicial.add(0, "Vale");
                listaPalavrasInicial.add(0, "Antepassados");
                listaPalavrasInicial.add(0, "Bem aventurado");
                listaPalavrasInicial.add(0, "Calabouço");
                listaPalavrasInicial.add(0, "Calebe");
                listaPalavrasInicial.add(0, "Celeiros");
                listaPalavrasInicial.add(0, "Consolo");
                listaPalavrasInicial.add(0, "Conspiração");
                listaPalavrasInicial.add(0, "Constranger");
                listaPalavrasInicial.add(0, "Fidelidade");
                listaPalavrasInicial.add(0, "Fruto do Espírito");
                listaPalavrasInicial.add(0, "Gomorra");
                listaPalavrasInicial.add(0, "Homicídio");
                listaPalavrasInicial.add(0, "Honestidade");
                listaPalavrasInicial.add(0, "Honra");
                listaPalavrasInicial.add(0, "Isabel");
                listaPalavrasInicial.add(0, "Isaías");
                listaPalavrasInicial.add(0, "Joio");
                listaPalavrasInicial.add(0, "Repouso");
                listaPalavrasInicial.add(0, "Repreensão");
                listaPalavrasInicial.add(0, "Resgate");
                listaPalavrasInicial.add(0, "Afligido");
                listaPalavrasInicial.add(0, "Ageu");
                listaPalavrasInicial.add(0, "Despojos");
                listaPalavrasInicial.add(0, "Oséias");
                listaPalavrasInicial.add(0, "Pentecoste");
                listaPalavrasInicial.add(0, "Recenseamento");
                listaPalavrasInicial.add(0, "Sumo Sacerdote");
                listaPalavrasInicial.add(0, "Sunamita");
                listaPalavrasInicial.add(0, "Tamareira");
                listaPalavrasInicial.add(0, "Tanque de Betesda");
                break;

            case 5:
                listaPalavrasInicial.add(0, "Água");
                listaPalavrasInicial.add(0, "Águias");
                listaPalavrasInicial.add(0, "Benção");
                listaPalavrasInicial.add(0, "Cálice");
                listaPalavrasInicial.add(0, "Calvário");
                listaPalavrasInicial.add(0, "Cama");
                listaPalavrasInicial.add(0, "Caminho");
                listaPalavrasInicial.add(0, "Deus");
                listaPalavrasInicial.add(0, "Fiel");
                listaPalavrasInicial.add(0, "Fuga");
                listaPalavrasInicial.add(0, "Isaque");
                listaPalavrasInicial.add(0, "Jonas");
                listaPalavrasInicial.add(0, "Novo Testamento");
                listaPalavrasInicial.add(0, "Ouro");
                listaPalavrasInicial.add(0, "Ovelha");
                listaPalavrasInicial.add(0, "Povo");
                listaPalavrasInicial.add(0, "Promessa");
                listaPalavrasInicial.add(0, "Rocha");
                listaPalavrasInicial.add(0, "Romanos");
                listaPalavrasInicial.add(0, "Valor");
                listaPalavrasInicial.add(0, "2 Tessalonicenses");
                listaPalavrasInicial.add(0, "2 Timóteo");
                listaPalavrasInicial.add(0, "Atento");
                listaPalavrasInicial.add(0, "Emaús");
                listaPalavrasInicial.add(0, "Embaixadores");
                listaPalavrasInicial.add(0, "Evangelistas");
                listaPalavrasInicial.add(0, "Exaltado");
                listaPalavrasInicial.add(0, "Mandamento");
                listaPalavrasInicial.add(0, "Muralhas");
                listaPalavrasInicial.add(0, "Pequeninos");
                listaPalavrasInicial.add(0, "Recompensa");
                listaPalavrasInicial.add(0, "Reconciliação");
                listaPalavrasInicial.add(0, "Reconstrução");
                listaPalavrasInicial.add(0, "Recursos");
                listaPalavrasInicial.add(0, "Respeitável");
                listaPalavrasInicial.add(0, "Súplica");
                listaPalavrasInicial.add(0, "Verbo");
                listaPalavrasInicial.add(0, "Vergonha");
                listaPalavrasInicial.add(0, "Visão");
                listaPalavrasInicial.add(0, "Voto");
                listaPalavrasInicial.add(0, "Anticristo");
                listaPalavrasInicial.add(0, "Centurião");
                listaPalavrasInicial.add(0, "Contrito");
                listaPalavrasInicial.add(0, "Gósen");
                listaPalavrasInicial.add(0, "Horebe");
                listaPalavrasInicial.add(0, "Leite e mel");
                listaPalavrasInicial.add(0, "Midianitas");
                listaPalavrasInicial.add(0, "Terra Natal");
                listaPalavrasInicial.add(0, "Tesouro Escondido");
                listaPalavrasInicial.add(0, "Tosquiadores");
                break;

            case 6:
                listaPalavrasInicial.add(0, "3 João");
                listaPalavrasInicial.add(0, "Antigo Testamento");
                listaPalavrasInicial.add(0, "Atos");
                listaPalavrasInicial.add(0, "Céu");
                listaPalavrasInicial.add(0, "Chamado");
                listaPalavrasInicial.add(0, "Chuva");
                listaPalavrasInicial.add(0, "Cidade");
                listaPalavrasInicial.add(0, "Exército");
                listaPalavrasInicial.add(0, "Governador");
                listaPalavrasInicial.add(0, "Governo");
                listaPalavrasInicial.add(0, "Graça");
                listaPalavrasInicial.add(0, "Grande");
                listaPalavrasInicial.add(0, "Lepra");
                listaPalavrasInicial.add(0, "Milagre");
                listaPalavrasInicial.add(0, "Músicos");
                listaPalavrasInicial.add(0, "Perdão");
                listaPalavrasInicial.add(0, "Perfeito");
                listaPalavrasInicial.add(0, "Rede");
                listaPalavrasInicial.add(0, "Sagrado");
                listaPalavrasInicial.add(0, "Vara");
                listaPalavrasInicial.add(0, "Ajoelhar");
                listaPalavrasInicial.add(0, "Canaã");
                listaPalavrasInicial.add(0, "Conversão");
                listaPalavrasInicial.add(0, "Cooperadores");
                listaPalavrasInicial.add(0, "Copeiro");
                listaPalavrasInicial.add(0, "Deus Forte");
                listaPalavrasInicial.add(0, "Figueira");
                listaPalavrasInicial.add(0, "Filemom");
                listaPalavrasInicial.add(0, "Funda");
                listaPalavrasInicial.add(0, "Fundamento");
                listaPalavrasInicial.add(0, "Humilde");
                listaPalavrasInicial.add(0, "Humilhar");
                listaPalavrasInicial.add(0, "Idolatria");
                listaPalavrasInicial.add(0, "Ismael");
                listaPalavrasInicial.add(0, "Números");
                listaPalavrasInicial.add(0, "Paciência");
                listaPalavrasInicial.add(0, "Povo santo");
                listaPalavrasInicial.add(0, "Testemunha");
                listaPalavrasInicial.add(0, "Testemunho");
                listaPalavrasInicial.add(0, "Tiago");
                listaPalavrasInicial.add(0, "Benção da Primogenitura");
                listaPalavrasInicial.add(0, "Emboscada");
                listaPalavrasInicial.add(0, "Jônatas");
                listaPalavrasInicial.add(0, "Mandrágoras");
                listaPalavrasInicial.add(0, "Propiciação");
                listaPalavrasInicial.add(0, "Resplandecer");
                listaPalavrasInicial.add(0, "Rúben");
                listaPalavrasInicial.add(0, "Transfiguração");
                listaPalavrasInicial.add(0, "Umbrais");
                listaPalavrasInicial.add(0, "Vanglória");
                break;

            case 7:
                listaPalavrasInicial.add(0, "Ajuda");
                listaPalavrasInicial.add(0, "Alegria");
                listaPalavrasInicial.add(0, "Canção");
                listaPalavrasInicial.add(0, "Coração");
                listaPalavrasInicial.add(0, "Coragem");
                listaPalavrasInicial.add(0, "Cordeiro");
                listaPalavrasInicial.add(0, "Coroa");
                listaPalavrasInicial.add(0, "Deuses");
                listaPalavrasInicial.add(0, "Filho");
                listaPalavrasInicial.add(0, "Ídolo");
                listaPalavrasInicial.add(0, "Imitadores");
                listaPalavrasInicial.add(0, "José");
                listaPalavrasInicial.add(0, "Manjedoura");
                listaPalavrasInicial.add(0, "Paciente");
                listaPalavrasInicial.add(0, "Padeiro");
                listaPalavrasInicial.add(0, "Pragas");
                listaPalavrasInicial.add(0, "Praia");
                listaPalavrasInicial.add(0, "Ressurreição");
                listaPalavrasInicial.add(0, "Sal");
                listaPalavrasInicial.add(0, "Voltar");
                listaPalavrasInicial.add(0, "Aba");
                listaPalavrasInicial.add(0, "Abandonado");
                listaPalavrasInicial.add(0, "Anunciar");
                listaPalavrasInicial.add(0, "Apedrejar");
                listaPalavrasInicial.add(0, "Autoridade");
                listaPalavrasInicial.add(0, "Bendito");
                listaPalavrasInicial.add(0, "Benjamim");
                listaPalavrasInicial.add(0, "Cidade de Davi");
                listaPalavrasInicial.add(0, "Endemoniado");
                listaPalavrasInicial.add(0, "Enfermidades");
                listaPalavrasInicial.add(0, "Enoque");
                listaPalavrasInicial.add(0, "Exilados");
                listaPalavrasInicial.add(0, "Exílio");
                listaPalavrasInicial.add(0, "Granizo");
                listaPalavrasInicial.add(0, "Leproso");
                listaPalavrasInicial.add(0, "Ministério");
                listaPalavrasInicial.add(0, "Ministro");
                listaPalavrasInicial.add(0, "Pérola");
                listaPalavrasInicial.add(0, "Propósito");
                listaPalavrasInicial.add(0, "Prosperar");
                listaPalavrasInicial.add(0, "Gade");
                listaPalavrasInicial.add(0, "Ismaelitas");
                listaPalavrasInicial.add(0, "Naamã");
                listaPalavrasInicial.add(0, "Obadias");
                listaPalavrasInicial.add(0, "Vara de Arão");
                listaPalavrasInicial.add(0, "Vestes sagradas");
                listaPalavrasInicial.add(0, "Vida Eterna");
                listaPalavrasInicial.add(0, "Zacarias");
                listaPalavrasInicial.add(0, "Zebulom");
                listaPalavrasInicial.add(0, "Zeloso");
                break;

            case 8:
                listaPalavrasInicial.add(0, "Abel");
                listaPalavrasInicial.add(0, "Apocalipse");
                listaPalavrasInicial.add(0, "Azeite");
                listaPalavrasInicial.add(0, "Besta");
                listaPalavrasInicial.add(0, "Cinzas");
                listaPalavrasInicial.add(0, "Ensino");
                listaPalavrasInicial.add(0, "Entendimento");
                listaPalavrasInicial.add(0, "Entrega");
                listaPalavrasInicial.add(0, "Êxodo");
                listaPalavrasInicial.add(0, "Israel");
                listaPalavrasInicial.add(0, "Israelitas");
                listaPalavrasInicial.add(0, "Proteção");
                listaPalavrasInicial.add(0, "Provas");
                listaPalavrasInicial.add(0, "Provérbios");
                listaPalavrasInicial.add(0, "Salmos");
                listaPalavrasInicial.add(0, "Salomão");
                listaPalavrasInicial.add(0, "Salvação");
                listaPalavrasInicial.add(0, "Salvador");
                listaPalavrasInicial.add(0, "Santo");
                listaPalavrasInicial.add(0, "Viúva");
                listaPalavrasInicial.add(0, "Aleluia");
                listaPalavrasInicial.add(0, "Alfa");
                listaPalavrasInicial.add(0, "Algema");
                listaPalavrasInicial.add(0, "Candeia");
                listaPalavrasInicial.add(0, "Candelabro");
                listaPalavrasInicial.add(0, "Coroação");
                listaPalavrasInicial.add(0, "Deuteronômio");
                listaPalavrasInicial.add(0, "Dez Mandamentos");
                listaPalavrasInicial.add(0, "Diácono");
                listaPalavrasInicial.add(0, "Filho da promessa");
                listaPalavrasInicial.add(0, "Filho do Homem");
                listaPalavrasInicial.add(0, "Gafanhotos");
                listaPalavrasInicial.add(0, "Gálatas");
                listaPalavrasInicial.add(0, "Josué");
                listaPalavrasInicial.add(0, "Mansidão");
                listaPalavrasInicial.add(0, "Manto");
                listaPalavrasInicial.add(0, "Mar da Galileia");
                listaPalavrasInicial.add(0, "Mar Vermelho");
                listaPalavrasInicial.add(0, "Maravilhas");
                listaPalavrasInicial.add(0, "Nação");
                listaPalavrasInicial.add(0, "Grão de Mostarda");
                listaPalavrasInicial.add(0, "Imoralidade");
                listaPalavrasInicial.add(0, "Levi");
                listaPalavrasInicial.add(0, "Miquéias");
                listaPalavrasInicial.add(0, "Miriã");
                listaPalavrasInicial.add(0, "Pães sem fermento");
                listaPalavrasInicial.add(0, "Pagãos");
                listaPalavrasInicial.add(0, "Pai da Eternidade");
                listaPalavrasInicial.add(0, "Perseguição");
                listaPalavrasInicial.add(0, "Prato de lentilhas");
                break;

            case 9:
                listaPalavrasInicial.add(0, "Aliança");
                listaPalavrasInicial.add(0, "Cansado");
                listaPalavrasInicial.add(0, "Corpo");
                listaPalavrasInicial.add(0, "Dificuldades");
                listaPalavrasInicial.add(0, "Digno");
                listaPalavrasInicial.add(0, "Dilúvio");
                listaPalavrasInicial.add(0, "Filho Pródigo");
                listaPalavrasInicial.add(0, "Imortal");
                listaPalavrasInicial.add(0, "Palavra");
                listaPalavrasInicial.add(0, "Semeador");
                listaPalavrasInicial.add(0, "Semente");
                listaPalavrasInicial.add(0, "Senhor");
                listaPalavrasInicial.add(0, "Servo");
                listaPalavrasInicial.add(0, "Sete");
                listaPalavrasInicial.add(0, "Socorro");
                listaPalavrasInicial.add(0, "Sonhador");
                listaPalavrasInicial.add(0, "Sonho");
                listaPalavrasInicial.add(0, "Surdos");
                listaPalavrasInicial.add(0, "Temor");
                listaPalavrasInicial.add(0, "Vitória");
                listaPalavrasInicial.add(0, "Baal");
                listaPalavrasInicial.add(0, "Babilônia");
                listaPalavrasInicial.add(0, "Betel");
                listaPalavrasInicial.add(0, "Bezerro de Ouro");
                listaPalavrasInicial.add(0, "Circuncisão");
                listaPalavrasInicial.add(0, "Clamor");
                listaPalavrasInicial.add(0, "Codornizes");
                listaPalavrasInicial.add(0, "Exortação");
                listaPalavrasInicial.add(0, "Gratidão");
                listaPalavrasInicial.add(0, "Levitas");
                listaPalavrasInicial.add(0, "Levítico");
                listaPalavrasInicial.add(0, "Mirra");
                listaPalavrasInicial.add(0, "Misericórdia");
                listaPalavrasInicial.add(0, "Obediência");
                listaPalavrasInicial.add(0, "Obras");
                listaPalavrasInicial.add(0, "Obreiro");
                listaPalavrasInicial.add(0, "Perseverança");
                listaPalavrasInicial.add(0, "Perverso");
                listaPalavrasInicial.add(0, "Prazeres");
                listaPalavrasInicial.add(0, "Provisão");
                listaPalavrasInicial.add(0, "Abigail");
                listaPalavrasInicial.add(0, "Apolo");
                listaPalavrasInicial.add(0, "Apostasia");
                listaPalavrasInicial.add(0, "Ervas amargas");
                listaPalavrasInicial.add(0, "Galileia");
                listaPalavrasInicial.add(0, "Gamaliel");
                listaPalavrasInicial.add(0, "Issacar");
                listaPalavrasInicial.add(0, "Jubileu");
                listaPalavrasInicial.add(0, "Maravilhoso Conselheiro");
                listaPalavrasInicial.add(0, "Naftali");
                break;

            case 10:
                listaPalavrasInicial.add(0, "Abraão");
                listaPalavrasInicial.add(0, "Apóstolos");
                listaPalavrasInicial.add(0, "Bíblia");
                listaPalavrasInicial.add(0, "Colheita");
                listaPalavrasInicial.add(0, "Esaú");
                listaPalavrasInicial.add(0, "Escolhido");
                listaPalavrasInicial.add(0, "Escravos");
                listaPalavrasInicial.add(0, "Grávida");
                listaPalavrasInicial.add(0, "Gregos");
                listaPalavrasInicial.add(0, "Guerra");
                listaPalavrasInicial.add(0, "Jacó");
                listaPalavrasInicial.add(0, "Jardim do Éden");
                listaPalavrasInicial.add(0, "Marcos");
                listaPalavrasInicial.add(0, "Maria");
                listaPalavrasInicial.add(0, "Missão");
                listaPalavrasInicial.add(0, "Nascimento");
                listaPalavrasInicial.add(0, "Pescadores");
                listaPalavrasInicial.add(0, "Tempestade");
                listaPalavrasInicial.add(0, "Templo");
                listaPalavrasInicial.add(0, "Virgem");
                listaPalavrasInicial.add(0, "Alicerce");
                listaPalavrasInicial.add(0, "Alma");
                listaPalavrasInicial.add(0, "Cantares de Salomão");
                listaPalavrasInicial.add(0, "Cântico");
                listaPalavrasInicial.add(0, "Corvo");
                listaPalavrasInicial.add(0, "Cova");
                listaPalavrasInicial.add(0, "Coxos");
                listaPalavrasInicial.add(0, "Filhos de Deus");
                listaPalavrasInicial.add(0, "Filipe");
                listaPalavrasInicial.add(0, "Filipenses");
                listaPalavrasInicial.add(0, "Filisteus");
                listaPalavrasInicial.add(0, "Gêmeos");
                listaPalavrasInicial.add(0, "Impiedade");
                listaPalavrasInicial.add(0, "Ímpio");
                listaPalavrasInicial.add(0, "Impossível");
                listaPalavrasInicial.add(0, "Incenso");
                listaPalavrasInicial.add(0, "Incredulidade");
                listaPalavrasInicial.add(0, "Judá");
                listaPalavrasInicial.add(0, "Judas ");
                listaPalavrasInicial.add(0, "Palha");
                listaPalavrasInicial.add(0, "Balaão");
                listaPalavrasInicial.add(0, "Diná");
                listaPalavrasInicial.add(0, "Expiação");
                listaPalavrasInicial.add(0, "Lia");
                listaPalavrasInicial.add(0, "Obstinados");
                listaPalavrasInicial.add(0, "Predecessor");
                listaPalavrasInicial.add(0, "Predição");
                listaPalavrasInicial.add(0, "Restauração");
                listaPalavrasInicial.add(0, "Retidão");
                listaPalavrasInicial.add(0, "Santuário");
                break;

            case 11:
                listaPalavrasInicial.add(0, "Altar");
                listaPalavrasInicial.add(0, "Baleia");
                listaPalavrasInicial.add(0, "Capacete");
                listaPalavrasInicial.add(0, "Crente");
                listaPalavrasInicial.add(0, "Crer");
                listaPalavrasInicial.add(0, "Criação");
                listaPalavrasInicial.add(0, "Criança");
                listaPalavrasInicial.add(0, "Cristãos");
                listaPalavrasInicial.add(0, "Cristo");
                listaPalavrasInicial.add(0, "Crucificação");
                listaPalavrasInicial.add(0, "Cruz");
                listaPalavrasInicial.add(0, "Culpa");
                listaPalavrasInicial.add(0, "Firme");
                listaPalavrasInicial.add(0, "Flecha");
                listaPalavrasInicial.add(0, "Infiel");
                listaPalavrasInicial.add(0, "Inimigos");
                listaPalavrasInicial.add(0, "Judas Iscariotes");
                listaPalavrasInicial.add(0, "Pão");
                listaPalavrasInicial.add(0, "Pregação");
                listaPalavrasInicial.add(0, "Vinho");
                listaPalavrasInicial.add(0, "Abrigo");
                listaPalavrasInicial.add(0, "Arão");
                listaPalavrasInicial.add(0, "Bispo");
                listaPalavrasInicial.add(0, "Colossenses");
                listaPalavrasInicial.add(0, "Coluna de Fogo");
                listaPalavrasInicial.add(0, "Coluna de Nuvem");
                listaPalavrasInicial.add(0, "Discernimento");
                listaPalavrasInicial.add(0, "Disciplina");
                listaPalavrasInicial.add(0, "Escudeiro");
                listaPalavrasInicial.add(0, "Ezequiel");
                listaPalavrasInicial.add(0, "Guerreiro");
                listaPalavrasInicial.add(0, "Liberdade");
                listaPalavrasInicial.add(0, "Libertador");
                listaPalavrasInicial.add(0, "Liderança");
                listaPalavrasInicial.add(0, "Líderes");
                listaPalavrasInicial.add(0, "Maria Madalena");
                listaPalavrasInicial.add(0, "Marta");
                listaPalavrasInicial.add(0, "Moderado");
                listaPalavrasInicial.add(0, "Ofensa");
                listaPalavrasInicial.add(0, "Prudente");
                listaPalavrasInicial.add(0, "Genealogia");
                listaPalavrasInicial.add(0, "Jefté");
                listaPalavrasInicial.add(0, "Natã");
                listaPalavrasInicial.add(0, "Pilatos");
                listaPalavrasInicial.add(0, "Senhor dos Exércitos");
                listaPalavrasInicial.add(0, "Senhor dos Senhores");
                listaPalavrasInicial.add(0, "Silas");
                listaPalavrasInicial.add(0, "Simão");
                listaPalavrasInicial.add(0, "Simeão");
                listaPalavrasInicial.add(0, "Sinagoga");
                break;

            case 12:
                listaPalavrasInicial.add(0, "Arca");
                listaPalavrasInicial.add(0, "Discípulos");
                listaPalavrasInicial.add(0, "Dívida");
                listaPalavrasInicial.add(0, "Dízimo");
                listaPalavrasInicial.add(0, "Doença");
                listaPalavrasInicial.add(0, "Escudo");
                listaPalavrasInicial.add(0, "Fama");
                listaPalavrasInicial.add(0, "Família");
                listaPalavrasInicial.add(0, "Faraó");
                listaPalavrasInicial.add(0, "Mateus");
                listaPalavrasInicial.add(0, "Moisés");
                listaPalavrasInicial.add(0, "Oferta");
                listaPalavrasInicial.add(0, "Piolhos");
                listaPalavrasInicial.add(0, "Planos");
                listaPalavrasInicial.add(0, "Reto");
                listaPalavrasInicial.add(0, "Retorno");
                listaPalavrasInicial.add(0, "Tempo");
                listaPalavrasInicial.add(0, "Tenda");
                listaPalavrasInicial.add(0, "Terremoto");
                listaPalavrasInicial.add(0, "Vida");
                listaPalavrasInicial.add(0, "Banquete");
                listaPalavrasInicial.add(0, "Capitão da Guarda");
                listaPalavrasInicial.add(0, "Caráter");
                listaPalavrasInicial.add(0, "Culpado");
                listaPalavrasInicial.add(0, "Generosidade");
                listaPalavrasInicial.add(0, "Jejum");
                listaPalavrasInicial.add(0, "Jeremias");
                listaPalavrasInicial.add(0, "Naufrágio");
                listaPalavrasInicial.add(0, "Parábola");
                listaPalavrasInicial.add(0, "Paralítico");
                listaPalavrasInicial.add(0, "Presbíteros");
                listaPalavrasInicial.add(0, "Publicano");
                listaPalavrasInicial.add(0, "Redenção");
                listaPalavrasInicial.add(0, "Rute");
                listaPalavrasInicial.add(0, "Salvo");
                listaPalavrasInicial.add(0, "Sara");
                listaPalavrasInicial.add(0, "Sarça");
                listaPalavrasInicial.add(0, "Sensato");
                listaPalavrasInicial.add(0, "Sodoma");
                listaPalavrasInicial.add(0, "Submissão");
                listaPalavrasInicial.add(0, "Acabe");
                listaPalavrasInicial.add(0, "Amabilidade");
                listaPalavrasInicial.add(0, "Blasfêmia");
                listaPalavrasInicial.add(0, "Coluna Sagrada");
                listaPalavrasInicial.add(0, "Fogo Consumidor");
                listaPalavrasInicial.add(0, "Habacuque");
                listaPalavrasInicial.add(0, "Iniquidade");
                listaPalavrasInicial.add(0, "Judeia");
                listaPalavrasInicial.add(0, "Linha de Batalha");
                listaPalavrasInicial.add(0, "Livramento");
                break;

            case 13:
                listaPalavrasInicial.add(0, "Acampamento");
                listaPalavrasInicial.add(0, "Amar");
                listaPalavrasInicial.add(0, "Barco");
                listaPalavrasInicial.add(0, "Carcereiro");
                listaPalavrasInicial.add(0, "Culto");
                listaPalavrasInicial.add(0, "Cura");
                listaPalavrasInicial.add(0, "Gênesis");
                listaPalavrasInicial.add(0, "Inocente");
                listaPalavrasInicial.add(0, "Páscoa");
                listaPalavrasInicial.add(0, "Passos");
                listaPalavrasInicial.add(0, "Pastor");
                listaPalavrasInicial.add(0, "Pastor de ovelhas");
                listaPalavrasInicial.add(0, "Presença");
                listaPalavrasInicial.add(0, "Presente");
                listaPalavrasInicial.add(0, "Presos");
                listaPalavrasInicial.add(0, "Primeiro");
                listaPalavrasInicial.add(0, "Pureza");
                listaPalavrasInicial.add(0, "Redentor");
                listaPalavrasInicial.add(0, "Sábado");
                listaPalavrasInicial.add(0, "VERDADEIRO");
                listaPalavrasInicial.add(0, "Arca da aliança");
                listaPalavrasInicial.add(0, "Boas Novas");
                listaPalavrasInicial.add(0, "Comandante");
                listaPalavrasInicial.add(0, "Domínio Próprio");
                listaPalavrasInicial.add(0, "Esdras");
                listaPalavrasInicial.add(0, "Fardo");
                listaPalavrasInicial.add(0, "Fariseus");
                listaPalavrasInicial.add(0, "Fôlego");
                listaPalavrasInicial.add(0, "Habitação");
                listaPalavrasInicial.add(0, "Judeus");
                listaPalavrasInicial.add(0, "Jugo");
                listaPalavrasInicial.add(0, "Ló");
                listaPalavrasInicial.add(0, "Oleiro");
                listaPalavrasInicial.add(0, "Plantações");
                listaPalavrasInicial.add(0, "Sustento");
                listaPalavrasInicial.add(0, "Tabernáculo");
                listaPalavrasInicial.add(0, "Timóteo");
                listaPalavrasInicial.add(0, "Ungir");
                listaPalavrasInicial.add(0, "Único");
                listaPalavrasInicial.add(0, "Unidade");
                listaPalavrasInicial.add(0, "Jericó");
                listaPalavrasInicial.add(0, "Matusalém");
                listaPalavrasInicial.add(0, "Monte das Oliveiras");
                listaPalavrasInicial.add(0, "Monte Sinai");
                listaPalavrasInicial.add(0, "Moriá");
                listaPalavrasInicial.add(0, "Naum");
                listaPalavrasInicial.add(0, "Nazaré");
                listaPalavrasInicial.add(0, "Nazireu");
                listaPalavrasInicial.add(0, "Retribuir");
                listaPalavrasInicial.add(0, "Samaria");
                break;

            case 14:
                listaPalavrasInicial.add(0, "Arco");
                listaPalavrasInicial.add(0, "Arco-íris");
                listaPalavrasInicial.add(0, "Armadura");
                listaPalavrasInicial.add(0, "Armas");
                listaPalavrasInicial.add(0, "Compaixão");
                listaPalavrasInicial.add(0, "Comunhão");
                listaPalavrasInicial.add(0, "Comunidade");
                listaPalavrasInicial.add(0, "Dons");
                listaPalavrasInicial.add(0, "Espada");
                listaPalavrasInicial.add(0, "Esperança");
                listaPalavrasInicial.add(0, "Esperar");
                listaPalavrasInicial.add(0, "Fé");
                listaPalavrasInicial.add(0, "Fome");
                listaPalavrasInicial.add(0, "Juiz");
                listaPalavrasInicial.add(0, "Juízes");
                listaPalavrasInicial.add(0, "Louvor");
                listaPalavrasInicial.add(0, "Lucas");
                listaPalavrasInicial.add(0, "Morte");
                listaPalavrasInicial.add(0, "Óleo");
                listaPalavrasInicial.add(0, "Verdade");
                listaPalavrasInicial.add(0, "Ação de graças");
                listaPalavrasInicial.add(0, "Amém");
                listaPalavrasInicial.add(0, "Insensato");
                listaPalavrasInicial.add(0, "Instruções");
                listaPalavrasInicial.add(0, "Jerusalém");
                listaPalavrasInicial.add(0, "Mediador");
                listaPalavrasInicial.add(0, "Meditar");
                listaPalavrasInicial.add(0, "Necessitado");
                listaPalavrasInicial.add(0, "Pátio");
                listaPalavrasInicial.add(0, "Primogênito");
                listaPalavrasInicial.add(0, "Purificação");
                listaPalavrasInicial.add(0, "Refinar");
                listaPalavrasInicial.add(0, "Refúgio");
                listaPalavrasInicial.add(0, "Samaritana");
                listaPalavrasInicial.add(0, "Véu");
                listaPalavrasInicial.add(0, "Videira");
                listaPalavrasInicial.add(0, "Vigília");
                listaPalavrasInicial.add(0, "Vigor");
                listaPalavrasInicial.add(0, "Vingança");
                listaPalavrasInicial.add(0, "Zaqueu");
                listaPalavrasInicial.add(0, "Barnabé");
                listaPalavrasInicial.add(0, "Boaz");
                listaPalavrasInicial.add(0, "Carmelo");
                listaPalavrasInicial.add(0, "Cutelo");
                listaPalavrasInicial.add(0, "Dã");
                listaPalavrasInicial.add(0, "Getsêmani");
                listaPalavrasInicial.add(0, "Hagar");
                listaPalavrasInicial.add(0, "Plenitude");
                listaPalavrasInicial.add(0, "Sarepta");
                listaPalavrasInicial.add(0, "Sentinela");
                break;

            case 15:
                listaPalavrasInicial.add(0, "Amigos");
                listaPalavrasInicial.add(0, "Amor");
                listaPalavrasInicial.add(0, "Harpa");
                listaPalavrasInicial.add(0, "Hebreus");
                listaPalavrasInicial.add(0, "Herança");
                listaPalavrasInicial.add(0, "Instrumentos");
                listaPalavrasInicial.add(0, "Medo");
                listaPalavrasInicial.add(0, "Paulo");
                listaPalavrasInicial.add(0, "Paz");
                listaPalavrasInicial.add(0, "Pecado");
                listaPalavrasInicial.add(0, "Poço");
                listaPalavrasInicial.add(0, "Poder");
                listaPalavrasInicial.add(0, "Príncipe");
                listaPalavrasInicial.add(0, "Puro");
                listaPalavrasInicial.add(0, "Rio");
                listaPalavrasInicial.add(0, "Sabedoria");
                listaPalavrasInicial.add(0, "Sábio");
                listaPalavrasInicial.add(0, "Sacerdote");
                listaPalavrasInicial.add(0, "Samuel");
                listaPalavrasInicial.add(0, "Vencedores");
                listaPalavrasInicial.add(0, "Arrependimento");
                listaPalavrasInicial.add(0, "Barrabás");
                listaPalavrasInicial.add(0, "Bonança");
                listaPalavrasInicial.add(0, "Carros de Guerra");
                listaPalavrasInicial.add(0, "Concubina");
                listaPalavrasInicial.add(0, "Dádiva");
                listaPalavrasInicial.add(0, "Dalila");
                listaPalavrasInicial.add(0, "Damasco");
                listaPalavrasInicial.add(0, "Doutrina");
                listaPalavrasInicial.add(0, "Espias");
                listaPalavrasInicial.add(0, "Feitiçaria");
                listaPalavrasInicial.add(0, "Forca");
                listaPalavrasInicial.add(0, "Gideão");
                listaPalavrasInicial.add(0, "Julgamento");
                listaPalavrasInicial.add(0, "Jumenta");
                listaPalavrasInicial.add(0, "Moscas");
                listaPalavrasInicial.add(0, "Saul");
                listaPalavrasInicial.add(0, "Sepulcro");
                listaPalavrasInicial.add(0, "Botija");
                listaPalavrasInicial.add(0, "Cativos");
                listaPalavrasInicial.add(0, "Acepção");
                listaPalavrasInicial.add(0, "Jessé");
                listaPalavrasInicial.add(0, "Lugar Santo");
                listaPalavrasInicial.add(0, "Neemias");
                listaPalavrasInicial.add(0, "Óleo de alegria");
                listaPalavrasInicial.add(0, "Regozijo");
                listaPalavrasInicial.add(0, "Sinal miraculoso");
                listaPalavrasInicial.add(0, "Sofonias");
                listaPalavrasInicial.add(0, "Tábuas da Aliança");
                listaPalavrasInicial.add(0, "Tadeu");
                break;

            case 16:
                listaPalavrasInicial.add(0, "Barro");
                listaPalavrasInicial.add(0, "Bondoso");
                listaPalavrasInicial.add(0, "Carta");
                listaPalavrasInicial.add(0, "Casamento");
                listaPalavrasInicial.add(0, "Castigo");
                listaPalavrasInicial.add(0, "Condenação");
                listaPalavrasInicial.add(0, "Danças");
                listaPalavrasInicial.add(0, "Daniel");
                listaPalavrasInicial.add(0, "Davi");
                listaPalavrasInicial.add(0, "Espírito");
                listaPalavrasInicial.add(0, "Espírito Santo");
                listaPalavrasInicial.add(0, "Forças");
                listaPalavrasInicial.add(0, "Gigantes");
                listaPalavrasInicial.add(0, "Jesus");
                listaPalavrasInicial.add(0, "Lutar");
                listaPalavrasInicial.add(0, "Luz");
                listaPalavrasInicial.add(0, "Mudo");
                listaPalavrasInicial.add(0, "Mulher");
                listaPalavrasInicial.add(0, "Olhos");
                listaPalavrasInicial.add(0, "Vaso");
                listaPalavrasInicial.add(0, "Açoites");
                listaPalavrasInicial.add(0, "Aconselhar");
                listaPalavrasInicial.add(0, "Herdeiro");
                listaPalavrasInicial.add(0, "Integridade");
                listaPalavrasInicial.add(0, "Íntegro");
                listaPalavrasInicial.add(0, "Intercessão");
                listaPalavrasInicial.add(0, "Nicodemos");
                listaPalavrasInicial.add(0, "Pecador");
                listaPalavrasInicial.add(0, "Pedido");
                listaPalavrasInicial.add(0, "Poderoso");
                listaPalavrasInicial.add(0, "Príncipe da Paz");
                listaPalavrasInicial.add(0, "Quebrantado");
                listaPalavrasInicial.add(0, "Queda");
                listaPalavrasInicial.add(0, "Querubim");
                listaPalavrasInicial.add(0, "Sacrifício");
                listaPalavrasInicial.add(0, "Conduta");
                listaPalavrasInicial.add(0, "Confessar");
                listaPalavrasInicial.add(0, "Débora");
                listaPalavrasInicial.add(0, "Decretos");
                listaPalavrasInicial.add(0, "Dedicado");
                listaPalavrasInicial.add(0, "Amós");
                listaPalavrasInicial.add(0, "Árvore da Vida");
                listaPalavrasInicial.add(0, "Dracma");
                listaPalavrasInicial.add(0, "Feixes");
                listaPalavrasInicial.add(0, "Juramento");
                listaPalavrasInicial.add(0, "Mefibosete");
                listaPalavrasInicial.add(0, "Melquisedeque");
                listaPalavrasInicial.add(0, "Rio Eufrates");
                listaPalavrasInicial.add(0, "Sepultura");
                listaPalavrasInicial.add(0, "Tenda da Congregação");
                break;
        }
    }

    public void adicionaPacoteInicial() {
        listaPalavrasInicial.add(0, "Acordo");
        listaPalavrasInicial.add(0, "Adão");
        listaPalavrasInicial.add(0, "Ana");
        listaPalavrasInicial.add(0, "Asas");
        listaPalavrasInicial.add(0, "Feliz");
        listaPalavrasInicial.add(0, "Pomba");
        listaPalavrasInicial.add(0, "Princípio");
        listaPalavrasInicial.add(0, "Prisão");
        listaPalavrasInicial.add(0, "Prisioneiros");
        listaPalavrasInicial.add(0, "Rei");
        listaPalavrasInicial.add(0, "Sandálias");
        listaPalavrasInicial.add(0, "Sangue");
        listaPalavrasInicial.add(0, "Sansão");
        listaPalavrasInicial.add(0, "Saulo");
        listaPalavrasInicial.add(0, "Serpente");
        listaPalavrasInicial.add(0, "Sincero");
        listaPalavrasInicial.add(0, "Sofrimento");
        listaPalavrasInicial.add(0, "Sol");
        listaPalavrasInicial.add(0, "Soldado");
        listaPalavrasInicial.add(0, "Suicídio");
        listaPalavrasInicial.add(0, "Talentos");
        listaPalavrasInicial.add(0, "Terror");
        listaPalavrasInicial.add(0, "Tesouro");
        listaPalavrasInicial.add(0, "Trabalho");
        listaPalavrasInicial.add(0, "Traição");
        listaPalavrasInicial.add(0, "Transformação");
        listaPalavrasInicial.add(0, "Travessia");
        listaPalavrasInicial.add(0, "Trevas");
        listaPalavrasInicial.add(0, "Tribos");
        listaPalavrasInicial.add(0, "Triste");
        listaPalavrasInicial.add(0, "Trombeta");
        listaPalavrasInicial.add(0, "Eclesiastes");
        listaPalavrasInicial.add(0, "Efésios");
        listaPalavrasInicial.add(0, "Estátua de sal");
        listaPalavrasInicial.add(0, "Ester");
        listaPalavrasInicial.add(0, "Estéril");
        listaPalavrasInicial.add(0, "Estevão");
        listaPalavrasInicial.add(0, "Estrangeiros");
        listaPalavrasInicial.add(0, "Fortaleza");
        listaPalavrasInicial.add(0, "Justiça");
        listaPalavrasInicial.add(0, "Justificado");
        listaPalavrasInicial.add(0, "Justo");
        listaPalavrasInicial.add(0, "Machado");
        listaPalavrasInicial.add(0, "Membro");
        listaPalavrasInicial.add(0, "Memorial");
        listaPalavrasInicial.add(0, "Mensageiro");
        listaPalavrasInicial.add(0, "Oliveira");
        listaPalavrasInicial.add(0, "Ômega");
        listaPalavrasInicial.add(0, "Oprimido");
        listaPalavrasInicial.add(0, "Rio Jordão");
        listaPalavrasInicial.add(0, "Rio Nilo");
        listaPalavrasInicial.add(0, "Bartolomeu");
        listaPalavrasInicial.add(0, "Gileade");
        listaPalavrasInicial.add(0, "Herodes");
        listaPalavrasInicial.add(0, "Hissopo");
        listaPalavrasInicial.add(0, "Interpretação de sonhos");
        listaPalavrasInicial.add(0, "Jetro");
        listaPalavrasInicial.add(0, "Jezabel");
        listaPalavrasInicial.add(0, "Mulher Virtuosa");
        listaPalavrasInicial.add(0, "Nínive");
        listaPalavrasInicial.add(0, "Pedra Angular");
        listaPalavrasInicial.add(0, "Raabe");
        listaPalavrasInicial.add(0, "Saduceus");
        listaPalavrasInicial.add(0, "Safira");
        listaPalavrasInicial.add(0, "Tenda do Encontro");
        listaPalavrasInicial.add(0, "Tito");
        listaPalavrasInicial.add(0, "Todo-Poderoso");
        listaPalavrasInicial.add(0, "Ur dos caldeus");
        listaPalavrasInicial.add(0, "Urim e Tumim");
        listaPalavrasInicial.add(0, "Zípora");

    }


    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void releasePlayerContagens() {
        if (mediaPlayerContagens != null) {
            mediaPlayerContagens.stop();
            mediaPlayerContagens.release();
            mediaPlayerContagens = null;
        }
    }
}

