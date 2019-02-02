package com.example.david.debatequicktabs;


import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.SheetsScopes;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.content.Intent;
import android.widget.EditText;
import android.util.Log;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Collections;


public class AdjunicatorActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjudicator);

        Button doneBtn = (Button) findViewById(R.id.doneBtn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Takes input from activity
                EditText urlInput = (EditText) findViewById(R.id.spreadsheetURL);
                EditText roundNumInfo = (EditText) findViewById(R.id.roundNum);
                EditText roundRoomLocation = (EditText) findViewById(R.id.roomLocation);

                EditText teamNameInput = (EditText) findViewById(R.id.govTeamName);
                EditText firstSpeakerInput = (EditText) findViewById(R.id.firstGovSpeaker);
                EditText firstPointInput = (EditText) findViewById(R.id.firstGovPoints);
                EditText secondSpeakerInput = (EditText) findViewById(R.id.secondGovSpeaker);
                EditText secondPointInput = (EditText) findViewById(R.id.secondGovPoints);

                //Stores input into object Team(starts with storing gov team info)
                final String sheetURL = urlInput.getText().toString();
                String roundNum = roundNumInfo.getText().toString();
                String roomLocation = roundRoomLocation.toString();

                RoundInfo roundInfo = new RoundInfo(roundNum, roomLocation);

                String teamName = teamNameInput.getText().toString();
                String speaker[] = new String[2];
                speaker[0] = firstSpeakerInput.getText().toString();
                speaker[1] = secondSpeakerInput.getText().toString();

                String speakerPoints[] = new String[2];
                speakerPoints[0] = firstPointInput.getText().toString();
                speakerPoints[1] = secondPointInput.getText().toString();
                int totalPoints = Integer.parseInt(speakerPoints[0]) + Integer.parseInt(speakerPoints[1]);

                final Team govTeam = new Team(teamName, speaker, speakerPoints, Integer.toString(totalPoints));

                //Takes input from activity and stores it into object Team (now it stores info for opp team)
                teamNameInput = (EditText) findViewById(R.id.oppTeamName);
                firstSpeakerInput = (EditText) findViewById(R.id.firstOppSpeaker);
                firstPointInput = (EditText) findViewById(R.id.firstOppPoints);
                secondSpeakerInput = (EditText) findViewById(R.id.secondOppSpeaker);
                secondPointInput = (EditText) findViewById(R.id.secondOppPoints);

                teamName = teamNameInput.getText().toString();
                speaker[0] = firstSpeakerInput.getText().toString();
                speakerPoints[0] = firstPointInput.getText().toString();
                speaker[1] = secondSpeakerInput.getText().toString();
                speakerPoints[1] = secondPointInput.getText().toString();
                totalPoints = Integer.parseInt(speakerPoints[0]) + Integer.parseInt(speakerPoints[1]);

                final Team oppTeam = new Team(teamName, speaker, speakerPoints, Integer.toString(totalPoints));

                if (Integer.parseInt(govTeam.teamScore) > Integer.parseInt(oppTeam.teamScore)) {
                    govTeam.win = "1"; //1 means team won, 0 means team lost
                    oppTeam.win = "0";
                } else { //Debates should never end in a tie, thus speaker scores show never be equal
                    govTeam.win = "0";
                    oppTeam.win = "1";
                }

                try {
                    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                    final String spreadsheetId = getID(sheetURL);
                    SendToSheets debateData = new SendToSheets(govTeam, oppTeam, roundInfo, spreadsheetId, HTTP_TRANSPORT);
                    SendToSheets.postData(debateData);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    } public static String getID(String url) {
        String id;
        int i = 39;
        boolean found = false;
        while (!found) {
            if (url.charAt(i) == '/') {
                found = true;
            }
            i++;
        }
        id = url.substring(39,i);
        return id;
    }
}

