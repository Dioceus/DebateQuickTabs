package com.example.david.debatequicktabs;

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

import java.util.List;
import java.util.Collections;


public class AdjunicatorActivity extends Activity{

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjunicator);

        Button doneBtn = (Button) findViewById(R.id.doneBtn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Takes input from activity
                EditText urlInput = (EditText) findViewById(R.id.spreadsheetURL);
                EditText roundInfo = (EditText) findViewById(R.id.roundNum);

                EditText teamNameInput = (EditText) findViewById(R.id.govTeamName);
                EditText firstSpeakerInput = (EditText) findViewById(R.id.firstGovSpeaker);
                EditText firstPointInput = (EditText) findViewById(R.id.firstGovPoints);
                EditText secondSpeakerInput = (EditText) findViewById(R.id.secondGovSpeaker);
                EditText secondPointInput = (EditText) findViewById(R.id.secondGovPoints);
                EditText totalTeamPoints = (EditText) findViewById(R.id.govTotalPoints);

                //Stores input into object Team(starts with storing gov team info)
                final String sheetURL = urlInput.getText().toString();
                //int roundNum = Integer.parseInt(roundInfo.getText().toString());
                String teamName = teamNameInput.getText().toString();
                String speaker[] = new String[2];
                speaker[0] = firstSpeakerInput.getText().toString();
                speaker[1] = secondSpeakerInput.getText().toString();

                String speakerPoints[] = new String[2];
                speakerPoints[0] = firstPointInput.getText().toString();
                speakerPoints[1] = secondPointInput.getText().toString();
                int totalPoints = Integer.parseInt(totalTeamPoints.getText().toString());
                //int totalPoints = speakerPoints[0] + speakerPoints[1];

                final Team govTeam = new Team(teamName, speaker, speakerPoints, totalPoints);

                //Takes input from activity and stores it into object Team (now it stores info for opp team)
                teamNameInput = (EditText) findViewById(R.id.oppTeamName);
                firstSpeakerInput = (EditText) findViewById(R.id.firstOppSpeaker);
                firstPointInput = (EditText) findViewById(R.id.firstOppPoints);
                secondSpeakerInput = (EditText) findViewById(R.id.secondOppSpeaker);
                secondPointInput = (EditText) findViewById(R.id.secondOppPoints);
                totalTeamPoints = (EditText) findViewById(R.id.oppTotalPoints);

                teamName = teamNameInput.getText().toString();
                speaker[0] = firstSpeakerInput.getText().toString();
                speakerPoints[0] = firstPointInput.getText().toString();
                speaker[1] = secondSpeakerInput.getText().toString();
                speakerPoints[1] = secondPointInput.getText().toString();
                totalPoints = Integer.parseInt(totalTeamPoints.getText().toString());
                //totalPoints = speakerPoints[0] + speakerPoints[1];

                final Team oppTeam = new Team(teamName, speaker, speakerPoints, totalPoints);

                if (govTeam.teamScore > oppTeam.teamScore) {
                    govTeam.win = "1"; //1 means team won, 0 means team lost
                    oppTeam.win = "0";
                } else { //Debates should never end in a tie, thus speaker scores show never be equal
                    govTeam.win = "0";
                    oppTeam.win = "1";
                }

                final NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
                final String spreadsheetId = sheetURL.substring(38, 82);

                SendToSheets debateData = new SendToSheets(govTeam, oppTeam, HTTP_TRANSPORT, spreadsheetId);
                debateData.postData(govTeam, oppTeam, HTTP_TRANSPORT, spreadsheetId);
            }
        });
    }
}

