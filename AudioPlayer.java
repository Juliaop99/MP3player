package Reproductor;

import java.io.File;
import java.util.Random;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * MP3 Offline Music Player with JavaFX
 * <p>
 * Manages audio playback using JavaFX.media library
 * </p>
 * 
 * @author Julià O.
 * @version 2.0 
*/
public class AudioPlayer extends Application {
    private class History {
        private String [] array;
        private int top;

        public History(int n) {
            array = new String [n];
            top = -1;
        }

        private void add(String item) {
            top += 1;
            top %= array.length;
            array[top] = item;
        }

        private String pop() {
            if (top == -1) {
                return null;
            }

            String lastSong = array[top];
            
            top -= 1;
            top %= array.length;
            return lastSong;
        }
    }
    private History history = new History(20);
    private String path;
    private File mainPath = new File(System.getProperty("user.home"));
    private ListView<File> fileSystem;
    private int currentIndex = 0;
    private MediaPlayer mediaPlayer;
    private boolean playing = false;
    private boolean sequential = true;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        //Control bar items
        Button playButton = new Button("Play");
        playButton.setOnAction(e -> {playPause();});
        Button nextSongButton = new Button("Next");
        nextSongButton.setOnAction(e -> {next();});
        Button prevSongButton = new Button("Previous");
        prevSongButton.setOnAction(e -> {previous();});

        HBox controlBar = new HBox(15);
        controlBar.setAlignment(Pos.CENTER);
        controlBar.setStyle("-fx-padding: 15px; -fx-background-color: #222222;");
        controlBar.getChildren().addAll(prevSongButton,playButton,nextSongButton);

        root.setBottom(controlBar);

        //File system
        fileSystem = new ListView<>();

        // File [] allFiles = mainPath.listFiles(file -> {
        //     if (file.isDirectory()) {
        //         return true;
        //     }
        //     if (file.isFile()) {
        //         String lowerCaseFile = file.getName().toLowerCase();
        //         return (lowerCaseFile.endsWith(".mp3")); 
        //     }
        //     return false;
        // });

        // if (allFiles != null) {
        //     fileSystem.getItems().clear();
        //     fileSystem.getItems().addAll(allFiles);
        // }

        fileSystem.setCellFactory(param -> new ListCell<File>() {
            @Override 
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        loadDirectory();


        Button goBackButton = new Button("<-");
        //goBackButton.setOnAction(e -> {goBack();});

        Label currentFolder = new Label();
        currentFolder.setText("Path: " + mainPath);

        fileSystem.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                File newValue = fileSystem.getSelectionModel().getSelectedItem();

                if (newValue != null) {
                    int index = fileSystem.getSelectionModel().getSelectedIndex();
                
                    if (!newValue.isDirectory()) {
                        currentIndex = index;
                        path = newValue.getAbsolutePath();
                        initMediaPlayer();
                    } else {
                        mainPath = newValue;
                        loadDirectory();
                        currentFolder.setText(mainPath.getAbsolutePath());
                    }
                }
            }
        });

        HBox pathBar = new HBox(5);
        pathBar.setAlignment(Pos.CENTER_LEFT);
        //pathBar.setStyle("");
        pathBar.getChildren().addAll(goBackButton, currentFolder);

        VBox fileBox = new VBox();
        fileBox.setAlignment(Pos.CENTER_LEFT);
        //fileBox.setStyle
        fileBox.getChildren().addAll(pathBar, fileSystem);
        VBox.setVgrow(fileSystem, Priority.ALWAYS);
        
        root.setLeft(fileBox);


        //Final scene config
        Scene scene = new Scene(root,500,300);
        primaryStage.setTitle("Mp3 player");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main (String [] args) {
        launch(args);
    }
    
    private void playPause() {
        if (playing) {
            mediaPlayer.pause();
            playing = false;
        } else {
            mediaPlayer.play();
            playing = true;
        }
    }

    private void next() {
        history.add(path);

        if (sequential) {
            nextSequential();
        } else {
            nextRandom();
        }

        initMediaPlayer();
        playPause();
    }

    private void nextRandom() {

    }

    private void nextSequential() {

    }

    private void previous() {
        String previousSong = history.pop();

        if (previousSong == null) {
            mediaPlayer.stop();
            mediaPlayer.play();
        } else {
            path = previousSong;
            initMediaPlayer();
        }
    }

    private void initMediaPlayer() {
        if (mediaPlayer != null) {
           mediaPlayer.dispose(); 
        }

        Media media = new Media(new File(path).toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        mediaPlayer.setOnEndOfMedia(() -> {
           playing = false; 
           next(); 
        });
    }

    private void loadDirectory() {
        File [] allFiles = mainPath.listFiles(file -> {
            if (file.isDirectory()) {
                return true;
            }
            if (file.isFile()) {
                String lowerCaseFile = file.getName().toLowerCase();
                return (lowerCaseFile.endsWith(".mp3")); 
            }
            return false;
        });

        if (allFiles != null) {
            fileSystem.getItems().clear();
            fileSystem.getItems().addAll(allFiles);
        }
    }
}