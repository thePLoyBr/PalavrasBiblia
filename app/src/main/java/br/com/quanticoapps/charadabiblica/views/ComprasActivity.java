package br.com.quanticoapps.charadabiblica.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import br.com.quanticoapps.charadabiblica.R;

import static br.com.quanticoapps.charadabiblica.views.MainActivity.ARQUIVO_PREFERENCIA;
import static br.com.quanticoapps.charadabiblica.views.MainActivity.database;
import static br.com.quanticoapps.charadabiblica.views.MainActivity.myRef;
import static br.com.quanticoapps.charadabiblica.views.SplashActivity.userConvertido;

public class ComprasActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    private BillingProcessor bp;
    private boolean comprado = false;
    private boolean moedaAdicionada;
    private TextView tvMoedasCompras;

    private TextView palavrasInstaladas;
    private int palavrasInst = 70;
    private Button btnPacotePremium;
    private Button btnPacoteA;
    private Button btnPacoteB;
    private Button btnPacoteC;
    private Button btnPacoteD;
    private Button btnPacoteE;
    private Button btnPacoteF;
    private Button btnPacoteG;
    private Button btnPacoteH;
    private Button btnPacoteI;
    private Button btnPacoteJ;
    private Button btnPacoteK;
    private Button btnPacoteL;
    private Button btnPacoteM;
    private Button btnPacoteN;
    private Button btnPacoteO;
    private Button btnPacoteP;

    private int increment = 0;
    private int moedasDoBanco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_compras);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        btnPacotePremium = findViewById(R.id.btn_pacotePremium);
        btnPacoteA = findViewById(R.id.btn_pacoteA);
        btnPacoteB = findViewById(R.id.btn_pacoteB);
        btnPacoteC = findViewById(R.id.btn_pacoteC);
        btnPacoteD = findViewById(R.id.btn_pacoteD);
        btnPacoteE = findViewById(R.id.btn_pacoteE);
        btnPacoteF = findViewById(R.id.btn_pacoteF);
        btnPacoteG = findViewById(R.id.btn_pacoteG);
        btnPacoteH = findViewById(R.id.btn_pacoteH);
        btnPacoteI = findViewById(R.id.btn_pacoteI);
        btnPacoteJ = findViewById(R.id.btn_pacoteJ);
        btnPacoteK = findViewById(R.id.btn_pacoteK);
        btnPacoteL = findViewById(R.id.btn_pacoteL);
        btnPacoteM = findViewById(R.id.btn_pacoteM);
        btnPacoteN = findViewById(R.id.btn_pacoteN);
        btnPacoteO = findViewById(R.id.btn_pacoteO);
        btnPacoteP = findViewById(R.id.btn_pacoteP);
        palavrasInstaladas = findViewById(R.id.tv_palavrasInstaladas);
        tvMoedasCompras = findViewById(R.id.tv_moedas_compras);


        monitorDBCompras(userConvertido);


        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            verificaPacotesOnline();
        } else {
            verificaPacotesOffline();
        }

        bp = new BillingProcessor(this, null, this);


        btnPacotePremium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bp.purchase(ComprasActivity.this, "1pacote_premium_pb.");
            }
        });

        btnPacoteA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moedasDoBanco >= 100) {
                    comprado = false;
                    compraPacotes(userConvertido, "1");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (comprado) {
                                btnPacoteA.setBackgroundColor(Color.parseColor("#ffffff"));
                                palavrasInst += 50;
                                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                            }
                        }
                    }, 1000);
                } else {
                    alertLoja();
                }
            }
        });

        btnPacoteB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moedasDoBanco >= 100) {
                    comprado = false;
                    compraPacotes(userConvertido, "2");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (comprado) {
                                btnPacoteB.setBackgroundColor(Color.parseColor("#ffffff"));
                                palavrasInst += 50;
                                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                            }
                        }
                    }, 1000);
                } else {
                    alertLoja();
                }
            }
        });

        btnPacoteC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moedasDoBanco >= 100) {
                    comprado = false;
                    compraPacotes(userConvertido, "3");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (comprado) {
                                btnPacoteC.setBackgroundColor(Color.parseColor("#ffffff"));
                                palavrasInst += 50;
                                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                            }
                        }
                    }, 1000);
                } else {
                    alertLoja();
                }
            }
        });

        btnPacoteD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moedasDoBanco >= 100) {
                    comprado = false;
                    compraPacotes(userConvertido, "4");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (comprado) {
                                btnPacoteD.setBackgroundColor(Color.parseColor("#ffffff"));
                                palavrasInst += 50;
                                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                            }
                        }
                    }, 1000);
                } else {
                    alertLoja();
                }
            }
        });

        btnPacoteE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moedasDoBanco >= 100) {
                    comprado = false;
                    compraPacotes(userConvertido, "5");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (comprado) {
                                btnPacoteE.setBackgroundColor(Color.parseColor("#ffffff"));
                                palavrasInst += 50;
                                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                            }
                        }
                    }, 1000);
                } else {
                    alertLoja();
                }
            }
        });

        btnPacoteF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moedasDoBanco >= 100) {
                    comprado = false;
                    compraPacotes(userConvertido, "6");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (comprado) {
                                btnPacoteF.setBackgroundColor(Color.parseColor("#ffffff"));
                                palavrasInst += 50;
                                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                            }
                        }
                    }, 1000);
                } else {
                    alertLoja();
                }
            }
        });

        btnPacoteG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moedasDoBanco >= 100) {
                    comprado = false;
                    compraPacotes(userConvertido, "7");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (comprado) {
                                btnPacoteG.setBackgroundColor(Color.parseColor("#ffffff"));
                                palavrasInst += 50;
                                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                            }
                        }
                    }, 1000);
                } else {
                    alertLoja();
                }
            }
        });

        btnPacoteH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moedasDoBanco >= 100) {
                    comprado = false;
                    compraPacotes(userConvertido, "8");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (comprado) {
                                btnPacoteH.setBackgroundColor(Color.parseColor("#ffffff"));
                                palavrasInst += 50;
                                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                            }
                        }
                    }, 1000);
                } else {
                    alertLoja();
                }
            }
        });

        btnPacoteI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moedasDoBanco >= 100) {
                    comprado = false;
                    compraPacotes(userConvertido, "9");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (comprado) {
                                btnPacoteI.setBackgroundColor(Color.parseColor("#ffffff"));
                                palavrasInst += 50;
                                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                            }
                        }
                    }, 1000);
                } else {
                    alertLoja();
                }
            }
        });

        btnPacoteJ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moedasDoBanco >= 100) {
                    comprado = false;
                    compraPacotes(userConvertido, "10");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (comprado) {
                                btnPacoteJ.setBackgroundColor(Color.parseColor("#ffffff"));
                                palavrasInst += 50;
                                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                            }
                        }
                    }, 1000);
                } else {
                    alertLoja();
                }
            }
        });

        btnPacoteK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moedasDoBanco >= 100) {
                    comprado = false;
                    compraPacotes(userConvertido, "11");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (comprado) {
                                btnPacoteK.setBackgroundColor(Color.parseColor("#ffffff"));
                                palavrasInst += 50;
                                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                            }
                        }
                    }, 1000);
                } else {
                    alertLoja();
                }
            }
        });

        btnPacoteL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moedasDoBanco >= 100) {
                    comprado = false;
                    compraPacotes(userConvertido, "12");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (comprado) {
                                btnPacoteL.setBackgroundColor(Color.parseColor("#ffffff"));
                                palavrasInst += 50;
                                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                            }
                        }
                    }, 1000);
                } else {
                    alertLoja();
                }
            }
        });

        btnPacoteM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moedasDoBanco >= 100) {
                    comprado = false;
                    compraPacotes(userConvertido, "13");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (comprado) {
                                btnPacoteM.setBackgroundColor(Color.parseColor("#ffffff"));
                                palavrasInst += 50;
                                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                            }
                        }
                    }, 1000);
                } else {
                    alertLoja();
                }
            }

        });

        btnPacoteN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moedasDoBanco >= 100) {
                    comprado = false;
                    compraPacotes(userConvertido, "14");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (comprado) {
                                btnPacoteN.setBackgroundColor(Color.parseColor("#ffffff"));
                                palavrasInst += 50;
                                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                            }
                        }
                    }, 1000);
                } else {
                    alertLoja();
                }
            }
        });

        btnPacoteO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moedasDoBanco >= 100) {
                    comprado = false;
                    compraPacotes(userConvertido, "15");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (comprado) {
                                btnPacoteO.setBackgroundColor(Color.parseColor("#ffffff"));
                                palavrasInst += 50;
                                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                            }
                        }
                    }, 1000);
                } else {
                    alertLoja();
                }
            }
        });

        btnPacoteP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moedasDoBanco >= 100) {
                    comprado = false;
                    compraPacotes(userConvertido, "16");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (comprado) {
                                btnPacoteP.setBackgroundColor(Color.parseColor("#ffffff"));
                                palavrasInst += 50;
                                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                            }
                        }
                    }, 1000);
                } else {
                    alertLoja();
                }
            }
        });
    }

    private void compraPacotes(String path, final String numPacote) {
        if (path != "") {
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int totalMoedas = Integer.parseInt(dataSnapshot.child(userConvertido).child("coins").getValue().toString());
                    if (totalMoedas >= 100 && !comprado) {
                        comprado = true;
                        btnPacoteA.setEnabled(false);
                        SharedPreferences sharedPreferences = getSharedPreferences(ARQUIVO_PREFERENCIA, 0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        totalMoedas -= 100;
                        myRef.child(userConvertido).child("coins").setValue(totalMoedas);
                        myRef.child(userConvertido).child("pacotes").child(numPacote).child("compras").setValue("comprado");
                        editor.putString(numPacote, "comprado");
                        editor.commit();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    private void compraPacotePremium(String path) {

        if (path != "") {
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.ARQUIVO_PREFERENCIA, 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    btnPacotePremium.setEnabled(false);

                    for (int i = 1; i <= 17; i++) {
                        myRef.child(userConvertido).child("pacotes").child(String.valueOf(i)).child("compras").setValue("comprado");
                        editor.putString(String.valueOf(i), "comprado");
                        editor.commit();
                    }

                    editor.putString("ads", "noAds");
                    editor.commit();
                    MainActivity.anuncios = "noAds";
                    Intent intent = new Intent(ComprasActivity.this, MainActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

    }

    private void cemMoedas() {
        bp.initialize();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!moedaAdicionada) {
                    moedaAdicionada = true;
                    int coinsOld = Integer.parseInt(dataSnapshot.child(userConvertido).child("coins").getValue().toString());
                    coinsOld += 100;
                    myRef.child(userConvertido).child("coins").setValue(coinsOld);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }

    private void trezentasMoedas() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!moedaAdicionada) {
                    moedaAdicionada = true;
                    int coinsOld = Integer.parseInt(dataSnapshot.child(userConvertido).child("coins").getValue().toString());
                    coinsOld += 300;
                    myRef.child(userConvertido).child("coins").setValue(coinsOld);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void seiscentasMoedas() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!moedaAdicionada) {
                    moedaAdicionada = true;
                    int coinsOld = Integer.parseInt(dataSnapshot.child(userConvertido).child("coins").getValue().toString());
                    coinsOld += 600;
                    myRef.child(userConvertido).child("coins").setValue(coinsOld);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private void alertLoja() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ComprasActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_money, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();

        ImageView btn100Moedas = view.findViewById(R.id.btn_100_moedas);
        btn100Moedas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moedaAdicionada = false;
                bp.purchase(ComprasActivity.this,"100moedas_pb.");
            }
        });

        ImageView btn300Moedas = view.findViewById(R.id.btn_300_moedas);
        btn300Moedas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moedaAdicionada = false;
                bp.purchase(ComprasActivity.this, "300moedas_pb.");
            }
        });

        ImageView btn600Moedas = view.findViewById(R.id.btn_600_moedas);
        btn600Moedas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moedaAdicionada = false;
                bp.purchase(ComprasActivity.this,"600moedas_pb.");
            }
        });

        ImageView btnPacotePremiumLoja = view.findViewById(R.id.btn_pacotePremiumLoja);
        btnPacotePremiumLoja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bp.purchase(ComprasActivity.this, "1pacote_premium_pb.");
            }
        });

    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {

        if (productId.equals("100moedas_pb.")) {
            bp.consumePurchase("100moedas_pb.");
            cemMoedas();
            Toast.makeText(getApplicationContext(), "Você comprou 100 Moedas", Toast.LENGTH_LONG).show();
        }

        if (productId.equals("300moedas_pb.")) {
            bp.consumePurchase("300moedas_pb.");
            trezentasMoedas();
            Toast.makeText(getApplicationContext(), "Você comprou 300 Moedas", Toast.LENGTH_LONG).show();
        }

        if (productId.equals("600moedas_pb.")) {
            bp.consumePurchase("600moedas_pb.");
            seiscentasMoedas();
            Toast.makeText(getApplicationContext(), "Você comprou 600 Moedas", Toast.LENGTH_LONG).show();
        }

        if (productId.equals("1pacote_premium_pb.")) {
            bp.consumePurchase("1pacote_premium_pb.");
            compraPacotePremium(userConvertido);
            btnPacotePremium.setBackgroundColor(Color.parseColor("#ffffff"));
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {
    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        Toast.makeText(getApplicationContext(), "Erro na compra, tente novamente", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBillingInitialized() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if (bp != null) {
            bp.release();
        }
        super.onStop();
    }

    private void pintaPacotesComprados(char letra) {

        switch (letra) {
            case 'a':
                btnPacoteA.setBackgroundColor(Color.parseColor("#ffffff"));
                palavrasInst += 50;
                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                btnPacoteA.setEnabled(false);
                break;
            case 'b':
                btnPacoteB.setBackgroundColor(Color.parseColor("#ffffff"));
                palavrasInst += 50;
                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                btnPacoteB.setEnabled(false);
                break;
            case 'c':
                btnPacoteC.setBackgroundColor(Color.parseColor("#ffffff"));
                palavrasInst += 50;
                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                btnPacoteC.setEnabled(false);
                break;
            case 'd':
                btnPacoteD.setBackgroundColor(Color.parseColor("#ffffff"));
                palavrasInst += 50;
                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                btnPacoteD.setEnabled(false);
                break;
            case 'e':
                btnPacoteE.setBackgroundColor(Color.parseColor("#ffffff"));
                palavrasInst += 50;
                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                btnPacoteE.setEnabled(false);
                break;
            case 'f':
                btnPacoteF.setBackgroundColor(Color.parseColor("#ffffff"));
                palavrasInst += 50;
                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                btnPacoteF.setEnabled(false);
                break;
            case 'g':
                btnPacoteG.setBackgroundColor(Color.parseColor("#ffffff"));
                palavrasInst += 50;
                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                btnPacoteG.setEnabled(false);
                break;
            case 'h':
                btnPacoteH.setBackgroundColor(Color.parseColor("#ffffff"));
                palavrasInst += 50;
                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                btnPacoteH.setEnabled(false);
                break;
            case 'i':
                btnPacoteI.setBackgroundColor(Color.parseColor("#ffffff"));
                palavrasInst += 50;
                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                btnPacoteI.setEnabled(false);
                break;
            case 'j':
                btnPacoteJ.setBackgroundColor(Color.parseColor("#ffffff"));
                palavrasInst += 50;
                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                btnPacoteJ.setEnabled(false);
                break;
            case 'k':
                btnPacoteK.setBackgroundColor(Color.parseColor("#ffffff"));
                palavrasInst += 50;
                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                btnPacoteK.setEnabled(false);
                break;
            case 'l':
                btnPacoteL.setBackgroundColor(Color.parseColor("#ffffff"));
                palavrasInst += 50;
                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                btnPacoteL.setEnabled(false);
                break;
            case 'm':
                btnPacoteM.setBackgroundColor(Color.parseColor("#ffffff"));
                palavrasInst += 50;
                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                btnPacoteM.setEnabled(false);
                break;
            case 'n':
                btnPacoteN.setBackgroundColor(Color.parseColor("#ffffff"));
                palavrasInst += 50;
                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                btnPacoteN.setEnabled(false);
                break;
            case 'o':
                btnPacoteO.setBackgroundColor(Color.parseColor("#ffffff"));
                palavrasInst += 50;
                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                btnPacoteO.setEnabled(false);
                break;
            case 'p':
                btnPacoteP.setBackgroundColor(Color.parseColor("#ffffff"));
                palavrasInst += 50;
                palavrasInstaladas.setText(String.valueOf(palavrasInst));
                btnPacoteP.setEnabled(false);
                break;
        }
    }

    public void verificaPacotesOffline() {
        SharedPreferences sharedPreferences = getSharedPreferences(ARQUIVO_PREFERENCIA, 0);
        for (char letra = 'a'; letra <= 'q'; letra++) {
            increment++;
            if (sharedPreferences.contains(String.valueOf(increment))) {
                pintaPacotesComprados(letra);
            }
        }

    }

    public void verificaPacotesOnline() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (char letra = 'a'; letra <= 'p'; letra++) {
                    increment++;
                    String pacotes = dataSnapshot.child(userConvertido).child("pacotes").child(String.valueOf(increment)).child("compras").getValue().toString();
                    if (pacotes.equals("comprado")) {
                        pintaPacotesComprados(letra);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void monitorDBCompras(String path) {
        if (path != "") {
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    tvMoedasCompras.setText(dataSnapshot.child(userConvertido).child("coins").getValue().toString());
                    moedasDoBanco = Integer.parseInt(dataSnapshot.child(userConvertido).child("coins").getValue().toString());

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

}

