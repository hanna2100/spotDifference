package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginController implements Initializable {

	@FXML
	private TextField txtFieldID;
	@FXML
	private PasswordField pwFieldPW;
	@FXML
	public Button btnLogin;
	@FXML
	private Button btnJoin;
	@FXML
	private Button btnExit;
	@FXML
	private Button btnConnectServer;

	private static ClientUser clientUser;
	public Stage primaryStage;

	private boolean keepLoginThread = true;
	private boolean loginSuccess = false;

	// �׽�Ʈ�� ���� ������, ��Ʈ
	String IP = "127.0.0.1";
	int port = 9000;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		btnConnectServer.setOnAction(event -> btnConnectServer(event));
		btnExit.setOnAction(event -> btnExit(event));
		btnLogin.setOnAction(event -> btnLogin(event));
		btnLogin.setDisable(true);
		btnJoin.setDisable(true);
		btnJoin.setOnAction(event -> btnJoin(event));

		if (clientUser == null)
			clientUser = new ClientUser();

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (keepLoginThread) {
					if (loginSuccess) {
						Platform.runLater(() -> {
							try {
								keepLoginThread = false;
								loginSuccess = false;

								FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
								Parent main = (Parent) loader.load();
								Scene scene = new Scene(main);

								MainController mainController = loader.getController();
								mainController.setPrimaryStage(primaryStage);
								scene.getStylesheets()
										.add(getClass().getResource("/application/clientcss.css").toExternalForm());
								scene.getStylesheets().add(
										"https://fonts.googleapis.com/css?family=Yeon+Sung&display=swap&subset=korean");
								primaryStage.setScene(scene);
								primaryStage.show();
								System.out.println("[�α��� ����]");

							} catch (IOException e) {
								System.out.println("[�α��� ���� ����]");
								e.printStackTrace();
							}

						});
					} // end of loginSuccess
					try {
						Thread.sleep(100);// ������ ������ ����� �������� 0.1�ʴ����� ����
					} catch (InterruptedException e) {
					}
				} // end of keepLoginThread

			}
		});
		// end of thread

		thread.setDaemon(true);
		thread.start();
		clientUser.setLoginController(this);

	}// end of initialize

	// ������
	private void btnExit(ActionEvent event) {
		Platform.exit();
	}

	// �α���
	private void btnLogin(ActionEvent event) {
		// ������ �α��� ��û ������
		clientUser.send(ClientProtocol.LOGIN_REQUEST + "|" + txtFieldID.getText() + "|" + pwFieldPW.getText());
	}

	// �����κ��� �α��� ���н� �˸�â
	public void alertLoginResult(String data, String id) {
		Platform.runLater(() -> {
			boolean checkIdPw = Boolean.parseBoolean(data);
			if (checkIdPw == true) { // �α��� ������ loginSuccess�� true�� ����� ���ν������� ���
				loginSuccess = true;
				clientUser.setId(id);
			} else
				ClientUser.alertDisplay(0, "�α��� ����", "�α��ο� �����߽��ϴ�", "�ߺ��� �α����̰ų� ���̵�/��й�ȣ ����		 �� ã�� �� �����ϴ�");
		});
	}

	// ȸ������
	private void btnJoin(ActionEvent event) {
		try {
			Stage dialog = new Stage();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/join.fxml"));
			Parent root = (Parent) loader.load();
			Scene scene = new Scene(root);
			JoinController joinController = loader.getController();
			joinController.setPrimaryStage(dialog);
			scene.getStylesheets().add(getClass().getResource("/application/clientcss.css").toExternalForm());
			scene.getStylesheets().add("https://fonts.googleapis.com/css?family=Yeon+Sung&display=swap&subset=korean");
			dialog.setScene(scene);
			dialog.setTitle("ȸ������");
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.setResizable(false);
			dialog.show();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// ������ ����&���������ϴ� ��۹�ư
	private void btnConnectServer(ActionEvent event) {
		Platform.runLater(() -> {
			if (btnConnectServer.getText().equals("��������")) {
				clientUser.startClient(IP, port);
				btnConnectServer.setText("�������");
				btnLogin.setDisable(false);
				btnJoin.setDisable(false);
			} else if (btnConnectServer.getText().equals("�������")) {
				clientUser.stopClient();
				btnLogin.setDisable(true);
				btnJoin.setDisable(true);
				btnConnectServer.setText("��������");
			}
		});
	}

	public static ClientUser getClientUser() {
		return LoginController.clientUser;
	}

	// ��Ʈ�ѷ� ���������� �������� �� �Լ�
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

}
