package nl.liacs.sports.football.parser.sql;

import com.google.common.collect.Iterables;

import nl.liacs.sports.football.parser.positional.models.Ball;
import nl.liacs.sports.football.parser.positional.models.Frame;
import nl.liacs.sports.football.parser.positional.models.Player;
import nl.liacs.sports.football.parser.positional.models.Record;
import nl.liacs.sports.football.parser.positional.models.Referee;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DBOutputWriter {

    public static void write(Connection con, List<Record> records, String teamHome, String teamAway) throws ClassNotFoundException, SQLException {
        /* Empty tables. */
        con.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
        con.createStatement().executeUpdate("TRUNCATE teams");
        con.createStatement().executeUpdate("TRUNCATE matches");
        con.createStatement().executeUpdate("TRUNCATE players");
        con.createStatement().executeUpdate("TRUNCATE referees");
        con.createStatement().executeUpdate("TRUNCATE matches_referees_maps");
        con.createStatement().executeUpdate("TRUNCATE frames");
        con.createStatement().executeUpdate("TRUNCATE referee_measurement");
        con.createStatement().executeUpdate("TRUNCATE ball_measurements");
        con.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS = 1");

        int home_team_id;
        try (NamedParameterStatement stmt = new NamedParameterStatement(con, "INSERT INTO teams(name) VALUES(:name)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString("name", teamHome);
            stmt.execute();
            home_team_id = stmt.getGeneratedKey();
        }

        int away_team_id;
        try (NamedParameterStatement stmt = new NamedParameterStatement(con, "INSERT INTO teams(name) VALUES(:name)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString("name", teamAway);
            stmt.execute();
            away_team_id = stmt.getGeneratedKey();
        }

        int match_id;
        try (NamedParameterStatement stmt = new NamedParameterStatement(con, "INSERT INTO matches(home_team_id, away_team_id, pitch_size_x, pitch_size_y) " +
                "VALUES(:home_team_id, :away_team_id, :pitch_size_x, :pitch_size_y)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt("home_team_id", home_team_id);
            stmt.setInt("away_team_id", away_team_id);
            stmt.setFloat("pitch_size_x", records.get(0).getAdditionalInfo().getPitchSize().get().getXDistance());
            stmt.setFloat("pitch_size_y", records.get(0).getAdditionalInfo().getPitchSize().get().getYDistance());
            stmt.execute();
            match_id = stmt.getGeneratedKey();
        }

        /* Insert players with unique names (just alphabetical letters right now). */
        for (int i = 0; i < 11; i++) {
            try (NamedParameterStatement stmt = new NamedParameterStatement(con, "INSERT INTO players(name) VALUES(:name)")) {
                stmt.setString("name", Character.toString((char) (65 + i)));
                stmt.execute();
            }
        }

        /* Insert players with unique names (just alphabetical letters right now). */
        for (int i = 11; i < 22; i++) {
            try (NamedParameterStatement stmt = new NamedParameterStatement(con, "INSERT INTO players(name) VALUES(:name)")) {
                stmt.setString("name", Character.toString((char) (65 + i)));
                stmt.execute();
            }
        }

        String[] types = {"main", "line", "line"};
        for (String type : types) {
            int referee_id;
            try (NamedParameterStatement stmt = new NamedParameterStatement(con, "INSERT INTO referees(type) VALUES(:type)", Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString("type", type);
                stmt.execute();
                referee_id = stmt.getGeneratedKey();
            }

            try (NamedParameterStatement stmt = new NamedParameterStatement(con, "INSERT INTO matches_referees_maps(match_id, referee_id) VALUES(:match_id, :referee_id)")) {
                stmt.setInt("match_id", match_id);
                stmt.setInt("referee_id", referee_id);
                stmt.execute();
            }
        }

        for (Record record : records) {
            Frame frame = record.getFrame();
            int frame_id;
            try (NamedParameterStatement stmt = new NamedParameterStatement(con, "INSERT INTO frames(frame_number, minute, section, match_id) VALUES(:frame_number, :minute, :section, :match_id)", Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt("frame_number", frame.getFrameNumber());
                stmt.setInt("minute", frame.getMinute());
                stmt.setInt("section", frame.getSection());
                stmt.setInt("match_id", match_id);
                stmt.execute();
                frame_id = stmt.getGeneratedKey();
            }

            assert (record.getReferees().size() == 3);

            int internal_id = 22;
            for (Referee referee : record.getReferees()) {
                try (NamedParameterStatement stmt = new NamedParameterStatement(con, "INSERT INTO referee_measurement(internal_id, x, y, speed, referee_id, frame_id) VALUES(:internal_id, :x, :y, :speed, :referee_id, :frame_id)", Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt("internal_id", internal_id);
                    stmt.setFloat("x", referee.getX());
                    stmt.setFloat("y", referee.getY());
                    stmt.setFloat("speed", referee.getSpeed());
                    stmt.setInt("referee_id", internal_id - 21);  // FIXME: cannot last for long..
                    stmt.setInt("frame_id", frame_id);
                    stmt.execute();
                    internal_id++;
                }
            }

            try (NamedParameterStatement stmt = new NamedParameterStatement(con, "INSERT INTO ball_measurements(x, y, z, flag, possession, frame_id) VALUES(:x, :y, :z, :flag, :possession, :frame_id)", Statement.RETURN_GENERATED_KEYS)) {
                Ball ball = record.getBall();
                stmt.setFloat("x", ball.getX());
                stmt.setFloat("y", ball.getY());
                stmt.setFloat("z", ball.getZ());
                stmt.setInt("flag", (ball.getFlag()) ? 1 : 0);
                stmt.setInt("possession", ball.getPossession());
                stmt.setInt("frame_id", frame_id);
                stmt.execute();
            }

            internal_id = 0;  // NOTE: these ID's are used in the original data and documentation to refer to entities (players and referees)

            for (Player player : Iterables.concat(record.getTeamHome(), record.getTeamAway())) {
                try (NamedParameterStatement stmt = new NamedParameterStatement(con, "INSERT INTO player_measurements(internal_id, x, y, speed, player_id, frame_id) VALUES(:internal_id, :x, :y, :speed, :player_id, :frame_id)", Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt("internal_id", internal_id);
                    stmt.setFloat("x", player.getX());
                    stmt.setFloat("y", player.getY());
                    stmt.setFloat("speed", player.getSpeed());
                    stmt.setInt("player_id", internal_id + 1);  // FIXME: this might break if more matches are added, since then we cannot easily compute the id this way
                    stmt.setInt("frame_id", frame_id);
                    stmt.execute();
                    internal_id++;
                }
            }
        }
    }
}
