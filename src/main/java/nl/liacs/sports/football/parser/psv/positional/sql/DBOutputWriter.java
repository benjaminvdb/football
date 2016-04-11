package nl.liacs.sports.football.parser.psv.positional.sql;

import nl.liacs.sports.football.parser.psv.positional.models.PositionalDataset;
import nl.liacs.sports.football.parser.psv.positional.models.PositionalRecord;
import nl.liacs.sports.football.parser.sql.NamedParameterStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;

public class DBOutputWriter {
    private static final int DEFAULT_BATCH_SIZE = 10000;

    private static final Logger log = LoggerFactory.getLogger(DBOutputWriter.class);

    private final Connection con;

    public DBOutputWriter(Connection con) {
        this.con = con;
    }

    /**
     * Returns ID of record in ResultSet if it is unique and -1 otherwise.
     * @param rs
     * @return
     * @throws SQLException
     */
    private int getUniqueId(ResultSet rs) throws SQLException {
        int nRecords = 0;
        int id = -1;
        while (rs.next()) {
            nRecords++;

            if (nRecords > 1) {
                throw new SQLException();
            }

            id = rs.getInt(1);
        }
        return id;
    }

    /**
     * Fetch the ID of a team with certain name.
     * @param teamName
     * @return
     * @throws SQLException
     */
    private int getTeamId(String teamName) throws SQLException {
        try (NamedParameterStatement stmt = new NamedParameterStatement(con, "SELECT team_id FROM teams WHERE name = :name", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString("name", teamName);
            ResultSet rs = stmt.executeQuery();
            try {
                return getUniqueId(rs);
            } catch (SQLException se) {
                throw new SQLException("team ids should be unique, but found more than one match");
            }
        }
    }

    /**
     * Add a team to the database with the given name. Skip it if it already exists.
     * @param teamName
     * @return
     * @throws SQLException
     */
    private int addTeam(String teamName) throws SQLException {
        int team_id = getTeamId(teamName);
        if (getTeamId(teamName) == -1) {
            log.info("team {} not yet in database, adding it...", teamName);
            try (NamedParameterStatement stmt = new NamedParameterStatement(con, "INSERT INTO teams(name) VALUES(:name)", Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString("name", teamName);
                stmt.execute();
                team_id = stmt.getGeneratedKey();
            }
            log.info("added team {} with id {}", teamName, team_id);
        } else {
            log.info("team {} already in database with id {}, skipping it.", teamName, team_id);
        }
        return team_id;
    }

    /**
     * Get the match ID of record identified by the given arguments.
     * @param homeTeamId
     * @param awayTeamId
     * @param date
     * @return
     * @throws SQLException
     */
    private int getMatchId(int homeTeamId, int awayTeamId, LocalDate date) throws SQLException {
        try (NamedParameterStatement stmt = new NamedParameterStatement(con, "SELECT match_id FROM matches WHERE home_team_id = :home_team_id AND away_team_id = :away_team_id AND date = :date", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt("home_team_id", homeTeamId);
            stmt.setInt("away_team_id", awayTeamId);
            stmt.setTimestamp("date", Timestamp.valueOf(date.atStartOfDay()));

            ResultSet rs = stmt.executeQuery();
            try {
                return getUniqueId(rs);
            } catch (SQLException se) {
                throw new SQLException("team ids should be unique, but found more than one match");
            }
        }
    }

    /**
     * Add the match to the database if it doesn't exist yet.
     * @param homeTeamId
     * @param awayTeamId
     * @param date
     * @return
     * @throws SQLException
     */
    private int addMatch(int homeTeamId, int awayTeamId, LocalDate date) throws SQLException {
        int match_id = getMatchId(homeTeamId, awayTeamId, date);
        if (match_id == -1) {
            log.info("match [{} vs. {} on {}] not yet in database, adding it...", homeTeamId, homeTeamId, date);
            try (NamedParameterStatement stmt = new NamedParameterStatement(con, "INSERT INTO matches(home_team_id, away_team_id, date) " +
                    "VALUES(:home_team_id, :away_team_id, :date)", Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt("home_team_id", homeTeamId);
                stmt.setInt("away_team_id", awayTeamId);
                stmt.setTimestamp("date", Timestamp.valueOf(date.atStartOfDay()));
                stmt.execute();
                match_id = stmt.getGeneratedKey();
            }
            log.info("added match with id {}", match_id);
        } else {
            log.info("match [{} vs. {} on {}] already in database, skipping it...", homeTeamId, awayTeamId, date);
        }
        return match_id;
    }

    private int getPlayerId(String name) throws SQLException {
        try (NamedParameterStatement stmt = new NamedParameterStatement(con, "SELECT player_id FROM players WHERE name = :name", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString("name", name);

            ResultSet rs = stmt.executeQuery();
            try {
                return getUniqueId(rs);
            } catch (SQLException se) {
                throw new SQLException("player ids should be unique, but found more than one player");
            }
        }
    }

    private int addPlayer(String name) throws SQLException {
        int player_id = getPlayerId(name);
        if (player_id == -1) {
            log.info("player {} not yet in database, adding it...", name);
            try (NamedParameterStatement stmt = new NamedParameterStatement(con, "INSERT INTO players(name) " +
                    "VALUES(:name)", Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString("name", name);
                stmt.execute();
                player_id = stmt.getGeneratedKey();
            }
            log.info("added player with id {}", player_id);
        } else {
            log.info("player {} already in database, skipping it...", name);
        }
        return player_id;
    }

    private int addFrame(int frame_number, int match_id, int millisecond) throws SQLException {
        log.trace("adding frame ({}, {}, {})", frame_number, match_id, millisecond);
        try (NamedParameterStatement stmt = new NamedParameterStatement(con, "INSERT INTO " +
                "frames(frame_number, match_id, millisecond) " +
                "VALUES(:frame_number, :match_id, :millisecond)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt("frame_number", frame_number);
            stmt.setInt("match_id", match_id);
            stmt.setInt("millisecond", millisecond);
            stmt.execute();
            int frame_id = stmt.getGeneratedKey();
            log.trace("added frame with id {}", frame_id);
            return frame_id;
        }
    }

    private int addPlayerMeasurement(float x, float y, float speed, int frame_id, int player_id) throws SQLException {
        log.trace("adding player measurement ({}, {}, {}, {}, {})", x, y, speed, frame_id, player_id);
        try (NamedParameterStatement stmt = new NamedParameterStatement(con, "INSERT INTO " +
                "player_measurements(x, y, speed, frame_id, player_id) " +
                "VALUES(:x, :y, :speed, :frame_id, :player_id)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setFloat("x", x);
            stmt.setFloat("y", y);
            stmt.setFloat("speed", speed);
            stmt.setInt("frame_id", frame_id);
            stmt.setInt("player_id", player_id);
            stmt.execute();
            int player_measurement_id = stmt.getGeneratedKey();  // NOTE: perhaps not needed?
            log.trace("added player_measurement with {}", player_measurement_id);
            return player_measurement_id;
        }
    }

    private int addBallMeasurement(float x, float y, float speed, int frame_id) throws SQLException {
        log.trace("adding ball measurement ({}, {}, {}, {})", x, y, speed, frame_id);
        try (NamedParameterStatement stmt = new NamedParameterStatement(con, "INSERT INTO " +
                "ball_measurements(x, y, speed, frame_id) " +
                "VALUES(:x, :y, :speed, :frame_id)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setFloat("x", x);
            stmt.setFloat("y", y);
            stmt.setFloat("speed", speed);
            stmt.setInt("frame_id", frame_id);
            stmt.execute();
            int ball_measurement_id = stmt.getGeneratedKey();  // NOTE: perhaps not needed?
            log.trace("added ball measurement with {}", ball_measurement_id);
            return ball_measurement_id;
        }
    }

    public void write(Connection con, PositionalDataset dataset, int chunkSize) throws ClassNotFoundException, SQLException {

//        /* Empty tables. */
//        con.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
//        con.createStatement().executeUpdate("TRUNCATE teams");
//        con.createStatement().executeUpdate("TRUNCATE matches");
//        con.createStatement().executeUpdate("TRUNCATE players");
//        con.createStatement().executeUpdate("TRUNCATE referees");
//        con.createStatement().executeUpdate("TRUNCATE matches_referees_maps");
//        con.createStatement().executeUpdate("TRUNCATE frames");
//        con.createStatement().executeUpdate("TRUNCATE referee_measurement");
//        con.createStatement().executeUpdate("TRUNCATE ball_measurements");
//        con.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS = 1");


        int home_team_id = addTeam(dataset.getTeamHome());
        int away_team_id = addTeam(dataset.getTeamAway());
        int match_id = addMatch(home_team_id, away_team_id, dataset.getDate());

        int previousTimestamp = -1;
        int frameCounter = 0;
        String previousPlayer = "";
        int player_id = -1;

        for (PositionalRecord record : dataset.getRecords()) {
            /* NOTE: we assume the median of the maximum value of the timestamp is the total match time of a normal 90 minute match.
            * This turned out to be 6579600. Since timestamp always in hundreds we get the number of frames in such a match by dividing by 100, so 65796.
            * We know that a match is 90 minutes, so frames_per_second = 65796 / 90 / 60 = 12.1844444
            * The time in between each frame is thus 1 / 12.1844444 = 0.08207185877
            * In milliseconds this is 0.08207185877 * 1000 = 82.071858771
            *
            * */
            double TIME_BETWEEN_FRAMES = 82.0718584716;

            int currentTimestamp = record.getTimestamp();

            /* Make sure we get well-formatted series per player, starting at 0. */
            if (currentTimestamp < previousTimestamp) {
                assert(currentTimestamp == 0);
                frameCounter = 0;
            } else {
                assert(currentTimestamp == previousTimestamp + 100);
            }

            int milliseconds = (int) Math.floor((double)frameCounter * TIME_BETWEEN_FRAMES);

            previousTimestamp = currentTimestamp;

            /* ADD FRAME */
            int frame_id = addFrame(frameCounter, match_id, milliseconds);

            /* TODO:

            1: Fix unicode decoding problem (e.g. player names)
            2: Add referee measurement

             */
            if (true) {
                throw new NotImplementedException();
            }


            /* ADD MEASUREMENT */
            if (record.getName().equals("ball")) {
                int ball_measurement_id = addBallMeasurement(record.getX(), record.getY(), record.getSpeed(), frame_id);
            } else {
                /* ADD PLAYER (if not seen before) */
                String currentPlayer = record.getName();
                if (!currentPlayer.equals(previousPlayer)) {  // Already added in previous iteration
                    player_id = addPlayer(record.getName());
                }
                int player_measurement_id = addPlayerMeasurement(record.getX(), record.getY(), record.getSpeed(), frame_id, player_id);
                previousPlayer = currentPlayer;
            }

            frameCounter++;
        }

//        private int timestamp;
//
//        private float x;
//
//        private float y;
//
//        private String markerName;
//
//        private float speed;
//
//        private float acceleration;
//
//        private String name;
    }
}
