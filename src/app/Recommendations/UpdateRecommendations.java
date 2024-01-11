package app.Recommendations;

import app.Admin;
import app.audio.Collections.Playlist;
import app.audio.Files.Song;
import app.player.Player;
import app.player.PlayerSource;
import app.user.Artist;
import app.user.User;
import fileio.input.CommandInput;

import java.util.*;

import static app.user.User.getPlayerInstance;

public class UpdateRecommendations {

    private static void fansPlaylist(User user) {
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
        Collections.sort(playlist, Comparator.comparingInt(Song::getLikes).reversed());
        ArrayList<Song> fansPlaylist = new ArrayList<>();

        // Add up to the first 5 liked songs to the list
        for (int i = 0; i < Math.min(5, playlist.size()); i++) {
            fansPlaylist.add(playlist.get(i));
        }
        user.setPlaylistRecommandation(fansPlaylist);
        String playlistName = artist.getName() + " Fan Club recommendations";
        user.setPlaylistRecommandationName(playlistName);
    }

    private static void randomSong(User user) {
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
            if (songOnLoad.getDuration() - user.getPlayerStats(user).getRemainedTime() >= 30) {
                int timeListened = songOnLoad.getDuration() - user.getPlayerStats(user).getRemainedTime();
                Random randomListen = new Random(timeListened);
                int index = randomListen.nextInt(songs.size());
                user.setSongRecommandation(songs.get(index).getName());
            }
        }
    }

    private static List<Song> getGenreSongs(ArrayList<Song> allSongs, String genre) {
        List<Song> genreSongs = new ArrayList<>();
        for (Song song : allSongs) {
            if (song.getGenre().equals(genre)) {
                genreSongs.add(song);
            }
        }
        return genreSongs;
    }

    private static void randomPlaylist(User user) {
        ArrayList<Song> allSongs = new ArrayList<>();
        ArrayList<Song> randomPlaylist = new ArrayList<>();

        allSongs.addAll(user.getLikedSongs());

        for (Playlist playlist : user.getPlaylists()) {
            allSongs.addAll(playlist.getSongs());
        }

        for (Playlist playlist : user.getFollowedPlaylists()) {
            allSongs.addAll(playlist.getSongs());
        }

        // Crează un map pentru a număra genurile
        Map<String, Integer> genreCountMap = new HashMap<>();

        // Numără melodiile pentru fiecare gen
        for (Song song : allSongs) {
            String genre = song.getGenre();
            genreCountMap.put(genre, genreCountMap.getOrDefault(genre, 0) + 1);
        }

        // Sortează genurile în funcție de numărul de melodii
        List<Map.Entry<String, Integer>> sortedGenres = new ArrayList<>(genreCountMap.entrySet());
        sortedGenres.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        if (sortedGenres.size() >= 1) {
            // Obține primul gen
            String firstGenre = sortedGenres.get(0).getKey();

            // Obține lista de melodii din primul gen
            List<Song> songsInFirstGenre = getGenreSongs(allSongs, firstGenre);
            Collections.sort(songsInFirstGenre, Comparator.comparingInt(Song::getLikes).reversed());
            List<Song> top5SongsInFirstGenre = songsInFirstGenre.subList(0, Math.min(5, songsInFirstGenre.size()));
            randomPlaylist.addAll(top5SongsInFirstGenre);
        }

        if (sortedGenres.size() >= 2) {
            // Obține al doilea gen
            String secondGenre = sortedGenres.get(1).getKey();

            // Obține lista de melodii din al doilea gen
            List<Song> songsInSecondGenre = getGenreSongs(allSongs, secondGenre);

            // Sortează lista în funcție de numărul de like-uri
            Collections.sort(songsInSecondGenre, Comparator.comparingInt(Song::getLikes).reversed());

            // Adaugă primele 3 melodii din songsInSecondGenre în playlist
            List<Song> top3SongsInSecondGenre = songsInSecondGenre.subList(0, Math.min(3, songsInSecondGenre.size()));

            randomPlaylist.addAll(top3SongsInSecondGenre);
        }

        if (sortedGenres.size() >= 3) {
            // Obține al treilea gen
            String thirdGenre = sortedGenres.get(2).getKey();

            // Obține lista de melodii din al treilea gen
            List<Song> songsInThirdGenre = getGenreSongs(allSongs, thirdGenre);

            // Sortează lista în funcție de numărul de like-uri
            Collections.sort(songsInThirdGenre, Comparator.comparingInt(Song::getLikes).reversed());

            // Adaugă primele 2 melodii din songsInThirdGenre în playlist
            List<Song> top2SongsInThirdGenre = songsInThirdGenre.subList(0, Math.min(2, songsInThirdGenre.size()));

            // Adaugă playlist-ul la lista de playlist-uri
            randomPlaylist.addAll(top2SongsInThirdGenre);
        }
        user.setPlaylistRecommandation(randomPlaylist);
        String playlistName = user.getUsername() + "'s recommendations";
        user.setPlaylistRecommandationName(playlistName);
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
            case "random_song" -> randomSong(user);
            case "random_playlist" -> randomPlaylist(user);
        }
        return "The recommendations for user " + user.getUsername() + " have been updated successfully.";
    }

    public static String loadRecommendation(CommandInput commandInput) {
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
