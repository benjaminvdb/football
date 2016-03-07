//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nl.liacs.sports.football.parser.bundesliga.positional.models;

import java.util.List;

public class Record {
    private Frame frame;
    private List<Player> team_home;
    private List<Player> team_away;
    private List<Referee> referees;
    private Ball ball;
    private AdditionalInfo additional_info;

    public Record() {
    }

    public Frame getFrame() {
        return this.frame;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
    }

    public List<Player> getTeamHome() {
        return this.team_home;
    }

    public void setTeamHome(List<Player> team_home) {
        this.team_home = team_home;
    }

    public List<Player> getTeamAway() {
        return this.team_away;
    }

    public void setTeamAway(List<Player> team_away) {
        this.team_away = team_away;
    }

    public List<Referee> getReferees() {
        return this.referees;
    }

    public void setReferees(List<Referee> referees) {
        this.referees = referees;
    }

    public Ball getBall() {
        return this.ball;
    }

    public void setBall(Ball ball) {
        this.ball = ball;
    }

    public AdditionalInfo getAdditionalInfo() {
        return this.additional_info;
    }

    public void setAdditionalInfo(AdditionalInfo additional_info) {
        this.additional_info = additional_info;
    }
}
