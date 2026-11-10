package Reproductor;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;
import java.io.ByteArrayInputStream;

import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.images.Artwork;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javafx.application.Application;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.Group;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * MP3 Offline Music Player with JavaFX
 * <p>
 * Manages audio playback using JavaFX.media library
 * </p>
 * 
 * @author Julià O.
 * @version 2.5
*/
public class AudioPlayer extends Application {
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
    private Label currentTimeLabel = new Label("00:00");
    private Label totalTimeLabel = new Label("00:00");
    private Label songTitle = new Label("No current song");
    private Button playButton;
    private FontIcon playIcon = new FontIcon(FontAwesomeSolid.PLAY_CIRCLE);
    private FontIcon pauseIcon = new FontIcon(FontAwesomeSolid.PAUSE_CIRCLE);
    private FontIcon loopIcon = new FontIcon(FontAwesomeSolid.REDO);
    private FontIcon noloopIcon = new FontIcon(FontAwesomeSolid.ARROW_RIGHT);
    private FontIcon randomIcon = new FontIcon(FontAwesomeSolid.RANDOM);
    private FontIcon sequentialIcon = new FontIcon(FontAwesomeSolid.SORT_AMOUNT_DOWN);
    private Slider progressBar;
    private double volume = 0.5;
    private int currentIndex = 0;
    private MediaPlayer mediaPlayer;
    private ImageView coverBackground;
    private boolean loop = false;
    private boolean playing = false;
    private boolean sequential = true;
    private Scene scene;
    private String transparentStyle = "-fx-background-color: transparent; " +
                                      "-fx-border-color: transparent; " +
                                      "-fx-focus-color: transparent; " +
                                      "-fx-faint-focus-color: transparent; " +
                                      "-fx-cursor: hand;";
    private String timeStyle =  "-fx-text-fill: #111111; -fx-font-family: 'Courier New';" + 
                                "-fx-font-size: 14px; -fx-font-weight: bold;";
    private String titleStyle = "-fx-text-fill: #111111; -fx-font-family: 'Times New Roman';" + 
                                "-fx-font-size: 20px; -fx-font-weight: bold;";


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

        ImageView vinyl = new ImageView();
        InputStream isV = getClass().getResourceAsStream("/VINYL.png");
        if (isV != null) {
            Image imageV = new Image(isV);
            vinyl = new ImageView(imageV);
        }
        vinyl.setFitWidth(width);   
        vinyl.setFitHeight(height); 
        vinyl.setLayoutX(0);
        vinyl.setLayoutY(0);

        ImageView coverBackground = new ImageView();
        InputStream isC = getClass().getResourceAsStream("/TRANSPARENT.png");
        if (isC != null) {
            Image imageC = new Image(isC);
            coverBackground = new ImageView(imageC);
        }
        coverBackground.setFitWidth(300);
        coverBackground.setFitHeight(300);
        coverBackground.setPreserveRatio(true);
        //508,488 is the center of the vinyl
        coverBackground.setLayoutX(508 - (300/2));
        coverBackground.setLayoutY(488 - (300/2)); 

        rotation = new RotateTransition(Duration.seconds(15), coverBackground);
        rotation.setByAngle(360);
        rotation.setCycleCount(Animation.INDEFINITE);
        rotation.setInterpolator(Interpolator.LINEAR);

        mainPane.getChildren().addAll(coverBackground, vinyl);

        //Control items
        playButton = new Button();
        playButton.setStyle(transparentStyle);
        playButton.setGraphic(playIcon);
        playIcon.setIconSize(48);
        pauseIcon.setIconSize(48);
        playButton.setFocusTraversable(false);
        playButton.setOnAction(e -> {playPause();});
        
        progressBar = new Slider();
        progressBar.getStyleClass().add("main-slider");
        progressBar.setPrefWidth(300); 
        progressBar.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (progressBar.isValueChanging() && mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
            }                
        });
        ProgressBar atrezzoProgressBar = new ProgressBar(0);
        atrezzoProgressBar.setMouseTransparent(true);
        atrezzoProgressBar.progressProperty().bind(progressBar.valueProperty().divide(progressBar.maxProperty()));
        atrezzoProgressBar.getStyleClass().add("progress-atrezzo");
        atrezzoProgressBar.setPrefWidth(490); 
        StackPane progressBarPane = new StackPane(atrezzoProgressBar, progressBar);
        progressBarPane.setAlignment(Pos.CENTER);
        HBox songTitlePane = new HBox();
        songTitlePane.setMaxWidth(Double.MAX_VALUE); 
        songTitlePane.setAlignment(Pos.CENTER);
        currentTimeLabel.setStyle(timeStyle);
        totalTimeLabel.setStyle(timeStyle);
        currentTimeLabel.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        totalTimeLabel.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        songTitle.setStyle(titleStyle);
        songTitle.setAlignment(Pos.CENTER);
        songTitle.setWrapText(true);
        songTitle.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(songTitle, javafx.scene.layout.Priority.ALWAYS);
        songTitlePane.getChildren().addAll(currentTimeLabel, songTitle, totalTimeLabel);
        VBox progressBarAndTitlePane = new VBox(20);
        progressBarAndTitlePane.setAlignment(Pos.CENTER);
        progressBarAndTitlePane.getChildren().addAll(songTitlePane, progressBarPane);

        Slider volumeSlider = new Slider(0, 100, volume * 100);
        volumeSlider.getStyleClass().add("main-slider");
        volumeSlider.setPrefWidth(150);
        FontIcon volumeIcon = new FontIcon(FontAwesomeSolid.VOLUME_UP);
        volumeIcon.setIconSize(20);

        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            volume = newValue.doubleValue() / 100.0;
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(volume);
                if (volume == 0) {
                    volumeIcon.setIconCode(FontAwesomeSolid.VOLUME_MUTE);
                } else if (volume < 0.7) {
                    volumeIcon.setIconCode(FontAwesomeSolid.VOLUME_DOWN);
                } else {
                    volumeIcon.setIconCode(FontAwesomeSolid.VOLUME_UP);
                }
            }
        });
        ProgressBar atrezzoVolumeSlider = new ProgressBar(0);
        atrezzoVolumeSlider.setMouseTransparent(true);
        atrezzoVolumeSlider.getStyleClass().add("progress-atrezzo");
        atrezzoVolumeSlider.setPrefWidth(150); 
        atrezzoVolumeSlider.progressProperty().bind(volumeSlider.valueProperty().divide(100.0));
        StackPane volumeSliderPane = new StackPane(atrezzoVolumeSlider, volumeSlider);
        
        Button nextSongButton = new Button();
        FontIcon nextSongIcon = new FontIcon(FontAwesomeSolid.STEP_FORWARD);
        nextSongIcon.setIconSize(24);
        nextSongButton.setGraphic(nextSongIcon);
        nextSongButton.setFocusTraversable(false);
        nextSongButton.setStyle(transparentStyle);
        nextSongButton.setOnAction(e -> {next();});
        Button prevSongButton = new Button();
        FontIcon prevSongIcon = new FontIcon(FontAwesomeSolid.STEP_BACKWARD);
        prevSongIcon.setIconSize(24);
        prevSongButton.setGraphic(prevSongIcon);
        prevSongButton.setFocusTraversable(false);
        prevSongButton.setStyle(transparentStyle);
        prevSongButton.setOnAction(e -> {previous();});
        Button shuffleButton = new Button();
        randomIcon.setIconSize(18);
        sequentialIcon.setIconSize(18);
        shuffleButton.setGraphic(sequentialIcon);
        shuffleButton.setFocusTraversable(false);
        shuffleButton.setStyle(transparentStyle);
        shuffleButton.setOnAction(e -> {shuffle(shuffleButton);});
        Button loopButton = new Button();
        loopIcon.setIconSize(18);
        noloopIcon.setIconSize(18);
        loopButton.setGraphic(noloopIcon);
        loopButton.setFocusTraversable(false);
        loopButton.setStyle(transparentStyle);
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

        VBox controlAndProgress = new VBox(30);
        controlAndProgress.setAlignment(Pos.CENTER);
        controlAndProgress.getChildren().addAll(progressBarAndTitlePane, controlButtons);

        HBox songAndVolume = new HBox(20);
        songAndVolume.setAlignment(Pos.CENTER);
        songAndVolume.getChildren().addAll(volumeIcon, volumeSliderPane);

        VBox controlVBox = new VBox(40); 
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
        fileSystem.setFixedCellSize(-1);
        fileSystem.setCellFactory(param -> new ListCell<File>() {
            @Override 
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                    setWrapText(true);
                    setMaxWidth(Double.MAX_VALUE);
                }
            }
        });
        loadDirectory();

        FontIcon goBackIcon = new FontIcon(FontAwesomeSolid.ARROW_LEFT);
        goBackIcon.setIconSize(20);
        goBackIcon.setIconColor(javafx.scene.paint.Color.WHITE); 
        Button goBackButton = new Button();
        goBackButton.setStyle(transparentStyle);
        goBackButton.setGraphic(goBackIcon);
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

        currentFolder.setStyle(
            "-fx-text-fill: white; " + 
            "-fx-font-family: 'Times New Roman'; " + 
            "-fx-font-size: 14px; " + 
            "-fx-font-weight: bold;"
        );

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
        java.net.URL cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("ERROR: /styles.css not found");
        }

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
            playButton.setGraphic(playIcon);

            rotation.pause();
        } else {
            mediaPlayer.play();
            playing = true;
            playButton.setGraphic(pauseIcon);
            
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

        File file = new File(path);
        songTitle.setText(file.getName()); 
        Media media = new Media(file.toURI().toString());
        try {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            if (tag != null) {
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte [] byteArray = artwork.getBinaryData();
                    Image coverImage = new Image (new ByteArrayInputStream(byteArray));
                    coverBackground.setImage(coverImage);
                }
            }
        } catch (Exception e) {
            System.err.println("Error obtaining cover!\n" + e);
        }
        mediaPlayer = new MediaPlayer(media);
        playing = false;

        mediaPlayer.setOnReady(() -> {
            progressBar.setMax(mediaPlayer.getTotalDuration().toSeconds());
            totalTimeLabel.setText(formatTime(mediaPlayer.getTotalDuration()));
            mediaPlayer.setVolume(volume);
        });

        mediaPlayer.currentTimeProperty().addListener((observable, oldTime, newTime) -> {
            progressBar.setValue(newTime.toSeconds());
            currentTimeLabel.setText(formatTime(newTime));
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
            button.setGraphic(randomIcon);
        } else {
            sequential = true;
            button.setGraphic(sequentialIcon);
        }
    }

    /**
     * 
     * @param button
     */
    private void loop(Button button) {
        if (loop) {
            loop = false;
            button.setGraphic(noloopIcon);
        } else {
            loop = true;
            button.setGraphic(loopIcon);
        }
    }

    private String formatTime(Duration duration) {
        if (duration == null) return "00:00";
        long totalSeconds = (long) duration.toSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}