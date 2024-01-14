package app.wrapped;

import app.Admin;
import app.user.User;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.CommandInput;

import static app.user.Artist.objectMapper;

/**
 * Utility class for generating wrapped information about a user based on a command input.
 */
public class Wrapped {

    /**
     * Generates a wrapped representation of user information based on the provided command input.
     *
     * @param commandInput The command input specifying the user for whom to
     *                     generate wrapped information.
     * @return An ObjectNode containing wrapped user information.
     */
    public static ObjectNode wrapped(final CommandInput commandInput) {
        User user = Admin.getUser(commandInput.getUsername());
        String type = user.getUserType();
        ObjectNode result = objectMapper.createObjectNode();

        switch (type) {
            case "user" -> result = wrapUser(user);
        }

        return result;
    }

    /**
     * Generates a wrapped representation of user information.
     *
     * @param user The user for whom to generate wrapped information.
     * @return An ObjectNode containing wrapped user information.
     */
    public static ObjectNode wrapUser(final User user) {
        ObjectNode result = objectMapper.createObjectNode();
        ObjectNode topArtists = objectMapper.createObjectNode();
        ObjectNode topGenres = objectMapper.createObjectNode();
        ObjectNode topSongs = objectMapper.createObjectNode();
        ObjectNode topAlbums = objectMapper.createObjectNode();

        result.putPOJO("topArtists", topArtists);
        result.putPOJO("topGenres", topGenres);
        result.putPOJO("topSongs", topSongs);
        result.putPOJO("topAlbums", topAlbums);
        ObjectNode topEpisodesNode = objectMapper.createObjectNode();

        for (EpisodeWrapp episodeWrapp : user.top5listenedEpisodes()) {
            String episodeName = episodeWrapp.getEpisode().getName();
            int listens = episodeWrapp.getListens();

            topEpisodesNode.put(episodeName, listens);
        }

        result.putPOJO("topEpisodes", topEpisodesNode);

        return result;
    }
}
