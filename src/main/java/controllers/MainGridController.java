package controllers;

import javafx.animation.AnimationTimer;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MainGridController {

    @FXML
    private Button newGameButton;
    @FXML
    private TextField timeField;

    @FXML
    private TextField recordField;

    @FXML
    private TextField counterField;

    @FXML
    private GridPane grid;

    @FXML
    private int counter;

    private long deltaTime;
    private Image image = new Image("view/burger.png");
    private long record = 0;
    private Random random = new Random();
    private LocalDateTime time;
    public LocalDateTime startTime;
    private boolean isFirstTime;
    private boolean countTime = true;
    private Thread timeThread;
    final ImageView imageView = new ImageView(image);
    final BlockingQueue<String> timeQueue = new ArrayBlockingQueue<>(1);
    TimeUpdater timeUpdater;
    final LongProperty lastUpdate = new SimpleLongProperty();
    final long minUpdateInterval = 1000; // nanoseconds. Set to higher number to slow output.

    @FXML
    public void initialize() {
        imageView.setFitHeight(50);
        imageView.setFitWidth(50);
        isFirstTime = true;
        counter = 10;

        newGameButton.setDisable(true);
        setCounterField();

        imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                imageClickHandler();
            }
        });
        grid.add(imageView, random.nextInt(10), random.nextInt(10));

        AnimationTimer timer = new AnimationTimer() {

            @Override
            public void handle(long now) {
                if (now - lastUpdate.get() > minUpdateInterval) {
                    final String timeMessage = timeQueue.poll();
                    if (timeMessage != null) {
                        timeField.setText(timeMessage);
                    }
                    lastUpdate.set(now);
                }
            }
        };
        timer.start();

    }

    private void imageClickHandler() {
        if (isFirstTime) {
            startTime = LocalDateTime.now();
            timeUpdater = new TimeUpdater(timeQueue, true, startTime);
            Thread t = new Thread(timeUpdater);
            t.setDaemon(true);
            t.start();
            isFirstTime = !isFirstTime;
        }
        counter--;
        setCounterField();
        if (counter == 0) {
            winAlertShow();
        } else {
            grid.getChildren().remove(imageView);
            grid.add(imageView, random.nextInt(10), random.nextInt(10));
        }
    }


    private void winAlertShow() {
        countTime = false;
        timeUpdater.setCountTime(false);
        deltaTime = TimeUpdater.getDeltaTime();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game over");
        alert.setHeaderText("The game is over, your time: " + getTimeFormat(deltaTime));
        alert.showAndWait();
        newGameButton.setDisable(false);
        updateRecord();
        grid.getChildren().remove(imageView);
    }

    private void updateRecord() {
        if (record == 0) {
            record = deltaTime;
        } else if (deltaTime < record) {
            record = deltaTime;
        }
        String recordDataFormat = getTimeFormat(record);
        recordField.setText(recordDataFormat);
    }


    private static String getTimeFormat(long value) {
        int amountOfSeconds = (int) value / 1000;
        int minutes = amountOfSeconds / 60;
        int sec = amountOfSeconds % 60;
        int milis = (int) value % 1000;

        return String.format("%02d:%02d:%d", minutes, sec, milis / 10);
    }


    private void setCounterField() {
        counterField.setText(String.valueOf(counter));
    }


    @FXML
    private void handleNewGame() {
        timeField.clear();
        initialize();
    }


    private static class TimeUpdater implements Runnable {
        private final BlockingQueue<String> timeText;
        private boolean countTime;
        private LocalDateTime time;
        private LocalDateTime startTime;
        private static long deltaTime;
        private final long MILIS_IN_ONE_HOUR = 3600000;

        public TimeUpdater(BlockingQueue<String> timeQueue, boolean countTime, LocalDateTime startTime) {
            this.timeText = timeQueue;
            this.countTime = countTime;
            this.startTime = startTime;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    while (countTime) {
                        time = LocalDateTime.now();

                        deltaTime = ChronoUnit.MILLIS.between(startTime, time);
                        System.out.println(deltaTime);
                        String timeRunning = MainGridController.getTimeFormat(deltaTime);
                        timeText.put(timeRunning);
                        if (deltaTime > MILIS_IN_ONE_HOUR) {
                            deltaTime = MILIS_IN_ONE_HOUR - 1;
                            timeRunning = MainGridController.getTimeFormat(deltaTime);
                            timeText.put(timeRunning);
                            countTime = false;
                            break;
                        }
                    }
                }
            } catch (InterruptedException exc) {
                System.out.println("TimeUpdater interrupted: exiting.");
            }
        }

        public void setCountTime(boolean countTime) {
            this.countTime = countTime;
        }

        public static long getDeltaTime() {
            return deltaTime;
        }
    }
}