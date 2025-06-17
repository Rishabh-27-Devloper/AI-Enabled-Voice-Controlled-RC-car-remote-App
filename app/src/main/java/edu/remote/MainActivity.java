package edu.remote;


import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import com.google.gson.Gson;
import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.widget.Switch;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;


@SuppressLint("UseSwitchCompatOrMaterialCode")
public class MainActivity extends AppCompatActivity {
    private SpeechRecognizer speechRecognizer;
    private static final String ESP32_DEVICE_NAME = "ESP32 RC"; // Replace with your ESP32 Bluetooth name
    private static final String ESP32_MAC_ADDRESS = "3C:8A:1F:7D:AE"; // Replace with your ESP32 MAC address
    private static final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SPP UUID

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private SeekBar messageInput;
    private ProgressBar loader;
    private Switch gear;
    private FloatingActionButton LBtn;
    private Button stopButton;
    private Gson gson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestBluetoothPermissions();
        }
        gson = new Gson();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        messageInput = findViewById(R.id.seekBar);
        loader = findViewById(R.id.progressBar);
        gear = findViewById(R.id.switch1);
        LBtn = findViewById(R.id.floatingActionButton);
        Button connectButton = findViewById(R.id.connectButton);
        stopButton = findViewById(R.id.button);
        stopButton.setOnClickListener(v -> sendDataToESP32("STOP"));
        FloatingActionButton normal = findViewById(R.id.floatingActionButton3);
        ImageButton voice = findViewById(R.id.imageButton);
        voice.setOnClickListener(v -> voiceCommandStart());
        normal.setOnClickListener(v -> sendDataToESP32("NORM"));
        stopLoading();
        connectButton.setOnClickListener(v -> connectToESP32());
        LBtn.setOnClickListener(v -> Direction("L"));
        messageInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String dat = "";
                if (gear.isChecked()){
                    dat += "F";
                }else {
                    dat += "B";
                }
                StringBuilder force = new StringBuilder(String.valueOf(progress));
                if (force.length() < 3){
                    for (int i=0;i<=(3-force.length());i++){
                        force.insert(0, "0");
                    }
                }
                dat += force;
                Log.e("Send Data",dat);
                sendDataToESP32(dat);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    private void showLoading(){
        loader.setVisibility(VISIBLE);
    }
    private void stopLoading(){
        loader.setVisibility(INVISIBLE);
    }
    private void voiceCommandStart(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        startListening();
    }
    private void startListening() {
        final String context = "As the AI driver of a toy car, generate a command sequence based on user instructions.\n" +
                "Commands:\n" +
                "Back Wheels:\n" +
                "FXXX: Forward at speed 0–255 (e.g., F112).\n" +
                "BXXX: Backward at speed 0–255 (e.g., B200).\n" +
                "STOP: Halt all wheels.\n" +
                "Front Wheels:\n" +
                "LEFT: Turn left.\n" +
                "RITE: Turn right.\n" +
                "NORM: Straighten.\n" +
                "Timing:\n" +
                "DXXXXX: Delay XXXXX ms (1–60,000).\n" +
                "Guidelines\n" +
                "Output only the command array.\n" +
                "For unrealistic requests (e.g., \"Fly the car\"), return [LEFT, D00100, RITE, D00100, NORM, STOP].\n" +
                "Example:\n" +
                "User: \"Go straight, then right.\"\n" +
                "Response: [F200, D05000, RITE, D00100, NORM, D02000, STOP]"+
                "Response sechema:{\"commandArray\":[list,of,commands]}";
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Toast.makeText(MainActivity.this, "Listening...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                Toast.makeText(MainActivity.this, "Processing...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int error) {
                Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    TextView textView = findViewById(R.id.textView4);
                    String Command = matches.get(0);
                    if (!Command.isEmpty()) {
                        textView.setText("Processing...\n"+Command);
                        GeminiApiService.generateTextUsingSdk(context+"\n and the command is \n"+Command, new GeminiApiService.ResponseCallback() {
                            @Override
                            public void onSuccess(String response) {
//                                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error: " + response, Toast.LENGTH_SHORT).show());
                                runOnUiThread(() -> textView.setText("Executing...\n"+Command));
                                Log.e("Executor",response);
                                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                                JsonArray Array = jsonObject.getAsJsonArray("commandArray");
                                String[] commandArray = gson.fromJson(Array, String[].class);
                                try{
                                    ExecuteInstructions(commandArray);
                                }catch (Exception e){
                                    runOnUiThread(() -> textView.setText("Execution Failed...Check Connection with ESP32, Error Details:\n"+ e));
                                }


                            }

                            @Override
                            public void onFailure(String error) {
                                Log.e("AI API",error);
                                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show());
                            }
                        });
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognizer.startListening(intent);
    }
    private void Direction(String dir){
        if (Objects.equals(dir, "L")) {
            sendDataToESP32("LEFT");
        } else if (Objects.equals(dir,"R")) {
            sendDataToESP32("RITE");
        }
    }
    private void ExecuteInstructions(String[] commands){
        for (String command : commands) {
            if (Objects.equals(command.charAt(0) ,'D')){
                int Delay = Integer.parseInt(command.substring(1));
                try {
                    Thread.sleep(Delay);  // Sleep for 2000 milliseconds (2 seconds)
                } catch (InterruptedException e) {
                    Log.e("Error", String.valueOf(e));
                }
            }else{
                sendDataToESP32(command);
            }
        }
    }
    @SuppressLint("MissingPermission")
    private void connectToESP32() {
        showLoading();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            stopLoading();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Enable Bluetooth first", Toast.LENGTH_SHORT).show();
            stopLoading();
            return;
        }

        BluetoothDevice device = findESP32Device();
        if (device == null) {
            Toast.makeText(this, "ESP32 not found", Toast.LENGTH_SHORT).show();
            stopLoading();
            return;
        }

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            Toast.makeText(this, "Connected to ESP32", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("Bluetooth", "Connection failed", e);
            Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show();
        }finally {
            stopLoading();
        }
    }

    @SuppressLint("MissingPermission")
    private BluetoothDevice findESP32Device() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices) {
            if (ESP32_DEVICE_NAME.equals(device.getName()) || ESP32_MAC_ADDRESS.equals(device.getAddress())) {
                return device;
            }
        }
        return null;
    }

    private void sendDataToESP32(String data) {
        if (outputStream == null) {
            Toast.makeText(this, "Not connected to ESP32", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            outputStream.write(data.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            Log.e("Bluetooth", "Error sending data", e);
            Toast.makeText(this, "Failed to send data", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void requestBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
        }
    }
}
