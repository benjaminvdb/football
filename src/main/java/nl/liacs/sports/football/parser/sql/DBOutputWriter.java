package nl.liacs.sports.football.parser.sql;

import com.google.common.collect.Iterables;

import nl.liacs.sports.football.parser.bundesliga.positional.models.Ball;
import nl.liacs.sports.football.parser.bundesliga.positional.models.Frame;
import nl.liacs.sports.football.parser.bundesliga.positional.models.Player;
import nl.liacs.sports.football.parser.bundesliga.positional.models.Record;
import nl.liacs.sports.football.parser.bundesliga.positional.models.Referee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ListIterator;

public class DBOutputWriter {
    private static final int DEFAULT_BATCH_SIZE = 10000;

    private static final Logger log = LoggerFactory.getLogger(DBOutputWriter.class);

    public static void write(Connection con, List<Record> records, String teamHome, String teamAway, int chunkSize) throws ClassNotFoundException, SQLException {
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

        int totalRecords = 0;
        while (totalRecords < records.size()) {

            NamedParameterStatement stmt = new NamedParameterStatement(con, "INSERT INTO frames(frame_number, minute, section, match_id) VALUES(:frame_number, :minute, :section, :match_id)", Statement.RETURN_GENERATED_KEYS);
            NamedParameterStatement stmt_ref = new NamedParameterStatement(con, "INSERT INTO referee_measurement(internal_id, x, y, speed, referee_id, frame_id) VALUES(:internal_id, :x, :y, :speed, :referee_id, :frame_id)", Statement.RETURN_GENERATED_KEYS);
            NamedParameterStatement stmt_ball = new NamedParameterStatement(con, "INSERT INTO ball_measurements(x, y, z, flag, possession, frame_id) VALUES(:x, :y, :z, :flag, :possession, :frame_id)", Statement.RETURN_GENERATED_KEYS);
            NamedParameterStatement stmt_player = new NamedParameterStatement(con, "INSERT INTO player_measurements(internal_id, x, y, speed, player_id, frame_id) VALUES(:internal_id, :x, :y, :speed, :player_id, :frame_id)", Statement.RETURN_GENERATED_KEYS);

            ListIterator<Record> it = records.listIterator(totalRecords);
            for (int currentBatchSize = 0; currentBatchSize < chunkSize && it.hasNext(); currentBatchSize++) {
                Record record = it.next();
                Frame frame = record.getFrame();
                stmt.setInt("frame_number", frame.getFrameNumber());
                stmt.setInt("minute", frame.getMinute());
                stmt.setInt("section", frame.getSection());
                stmt.setInt("match_id", match_id);
                stmt.addBatch();
            }
            stmt.executeBatch();
            List<Integer> frameIds = stmt.getGeneratedIntegerKeys();
            stmt.close();

            it = records.listIterator(totalRecords);
            for (int frame_id : frameIds) {
                Record record = it.next();
                int internal_id = 0;
                for (Player player : Iterables.concat(record.getTeamHome(), record.getTeamAway())) {
                    stmt_player.setInt("internal_id", internal_id);
                    stmt_player.setFloat("x", player.getX());
                    stmt_player.setFloat("y", player.getY());
                    stmt_player.setFloat("speed", player.getSpeed());
                    stmt_player.setInt("player_id", internal_id + 1);  // FIXME: this might break if more matches are added, since then we cannot easily compute the id this way
                    stmt_player.setInt("frame_id", frame_id);
                    stmt_player.execute();
                    internal_id++;
                }
            }
            stmt_player.executeBatch();
            stmt_player.close();

            it = records.listIterator(totalRecords);
            for (int frame_id : frameIds) {
                Record record = it.next();
                int internal_id = 22;
                for (Referee referee : record.getReferees()) {
                    stmt_ref.setInt("internal_id", internal_id);
                    stmt_ref.setFloat("x", referee.getX());
                    stmt_ref.setFloat("y", referee.getY());
                    stmt_ref.setFloat("speed", referee.getSpeed());
                    stmt_ref.setInt("referee_id", internal_id - 21);  // FIXME: cannot last for long..
                    stmt_ref.setInt("frame_id", frame_id);
                    stmt_ref.addBatch();
                    internal_id++;
                }
            }
            stmt_ref.executeBatch();
            stmt_ref.close();

            it = records.listIterator(totalRecords);
            for (int frame_id : frameIds) {
                Record record = it.next();
                Ball ball = record.getBall();
                stmt_ball.setFloat("x", ball.getX());
                stmt_ball.setFloat("y", ball.getY());
                stmt_ball.setFloat("z", ball.getZ());
                stmt_ball.setInt("flag", (ball.getFlag()) ? 1 : 0);
                stmt_ball.setInt("possession", ball.getPossession());
                stmt_ball.setInt("frame_id", frame_id);
                stmt_ball.addBatch();
            }
            stmt_ball.executeBatch();
            stmt_ball.close();

            totalRecords += frameIds.size();

            log.info("currently at {} records", totalRecords);
        }
    }
}
