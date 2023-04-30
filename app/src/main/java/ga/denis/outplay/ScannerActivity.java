package ga.denis.outplay;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class ScannerActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA},1);;
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ScannerActivity.this, result.getText(), Toast.LENGTH_SHORT).show();

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    SocketHandler.setSocket(new Socket("142.132.174.213", 10000));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                OutputStream output = null;

                                try {
                                    output = SocketHandler.getSocket().getOutputStream();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    output.write(result.getText().getBytes());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    output.write(("addplayer_" + SocketHandler.getName()).getBytes());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                BufferedReader bufferedReader = null;

                                try {
                                    bufferedReader = new BufferedReader(new InputStreamReader(SocketHandler.getSocket().getInputStream()));
                                    if (bufferedReader != null) System.out.println("bufferedReader set");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                String message = "";

                                while (!message.split("_")[0].equals("setID")) {
                                    try {
                                        message = bufferedReader.readLine();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                Intent intent = new Intent(ScannerActivity.this, WaitActivity.class);
                                intent.putExtra("playerID", Integer.parseInt(message.split("_")[1]));
                                startActivity(intent);
                                finish();
                                return;
                            }
                        });
                        thread.start();
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}