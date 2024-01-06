package app.utils;

import app.user.Artist;
import app.user.Host;
import app.user.User;

public interface UserVisitor {
    String deleteUser(User user);

    String deleteUser(Artist artist);

    String deleteUser(Host host);
}
