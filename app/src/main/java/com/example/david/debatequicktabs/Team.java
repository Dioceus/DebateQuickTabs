package com.example.david.debatequicktabs;

public class Team {

    String teamName;
    String[] speakerNames;
    int[] speakerScores;
    int teamScore;
    boolean win;

    public Team(String teamName, String[] speakerNames, int[] speakerScores, int teamScore) {
        this.teamName = teamName;
        this.speakerNames = speakerNames;
        this.speakerScores = speakerScores;
        this.teamScore = teamScore;
        //this.win = win;
    }
}
