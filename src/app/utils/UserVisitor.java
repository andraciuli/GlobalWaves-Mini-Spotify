package app.utils;

import app.user.Artist;
import app.user.Host;
import app.user.User;

/**
 * The {@code UserVisitor} interface defines methods for visiting different types of users.
 * Implementations of this interface can perform specific actions for each user type.
 */
public interface UserVisitor {

    /**
     * Deletes a regular user.
     *
     * @param user The user to be deleted.
     * @return A message indicating the result of the deletion operation.
     */
    String deleteUser(User user);

    /**
     * Deletes an artist user.
     *
     * @param artist The artist to be deleted.
     * @return A message indicating the result of the deletion operation.
     */
    String deleteUser(Artist artist);

    /**
     * Deletes a host user.
     *
     * @param host The host to be deleted.
     * @return A message indicating the result of the deletion operation.
     */
    String deleteUser(Host host);
}
