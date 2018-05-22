package cleverhouse.ch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class Connect extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "MyLog";

    EditText infoIp;
    EditText infoPort;
    Button btnOk;
    Button scanWifi;
    Switch statusConnection;
    ListView listView;

    SharedPreferences startData;
    final String SAVED_IP = "saved_ip";
    final String SAVED_PORT = "saved_port";

    private Element [] nets;
    private WifiManager wifiManager;
    private List<ScanResult> wifiList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        infoIp = findViewById(R.id.infoIp);
        infoPort = findViewById(R.id.infoPort);
        btnOk = findViewById(R.id.btnOK);
        btnOk.setOnClickListener(this);
        scanWifi = findViewById(R.id.scanWifi);
        scanWifi.setOnClickListener(this);
        listView =findViewById(R.id.listView);
        statusConnection = findViewById(R.id.statusConnection);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        try {
            if (wifiManager.isWifiEnabled()){
                statusConnection.setChecked(true);
            }
        }
        catch (NullPointerException e){
            Log.d(TAG,"NullPointerException first switch initiation");
        }

        statusConnection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                    wifiManager.setWifiEnabled(true);
                else
                    wifiManager.setWifiEnabled(false);
            }
        });

        loadText();
    }

    void saveText() {
        startData = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = startData.edit();
        ed.putString(SAVED_IP, infoIp.getText().toString());
        ed.putString(SAVED_PORT, infoPort.getText().toString());
        ed.commit();
        //Toast.makeText(this, "Text saved", Toast.LENGTH_SHORT).show();
    }

    void loadText() {
        startData = getPreferences(MODE_PRIVATE);
        String savedIp = startData.getString(SAVED_IP, "");
        infoIp.setText(savedIp);
        String savedPort = startData.getString(SAVED_PORT, "");
        infoPort.setText(savedPort);
        //Toast.makeText(this, "Text loaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scanWifi:
                Log.d(TAG,"button scan start");
                try {
                    detectWifi();
                    Snackbar.make(view,"                   Scaning...",Snackbar.LENGTH_SHORT).setAction("Action",null).show();
                }
                catch (Exception e){
                Toast toast = Toast.makeText(getApplicationContext(),"Check Your Wifi State",Toast.LENGTH_SHORT);
                toast.show();
                }
                break;
            case R.id.btnOK:
                Log.d(TAG,"button OK start");
                saveText();
                Intent intent = new Intent(this, Controllers.class);
                intent.putExtra("ipAddress", infoIp.getText().toString());
                intent.putExtra("port", infoPort.getText().toString());
                startActivity(intent);
                break;
        }
    }

    public void detectWifi(){
        Log.d(TAG,"inside method");
        this.wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.wifiManager.startScan();
        this.wifiList = this.wifiManager.getScanResults();

        Log.d("TAG", wifiList.toString());

        this.nets = new Element[wifiList.size()];

        for (int i = 0; i<wifiList.size(); i++){
            String item = wifiList.get(i).toString();
            String[] vector_item = item.split(",");
            String item_essid = vector_item[0];
            String item_capabilities = vector_item[2];
            String item_level = vector_item[3];
            String ssid = item_essid.split(": ")[1];
            String security = item_capabilities.split(": ")[1];
            String level = item_level.split(": ")[1];
            nets[i] = new Element(ssid, security, level);
        }

        AdapterElements adapterElements = new AdapterElements(this);
        ListView netList = findViewById(R.id.listView);
        netList.setAdapter(adapterElements);
    }
    class AdapterElements extends ArrayAdapter<Object> {
        Activity context;

        public AdapterElements(Activity context) {
            super(context, R.layout.items, nets);
            this.context = context;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = context.getLayoutInflater();
            View item = inflater.inflate(R.layout.items, null);

            TextView tvSsid = item.findViewById(R.id.tvSSID);
            tvSsid.setText(nets[position].getTitle());

            TextView tvSecurity = item.findViewById(R.id.tvSecurity);
            tvSecurity.setText(nets[position].getSecurity());

            TextView tvLevel = item.findViewById(R.id.tvLevel);
            String level = nets[position].getLevel();
            try{
                int i = Integer.parseInt(level);
                if (i>-50){
                    tvLevel.setText("Высокий");
                } else if (i<=-50 && i>-80){
                    tvLevel.setText("Средний");
                } else if (i<=-80){
                    tvLevel.setText("Низкий");
                }
            } catch (NumberFormatException e){
                Log.d("TAG", "Неверный формат строки");
            }
            return item;
        }
    }
}
