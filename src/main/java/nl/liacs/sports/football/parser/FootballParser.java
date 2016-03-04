package nl.liacs.sports.football.parser;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import nl.liacs.sports.football.parser.meta.MetadataParser;
import nl.liacs.sports.football.parser.positional.PositionalParser;
import nl.liacs.sports.football.parser.sql.DBOutputWriter;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Optional;
import java.util.Properties;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;


public class FootballParser {
    private static String TEAM_HOME = "FC Koln";
    private static String TEAM_AWAY = "Hannover 96";

    public static void main(String[] args) throws ClassNotFoundException {
        Logger log = (Logger) LoggerFactory.getLogger(FootballParser.class);
        log.setLevel(Level.ALL);

        /* Iput argument parser. */
        ArgumentParser argumentParser = ArgumentParsers.newArgumentParser("Parser")
                .defaultHelp(true)
                .description("Parse football data and store in database.");
        argumentParser.addArgument("data")
                .help("File with positional data");
        argumentParser.addArgument("metadata")
                .help("File with metadata");
        argumentParser.addArgument("-u", "--user").setDefault("root")
                .help("Username for connecting to the database.");
        argumentParser.addArgument("-p", "--password").setDefault("")
                .help("Password for connecting to the database");
        argumentParser.addArgument("-H", "--host").setDefault("127.0.0.1")
                .help("Host to connect to");
        argumentParser.addArgument("-P", "--port").setDefault("3306")
                .help("Port to connect to");
        argumentParser.addArgument("-c", "--chunk-size").setDefault("3000")
                .help("Chunk size (larger is faster, but uses more memory)");
        Namespace ns = null;
        try {
            ns = argumentParser.parseArgs(args);
        } catch (ArgumentParserException e) {
            argumentParser.handleError(e);
            System.exit(1);
        }

        File dataFile = new File(ns.getString("data"));
        File metadataFile = new File(ns.getString("metadata"));
        int chunkSize = ns.getInt("chunk-size");

        log.info("using chunk size of {}", chunkSize);

        MetadataParser metadataParser;
        PositionalParser parser;
        try {
            /* Parse data. */
            log.info("Parsing positional data...");
            parser = new PositionalParser(dataFile);

            log.info("Parsing metadata...");
            metadataParser = new MetadataParser(metadataFile);  // TODO: retrieve more interesting metadata

            /* Save data to MySQL database. */
            // TODO: make connections parameters input parameters.
            String user = ns.get("user");
            String password = ns.getString("password");
            String host = ns.getString("host");
            String port = ns.getString("port");
            try (Connection con = getConnection(user, password, host, port)) {
                DBOutputWriter.write(
                        con,
                        parser.getRecords(),
                        metadataParser.getTeamHome(),
                        metadataParser.getTeamAway(),
                        chunkSize
                );
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection(String userName,
                                    String password,
                                    String host,
                                    String port) throws SQLException, ClassNotFoundException {
        Properties props = new Properties();
        props.put("user", userName);
        props.put("password", password);
        Class.forName("com.mysql.jdbc.Driver");
        Connection con = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/psv",
                    props);
        System.out.println("Connected to database");
        return con;
    }
}
