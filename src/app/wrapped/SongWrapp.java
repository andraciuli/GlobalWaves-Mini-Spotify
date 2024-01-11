package app.wrapped;

import app.audio.Files.Song;
import lombok.Getter;
import lombok.Setter;

public class SongWrapp {
    @Getter
    @Setter
    private Song song;
    @Getter
    @Setter
    private int listens;

    public SongWrapp(Song song) {
        this.song = song;
        listens = 1;
    }

    public void incrementListens() {
        listens++;
    }
}
