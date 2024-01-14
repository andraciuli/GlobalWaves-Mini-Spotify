package app.observer;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * An interface for observers that receive updates from a subject.
 */
public interface Observer {

    /**
     * This method is called by the subject to notify the observer of any updates.
     *
     * @param notification The notification in the form of an ObjectNode.
     */
    void update(ObjectNode notification);
}
