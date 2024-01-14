package app.Recommendations;

import app.Admin;
import app.audio.Collections.Playlist;
import app.audio.Files.Song;
import app.player.Player;
import app.player.PlayerSource;
import app.sortStrategy.SortByLikes;
import app.user.Artist;
import app.user.User;
import fileio.input.CommandInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import static app.user.User.getPlayerInstance;

/**
 * The {@code UpdateRecommendations} class provides methods for
 * updating and loading user recommendations.
 */
public final class UpdateRecommendations {
    private static final int TOP_LIMIT = 5;
    private static final int DURATION_SONG = 30;
    private static final int TOP_LIMIT_SEC_GENRE = 3;
    private static final int TOP_LIMIT_THIRD_GENRE = 2;

    private UpdateRecommendations() {
        // Private constructor to prevent instantiation of the utility class.
    }

    /**
     * Generates a playlist based on the top 5 liked songs of the artist's top 5 fans.
     *
     * @param user The user for whom the playlist is generated.
     */
    private static void fansPlaylist(final User user) {
        Player player = getPlayerInstance(user);
        PlayerSource source = Player.getPlayerSourceInstance(player);
        Song songOnListen = null;
        if (source != null) {
            if (source.getAudioFile() != null) {
                songOnListen = Admin.getSong(source.getAudioFile().getName());
            }
        }
        Artist artist = Admin.getArtist(songOnListen.getArtist());
        ArrayList<User> fans = artist.top5Fans();
        ArrayList<Song> playlist = new ArrayList<>();
        for (User fan : fans) {
            playlist.addAll(fan.top5LikedSongs());
        }
        Set<Song> uniqueSongsSet = new HashSet<>(playlist);
        playlist = new ArrayList<>(uniqueSongsSet);
        SortByLikes sort = new SortByLikes();
        sort.sort(playlist);
        ArrayList<Song> fansPlaylist = new ArrayList<>();

        // Add up to the first 5 liked songs to the list
        for (int i = 0; i < Math.min(TOP_LIMIT, playlist.size()); i++) {
            fansPlaylist.add(playlist.get(i));
        }
        user.setPlaylistRecommandation(fansPlaylist);
        String playlistName = artist.getName() + " Fan Club recommendations";
        user.setPlaylistRecommandationName(playlistName);
    }

    /**
     * Generates a random song recommendation based on the genre of the
     * currently playing song.
     *
     * @param user The user for whom the recommendation is generated.
     */
    private static void randomSong(final User user) {
        Player player = getPlayerInstance(user);
        PlayerSource source = Player.getPlayerSourceInstance(player);
        Song songOnLoad = null;
        if (source != null) {
            if (source.getAudioFile() != null) {
                songOnLoad = Admin.getSong(source.getAudioFile().getName());
            }
        }
        ArrayList<Song> songs = new ArrayList<>();
        if (songOnLoad != null) {
            for (Song song : Admin.getSongs()) {
                if (song.getGenre().equals(songOnLoad.getGenre())) {
                    songs.add(song);
                }
            }
            if (songOnLoad.getDuration() - user.getPlayerStats(user).getRemainedTime()
                    >= DURATION_SONG) {
                int timeListened = songOnLoad.getDuration()
                        - user.getPlayerStats(user).getRemainedTime();
                Random randomListen = new Random(timeListened);
                int index = randomListen.nextInt(songs.size());
                user.setSongRecommandation(songs.get(index).getName());
            }
        }
    }

    /**
     * Returns a list of songs that belong to a specific genre from the user's
     * liked songs, playlists, and followed playlists.
     *
     * @param allSongs The list of all songs from liked songs, playlists, and followed playlists.
     * @param genre    The genre for which to filter songs.
     * @return A list of songs belonging to the specified genre.
     */
    private static List<Song> getGenreSongs(final ArrayList<Song> allSongs, final String genre) {
        List<Song> genreSongs = new ArrayList<>();
        for (Song song : allSongs) {
            if (song.getGenre().equals(genre)) {
                genreSongs.add(song);
            }
        }
        return genreSongs;
    }

    /**
     * Generates a random playlist based on the user's liked songs,
     * playlists, and followed playlists.
     * The playlist includes top songs from the user's most frequently liked genres.
     *
     * @param user The user for whom the playlist is generated.
     */
    private static void randomPlaylist(final User user) {
        ArrayList<Song> allSongs = new ArrayList<>();
        ArrayList<Song> randomPlaylist = new ArrayList<>();

        allSongs.addAll(user.getLikedSongs());

        for (Playlist playlist : user.getPlaylists()) {
            allSongs.addAll(playlist.getSongs());
        }

        for (Playlist playlist : user.getFollowedPlaylists()) {
            allSongs.addAll(playlist.getSongs());
        }

        // Create a map to count genres
        Map<String, Integer> genreCountMap = new HashMap<>();

        // Count songs for each genre
        for (Song song : allSongs) {
            String genre = song.getGenre();
            genreCountMap.put(genre, genreCountMap.getOrDefault(genre, 0) + 1);
        }

        // Sort genres based on the number of songs
        List<Map.Entry<String, Integer>> sortedGenres = new ArrayList<>(genreCountMap.entrySet());
        sortedGenres.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
        SortByLikes sort = new SortByLikes();

        if (sortedGenres.size() >= 1) {
            // Get the first genre
            String firstGenre = sortedGenres.get(0).getKey();

            // Get the list of songs from the first genre
            List<Song> songsInFirstGenre = getGenreSongs(allSongs, firstGenre);
            sort.sort(songsInFirstGenre);
            List<Song> top5SongsInFirstGenre = songsInFirstGenre.subList(0,
                    Math.min(TOP_LIMIT, songsInFirstGenre.size()));
            randomPlaylist.addAll(top5SongsInFirstGenre);
        }

        if (sortedGenres.size() >= 2) {
            // Get the second genre
            String secondGenre = sortedGenres.get(1).getKey();

            // Get the list of songs from the second genre
            List<Song> songsInSecondGenre = getGenreSongs(allSongs, secondGenre);

            sort.sort(songsInSecondGenre);

            // Add the top 3 songs from songsInSecondGenre to the playlist
            List<Song> top3SongsInSecondGenre = songsInSecondGenre.subList(0,
                    Math.min(TOP_LIMIT_SEC_GENRE, songsInSecondGenre.size()));

            randomPlaylist.addAll(top3SongsInSecondGenre);
        }

        if (sortedGenres.size() >= TOP_LIMIT_SEC_GENRE) {
            // Get the third genre
            String thirdGenre = sortedGenres.get(2).getKey();

            // Get the list of songs from the third genre
            List<Song> songsInThirdGenre = getGenreSongs(allSongs, thirdGenre);

            sort.sort(songsInThirdGenre);

            // Add the top 2 songs from songsInThirdGenre to the playlist
            List<Song> top2SongsInThirdGenre = songsInThirdGenre.subList(0,
                    Math.min(TOP_LIMIT_THIRD_GENRE, songsInThirdGenre.size()));

            // Add the playlist to the list of playlists
            randomPlaylist.addAll(top2SongsInThirdGenre);
        }
        user.setPlaylistRecommandation(randomPlaylist);
        String playlistName = user.getUsername() + "'s recommendations";
        user.setPlaylistRecommandationName(playlistName);
    }

    /**
     * Updates user recommendations based on the given command input.
     *
     * @param commandInput The command input specifying the type of recommendation to update.
     * @return A message indicating the success of the recommendation update.
     */
    public static String updateRecommendations(final CommandInput commandInput) {
        if (Admin.getUser(commandInput.getUsername()) == null) {
            return "The username " + commandInput.getUsername() + " doesn't exist.";
        }

        User user = Admin.getUser(commandInput.getUsername());
        if (!"user".equals(user.getUserType())) {
            return commandInput.getUsername() + " is not a normal user.";
        }

        switch (commandInput.getRecommendationType()) {
            case "fans_playlist" -> fansPlaylist(user);
            case "random_song" -> randomSong(user);
            case "random_playlist" -> randomPlaylist(user);
            default -> throw new IllegalArgumentException("Invalid recommendation type: "
                    + commandInput.getRecommendationType());
        }
        return "The recommendations for user " + user.getUsername()
                + " have been updated successfully.";
    }

    /**
     * Loads the user's song recommendation into the player's source for playback.
     *
     * @param commandInput The command input specifying the user for whom to load
     *                     the recommendation.
     * @return A message indicating the success of the recommendation loading.
     */
    public static String loadRecommendation(final CommandInput commandInput) {
        User user = Admin.getUser(commandInput.getUsername());
        if (user.getSongRecommandation() == null) {
            return "You can't load an empty audio collection!";
        }
        Song songRecommendation = Admin.getSong(user.getSongRecommandation());
        if (songRecommendation != null) {
            user.getPlayer().setSource(songRecommendation, "song");
            user.updateListenSongs();
            user.getPlayer().pause();
        }

        return "Playback loaded successfully.";
    }
}
