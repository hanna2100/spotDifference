package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.RankVO;

public class MyInfomationController implements Initializable {
	@FXML
	private Label lblInfo;
	@FXML
	private Label lblId;
	@FXML
	private Label lblNickname;
	@FXML
	private Button btnOk;
	@FXML
	private Button btnNickChange;
	@FXML
	private Button btnHistory;
	@FXML
	private Button btnPwConfirm;
	@FXML
	private Button btnWithdraw;
	@FXML
	private PasswordField pwField1;
	@FXML
	private PasswordField pwField2;
	@FXML
	private Button btnPwChange;

	private static ClientUser clientUser;
	private ObservableList<RankVO> rankHistory = FXCollections.observableArrayList();

	private Stage dialog;
	private Stage primaryStage;
	private final int pwMinLength = 4; // 비번 최소길이
	private final int pwMaxLength = 10; // 비번 최대길이

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		if (clientUser == null) {
			clientUser = LoginController.getClientUser();
			clientUser.setMyInfomationController(this);
		}
		// 라벨세팅
		lblInfo.setText(clientUser.getId() + "의 정보");
		lblId.setText(clientUser.getId());
		lblNickname.setText(clientUser.getNickname());

		btnPwChange.setDisable(true);
		// 버튼이벤트
		btnNickChange.setOnAction(e -> {
			userNicknameChangeAction();
		});
		// 비번 서로 같은지 확인
		btnPwConfirm.setOnAction(e -> {
			passwordCheckAction();
		});
		// 비번변경
		btnPwChange.setOnAction(e -> {
			passwordChangeAction();
		});
		// 회원탈퇴
		btnWithdraw.setOnAction(e -> {
			applyWithdrawMymembership();
		});
		// 차트보기
		btnHistory.setOnAction(e -> {
			showMyGameHistoryChart();
		});

		btnOk.setOnAction(e -> {
			primaryStage.close();
		});

	}

	// 비번변경
	private void passwordChangeAction() {
		String pw1 = pwField1.getText();
		String pw2 = pwField2.getText();

		if (pw1.length() < pwMinLength || pw2.length() < pwMinLength || pw1.length() > pwMaxLength
				|| pw2.length() > pwMaxLength) {
			ClientUser.alertDisplay(0, "비밀번호 확인", "사용할 수 없는 비밀번호 형식입니다", "비밀번호는 4~10자 사이로 입력해주세요");
		} else {
			if (!pw1.equals(pw2)) {
				ClientUser.alertDisplay(0, "비밀번호 확인", "비밀번호가 일치하지 않습니다");
			} else {
				clientUser.send(ClientProtocol.MYINFO_PWCHANGE + "|" + clientUser.getId() + "|"+pw1);
				
			}
		}

	}

	// 회원탈퇴신청
	private void applyWithdrawMymembership() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("회원탈퇴 경고");
		alert.setHeaderText("탈퇴 시 회원의 모든 정보가 사라집니다. 정말 탈퇴하시겠습니까?");
		alert.setContentText("탈퇴된 회원의 자료는 복구할 수 없습니다.");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			clientUser.send(ClientProtocol.MYINFO_WITHDRAW + "|" + clientUser.getId());
		} else {
			alert.close();
		}
	}

	// 비번 같은지 확인
	private void passwordCheckAction() {
		String pw1 = pwField1.getText();
		String pw2 = pwField2.getText();

		if (pw1.length() < pwMinLength || pw2.length() < pwMinLength || pw1.length() > pwMaxLength
				|| pw2.length() > pwMaxLength) {
			ClientUser.alertDisplay(0, "비밀번호 확인", "사용할 수 없는 비밀번호 형식입니다", "비밀번호는 4~10자 사이로 입력해주세요");
		} else {
			if (!pw1.equals(pw2)) {
				ClientUser.alertDisplay(0, "비밀번호 확인", "비밀번호가 일치하지 않습니다");
			} else {
				ClientUser.alertDisplay(1, "비밀번호 확인", "비밀번호가 일치합니다");
				btnPwChange.setDisable(false);
			}
		}
	}

	// 닉네임 변경하기 다이얼로그창
	private void userNicknameChangeAction() {
		try {
			dialog = new Stage(StageStyle.UTILITY);
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.initOwner(lblInfo.getScene().getWindow());
			dialog.setTitle("닉네임 변경하기");
			Parent root = (VBox) FXMLLoader.load(getClass().getResource("/view/nickchange.fxml"));

			TextField txtField = (TextField) root.lookup("#txtField");
			Button btnDupCheck = (Button) root.lookup("#btnDupCheck");
			Button btnOk = (Button) root.lookup("#btnOk");
			Button btnCancel = (Button) root.lookup("#btnCancel");

			int nameMinLength = 1; // 닉네임 최소길이
			int nameMaxLength = 8; // 닉네임 최대길이

			btnDupCheck.setOnAction(e -> {
				String name = txtField.getText();
				if (name.equals("") || name.length() < nameMinLength || name.length() > nameMaxLength
						|| !name.matches("^[0-9a-zA-Z가-힣]*$")) {
					ClientUser.alertDisplay(0, "닉네임 중복확인", "사용할 수 없는 닉네임 형식입니다", "1~8자 이내의 한글,영문,숫자로 구성하세요");
				} else {
					// DB연동 중복확인
					clientUser.send(ClientProtocol.MYINFO_NICKDUPLICATE + "|" + name);
				}

			});

			btnOk.setOnAction(event -> {
				String name = txtField.getText();
				clientUser.send(ClientProtocol.MYINFO_NICK_CHANGE + "|" + name + "|" + clientUser.getId());
				dialog.close();
			});
			btnCancel.setOnAction(event -> {
				dialog.close();
			});

			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/application/clientcss.css").toExternalForm());
			scene.getStylesheets().add("https://fonts.googleapis.com/css?family=Yeon+Sung&display=swap&subset=korean");
			dialog.setScene(scene);
			dialog.setResizable(false);
			dialog.show();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void showMyGameHistoryChart() {
		try {
			// selectedRankVO = getSelectionModel().getSelectedItems();
			clientUser.send(ClientProtocol.MYINFO_HISTORY + "|" + clientUser.getNickname());
			Thread.sleep(500);
			Parent root = FXMLLoader.load(getClass().getResource("/view/chart.fxml"));
			Stage stage = new Stage(StageStyle.UTILITY);
			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(btnHistory.getScene().getWindow());
			stage.setTitle(clientUser.getNickname() + "의 랭크전적");

			PieChart pieChart = (PieChart) root.lookup("#pieChart");
			LineChart lineChart = (LineChart) root.lookup("#lineChart");
			Button btnRating = (Button) root.lookup("#btnRating");
			Button btnOk = (Button) root.lookup("#btnOk");

			int lastDataIndex = rankHistory.size() - 1;

			pieChart.setData(FXCollections.observableArrayList(
					new PieChart.Data("승리", (double) (rankHistory.get(lastDataIndex).getVictory())),
					new PieChart.Data("패배", rankHistory.get(lastDataIndex).getDefeat())));

			pieChart.setVisible(false);

			XYChart.Series rpHistory = new XYChart.Series(); // chart label
			rpHistory.setName(clientUser.getNickname() + "의 RP 변화");

			ObservableList rpList = FXCollections.observableArrayList();
			for (int i = 0; i < rankHistory.size(); i++) {
				rpList.add(new XYChart.Data(i + "", rankHistory.get(i).getRp()));

				System.out.println("x: " + rankHistory.get(i).getNickname() + ",y =" + rankHistory.get(i).getRp());
			}
			rpHistory.setData(rpList);
			lineChart.getData().add(rpHistory);

			btnRating.setText("승률 보기");
			btnRating.setOnAction(e3 -> {
				if (btnRating.getText().equals("승률 보기")) {
					btnRating.setText("RP 변화 보기");
					lineChart.setVisible(false);
					pieChart.setVisible(true);
				} else {
					btnRating.setText("승률 보기");
					lineChart.setVisible(true);
					pieChart.setVisible(false);
				}
			});

			btnOk.setOnAction(e2 -> {
				stage.close();
			});

			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.show();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	public void alertNickDuplicate(String data) {
		Platform.runLater(() -> {
			boolean duplicate = Boolean.parseBoolean(data); //
			if (duplicate == true) {
				ClientUser.alertDisplay(0, "닉네임 중복 검사", "중복된 닉네임입니다");
			} else {
				ClientUser.alertDisplay(1, "닉네임 중복 검사", "사용 가능한 닉네임입니다");
			}
		});

	}

	public void alertNickChangeResult(String result, String nick) {
		Platform.runLater(() -> {
			if (Boolean.parseBoolean(result) == true) {
				ClientUser.alertDisplay(1, "닉네임 변경 성공", "닉네임이 " + nick + "(으)로 변경되었습니다. 게임을 다시 실행해주세요!",
						"확인을 누르시면 게임이 종료됩니다");
				clientUser.stopClient();
				Platform.exit();
			} else {
				ClientUser.alertDisplay(0, "닉네임 변경 실패", "닉네임을 변경할 수 없습니다", "닉네임 형식이나 서버상태를 확인해주세요");
			}

		});

	}

	public void alertWithdrawResult(String data) {
		Platform.runLater(() -> {
			if (Boolean.parseBoolean(data) == true) {
				ClientUser.alertDisplay(1, "회원탈퇴 완료", "탈퇴가... 완료되었습니다", "쒸익쒸익...게임 꺼진다...");
				clientUser.stopClient();
				Platform.exit();
			} else {
				ClientUser.alertDisplay(0, "회원탈퇴 실패", "탈퇴 할 수 없습니다", "고의가 아니고 진짜로 DB연결을 할 수 없어요..");
			}

		});

	}

	public void getRankHistory(String rankHistorydata) {

		StringTokenizer token = new StringTokenizer(rankHistorydata, "//,");

		rankHistory.removeAll(rankHistory);
		ArrayList<RankVO> temp = new ArrayList<RankVO>();
		while (token.hasMoreTokens()) {
			String nick = token.nextToken();
			int rp = Integer.parseInt(token.nextToken());
			int vic = Integer.parseInt(token.nextToken());
			int def = Integer.parseInt(token.nextToken());
			int to = Integer.parseInt(token.nextToken());
			RankVO rVO = new RankVO(nick, rp, vic, def, to);
			temp.add(rVO);
		}
		for (int i = temp.size() - 1; i >= 0; i--) {
			rankHistory.add(temp.get(i));
		}

	}

	public void getRankHistory() {
		Platform.runLater(() -> {
			clientUser.alertDisplay(0, "전적 보기", "전적을 불러 올 수 없습니다", "DB에 저장된 정보가 없습니다");
		});

	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	public void alertPasswordChangeResult(String data) {
		Platform.runLater(() -> {
			boolean result = Boolean.parseBoolean(data); //
			if (result == true) {
				ClientUser.alertDisplay(1, "비밀번호 변경 완료", "비밀번호 변경이 완료되었습니다");
			} else {
				ClientUser.alertDisplay(0, "비밀번호 변경 실패", "비밀번호 변경요청이 거절되었습니다", "서버 연결상태를 확인해주세요");
			}
		});

	}

}
