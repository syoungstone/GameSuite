import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PoolUI extends Application {

    private PoolTable table = new PoolTable();
    private Scene scene = new Scene(table, PoolTable.SCENE_WIDTH, PoolTable.SCENE_HEIGHT);

    @Override
    public void start(Stage primaryStage) {

        // Sets title and scene for primaryStage, makes window size constant, and launches window
        primaryStage.setTitle("8-ball Pool");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
