package nl.liacs.sports.football.parser;

import nl.liacs.sports.football.parser.meta.MetadataParser;
import nl.liacs.sports.football.parser.positional.PositionalParser;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

public class DataMerger {
    private org.slf4j.Logger log = LoggerFactory.getLogger(DataMerger.class);

    public DataMerger(File positionalData, File metadata) throws IOException, ParseException {
        PositionalParser positionalParser = new PositionalParser(positionalData);
        MetadataParser metadataParser = new MetadataParser(metadata);
    }
}
