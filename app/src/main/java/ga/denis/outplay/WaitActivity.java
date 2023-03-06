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

                for (;;) {
                    String message = "";
                    try {
                        message = bufferedReader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String[] divided = message.split("_");

                    System.out.println(message);

                    if (divided[0].equals("startgame")) {
                        Intent intent = getIntent();
                        intent.setClass(WaitActivity.this, GameplayActivity.class);
                        intent.putExtra("poly1", new LatLng(Double.parseDouble(divided[1]),Double.parseDouble(divided[2])));
                        intent.putExtra("poly2", new LatLng(Double.parseDouble(divided[3]),Double.parseDouble(divided[4])));
                        intent.putExtra("poly3", new LatLng(Double.parseDouble(divided[5]),Double.parseDouble(divided[6])));
                        intent.putExtra("poly4", new LatLng(Double.parseDouble(divided[7]),Double.parseDouble(divided[8])));
                        intent.putExtra("team", divided[9]);
                        intent.putExtra("playerID", Integer.parseInt(divided[10]));
                        startActivity(intent);
                    }
                }
            }
        });
        thread.start();
    }
}