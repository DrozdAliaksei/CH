package cleverhouse.ch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

public class Controllers extends AppCompatActivity implements View.OnClickListener {

    Button turnOn;
    Button turnOff;
    Spinner pinArray;

    Intent data;

    SharedPreferences startData;
    final String SAVED_IP = "saved_ip";
    final String SAVED_PORT = "saved_port";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controllers);
        turnOn = findViewById(R.id.turnOn);
        turnOff = findViewById(R.id.turnOff);
        pinArray = findViewById(R.id.pinArray);
        turnOn.setOnClickListener(this);
        turnOff.setOnClickListener(this);
        pinArray.setSelection(12);
        data = getIntent();
    }

    @Override
    public void onClick(View view) {


        // номер вывода
        String parameterValue = "";
        // получить ip адрес
        //String ipAddress = startData.getString(SAVED_IP,"");
        String ipAddress = data.getStringExtra("ipAddress");
        // получить номер порта
        //String portNumber = startData.getString(SAVED_PORT,"");
        String portNumber = data.getStringExtra("port");

        switch (view.getId()){
            case R.id.turnOn:
                //ToDo
                parameterValue = "13";
                break;
            case R.id.turnOff:
                //ToDo
                parameterValue = "13";
                break;
        }

        // выполнить HTTP запрос
        if(ipAddress.length()>0 && portNumber.length()>0) {
            new HttpRequestAsyncTask(
                    view.getContext(), parameterValue, ipAddress, portNumber, "pin"
            ).execute();
        }
    }

    /**
     * Description: Послать HTTP Get запрос на указанные ip адрес и порт.
     * Также послать параметр "parameterName" со значением "parameterValue".
     * @param parameterValue номер порта, у которого необходимо изменить состояние
     * @param ipAddress ip адрес, на который необходимо послать запрос
     * @param portNumber номер порта ip адреса
     * @param parameterName
     * @return Текст ответа с ip адреса или сообщение ERROR, если не получилось получить ответ
     */
    public String sendRequest(String parameterValue, String ipAddress, String portNumber, String parameterName) {
        String serverResponse = "ERROR";

        try {

            HttpClient httpclient = new DefaultHttpClient(); // создать HTTP клиента
            // установить URL, например, http://myIpaddress:myport/?pin=13 (например, переключить вывод 13)
            URI website = new URI("http://"+ipAddress+":"+portNumber+"/?"+parameterName+"="+parameterValue);
            HttpGet getRequest = new HttpGet(); // создать объект HTTP GET
            getRequest.setURI(website);         // установить URL для GET запроса
            HttpResponse response = httpclient.execute(getRequest); // выполнить запрос
            // получить ответ сервера с заданным ip адресом
            InputStream content = null;
            content = response.getEntity().getContent();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    content
            ));
            serverResponse = in.readLine();
            // Закрыть соединение
            content.close();
        } catch (ClientProtocolException e) {
            // ошибка HTTP
            serverResponse = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            // ошибка ввода/вывода
            serverResponse = e.getMessage();
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // ошибка синтаксиса URL
            serverResponse = e.getMessage();
            e.printStackTrace();
        }
        // вернуть текст отклика сервера
        return serverResponse;
    }


    /**
     * AsyncTask необходим для выполнения HTTP запроса в фоне, чтобы они не блокировали
     * пользовательский интерфейс.
     */
    private class HttpRequestAsyncTask extends AsyncTask<Void, Void, Void> {

        // объявить необходимые переменные
        private String requestReply,ipAddress, portNumber;
        private Context context;
        private AlertDialog alertDialog;
        private String parameter;
        private String parameterValue;

        /**
         * Description: Конструктор класса asyncTask. Назначить значения, используемые в других методах.
         * @param context контекст приложения, необходим для создания диалога
         * @param parameterValue номер вывода для переключения
         * @param ipAddress ip адрес, на который необходимо послать запрос
         * @param portNumber номер порта ip адреса
         */
        public HttpRequestAsyncTask(Context context, String parameterValue, String ipAddress, String portNumber, String parameter)
        {
            this.context = context;

            alertDialog = new AlertDialog.Builder(this.context)
                    .setTitle("HTTP Response From IP Address:")
                    .setCancelable(true)
                    .create();

            this.ipAddress = ipAddress;
            this.parameterValue = parameterValue;
            this.portNumber = portNumber;
            this.parameter = parameter;
        }

        /**
         * Name: doInBackground
         * Description: Отправляет запрос на ip адрес
         * @param voids
         * @return
         */
        @Override
        protected Void doInBackground(Void... voids) {
            alertDialog.setMessage("Data sent, waiting for reply from server...");
            if(!alertDialog.isShowing())
            {
                alertDialog.show();
            }
            requestReply = sendRequest(parameterValue,ipAddress,portNumber, parameter);
            return null;
        }

        /**
         * Name: onPostExecute
         * Description: Данная функция выполняется после возвращения ответа на HTTP запрос на ip адрес.
         * Функция устанавливает сообщение диалога с текстом ответа от сервера и отображает диалог,
         * если он уже не показан (в случае, если он был закрыт случайно);
         * @param aVoid void параметр
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            alertDialog.setMessage(requestReply);
            if(!alertDialog.isShowing())
            {
                alertDialog.show(); // показать диалог
            }
        }

        /**
         * Name: onPreExecute
         * Description: Данная функция выполняется перед отправкой HTTP запроса на ip адрес.
         * Функция установит сообщение диалога и отобразит диалоговое окно.
         */
        @Override
        protected void onPreExecute() {
            alertDialog.setMessage("Sending data to server, please wait...");
            if(!alertDialog.isShowing())
            {
                alertDialog.show();
            }
        }

    }

}
