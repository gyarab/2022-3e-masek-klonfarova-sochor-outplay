package ga.denis.outplay;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class WaitActivity extends AppCompatActivity {
    BufferedReader bufferedReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(SocketHandler.getSocket().getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String message = "";
                String[] divided = null;

                for (; ; ) {
                    try {
                        message = bufferedReader.readLine();
                        divided = message.split("_");
                        System.out.println("Zprava 0 je: " + divided[0]);

                        if (divided[0].equals("startgame")) {

                            System.out.println(message);

                            Intent intent = getIntent();
                            intent.setClass(WaitActivity.this, GameplayActivity.class);
                            intent.putExtra("poly1", new LatLng(Double.parseDouble(divided[1]), Double.parseDouble(divided[2])));
                            intent.putExtra("poly2", new LatLng(Double.parseDouble(divided[3]), Double.parseDouble(divided[4])));
                            intent.putExtra("poly3", new LatLng(Double.parseDouble(divided[5]), Double.parseDouble(divided[6])));
                            intent.putExtra("poly4", new LatLng(Double.parseDouble(divided[7]), Double.parseDouble(divided[8])));

                            for (int i = 1; i < 5; i++) {
                                intent.putExtra("team" + i, divided[8 + i]);
                            }

                            ArrayList<LatLng> lokace = new ArrayList<>();

                            for (int i = 13; i < divided.length; i = i + 2) {
                                lokace.add(new LatLng(Double.parseDouble(divided[i]), Double.parseDouble(divided[i + 1])));
                            }

                            intent.putExtra("checkLoc", lokace);

                            //intent.putExtra("playerID", Integer.parseInt(divided[10]));
                            startActivity(intent);
                            finish();
                            return;
//                              else if (divided2[0].equals("startgame") && Integer.parseInt(divided2[10]) == getIntent().getExtras().getInt("playerID")) {
//                                Intent intent = getIntent();
//                                intent.setClass(WaitActivity.this, GameplayActivity.class);
//                                intent.putExtra("poly1", new LatLng(Double.parseDouble(divided2[1]),Double.parseDouble(divided2[2])));
//                                intent.putExtra("poly2", new LatLng(Double.parseDouble(divided2[3]),Double.parseDouble(divided2[4])));
//                                intent.putExtra("poly3", new LatLng(Double.parseDouble(divided2[5]),Double.parseDouble(divided2[6])));
//                                intent.putExtra("poly4", new LatLng(Double.parseDouble(divided2[7]),Double.parseDouble(divided2[8])));
//                                intent.putExtra("team", divided2[9]);
//                                //intent.putExtra("playerID", Integer.parseInt(divided[10]));
//                                startActivity(intent);
//                                finish();
//                                return;
//                            } else if (divided3[0].equals("startgame") && Integer.parseInt(divided3[10]) == getIntent().getExtras().getInt("playerID")) {
//                                Intent intent = getIntent();
//                                intent.setClass(WaitActivity.this, GameplayActivity.class);
//                                intent.putExtra("poly1", new LatLng(Double.parseDouble(divided3[1]),Double.parseDouble(divided3[2])));
//                                intent.putExtra("poly2", new LatLng(Double.parseDouble(divided3[3]),Double.parseDouble(divided3[4])));
//                                intent.putExtra("poly3", new LatLng(Double.parseDouble(divided3[5]),Double.parseDouble(divided3[6])));
//                                intent.putExtra("poly4", new LatLng(Double.parseDouble(divided3[7]),Double.parseDouble(divided3[8])));
//                                intent.putExtra("team", divided3[9]);
//                                //intent.putExtra("playerID", Integer.parseInt(divided[10]));
//                                startActivity(intent);
//                                finish();
//                                return;
//                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            }
        });
        thread.start();
    }
}