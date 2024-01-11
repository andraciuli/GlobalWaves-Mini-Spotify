package app.pageSystem;

import app.Admin;
import app.CommandRunner;
import app.audio.Collections.Album;
import app.audio.Collections.Playlist;
import app.audio.Collections.Podcast;
import app.player.Player;
import app.player.PlayerSource;
import app.user.Artist;
import app.user.Host;
import app.user.User;
import app.utils.Enums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import app.audio.Files.Song;
import fileio.input.CommandInput;
import lombok.Getter;
import lombok.Setter;

public final class PageCommands {
    @Getter
    @Setter
    private static String selectedArtist;
    @Getter
    @Setter
    private static String selectedHost;
    private static final int TOP_LIMIT = 5;

    private PageCommands() {
        // Private constructor to hide the implicit public one
    }

    /**
     * Retrieves the top liked songs for a given user,
     * sorted by the number of likes in descending order.
     *
     * @param user The user for whom the top liked songs are retrieved.
     * @return A list of up to 5 top liked songs, containing their names.
     */
    public static ArrayList<String> getLikedSongs(final User user) {
        List<Song> likedSongs = new ArrayList<>(user.getLikedSongs());
        // Sort likedSongs in descending order based on the number of likes
        Collections.sort(likedSongs, Comparator.comparingInt(Song::getLikes).reversed());

        // Get the first 5 songs from the sorted list (or fewer if the list is smaller)
        ArrayList<String> topLikedSongs = new ArrayList<>();
        int count = 0;
        for (Song song : likedSongs) {
            if (count >= TOP_LIMIT) {
                break;
            }
            topLikedSongs.add(song.getName());
            count++;
        }
        return topLikedSongs;
    }

    /**
     * Retrieves the top followed playlists for a given user,
     * sorted by the number of followers in descending order.
     *
     * @param user The user for whom the top followed playlists are retrieved.
     * @return A list of up to 5 top followed playlists, containing their names.
     */
    public static ArrayList<String> getFollowedPlaylists(final User user) {
        ArrayList<Playlist> followedPlaylists = user.getFollowedPlaylists();
        // Sort likedSongs in descending order based on the number of likes
        Collections.sort(followedPlaylists,
                Comparator.comparingInt(Playlist::getFollowers).reversed());

        // Get the first 5 songs from the sorted list (or fewer if the list is smaller)
        ArrayList<String> topFollowedPlaylists = new ArrayList<>();
        int count = 0;
        for (Playlist playlist : followedPlaylists) {
            if (count >= TOP_LIMIT) {
                break;
            }
            topFollowedPlaylists.add(playlist.getName());
            count++;
        }
        return topFollowedPlaylists;
    }

    /**
     * Generates and returns a string representation of the current page for a given user.
     *
     * @param user The user for whom the current page information is generated.
     * @return A string containing information about the current page for the user.
     */
    public static String printCurrentPage(final User user) {
        StringBuilder result = new StringBuilder();

        if (user.getConnectionStatus().equals(Enums.ConnectionStatus.OFFLINE)) {
            result.append(user.getUsername()).append(" is offline.");
            return result.toString();
        }

        Enums.currentPage currentPage = user.getCurrentPage();
        switch (currentPage) {
            case HOME:
                ArrayList<String> likedSongs = PageCommands.getLikedSongs(user);
                ArrayList<String> followedPlaylists = PageCommands.getFollowedPlaylists(user);
                result.append("Liked songs:\n\t").append(likedSongs)
                        .append("\n\nFollowed playlists:\n\t").append(followedPlaylists)
                        .append("\n\nSong recommendations:\n\t[").append(user.getSongRecommandation())
                        .append("]\n\nPlaylists recommendations:\n\t[").append(user.getPlaylistRecommandationName()).append("]");
                break;
            case ARTIST:
                Artist artist = Admin.getArtist(selectedArtist);

                // Albums
                String albumsInfo = "Albums:\n\t[" + artist.getAlbums().stream()
                        .map(Album::getName).collect(Collectors.joining(", "));

                // Merch
                String merchInfo = "Merch:\n\t[" + artist.getMerches().stream()
                        .map(merch -> merch.getName() + " - " + merch.getPrice()
                                + ":\n\t" + merch.getDescription())
                        .collect(Collectors.joining(", "));

                // Events
                String eventsInfo = "Events:\n\t[" + artist.getEvents().stream()
                        .map(event -> event.getName() + " - " + event.getDate()
                                + ":\n\t" + event.getDescription())
                        .collect(Collectors.joining(", "));

                // Combine everything
                result.append(albumsInfo).append("]\n\n").append(merchInfo).append("]\n\n")
                        .append(eventsInfo).append("]");
                break;
            case HOST:
                Host host = Admin.getHost(selectedHost);
                // Podcasts
                String podcastsInfo = "Podcasts:\n\t[" + host.getPodcasts().stream()
                        .map(podcast -> podcast.getName() + ":\n\t["
                                + podcast.getEpisodes().stream()
                                        .map(episode -> episode.getName() + " - "
                                                + episode.getDescription())
                                        .collect(Collectors.joining(", "))
                                + "]\n")
                        .collect(Collectors.joining(", ")) + "]";

                // Announcements
                String announcementsInfo = "Announcements:\n\t[" + host.getAnnouncements().stream()
                        .map(announcement -> announcement.getName() + ":\n\t"
                                + announcement.getDescription())
                        .collect(Collectors.joining(", ")) + "]";

                // Combine everything
                result.append(podcastsInfo).append("\n\n").append(announcementsInfo);
                break;
            case LIKED_CONTENT:
                result.append("Liked songs:\n\t[");
                for (int i = 0; i < user.getLikedSongs().size(); i++) {
                    Song song = user.getLikedSongs().get(i);
                    result.append(song.getName()).append(" - ").append(song.getArtist());
                    if (i < user.getLikedSongs().size() - 1) {
                        result.append(", ");
                    }
                }
                result.append("]\n\nFollowed playlists:\n\t[");
                for (int i = 0; i < user.getFollowedPlaylists().size(); i++) {
                    Playlist playlist = user.getFollowedPlaylists().get(i);
                    result.append(playlist.getName()).append(" - ").append(playlist.getOwner());
                    if (i < user.getFollowedPlaylists().size() - 1) {
                        result.append(", ");
                    }
                }
                result.append("]");
                break;
            default:
                break;

        }
        return result.toString();
    }

    /**
     * Changes the current page for a user based on the provided command input.
     *
     * @param commandInput The command input containing information about the page change request.
     * @param user         The user for whom the page change is performed.
     * @return A string indicating the success or failure of the page change operation.
     */
    public static String changePage(final CommandInput commandInput, final User user) {
        user.setPreviousPage(user.getCurrentPage());
        if (commandInput.getNextPage().equals("Home")) {
            user.setCurrentPage(Enums.currentPage.HOME);
            user.getIstoricPages().add(user.getCurrentPage());
        } else if (commandInput.getNextPage().equals("LikedContent")) {
            user.setCurrentPage(Enums.currentPage.LIKED_CONTENT);
            user.getIstoricPages().add(user.getCurrentPage());
        } else if (commandInput.getNextPage().equals("Artist")) {
            user.setCurrentPage(Enums.currentPage.ARTIST);
            Player player = user.getPlayerInstance(user);
            PlayerSource source = Player.getPlayerSourceInstance(player);
            Song songOnLoad = null;
            if (source != null) {
                if (source.getAudioFile() != null) {
                    songOnLoad = Admin.getSong(source.getAudioFile().getName());
                }
            }
            if (songOnLoad != null) {
                selectedArtist = songOnLoad.getArtist();
            }
            user.getIstoricPages().add(user.getCurrentPage());
        } else if (commandInput.getNextPage().equals("Host")) {
            user.setCurrentPage(Enums.currentPage.HOST);
            Player player = user.getPlayerInstance(user);
            PlayerSource source = Player.getPlayerSourceInstance(player);
            Podcast podcastOnLoad = null;
            if (source != null) {
                if (source.getAudioCollection() != null) {
                    podcastOnLoad = Admin.getPodcast(source.getAudioCollection().getName());
                }
            }
            if (podcastOnLoad != null) {
                selectedHost = podcastOnLoad.getOwner();
            }
            user.getIstoricPages().add(user.getCurrentPage());
        } else {
            return commandInput.getUsername() + " is trying to access a non-existent page.";
        }
        return commandInput.getUsername() + " accessed "
                + commandInput.getNextPage() + " successfully.";
    }

    public static String previousPage(final CommandInput commandInput) {
        User user = Admin.getUser(commandInput.getUsername());
        if (user.getPreviousPage().equals(Enums.currentPage.DEFAULT)) {
            return "There are no pages left to go back.";
        }
        user.setNextPage(user.getCurrentPage());
        //user.setCurrentPage(user.getPreviousPage());
        int index = user.getIstoricPages().indexOf(user.getCurrentPage());
        if (index != -1 && index > 0) {
            user.setCurrentPage(user.getIstoricPages().get(index - 1));
        } else {
            return "There are no pages left to go back.";
        }

        return "The user " + commandInput.getUsername() + " has navigated successfully to the previous page.";
    }

    public static String nextPage(final CommandInput commandInput) {
        User user = Admin.getUser(commandInput.getUsername());
        if (user.getNextPage().equals(Enums.currentPage.DEFAULT)) {
            return "There are no pages left to go forward.";
        }
        user.setPreviousPage(user.getCurrentPage());
        user.setCurrentPage(user.getNextPage());
        user.setNextPage(Enums.currentPage.DEFAULT);
        return "The user " + commandInput.getUsername() + " has navigated successfully to the next page.";
    }
}
