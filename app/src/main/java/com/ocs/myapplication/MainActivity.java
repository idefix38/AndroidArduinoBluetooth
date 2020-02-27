package com.ocs.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
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
        lvPeripheriques = findViewById(R.id.lvPeripheriques);
        btnAllumer = findViewById(R.id.btnAllumer);
        btnEteindre = findViewById(R.id.btnEteindre);
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


            // Recherche des périphériques connus
            Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
            peripheriques = new ArrayList<PeripheriqueBluetooth>();

            // Boucle sur la liste des BluetoothDevice pour créer une liste de d'objets Peripherique
            for (BluetoothDevice blueDevice : devices) {
                PeripheriqueBluetooth p = new PeripheriqueBluetooth(blueDevice,handler);
                peripheriques.add(p);
            }
            // Création de l'adapteur pour la listview
            PeripheriqueListViewAdaptater adaptater = new PeripheriqueListViewAdaptater(peripheriques);
            lvPeripheriques.setAdapter(adaptater);


        }
    }

    private class PeripheriqueListViewAdaptater extends BaseAdapter {
        private List<PeripheriqueBluetooth> list;

        //Constructeur
        public PeripheriqueListViewAdaptater( List<PeripheriqueBluetooth> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public PeripheriqueBluetooth getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * Retourne la vue d'un peripherique
         * @param position
         * @param convertView
         * @param parent
         * @return
         */
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view= getLayoutInflater().inflate(R.layout.listeperipheriqueslayout,null);
            // Texte et bouton de la vue listeperipheriqueslayout
            TextView name = view.findViewById(R.id.txtPeriphName);
            Button button = view.findViewById(R.id.btnConnecter);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // On click sur le bouton connecter
                    arduino = list.get(position);
                    arduino.connecter();
                    Toast.makeText(getApplicationContext(), "Connexion a " + arduino.getNom(), Toast.LENGTH_SHORT).show();
                }
            });
            name.setText(list.get(position).getNom());
            return view;
        }
    }



}
