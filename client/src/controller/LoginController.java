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

	// 테스트를 위한 아이피, 포트
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
								System.out.println("[로그인 성공]");

							} catch (IOException e) {
								System.out.println("[로그인 성공 오류]");
								e.printStackTrace();
							}

						});
					} // end of loginSuccess
					try {
						Thread.sleep(100);// 과도한 스레드 사용을 막기위해 0.1초단위로 감지
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

	// 나가기
	private void btnExit(ActionEvent event) {
		Platform.exit();
	}

	// 로그인
	private void btnLogin(ActionEvent event) {
		// 서버로 로그인 요청 보내기
		clientUser.send(ClientProtocol.LOGIN_REQUEST + "|" + txtFieldID.getText() + "|" + pwFieldPW.getText());
	}

	// 서버로부터 로그인 실패시 알림창
	public void alertLoginResult(String data, String id) {
		Platform.runLater(() -> {
			boolean checkIdPw = Boolean.parseBoolean(data);
			if (checkIdPw == true) { // 로그인 성공시 loginSuccess를 true로 만들어 메인스테이지 띄움
				loginSuccess = true;
				clientUser.setId(id);
			} else
				ClientUser.alertDisplay(0, "로그인 실패", "로그인에 실패했습니다", "중복된 로그인이거나 아이디/비밀번호 정보		 를 찾을 수 없습니다");
		});
	}

	// 회원가입
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
			dialog.setTitle("회원가입");
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.setResizable(false);
			dialog.show();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// 서버와 연결&연결해제하는 토글버튼
	private void btnConnectServer(ActionEvent event) {
		Platform.runLater(() -> {
			if (btnConnectServer.getText().equals("서버연결")) {
				clientUser.startClient(IP, port);
				btnConnectServer.setText("연결취소");
				btnLogin.setDisable(false);
				btnJoin.setDisable(false);
			} else if (btnConnectServer.getText().equals("연결취소")) {
				clientUser.stopClient();
				btnLogin.setDisable(true);
				btnJoin.setDisable(true);
				btnConnectServer.setText("서버연결");
			}
		});
	}

	public static ClientUser getClientUser() {
		return LoginController.clientUser;
	}

	// 컨트롤러 세팅을위한 스테이지 셋 함수
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

}
