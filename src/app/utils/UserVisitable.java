package app.utils;

import app.Admin;

/**
 * The {@code UserVisitable} interface defines a method for accepting a visitor for user deletion.
 * Classes implementing this interface can be visited by a {@link Admin.Deletion} visitor.
 */
public interface UserVisitable {

    /**
     * Accepts a visitor for user deletion.
     *
     * @param deletion The visitor representing the deletion operation.
     * @return A message indicating the result of the deletion operation.
     */
    String acceptVisitor(Admin.Deletion deletion);
}
