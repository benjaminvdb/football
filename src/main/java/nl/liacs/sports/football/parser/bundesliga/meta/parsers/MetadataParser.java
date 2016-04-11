package nl.liacs.sports.football.parser.bundesliga.meta.parsers;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import nl.liacs.sports.football.parser.bundesliga.meta.models.Player;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MetadataParser {
    private String teamHome;
    private String teamAway;
    private List<Player> players;
    private Date date;
    private File file;

    public MetadataParser(File file) throws IOException, ParseException {
        this.file = file;
        parse();
    }

    public String getTeamHome() {
        return teamHome;
    }

    public void setTeamHome(String teamHome) {
        this.teamHome = teamHome;
    }

    public String getTeamAway() {
        return teamAway;
    }

    public void setTeamAway(String teamAway) {
        this.teamAway = teamAway;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    private void parse() throws ParseException, IOException {
        String xml = Files.toString(this.file, Charset.forName("utf-8"));
        org.jsoup.parser.Parser parser = org.jsoup.parser.Parser.xmlParser();
        Document doc = Jsoup.parse(xml, "", parser);

        /* Obtain the team names from the XML metadata. */
        List<String> teamNames = doc.select("sports-content-code[code-type=team]").stream()
                .map(x -> x.attr("code-name").toString())
                .collect(Collectors.toList());

        Preconditions.checkState(teamNames.size() == 2, "two team names should be defined");
        teamHome = teamNames.get(0);
        teamAway = teamNames.get(1);

        /* Get the date of the event. */
        String dateString = doc.select("event-metadata").attr("date-coverage-value");
        date = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ").parse(dateString);

        /* Collect player metadata. */
        players = doc.select("player").stream()
                .map(x -> new Player() {{
                    Elements metadata = x.getElementsByTag("player-metadata");
                    setPosition(metadata.attr("position-event"));
                    setUniformNumber(Integer.parseInt(metadata.attr("uniform-number")));
                    setInternalId(metadata.attr("id"));

                    Elements name = x.getElementsByTag("name");
                    setFullName(name.attr("full"));
                    setNickName(name.attr("nick"));
                    setLastName(name.attr("last"));
                }})
                .collect(Collectors.toList());
    }
}
