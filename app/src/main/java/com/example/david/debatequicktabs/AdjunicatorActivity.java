package com.example.david.debatequicktabs;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
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
import android.util.Log;

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

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import java.util.Collections;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.google.api.client.auth.oauth2.Credential;


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
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

    private static final String APPLICATION_NAME = "Debate Mobile Adjundication";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    //private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Create the main activity.
     *
     * @param savedInstanceState previously saved instance data.
     */


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        EditText urlInput = (EditText) findViewById(R.id.spreadsheetURL);
        EditText teamNameInput = (EditText) findViewById(R.id.govTeamName);
        EditText firstSpeakerInput = (EditText) findViewById(R.id.firstGovSpeaker);
        EditText firstPointInput = (EditText) findViewById(R.id.firstGovPoints);
        EditText secondSpeakerInput = (EditText) findViewById(R.id.secondGovSpeaker);
        EditText secondPointInput = (EditText) findViewById(R.id.secondGovPoints);
        EditText roundInfo = (EditText) findViewById(R.id.roundNum);

        final String sheetURL = urlInput.getText().toString();
        int roundNum = Integer.parseInt(roundInfo.getText().toString());
        String teamName = teamNameInput.getText().toString();
        String speaker[] = new String[2];
        speaker[0] = firstSpeakerInput.getText().toString();
        speaker[1] = secondSpeakerInput.getText().toString();

        int speakerPoints[] = new int[2];
        speakerPoints[0] = Integer.parseInt(firstPointInput.getText().toString());
        speakerPoints[1] = Integer.parseInt(secondPointInput.getText().toString());
        int totalPoints = speakerPoints[0] + speakerPoints[1];

        final Team govTeam = new Team(teamName, speaker, speakerPoints, totalPoints);

        teamNameInput = (EditText) findViewById(R.id.oppTeamName);
        firstSpeakerInput = (EditText) findViewById(R.id.firstOppSpeaker);
        firstPointInput = (EditText) findViewById(R.id.firstOppPoints);
        secondSpeakerInput = (EditText) findViewById(R.id.secondOppSpeaker);
        secondPointInput = (EditText) findViewById(R.id.secondOppPoints);

        teamName = teamNameInput.getText().toString();
        speaker[0] = firstSpeakerInput.getText().toString();
        speakerPoints[0] = Integer.parseInt(firstPointInput.getText().toString());
        speaker[1] = secondSpeakerInput.getText().toString();
        speakerPoints[1] = Integer.parseInt(secondPointInput.getText().toString());
        totalPoints = speakerPoints[0] + speakerPoints[1];

        final Team oppTeam = new Team(teamName, speaker, speakerPoints, totalPoints);

        if (govTeam.teamScore > oppTeam.teamScore) {
            govTeam.win = 1; //1 means team won, 0 means team lost
            oppTeam.win = 0;
        } else { //Debate should never end in a tie, thus speaker scores show never be equal
            govTeam.win = 0;
            oppTeam.win = 1;
        }

        Button doneBtn = (Button) findViewById(R.id.doneBtn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                final NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
                final String spreadsheetId = sheetURL.substring(38, 82);

                //postData needs parameters, govTeam, oppTeam, HTTP_Transport, and spreadsheetId
                postData(govTeam, oppTeam, spreadsheetId, HTTP_TRANSPORT);


    } public void postData(Team govTeam, Team oppTeam, String spreadsheetId, final NetHttpTransport HTTP_TRANSPORT) throws IOException{
                //Range for speakers: A2:G
                //Range for teams: A2:F
                final String range = "Raw Speaker Data: A2:G";
                Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
                ValueRange response = service.spreadsheets().values()
                        .get(spreadsheetId, range).execute();

                List<List<Object>> values = response.getValues();

                for (int i = 0; i < 4; i++) {
                    for (List row : values) {
                        if (i == 1) {
                            if (row.equals(govTeam.speakerNames[0].toLowerCase())) {
                                //UpdateValuesResponse result =
                                // service.spreadsheets().values().update(spreadsheetId, range)
                                //     .setValueInputOption(valueInputOption)
                                //     .execute();
                            } else if (row.equals(govTeam.speakerNames[1].toLowerCase())) {

                            } else if (row.equals(oppTeam.speakerNames[0].toLowerCase())) {

                            } else if (row.equals(oppTeam.speakerNames[1].toLowerCase())) {

                            }
                        }
                    }
                }

                for (int j = 0; j < 2; j++) {
                    for (List row : values) {
                        if (j == 1) {
                            if (row.equals(govTeam.teamName.toLowerCase())) {

                            } else if (row.equals(oppTeam.teamName.toLowerCase())) {

                            }
                        }
                    }
                }

            }

        });
    }
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws
            IOException {
        // Load client secrets.
        InputStream in = AdjunicatorActivity.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

}

