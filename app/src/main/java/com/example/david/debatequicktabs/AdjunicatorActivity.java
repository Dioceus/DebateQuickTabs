package com.example.david.debatequicktabs;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
//import androidlabs.gsheets2.R;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class AdjunicatorActivity extends Activity {
    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    private Button mCallApiButton;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Call Google Sheets API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS_READONLY };

    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        EditText urlInput = (EditText) findViewById(R.id.spreadsheetURL);
        EditText teamNameInput = (EditText)findViewById(R.id.govTeamName);
        EditText firstSpeakerInput = (EditText)findViewById(R.id.firstGovSpeaker);
        EditText firstPointInput = (EditText)findViewById(R.id.firstGovPoints);
        EditText secondSpeakerInput = (EditText)findViewById(R.id.secondGovSpeaker);
        EditText secondPointInput = (EditText)findViewById(R.id.secondGovPoints);

        final String sheetURL = urlInput.getText().toString();
        String teamName = teamNameInput.getText().toString();
        String speaker[] = new String[2];
        speaker[0] = firstSpeakerInput.getText().toString();
        speaker[1] = secondSpeakerInput.getText().toString();

        int speakerPoints[] = new int[2];
        speakerPoints[0] = Integer.parseInt(firstPointInput.getText().toString());
        speakerPoints[1] = Integer.parseInt(secondPointInput.getText().toString());
        int totalPoints = speakerPoints[0] + speakerPoints[1];

        Team govTeam = new Team(teamName, speaker, speakerPoints, totalPoints);

        teamNameInput = (EditText)findViewById(R.id.oppTeamName);
        firstSpeakerInput = (EditText)findViewById(R.id.firstOppSpeaker);
        firstPointInput = (EditText)findViewById(R.id.firstOppPoints);
        secondSpeakerInput = (EditText)findViewById(R.id.secondOppSpeaker);
        secondPointInput = (EditText)findViewById(R.id.secondOppPoints);

        teamName = teamNameInput.getText().toString();
        speaker[0] = firstSpeakerInput.getText().toString();
        speakerPoints[0] = Integer.parseInt(firstPointInput.getText().toString());
        speaker[1] = secondSpeakerInput.getText().toString();
        speakerPoints[1] = Integer.parseInt(secondPointInput.getText().toString());
        totalPoints = speakerPoints[0] + speakerPoints[1];

        Team oppTeam = new Team(teamName, speaker, speakerPoints, totalPoints);

        Button doneBtn = (Button) findViewById(R.id.doneBtn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //Assume all people/Teams have been listed in the spreadsheet
                //postDataParamas.put
                new SendRequest(sheetURL).execute();
            }
        });

    }
}  public class SendRequest extends AsyncTask<String, Void, String> {

    String inputURL;

    public SendRequest(String inputURL) {
        this.inputURL = inputURL;
    }

    protected void onPreExecute(){}

    protected String doInBackground(String... arg0) {

        try{

            URL url = new URL(inputURL);
            // https://script.google.com/macros/s/AKfycbyuAu6jWNYMiWt9X5yp63-hypxQPlg5JS8NimN6GEGmdKZcIFh0/exec
            JSONObject postDataParams = new JSONObject();

            for (int i = 0; i < inputURL.length(); i++) {
                if (inputURL.charAt(i).contentEquals('/')) {

                }
            }

            String id= "1hYZGyo5-iFpuwofenZ6s-tsaFPBQRSx9HQYydigA4Dg";

            postDataParams.put("name",name);
            postDataParams.put("country",country);
            postDataParams.put("id",id);


            Log.e("params",postDataParams.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();

            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {

                BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer sb = new StringBuffer("");
                String line="";

                while((line = in.readLine()) != null) {

                    sb.append(line);
                    break;
                }

                in.close();
                return sb.toString();

            }
            else {
                return new String("false : "+responseCode);
            }
        }
        catch(Exception e){
            return new String("Exception: " + e.getMessage());
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(getApplicationContext(), result,
                Toast.LENGTH_LONG).show();

    }
}

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }
}

