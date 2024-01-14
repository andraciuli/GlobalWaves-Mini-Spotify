package app.user;

import app.Admin;
import app.audio.Collections.Podcast;
import app.audio.Files.Episode;
import app.audio.LibraryEntry;
import app.player.Player;
import app.player.PlayerSource;
import app.utils.UserVisitable;
import app.wrapped.EpisodeWrapp;
import app.wrapped.UserWrapp;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.CommandInput;
import fileio.input.EpisodeInput;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static app.user.Artist.objectMapper;

public class Host extends LibraryEntry implements UserVisitable {
    @Getter
    private String name;
    @Getter
    private int age;
    @Getter
    private String city;
    @Getter
    private String userType;
    @Getter
    private ArrayList<Podcast> podcasts;
    @Getter
    private ArrayList<Announcement> announcements;
    @Getter
    private ArrayList<EpisodeWrapp> listenedEpisodes;
    @Getter
    private ArrayList<UserWrapp> listeners;
    private int numberListeners;
    public void incrementListens() {
        numberListeners++;
    }


    public Host(final String name, final int age, final String city, final String userType) {
        super(name);
        this.name = name;
        this.age = age;
        this.city = city;
        this.userType = userType;
        podcasts = new ArrayList<>();
        announcements = new ArrayList<>();
        listenedEpisodes = new ArrayList<>();
        listeners = new ArrayList<>();
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

    private static boolean hasDuplicates(final ArrayList<EpisodeInput> episodes) {
        HashSet<String> uniqueElements = new HashSet<>();
        for (EpisodeInput episode : episodes) {
            if (!uniqueElements.add(episode.getName())) {
                // If the element is already in the HashSet, it's a duplicate
                return true;
            }
        }
        // No duplicates found
        return false;
    }

    /**
     * Adds a new podcast to the host's collection.
     *
     * @param commandInput The command input containing information about the new podcast.
     * @return A message indicating the success or failure of the operation.
     */
    public static String addPodcast(final CommandInput commandInput) {
        // Check if the user with the given username exists
        if (Admin.getUser(commandInput.getUsername()) == null) {
            return "The username " + commandInput.getUsername() + " doesn't exist.";
        }

        // Retrieve the user and check if they are a host
        User user = Admin.getUser(commandInput.getUsername());
        if (!"host".equals(user.getUserType())) {
            return commandInput.getUsername() + " is not a host.";
        }

        // Check if the host already has a podcast with the same name
        Host host = Admin.getHost(commandInput.getUsername());
        if (!host.getPodcasts().isEmpty()) {
            for (Podcast podcast : host.getPodcasts()) {
                if (podcast.getName().equals(commandInput.getName())) {
                    return commandInput.getUsername() + " has another podcast with the same name.";
                }
            }
        }

        // Check for duplicates in the list of episodes
        boolean hasDuplicates = hasDuplicates(commandInput.getEpisodes());
        if (hasDuplicates) {
            return commandInput.getUsername() + " has the same episode in this podcast.";
        }

        // Create a list to store episodes for the new podcast
        List<Episode> episodesAlbum = new ArrayList<>();
        for (EpisodeInput episodeInput : commandInput.getEpisodes()) {
            Episode episodeInAlbum = new Episode(episodeInput.getName(),
                    episodeInput.getDuration(), episodeInput.getDescription());
            episodesAlbum.add(episodeInAlbum);
        }

        // Create a new Podcast and add it to the user and host
        Podcast podcast = new Podcast(commandInput.getName(),
                commandInput.getUsername(), episodesAlbum);
        user.getPodcasts().add(podcast);
        host.getPodcasts().add(podcast);

        // Update the list of podcasts in the system
        List<Podcast> podcastsToAdd = Admin.getPodcasts();
        podcastsToAdd.add(podcast);
        Admin.updatePodcastList(podcastsToAdd);

        return commandInput.getUsername() + " has added new podcast successfully.";
    }

    /**
     * Removes a podcast from the host's collection.
     *
     * @param commandInput The command input containing information about the podcast to be removed.
     * @return A message indicating the success or failure of the operation.
     */
    public static String removePodcast(final CommandInput commandInput) {
        // Check if the user with the given username exists
        if (Admin.getUser(commandInput.getUsername()) == null) {
            return "The username " + commandInput.getUsername() + " doesn't exist.";
        }
        // Retrieve the user and check if they are a host
        User user = Admin.getUser(commandInput.getUsername());

        if (!"host".equals(user.getUserType())) {
            return commandInput.getUsername() + " is not a host.";
        }

        Host host = Admin.getHost(commandInput.getUsername());
        // Find the podcast to remove based on the provided name
        Podcast podcastToRemove = null;
        for (Podcast podcast : host.getPodcasts()) {
            if (commandInput.getName().equals(podcast.getName())) {
                podcastToRemove = podcast;
                break;
            }
        }

        // Check if the podcast exists
        if (podcastToRemove == null) {
            return commandInput.getUsername() + " doesn't have a podcast with the given name.";
        }

        // Check for active players using the podcast
        for (User user1 : Admin.getUsers()) {
            Player player = User.getPlayerInstance(user1);
            PlayerSource source = Player.getPlayerSourceInstance(player);
            if (source != null) {
                if (source.getAudioCollection().getName().equals(commandInput.getName())) {
                    return commandInput.getUsername() + " can't delete this podcast.";
                }
            }
        }

        // Remove the podcast from the user and host
        user.getPodcasts().remove(podcastToRemove);
        host.getPodcasts().remove(podcastToRemove);

        // Update the list of podcasts in the system
        List<Podcast> podcastsToRemove = Admin.getPodcasts();
        podcastsToRemove.remove(podcastToRemove);
        Admin.updatePodcastList(podcastsToRemove);

        return commandInput.getUsername() + " deleted the podcast successfully.";
    }

    /**
     * Retrieves information about the host's podcasts.
     *
     * @param commandInput The command input containing information about the host.
     * @return A list of ObjectNode containing podcast details.
     */
    public static ArrayList<ObjectNode> showPodcasts(final CommandInput commandInput) {
        ArrayList<ObjectNode> result = new ArrayList<>();
        User user = Admin.getUser(commandInput.getUsername());
        Host host = Admin.getHost(commandInput.getUsername());
        for (Podcast podcast : host.getPodcasts()) {
            ObjectNode showPodcasts = objectMapper.createObjectNode();
            showPodcasts.put("name", podcast.getName());
            ArrayList<String> episodeName = new ArrayList<>();
            for (Episode episode : podcast.getEpisodes()) {
                episodeName.add(episode.getName());
            }
            showPodcasts.putPOJO("episodes", episodeName);
            result.add(showPodcasts);
        }
        return result;
    }

    /**
     * Adds a new announcement to the host's collection.
     *
     * @param commandInput The command input containing information about the new announcement.
     * @return A message indicating the success or failure of the operation.
     */
    public static String addAnnouncement(final CommandInput commandInput) {
        // Check if the user with the given username exists
        if (Admin.getUser(commandInput.getUsername()) == null) {
            return "The username " + commandInput.getUsername() + " doesn't exist.";
        } else {
            // Retrieve the user and check if they are a host
            User user = Admin.getUser(commandInput.getUsername());
            if (!"host".equals(user.getUserType())) {
                return commandInput.getUsername() + " is not a host.";
            }
        }

        Host hostFound = Admin.getHost(commandInput.getUsername());
        // Check if the host already has announcements with the same name
        if (!hostFound.getAnnouncements().isEmpty()) {
            for (Announcement announcement : hostFound.getAnnouncements()) {
                if (announcement.getName().equals(commandInput.getName())) {
                    return commandInput.getUsername()
                            + " has already added an announcement with this name.";
                }
            }
        }

        // Add a new announcement for the host
        hostFound.getAnnouncements().add(new Announcement(commandInput.getUsername(),
                commandInput.getName(), commandInput.getDescription()));

        return commandInput.getUsername() + " has successfully added new announcement.";
    }

    /**
     * Removes an announcement from the host's collection.
     *
     * @param commandInput The command input containing
     *                     information about the announcement to be removed.
     * @return A message indicating the success or failure of the operation.
     */
    public static String removeAnnouncement(final CommandInput commandInput) {
        // Check if the user with the given username exists
        if (Admin.getUser(commandInput.getUsername()) == null) {
            return "The username " + commandInput.getUsername() + " doesn't exist.";
        } else {
            // Retrieve the user and check if they are a host
            User user = Admin.getUser(commandInput.getUsername());
            if (!"host".equals(user.getUserType())) {
                return commandInput.getUsername() + " is not a host.";
            }
        }

        Host host = Admin.getHost(commandInput.getUsername());
        // Find the announcement to remove based on the provided name
        Announcement announcementToRemove = null;
        for (Announcement announcement : host.getAnnouncements()) {
            if (commandInput.getName().equals(announcement.getName())) {
                announcementToRemove = announcement;
                break;
            }
        }

        // Check if the announcement exists
        if (announcementToRemove == null) {
            return commandInput.getUsername() + " has no announcement with the given name.";
        }

        // Remove the announcement from the host
        host.getAnnouncements().remove(announcementToRemove);

        return commandInput.getUsername() + " has successfully deleted the announcement.";
    }

    /**
     * Inner class representing an announcement made by the host.
     */
    public static class Announcement {
        private String host;
        @Getter
        private String name;
        @Getter
        private String description;

        /**
         * Constructor for the Announcement class.
         *
         * @param host        The host making the announcement.
         * @param name        The name of the announcement.
         * @param description The description of the announcement.
         */
        public Announcement(final String host, final String name, final String description) {
            this.host = host;
            this.name = name;
            this.description = description;
        }
    }
}
