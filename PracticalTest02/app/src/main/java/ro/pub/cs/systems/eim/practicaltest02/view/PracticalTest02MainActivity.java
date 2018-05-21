package ro.pub.cs.systems.eim.practicaltest02.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ro.pub.cs.systems.eim.practicaltest02.R;
import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.network.ClientThread;
import ro.pub.cs.systems.eim.practicaltest02.network.ServerThread;

public class PracticalTest02MainActivity extends AppCompatActivity {

    // Server widgets
    private Button connectButton = null;

    // Client widgets
    private EditText clientAddressEditText = null;
    private EditText clientPortEditText = null;
    private EditText clientCommandEditText = null;
    private Button sendButton = null;

    private ServerThread serverThread = null;
    private ClientThread clientThread = null;

    private ConnectButtonClickListener connectButtonClickListener = new ConnectButtonClickListener();

    private class ConnectButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
//            String serverPort = serverPortEditText.getText().toString();
//            if (serverPort == null || serverPort.isEmpty()) {
//                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Server port should be filled!", Toast.LENGTH_SHORT).show();
//                return;
//            }
            serverThread = new ServerThread(5000);
            if (serverThread.getServerSocket() == null) {
                Log.e(Constants.TAG, "[MAIN ACTIVITY] Could not create server thread!");
                return;
            }
            serverThread.start();
        }

    }

    private SendCommandButtonClickListener sendCommandButtonClickListener = new SendCommandButtonClickListener();

    private class SendCommandButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            String clientAddress = clientAddressEditText.getText().toString();
            String clientPort = clientPortEditText.getText().toString();
            if (clientAddress == null || clientAddress.isEmpty()
                    || clientPort == null || clientPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Client connection parameters should be filled!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (serverThread == null || !serverThread.isAlive()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] There is no server to connect to!", Toast.LENGTH_SHORT).show();
                return;
            }

            String command = clientCommandEditText.getText().toString();
            if (command == null || command.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Parameters from client should be filled", Toast.LENGTH_SHORT).show();
                return;
            }

            clientThread = new ClientThread(
                    clientAddress, Integer.parseInt(clientPort), command
            );
            clientThread.start();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Constants.TAG, "[MAIN ACTIVITY] onCreate() callback method has been invoked");
        setContentView(R.layout.activity_practical_test02_main);

        connectButton = (Button) findViewById(R.id.connect_button);
        clientAddressEditText = (EditText) findViewById(R.id.client_address_edit_text);
        clientPortEditText = (EditText) findViewById(R.id.client_port_edit_text);


        clientCommandEditText = (EditText) findViewById(R.id.client_command_edit_text);
        sendButton = (Button) findViewById(R.id.send_command_button);

        connectButton.setOnClickListener(connectButtonClickListener);
        sendButton.setOnClickListener(sendCommandButtonClickListener);
//        serverPortEditText = (EditText)findViewById(R.id.server_port_edit_text);
//        clientAddressEditText = (EditText)findViewById(R.id.client_address_edit_text);
//        clientPortEditText = (EditText)findViewById(R.id.client_port_edit_text);
//        cityEditText = (EditText)findViewById(R.id.city_edit_text);
//        informationTypeSpinner = (Spinner)findViewById(R.id.information_type_spinner);
//        getWeatherForecastButton = (Button)findViewById(R.id.get_weather_forecast_button);
//        getWeatherForecastButton.setOnClickListener(getWeatherForecastButtonClickListener);
//        weatherForecastTextView = (TextView)findViewById(R.id.weather_forecast_text_view);


    }

    @Override
    protected void onDestroy() {
        Log.i(Constants.TAG, "[MAIN ACTIVITY] onDestroy() callback method has been invoked");
        if (serverThread != null) {
            serverThread.stopThread();
        }
        super.onDestroy();
    }

}
