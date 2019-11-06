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
	private final int pwMinLength = 4; // ��� �ּұ���
	private final int pwMaxLength = 10; // ��� �ִ����

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		if (clientUser == null) {
			clientUser = LoginController.getClientUser();
			clientUser.setMyInfomationController(this);
		}
		// �󺧼���
		lblInfo.setText(clientUser.getId() + "�� ����");
		lblId.setText(clientUser.getId());
		lblNickname.setText(clientUser.getNickname());

		btnPwChange.setDisable(true);
		// ��ư�̺�Ʈ
		btnNickChange.setOnAction(e -> {
			userNicknameChangeAction();
		});
		// ��� ���� ������ Ȯ��
		btnPwConfirm.setOnAction(e -> {
			passwordCheckAction();
		});
		// �������
		btnPwChange.setOnAction(e -> {
			passwordChangeAction();
		});
		// ȸ��Ż��
		btnWithdraw.setOnAction(e -> {
			applyWithdrawMymembership();
		});
		// ��Ʈ����
		btnHistory.setOnAction(e -> {
			showMyGameHistoryChart();
		});

		btnOk.setOnAction(e -> {
			primaryStage.close();
		});

	}

	// �������
	private void passwordChangeAction() {
		String pw1 = pwField1.getText();
		String pw2 = pwField2.getText();

		if (pw1.length() < pwMinLength || pw2.length() < pwMinLength || pw1.length() > pwMaxLength
				|| pw2.length() > pwMaxLength) {
			ClientUser.alertDisplay(0, "��й�ȣ Ȯ��", "����� �� ���� ��й�ȣ �����Դϴ�", "��й�ȣ�� 4~10�� ���̷� �Է����ּ���");
		} else {
			if (!pw1.equals(pw2)) {
				ClientUser.alertDisplay(0, "��й�ȣ Ȯ��", "��й�ȣ�� ��ġ���� �ʽ��ϴ�");
			} else {
				clientUser.send(ClientProtocol.MYINFO_PWCHANGE + "|" + clientUser.getId() + "|"+pw1);
				
			}
		}

	}

	// ȸ��Ż���û
	private void applyWithdrawMymembership() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("ȸ��Ż�� ���");
		alert.setHeaderText("Ż�� �� ȸ���� ��� ������ ������ϴ�. ���� Ż���Ͻðڽ��ϱ�?");
		alert.setContentText("Ż��� ȸ���� �ڷ�� ������ �� �����ϴ�.");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			clientUser.send(ClientProtocol.MYINFO_WITHDRAW + "|" + clientUser.getId());
		} else {
			alert.close();
		}
	}

	// ��� ������ Ȯ��
	private void passwordCheckAction() {
		String pw1 = pwField1.getText();
		String pw2 = pwField2.getText();

		if (pw1.length() < pwMinLength || pw2.length() < pwMinLength || pw1.length() > pwMaxLength
				|| pw2.length() > pwMaxLength) {
			ClientUser.alertDisplay(0, "��й�ȣ Ȯ��", "����� �� ���� ��й�ȣ �����Դϴ�", "��й�ȣ�� 4~10�� ���̷� �Է����ּ���");
		} else {
			if (!pw1.equals(pw2)) {
				ClientUser.alertDisplay(0, "��й�ȣ Ȯ��", "��й�ȣ�� ��ġ���� �ʽ��ϴ�");
			} else {
				ClientUser.alertDisplay(1, "��й�ȣ Ȯ��", "��й�ȣ�� ��ġ�մϴ�");
				btnPwChange.setDisable(false);
			}
		}
	}

	// �г��� �����ϱ� ���̾�α�â
	private void userNicknameChangeAction() {
		try {
			dialog = new Stage(StageStyle.UTILITY);
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.initOwner(lblInfo.getScene().getWindow());
			dialog.setTitle("�г��� �����ϱ�");
			Parent root = (VBox) FXMLLoader.load(getClass().getResource("/view/nickchange.fxml"));

			TextField txtField = (TextField) root.lookup("#txtField");
			Button btnDupCheck = (Button) root.lookup("#btnDupCheck");
			Button btnOk = (Button) root.lookup("#btnOk");
			Button btnCancel = (Button) root.lookup("#btnCancel");

			int nameMinLength = 1; // �г��� �ּұ���
			int nameMaxLength = 8; // �г��� �ִ����

			btnDupCheck.setOnAction(e -> {
				String name = txtField.getText();
				if (name.equals("") || name.length() < nameMinLength || name.length() > nameMaxLength
						|| !name.matches("^[0-9a-zA-Z��-�R]*$")) {
					ClientUser.alertDisplay(0, "�г��� �ߺ�Ȯ��", "����� �� ���� �г��� �����Դϴ�", "1~8�� �̳��� �ѱ�,����,���ڷ� �����ϼ���");
				} else {
					// DB���� �ߺ�Ȯ��
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
			stage.setTitle(clientUser.getNickname() + "�� ��ũ����");

			PieChart pieChart = (PieChart) root.lookup("#pieChart");
			LineChart lineChart = (LineChart) root.lookup("#lineChart");
			Button btnRating = (Button) root.lookup("#btnRating");
			Button btnOk = (Button) root.lookup("#btnOk");

			int lastDataIndex = rankHistory.size() - 1;

			pieChart.setData(FXCollections.observableArrayList(
					new PieChart.Data("�¸�", (double) (rankHistory.get(lastDataIndex).getVictory())),
					new PieChart.Data("�й�", rankHistory.get(lastDataIndex).getDefeat())));

			pieChart.setVisible(false);

			XYChart.Series rpHistory = new XYChart.Series(); // chart label
			rpHistory.setName(clientUser.getNickname() + "�� RP ��ȭ");

			ObservableList rpList = FXCollections.observableArrayList();
			for (int i = 0; i < rankHistory.size(); i++) {
				rpList.add(new XYChart.Data(i + "", rankHistory.get(i).getRp()));

				System.out.println("x: " + rankHistory.get(i).getNickname() + ",y =" + rankHistory.get(i).getRp());
			}
			rpHistory.setData(rpList);
			lineChart.getData().add(rpHistory);

			btnRating.setText("�·� ����");
			btnRating.setOnAction(e3 -> {
				if (btnRating.getText().equals("�·� ����")) {
					btnRating.setText("RP ��ȭ ����");
					lineChart.setVisible(false);
					pieChart.setVisible(true);
				} else {
					btnRating.setText("�·� ����");
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
				ClientUser.alertDisplay(0, "�г��� �ߺ� �˻�", "�ߺ��� �г����Դϴ�");
			} else {
				ClientUser.alertDisplay(1, "�г��� �ߺ� �˻�", "��� ������ �г����Դϴ�");
			}
		});

	}

	public void alertNickChangeResult(String result, String nick) {
		Platform.runLater(() -> {
			if (Boolean.parseBoolean(result) == true) {
				ClientUser.alertDisplay(1, "�г��� ���� ����", "�г����� " + nick + "(��)�� ����Ǿ����ϴ�. ������ �ٽ� �������ּ���!",
						"Ȯ���� �����ø� ������ ����˴ϴ�");
				clientUser.stopClient();
				Platform.exit();
			} else {
				ClientUser.alertDisplay(0, "�г��� ���� ����", "�г����� ������ �� �����ϴ�", "�г��� �����̳� �������¸� Ȯ�����ּ���");
			}

		});

	}

	public void alertWithdrawResult(String data) {
		Platform.runLater(() -> {
			if (Boolean.parseBoolean(data) == true) {
				ClientUser.alertDisplay(1, "ȸ��Ż�� �Ϸ�", "Ż��... �Ϸ�Ǿ����ϴ�", "���;���...���� ������...");
				clientUser.stopClient();
				Platform.exit();
			} else {
				ClientUser.alertDisplay(0, "ȸ��Ż�� ����", "Ż�� �� �� �����ϴ�", "���ǰ� �ƴϰ� ��¥�� DB������ �� �� �����..");
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
			clientUser.alertDisplay(0, "���� ����", "������ �ҷ� �� �� �����ϴ�", "DB�� ����� ������ �����ϴ�");
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
				ClientUser.alertDisplay(1, "��й�ȣ ���� �Ϸ�", "��й�ȣ ������ �Ϸ�Ǿ����ϴ�");
			} else {
				ClientUser.alertDisplay(0, "��й�ȣ ���� ����", "��й�ȣ �����û�� �����Ǿ����ϴ�", "���� ������¸� Ȯ�����ּ���");
			}
		});

	}

}
