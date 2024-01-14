package app.wrapped;

import app.user.User;
import lombok.Getter;
import lombok.Setter;

/**
 * A class representing a wrapped version of a user, including the user and the number of listens.
 */
public class UserWrapp {

    @Getter
    @Setter
    private User user;

    @Getter
    @Setter
    private int listens;

    /**
     * Constructs a UserWrapp instance for the given user with an initial listen count of 1.
     *
     * @param user The user to wrap.
     */
    public UserWrapp(final User user) {
        this.user = user;
        listens = 1;
    }

    /**
     * Increments the listen count for the wrapped user.
     */
    public void incrementListens() {
        listens++;
    }
}
