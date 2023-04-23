package ga.denis.outplay;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class InviteActivity extends AppCompatActivity implements View.OnClickListener {
    BufferedReader bufferedReader;
    Bitmap qr;
    ImageView qrCode;
    OutputStream output;
    Button inviteContinue;
    RelativeLayout[] players = new RelativeLayout[3];
    byte playerAmount = 0;
    TextView[] playerNames = new TextView[3];
    TextView myName;
    Button[] team = new Button[3];
    Button myTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        qrCode = (ImageView) findViewById(R.id.qrCode);
        inviteContinue = (Button) findViewById(R.id.inviteContinue);
        inviteContinue.setOnClickListener(this);

        //players[0] = findViewById(R.id.player1);
        players[0] = findViewById(R.id.player2);
        players[1] = findViewById(R.id.player3);
        players[2] = findViewById(R.id.player4);

        playerNames[0] = findViewById(R.id.player2name);
        playerNames[1] = findViewById(R.id.player3name);
        playerNames[2] = findViewById(R.id.player4name);

        myName = findViewById(R.id.player1name);
        myName.setText(SocketHandler.getName());

        myTeam = findViewById(R.id.player1button);

        team[0] = findViewById(R.id.player2button);
        team[1] = findViewById(R.id.player3button);
        team[2] = findViewById(R.id.player4button);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("attempting connection");
                    SocketHandler.setSocket(new Socket("142.132.174.213", 10000));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    output = SocketHandler.getSocket().getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(SocketHandler.getSocket().getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    output.write("1".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                QRGEncoder qrgEncoder = null;
                try {
                    qrgEncoder = new QRGEncoder(bufferedReader.readLine(),
                            null, QRGContents.Type.TEXT, 800);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                qrgEncoder.setColorBlack(Color.parseColor("#FFBA85FB"));
                qrgEncoder.setColorWhite(Color.parseColor("#FF121212"));
                qr = Bitmap.createBitmap(qrgEncoder.getBitmap());

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        qrCode.setImageBitmap(qr);
                    }
                });

                System.out.println("starting for");

                for (;;) {
                    String message = "";
                    try {
                        message = bufferedReader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String[] divided = message.split("_");
                    if (divided[0].equals("playersreload")) {
                        System.out.println("reloading players");
                        players[0].setVisibility(View.INVISIBLE);
                        players[1].setVisibility(View.INVISIBLE);
                        players[2].setVisibility(View.INVISIBLE);
                    } else if (divided[0].equals("addplayer")) {
                        if (playerAmount < 3) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    players[playerAmount].setVisibility(View.VISIBLE);
                                    playerNames[playerAmount].setText(divided[1]);
                                    playerAmount++;
                                }
                            });

                        } else {
                            System.out.println("Players full");
                        }
                    }
                }
            }
        });

        thread.start();

//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    output = SocketHandler.getSocket().getOutputStream();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                try {
//                    bufferedReader = new BufferedReader(new InputStreamReader(SocketHandler.getSocket().getInputStream()));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                for (;;) {
//                    String message = "";
//                    try {
//                        message = bufferedReader.readLine();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    String[] divided = message.split("_");
//                    if (divided[0].equals("playersreload")) {
//                        players[0].setVisibility(View.INVISIBLE);
//                        players[1].setVisibility(View.INVISIBLE);
//                        players[2].setVisibility(View.INVISIBLE);
//                    } else if (divided[0].equals("addplayer")) {
//                        if (playerAmount < 3) {
//                            players[playerAmount].setVisibility(View.VISIBLE);
//                            playerNames[playerAmount].setText(divided[1]);
//                            playerAmount++;
//                        } else {
//                            System.out.println("Players full");
//                        }
//                    }
//                }
//            }
//        });
//
//        t.start();
    }

    @Override
    public void onClick(View v) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LatLng poly1 = getIntent().getExtras().getParcelable("poly1");
                    LatLng poly2 = getIntent().getExtras().getParcelable("poly2");
                    LatLng poly3 = getIntent().getExtras().getParcelable("poly3");
                    LatLng poly4 = getIntent().getExtras().getParcelable("poly4");
                    output.write(("startgame_" + poly1.latitude + "_" + poly1.longitude + "_" + poly2.latitude + "_" + poly2.longitude + "_" + poly3.latitude + "_" + poly3.longitude + "_" + poly4.latitude + "_" + poly4.longitude + "_" + "eliminate_2").getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent intent = getIntent();
                intent.setClass(InviteActivity.this, GameplayActivity.class);
                startActivity(intent);
            }
        });
        thread.start();
    }
}