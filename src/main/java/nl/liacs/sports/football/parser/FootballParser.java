package nl.liacs.sports.football.parser;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

import nl.liacs.sports.football.parser.bundesliga.meta.parsers.MetadataParser;
import nl.liacs.sports.football.parser.bundesliga.positional.PositionalParser;
import nl.liacs.sports.football.parser.sql.DBOutputWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Properties;

import ch.qos.logback.classic.Level;

public class FootballParser {
    private static String TEAM_HOME = "FC Koln";
    private static String TEAM_AWAY = "Hannover 96";
    final private static Logger log = LoggerFactory.getLogger(FootballParser.class);

    public static void main(String[] args) throws ClassNotFoundException {
        ((ch.qos.logback.classic.Logger)log).setLevel(Level.ALL);

        /* Iput argument parser. */
        ArgumentParser argumentParser = ArgumentParsers.newArgumentParser("Parser")
                .defaultHelp(true)
                .description("Parse football data and store in database.");
        Subparsers subparsers = argumentParser.addSubparsers().dest("subparser_name");

        /* Create a subcommand to handle Bundesliga data. */
        Subparser bundesligaSubparser = subparsers.addParser("bundesliga")
                .defaultHelp(true)
                .description("Parser for Bundesliga data");
        setDefaultArguments(bundesligaSubparser);

        /* Create another subcommand to handle PSV data. */
        Subparser psvSubparser = subparsers.addParser("psv")
                .defaultHelp(true)
                .description("Parser for PSV data");
        setDefaultArguments(psvSubparser);

        /* Parse the input arguments and launcher one of the parsers. */
        Namespace ns = null;
        try {
            ns = argumentParser.parseArgs(args);

            String user = ns.get("user");
            String password = ns.getString("password");
            String host = ns.getString("host");
            String port = ns.getString("port");

            String subcommand = ns.getString("subparser_name");

            try (Connection con = getConnection(user, password, host, port)) {
                switch (subcommand) {
                    case "bundesliga":

                        parseBundesliga(ns);
                        break;
                    case "psv":
                        parsePSV(ns);
                        break;
                    default:
                        throw new IllegalArgumentException("illegal subcommand: " + subcommand);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


        } catch (ArgumentParserException e) {
            argumentParser.handleError(e);
            System.exit(1);
        }
    }

    /**
     * Parse the Bundesliga data.
     * @param ns  the namespace as constructed by the argument parser
     * @throws ClassNotFoundException
     */
    public static void parseBundesliga(Namespace ns) throws ClassNotFoundException {
        File dataFile = new File(ns.getString("data"));
        log.info("reading positional data from {}", dataFile.getAbsolutePath());

        File metadataFile = new File(ns.getString("metadata"));
        log.info("reading metadata from {}", metadataFile.getAbsolutePath());

        int chunkSize = Integer.parseInt(ns.getString("chunkSize"));
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

    /**
     * Parse the PSV data.
     * @param ns  the namespace as constructed by the argument parser
     * @throws ClassNotFoundException
     */
    public static void parsePSV(Namespace ns) {

    }

    /**
     * Many of the arguments for the subcommands are the same, so we can reuse them using this
     * function.
     * @param subparser
     * @return
     */
    public static Subparser setDefaultArguments(Subparser subparser) {
        subparser.addArgument("data").type(Arguments.fileType().acceptSystemIn().verifyCanRead())
                .help("File with positional data");
        subparser.addArgument("metadata")
                .help("File with metadata").type(Arguments.fileType().acceptSystemIn().verifyCanRead());
        subparser.addArgument("-u", "--user").setDefault("root")
                .help("Username for connecting to the database.");
        subparser.addArgument("-p", "--password").setDefault("")
                .help("Password for connecting to the database");
        subparser.addArgument("-H", "--host").setDefault("127.0.0.1")
                .help("Host to connect to");
        subparser.addArgument("-P", "--port").setDefault("3306")
                .help("Port to connect to");
        subparser.addArgument("-c", "--chunkSize").setDefault("3000")
                .help("Chunk size (larger is faster, but uses more memory)");
        return subparser;
    }

    /**
     * Establish a connection with the MySQL database.
     * @param userName
     * @param password
     * @param host
     * @param port
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
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
