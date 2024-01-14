# Proiect GlobalWaves  - Etapa 2
## Ciulinca Andra Stefania - 324CA

<div align="center"><img src="https://tenor.com/view/listening-to-music-spongebob-gif-8009182.gif" width="300px"></div>

## Program's flow
### This project involves the implementation of an application with functionalities similar to Spotify, simulating various user actions. These actions are simulated using commands provided in input files that are read and displayed using json files.

## New commands
The user can now buy merch from an artist and see what they bought. Merch Purchase Attempt:
* Iterate through the artist's merch collection.
* If the specified merch item is found, update revenue, add the merch to the user's collection, and return a success message.
* If the merch item is not found, return an error message indicating that the merch doesn't exist.

The user can also subscribe/unsubscribe from an artist. If the user is subscribed to an artist he is going to receive
notifications from them. The notification system was build using observer design pattern. The artist implements the Subject
interface and implement three methods that notify the subscriber about an event, their merches, or a new album, then the user
(implements Observer) is updated and notified.

The UpdateRecommendations class provides methods for updating and loading user recommendations.
It includes static methods for generating various types of recommendations and updating the user's recommendations based on
a command input. It uses strategy design pattern to help sort the songs by the number of likes(the sorting by name can be used
for artist wrapped).
### fansPlaylist Method:
Generates a playlist based on the top 5 liked songs of the artist's top 5 fans.
  * Retrieves the currently playing song, gets the artist, and then gets the top 5 fans of that artist.
  * Creates a playlist by adding the top 5 liked songs of each fan.
  * Removes duplicate songs and sorts the playlist by likes.
  * Limits the playlist to the top 5 liked songs and updates the user's recommendation playlist.
### randomSong Method:
Generates a random song recommendation based on the genre of the currently playing song.
* Retrieves the currently playing song, collects all songs with the same genre, and randomly selects a song from that genre.
* The recommendation is based on the user's remaining time in the current song, ensuring the selected song fits within the remaining time.

### randomPlaylist Method:
Generates a random playlist based on the user's liked songs, playlists, and followed playlists.
* Counts the occurrence of each genre in the user's songs, sorts genres by count, and selects the top genres.
* Adds top songs from these genres to the playlist.

### updateRecommendations Method:
Updates user recommendations based on the given command input.
* Checks if the user exists and is a normal user.
* Switches based on the recommendation type specified in the command input and calls the corresponding 
 method (fansPlaylist, randomSong, or randomPlaylist).

### loadRecommendation Method:
Loads the user's song recommendation into the player's source for playback.

The PageNavigation class implements the Command interface, indicating that it represents a command pattern for page navigation.
It provides methods to go to the next page (execute) and to go to the previous (undo) for a given user.


(Deleting Users, Artists, and Hosts:

The deleteUser method deletes a user, artist, or host based on the provided command input.
It determines the user type, creates a Deletion instance, and calls the appropriate deleteUser method based on the user type.
* Deletion Class (Deletion):

This inner class implements the UserVisitor interface.
It provides methods to delete an artist, host, or regular user, considering associations with playlists, followers, and current player instances.
It checks whether the artist or host is being played or is set as the current page for any user before deletion.)