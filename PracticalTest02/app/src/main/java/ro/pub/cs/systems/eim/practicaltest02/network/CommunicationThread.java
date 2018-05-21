package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;
import ro.pub.cs.systems.eim.practicaltest02.model.Alarm;
import ro.pub.cs.systems.eim.practicaltest02.model.WeatherForecastInformation;

public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client");
            String command = bufferedReader.readLine();
            Integer hour = null;
            Integer min = null;
            if (command == null || command.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client ");
                return;
            }

            if (command.equals("set")) {
                hour = Integer.parseInt(bufferedReader.readLine());
                min = Integer.parseInt(bufferedReader.readLine());
                serverThread.setData(socket.getInetAddress().toString(), new Alarm(hour, min));
            }
            if (command.equals("reset")) {
                serverThread.getData().remove(socket.getInetAddress().toString());
            }
            if (command.equals("poll")) {
                HashMap<String, Alarm> data = serverThread.getData();
                if (data.containsKey(socket.getInetAddress().toString())) {
                    Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                    Alarm alarm = data.get(socket.getInetAddress().toString());

                    Socket nistSocket = new Socket("128.138.140.44", 13);
                    if (nistSocket == null) {
                        Log.e(Constants.TAG, "[COMM THREAD] Could not create socket!");
                        return;
                    }

                    BufferedReader bf = Utilities.getReader(nistSocket);
                    bf.readLine();
                    String time = bf.readLine();

                    Log.i(Constants.TAG, "[COMM THREAD] TIME IS: " + time);
                    Integer nistHour = Integer.parseInt((time.split("\\s+")[2]).split(":")[0]);
                    Integer nistMin = Integer.parseInt((time.split("\\s+")[2]).split(":")[1]);

                    if(alarm.hour < nistHour)
                    {
                        printWriter.println("Inactive");
                        return;
                    }
                    else if(alarm.hour == nistHour && alarm.min < nistMin) {
                        printWriter.println("Inactive");
                        printWriter.flush();
                        return;
                    }
                    else
                    {
                        printWriter.println("Active");
                        printWriter.flush();
                        return;
                    }
                } else {
                    Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
                    printWriter.println("none");
                    printWriter.flush();
                }

                socket.close();
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

//    HashMap<String, Alarm> data = serverThread.getData();
//        if(data.containsKey(socket.getInetAddress().
//
//    toString()))
//
//    {
//        Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
//        Alarm alarm = data.get(socket.getInetAddress().toString());
//    } else
//
//    {
//        Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
////                HttpClient httpClient = new DefaultHttpClient();
////                HttpPost httpPost = new HttpPost(Constants.WEB_SERVICE_ADDRESS);
////                List<NameValuePair> params = new ArrayList<>();
////                params.add(new BasicNameValuePair(Constants.QUERY_ATTRIBUTE, city));
////                UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
////                httpPost.setEntity(urlEncodedFormEntity);
////                ResponseHandler<String> responseHandler = new BasicResponseHandler();
////                String pageSourceCode = httpClient.execute(httpPost, responseHandler);
////                if (pageSourceCode == null) {
////                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
////                    return;
////                }
////                Document document = Jsoup.parse(pageSourceCode);
////                Element element = document.child(0);
////                Elements elements = element.getElementsByTag(Constants.SCRIPT_TAG);
////                for (Element script: elements) {
////                    String scriptData = script.data();
////                    if (scriptData.contains(Constants.SEARCH_KEY)) {
////                        int position = scriptData.indexOf(Constants.SEARCH_KEY) + Constants.SEARCH_KEY.length();
////                        scriptData = scriptData.substring(position);
////                        JSONObject content = new JSONObject(scriptData);
////                        JSONObject currentObservation = content.getJSONObject(Constants.CURRENT_OBSERVATION);
////                        String temperature = currentObservation.getString(Constants.TEMPERATURE);
////                        String windSpeed = currentObservation.getString(Constants.WIND_SPEED);
////                        String condition = currentObservation.getString(Constants.CONDITION);
////                        String pressure = currentObservation.getString(Constants.PRESSURE);
////                        String humidity = currentObservation.getString(Constants.HUMIDITY);
////                        weatherForecastInformation = new WeatherForecastInformation(
////                                temperature, windSpeed, condition, pressure, humidity
////                        );
////                        serverThread.setData(city, weatherForecastInformation);
////                        break;
////                    }
////                }
//    }
//
//    //            if (alarm == null) {
////                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Weather Forecast Information is null!");
////                return;
////            }
//    String result = null;
////            switch(informationType) {
////                case Constants.ALL:
////                    result = weatherForecastInformation.toString();
////                    break;
////                case Constants.TEMPERATURE:
////                    result = weatherForecastInformation.getTemperature();
////                    break;
////                case Constants.WIND_SPEED:
////                    result = weatherForecastInformation.getWindSpeed();
////                    break;
////                case Constants.CONDITION:
////                    result = weatherForecastInformation.getCondition();
////                    break;
////                case Constants.HUMIDITY:
////                    result = weatherForecastInformation.getHumidity();
////                    break;
////                case Constants.PRESSURE:
////                    result = weatherForecastInformation.getPressure();
////                    break;
////                default:
////                    result = "[COMMUNICATION THREAD] Wrong information type (all / temperature / wind_speed / condition / humidity / pressure)!";
////            }
////        printWriter.println(result);
////        printWriter.flush();
////    } catch(
////    IOException ioException)
//
//    {
//        Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
//        if (Constants.DEBUG) {
//            ioException.printStackTrace();
//        }
//    } finally
//
//    {
//        if (socket != null) {
//            try {
//                socket.close();
//            } catch (IOException ioException) {
//                Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
//                if (Constants.DEBUG) {
//                    ioException.printStackTrace();
//                }
//            }
//        }
//    }

}
