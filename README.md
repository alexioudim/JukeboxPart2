
# Jukebox

A simple music player that plays mp3 and m3u files.

## Compile

Compile Jukebox using the following maven command inside the project directory:

```bash
mvn package
```
## Run 

After compiling the project using maven, a jar file will be created inside the target folder of the project directory

Use the following command to run the project:

```bash
java -jar [filename]
```

## Functions and buttons

Import List: Lets the user choose a directory of mp3 files, a singular mp3 file or an m3u file.These files then get imprted and show up in the list of the player.

Song list: Shows all the imported songs with their metadata(title,artist etc.). The user can double-click each song on the list to play it.

Play-Pause: Plays/resumes and pauses the current song. 

Random: Randomizes the order of the song list.

Loop: Plays the current song on loop. 

Stop: Permanently stops the current song.

Forward: Plays the next song. The next song is determined on whether the Random button is pressed or not.

Backward: Rewinds the current song and plays it from the beginning. 







