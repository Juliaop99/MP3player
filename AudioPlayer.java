package Reproductor;

import java.io.File;
import java.util.Arrays;
import java.util.Random;
//import java.util.prefs.*;

import javafx.application.Application;
import javafx.animation.RotateTransition;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.Group;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Pane;

/**
 * MP3 Offline Music Player with JavaFX
 * <p>
 * Manages audio playback using JavaFX.media library
 * </p>
 * 
 * @author Julià O.
 * @version 2.4
*/
public class AudioPlayer extends Application {
    //private static final Preferences prefs = Preferences.userRoot().node("MP3Player");
    private RotateTransition rotation;
    private static final double width = 1672; 
    private static final double height = 941;  
    /**
     * 
     */
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
    private double volume = 0.5;
    private int currentIndex = 0;
    private MediaPlayer mediaPlayer;
    private boolean loop = false;
    private boolean playing = false;
    private boolean sequential = true;
    private Scene scene;


    /**    
     * 
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage primaryStage) {
        Pane mainPane = new Pane();
        Pane configPane = new Pane();

        // Main pane
        mainPane.setPrefSize(width, height);
        mainPane.setMinSize(width, height);
        mainPane.setMaxSize(width, height);
        mainPane.setClip(new Rectangle(width, height));

        ImageView cover = new ImageView("file:/home/julia/Documents/VSC/Reproductor/COVER.png");
        
        ImageView vinyl = new ImageView("file:/home/julia/Documents/VSC/Reproductor/VINYL.png");
        vinyl.setFitWidth(width);   
        vinyl.setFitHeight(height); 
        vinyl.setLayoutX(0);
        vinyl.setLayoutY(0);

        mainPane.getChildren().addAll(cover, vinyl);

        //Control items
        playButton = new Button("Play");
        playButton.setFocusTraversable(false);
        playButton.setOnAction(e -> {playPause();});
        
        progressBar = new Slider();
        progressBar.setPrefWidth(300); 
        progressBar.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (progressBar.isValueChanging() && mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
            }                
        });
        
        Slider volumeSlider = new Slider(0, 100, volume * 100);
        volumeSlider.setPrefWidth(150);
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            volume = newValue.doubleValue() / 100.0;
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(volume);
            }
        });
        
        Button nextSongButton = new Button("Next");
        nextSongButton.setFocusTraversable(false);
        nextSongButton.setOnAction(e -> {next();});
        Button prevSongButton = new Button("Previous");
        prevSongButton.setFocusTraversable(false);
        prevSongButton.setOnAction(e -> {previous();});
        Button shuffleButton = new Button("Sequential");
        shuffleButton.setFocusTraversable(false);
        shuffleButton.setOnAction(e -> {shuffle(shuffleButton);});
        Button loopButton = new Button("No loop");
        loopButton.setFocusTraversable(false);
        loopButton.setOnAction(e -> {loop(loopButton);});
        Button configButton = new Button("Config");
        configButton.setFocusTraversable(false);
        configButton.setOnAction(e -> {
            mainPane.setVisible(false); 
            configPane.setVisible(true); 
        });

        HBox controlButtons = new HBox(15);
        controlButtons.setAlignment(Pos.CENTER);
        controlButtons.getChildren().addAll(shuffleButton,prevSongButton,playButton,nextSongButton,loopButton);

        VBox controlAndProgress = new VBox(15);
        controlAndProgress.setAlignment(Pos.CENTER);
        controlAndProgress.getChildren().addAll(controlButtons, progressBar);

        HBox songAndVolume = new HBox(100);
        songAndVolume.setAlignment(Pos.CENTER);
        songAndVolume.getChildren().addAll(volumeSlider);

        VBox controlVBox = new VBox(100); 
        controlVBox.setAlignment(Pos.CENTER); 
        controlVBox.setLayoutX(950); 
        controlVBox.setLayoutY(550); 
        controlVBox.setPrefWidth(540);
        controlVBox.setPrefHeight(260);
        controlVBox.setMinSize(540, 260); 
        controlVBox.setMaxSize(540, 260);
        controlVBox.setStyle("-fx-padding: 15px;");
        
        controlVBox.getChildren().addAll(controlAndProgress,songAndVolume);
        mainPane.getChildren().add(controlVBox);

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
        pathBar.getChildren().addAll(goBackButton, currentFolder);

        VBox fileBox = new VBox(10);
        fileBox.getChildren().addAll(pathBar, fileSystem);
        fileBox.setLayoutX(570);
        fileBox.setLayoutY(170); 
        fileBox.setPrefWidth(210); 
        fileBox.setPrefHeight(560);
        fileBox.setMinSize(210, 560); 
        fileBox.setMaxSize(210, 560); 
        fileBox.setStyle("-fx-padding: 15px;");
        
        VBox.setVgrow(fileSystem, javafx.scene.layout.Priority.ALWAYS); 
        
        mainPane.getChildren().add(fileBox);

        // Configuration Pane
        configPane.setPrefSize(width, height);
        configPane.setMinSize(width, height);
        configPane.setMaxSize(width, height);
        configPane.setClip(new Rectangle(width, height));
        configPane.setVisible(false);
        configPane.setStyle("-fx-background-color: #222;");
        
        TextField hola = new TextField("Hola");
        hola.setLayoutX(600);
        hola.setLayoutY(400);
        hola.setPrefWidth(400);
        
        Button mainPageButton = new Button("MainPage");
        mainPageButton.setLayoutX(600);
        mainPageButton.setLayoutY(460);
        mainPageButton.setOnAction(e -> {
            configPane.setVisible(false); 
            mainPane.setVisible(true); 
        });
        
        configPane.getChildren().addAll(hola, mainPageButton);

        // Group + Scale + Translate
        Group grupo = new Group(mainPane, configPane);

        grupo.setManaged(false); 

        Scale scale = new Scale(1, 1, 0, 0);
        Translate translate = new Translate(0, 0);
        grupo.getTransforms().addAll(translate, scale);

        Pane root = new Pane();
        root.setStyle("-fx-background-color: #111111;"); 
        root.getChildren().add(grupo);

        // Final scene config
        scene = new Scene(root, 1000, 562); 

        // Scaling
        Runnable actualizarEscala = () -> {
            double w = scene.getWidth();
            double h = scene.getHeight();
            
            if (w > 0 && h > 0) {
                double factor = Math.max(w / width, h / height);
                
                scale.setX(factor);
                scale.setY(factor);
                
                translate.setX((w - (width * factor)) / 2.0);
                translate.setY((h - (height * factor)) / 2.0);
            }
        };

        primaryStage.setOnShown(e -> actualizarEscala.run());
        scene.widthProperty().addListener((o, a, n) -> actualizarEscala.run());
        scene.heightProperty().addListener((o, a, n) -> actualizarEscala.run());

        // Keyboard shortcuts
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.SPACE) playPause();
            else if (e.getCode() == KeyCode.N) next();
            else if (e.getCode() == KeyCode.P) previous();
            e.consume();
        });

        primaryStage.setTitle("Mp3 player");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * 
     * 
     * @param args
     */
    public static void main (String [] args) {
        launch(args);
    }
    
    /**
     * 
     * 
     */
    private void playPause() {
        if (playing) {
            mediaPlayer.pause();
            playing = false;
            playButton.setText("Play");

            rotation.pause();
        } else {
            mediaPlayer.play();
            playing = true;
            playButton.setText("Pause");
            
            rotation.play();
        }
    }

    /**
     * 
     */
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

    /**
     * 
     */
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

    /**
     * 
     */
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

    /**
     * 
     */
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

    /**
     * 
     */
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

    /**
     * 
     */
    private void goBack() {
        String superDirectory = mainPath.getParent();

        if (superDirectory != null) {
            File superFileDirectory = new File(superDirectory);
            mainPath = superFileDirectory;
            loadDirectory();
        }
    }

    /**
     * 
     * @param button
     */
    private void shuffle(Button button) {
        if (sequential) {
            sequential = false;
            button.setText("Random");
        } else {
            sequential = true;
            button.setText("Sequential");
        }
    }

    /**
     * 
     * @param button
     */
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