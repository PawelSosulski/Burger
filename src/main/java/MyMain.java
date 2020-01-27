import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MyMain extends Application {


    private AnchorPane root;

    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loaderMainStage = new FXMLLoader();
        loaderMainStage.setLocation(getClass().getResource("/view/mainGrid.fxml"));
        root = loaderMainStage.load();
        //MainStageController mainStageController = loaderMainStage.getController();
        //mainStageController.setMainApp(this);
        primaryStage.setTitle("Running Burger");
        primaryStage.setScene(new Scene(root, primaryStage.getWidth(), primaryStage.getHeight()));
        primaryStage.setResizable(false);
        Image imageIcon = new Image("/view/burger.png");
        primaryStage.getIcons().add(imageIcon);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
