package application;

import java.io.IOException;

import controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientMain extends Application {

	@Override
	public void start(Stage primaryStage) throws IOException {

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
		Parent root = loader.load();
		LoginController loginController = loader.getController();
		loginController.setPrimaryStage(primaryStage);
		Scene scene = new Scene(root);
		//css와 구글무료폰트 적용
		scene.getStylesheets().add(getClass().getResource("/application/clientcss.css").toExternalForm());
		scene.getStylesheets().add("https://fonts.googleapis.com/css?family=Yeon+Sung&display=swap&subset=korean");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();

	}

	public static void main(String[] args) {
		launch(args);
	}

}
