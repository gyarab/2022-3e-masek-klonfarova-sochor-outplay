package ga.denis.outplay.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ga.denis.outplay.GameplayActivity;
import ga.denis.outplay.R;
import ga.denis.outplay.SocketHandler;

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
                    if (message.equals("startgame")) {
                        Intent intent = getIntent();
                        intent.setClass(WaitActivity.this, GameplayActivity.class);
                    }
                }
            }
        });
    }
}