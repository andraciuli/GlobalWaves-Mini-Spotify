package app.user;

import app.Admin;
import app.audio.Collections.Podcast;
import app.audio.Collections.Album;
import app.audio.Collections.Playlist;
import app.audio.Collections.AudioCollection;
import app.audio.Collections.PlaylistOutput;
import app.audio.Files.AudioFile;
import app.audio.Files.Episode;
import app.audio.Files.Song;
import app.audio.LibraryEntry;
import app.observer.Observer;
import app.pageSystem.PageCommands;
import app.player.Player;
import app.player.PlayerSource;
import app.player.PlayerStats;
import app.searchBar.Filters;
import app.searchBar.SearchBar;
import app.utils.Enums;
import app.utils.UserVisitable;
import app.wrapped.EpisodeWrapp;
import app.wrapped.SongWrapp;
import app.wrapped.UserWrapp;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.CommandInput;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * The type User.
 */
public class User implements UserVisitable, Observer {
    @Getter
    private String username;
    @Getter
    private int age;
    @Getter
    private String city;
    @Getter
    private ArrayList<Playlist> playlists;
    @Getter
    @Setter
    private ArrayList<Song> likedSongs;
    @Getter
    private ArrayList<Playlist> followedPlaylists;
    @Getter
    private final Player player;
    private final SearchBar searchBar;
    private boolean lastSearched;
    @Getter
    @Setter
    private Enums.ConnectionStatus connectionStatus;
    @Getter
    private String userType;
    @Getter
    private ArrayList<Album> albums;
    @Getter
    @Setter
    private Enums.currentPage currentPage;
    @Getter
    @Setter
    private Enums.currentPage previousPage;
    @Getter
    @Setter
    private Enums.currentPage nextPage;
    @Getter
    private String lastSearchType;
    @Getter
    private ArrayList<Podcast> podcasts;
    @Getter
    private String select;
    @Getter
    private ArrayList<String> merches;
    @Getter
    private ArrayList<String> subscribedTo;
    @Getter
    @Setter
    private ArrayList<ObjectNode> notifications = new ArrayList<>();
    @Getter
    private ArrayList<SongWrapp> listenedSongs;
    @Getter
    @Setter
    private String songRecommandation;
    @Getter
    @Setter
    private ArrayList<Song> playlistRecommandation;
    @Getter
    @Setter
    private String playlistRecommandationName;
    @Getter
    @Setter
    private ArrayList<Enums.currentPage> istoricPages;
    @Getter
    private ArrayList<EpisodeWrapp> listenedEpisodes;
    @Getter
    private ArrayList<Artist> artists;
    private static final int TOP_LIMIT = 5;
    @Getter
    private int loadTime;
    @Getter
    private int searchedTime;

    /**
     * Instantiates a new User.
     *
     * @param username the username
     * @param age      the age
     * @param city     the city
     */
    public User(final String username, final int age, final String city, final String userType) {
        this.username = username;
        this.age = age;
        this.city = city;
        this.userType = userType;
        playlists = new ArrayList<>();
        likedSongs = new ArrayList<>();
        followedPlaylists = new ArrayList<>();
        player = new Player();
        searchBar = new SearchBar(username);
        lastSearched = false;
        this.connectionStatus = Enums.ConnectionStatus.ONLINE;
        albums = new ArrayList<>();
        currentPage = Enums.currentPage.HOME;
        previousPage = Enums.currentPage.DEFAULT;
        nextPage = Enums.currentPage.DEFAULT;
        lastSearchType = new String();
        podcasts = new ArrayList<>();
        merches = new ArrayList<>();
        subscribedTo = new ArrayList<>();
        notifications = new ArrayList<>();
        listenedSongs = new ArrayList<>();
        playlistRecommandation = new ArrayList<>();
        playlistRecommandationName = new String();
        songRecommandation = new String();
        istoricPages = new ArrayList<>();
        listenedEpisodes = new ArrayList<>();
        artists = new ArrayList<>();
    }

    /**
     * Buys a merch item for the user from the selected artist's collection.
     *
     * @param commandInput The command input specifying the username and merch details.
     * @return A message indicating the success or failure of the merch purchase.
     */
    public static String buyMerch(final CommandInput commandInput) {
        // Check if the user exists
        if (Admin.getUser(commandInput.getUsername()) == null) {
            return "The username " + commandInput.getUsername() + " doesn't exist.";
        }

        User user = Admin.getUser(commandInput.getUsername());

        // Check if the user is on the artist's page
        if (!user.getCurrentPage().equals(Enums.currentPage.ARTIST)) {
            return "Cannot buy merch from this page.";
        }

        Artist artist = Admin.getArtist(user.select);

        // Attempt to find and buy the specified merch item
        for (Artist.Merch merch : artist.getMerches()) {
            if (merch.getName().equals(commandInput.getName())) {
                double merchRevenue = artist.getMerchRevenue();
                merchRevenue += merch.getPrice();
                artist.setMerchRevenue(merchRevenue);
                user.merches.add(commandInput.getName());
                return commandInput.getUsername() + " has added new merch successfully.";
            }
        }

        return "The merch " + commandInput.getName() + " doesn't exist.";
    }

    /**
     * Retrieves the list of merch items owned by the user.
     *
     * @param commandInput The command input specifying the username.
     * @return The list of merch items owned by the user.
     */
    public static ArrayList<String> seeMerch(final CommandInput commandInput) {
        User user = Admin.getUser(commandInput.getUsername());
        return user.merches;
    }

    /**
     * Subscribes or unsubscribes the user to/from an artist or host.
     *
     * @param commandInput The command input specifying the subscription details.
     * @return A message indicating the success or failure of the subscription action.
     */
    public static String subscribe(final CommandInput commandInput) {
        // Check if the user exists
        if (Admin.getUser(commandInput.getUsername()) == null) {
            return "The username " + commandInput.getUsername() + " doesn't exist.";
        }

        User user = Admin.getUser(commandInput.getUsername());

        // Check if the user is on the artist or host page
        if (!user.getCurrentPage().equals(Enums.currentPage.ARTIST)
                && !user.getCurrentPage().equals(Enums.currentPage.HOST)) {
            return "To subscribe you need to be on the page of an artist or host.";
        }

        String unsubscribe = null;

        // Check if the user is already subscribed to the current artist or host
        if (!user.getSubscribedTo().isEmpty()) {
            for (String name : user.subscribedTo) {
                if (name.equals(user.select)) {
                    unsubscribe = name;
                }
            }
        }

        Artist artist = Admin.getArtist(user.select);

        // Unsubscribe the user if already subscribed, otherwise subscribe
        if (unsubscribe != null) {
            user.getSubscribedTo().remove(unsubscribe);
            ArrayList<String> subscribers = artist.getSubscribers();
            subscribers.remove(user.getUsername());
            artist.setSubscribers(subscribers);
            return commandInput.getUsername() + " unsubscribed from "
                    + user.select + " successfully.";
        }

        user.getSubscribedTo().add(user.select);
        ArrayList<String> subscribers = new ArrayList<>();
        subscribers = artist.getSubscribers();
        subscribers.add(user.getUsername());
        artist.setSubscribers(subscribers);
        return commandInput.getUsername() + " subscribed to " + user.select + " successfully.";
    }

    @Override
    public void update(final ObjectNode notification) {
        notifications.add(notification);
    }

    /**
     * Retrieves the notifications for the user and clears the notification list.
     *
     * @param commandInput The command input specifying the username.
     * @return The list of notifications for the user.
     */
    public static ArrayList<ObjectNode> showNotifications(final CommandInput commandInput) {
        User user = Admin.getUser(commandInput.getUsername());
        ArrayList<ObjectNode> notifications = user.getNotifications();
        user.setNotifications(new ArrayList<>());
        return notifications;
    }

    /**
     * Updates the list of songs that the user has listened to based on the current playing song.
     */
    public void updateListenSongs() {
        PlayerSource source = Player.getPlayerSourceInstance(player);

        String songName = new String();
        Song songOnListen = null;

        if (source != null) {
            if (source.getAudioFile() != null) {
                songName = source.getAudioFile().getName();
            }
        }

        // Find the song being listened to in the list of available songs
        for (Song song : Admin.getSongs()) {
            if (song.getName().equals(songName)) {
                songOnListen = song;
                break;
            }
        }

        int containsSong = 1;
        int containsUser = 1;

        if (songOnListen != null) {
            // Check if the user has already listened to this song
            for (SongWrapp song : listenedSongs) {
                if (songOnListen.getName().equals(song.getSong().getName())) {
                    song.incrementListens();
                    break;
                } else {
                    containsSong = 0;
                }
            }

            // If the user hasn't listened to this song, add it to the list
            if (containsSong == 0) {
                listenedSongs.add(new SongWrapp(songOnListen));
            }

            // Update listeners and set 'wasPlayed' flag for the artist
            for (Artist artist : Admin.getArtists()) {
                if (songOnListen.getArtist().equals(artist.getName())) {
                    for (UserWrapp user : artist.getListeners()) {
                        if (user.getUser().equals(this)) {
                            user.incrementListens();
                            break;
                        } else {
                            containsUser = 0;
                        }
                    }
                    if (containsUser == 0) {
                        artist.getListeners().add(new UserWrapp(this));
                    }
                    if (!artist.isWasPlayed()) {
                        artist.setWasPlayed(true);
                    }
                }
            }
        }
    }

    /**
     * Updates the list of episodes that the user has listened to based
     * on the current playing podcast episode.
     */
    public void updateListenPodcast() {
        PlayerSource source = Player.getPlayerSourceInstance(player);

        String name = new String();
        String nameEpisode = new String();
        Podcast podcastOnListen = null;
        Episode episodeOnListen = null;

        if (source != null) {
            if (source.getAudioCollection() != null) {
                name = source.getAudioCollection().getName();
            }
        }

        // Find the podcast being listened to in the list of available podcasts
        for (Podcast podcast : Admin.getPodcasts()) {
            if (name.equals(podcast.getName())) {
                podcastOnListen = podcast;
            }
        }

        if (source != null) {
            if (source.getAudioFile() != null) {
                nameEpisode = source.getAudioFile().getName();
            }
        }

        // Find the episode being listened to in the list of available episodes
        if (podcastOnListen != null) {
            for (Episode episode : podcastOnListen.getEpisodes()) {
                if (episode.getName().equals(nameEpisode)) {
                    episodeOnListen = episode;
                    break;
                }
            }
        }

        boolean containsEpisode = false;

        if (episodeOnListen != null) {
            // Check if the user has already listened to this episode
            for (EpisodeWrapp episode : listenedEpisodes) {
                if (nameEpisode.equals(episode.getEpisode().getName())) {
                    episode.incrementListens();
                    containsEpisode = true;
                    break;
                }
            }

            // If the user hasn't listened to this episode, add it to the list
            if (!containsEpisode) {
                listenedEpisodes.add(new EpisodeWrapp(episodeOnListen));
            }

            // Update listeners for the podcast and host
            for (Host host : Admin.getHosts()) {
                if (podcastOnListen.getOwner().equals(host.getName())) {
                    if (!host.containsListener(this.username)) {
                        host.incrementListens();
                        host.getListeners().add(new UserWrapp(this));
                    }
                    boolean hostContainsEpisode = false;
                    for (EpisodeWrapp episode : host.getListenedEpisodes()) {
                        if (episode.getEpisode().getName().equals(nameEpisode)) {
                            episode.incrementListens();
                            hostContainsEpisode = true;
                            break;
                        }
                    }
                    if (!hostContainsEpisode) {
                        host.getListenedEpisodes().add(new EpisodeWrapp(episodeOnListen));
                    }
                }
            }
            if ((searchedTime - loadTime) > getPlayerStats(this).getRemainedTime()) {
                int index = podcastOnListen.getEpisodes().indexOf(episodeOnListen);
                if (index >= 1) {
                    Episode previousEpisode = podcastOnListen.getEpisodes().get(index - 1);
                    for (EpisodeWrapp episode : listenedEpisodes) {
                        if (previousEpisode.getName().equals(episode.getEpisode().getName())) {
                            episode.incrementListens();
                            break;
                        }
                    }
                    incrementHostListens(previousEpisode.getName(), podcastOnListen);
                }
            }
        }
    }

    /**
     * Increments the listen count for a specific episode of a podcast associated with a given host.
     *
     * This method iterates through the list of hosts managed by the Admin class, finds the host
     * who owns the podcast currently being listened to, and increments the listen count for the
     * specified episode if it is found in the host's listened episodes list.
     *
     * @param episodeName The name of the episode for which the listen count should be incremented.
     * @param podcastOnListen The Podcast object representing the podcast currently
     *                        being listened to.
     */
    public void incrementHostListens(final String episodeName, final Podcast podcastOnListen) {
        for (Host host : Admin.getHosts()) {
            if (podcastOnListen.getOwner().equals(host.getName())) {
                for (EpisodeWrapp episode : host.getListenedEpisodes()) {
                    if (episode.getEpisode().getName()
                            .equals(episodeName)) {
                        episode.incrementListens();
                        break;
                    }
                }
            }
        }
    }


    /**
     * Retrieves the top 5 listened episodes by the user.
     *
     * @return The list of top 5 listened episodes.
     */
    public ArrayList<EpisodeWrapp> top5listenedEpisodes() {
        // Sort the episodes based on the number of listens in descending order
        Collections.sort(listenedEpisodes, Comparator
                .comparingInt(EpisodeWrapp::getListens)
                .reversed()
                .thenComparing(episodeWrapp -> episodeWrapp.getEpisode().getName()));

        // Create a list to store the top 5 episodes
        ArrayList<EpisodeWrapp> top5Episodes = new ArrayList<>();

        // Add up to the first 5 episodes to the list
        for (int i = 0; i < Math.min(TOP_LIMIT, listenedEpisodes.size()); i++) {
            top5Episodes.add(listenedEpisodes.get(i));
        }
        return top5Episodes;
    }

    /**
     * Retrieves the top 5 liked songs by the user.
     *
     * @return The list of top 5 liked songs.
     */
    public ArrayList<Song> top5LikedSongs() {
        Collections.sort(likedSongs, Comparator.comparingInt(Song::getLikes).reversed());

        // Create a list to store the top 5 liked songs
        ArrayList<Song> top5LikedSongsList = new ArrayList<>();

        // Add up to the first 5 liked songs to the list
        for (int i = 0; i < Math.min(TOP_LIMIT, likedSongs.size()); i++) {
            top5LikedSongsList.add(likedSongs.get(i));
        }

        return top5LikedSongsList;
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
     * Retrieves the player instance associated with the specified user.
     *
     * @param user The user for whom the player instance is retrieved.
     * @return The player instance associated with the user.
     */
    public static Player getPlayerInstance(final User user) {
        return user.getPlayer();
    }

    /**
     * Toggles the connection status of the user between online and offline.
     * If the connection status changes to OFFLINE and
     * the player is currently playing an audio file,
     * it pauses the playback.
     *
     * @return A message indicating the success of the status change.
     */
    public String switchConnectionStatus() {
        if (connectionStatus.equals(Enums.ConnectionStatus.ONLINE)) {
            connectionStatus = Enums.ConnectionStatus.OFFLINE;
        } else {
            connectionStatus = Enums.ConnectionStatus.ONLINE;
        }
        return username + " has changed status successfully.";
    }

    /**
     * Search array list.
     *
     * @param filters the filters
     * @param type    the type
     * @return the array list
     */
    public ArrayList<String> search(final Filters filters, final String type,
                                    final CommandInput commandInput) {
        searchBar.clearSelection();
        searchedTime = commandInput.getTimestamp();
        updateListenPodcast();
        player.stop();

        lastSearched = true;
        lastSearchType = type;
        ArrayList<String> results = new ArrayList<>();
        List<LibraryEntry> libraryEntries = searchBar.search(filters, type);

        for (LibraryEntry libraryEntry : libraryEntries) {
            results.add(libraryEntry.getName());
        }
        return results;
    }

    /**
     * Select string.
     *
     * @param itemNumber the item number
     * @return the string
     */
    public String select(final int itemNumber) {
        if (!lastSearched) {
            return "Please conduct a search before making a selection.";
        }

        lastSearched = false;


        LibraryEntry selected = searchBar.select(itemNumber);

        if (selected == null) {
            return "The selected ID is too high.";
        }
        select = selected.getName();
        if (searchBar.getLastSearchType().equals("artist")) {
            setCurrentPage(Enums.currentPage.ARTIST);

            PageCommands.setSelectedArtist(selected.getName());
            return "Successfully selected " + selected.getName() + "'s page.";
        }

        if (searchBar.getLastSearchType().equals("host")) {
            currentPage = Enums.currentPage.HOST;

            PageCommands.setSelectedHost(selected.getName());
            return "Successfully selected " + selected.getName() + "'s page.";
        }

        return "Successfully selected %s.".formatted(selected.getName());
    }

    /**
     * Load string.
     *
     * @return the string
     */
    public String load(final CommandInput commandInput) {
        if (searchBar.getLastSelected() == null) {
            return "Please select a source before attempting to load.";
        }
        loadTime = commandInput.getTimestamp();

        if (!searchBar.getLastSearchType().equals("song")
                && ((AudioCollection) searchBar.getLastSelected())
                .getNumberOfTracks() == 0) {
            return "You can't load an empty audio collection!";
        }

        player.setSource(searchBar.getLastSelected(), searchBar.getLastSearchType());
        updateListenSongs();
        searchBar.clearSelection();

        player.pause();

        return "Playback loaded successfully.";
    }

    /**
     * Play pause string.
     *
     * @return the string
     */
    public String playPause() {
        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before attempting to pause or resume playback.";
        }

        player.pause();

        if (player.getPaused()) {
            return "Playback paused successfully.";
        } else {
            return "Playback resumed successfully.";
        }
    }

    /**
     * Repeat string.
     *
     * @return the string
     */
    public String repeat() {
        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before setting the repeat status.";
        }


        Enums.RepeatMode repeatMode = player.repeat();
        String repeatStatus = "";

        switch (repeatMode) {
            case NO_REPEAT:
                repeatStatus = "no repeat";
                break;
            case REPEAT_ONCE:
                repeatStatus = "repeat once";
                break;
            case REPEAT_ALL:
                repeatStatus = "repeat all";
                break;
            case REPEAT_INFINITE:
                repeatStatus = "repeat infinite";
                break;
            case REPEAT_CURRENT_SONG:
                repeatStatus = "repeat current song";
                break;
            // Add a default case if necessary
            default:
                repeatStatus = "unknown repeat mode";
                break;
        }

        return "Repeat mode changed to %s.".formatted(repeatStatus);
    }

    /**
     * Shuffle string.
     *
     * @param seed the seed
     * @return the string
     */
    public String shuffle(final Integer seed) {
        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before using the shuffle function.";
        }

        if (!player.getType().equals("playlist") && !player.getType().equals("album")) {
            return "The loaded source is not a playlist or an album.";
        }

        player.shuffle(seed);

        if (player.getShuffle()) {
            return "Shuffle function activated successfully.";
        }
        return "Shuffle function deactivated successfully.";
    }

    /**
     * Forward string.
     *
     * @return the string
     */
    public String forward() {
        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before attempting to forward.";
        }

        if (!player.getType().equals("podcast")) {
            return "The loaded source is not a podcast.";
        }

        player.skipNext();

        return "Skipped forward successfully.";
    }

    /**
     * Backward string.
     *
     * @return the string
     */
    public String backward() {
        if (player.getCurrentAudioFile() == null) {
            return "Please select a source before rewinding.";
        }

        if (!player.getType().equals("podcast")) {
            return "The loaded source is not a podcast.";
        }

        player.skipPrev();

        return "Rewound successfully.";
    }

    /**
     * Like string.
     *
     * @return the string
     */
    public String like() {
        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before liking or unliking.";
        }


        if (!player.getType().equals("song")
                && !player.getType().equals("playlist")
                && !player.getType().equals("album")) {
            return "Loaded source is not a song.";
        }


        Song song = (Song) player.getCurrentAudioFile();

        if (likedSongs.contains(song)) {
            likedSongs.remove(song);
            song.dislike();

            return "Unlike registered successfully.";
        }

        likedSongs.add(song);
        song.like();
        return "Like registered successfully.";
    }

    /**
     * Next string.
     *
     * @return the string
     */
    public String next() {
        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before skipping to the next track.";
        }

        player.next();

        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before skipping to the next track.";
        }

        return "Skipped to next track successfully. The current track is %s."
                .formatted(player.getCurrentAudioFile().getName());
    }

    /**
     * Prev string.
     *
     * @return the string
     */
    public String prev() {
        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before returning to the previous track.";
        }

        player.prev();

        return "Returned to previous track successfully. The current track is %s."
                .formatted(player.getCurrentAudioFile().getName());
    }

    /**
     * Create playlist string.
     *
     * @param name      the name
     * @param timestamp the timestamp
     * @return the string
     */
    public String createPlaylist(final String name, final int timestamp) {
        if (playlists.stream().anyMatch(playlist -> playlist.getName().equals(name))) {
            return "A playlist with the same name already exists.";
        }

        playlists.add(new Playlist(name, username, timestamp));

        return "Playlist created successfully.";
    }

    /**
     * Add remove in playlist string.
     *
     * @param id the id
     * @return the string
     */
    public String addRemoveInPlaylist(final int id) {
        if (player.getCurrentAudioFile() == null) {
            return "Please load a source before adding to or removing from the playlist.";
        }

        if (player.getType().equals("podcast")) {
            return "The loaded source is not a song.";
        }

        if (id > playlists.size()) {
            return "The specified playlist does not exist.";
        }

        Playlist playlist = playlists.get(id - 1);

        if (playlist.containsSong((Song) player.getCurrentAudioFile())) {
            playlist.removeSong((Song) player.getCurrentAudioFile());
            return "Successfully removed from playlist.";
        }

        playlist.addSong((Song) player.getCurrentAudioFile());
        return "Successfully added to playlist.";
    }

    /**
     * Switch playlist visibility string.
     *
     * @param playlistId the playlist id
     * @return the string
     */
    public String switchPlaylistVisibility(final Integer playlistId) {
        if (playlistId > playlists.size()) {
            return "The specified playlist ID is too high.";
        }

        Playlist playlist = playlists.get(playlistId - 1);
        playlist.switchVisibility();

        if (playlist.getVisibility() == Enums.Visibility.PUBLIC) {
            return "Visibility status updated successfully to public.";
        }

        return "Visibility status updated successfully to private.";
    }

    /**
     * Show playlists array list.
     *
     * @return the array list
     */
    public ArrayList<PlaylistOutput> showPlaylists() {
        ArrayList<PlaylistOutput> playlistOutputs = new ArrayList<>();
        for (Playlist playlist : playlists) {
            playlistOutputs.add(new PlaylistOutput(playlist));
        }

        return playlistOutputs;
    }

    /**
     * Follow string.
     *
     * @return the string
     */
    public String follow() {
        LibraryEntry selection = searchBar.getLastSelected();
        String type = searchBar.getLastSearchType();

        if (selection == null) {
            return "Please select a source before following or unfollowing.";
        }

        if (!type.equals("playlist")) {
            return "The selected source is not a playlist.";
        }

        Playlist playlist = (Playlist) selection;

        if (playlist.getOwner().equals(username)) {
            return "You cannot follow or unfollow your own playlist.";
        }

        if (followedPlaylists.contains(playlist)) {
            followedPlaylists.remove(playlist);
            playlist.decreaseFollowers();

            return "Playlist unfollowed successfully.";
        }

        followedPlaylists.add(playlist);
        playlist.increaseFollowers();


        return "Playlist followed successfully.";
    }

    /**
     * Gets player stats.
     *
     * @return the player stats
     */
    public PlayerStats getPlayerStats(final User user) {
        return player.getStats(user);
    }

    /**
     * Show preferred songs array list.
     *
     * @return the array list
     */
    public ArrayList<String> showPreferredSongs() {
        ArrayList<String> results = new ArrayList<>();
        for (AudioFile audioFile : likedSongs) {
            results.add(audioFile.getName());
        }

        return results;
    }

    /**
     * Gets preferred genre.
     *
     * @return the preferred genre
     */
    public String getPreferredGenre() {
        String[] genres = {"pop", "rock", "rap"};
        int[] counts = new int[genres.length];
        int mostLikedIndex = -1;
        int mostLikedCount = 0;

        for (Song song : likedSongs) {
            for (int i = 0; i < genres.length; i++) {
                if (song.getGenre().equals(genres[i])) {
                    counts[i]++;
                    if (counts[i] > mostLikedCount) {
                        mostLikedCount = counts[i];
                        mostLikedIndex = i;
                    }
                    break;
                }
            }
        }

        String preferredGenre = mostLikedIndex != -1 ? genres[mostLikedIndex] : "unknown";
        return "This user's preferred genre is %s.".formatted(preferredGenre);
    }

    /**
     * Simulate time.
     *
     * @param time the time
     */
    public void simulateTime(final int time) {
        player.simulatePlayer(time);
    }

}
