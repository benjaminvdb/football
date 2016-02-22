//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nl.liacs.sports.football.parser.positional.parsers;

import nl.liacs.sports.football.parser.exceptions.InvalidInputLineException;
import nl.liacs.sports.football.parser.positional.PositionalParser;
import nl.liacs.sports.football.parser.positional.models.Frame;

public class FrameParser {

    public static Frame parse(String s) throws InvalidInputLineException {
        String[] entities = PositionalParser.chunkEntities(s);
        if (entities.length != 1) {
            throw new InvalidInputLineException("a frame block should consist of only one entity, got " + entities.length);
        } else {
            final String[] fields = PositionalParser.chunkFields(entities[0], 3);
            return new Frame() {
                {
                    setFrameNumber(Integer.parseInt(fields[0]));
                    setMinute(Integer.parseInt(fields[1]));
                    setSection(Integer.parseInt(fields[2]));
                }
            };
        }
    }
}
