package app.pageSystem;

import app.user.User;

interface Command {
    void execute(User user);

    void undo(User user);
}
