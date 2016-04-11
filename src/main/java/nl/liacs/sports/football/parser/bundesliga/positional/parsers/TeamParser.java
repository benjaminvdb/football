//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nl.liacs.sports.football.parser.bundesliga.positional.parsers;

import nl.liacs.sports.football.parser.bundesliga.positional.PositionalParser;
import nl.liacs.sports.football.parser.bundesliga.positional.models.Player;
import nl.liacs.sports.football.parser.exceptions.InvalidInputLineException;

import java.util.ArrayList;
import java.util.List;

public class TeamParser {
    private static final String DEFAULT_DELIMITER = ",";
    private static final String DEFAULT_END = ";";

    public static List<Player> parse(String s) throws InvalidInputLineException {
        String[] players = PositionalParser.chunkEntities(s);
        ArrayList<Player> newPlayers = new ArrayList();
        for (String player : players) {
            final String[] fields = PositionalParser.chunkFields(player, 4);
            Player newPlayer = new Player() {
                {
                    setJerseyNumber(Integer.parseInt(fields[0]));
                    setX(Float.parseFloat(fields[1]));
                    setY(Float.parseFloat(fields[2]));
                    setSpeed(Float.parseFloat(fields[3]));
                }
            };
            newPlayers.add(newPlayer);
        }

        return newPlayers;
    }
}
