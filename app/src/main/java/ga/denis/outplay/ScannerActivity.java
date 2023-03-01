package ga.denis.outplay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import ga.denis.outplay.ui.WaitActivity;

public class ScannerActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
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
                                    SocketHandler.setSocket(new Socket("217.30.67.109",10000));
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

                                startActivity(new Intent(ScannerActivity.this, WaitActivity.class));
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