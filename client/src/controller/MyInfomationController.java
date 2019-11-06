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
	private final int pwMinLength = 4; // ºñ¹ø ÃÖ¼Ò±æÀÌ
	private final int pwMaxLength = 10; // ºñ¹ø ÃÖ´ë±æÀÌ

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		if (clientUser == null) {
			clientUser = LoginController.getClientUser();
			clientUser.setMyInfomationController(this);
		}
		// ¶óº§¼¼ÆÃ
		lblInfo.setText(clientUser.getId() + "ÀÇ Á¤º¸");
		lblId.setText(clientUser.getId());
		lblNickname.setText(clientUser.getNickname());

		btnPwChange.setDisable(true);
		// ¹öÆ°ÀÌº¥Æ®
		btnNickChange.setOnAction(e -> {
			userNicknameChangeAction();
		});
		// ºñ¹ø ¼­·Î °°ÀºÁö È®ÀÎ
		btnPwConfirm.setOnAction(e -> {
			passwordCheckAction();
		});
		// ºñ¹øº¯°æ
		btnPwChange.setOnAction(e -> {
			passwordChangeAction();
		});
		// È¸¿øÅ»Åð
		btnWithdraw.setOnAction(e -> {
			applyWithdrawMymembership();
		});
		// Â÷Æ®º¸±â
		btnHistory.setOnAction(e -> {
			showMyGameHistoryChart();
		});

		btnOk.setOnAction(e -> {
			primaryStage.close();
		});

	}

	// ºñ¹øº¯°æ
	private void passwordChangeAction() {
		String pw1 = pwField1.getText();
		String pw2 = pwField2.getText();

		if (pw1.length() < pwMinLength || pw2.length() < pwMinLength || pw1.length() > pwMaxLength
				|| pw2.length() > pwMaxLength) {
			ClientUser.alertDisplay(0, "ºñ¹Ð¹øÈ£ È®ÀÎ", "»ç¿ëÇÒ ¼ö ¾ø´Â ºñ¹Ð¹øÈ£ Çü½ÄÀÔ´Ï´Ù", "ºñ¹Ð¹øÈ£´Â 4~10ÀÚ »çÀÌ·Î ÀÔ·ÂÇØÁÖ¼¼¿ä");
		} else {
			if (!pw1.equals(pw2)) {
				ClientUser.alertDisplay(0, "ºñ¹Ð¹øÈ£ È®ÀÎ", "ºñ¹Ð¹øÈ£°¡ ÀÏÄ¡ÇÏÁö ¾Ê½À´Ï´Ù");
			} else {
				clientUser.send(ClientProtocol.MYINFO_PWCHANGE + "|" + clientUser.getId() + "|"+pw1);
				
			}
		}

	}

	// È¸¿øÅ»Åð½ÅÃ»
	private void applyWithdrawMymembership() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("È¸¿øÅ»Åð °æ°í");
		alert.setHeaderText("Å»Åð ½Ã È¸¿øÀÇ ¸ðµç Á¤º¸°¡ »ç¶óÁý´Ï´Ù. Á¤¸» Å»ÅðÇÏ½Ã°Ú½À´Ï±î?");
		alert.setContentText("Å»ÅðµÈ È¸¿øÀÇ ÀÚ·á´Â º¹±¸ÇÒ ¼ö ¾ø½À´Ï´Ù.");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			clientUser.send(ClientProtocol.MYINFO_WITHDRAW + "|" + clientUser.getId());
		} else {
			alert.close();
		}
	}

	// ºñ¹ø °°ÀºÁö È®ÀÎ
	private void passwordCheckAction() {
		String pw1 = pwField1.getText();
		String pw2 = pwField2.getText();

		if (pw1.length() < pwMinLength || pw2.length() < pwMinLength || pw1.length() > pwMaxLength
				|| pw2.length() > pwMaxLength) {
			ClientUser.alertDisplay(0, "ºñ¹Ð¹øÈ£ È®ÀÎ", "»ç¿ëÇÒ ¼ö ¾ø´Â ºñ¹Ð¹øÈ£ Çü½ÄÀÔ´Ï´Ù", "ºñ¹Ð¹øÈ£´Â 4~10ÀÚ »çÀÌ·Î ÀÔ·ÂÇØÁÖ¼¼¿ä");
		} else {
			if (!pw1.equals(pw2)) {
				ClientUser.alertDisplay(0, "ºñ¹Ð¹øÈ£ È®ÀÎ", "ºñ¹Ð¹øÈ£°¡ ÀÏÄ¡ÇÏÁö ¾Ê½À´Ï´Ù");
			} else {
				ClientUser.alertDisplay(1, "ºñ¹Ð¹øÈ£ È®ÀÎ", "ºñ¹Ð¹øÈ£°¡ ÀÏÄ¡ÇÕ´Ï´Ù");
				btnPwChange.setDisable(false);
			}
		}
	}

	// ´Ð³×ÀÓ º¯°æÇÏ±â ´ÙÀÌ¾ó·Î±×Ã¢
	private void userNicknameChangeAction() {
		try {
			dialog = new Stage(StageStyle.UTILITY);
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.initOwner(lblInfo.getScene().getWindow());
			dialog.setTitle("´Ð³×ÀÓ º¯°æÇÏ±â");
			Parent root = (VBox) FXMLLoader.load(getClass().getResource("/view/nickchange.fxml"));

			TextField txtField = (TextField) root.lookup("#txtField");
			Button btnDupCheck = (Button) root.lookup("#btnDupCheck");
			Button btnOk = (Button) root.lookup("#btnOk");
			Button btnCancel = (Button) root.lookup("#btnCancel");

			int nameMinLength = 1; // ´Ð³×ÀÓ ÃÖ¼Ò±æÀÌ
			int nameMaxLength = 8; // ´Ð³×ÀÓ ÃÖ´ë±æÀÌ

			btnDupCheck.setOnAction(e -> {
				String name = txtField.getText();
				if (name.equals("") || name.length() < nameMinLength || name.length() > nameMaxLength
						|| !name.matches("^[0-9a-zA-Z°¡-ÆR]*$")) {
					ClientUser.alertDisplay(0, "´Ð³×ÀÓ Áßº¹È®ÀÎ", "»ç¿ëÇÒ ¼ö ¾ø´Â ´Ð³×ÀÓ Çü½ÄÀÔ´Ï´Ù", "1~8ÀÚ ÀÌ³»ÀÇ ÇÑ±Û,¿µ¹®,¼ýÀÚ·Î ±¸¼ºÇÏ¼¼¿ä");
				} else {
					// DB¿¬µ¿ Áßº¹È®ÀÎ
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
			stage.setTitle(clientUser.getNickname() + "ÀÇ ·©Å©ÀüÀû");

			PieChart pieChart = (PieChart) root.lookup("#pieChart");
			LineChart lineChart = (LineChart) root.lookup("#lineChart");
			Button btnRating = (Button) root.lookup("#btnRating");
			Button btnOk = (Button) root.lookup("#btnOk");

			int lastDataIndex = rankHistory.size() - 1;

			pieChart.setData(FXCollections.observableArrayList(
					new PieChart.Data("½Â¸®", (double) (rankHistory.get(lastDataIndex).getVictory())),
					new PieChart.Data("ÆÐ¹è", rankHistory.get(lastDataIndex).getDefeat())));

			pieChart.setVisible(false);

			XYChart.Series rpHistory = new XYChart.Series(); // chart label
			rpHistory.setName(clientUser.getNickname() + "ÀÇ RP º¯È­");

			ObservableList rpList = FXCollections.observableArrayList();
			for (int i = 0; i < rankHistory.size(); i++) {
				rpList.add(new XYChart.Data(i + "", rankHistory.get(i).getRp()));

				System.out.println("x: " + rankHistory.get(i).getNickname() + ",y =" + rankHistory.get(i).getRp());
			}
			rpHistory.setData(rpList);
			lineChart.getData().add(rpHistory);

			btnRating.setText("½Â·ü º¸±â");
			btnRating.setOnAction(e3 -> {
				if (btnRating.getText().equals("½Â·ü º¸±â")) {
					btnRating.setText("RP º¯È­ º¸±â");
					lineChart.setVisible(false);
					pieChart.setVisible(true);
				} else {
					btnRating.setText("½Â·ü º¸±â");
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
				ClientUser.alertDisplay(0, "´Ð³×ÀÓ Áßº¹ °Ë»ç", "Áßº¹µÈ ´Ð³×ÀÓÀÔ´Ï´Ù");
			} else {
				ClientUser.alertDisplay(1, "´Ð³×ÀÓ Áßº¹ °Ë»ç", "»ç¿ë °¡´ÉÇÑ ´Ð³×ÀÓÀÔ´Ï´Ù");
			}
		});

	}

	public void alertNickChangeResult(String result, String nick) {
		Platform.runLater(() -> {
			if (Boolean.parseBoolean(result) == true) {
				ClientUser.alertDisplay(1, "´Ð³×ÀÓ º¯°æ ¼º°ø", "´Ð³×ÀÓÀÌ " + nick + "(À¸)·Î º¯°æµÇ¾ú½À´Ï´Ù. °ÔÀÓÀ» ´Ù½Ã ½ÇÇàÇØÁÖ¼¼¿ä!",
						"È®ÀÎÀ» ´©¸£½Ã¸é °ÔÀÓÀÌ Á¾·áµË´Ï´Ù");
				clientUser.stopClient();
				Platform.exit();
			} else {
				ClientUser.alertDisplay(0, "´Ð³×ÀÓ º¯°æ ½ÇÆÐ", "´Ð³×ÀÓÀ» º¯°æÇÒ ¼ö ¾ø½À´Ï´Ù", "´Ð³×ÀÓ Çü½ÄÀÌ³ª ¼­¹ö»óÅÂ¸¦ È®ÀÎÇØÁÖ¼¼¿ä");
			}

		});

	}

	public void alertWithdrawResult(String data) {
		Platform.runLater(() -> {
			if (Boolean.parseBoolean(data) == true) {
				ClientUser.alertDisplay(1, "È¸¿øÅ»Åð ¿Ï·á", "Å»Åð°¡... ¿Ï·áµÇ¾ú½À´Ï´Ù", "¾¯ÀÍ¾¯ÀÍ...°ÔÀÓ ²¨Áø´Ù...");
				clientUser.stopClient();
				Platform.exit();
			} else {
				ClientUser.alertDisplay(0, "È¸¿øÅ»Åð ½ÇÆÐ", "Å»Åð ÇÒ ¼ö ¾ø½À´Ï´Ù", "°íÀÇ°¡ ¾Æ´Ï°í ÁøÂ¥·Î DB¿¬°áÀ» ÇÒ ¼ö ¾ø¾î¿ä..");
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
			clientUser.alertDisplay(0, "ÀüÀû º¸±â", "ÀüÀûÀ» ºÒ·¯ ¿Ã ¼ö ¾ø½À´Ï´Ù", "DB¿¡ ÀúÀåµÈ Á¤º¸°¡ ¾ø½À´Ï´Ù");
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
				ClientUser.alertDisplay(1, "ºñ¹Ð¹øÈ£ º¯°æ ¿Ï·á", "ºñ¹Ð¹øÈ£ º¯°æÀÌ ¿Ï·áµÇ¾ú½À´Ï´Ù");
			} else {
				ClientUser.alertDisplay(0, "ºñ¹Ð¹øÈ£ º¯°æ ½ÇÆÐ", "ºñ¹Ð¹øÈ£ º¯°æ¿äÃ»ÀÌ °ÅÀýµÇ¾ú½À´Ï´Ù", "¼­¹ö ¿¬°á»óÅÂ¸¦ È®ÀÎÇØÁÖ¼¼¿ä");
			}
		});

	}

}
