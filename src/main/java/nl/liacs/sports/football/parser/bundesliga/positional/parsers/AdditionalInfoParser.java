//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nl.liacs.sports.football.parser.bundesliga.positional.parsers;

import nl.liacs.sports.football.parser.bundesliga.positional.PositionalParser;
import nl.liacs.sports.football.parser.bundesliga.positional.models.AdditionalInfo;
import nl.liacs.sports.football.parser.bundesliga.positional.models.BallDistance;
import nl.liacs.sports.football.parser.bundesliga.positional.models.PitchSize;
import nl.liacs.sports.football.parser.bundesliga.positional.models.PlayerInit;
import nl.liacs.sports.football.parser.bundesliga.positional.models.PlayerSwap;
import nl.liacs.sports.football.parser.exceptions.InvalidInputFieldException;

import java.util.Optional;

public class AdditionalInfoParser {
    public AdditionalInfoParser() {
    }

    public static AdditionalInfo parse(String s) {
        String[] entities = PositionalParser.chunkEntities(s);
        AdditionalInfo additionalInfo = new AdditionalInfo();
        for (String entity : entities) {
            final String[] fields = PositionalParser.chunkFields(entity);
            if(fields[0].isEmpty()) {
                break;
            }

            int id = Integer.parseInt(fields[0]);
            switch(id) {
                case 1:
                    additionalInfo.setPlayerInit(Optional.of(new PlayerInit() {
                        {
                            this.setId(Float.parseFloat(fields[1]));
                            this.setFrom(Float.parseFloat(fields[2]));
                            this.setTo(Float.parseFloat(fields[3]));
                        }
                    }));
                    break;
                case 2:
                    additionalInfo.setPlayerSwap(Optional.of(new PlayerSwap() {
                        {
                            this.setId1(Float.parseFloat(fields[1]));
                            this.setId2(Float.parseFloat(fields[2]));
                            this.setFrom(Float.parseFloat(fields[3]));
                            this.setTo(Float.parseFloat(fields[4]));
                        }
                    }));
                    break;
                case 3:
                    additionalInfo.setBallDistance(Optional.of(new BallDistance() {
                        {
                            this.setDistance(Float.parseFloat(fields[1]));
                        }
                    }));
                    break;
                case 4:
                    additionalInfo.setPitchSize(Optional.of(new PitchSize() {
                        {
                            this.setXDistance(Float.parseFloat(fields[1]));
                            this.setYDistance(Float.parseFloat(fields[2]));
                        }
                    }));
                    break;
                default:
                    throw new InvalidInputFieldException("invalid field specifier in AdditionalInfo block");
            }
        }

        return additionalInfo;
    }
}
