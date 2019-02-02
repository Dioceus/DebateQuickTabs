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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.InputStreamReader;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.util.Collections;
import java.io.InputStream;

public class SendToSheets {

    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

    private static final String APPLICATION_NAME = "Debate Mobile Adjudication";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    //static final NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();

    Team govTeam;
    Team oppTeam;
    final String spreadsheetID;
    final NetHttpTransport HTTP_TRANSPORT;

    public SendToSheets(Team govTeam, Team oppTeam, String spreadsheetID, NetHttpTransport HTTP_TRANSPORT) {

        this.govTeam = govTeam;
        this.oppTeam = oppTeam;
        this.spreadsheetID = spreadsheetID;
        this.HTTP_TRANSPORT = HTTP_TRANSPORT;


    } public static void postData(SendToSheets data) {
        //Range for speakers: A2:G
        //Range for teams: A2:F
        final String[] range = new String[2];
        range[0] = "Raw Speaker Data: A2:G";
        range[1] = "Raw Team Data: A2:F";

        try {
            //GeneralSecurityExceptions don't get caught by try catch blocks, only when they are thrown by the caller
            Sheets service = new Sheets.Builder(data.HTTP_TRANSPORT, JSON_FACTORY, getCredentials(data.HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            ValueRange response = service.spreadsheets().values()
                    .get(data.spreadsheetID, range[0]).execute();

            List<List<Object>> values = response.getValues();
            //Haven't sent teamScore info yet
            for (int i = 0; i < 4; i++) { //Counts to store information for the round
                for (List row : values) {
                    sendData(data.spreadsheetID, service, range, data.govTeam.speakerScores[0]);
                    sendData(data.spreadsheetID, service, range, data.govTeam.speakerScores[1]);
                    sendData(data.spreadsheetID, service, range, data.govTeam.speakerScores[0]);
                    sendData(data.spreadsheetID, service, range, data.govTeam.speakerScores[1]);
                    /*

                    if (row.equals(govTeam.speakerNames[0].toLowerCase())) { //Enter PM Speaker Score
                        sendData(spreadsheetId, service, range, govTeam.speakerScores[0]);

                    } else if (row.equals(govTeam.speakerNames[1].toLowerCase())) { //Enter DPM Speaker Score
                        sendData(spreadsheetId, service, range, govTeam.speakerScores[1]);

                    } else if (row.equals(oppTeam.speakerNames[0].toLowerCase())) { //Enter First Opposition Speaker Score
                        sendData(spreadsheetId, service, range, oppTeam.speakerScores[0]);

                    } else if (row.equals(oppTeam.speakerNames[1].toLowerCase())) { //Enter Second opposition Speaker Score
                        sendData(spreadsheetId, service, range, oppTeam.speakerScores[1]);
                    }*/

                }
            }
            /*
            for (int j = 0; j < 2; j++) {
                for (List row : values) {
                    if (row.equals(govTeam.teamName.toLowerCase())) {
                        sendData(spreadsheetId, service, range, govTeam.win);
                    } else if (row.equals(oppTeam.teamName.toLowerCase())) {
                        sendData(spreadsheetId, service, range, oppTeam.win);
                    }
                }
            }*/

        } catch (IOException e) {
            e.printStackTrace();
        }

    } public static void sendData(final String spreadsheetId, Sheets service, final String[] range, String information) throws IOException{

        List<List<Object>> insertValues  = getData(information);

        ValueRange body = new ValueRange()
                .setValues(insertValues);
        UpdateValuesResponse result =
                service.spreadsheets().values().update(spreadsheetId,  range[0], body)
                        .setValueInputOption("RAW")
                        .execute();

    } public static List<List<Object>> getData (String dataInsert)  {

        List<Object> data1 = new ArrayList<Object>();
        data1.add (dataInsert);

        List<List<Object>> data = new ArrayList<List<Object>>();
        data.add (data1);

        return data;

    } private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws
            IOException {
        // Load client secrets.

        InputStream in = SendToSheets.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));


        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

    }

}
