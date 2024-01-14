package app.pageSystem;

import app.user.User;
import app.utils.Enums;

/**
 * Implementation of the {@link Command} interface that handles navigation between pages for a user.
 */
public class PageNavigation implements Command {

    /**
     * Executes the navigation to the next page for the user.
     *
     * @param user The user for whom the navigation is performed.
     */
    @Override
    public void execute(final User user) {
        user.setPreviousPage(user.getCurrentPage());
        user.setCurrentPage(user.getNextPage());
        user.setNextPage(Enums.currentPage.DEFAULT);
    }

    /**
     * Undoes the previous navigation and moves the user back to the previous page.
     *
     * @param user The user for whom the undo operation is performed.
     */
    @Override
    public void undo(final User user) {
        user.setNextPage(user.getCurrentPage());
        int index = user.getIstoricPages().indexOf(user.getCurrentPage());
        if (index != -1 && index > 0) {
            user.setCurrentPage(user.getIstoricPages().get(index - 1));
        }
    }
}
