package Reproductor;

import java.util.Scanner;
import java.io.File;

/**
 * MP3 music player protoype
 * Functions: Select song, pause, play, stop.
 * 
 * @author Julià O.
 * @version 1.0 
*/
public class AudioPlayer {
    //ATRIBUTES
    private String songPath;
    private String state = "STOPPED"; //STOPPED,PAUSED,PLAYING
    
    //FUNCTIONS
    public static void main (String [] args) {
        AudioPlayer app = new AudioPlayer();
        app.start();
    }

    public void start() {
        //DECLARATIONS
        //Scanner object that will read everything
        Scanner terminal = new Scanner(System.in);
        boolean choosing = true;

        //ACTIONS
        //Select an option
        while (choosing) {
            switch (menu(terminal)) {
                case 1 -> selectSong(terminal);
                case 2 -> playPause();
                case 3 -> nextSong();
                case 4 -> choosing = false;
                default -> System.err.println("Not an option!");
            }
        }
    }

    /**
     * Selects an option from the Menu interface
     * 
     * @param terminal the {@link Scanner} instance that reads the terminal
     * @return the option value selected by the user
     */
    private int menu(Scanner terminal) {
        //DECLARATIONS
        String text = """
                --- MP3 music player ---
                1.Select song
                2.Play/pause song
                3.Next song
                4.Exit
                ------------------------
                """;
        
        //ACTIONS
        System.out.println(text);
        return Integer.parseInt(terminal.nextLine());
    }

    /**
     * Prompts the user to enter a file path, validates that it points to an existing
     * MP3 file, and updates the player's active song path.
     * 
     * @param terminal the {@link Scanner} instance that reads the terminal
     */
    private void selectSong(Scanner terminal) {
        //DECLARATIONS
        String songPathWritten;
        File song;

        //ACTIONS
        //Obtain the songPath
        System.out.println("Introduce song path:");
        songPathWritten = terminal.nextLine();
        song = new File(songPathWritten);

        //Verify the songpath written (must exist, has to be an archive, .mp3 )
        if (!song.exists()) {
            System.err.println("\nSong path does not exist!");
            return;
        }

        if (!song.isFile()) {
            System.err.println("\nSong path is not a file!");
            return;
        }

        if (!songPathWritten.toLowerCase().endsWith(".mp3")) {
            System.err.println("\nSong path is not an .mp3 file");
            return;
        }

        //Update the songpath of the class
        songPath = songPathWritten;

        //Feedback
        System.out.println("\nSong updated! c:\n");
        return;
    }

    /**
     * description
     * 
     * @param
     * @return
     * @throws
     */
    private void playPause() {

    }

    /**
     * description
     * 
     * @param
     * @return
     * @throws
     */
    private void nextSong() {

    }
}
