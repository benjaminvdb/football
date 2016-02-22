//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nl.liacs.sports.football.parser.positional.parsers;

import nl.liacs.sports.football.parser.exceptions.InvalidInputLineException;
import nl.liacs.sports.football.parser.positional.PositionalParser;
import nl.liacs.sports.football.parser.positional.models.Ball;

public class BallParser {
    public static Ball parse(String s) {
        String[] entities = PositionalParser.chunkEntities(s);
        if(entities.length != 1) {
            throw new InvalidInputLineException("a ball block should consist of only one entity, got " + entities.length);
        } else {
            final String[] fields = PositionalParser.chunkFields(entities[0], 6);
            return new Ball() {
                {
                    setX(Float.parseFloat(fields[0]));
                    setY(Float.parseFloat(fields[1]));
                    setZ(Float.parseFloat(fields[2]));
                    setSpeed(Float.parseFloat(fields[3]));
                    setFlag(Boolean.parseBoolean(fields[4]));
                    setPossession(Integer.parseInt(fields[5]));
                }
            };
        }
    }
}