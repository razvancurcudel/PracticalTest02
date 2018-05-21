package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;

public class ClientThread extends Thread {

    private String address;
    private int port;
    private String command;

    private Socket socket;

    public ClientThread(String address, int port, String command) {
        this.address = address;
        this.port = port;
        this.command = command;
    }

    @Override
    public void run() {
        String commandType = null;

        Integer hour = null;
        Integer min = null;

        if (command.startsWith("set")) {
            commandType = "set";
            String[] commandParams= command.split(",");
            hour = Integer.parseInt(commandParams[1]);
            min = Integer.parseInt(commandParams[2]);
        } else if (command.startsWith("reset")) {
            commandType = "reset";
        } else if (command.startsWith("poll")) {
            commandType = "poll";
        }

        Log.i(Constants.TAG, commandType + hour + min);

        try {
            socket = new Socket(address, port);
            if (socket == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Could not create socket!");
                return;
            }
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            printWriter.println(commandType);
            printWriter.flush();
            printWriter.println(hour);
            printWriter.flush();
            printWriter.println(min);
            printWriter.flush();
            String response;
            if(commandType.equals("poll"))
            {
                while ((response = bufferedReader.readLine()) != null) {
                    Log.i(Constants.TAG, "ALARM IS " + response);
                }
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

}
