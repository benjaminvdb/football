package nl.liacs.sports.football.parser.psv.positional.models;

import java.time.LocalDate;
import java.util.List;

public class PositionalDataset {
    private List<PositionalRecord> records;
    private String teamHome;
    private String teamAway;

    private LocalDate date;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public PositionalDataset(List<PositionalRecord> records, String teamHome, String teamAway) {
        this.records = records;
        this.teamHome = teamHome;
        this.teamAway = teamAway;
    }

    public List<PositionalRecord> getRecords() {
        return records;
    }

    public String getTeamHome() {
        return teamHome;
    }

    public String getTeamAway() {
        return teamAway;
    }
}
