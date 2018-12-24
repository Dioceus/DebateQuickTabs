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

import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.content.Intent;
import android.widget.EditText;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.InputStreamReader;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;

import org.mortbay.jetty.HttpOnlyCookie;

import java.util.Collections;
import java.io.InputStream;


public class AdjunicatorActivity extends Activity{

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

    private static final String APPLICATION_NAME = "Debate Mobile Adjundication";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";


    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjunicator);

        //Takes input from activity
        EditText urlInput = (EditText) findViewById(R.id.spreadsheetURL);
        EditText teamNameInput = (EditText) findViewById(R.id.govTeamName);
        EditText firstSpeakerInput = (EditText) findViewById(R.id.firstGovSpeaker);
        EditText firstPointInput = (EditText) findViewById(R.id.firstGovPoints);
        EditText secondSpeakerInput = (EditText) findViewById(R.id.secondGovSpeaker);
        EditText secondPointInput = (EditText) findViewById(R.id.secondGovPoints);
        EditText roundInfo = (EditText) findViewById(R.id.roundNum);

        //Stores input into object Team(starts with storing gov team info)
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

        //Takes input from activity and stores it into object Team (now it stores info for opp team)
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

        final NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
        final String spreadsheetId = sheetURL.substring(38, 82);
        /*
        Button doneBtn = (Button) findViewById(R.id.doneBtn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){

                //AdjunicatorActivityException sendData = new AdjunicatorActivityException(govTeam, oppTeam, HTTP_TRANSPORT, spreadsheetId);
                //postData(sendData);
                Intent intent = new Intent(getApplicationContext(),AddItem.class);
                startActivity(intent);


    } */
        Button doneBtn = (Button) findViewById(R.id.doneBtn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
                postData(govTeam, oppTeam, HTTP_TRANSPORT, spreadsheetId);
            }
        });


    } public void postData(Team govTeam, Team oppTeam, NetHttpTransport HTTP_TRANSPORT, String spreadsheetId){
                //Range for speakers: A2:G
                //Range for teams: A2:F
                final String[] range = new String[2];
                range[0] = "Raw Speaker Data: A2:G";
                range[1] = "Raw Team Data: A2:F";

        try {

            Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range[0]).execute();

            List<List<Object>> values = response.getValues();

            for (int i = 0; i < 4; i++) { //Counts to store information for the round
                for (List row : values) {
                    //  if (i == 1) {
                    if (row.equals(govTeam.speakerNames[0].toLowerCase())) { //Enter PM Speaker Score
                        sendData(spreadsheetId, service, range, govTeam.speakerScores[0]);

                    } else if (row.equals(govTeam.speakerNames[1].toLowerCase())) { //Enter DPM Speaker Score
                        sendData(spreadsheetId, service, range, govTeam.speakerScores[1]);

                    } else if (row.equals(oppTeam.speakerNames[0].toLowerCase())) { //Enter First Opposition Speaker Score
                        sendData(spreadsheetId, service, range, oppTeam.speakerScores[0]);

                    } else if (row.equals(oppTeam.speakerNames[1].toLowerCase())) { //Enter Second opposition Speaker Score
                        sendData(spreadsheetId, service, range, oppTeam.speakerScores[1]);
                    }
                    // }
                }
            }

            for (int j = 0; j < 2; j++) {
                for (List row : values) {
                    // if (j == 1) {
                    if (row.equals(govTeam.teamName.toLowerCase())) {
                        sendData(spreadsheetId, service, range, govTeam.win);
                    } else if (row.equals(oppTeam.teamName.toLowerCase())) {
                        sendData(spreadsheetId, service, range, oppTeam.win);
                    }
                    //  }
                }
            }



        } catch (IOException e) {
            e.printStackTrace();
        }

    } public static List<List<Object>> getData (String dataInsert)  {

        List<Object> data1 = new ArrayList<Object>();
        data1.add (dataInsert);

        List<List<Object>> data = new ArrayList<List<Object>>();
        data.add (data1);

        return data;

    } public void sendData(final String spreadsheetId, Sheets service, final String[] range, int information) throws IOException{

        List<List<Object>> insertValues  = getData(String.valueOf(information));

        ValueRange body = new ValueRange()
                .setValues(insertValues);
        UpdateValuesResponse result =
                service.spreadsheets().values().update(spreadsheetId,  range[0], body)
                        .setValueInputOption("RAW")
                        .execute();

    } private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws
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

