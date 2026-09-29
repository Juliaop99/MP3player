package Reproductor;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;

/**
 * Terminal-based MP3 Audio Player Prototype (v1.0).
 * <p>
 * Manages audio playback using JavaZoom's JLayer library (AdvancedPlayer) in a dedicated
 * background thread to keep the console menu responsive.
 * </p>
 * 
 * @author Julià O.
 * @version 1.0 
*/
public class AudioPlayer implements Runnable {
    //ATRIBUTES
    /** Tracks the frame index for playback tracking. */
    private int currentFrame;

    /** Absolute or relative system path to the target .mp3 file. */
    private String songPath = null;

    /** Current state of the player: "STOPPED", "PAUSED", or "PLAYING". */
    private String state = "STOPPED";

    /** JLayer AdvancedPlayer instance running on the worker thread. */
    private AdvancedPlayer player;
    
    //FUNCTIONS
    public static void main (String [] args) {
        AudioPlayer app = new AudioPlayer();
        app.start();
    }

    /**
     * Executes audio playback in a dedicated background thread.
     * <p>
     * Initializes the {@link AdvancedPlayer} stream, attaches a {@link PlaybackListener} 
     * to monitor playback progress, and manages state transitions upon song completion.
     * </p>
     */
    @Override 
    public void run() {
        try (FileInputStream stream = new FileInputStream(songPath)) {
            player = new AdvancedPlayer(stream);
            player.setPlayBackListener(new PlaybackListener() {
                @Override
                public void playbackFinished(PlaybackEvent event) {
                    //System.out.println("[DEBUG] Event frame: " + event.getFrame());
                    currentFrame += event.getFrame();
                    //System.out.println("[DEBUG] Current accumulated frame: " + currentFrame);
                }
            });

            System.out.println("\nPlaying: " + songPath);
            state = "PLAYING";
            player.play(currentFrame, Integer.MAX_VALUE);
            if ("PLAYING".equals(state)) {
                state = "STOPPED";
                currentFrame = 0;
                System.out.println("\nSong finished");
            }
        }
        catch (Exception e) {
            System.err.println("\n[ERROR] Playback failed: " + e.getMessage());
        }
    }

    /**
     * Starts the main interactive terminal loop.
     * Continuously prompts the user for menu options until the exit command is triggered.
     */
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
                case 3 -> choosing = false;
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
                2.Play/stop song
                3.Exit
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
        System.out.println("Introduce song path:");
        songPathWritten = terminal.nextLine();
        song = new File(songPathWritten);

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

        songPath = songPathWritten;

        System.out.println("\nSong updated! c:\n");
        return;
    }

    /**
     * Toggles playback state.
     * Stops the active {@link AdvancedPlayer} instance if audio is currently playing, 
     * or launches a new worker thread to begin or resume playback if stopped/paused.
     */
    private void playPause() {
        switch (state) {
            case "PLAYING":
                state = "PAUSED";
                player.stop();
                break;
            
            default:
                Thread music = new Thread(this);
                music.start();
                break;
        }
    }
}
