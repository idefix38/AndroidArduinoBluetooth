package com.ocs.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class configBluetooth extends AppCompatActivity {

    private ArrayList<PeripheriqueBluetooth> peripheriques = null; // Liste des peripheriques bluetooth
    private ListView lvPeripheriques; // ListView pour afficher la liste des périphériques
    private BluetoothAdapter blueAdapter = null;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_bluetooth);

        // Récupère les composants du layout
        lvPeripheriques = findViewById(R.id.lvPeripheriques);

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
            //peripheriques = new ArrayList<PeripheriqueBluetooth>();
            List<String> devicesName =  new ArrayList<>();

            // Boucle sur la liste des BluetoothDevice pour créer une liste de d'objets Peripherique
            for (BluetoothDevice blueDevice : devices) {
                //PeripheriqueBluetooth p = new PeripheriqueBluetooth(blueDevice, handler);
                //peripheriques.add(p);
                devicesName.add(blueDevice.getName());
            }

            // Création de l'adapteur pour la listview
            PeripheriqueListViewAdaptater adaptater = new PeripheriqueListViewAdaptater(devicesName);
            lvPeripheriques.setAdapter(adaptater);

        }
    }

    private class PeripheriqueListViewAdaptater extends BaseAdapter {
        private List<String> list;

        //Constructeur
        public PeripheriqueListViewAdaptater( List<String> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public String getItem(int position) {
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
                    // On à clické sur le bouton connecter
                    // Sauvegarde le choix de l'utilisateur dans les préférence de l'application
                    SharedPreferences settings = getApplicationContext().getSharedPreferences("ARDUINO", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    settings.edit().putString("ARDUINO", list.get(position)).commit();

                    // Retourne sur l'activité principale
                    Intent mainactivity = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(mainactivity);
                    finish();

                    //Toast.makeText(getApplicationContext(), "Connexion a " + list.get(position), Toast.LENGTH_SHORT).show();
                }
            });
            name.setText(list.get(position));
            return view;
        }
    }

}
