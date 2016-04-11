//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nl.liacs.sports.football.parser.bundesliga.positional;

import com.google.common.io.CharStreams;

import nl.liacs.sports.football.parser.bundesliga.positional.models.Record;
import nl.liacs.sports.football.parser.bundesliga.positional.parsers.AdditionalInfoParser;
import nl.liacs.sports.football.parser.bundesliga.positional.parsers.BallParser;
import nl.liacs.sports.football.parser.bundesliga.positional.parsers.FrameParser;
import nl.liacs.sports.football.parser.bundesliga.positional.parsers.RefereesParser;
import nl.liacs.sports.football.parser.bundesliga.positional.parsers.TeamParser;
import nl.liacs.sports.football.parser.exceptions.InvalidInputLineException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PositionalParser {
    private static final String DEFAULT_DELIMITER = ",";
    private static final String DEFAULT_END = ";";

    private File file;

    private List<Record> records;

    public PositionalParser(File file) throws IOException {
        this.file = file;
        this.records = parse();
    }

    public static String[] chunkFields(String s, int nparts) throws InvalidInputLineException {
        String[] parts = s.split(",", nparts);
        if (parts.length != nparts) {
            throw new InvalidInputLineException("this block was expected to contain " + Integer.toString(nparts) + " fields.");
        } else {
            return parts;
        }
    }

    public static String[] chunkFields(String s) throws InvalidInputLineException {
        return s.split(",");
    }

    public static String[] chunkEntities(String s) throws InvalidInputLineException {
        return s.split(";");
    }

    public List<Record> getRecords() {
        return records;
    }

    /**
     * Deserialize the input data into a more manageable format that can be serialized to JSON
     * easily.
     *
     * @return a list of Record objects, each structurally equivalent to the input data.
     */
    private List<Record> parse() throws IOException {
        List<Record> records = new ArrayList<>();

        List<String> lines = CharStreams.readLines(new FileReader(this.file));

        for (String line : lines) {
            final String[] block = line.split("#", 6);
            Record record = new Record() {
                {
                    setFrame(FrameParser.parse(block[0]));
                    setTeamHome(TeamParser.parse(block[1]));
                    setTeamAway(TeamParser.parse(block[2]));
                    setReferees(RefereesParser.parse(block[3]));
                    setBall(BallParser.parse(block[4]));
                    setAdditionalInfo(AdditionalInfoParser.parse(block[5]));
                }
            };
            records.add(record);
        }
        return records;
    }
}
