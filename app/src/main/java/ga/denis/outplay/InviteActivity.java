package ga.denis.outplay;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        qrCode = (ImageView) findViewById(R.id.qrCode);
        inviteContinue = (Button) findViewById(R.id.inviteContinue);
        inviteContinue.setOnClickListener(this);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("attempting connection");
                    SocketHandler.setSocket(new Socket("217.30.67.109", 10000));
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
                            null, QRGContents.Type.TEXT, 1024);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                qrgEncoder.setColorBlack(Color.BLACK);
                qrgEncoder.setColorWhite(Color.WHITE);
                qr = Bitmap.createBitmap(qrgEncoder.getBitmap(), 10, 10, 800, 800);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        qrCode.setImageBitmap(qr);
                    }
                });
            }
        });

        thread.start();
    }

    @Override
    public void onClick(View v) {
        Intent intent = getIntent();
        intent.setClass(InviteActivity.this, GameplayActivity.class);
        startActivity(intent);
    }
}