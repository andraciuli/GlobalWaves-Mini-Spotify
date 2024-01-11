package app.wrapped;

import app.audio.Files.Song;
import app.user.User;
import lombok.Getter;
import lombok.Setter;

public class UserWrapp {
    @Getter
    @Setter
    private User user;
    @Getter
    @Setter
    private int listens;

    public UserWrapp(User user) {
        this.user = user;
        listens = 1;
    }

    public void incrementListens() {
        listens++;
    }
}
