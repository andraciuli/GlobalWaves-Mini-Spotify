# Proiect GlobalWaves
## Ciulinca Andra Stefania - 324CA

<div align="center"><img src="https://tenor.com/view/listening-to-music-spongebob-gif-8009182.gif" width="300px"></div>

## Program's flow
### This project involves the implementation of an application with functionalities similar to Spotify, simulating various user actions. These actions are simulated using commands provided in input files that are read and displayed using json files.
## Entities
* Audio File
  * Song
  * Podcast Episode
* Audio Collections
  * Library: Represents all songs on the platform. All users have access to the entire library.
  * Playlist: A collection of songs created by a user. It can be public or private.
  * Podcast: A collection of episodes with a specified order.
  * Album: A collection of songs added by an artist - new.
* Users
  * Artist - user that can add/remove albums, events, merch and have their own page in the system
  * Host - user that can add/remove podcasts, announcement and have thei own page
  * Normal user - can access the following commands
    * Connection Status: The user can switch between online and offline status, affecting playback.
      The user can check and change their connection status.
      The user can switch between online and offline status, affecting playback.
      The user can check and change their connection status.
    * Search: The user can perform searches using a search bar with specified filters and types.
    * Selection: The user can select items from search results, with different actions based on the type of search.
    * Playback Control:The user can load, play/pause, repeat, shuffle, forward, and rewind audio playback.
      Like/unlike a song.
      Skip to the next or previous track.
    * Playlist Actions: The user can create playlists, add/remove songs to/from playlists, and switch playlist visibility.
      The user can follow/unfollow playlists and view their own playlists.
    * Stats and Preferences: The user can view player statistics.
      The user can view preferred songs and preferred genre based on liked songs.

The program firstly initializes data structures for users, songs, and playlists and processes 
a series of commands to simulate user interactions with the platform. The main functionalities 
include searching, selecting, loading, managing playlists, and obtaining user and general statistics. 
A loop processes each command from the input. Depending on the command type, specific actions are taken.
The results of each command are stored in the outputs object.
####
The SearchBar class is an integral part of the music platform simulation, providing methods for 
searching songs, podcasts, and playlists based on specified filters. The class efficiently manages 
the state of whether a search has been performed using the searched variable. 
This is crucial for ensuring that subsequent actions in the simulation are based on whether a valid 
search has been executed. For playlists we take into account the visibility status (public or private).
The search results are stored in an ArrayNode and returned as an ArrayList of names.
Then the user is able to select an item from search results. The select method checks whether
a search has been conducted, and if not, displays an error message. If the selected item number is too high,
it also displays an error message. If the selection is successful, it clears the search status, sets the 
selection status to true, and returns the selected item's name.
####
The Player class is responsible for managing the playback of the selected item in the music 
application. The result is loaded into the player and starts playing.The checkEpisode method is responsible 
for checking the current episode of a podcast, updating the time watched, and moving to the next episode 
if necessary. This ensures accurate tracking of podcast playback.The getRemainedTime method calculates 
the remaining time for the currently loaded item. It considers factors such as pausing, loading time, 
and podcast episode durations. This information is crucial for displaying accurate playback status.
The playPause method toggles the playback state between play and pause. It updates relevant timestamps and 
status messages, ensuring smooth user interaction.
####
The Playlist class is responsible for managing playlists in the music application. It encapsulates information
such as playlist name, owner, songs, visibility, and followers. The class includes methods for various user 
interactions related to playlists, such as creating a playlist, switching visibility, displaying user
playlists, adding/removing songs, following/unfollowing playlists, and retrieving the top 5 playlists
based on followers. When creating a playlist we add it to the user's playlist collection and to the playlist
collection available to every user. To add/remove a song from a playlist firstly, if the source is a song, 
it iterates through the library to find the specified song by name. It then iterates through the user's 
playlists to find the specified playlist based on the given playlist ID.
If the playlist is found, it checks whether the playlist already contains the specified song.
If the song is present, it is removed from the playlist. If not, the song is added to the playlist.
The method also updates the playlist in the owner's list of playlists. The userFollow method allows users 
to follow or unfollow a playlist. It checks if a source is selected, if it's a playlist, and if the playlist 
visibility is public. It also checks if the playlist owner is not the user performing the operation. The user 
can also like a song and have a list of preferred songs.
The PageCommands class encapsulates functionalities related to managing user interaction
with different pages in the music and podcast application. The methods provide a way to
retrieve and display information about liked content, followed playlists, and the current
page. The class contributes to the modular design of the overall application. The class
interacts with other classes in the app package, such as Admin, Artist, Host, User, and
various collections like Album, Playlist, and Song. The code utilizes Java Streams for
concise and expressive operations, such as sorting and mapping.

The Java class, Artist, represents an artist in the music application. The class extends LibraryEntry and implements
the UserVisitable interface. It contains nested classes for representing events (Event) and 
merchandise items (Merch) associated with the artist.Implements the acceptVisitor method from
the UserVisitable interface, allowing the artist to accept a deletion visitor. Provides a method getNumberLikes that
calculates the total number of likes for the artist based on the likes of their songs in albums.Provides methods to add 
and remove albums (addAlbum, removeAlbum).
Checks for duplicate album names and duplicate songs within the input when adding a new album.
Handles the removal of albums, considering whether they are currently being used by any player in the system.
In summary, the Artist class encapsulates functionalities related to managing albums, events, and merchandise for
an artist within the music and podcast application. It follows principles of encapsulation and provides methods for
performing various operations on artist-related entities.

The Java class, Host, represents a host in a music and podcast application. The class extends LibraryEntry and
implements the UserVisitable interface.
It contains an inner class, Announcement, to represent announcements made by the host.Implements the acceptVisitor
method from the UserVisitable interface, allowing the host to accept a deletion visitor.
Provides methods to add and remove podcasts (addPodcast, removePodcast).
Checks for duplicate podcast names and duplicate episodes within the input when adding a new podcast.
Handles the removal of podcasts, considering whether they are currently being used by any player in the system.
The Host class encapsulates functionalities related to managing podcasts and announcements for a host
within the music and podcast application. It follows principles of encapsulation and provides methods for performing
various operations on host-related entities.

There are new commands added to the Admin class too:
* Adding Users, Artists, and Hosts:

The addUser method adds a new user, artist, or host based on the provided command input.
It checks if the username is already taken and creates a new user instance accordingly.
For artists and hosts, it calls addArtist and addHost methods, respectively, to perform additional tasks.
The addArtist and addHost methods set the connection status to offline, add the new user to the users list,
and check if the artist or host already exists before adding them to the corresponding list (artists or hosts).
* Deleting Users, Artists, and Hosts:

The deleteUser method deletes a user, artist, or host based on the provided command input.
It determines the user type, creates a Deletion instance, and calls the appropriate deleteUser method based on the user type.
* Deletion Class (Deletion):

This inner class implements the UserVisitor interface.
It provides methods to delete an artist, host, or regular user, considering associations with playlists, followers, and current player instances.
It checks whether the artist or host is being played or is set as the current page for any user before deletion.
It updates playlists and followers when deleting a user.
* Online Users and Sorting:

The getOnlineUsers method retrieves a list of usernames for online users based on their connection status.
The UserComparatorByType class is a comparator used for sorting users based on their user type in a predefined
order ("user" < "artist" < "host").
The getAllUsers method retrieves a sorted list of all usernames based on user type.
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
