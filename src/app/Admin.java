package app;

import app.audio.Collections.Album;
import app.audio.Collections.Playlist;
import app.audio.Collections.Podcast;
import app.audio.Files.Episode;
import app.audio.Files.Song;
import app.player.Player;
import app.player.PlayerSource;
import app.user.Artist;
import app.user.Host;
import app.user.User;
import app.utils.Enums;
import app.utils.UserVisitor;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.CommandInput;
import fileio.input.EpisodeInput;
import fileio.input.PodcastInput;
import fileio.input.SongInput;
import fileio.input.UserInput;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static app.user.Artist.objectMapper;


/**
 * The type Admin.
 */
public final class Admin {
    @Getter
    private static List<User> users = new ArrayList<>();
    @Getter
    private static List<Artist> artists = new ArrayList<>();
    @Getter
    private static List<Host> hosts = new ArrayList<>();
    private static List<Song> songs = new ArrayList<>();
    private static List<Podcast> podcasts = new ArrayList<>();
    private static List<Album> albums = new ArrayList<>();
    private static int timestamp = 0;
    private static final int TOP_LIMIT = 5;

    // Private constructor to prevent instantiation
    private Admin() {
    }

    /**
     * Update the list of songs.
     *
     * @param songsUpdate The updated list of songs.
     */
    public static void updateSongList(final List<Song> songsUpdate) {
        songs = songsUpdate;
    }

    /**
     * Update the list of albums.
     *
     * @param albumsUpdate The updated list of albums.
     */
    public static void updateAlbumList(final List<Album> albumsUpdate) {
        albums = albumsUpdate;
    }

    /**
     * Update the list of podcasts.
     *
     * @param podcastsUpdate The updated list of podcasts.
     */
    public static void updatePodcastList(final List<Podcast> podcastsUpdate) {
        podcasts = podcastsUpdate;
    }

    /**
     * Sets users.
     *
     * @param userInputList the user input list
     */
    public static void setUsers(final List<UserInput> userInputList) {
        users = new ArrayList<>();
        for (UserInput userInput : userInputList) {
            users.add(new User(userInput.getUsername(),
                    userInput.getAge(), userInput.getCity(),
                    "user"));
        }
    }

    /**
     * Sets songs.
     *
     * @param songInputList the song input list
     */
    public static void setSongs(final List<SongInput> songInputList) {
        songs = new ArrayList<>();
        for (SongInput songInput : songInputList) {
            songs.add(new Song(songInput.getName(), songInput.getDuration(), songInput.getAlbum(),
                    songInput.getTags(), songInput.getLyrics(), songInput.getGenre(),
                    songInput.getReleaseYear(), songInput.getArtist()));
        }
    }

    /**
     * Sets podcasts.
     *
     * @param podcastInputList the podcast input list
     */
    public static void setPodcasts(final List<PodcastInput> podcastInputList) {
        podcasts = new ArrayList<>();
        for (PodcastInput podcastInput : podcastInputList) {
            List<Episode> episodes = new ArrayList<>();
            for (EpisodeInput episodeInput : podcastInput.getEpisodes()) {
                episodes.add(new Episode(episodeInput.getName(),
                        episodeInput.getDuration(),
                        episodeInput.getDescription()));
            }
            podcasts.add(new Podcast(podcastInput.getName(), podcastInput.getOwner(), episodes));
        }
    }

    /**
     * Gets songs.
     *
     * @return the songs
     */
    public static List<Song> getSongs() {
        return new ArrayList<>(songs);
    }

    /**
     * Gets albums.
     *
     * @return the albums
     */
    public static List<Album> getAlbums() {
        List<Album> albums = new ArrayList<>();
        for (User user : users) {
            albums.addAll(user.getAlbums());
        }
        return albums;
    }


    /**
     * Gets podcasts.
     *
     * @return the podcasts
     */
    public static List<Podcast> getPodcasts() {
        return new ArrayList<>(podcasts);
    }

    /**
     * Gets playlists.
     *
     * @return the playlists
     */
    public static List<Playlist> getPlaylists() {
        List<Playlist> playlists = new ArrayList<>();
        for (User user : users) {
            playlists.addAll(user.getPlaylists());
        }
        return playlists;
    }

    /**
     * Gets user.
     *
     * @param username the username
     * @return the user
     */
    public static User getUser(final String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public static Song getSong(final String songName) {
        for (Song song : songs) {
            if (song.getName().equals(songName)) {
                return song;
            }
        }
        return null;
    }

    public static Podcast getPodcast(final String podcastName) {
        for (Podcast podcast : podcasts) {
            if (podcast.getName().equals(podcastName)) {
                return podcast;
            }
        }
        return null;
    }

    /**
     * Gets artist.
     *
     * @param username the username
     * @return the artist
     */
    public static Artist getArtist(final String username) {
        for (Artist artist : artists) {
            if (artist.getName().equals(username)) {
                return artist;
            }
        }
        return null;
    }

    /**
     * Gets host.
     *
     * @param username the username
     * @return the host
     */
    public static Host getHost(final String username) {
        for (Host host : hosts) {
            if (host.getName().equals(username)) {
                return host;
            }
        }
        return null;
    }

    /**
     * Update timestamp.
     *
     * @param newTimestamp the new timestamp
     */
    public static void updateTimestamp(final int newTimestamp) {
        int elapsed = newTimestamp - timestamp;
        timestamp = newTimestamp;
        if (elapsed == 0) {
            return;
        }

        for (User user : users) {
            if (user.getConnectionStatus().equals(Enums.ConnectionStatus.ONLINE)) {
                user.simulateTime(elapsed);
            }
        }
    }

    /**
     * Gets top 5 songs.
     *
     * @return the top 5 songs
     */
    public static List<String> getTop5Songs() {
        List<Song> sortedSongs = new ArrayList<>(songs);
        sortedSongs.sort(Comparator.comparingInt(Song::getLikes).reversed());
        List<String> topSongs = new ArrayList<>();
        int count = 0;
        for (Song song : sortedSongs) {
            if (count >= TOP_LIMIT) {
                break;
            }
            topSongs.add(song.getName());
            count++;
        }
        return topSongs;
    }

    /**
     * Gets top 5 playlists.
     *
     * @return the top 5 playlists
     */
    public static List<String> getTop5Playlists() {
        List<Playlist> sortedPlaylists = new ArrayList<>(getPlaylists());
        sortedPlaylists.sort(Comparator.comparingInt(Playlist::getFollowers)
                .reversed()
                .thenComparing(Playlist::getTimestamp, Comparator.naturalOrder()));
        List<String> topPlaylists = new ArrayList<>();
        int count = 0;
        for (Playlist playlist : sortedPlaylists) {
            if (count >= TOP_LIMIT) {
                break;
            }
            topPlaylists.add(playlist.getName());
            count++;
        }
        return topPlaylists;
    }

    /**
     * Gets top 5 albums.
     *
     * @return the top 5 albums
     */
    public static List<String> getTop5Albums() {
        List<Album> sortedAlbums = new ArrayList<>(getAlbums());
        sortedAlbums.sort(Comparator.comparingInt(Album::getNumberLikes).reversed());
        List<String> topAlbums = new ArrayList<>();
        int count = 0;
        for (Album album : sortedAlbums) {
            if (count >= TOP_LIMIT) {
                break;
            }
            topAlbums.add(album.getName());
            count++;
        }
        return topAlbums;
    }

    /**
     * Gets top 5 artists byt he total number of likes.
     *
     * @return the top 5 artists
     */
    public static List<String> getTop5Artists() {
        List<Artist> sortedArtists = new ArrayList<>(getArtists());
        sortedArtists.sort(Comparator.comparingInt(Artist::getNumberLikes).reversed());
        List<String> topArtists = new ArrayList<>();
        int count = 0;
        for (Artist artist : sortedArtists) {
            if (count >= TOP_LIMIT) {
                break;
            }
            topArtists.add(artist.getName());
            count++;
        }
        return topArtists;
    }

    /**
     * Adds a new user, artist, or host based on the provided command input.
     *
     * @param commandInput The input containing user details and type.
     * @return A message indicating the success or failure of the operation.
     */
    public static String addUser(final CommandInput commandInput) {
        for (User user : users) {
            if (user.getUsername().equals(commandInput.getUsername())) {
                return "The username " + user.getUsername() + " is already taken.";
            }
        }
        User newUser;
        if (commandInput.getType().equals("user")) {
            newUser = new User(commandInput.getUsername(),
                    commandInput.getAge(), commandInput.getCity(),
                    "user");
            users.add(newUser);
        }
        if (commandInput.getType().equals("artist")) {
            newUser = new User(commandInput.getUsername(),
                    commandInput.getAge(), commandInput.getCity(),
                    "artist");
            addArtist(commandInput, newUser);
        }
        if (commandInput.getType().equals("host")) {
            newUser = new User(commandInput.getUsername(),
                    commandInput.getAge(),
                    commandInput.getCity(), "host");
            addHost(commandInput, newUser);
        }
        return "The username " + commandInput.getUsername() + " has been added successfully.";
    }

    /**
     * Add an artist to the artists list
     * Sets the connectionStatus to offline
     *
     * @param commandInput
     * @param newUser
     */
    public static void addArtist(final CommandInput commandInput, final User newUser) {
        newUser.setConnectionStatus(Enums.ConnectionStatus.OFFLINE);
        users.add(newUser);
        int ok = 1;

        // Check if the artist already exists in the list
        for (Artist artist : artists) {
            if (artist.getName().equals(commandInput.getUsername())) {
                ok = 0;
            }
        }
        if (ok == 1) {
            artists.add(new Artist(commandInput.getUsername(),
                    commandInput.getAge(), commandInput.getCity(),
                    commandInput.getType()));
        }
    }

    /**
     * Add a host to the artists list
     * Sets the connectionStatus to offline
     *
     * @param commandInput
     * @param newUser
     */
    public static void addHost(final CommandInput commandInput, final User newUser) {
        newUser.setConnectionStatus(Enums.ConnectionStatus.OFFLINE);
        users.add(newUser);

        //Check if the host already exists
        int ok = 1;
        for (Host host : hosts) {
            if (host.getName().equals(commandInput.getUsername())) {
                ok = 0;
            }
        }
        if (ok == 1) {
            hosts.add(new Host(commandInput.getUsername(),
                    commandInput.getAge(), commandInput.getCity(),
                    commandInput.getType()));
        }
    }

    /**
     * Deletes a user, artist, or host based on the provided command input.
     *
     * @param commandInput The input containing the username and type.
     * @return A message indicating the success or failure of the operation.
     */
    public static String deleteUser(final CommandInput commandInput) {
        if (getUser(commandInput.getUsername()) == null) {
            return "The username " + commandInput.getUsername() + " doesn't exist.";
        }
        String type = new String();
        for (User user : users) {
            if (commandInput.getUsername().equals(user.getUsername())) {
                type = user.getUserType();
                break;
            }
        }
        Deletion deletion = new Deletion(commandInput);
        String message = new String();
        if (type.equals("artist")) {
            message = getArtist(commandInput.getUsername()).acceptVisitor(deletion);

        }

        if (type.equals("host")) {
            message = getHost(commandInput.getUsername()).acceptVisitor(deletion);
        }

        if (type.equals("user")) {
            message = getUser(commandInput.getUsername()).acceptVisitor(deletion);
        }
        return message;
    }

    public static class Deletion implements UserVisitor {

        private final CommandInput commandInput;

        public Deletion(final CommandInput commandInput) {
            this.commandInput = commandInput;
        }

        /**
         * Deletes an artist, including associated albums and songs.
         * Updates playlists and followers accordingly.
         *
         * @return A message indicating the success or failure of the operation.
         */
        public String deleteUser(final Artist artist) {
            User userToDelete = getUser(commandInput.getUsername());
            //Artist artist = getArtist(commandInput.getUsername());

            // Check if artist is referenced in any user's player or current page
            for (User user : getUsers()) {
                Player player = User.getPlayerInstance(user);
                PlayerSource source = Player.getPlayerSourceInstance(player);
                if (source != null) {
                    // Check if artist's albums or songs are currently being played
                    for (Album album : artist.getAlbums()) {
                        if (source.getAudioCollection() != null
                                && source.getAudioCollection().getName()
                                .equals(album.getName())) {
                            return commandInput.getUsername() + " can't be deleted.";
                        }

                        if (source.getAudioFile() != null) {
                            for (Song song : album.getSongs()) {
                                if (source.getAudioFile().getName().equals(song.getName())) {
                                    return commandInput.getUsername() + " can't be deleted.";
                                }
                            }
                        }
                    }
                }

                // Check if artist is the current page for any user
                if (user.getCurrentPage().equals(Enums.currentPage.ARTIST)) {
                    return commandInput.getUsername() + " can't be deleted.";
                }
            }

            // Remove artist's songs from the system
            for (Album album : artist.getAlbums()) {
                List<Song> songsToRemove = Admin.getSongs();
                songsToRemove.removeAll(album.getSongs());
                Admin.updateSongList(songsToRemove);
            }

            // Update playlists of all users to remove artist's songs in the album
            for (User user : Admin.getUsers()) {
                for (Playlist playlist : user.getPlaylists()) {
                    List<Song> songsInPlaylist = playlist.getSongs();

                    // Remove artist's albums from playlists
                    for (Album album : artist.getAlbums()) {
                        songsInPlaylist.removeAll(album.getSongs());
                        playlist.getSongs().removeAll(album.getSongs());
                    }
                }

                // Remove liked songs associated with the artist
                user.getLikedSongs().removeIf(song -> song.getArtist()
                        .equals(commandInput.getUsername()));
            }

            // Remove artist from the system
            users.remove(userToDelete);
            artists.remove(artist);
            return commandInput.getUsername() + " was successfully deleted.";
        }

        /**
         * Deletes a host, including associated podcasts.
         *
         * @return A message indicating the success or failure of the operation.
         */
        public String deleteUser(final Host host) {
            User userToDelete = getUser(commandInput.getUsername());
            //Host host = getHost(commandInput.getUsername());

            // Check if host is referenced in any user's player or current page
            for (User user : getUsers()) {
                Player player = User.getPlayerInstance(user);
                PlayerSource source = Player.getPlayerSourceInstance(player);

                if (source != null) {
                    // Check if host's podcasts are currently being played
                    for (Podcast podcast : userToDelete.getPodcasts()) {
                        if (source.getAudioCollection() != null
                                && source.getAudioCollection().getName()
                                .equals(podcast.getName())) {
                            return commandInput.getUsername() + " can't be deleted.";
                        }
                    }
                }

                // Check if host is the current page for any user
                if (user.getCurrentPage().equals(Enums.currentPage.HOST)) {
                    return commandInput.getUsername() + " can't be deleted.";
                }
            }

            // Remove host from the system
            users.remove(userToDelete);
            hosts.remove(host);
            return commandInput.getUsername() + " was successfully deleted.";
        }

        /**
         * Deletes a normal user, updating playlists and followers.
         *
         * @return A message indicating the success or failure of the operation.
         */
        public String deleteUser(final User userToDelete) {
            //User userToDelete = getUser(commandInput.getUsername());

            // Check if user is referenced in any user's player or current page
            for (User user : users) {
                Player player = User.getPlayerInstance(user);
                PlayerSource source = Player.getPlayerSourceInstance(player);

                if (source != null) {
                    // Check if user's playlists are currently being played
                    for (Playlist playlist : userToDelete.getPlaylists()) {
                        if (source.getAudioCollection() != null
                                && source.getAudioCollection().getName()
                                .equals(playlist.getName())) {
                            return commandInput.getUsername() + " can't be deleted.";
                        }
                    }
                }
            }

            // Update playlists of all users to remove user's playlists
            for (User user : users) {
                user.getFollowedPlaylists().removeIf(playlist ->
                        playlist.getOwner().equals(commandInput.getUsername()));
            }

            // Update followers count for playlists followed by the deleted user
            for (Playlist playlist : userToDelete.getFollowedPlaylists()) {
                playlist.setFollowers(playlist.getFollowers() - 1);
            }

            // Remove user from the system
            users.remove(userToDelete);
            return commandInput.getUsername() + " was successfully deleted.";
        }
    }

    /**
     * Retrieves a list of usernames for online users.
     *
     * @return A list of usernames for online users.
     */
    public static List<String> getOnlineUsers() {
        List<String> onlineUsers = new ArrayList<>();
        for (User user : users) {
            if (user.getConnectionStatus().equals(Enums.ConnectionStatus.ONLINE)) {
                onlineUsers.add(user.getUsername());
            }
        }
        return onlineUsers;
    }

    /**
     * Comparator for sorting users based on user type.
     */
    public static class UserComparatorByType implements Comparator<User> {
        private List<String> userTypeOrder = List.of("user", "artist", "host");

        /**
         * Compares two users based on their user types, following a predefined order.
         * Users are sorted in the order: "user" < "artist" < "host".
         *
         * @param user1 The first user to compare.
         * @param user2 The second user to compare.
         * @return A negative integer, zero, or a positive integer based on user type.
         */
        @Override
        public int compare(final User user1, final User user2) {
            String userType1 = user1.getUserType();
            String userType2 = user2.getUserType();

            return userTypeOrder.indexOf(userType1) - userTypeOrder.indexOf(userType2);
        }
    }

    /**
     * Retrieves a sorted list of all usernames based on user type.
     *
     * @return A sorted list of all usernames.
     */
    public static ArrayList<String> getAllUsers() {
        users.sort(new UserComparatorByType());

        ArrayList<String> result = new ArrayList<>();
        for (User user : users) {
            result.add(user.getUsername());
        }

        return result;
    }

    public static ObjectNode endProgram() {
        ObjectNode result = objectMapper.createObjectNode();
        ArrayList<Artist> rankedArtists = new ArrayList<>();
        for (Artist artist : getArtists()) {
            if (artist.getMerchRevenue() != 0 || artist.getSongRevenue() != 0 || artist.isWasPlayed()) {
                rankedArtists.add(artist);
            }
        }
        rankedArtists.sort((a1, a2) -> {
            if (a1.getMerchRevenue() != 0 && a2.getMerchRevenue() != 0) {
                // Sort by merchRevenue in descending order
                return Double.compare(a2.getMerchRevenue(), a1.getMerchRevenue());
            } else {
                // Sort alphabetically
                return a1.getName().compareToIgnoreCase(a2.getName());
            }
        });

        int ranking = 1;
        for (Artist artist : rankedArtists) {
            ObjectNode artistNode = objectMapper.createObjectNode();
            artistNode.put("merchRevenue", artist.getMerchRevenue());
            artistNode.put("songRevenue", artist.getSongRevenue());
            artistNode.put("ranking", ranking++);
            artistNode.put("mostProfitableSong", "N/A");

            // Add the artist node to the array
            result.putPOJO(artist.getName(), artistNode);
        }

        return result;
    }

    /**
     * Reset.
     */
    public static void reset() {
        users = new ArrayList<>();
        songs = new ArrayList<>();
        podcasts = new ArrayList<>();
        artists = new ArrayList<>();
        hosts = new ArrayList<>();
        timestamp = 0;
    }
}
