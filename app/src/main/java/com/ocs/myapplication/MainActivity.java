package com.ocs.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MainActivity extends AppCompatActivity {


    //private final static int REQUEST_CODE_ENABLE_BLUETOOTH = 0;
    private ArrayList<PeripheriqueBluetooth> peripheriques = null; // Liste des peripheriques bluetooth
    private ListView lvPeripheriques; // ListView pour afficher la liste des périphériques
    private PeripheriqueBluetooth arduino =null; // stock le péripherique choisit
    private Button btnAllumer= null;
    private Button btnEteindre =null;
    private Button btnBluetooth = null;
    private TextView temp = null;
    private TextView humid = null;
    private BluetoothAdapter blueAdapter = null;

    private final Handler handler = new Handler() {
        /**
         * Reçois le message de la classe Reception contenant les infos de l'arduino
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String donnees = msg.obj.toString();
            String[] array = donnees.split("\\|", -1);
            String[] arraytemp = array[0].split("\\:", -1);
            String[] arrayhumid = array[1].split("\\:", -1);
            //final TextView myTextView = (TextView)findViewById(R.id.myTextView);
            temp.setText("Température : " + arraytemp[1]);
            humid.setText("Humidité : " + arrayhumid[1]);
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("DEBUG","Démarrage de l'application");

        // Récupère les composants du layout
        //lvPeripheriques = findViewById(R.id.lvPeripheriques);
        btnAllumer = findViewById(R.id.btnAllumer);
        btnEteindre = findViewById(R.id.btnEteindre);
        btnBluetooth = findViewById(R.id.btnBluetooth);
        temp = findViewById(R.id.temp);
        humid = findViewById(R.id.humid);

        //Gestion du clic sur allumer
        btnAllumer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             // On a clické sur allumer
             arduino.envoyer("on\r\n");
            }
        });

        //Gestion du clic sur eteindre
        btnEteindre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On a clické sur etteindre
                arduino.envoyer("off\r\n");
            }
        });

        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On a clické sur etteindre
                Intent configBT = new Intent(getApplicationContext(), configBluetooth.class);
                startActivity(configBT);
                finish();
            }
        });


        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Le péripherique ne gère pas le bluetooth
            Toast.makeText(getApplicationContext(), "Pas de bluetooth sur ce périphérique", Toast.LENGTH_SHORT).show();
        } else {
            // Le péripherique gère le bluetooth
            // Test si le bluetooth est activé
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "Activation du bluetooth ...", Toast.LENGTH_SHORT).show();
                // Possibilité 1 :
                //Intent activeBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //startActivityForResult(activeBlueTooth, REQUEST_CODE_ENABLE_BLUETOOTH);
                // ou Possibilité 2:
                // Active le bluetooth
                bluetoothAdapter.enable();
                Toast.makeText(getApplicationContext(), "Bluetooth activé , vous devez redémarer l'application", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth activé", Toast.LENGTH_SHORT).show();
            }


            // Recupere le nom du périphérique choisit par l'utilisateur
            // Utilise le nom DSS TECH HC-05 par défaut si aucun périphéruqe n'a été choisit
            SharedPreferences settings = getApplicationContext().getSharedPreferences("ARDUINO", Context.MODE_PRIVATE);
            String arduinoName = settings.getString("ARDUINO","");

            // Recherche des périphériques connus
            Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();

            boolean peripheriqueTrouve = false;

            // Boucle sur la liste des BluetoothDevice
            for (BluetoothDevice blueDevice : devices) {

                // Si le péripherique correspond au peripherique choisit par l'utilisateur ou au peripheruq epar défaut,
                // on se connecte
                if (arduinoName.compareTo(blueDevice.getName())==0)
                {
                    PeripheriqueBluetooth p = new PeripheriqueBluetooth(blueDevice, handler);
                    peripheriqueTrouve = true;
                    p.connecter();
                    Toast.makeText(getApplicationContext(), "Connexion a " + p.getNom(), Toast.LENGTH_SHORT).show();
                    break; // Sort de la boucle for
                }
            }

            // Si aucun péripehrique ne correpond on affiche un message d'erreur
            if (!peripheriqueTrouve) {
                Toast.makeText(getApplicationContext(), "Aucun périphérique compatible n'a été trouvé. ", Toast.LENGTH_SHORT).show();
            }


        }
    }




}
