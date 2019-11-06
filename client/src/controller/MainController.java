package controller;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.GameRoomVO;
import model.RankVO;

public class MainController implements Initializable {
	@FXML
	private AnchorPane anchorPane;
	@FXML
	private TextArea txtAreaChatting;
	@FXML
	private TextField txtFieldChatting;
	@FXML
	private TableView<RankVO> tblRank = new TableView<RankVO>();
	@FXML
	private Button btnRefresh;
	@FXML
	private Button btnGameStart;
	@FXML
	private Button btnCreateRoom;
	@FXML
	private Button btnLogout;
	@FXML
	private Button btnMyinfo;
	@FXML
	private TextArea txtAreaClients;
	@FXML
	private TableView<GameRoomVO> tblGameRoom = new TableView<GameRoomVO>();
	private ObservableList<GameRoomVO> selectedGameRoom;
	private ObservableList<RankVO> selectedRankVO;
	private int selectedIndex;

	private ObservableList<GameRoomVO> roomList = FXCollections.observableArrayList();
	private ObservableList<RankVO> rankList = FXCollections.observableArrayList();
	private ObservableList<RankVO> rankHistory = FXCollections.observableArrayList();
	// ���� ���ΰ�ħ�� ����db�κ��� ���� ���� gameroomVO��ü����Ʈ
	private static ArrayList<GameRoomVO> newRoomList = new ArrayList<GameRoomVO>();;
	// ��ũ����Ʈ ���ΰ�ħ
	private static ArrayList<RankVO> newRankList = new ArrayList<RankVO>();;
	// ���� rp �����丮
	private ArrayList<RankVO> arrRankVO = new ArrayList<RankVO>();

	public Stage primaryStage;
	public Stage dialog;
	private static ClientUser clientUser;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// ������ü ��������
		if (clientUser == null) {
			clientUser = LoginController.getClientUser();
			clientUser.setMainController(this);
			clientUser.send(ClientProtocol.USERINFO_JOIN + "|" + clientUser.getId());
			try {
				// �����κ��� ������ �޾ƿ��� �ð� Ȯ���� ���� ������ ��
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			clientUser = GameRoomController.getClientUser();
			clientUser.setMainController(this);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// ���ӹ� ���̺� ui����
		gameRoomTableViewSetting();
		// �� ���̺� db ���� �޾ƿ���, �����ڸ���Ʈ ���
		refreshMainUI();
		// ��ũ���̺� ����
		rankTableViewSetting();

		// �����ϱ��ư ��Ȱ��ȭ
		btnGameStart.setDisable(true);

		// ä�ù濡 ȯ���޼��� ���
		clientUser.send(ClientProtocol.WELCOME + "|");
		txtAreaChatting.setEditable(false);

		// 1.���ο����� ��üä��
		txtFieldChatting.setOnKeyPressed(event -> {
			mainChattingSend(event);
		});
		// 2.�� �����
		btnCreateRoom.setOnAction(event -> {
			createNewGameRoom(event);
		});
		// 3. ���ΰ�ħ
		btnRefresh.setOnAction(event -> {
			refreshMainUI();
		});
		// 4. �� ��� Ŭ�� (���ӿ� �Խ�Ʈ�� ����)
		tblGameRoom.setOnMousePressed((e) -> {
			selectGameRoom();
		});
		// 5. �� �����ϱ�
		btnGameStart.setOnAction(event -> {
			enterGameRoomAction();
		});
		// ��ũŬ���� ������Ʈ
		tblRank.setOnMouseClicked((e) -> {
			rankTableViewSetting(e);
		});
		// ����������
		btnMyinfo.setOnAction(e -> {
			showMyInfomation();
		});

		// �α׾ƿ�
		btnLogout.setOnAction(event -> {
			clientUser.stopClient();
			Platform.exit();

		});

	}

	// 1.���ο����� ��üä��
	private void mainChattingSend(KeyEvent event) {
		// ä�ó����� ������ �����͸� ������ ����
		if (txtFieldChatting.getText().equals("")) {
			return;
		}
		// ������ ���� ä�� ��û�ϱ� (��������, �����г���, ä�ó���)
		if (event.getCode().equals(KeyCode.ENTER)) {
			clientUser
					.send(ClientProtocol.CHATTING + "|" + clientUser.getNickname() + "|" + txtFieldChatting.getText());
			txtFieldChatting.clear();
		}
	}

	// 2.�� �����
	private void createNewGameRoom(ActionEvent event) {
		try {
			dialog = new Stage();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/createroom.fxml"));
			Parent root = (Parent) loader.load();
			Scene scene = new Scene(root);
			CreateRoomController createRoomController = loader.getController();
			createRoomController.setPrimaryStage(dialog);
			scene.getStylesheets().add(getClass().getResource("/application/clientcss.css").toExternalForm());
			scene.getStylesheets().add("https://fonts.googleapis.com/css?family=Yeon+Sung&display=swap&subset=korean");
			dialog.setScene(scene);
			dialog.setTitle("�� �����");
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.setResizable(false);
			dialog.show();

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("[�� ��������]");
		}

	}

	// 3.�� ����Ʈ ���ΰ�ħ
	private void refreshMainUI() {
		// ���� ���ΰ�ħ
		clientUser.send(ClientProtocol.MAINPAGE_REFRESH + "|");
	}

	// 3-1. �� ��� ������Ʈ�� ���� �����κ��� db���ִ� ���ӹ� ��ü�� �ϳ��� �޾ƿ�. ���� ��ü�� newRoomList�� ����
	public void createNewRoomList(String list) {

		newRoomList.removeAll(newRoomList);
		StringTokenizer token = new StringTokenizer(list, "//,");

		while (token.hasMoreTokens()) {
			int rp = Integer.parseInt(token.nextToken());
			String roomName = (token.nextToken());
			String host = (token.nextToken());
			String state = (token.nextToken());
			String roomRock = (token.nextToken());
			GameRoomVO grVO = new GameRoomVO(rp, roomName, host, state, roomRock);
			newRoomList.add(grVO);
		}

		roomList.removeAll(roomList);
		for (GameRoomVO grVO : newRoomList) {
			roomList.add(grVO);
		}
		tblGameRoom.setItems(roomList);

	}

	public void createNewRoomList() {
		Platform.runLater(() -> {
			clientUser.alertDisplay(1, "Null �׹� Exception", "������ ���ӹ��� �����ϴ�", "���� ���� ������ּ���!");

		});

	}

	// 4. ���ӹ漱�� �Լ�
	private void selectGameRoom() {
		// ������ ��ü ��������
		selectedIndex = tblGameRoom.getSelectionModel().getSelectedIndex();
		selectedGameRoom = tblGameRoom.getSelectionModel().getSelectedItems();
		btnGameStart.setDisable(false);
	}

	// 5. �� ������ �����ϱ� ��ư �׼�
	private void enterGameRoomAction() {
		// �������ϰ�� �ٷ� ������ enter��û ������
		String roomRock = selectedGameRoom.get(0).getRoomRock();
		clientUser.setHostData(selectedGameRoom.get(0).getHost());
		if (roomRock.equals("����")) {
			clientUser.send(ClientProtocol.GUEST_ENTER + "|" + "false" + "|" + selectedGameRoom.get(0).getHost() + "|"
					+ clientUser.getNickname());
		} else { // ��й��ϰ�� �н����� �Է�â ����
			VBox root;
			try {
				Stage dialog = new Stage(StageStyle.UTILITY);
				dialog.initModality(Modality.WINDOW_MODAL);
				dialog.initOwner(txtAreaChatting.getScene().getWindow());
				dialog.setTitle("��й�ȣ �Է�");
				root = (VBox) FXMLLoader.load(getClass().getResource("/view/password.fxml"));
				// ===============================
				TextField txtFieldPw = (TextField) root.lookup("#txtFieldPw");
				Button btnOk = (Button) root.lookup("#btnOk");
				Button btnCancel = (Button) root.lookup("#btnCancel");

				// �н�����ĭ ���� 4�ڷ� ����
				DecimalFormat format = new DecimalFormat("####");
				txtFieldPw.setTextFormatter(new TextFormatter<>(event -> {
					if (event.getControlNewText().isEmpty()) {
						return event;
					}
					ParsePosition parsePosition = new ParsePosition(0);
					Object object = format.parse(event.getControlNewText(), parsePosition);
					if (object == null || parsePosition.getIndex() < event.getControlNewText().length()
							|| event.getControlNewText().length() == 5) {
						return null;
					} else {
						return event;
					}
				}));
				btnOk.setOnAction(event -> {
					clientUser.send(ClientProtocol.GUEST_ENTER + "|" + "true" + "|" + selectedGameRoom.get(0).getHost()
							+ "|" + clientUser.getNickname() + "|" + txtFieldPw.getText());
					dialog.close();
				});
				btnCancel.setOnAction(event -> {
					dialog.close();
				});
				Scene scene = new Scene(root);
				scene.getStylesheets().add(getClass().getResource("/application/clientcss.css").toExternalForm());
				scene.getStylesheets()
						.add("https://fonts.googleapis.com/css?family=Yeon+Sung&display=swap&subset=korean");
				dialog.setScene(scene);
				dialog.setResizable(false);
				dialog.show();
			} catch (IOException e) {
				Platform.runLater(() -> {
					clientUser.alertDisplay(0, "���� ����", "���ӿ� ������ �� �����ϴ�");
				});
			}
		}
	}

	// ��ũ ��� ������Ʈ�� ���� �����κ��� db���ִ� ��ũvo ��ü�� �ϳ��� �޾ƿ�.
	public void createNewRankList(String list) {
		newRankList.removeAll(newRankList);

		StringTokenizer token = new StringTokenizer(list, "//,");
		while (token.hasMoreTokens()) {
			String nick = token.nextToken();
			int rp = Integer.parseInt(token.nextToken());
			int vic = Integer.parseInt(token.nextToken());
			int def = Integer.parseInt(token.nextToken());
			int to = Integer.parseInt(token.nextToken());
			RankVO rVO = new RankVO(nick, rp, vic, def, to);
			newRankList.add(rVO);
		}
		rankList.removeAll(rankList);
		for (RankVO rVO : newRankList) {
			rankList.add(rVO);
		}
		tblRank.setItems(rankList);

	}

	public void createNewRankList() {
		Platform.runLater(() -> {

			clientUser.alertDisplay(1, "Null ���� Exception", "��ũ����Ʈ�� ����ֽ��ϴ�", "�ѹ��� �����ص� ���� 1��!");
		});
	}

	// �����κ��� CHATTING ���������� ������, �ؽ�Ʈ���� �ش� �����͸� �����ִ� �޼ҵ�
	public void showMainChatting(String[] data) {
		Platform.runLater(() -> txtAreaChatting.appendText("[" + data[1] + "] :" + data[2] + "\n"));
	}

	// �α��ν� �λ�޽���, �ٸ�����鿡�� ������ �˸�
	public void showMainChatting(String data) {
		Platform.runLater(() -> txtAreaChatting.appendText(data + "\n"));
	}

	// �����κ��� ���Խ� ������ ���������� �޴� �Լ�
	public void setUserJoinInfo(String id, String pw, String nickname) {
		clientUser.setId(id);
		clientUser.setPw(pw);
		clientUser.setNickname(nickname);
	}

	// �α����� ���������� ���ο��� DB ȸ�������� �޾ƿ� �� ���� ���
	public void alertNotFoundUserInfo() {
		Platform.runLater(() -> {
			ClientUser.alertDisplay(0, "��� ����", "�����κ��� ȸ�������� �ҷ� �� �� �����ϴ�", "���α׷��� �ڵ� ����˴ϴ�");
			clientUser.stopClient();
			Platform.exit();
		});
	}

	// ������� db�� ���������� ���� �����ߴ��� �����κ��� �����͸� ����
	public void alertRoomCreateResult(String data) {
		Platform.runLater(() -> {
			boolean enterable = Boolean.parseBoolean(data);
			if (enterable == true) {
				// ���ӷ� ��������&��Ʈ�ѷ� �ε�
				try {
					FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/gameroom.fxml"));
					Parent main = (Parent) loader.load();
					Scene scene = new Scene(main);
					GameRoomController gameRoomController = loader.getController();
					gameRoomController.setPrimaryStage(primaryStage);
					scene.getStylesheets().add(getClass().getResource("/application/clientcss.css").toExternalForm());
					scene.getStylesheets()
							.add("https://fonts.googleapis.com/css?family=Yeon+Sung&display=swap&subset=korean");
					primaryStage.setScene(scene);
					primaryStage.show();
				} catch (IOException e) {
					e.printStackTrace();
					ClientUser.alertDisplay(0, "��� ����", "�����κ��� �� ����� ��û�� �����Ǿ����ϴ�", "���� ���¸� Ȯ�����ּ���");

				}
			} else {
				ClientUser.alertDisplay(0, "����� ����", "�����κ��� �� ����� ��û�� �����Ǿ����ϴ�", "���������� �Ǿ����� �ʰų� DB������ �߻��߽��ϴ�");

			}

		});

	}

	// �� �����ϱ� �Ҷ� ���尡�� ���θ� �����κ��� ���޹��� //data�� true�ϰ�� ���ӷ����� �̵�
	public void alertGuestEnterResult(String data) {
		// �Խ�Ʈ �����ߴٰ� ������ �˸���(���� ������ ȣ��Ʈ���� �Խ�Ʈ�� ���������� �˷���)
		Platform.runLater(() -> {
			boolean enterable = Boolean.parseBoolean(data);
			if (enterable == true) {
				// ���ӷ� ��������&��Ʈ�ѷ� �ε�
				try {
					FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/gameroom.fxml"));
					Parent main = (Parent) loader.load();
					Scene scene = new Scene(main);
					GameRoomController gameRoomController = loader.getController();
					gameRoomController.setPrimaryStage(primaryStage);
					scene.getStylesheets().add(getClass().getResource("/application/clientcss.css").toExternalForm());
					scene.getStylesheets()
							.add("https://fonts.googleapis.com/css?family=Yeon+Sung&display=swap&subset=korean");
					primaryStage.setScene(scene);
					primaryStage.show();
					clientUser.send(ClientProtocol.GAMEROOM_FULL + "|" + selectedGameRoom.get(0).getHost());
				} catch (IOException e) {
					e.printStackTrace();
					ClientUser.alertDisplay(0, "��� ����", "�����κ��� �����ϱ� ��û�� �����Ǿ����ϴ�", "���� ���¸� Ȯ�����ּ���");

				}
			} else {
				ClientUser.alertDisplay(0, "���� ����", "���� �̹� �������̰ų� ��й�ȣ �����Դϴ�", "������ ���ΰ�ħ ���ֽð� ��й��ϰ�� ��й�ȣ�� ��Ȯ�� �Է��ϼ���");

			}

		});

	}

	// ���ӹ� ���̺� ui����
	public void gameRoomTableViewSetting() {
		tblGameRoom.setEditable(false);

		TableColumn colRp = new TableColumn("Rp");
		colRp.setMinWidth(50);
		colRp.setStyle("-fx-alignment:CENTER;");
		colRp.setCellValueFactory(new PropertyValueFactory("rp"));

		TableColumn colRoomName = new TableColumn("����");
		colRoomName.setMinWidth(250);
		colRoomName.setStyle("-fx-alignment:CENTER;");
		colRoomName.setCellValueFactory(new PropertyValueFactory("roomName"));

		TableColumn colHost = new TableColumn("����");
		colHost.setMinWidth(120);
		colHost.setStyle("-fx-alignment:CENTER;");
		colHost.setCellValueFactory(new PropertyValueFactory("host"));

		TableColumn colState = new TableColumn("����");
		colState.setMinWidth(50);
		colState.setStyle("-fx-alignment:CENTER;");
		colState.setCellValueFactory(new PropertyValueFactory("state"));

		TableColumn colRoomRock = new TableColumn("��������");
		colRoomRock.setMinWidth(90);
		colRoomRock.setStyle("-fx-alignment:CENTER;");
		colRoomRock.setCellValueFactory(new PropertyValueFactory("roomRock"));

		tblGameRoom.getColumns().add(colRp);
		tblGameRoom.getColumns().add(colRoomName);
		tblGameRoom.getColumns().add(colHost);
		tblGameRoom.getColumns().add(colState);
		tblGameRoom.getColumns().add(colRoomRock);

	}

	// rp ���̺� ui����
	public void rankTableViewSetting() {
		tblRank.setEditable(false);

		TableColumn colRp = new TableColumn("Rp");
		colRp.setMinWidth(95);
		colRp.setStyle("-fx-alignment:CENTER;");
		colRp.setCellValueFactory(new PropertyValueFactory("rp"));

		TableColumn colNickname = new TableColumn("�г���");
		colNickname.setMinWidth(190);
		colNickname.setStyle("-fx-alignment:CENTER;");
		colNickname.setCellValueFactory(new PropertyValueFactory("nickname"));

		tblRank.getColumns().add(colRp);
		tblRank.getColumns().add(colNickname);

	}

	public void rankTableViewSetting(MouseEvent e) {
		// ���� Ŭ������ ����
		if (e.getClickCount() != 2) {
			return;
		}
		try {
			selectedRankVO = tblRank.getSelectionModel().getSelectedItems();
			clientUser.send(ClientProtocol.RANK_HISTORY + "|" + selectedRankVO.get(0).getNickname());
			Thread.sleep(2000);

			Parent root = FXMLLoader.load(getClass().getResource("/view/chart.fxml"));
			Stage stage = new Stage(StageStyle.UTILITY);
			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(btnRefresh.getScene().getWindow());
			stage.setTitle(selectedRankVO.get(0).getNickname() + "�� ��ũ����");

			PieChart pieChart = (PieChart) root.lookup("#pieChart");
			LineChart lineChart = (LineChart) root.lookup("#lineChart");
			Button btnRating = (Button) root.lookup("#btnRating");
			Button btnOk = (Button) root.lookup("#btnOk");

			pieChart.setData(FXCollections.observableArrayList(
					new PieChart.Data("�¸�", (double) (selectedRankVO.get(0).getVictory())),
					new PieChart.Data("�й�", selectedRankVO.get(0).getDefeat())));

			pieChart.setVisible(false);

			XYChart.Series rpHistory = new XYChart.Series(); // chart label
			rpHistory.setName(selectedRankVO.get(0).getNickname() + "�� RP ��ȭ");

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

	// ������ ��ũ ���� �����ϱ�
	public void getRankHistory(String rankHistorydata) {

		StringTokenizer token = new StringTokenizer(rankHistorydata, "//,");
		//������ �����ڷ� ����
		rankHistory.removeAll(rankHistory);
		ArrayList<RankVO> temp = new ArrayList<RankVO>();
		while (token.hasMoreTokens()) {
			String nick = token.nextToken();
			int rp = Integer.parseInt(token.nextToken()); //��ũ����Ʈ
			int vic = Integer.parseInt(token.nextToken()); //�¸�
			int def = Integer.parseInt(token.nextToken()); //�й�
			int total = Integer.parseInt(token.nextToken()); //��ü���Ӽ�
			RankVO rVO = new RankVO(nick, rp, vic, def, total);
			//���ο� �����ڷ� �߰� (�ֽ�->���� ������ �Էµ�)
			temp.add(rVO);
		}
		// ����->�ֽż����� ������
		for (int i = temp.size() - 1; i >= 0; i--) {
			rankHistory.add(temp.get(i));
		}

	}

	private void showMyInfomation() {

		try {
			dialog = new Stage();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/myinfomation.fxml"));
			Parent main = (Parent) loader.load();
			Scene scene = new Scene(main);
			MyInfomationController myInfomationController = loader.getController();
			myInfomationController.setPrimaryStage(dialog);
			scene.getStylesheets().add(getClass().getResource("/application/clientcss.css").toExternalForm());
			scene.getStylesheets().add("https://fonts.googleapis.com/css?family=Yeon+Sung&display=swap&subset=korean");
			dialog.setTitle("�� ����");
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.setResizable(false);
			dialog.setScene(scene);
			dialog.show();

		} catch (IOException e) {
			e.printStackTrace();
			ClientUser.alertDisplay(0, "�� ���� �ε�����", "�� ������ �ҷ����µ� ������ �߻��߽��ϴ�", "���� ���¸� Ȯ�����ּ���");

		}

	}

	public void getRankHistory() {
		Platform.runLater(() -> {
			rankHistory.removeAll(rankHistory);
			clientUser.alertDisplay(1, "���� ����", "�ش� �÷��̾�� ������ �����ϴ�");
		});
	}

	public static ClientUser getClientUser() {
		return clientUser;
	}

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	public void showPlayersList(String playerList) {
		Platform.runLater(() -> {
			txtAreaClients.clear();
			StringTokenizer token = new StringTokenizer(playerList, ",");
			String list = "";
			while (token.hasMoreTokens()) {
				
				String nextToken = token.nextToken();
				
				if (!nextToken.equals("null")) {
					list = list + nextToken + "\n";
				}
			}
			txtAreaClients.appendText(list);

		});

	}

}
