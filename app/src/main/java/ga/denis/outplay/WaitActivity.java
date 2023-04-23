package ga.denis.outplay;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
                String message1 = "";
                String message2 = "";
                String message3 = "";
                String[] divided1 = null;
                String[] divided2 = null;
                String[] divided3 = null;
                for (;;) {


                    try {
                        message = bufferedReader.readLine();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String[] divided = message.split("%");
                    System.out.println("Zprava 0 je: " + divided[0]);

                    if (divided[0].contains("startgame")) {

                        message1 = divided[0];
                        message2 = divided[1];
                        message3 = divided[2];

                        divided1 = message1.split("_");
                        divided2 = message2.split("_");
                        divided3 = message3.split("_");

                        System.out.println(message);

                    if (divided1[0].equals("startgame") && Integer.parseInt(divided1[10]) == getIntent().getExtras().getInt("playerID")) {
                        Intent intent = getIntent();
                        intent.setClass(WaitActivity.this, GameplayActivity.class);
                        intent.putExtra("poly1", new LatLng(Double.parseDouble(divided1[1]),Double.parseDouble(divided1[2])));
                        intent.putExtra("poly2", new LatLng(Double.parseDouble(divided1[3]),Double.parseDouble(divided1[4])));
                        intent.putExtra("poly3", new LatLng(Double.parseDouble(divided1[5]),Double.parseDouble(divided1[6])));
                        intent.putExtra("poly4", new LatLng(Double.parseDouble(divided1[7]),Double.parseDouble(divided1[8])));
                        intent.putExtra("team", divided1[9]);
                        //intent.putExtra("playerID", Integer.parseInt(divided[10]));
                        startActivity(intent);
                    } else if (divided2[0].equals("startgame") && Integer.parseInt(divided2[10]) == getIntent().getExtras().getInt("playerID")) {
                        Intent intent = getIntent();
                        intent.setClass(WaitActivity.this, GameplayActivity.class);
                        intent.putExtra("poly1", new LatLng(Double.parseDouble(divided2[1]),Double.parseDouble(divided2[2])));
                        intent.putExtra("poly2", new LatLng(Double.parseDouble(divided2[3]),Double.parseDouble(divided2[4])));
                        intent.putExtra("poly3", new LatLng(Double.parseDouble(divided2[5]),Double.parseDouble(divided2[6])));
                        intent.putExtra("poly4", new LatLng(Double.parseDouble(divided2[7]),Double.parseDouble(divided2[8])));
                        intent.putExtra("team", divided2[9]);
                        //intent.putExtra("playerID", Integer.parseInt(divided[10]));
                        startActivity(intent);
                    } else if (divided3[0].equals("startgame") && Integer.parseInt(divided3[10]) == getIntent().getExtras().getInt("playerID")) {
                        Intent intent = getIntent();
                        intent.setClass(WaitActivity.this, GameplayActivity.class);
                        intent.putExtra("poly1", new LatLng(Double.parseDouble(divided3[1]),Double.parseDouble(divided3[2])));
                        intent.putExtra("poly2", new LatLng(Double.parseDouble(divided3[3]),Double.parseDouble(divided3[4])));
                        intent.putExtra("poly3", new LatLng(Double.parseDouble(divided3[5]),Double.parseDouble(divided3[6])));
                        intent.putExtra("poly4", new LatLng(Double.parseDouble(divided3[7]),Double.parseDouble(divided3[8])));
                        intent.putExtra("team", divided3[9]);
                        //intent.putExtra("playerID", Integer.parseInt(divided[10]));
                        startActivity(intent);
                    }
                    }
                }
            }
        });
        thread.start();
    }
}