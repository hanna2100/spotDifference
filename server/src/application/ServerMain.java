package application;

import controller.ServerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerMain extends Application{
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		Parent root = FXMLLoader.load(getClass().getResource("/view/server.fxml"));
		Scene sc = new Scene(root);
		primaryStage.setOnCloseRequest(event-> new ServerController().stopServer());
		primaryStage.setScene(sc);
		primaryStage.show();

	}
	
	//프로그램의 진입점
	public static void main(String[] args) {
		launch(args);

	}


}
