package nl.liacs.sports.football.parser.positional;

import nl.liacs.sports.football.parser.positional.models.Record;

import java.util.List;

public class PositionDataset {
    public List<Record> getRecords() {
        return records;
    }

    public String getTeamHome() {
        return teamHome;
    }

    public String getTeamAway() {
        return teamAway;
    }

    private List<Record> records;
    private String teamHome;
    private String teamAway;

    public PositionDataset(List<Record> records, String teamHome, String teamAway) {
        this.records = records;
        this.teamHome = teamHome;
        this.teamAway = teamAway;
    }
}
