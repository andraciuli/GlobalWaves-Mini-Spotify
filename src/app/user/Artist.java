package app.user;

import app.Admin;
import app.audio.Collections.Album;
import app.audio.Collections.Playlist;
import app.audio.Files.Song;
import app.audio.LibraryEntry;
import app.observer.Subject;
import app.player.Player;
import app.player.PlayerSource;
import app.utils.UserVisitable;
import app.wrapped.UserWrapp;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.CommandInput;
import fileio.input.SongInput;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Artist extends LibraryEntry implements UserVisitable, Subject {
    public static ObjectMapper objectMapper = new ObjectMapper();
    @Getter
    private String name;
    @Getter
    private int age;
    @Getter
    private String city;
    @Getter
    private String userType;
    @Getter
    private ArrayList<Album> albums;
    @Getter
    private static ArrayList<Event> events;
    @Getter
    private ArrayList<Merch> merches;
    @Getter
    @Setter
    private double merchRevenue;
    @Getter
    private double songRevenue;
    @Getter
    @Setter
    private ArrayList<String> subscribers;
    @Getter
    @Setter
    private ArrayList<UserWrapp> listeners;
    @Getter
    @Setter
    private boolean wasPlayed;
    private static final int TOP_LIMIT = 5;

    public Artist(final String name, final int age, final String city, final String userType) {
        super(name);
        this.name = name;
        this.age = age;
        this.city = city;
        this.userType = userType;
        albums = new ArrayList<>();
        events = new ArrayList<>();
        merches = new ArrayList<>();
        merchRevenue = 0;
        songRevenue = 0;
        subscribers = new ArrayList<>();
        listeners = new ArrayList<>();
        wasPlayed = false;
    }

    /**
     * Accepts a deletion visitor and delegates the deletion operation to the appropriate method
     * based on the user type.
     *
     * @param deletion The deletion visitor.
     * @return A message indicating the success or failure of the deletion operation.
     */
    @Override
    public String acceptVisitor(final Admin.Deletion deletion) {
        return deletion.deleteUser(this);
    }

    /**
     * Get the total number of likes for the artist, based
     * on the likes of their song in their albums.
     *
     * @return The total number of likes for the artist.
     */
    public int getNumberLikes() {
        int likes = 0;
        for (Album album : getAlbums()) {
            likes += album.getNumberLikes();
        }
        return likes;
    }

    public ArrayList<User> top5Fans() {
        // Sort the listeners based on the number of listens in descending order
        Collections.sort(listeners, Comparator.comparingInt(UserWrapp::getListens).reversed());

        // Create a list to store the top 5 fans
        ArrayList<User> top5FansList = new ArrayList<>();

        // Add up to the first 5 fans to the list
        for (int i = 0; i < Math.min(TOP_LIMIT, listeners.size()); i++) {
            top5FansList.add(listeners.get(i).getUser());
        }

        return top5FansList;
    }

    private static boolean hasDuplicates(final ArrayList<SongInput> songs) {
        HashSet<String> uniqueElements = new HashSet<>();
        for (SongInput song : songs) {
            if (!uniqueElements.add(song.getName())) {
                // If the element is already in the HashSet, it's a duplicate
                return true;
            }
        }
        // No duplicates found
        return false;
    }

    /**
     * Adds a new album for the artist based on the provided command input.
     *
     * @param commandInput The input containing details of the new album.
     * @return A message indicating the success or failure of the operation.
     */
    public static String addAlbum(final CommandInput commandInput) {
        // Check if the specified username exists in the system
        if (Admin.getUser(commandInput.getUsername()) == null) {
            return "The username " + commandInput.getUsername() + " doesn't exist.";
        }
        User user = Admin.getUser(commandInput.getUsername());
        // Check if the user with the specified username is of type "artist"
        if (!"artist".equals(user.getUserType())) {
            return commandInput.getUsername() + " is not an artist.";
        }

        // Check for duplicate album names for the artist
        Artist artist = Admin.getArtist(commandInput.getUsername());
        if (!user.getAlbums().isEmpty()) {
            for (Album album : user.getAlbums()) {
                if (commandInput.getName().equals(album.getName())) {
                    return commandInput.getUsername() + " has another album with the same name.";
                }
            }
        }

        // Check for duplicate songs in the input
        boolean hasDuplicates = hasDuplicates(commandInput.getSongs());
        if (hasDuplicates) {
            return commandInput.getUsername() + " has the same song at least twice in this album.";
        }

        // Create new Song instances for each song in the input and update the system's song list
        ArrayList<Song> songsAlbum = new ArrayList<>();
        List<Song> songs = Admin.getSongs();
        for (SongInput song : commandInput.getSongs()) {
            Song songInAlbum = new Song(song.getName(), song.getDuration(), song.getAlbum(),
                    song.getTags(), song.getLyrics(), song.getGenre(), song.getReleaseYear(),
                    song.getArtist());
            songs.add(songInAlbum);
            songsAlbum.add(songInAlbum);
        }
        Admin.updateSongList(songs);

        // Create and add a new Album instance for the artist and update the system's album list
        Album album = new Album(commandInput.getName(), commandInput.getUsername(),
                commandInput.getReleaseYear(), commandInput.getDescription(), songsAlbum);
        user.getAlbums().add(album);
        artist.getAlbums().add(album);
        List<Album> albumsToAdd = Admin.getAlbums();
        albumsToAdd.add(album);
        Admin.updateAlbumList(albumsToAdd);

        artist.notifyObserversAlbum();
        return commandInput.getUsername() + " has added new album successfully.";
    }

    /**
     * Removes an album for the artist based on the provided command input.
     *
     * @param commandInput The input containing details of the album to be removed.
     * @return A message indicating the success or failure of the operation.
     */
    public static String removeAlbum(final CommandInput commandInput) {
        // Check if the specified username exists in the system
        if (Admin.getUser(commandInput.getUsername()) == null) {
            return "The username " + commandInput.getUsername() + " doesn't exist.";
        } else {
            User user = Admin.getUser(commandInput.getUsername());

            // Check if the user with the specified username is of type "artist"
            if (!"artist".equals(user.getUserType())) {
                return commandInput.getUsername() + " is not an artist.";
            }
        }
        User user = Admin.getUser(commandInput.getUsername());
        Artist artist = Admin.getArtist(commandInput.getUsername());
        Album albumToRemove = null;

        // Find the album to be removed within the artist's albums
        for (Album album : artist.getAlbums()) {
            if (commandInput.getName().equals(album.getName())) {
                albumToRemove = album;
                break;
            }
        }

        // Check if the album with the given name exists for the artist
        if (albumToRemove == null) {
            return commandInput.getUsername() + " doesn't have an album with the given name.";
        }

        // Check if the album is currently being used by any player in the system
        for (User user1 : Admin.getUsers()) {
            Player player = User.getPlayerInstance(user1);
            PlayerSource source = Player.getPlayerSourceInstance(player);
            if (source != null) {
                if (source.getAudioCollection() != null) {
                    if (source.getAudioCollection().getName().equals(commandInput.getName())) {
                        return commandInput.getUsername() + " can't delete this album.";
                    }
                }
            }
        }

        // Remove references to the album's songs in users' playlists and liked songs
        for (User user1 : Admin.getUsers()) {
            for (Playlist playlist : user1.getPlaylists()) {
                playlist.getSongs().removeAll(albumToRemove.getSongs());
            }
            user1.getLikedSongs().removeAll(albumToRemove.getSongs());
        }

        // Remove the album from the artist and update the system's album and song lists
        user.getAlbums().remove(albumToRemove);
        artist.getAlbums().remove(albumToRemove);
        List<Album> albumsToRemove = Admin.getAlbums();
        albumsToRemove.remove(albumToRemove);
        Admin.updateAlbumList(albumsToRemove);
        List<Song> songsToRemove = Admin.getSongs();
        songsToRemove.removeAll(albumToRemove.getSongs());
        Admin.updateSongList(songsToRemove);
        return commandInput.getUsername() + " deleted the album successfully.";
    }

    /**
     * Shows the list of albums for the artist based on the provided command input.
     *
     * @param commandInput The input containing details for displaying albums.
     * @return The list of albums along with their associated songs.
     */
    public static ArrayList<ObjectNode> showAlbums(final CommandInput commandInput) {
        ArrayList<ObjectNode> result = new ArrayList<>();
        User user = Admin.getUser(commandInput.getUsername());
        for (Album album : user.getAlbums()) {
            ObjectNode showAlbum = objectMapper.createObjectNode();
            showAlbum.put("name", album.getName());
            ArrayList<String> songNames = new ArrayList<>();
            for (Song song : album.getSongs()) {
                songNames.add(song.getName());
            }
            showAlbum.putPOJO("songs", songNames);
            result.add(showAlbum);
        }
        return result;
    }

    /**
     * Adds a new event for the artist based on the provided command input.
     *
     * @param commandInput The input containing details of the new event.
     * @return A message indicating the success or failure of the operation.
     */
    public static String addEvent(final CommandInput commandInput) {
        if (Admin.getUser(commandInput.getUsername()) == null) {
            return "The username " + commandInput.getUsername() + " doesn't exist.";
        } else {
            User user = Admin.getUser(commandInput.getUsername());
            if (!"artist".equals(user.getUserType())) {
                return commandInput.getUsername() + " is not an artist.";
            }
        }

        if (!Event.validDate(commandInput.getDate())) {
            return "Event for " + commandInput.getUsername() + " does not have a valid date.";
        }
        Artist artistFound = Admin.getArtist(commandInput.getUsername());
        artistFound.getEvents().add(new Event(commandInput.getUsername(), commandInput.getName(),
                commandInput.getDescription(), commandInput.getDate()));

        artistFound.notifyObserversEvent();

        return commandInput.getUsername() + " has added new event successfully.";

    }

    /**
     * Removes an event for the artist based on the provided command input.
     *
     * @param commandInput The input containing details of the event to be removed.
     * @return A message indicating the success or failure of the operation.
     */
    public static String removeEvent(final CommandInput commandInput) {
        if (Admin.getUser(commandInput.getUsername()) == null) {
            return "The username " + commandInput.getUsername() + " doesn't exist.";
        }
        User user = Admin.getUser(commandInput.getUsername());

        if (!"artist".equals(user.getUserType())) {
            return commandInput.getUsername() + " is not an artist.";
        }

        // Find the event to be removed within the artist's events
        Artist artist = Admin.getArtist(commandInput.getUsername());
        Event eventToRemove = null;
        for (Event event : artist.getEvents()) {
            if (commandInput.getName().equals(event.getName())) {
                eventToRemove = event;
                break;
            }
        }

        if (eventToRemove == null) {
            return commandInput.getUsername()
                    + " doesn't have an event with the name "
                    + commandInput.getName() + ".";
        }


        // Remove event from the artist's list of events
        artist.getEvents().remove(eventToRemove);

        return commandInput.getUsername() + " deleted the event successfully.";
    }

    /**
     * Adds a new merchandise item for the artist based on the provided command input.
     *
     * @param commandInput The input containing details of the new merchandise item.
     * @return A message indicating the success or failure of the operation.
     */
    public static String addMerch(final CommandInput commandInput) {
        if (Admin.getUser(commandInput.getUsername()) == null) {
            return "The username " + commandInput.getUsername() + " doesn't exist.";
        } else {
            User user = Admin.getUser(commandInput.getUsername());
            if (!"artist".equals(user.getUserType())) {
                return commandInput.getUsername() + " is not an artist.";
            }
        }

        Artist artistFound = Admin.getArtist(commandInput.getUsername());
        if (!artistFound.getMerches().isEmpty()) {
            for (Merch merch : artistFound.merches) {
                if (merch.getName().equals(commandInput.getName())) {
                    return commandInput.getUsername() + " has merchandise with the same name.";
                }
            }
        }

        if (commandInput.getPrice() < 0) {
            return "Price for merchandise can not be negative.";
        }
        artistFound.getMerches().add(new Merch(commandInput.getUsername(), commandInput.getName(),
                commandInput.getDescription(), commandInput.getPrice()));

        artistFound.notifyObserversMerch();
        return commandInput.getUsername() + " has added new merchandise successfully.";
    }

    /**
     * Notifies all subscribers about a new event from the observable object.
     */
    @Override
    public void notifyObserversEvent() {
        for (User user1 : Admin.getUsers()) {
            // Check if there are subscribers
            if (!this.getSubscribers().isEmpty()) {
                // Iterate through each subscriber
                for (String nameSub : this.getSubscribers()) {
                    // Notify the subscriber if their username matches
                    if (user1.getUsername().equals(nameSub)) {
                        // Create a notification object
                        ObjectNode notification = objectMapper.createObjectNode();
                        notification.put("name", "New Event");
                        notification.put("description", "New Event from " + this.getName() + ".");

                        // Update the subscriber with the notification
                        user1.update(notification);
                    }
                }
            }
        }
    }

    /**
     * Notifies all subscribers about new merchandise from the observable object.
     */
    @Override
    public void notifyObserversMerch() {
        for (User user1 : Admin.getUsers()) {
            // Check if there are subscribers
            if (!this.getSubscribers().isEmpty()) {
                // Iterate through each subscriber
                for (String nameSub : this.getSubscribers()) {
                    // Notify the subscriber if their username matches
                    if (user1.getUsername().equals(nameSub)) {
                        // Create a notification object
                        ObjectNode notification = objectMapper.createObjectNode();
                        notification.put("name", "New Merchandise");
                        notification.put("description", "New Merchandise from "
                                + this.getName() + ".");

                        // Update the subscriber with the notification
                        user1.update(notification);
                    }
                }
            }
        }
    }

    /**
     * Notifies all subscribers about a new album from the observable object.
     */
    @Override
    public void notifyObserversAlbum() {
        for (User user1 : Admin.getUsers()) {
            // Check if there are subscribers
            if (!this.getSubscribers().isEmpty()) {
                // Iterate through each subscriber
                for (String nameSub : this.getSubscribers()) {
                    // Notify the subscriber if their username matches
                    if (user1.getUsername().equals(nameSub)) {
                        // Create a notification object
                        ObjectNode notification = objectMapper.createObjectNode();
                        notification.put("name", "New Album");
                        notification.put("description", "New Album from " + this.getName() + ".");

                        // Update the subscriber with the notification
                        user1.update(notification);
                    }
                }
            }
        }
    }



    /**
     * Represents an event associated with an artist,
     * including details such as name, description, and date.
     */
    public static class Event {
        private String artist;
        @Getter
        private String name;
        @Getter
        private String description;
        @Getter
        private String date;
        private static final int MONTH_LIMIT = 12;
        private static final int DAY_LIMIT = 31;
        private static final int YEAR_UPPER_LIMIT = 2023;
        private static final int YEAR_LOWER_LIMIT = 1900;
        private static final int FEB_LIMIT = 28;

        public Event(final String artist, final String name,
                     final String description, final String date) {
            this.artist = artist;
            this.name = name;
            this.date = date;
            this.description = description;
        }

        /**
         * Checks if the provided date is valid for an event.
         *
         * @param date The date to validate.
         * @return True if the date is valid, false otherwise.
         */
        public static boolean validDate(final String date) {
            try {
                // Define the date format as "dd-MM-yyyy"
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

                // Parse the input date string into a LocalDate object
                LocalDate localDate = LocalDate.parse(date, formatter);

                // Extract day, month, and year from the LocalDate object
                int day = localDate.getDayOfMonth();
                int month = localDate.getMonthValue();
                int year = localDate.getYear();

                // Validate February: if the month is February,
                // ensure the day is not greater than 28
                if (month == 2 && day > FEB_LIMIT) {
                    return false;
                }

                // Validate month (1 to 12), day (1 to 31), and year (1900 to 2023)
                return month >= 1 && month <= MONTH_LIMIT && day >= 1 && day <= DAY_LIMIT
                        && year >= YEAR_LOWER_LIMIT && year <= YEAR_UPPER_LIMIT;
            } catch (Exception e) {
                // Catch any exceptions during the parsing or validation process and return false
                return false;
            }
        }
    }

    /**
     * Represents merchandise items associated with an artist,
     * including details such as name, description, and price.
     */
    public static class Merch {
        private String artist;
        @Getter
        private String name;
        @Getter
        private String description;
        @Getter
        private int price;

        public Merch(final String artist, final String name,
                     final String description, final int price) {
            this.artist = artist;
            this.name = name;
            this.description = description;
            this.price = price;
        }

    }
}
