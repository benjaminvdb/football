package nl.liacs.sports.football.parser.psv.positional.parsers;

import com.google.common.base.Preconditions;

import nl.liacs.sports.football.parser.psv.positional.models.PositionalDataset;
import nl.liacs.sports.football.parser.psv.positional.models.PositionalRecord;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PositionalParser {
    private static final Logger log = LoggerFactory.getLogger(PositionalParser.class);

    private final static int NUM_FIELDS = 7;
    private final static int LIMIT_RECORDS = 5;

    private File file;

    public PositionalParser(File file) {
        this.file = file;
    }

    public PositionalDataset parse() {
        Reader in = null;
        try {
            log.info("parsing file {}", file.getName());

            String filename = file.getName().replace("DQA", "").replace("ERE", "").replace(".csv", "").trim();
            Matcher matcher = Pattern.compile("(\\d+)_(\\d+)_(\\d+)[ _-]*([A-Z].+) ?[-_] ?([A-Z].+)").matcher(filename);
            matcher.find();

            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int day = Integer.parseInt(matcher.group(3));

            String teamHome = matcher.group(4);
            String teamAway = matcher.group(5);

            LocalDate date = LocalDate.of(year, month, day);

            List<PositionalRecord> outRecords = new ArrayList<>();

//            in = new FileReader(this.file);
//            Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);

            final Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            final CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());

            int count = 0;

            for (CSVRecord record : parser) {
                if (count == LIMIT_RECORDS) {
                    break;
                }

                Preconditions.checkState(
                        record.size() == NUM_FIELDS || record.size() == NUM_FIELDS + 1,
                        "invalid record length, expected %d, got %d in file %s",
                        NUM_FIELDS, record.size(), this.file);
                PositionalRecord posRecord = new PositionalRecord();
                posRecord.setTimestamp(Integer.parseInt(record.get("Timestamp")));
                posRecord.setX(Float.parseFloat(record.get("X")));
                posRecord.setY(Float.parseFloat(record.get("Y")));
                posRecord.setMarkerName(record.get("Marker Name"));

                /* NOTE: Sometimes in Dutch and sometimes in English... */
                try {
                    posRecord.setSpeed(Float.parseFloat(record.get("Speed")));
                } catch (IllegalArgumentException iae) {
                    posRecord.setSpeed(Float.parseFloat(record.get("Snelheid")));
                }

                posRecord.setAcceleration(Float.parseFloat(record.get("Acceleration")));

                /* NOTE: Sometimes in Dutch and sometimes in English... */
                try {
                    posRecord.setName(record.get("Name"));
                } catch (IllegalArgumentException iae) {
                    posRecord.setName(record.get("Naam"));
                }

                /* NOTE: Some files have an extra field 'ID'. */
                if (record.size() == NUM_FIELDS + 1) {
                    posRecord.setID(Integer.parseInt(record.get("ID")));
                }

                outRecords.add(posRecord);

                count++;
            }

            PositionalDataset dataset = new PositionalDataset(outRecords, teamHome, teamAway);
            dataset.setDate(date);

            return dataset;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
