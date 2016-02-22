//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nl.liacs.sports.football.parser.positional.parsers;

import nl.liacs.sports.football.parser.positional.PositionalParser;
import nl.liacs.sports.football.parser.positional.models.Referee;

import java.util.ArrayList;
import java.util.List;

public class RefereesParser {

    public static List<Referee> parse(String s) {
        String[] referees = PositionalParser.chunkEntities(s);
        ArrayList<Referee> newReferees = new ArrayList();
        for (String referee : referees) {
            final String[] fields = PositionalParser.chunkFields(referee, 3);
            Referee newReferee = new Referee() {
                {
                    setX(Float.parseFloat(fields[0]));
                    setY(Float.parseFloat(fields[1]));
                    setSpeed(Float.parseFloat(fields[2]));
                }
            };
            newReferees.add(newReferee);
        }

        return newReferees;
    }
}
