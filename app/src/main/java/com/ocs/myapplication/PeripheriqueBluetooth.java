package com.ocs.myapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class PeripheriqueBluetooth extends Thread
{
    // Connection entre le smartphone et l'arduino
    private BluetoothSocket socket = null;
    // Flux de reception des données
    private InputStream receiveStream = null;
    // Flux d'écriture des données
    private OutputStream sendStream = null;

    // Thread de reception
    private Reception reception=null;

    // Etat pour savoir si le periherique est connecté
    private boolean estConnecte = false;

    private String nom;
    private String adresse;
    private Handler handler = null;
    private BluetoothDevice device = null;

    /**
     * Constructeur
     * @param device
     * @param handler
     */
    public PeripheriqueBluetooth(BluetoothDevice device, Handler handler)
    {
        if(device != null)
        {
            this.device = device;
            this.nom = device.getName();
            this.adresse = device.getAddress();
            this.handler = handler;
        }
        else {
            this.device = device;
            this.nom = "Aucun";
            this.adresse = "";
            this.handler = handler;
        }
        try
        {
            // Création de la connection
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            receiveStream = socket.getInputStream();
            sendStream = socket.getOutputStream();
        }
        catch (IOException e)
        {
            Log.d("DEBUG","Erreur de creation du socket sur " + this.nom);
            //e.printStackTrace();
            socket = null;
        }

        if(socket != null) {
            reception = new Reception(handler);
        }
    }

    /**
     * Retourne le nom du péripherique
     * @return
     */
    public String getNom()
    {
        return nom;
    }

    /**
     * Retourne l'adresse du péripherique
     * @return
     */
    public String getAdresse()
    {
        return adresse;
    }

    /**
     * Retourne True ou False selon que le péripherique est connecté ou non
     * @return
     */
    public boolean estConnecte()
    {
        return estConnecte;
    }

    public void setNom(String nom)
    {
        this.nom = nom;
    }

    public String toString()
    {
        return "\nNom : " + nom + "\nAdresse : " + adresse;
    }

    /**
     * Envoie des données au module arduino
     * @param data
     */
    public void envoyer(final String data)
    {
        if(socket == null)
            return;

        new Thread()
        {
            @Override public void run()
            {
                try
                {
                    if(socket.isConnected())
                    {
                        Log.d("DEBUG","Envoi des données : " + data);
                        sendStream.write(data.getBytes());
                        sendStream.flush();
                    }
                }
                catch (IOException e)
                {
                    Log.d("DEBUG","Erreur sending data " );
                }
            }
        }.start();

    }

    /**
     * Connection du smartphone à l'arduino
     */
    public void connecter()
    {
        Log.d("DEBUG","Connexion à " + this.nom);

        new Thread()
        {
            @Override public void run()
            {
                try
                {
                    Log.d("DEBUG","Demarrage du thread  ");
                    socket.connect();
                    Log.d("DEBUG","socket connect OK  ");

                    //Message msg = Message.obtain();
                    //msg.arg1 = 1;
                    //handler.sendMessage(msg);
                    reception.start();
                    Log.d("DEBUG","Reception.start() OK  ");

                }
                catch (IOException e)
                {
                    Log.d("DEBUG","<Socket> error connect ");
                }
            }
        }.start();
    }

    /**
     * Deconnexion du smartphone
     * @return
     */
    public boolean deconnecter()
    {
        try
        {
            reception.arreter();
            socket.close();
            return true;
        }
        catch (IOException e)
        {
            System.out.println("<Socket> error close");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Class pour gérer la reception des données
     */
    private class Reception extends Thread {

        Handler handlerUI;
        private boolean fini;

        /**
         * Constructeur
         * @param h
         */
        Reception(Handler h) {
            handlerUI = h;
            fini = false;
        }

        /**
         * Lecture des données en provenance de l'arduino
         */
        @Override
        public void run() {

            Log.d("DEBUG","Reception Run() " );
            BufferedReader reception = new BufferedReader(new InputStreamReader(receiveStream));

            //Lecture des infos toutes les 250 millisecondes
            while (!fini) {
                try {
                    String trame = "";
                    if (reception.ready()) {
                        trame = reception.readLine();
                    }
                    if (trame.length() > 0) {
                        // Affichage dans la console des données recues
                        Log.d("DEBUG", "run() trame : " + trame);
                        Message msg = Message.obtain();
                        msg.what = 2;
                        msg.obj = trame;
                        handlerUI.sendMessage(msg);
                    }
                } catch (IOException e) {
                    Log.d("DEBUG","Erreur de lecture des donneés " );
                    //e.printStackTrace();
                }
                try {
                    // Attente de 250 millisecondes
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Stop la lecture des données
         */
        public void arreter() {
            if (fini == false) {
                fini = true;
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
