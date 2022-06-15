package com.example.obi.network;

import android.os.AsyncTask;

import java.io.IOException;

public class ConnectTask extends AsyncTask<Void,Void,Void> {

    private TcpClient tcpClient;

    @Override
    protected Void doInBackground(Void... voids) {
        tcpClient = TcpClient.getInstance();
        try {
            tcpClient.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}