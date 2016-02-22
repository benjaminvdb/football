//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nl.liacs.sports.football.parser.positional.models;

import java.util.Optional;

public class AdditionalInfo {
    private Optional<PlayerInit> player_init = Optional.empty();
    private Optional<PlayerSwap> player_swap = Optional.empty();
    private Optional<BallDistance> ball_distance = Optional.empty();
    private Optional<PitchSize> pitch_size = Optional.empty();

    public AdditionalInfo() {
    }

    public Optional<PlayerInit> getPlayerInit() {
        return this.player_init;
    }

    public void setPlayerInit(Optional<PlayerInit> player_init) {
        this.player_init = player_init;
    }

    public Optional<PlayerSwap> getPlayerSwap() {
        return this.player_swap;
    }

    public void setPlayerSwap(Optional<PlayerSwap> player_swap) {
        this.player_swap = player_swap;
    }

    public Optional<BallDistance> getBallDistance() {
        return this.ball_distance;
    }

    public void setBallDistance(Optional<BallDistance> ball_distance) {
        this.ball_distance = ball_distance;
    }

    public Optional<PitchSize> getPitchSize() {
        return this.pitch_size;
    }

    public void setPitchSize(Optional<PitchSize> pitch_size) {
        this.pitch_size = pitch_size;
    }
}
