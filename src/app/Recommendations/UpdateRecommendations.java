package app.Recommendations;

import app.Admin;
import app.audio.Files.Song;
import app.player.Player;
import app.player.PlayerSource;
import app.user.Artist;
import app.user.User;
import fileio.input.CommandInput;

import java.util.*;

import static app.user.User.getPlayerInstance;

public class UpdateRecommendations {

    private static String fansPlaylist(User user) {
        Player player = getPlayerInstance(user);
        PlayerSource source = Player.getPlayerSourceInstance(player);
        String songName = new String();
        Song songOnListen = null;
        if (source != null) {
            if (source.getAudioFile() != null) {
                songName = source.getAudioFile().getName();
            }
        }
        for (Song song : Admin.getSongs()) {
            if (song.getName().equals(songName)) {
                songOnListen = song;
                break;
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
        Collections.sort(playlist, Comparator.comparingInt(Song::getLikes).reversed());
        ArrayList<Song> fansPlaylist = new ArrayList<>();

        // Add up to the first 5 liked songs to the list
        for (int i = 0; i < Math.min(5, playlist.size()); i++) {
            fansPlaylist.add(playlist.get(i));
        }
        user.setFansPlaylist(fansPlaylist);
        String playlistName = artist.getName() + " Fan Club recommendations";
        user.setFansPlaylistName(playlistName);
        return "The recommendations for user " + user.getUsername() + " have been updated successfully.";
    }

    public static String updateRecommendations(CommandInput commandInput) {
        if (Admin.getUser(commandInput.getUsername()) == null) {
            return "The username " + commandInput.getUsername() + " doesn't exist.";
        }

        User user = Admin.getUser(commandInput.getUsername());
        if (!"user".equals(user.getUserType())) {
            return commandInput.getUsername() + " is not a normal user.";
        }

        switch (commandInput.getRecommendationType()) {
            case "fans_playlist" -> fansPlaylist(user);
        }
        return "The recommendations for user " + user.getUsername() + " have been updated successfully.";
    }
}
