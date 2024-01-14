package app.observer;

/**
 * The {@code Subject} interface defines methods for notifying observers of different events.
 * Classes implementing this interface act as subjects that observers can subscribe to.
 */
public interface Subject {

    /**
     * Notifies all registered observers about a generic event.
     */
    void notifyObserversEvent();

    /**
     * Notifies all registered observers about a merch-related event.
     */
    void notifyObserversMerch();

    /**
     * Notifies all registered observers about an album-related event.
     */
    void notifyObserversAlbum();
}
