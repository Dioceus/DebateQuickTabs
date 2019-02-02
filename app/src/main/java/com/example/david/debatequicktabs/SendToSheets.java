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
    RoundInfo roundInfo;
    final String spreadsheetID;
    final NetHttpTransport HTTP_TRANSPORT;

    public SendToSheets(Team govTeam, Team oppTeam, RoundInfo roundInfo, String spreadsheetID, NetHttpTransport HTTP_TRANSPORT) {

        this.govTeam = govTeam;
        this.oppTeam = oppTeam;
        this.roundInfo = roundInfo;
        this.spreadsheetID = spreadsheetID;
        this.HTTP_TRANSPORT = HTTP_TRANSPORT;


    } public static void postData(SendToSheets data) {

        String range = "A2:N";

        try {

            Sheets service = new Sheets.Builder(data.HTTP_TRANSPORT, JSON_FACTORY, getCredentials(data.HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            ValueRange response = service.spreadsheets().values()
                    .get(data.spreadsheetID, range).execute();

            List<List<Object>> values = response.getValues();

            for (List row : values) {

                sendData(data.spreadsheetID, service, range, data.roundInfo.location);
                sendData(data.spreadsheetID, service, range, data.roundInfo.roundNum);

                sendData(data.spreadsheetID, service, range, data.govTeam.teamName);
                sendData(data.spreadsheetID, service, range, data.govTeam.win);
                sendData(data.spreadsheetID, service, range, data.govTeam.speakerNames[0]);
                sendData(data.spreadsheetID, service, range, data.govTeam.speakerScores[0]);
                sendData(data.spreadsheetID, service, range, data.govTeam.speakerNames[1]);
                sendData(data.spreadsheetID, service, range, data.govTeam.speakerScores[1]);

                sendData(data.spreadsheetID, service, range, data.oppTeam.teamName);
                sendData(data.spreadsheetID, service, range, data.oppTeam.win);
                sendData(data.spreadsheetID, service, range, data.oppTeam.speakerNames[0]);
                sendData(data.spreadsheetID, service, range, data.oppTeam.speakerScores[0]);
                sendData(data.spreadsheetID, service, range, data.oppTeam.speakerNames[1]);
                sendData(data.spreadsheetID, service, range, data.oppTeam.speakerScores[1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    } public static void sendData(final String spreadsheetId, Sheets service, final String range, String information) throws IOException{

        List<List<Object>> insertValues  = getData(information);

        ValueRange body = new ValueRange()
                .setValues(insertValues);
        UpdateValuesResponse result =
                service.spreadsheets().values().update(spreadsheetId,  range, body)
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
