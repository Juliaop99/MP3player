package Reproductor;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.util.Duration;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
 * @version 2.1
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
    private File [] allFiles;
    private Label currentFolder = new Label();
    private Button playButton;
    private Slider progressBar;
    private double volume;
    private int currentIndex = 0;
    private MediaPlayer mediaPlayer;
    private boolean loop = false;
    private boolean playing = false;
    private boolean sequential = true;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        //Control bar items
        playButton = new Button("Play");
        playButton.setOnAction(e -> {playPause();});
        progressBar = new Slider();
        progressBar.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (progressBar.isValueChanging()) {
                mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
            }                
        });
        Slider volumeSlider = new Slider();
        volumeSlider.setMin(0);
        volumeSlider.setMax(100);
        volumeSlider.setValue(50);
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            volume = newValue.doubleValue() / 100.0;
    
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(volume);
            }
        });
        Button nextSongButton = new Button("Next");
        nextSongButton.setOnAction(e -> {next();});
        Button prevSongButton = new Button("Previous");
        prevSongButton.setOnAction(e -> {previous();});
        Button shuffleButton = new Button("Sequential");
        shuffleButton.setOnAction(e -> {shuffle(shuffleButton);});
        Button loopButton = new Button("No loop");
        loopButton.setOnAction(e -> {loop(loopButton);});

        HBox controlBar = new HBox(15);
        controlBar.setAlignment(Pos.CENTER);
        controlBar.setStyle("-fx-padding: 15px; -fx-background-color: #222222;");
        controlBar.getChildren().addAll(shuffleButton,prevSongButton,playButton,nextSongButton,loopButton,progressBar);

        root.setBottom(controlBar);

        //File system
        fileSystem = new ListView<>();
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
        goBackButton.setOnAction(e -> {goBack();});

        fileSystem.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                File newValue = fileSystem.getSelectionModel().getSelectedItem();

                if (newValue != null) {
                    int index = fileSystem.getSelectionModel().getSelectedIndex();
                    
                    if (!newValue.isDirectory()) {
                        currentIndex = index;
                        path = newValue.getAbsolutePath();
                        initMediaPlayer();
                        playPause();
                    } else {
                        mainPath = newValue;
                        loadDirectory();
                    }
                }
            }
        });

        HBox pathBar = new HBox(5);
        pathBar.setAlignment(Pos.CENTER_LEFT);
        //pathBar.setStyle("");
        pathBar.getChildren().addAll(goBackButton, currentFolder);

        VBox fileBox = new VBox();
        fileBox.setAlignment(Pos.TOP_LEFT);
        //fileBox.setStyle
        fileBox.getChildren().addAll(pathBar, fileSystem);
        //VBox.setVgrow(fileSystem, Priority.ALWAYS);
        
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
            playButton.setText("Play");
        } else {
            mediaPlayer.play();
            playing = true;
            playButton.setText("Pause");
        }
    }

    private void next() {
        if (!loop) {
            history.add(path);

            if (sequential) {
                currentIndex = (currentIndex + 1) % allFiles.length;
            } else {
                Random r = new Random();
                currentIndex = (r.nextInt(allFiles.length - 1));
            }
        }

        path = allFiles[currentIndex].getAbsolutePath();
        initMediaPlayer();
        playPause();
    }

    private void previous() {
        String previousSong = history.pop();

        if (previousSong == null) {
            mediaPlayer.stop();
            mediaPlayer.play();
        } else {
            currentIndex -= 1;
            path = previousSong;
            initMediaPlayer();
            playPause();
        }
    }

    private void initMediaPlayer() {
        if (mediaPlayer != null) {
           mediaPlayer.dispose(); 
        }

        Media media = new Media(new File(path).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        playing = false;

        mediaPlayer.setOnReady(() -> {
            progressBar.setMax(mediaPlayer.getTotalDuration().toSeconds());
            mediaPlayer.setVolume(volume);
        });

        mediaPlayer.currentTimeProperty().addListener((observable, oldTime, newTime) -> {
            progressBar.setValue(newTime.toSeconds());
        });

        mediaPlayer.setOnEndOfMedia(() -> {
           playing = false; 
           next(); 
        });
    }

    private void loadDirectory() {
        allFiles = mainPath.listFiles(file -> {
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
            sortListView();
            fileSystem.getItems().clear();
            fileSystem.getItems().addAll(allFiles);
            currentFolder.setText(mainPath.getAbsolutePath());
        }
    }

    private void sortListView() {
        Arrays.sort(allFiles, (file1,file2) -> {
            if (file1.isDirectory() && !file2.isDirectory()) {
                return -1;
            } else if (file2.isDirectory() && !file1.isDirectory()) {
                return 1;
            }

            return file1.getName().compareTo(file2.getName());
        });
    }

    private void goBack() {
        String superDirectory = mainPath.getParent();

        if (superDirectory != null) {
            File superFileDirectory = new File(superDirectory);
            mainPath = superFileDirectory;
            loadDirectory();
        } else {
            //Block the button if it has no parent (not necessary)
        }
    }

    private void shuffle(Button button) {
        if (sequential) {
            sequential = false;
            button.setText("Random");
        } else {
            sequential = true;
            button.setText("Sequential");
        }
    }

    private void loop(Button button) {
        if (loop) {
            loop = false;
            button.setText("No loop");
        } else {
            loop = true;
            button.setText("Looping");
        }
    }
}