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
	// 방목록 새로고침시 서버db로부터 새로 받은 gameroomVO객체리스트
	private static ArrayList<GameRoomVO> newRoomList = new ArrayList<GameRoomVO>();;
	// 랭크리스트 새로고침
	private static ArrayList<RankVO> newRankList = new ArrayList<RankVO>();;
	// 개인 rp 히스토리
	private ArrayList<RankVO> arrRankVO = new ArrayList<RankVO>();

	public Stage primaryStage;
	public Stage dialog;
	private static ClientUser clientUser;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// 유저객체 가져오기
		if (clientUser == null) {
			clientUser = LoginController.getClientUser();
			clientUser.setMainController(this);
			clientUser.send(ClientProtocol.USERINFO_JOIN + "|" + clientUser.getId());
			try {
				// 서버로부터 정보를 받아오는 시간 확보를 위해 슬립을 줌
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
		// 게임방 테이블 ui세팅
		gameRoomTableViewSetting();
		// 각 테이블 db 정보 받아오기, 접속자리스트 출력
		refreshMainUI();
		// 랭크테이블 세팅
		rankTableViewSetting();

		// 입장하기버튼 비활성화
		btnGameStart.setDisable(true);

		// 채팅방에 환영메세지 출력
		clientUser.send(ClientProtocol.WELCOME + "|");
		txtAreaChatting.setEditable(false);

		// 1.메인에서의 전체채팅
		txtFieldChatting.setOnKeyPressed(event -> {
			mainChattingSend(event);
		});
		// 2.방 만들기
		btnCreateRoom.setOnAction(event -> {
			createNewGameRoom(event);
		});
		// 3. 새로고침
		btnRefresh.setOnAction(event -> {
			refreshMainUI();
		});
		// 4. 방 목록 클릭 (게임에 게스트로 들어가기)
		tblGameRoom.setOnMousePressed((e) -> {
			selectGameRoom();
		});
		// 5. 방 입장하기
		btnGameStart.setOnAction(event -> {
			enterGameRoomAction();
		});
		// 랭크클릭시 파이차트
		tblRank.setOnMouseClicked((e) -> {
			rankTableViewSetting(e);
		});
		// 내정보보기
		btnMyinfo.setOnAction(e -> {
			showMyInfomation();
		});

		// 로그아웃
		btnLogout.setOnAction(event -> {
			clientUser.stopClient();
			Platform.exit();

		});

	}

	// 1.메인에서의 전체채팅
	private void mainChattingSend(KeyEvent event) {
		// 채팅내용이 없으면 데이터를 보내지 않음
		if (txtFieldChatting.getText().equals("")) {
			return;
		}
		// 서버로 대기실 채팅 요청하기 (프로토콜, 유저닉네임, 채팅내용)
		if (event.getCode().equals(KeyCode.ENTER)) {
			clientUser
					.send(ClientProtocol.CHATTING + "|" + clientUser.getNickname() + "|" + txtFieldChatting.getText());
			txtFieldChatting.clear();
		}
	}

	// 2.방 만들기
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
			dialog.setTitle("방 만들기");
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.setResizable(false);
			dialog.show();

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("[방 만들기오류]");
		}

	}

	// 3.방 리스트 새로고침
	private void refreshMainUI() {
		// 방목록 새로고침
		clientUser.send(ClientProtocol.MAINPAGE_REFRESH + "|");
	}

	// 3-1. 방 목록 업데이트를 위해 서버로부터 db에있는 게임방 객체를 하나씩 받아옴. 받은 객체를 newRoomList에 저장
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
			clientUser.alertDisplay(1, "Null 겜방 Exception", "생성된 게임방이 없습니다", "방을 새로 만들어주세요!");

		});

	}

	// 4. 게임방선택 함수
	private void selectGameRoom() {
		// 선택한 객체 가져오기
		selectedIndex = tblGameRoom.getSelectionModel().getSelectedIndex();
		selectedGameRoom = tblGameRoom.getSelectionModel().getSelectedItems();
		btnGameStart.setDisable(false);
	}

	// 5. 방 선택후 입장하기 버튼 액션
	private void enterGameRoomAction() {
		// 공개방일경우 바로 서버로 enter요청 보내기
		String roomRock = selectedGameRoom.get(0).getRoomRock();
		clientUser.setHostData(selectedGameRoom.get(0).getHost());
		if (roomRock.equals("공개")) {
			clientUser.send(ClientProtocol.GUEST_ENTER + "|" + "false" + "|" + selectedGameRoom.get(0).getHost() + "|"
					+ clientUser.getNickname());
		} else { // 비밀방일경우 패스워드 입력창 띄우기
			VBox root;
			try {
				Stage dialog = new Stage(StageStyle.UTILITY);
				dialog.initModality(Modality.WINDOW_MODAL);
				dialog.initOwner(txtAreaChatting.getScene().getWindow());
				dialog.setTitle("비밀번호 입력");
				root = (VBox) FXMLLoader.load(getClass().getResource("/view/password.fxml"));
				// ===============================
				TextField txtFieldPw = (TextField) root.lookup("#txtFieldPw");
				Button btnOk = (Button) root.lookup("#btnOk");
				Button btnCancel = (Button) root.lookup("#btnCancel");

				// 패스워드칸 숫자 4자로 제한
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
					clientUser.alertDisplay(0, "입장 오류", "게임에 입장할 수 없습니다");
				});
			}
		}
	}

	// 랭크 목록 업데이트를 위해 서버로부터 db에있는 랭크vo 객체를 하나씩 받아옴.
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

			clientUser.alertDisplay(1, "Null 랭쿠 Exception", "랭크리스트가 비어있습니다", "한번만 게임해도 내가 1등!");
		});
	}

	// 서버로부터 CHATTING 프로토콜을 받으면, 텍스트에어리어에 해당 데이터를 보여주는 메소드
	public void showMainChatting(String[] data) {
		Platform.runLater(() -> txtAreaChatting.appendText("[" + data[1] + "] :" + data[2] + "\n"));
	}

	// 로그인시 인사메시지, 다른사람들에게 입장을 알림
	public void showMainChatting(String data) {
		Platform.runLater(() -> txtAreaChatting.appendText(data + "\n"));
	}

	// 서버로부터 가입시 기입한 유저정보를 받는 함수
	public void setUserJoinInfo(String id, String pw, String nickname) {
		clientUser.setId(id);
		clientUser.setPw(pw);
		clientUser.setNickname(nickname);
	}

	// 로그인은 성공했으나 메인에서 DB 회원정보를 받아올 수 없을 경우
	public void alertNotFoundUserInfo() {
		Platform.runLater(() -> {
			ClientUser.alertDisplay(0, "통신 오류", "서버로부터 회원정보를 불러 올 수 없습니다", "프로그램이 자동 종료됩니다");
			clientUser.stopClient();
			Platform.exit();
		});
	}

	// 방생성시 db에 성공적으로 방을 생성했는지 서버로부터 데이터를 받음
	public void alertRoomCreateResult(String data) {
		Platform.runLater(() -> {
			boolean enterable = Boolean.parseBoolean(data);
			if (enterable == true) {
				// 게임룸 스테이지&컨트롤러 로드
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
					ClientUser.alertDisplay(0, "통신 오류", "서버로부터 방 만들기 요청이 거절되었습니다", "서버 상태를 확인해주세요");

				}
			} else {
				ClientUser.alertDisplay(0, "방생성 오류", "서버로부터 방 만들기 요청이 거절되었습니다", "서버연결이 되어있지 않거나 DB오류가 발생했습니다");

			}

		});

	}

	// 방 입장하기 할때 입장가능 여부를 서버로부터 전달받음 //data가 true일경우 게임룸으로 이동
	public void alertGuestEnterResult(String data) {
		// 게스트 입장했다고 서버에 알리기(이후 서버가 호스트에게 게스트가 입장했음을 알려줌)
		Platform.runLater(() -> {
			boolean enterable = Boolean.parseBoolean(data);
			if (enterable == true) {
				// 게임룸 스테이지&컨트롤러 로드
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
					ClientUser.alertDisplay(0, "통신 오류", "서버로부터 입장하기 요청이 거절되었습니다", "서버 상태를 확인해주세요");

				}
			} else {
				ClientUser.alertDisplay(0, "입장 오류", "방이 이미 게임중이거나 비밀번호 오류입니다", "방목록을 새로고침 해주시고 비밀방일경우 비밀번호를 정확히 입력하세요");

			}

		});

	}

	// 게임방 테이블 ui세팅
	public void gameRoomTableViewSetting() {
		tblGameRoom.setEditable(false);

		TableColumn colRp = new TableColumn("Rp");
		colRp.setMinWidth(50);
		colRp.setStyle("-fx-alignment:CENTER;");
		colRp.setCellValueFactory(new PropertyValueFactory("rp"));

		TableColumn colRoomName = new TableColumn("방제");
		colRoomName.setMinWidth(250);
		colRoomName.setStyle("-fx-alignment:CENTER;");
		colRoomName.setCellValueFactory(new PropertyValueFactory("roomName"));

		TableColumn colHost = new TableColumn("방장");
		colHost.setMinWidth(120);
		colHost.setStyle("-fx-alignment:CENTER;");
		colHost.setCellValueFactory(new PropertyValueFactory("host"));

		TableColumn colState = new TableColumn("상태");
		colState.setMinWidth(50);
		colState.setStyle("-fx-alignment:CENTER;");
		colState.setCellValueFactory(new PropertyValueFactory("state"));

		TableColumn colRoomRock = new TableColumn("공개여부");
		colRoomRock.setMinWidth(90);
		colRoomRock.setStyle("-fx-alignment:CENTER;");
		colRoomRock.setCellValueFactory(new PropertyValueFactory("roomRock"));

		tblGameRoom.getColumns().add(colRp);
		tblGameRoom.getColumns().add(colRoomName);
		tblGameRoom.getColumns().add(colHost);
		tblGameRoom.getColumns().add(colState);
		tblGameRoom.getColumns().add(colRoomRock);

	}

	// rp 테이블 ui세팅
	public void rankTableViewSetting() {
		tblRank.setEditable(false);

		TableColumn colRp = new TableColumn("Rp");
		colRp.setMinWidth(95);
		colRp.setStyle("-fx-alignment:CENTER;");
		colRp.setCellValueFactory(new PropertyValueFactory("rp"));

		TableColumn colNickname = new TableColumn("닉네임");
		colNickname.setMinWidth(190);
		colNickname.setStyle("-fx-alignment:CENTER;");
		colNickname.setCellValueFactory(new PropertyValueFactory("nickname"));

		tblRank.getColumns().add(colRp);
		tblRank.getColumns().add(colNickname);

	}

	public void rankTableViewSetting(MouseEvent e) {
		// 더블 클릭인지 점검
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
			stage.setTitle(selectedRankVO.get(0).getNickname() + "의 랭크전적");

			PieChart pieChart = (PieChart) root.lookup("#pieChart");
			LineChart lineChart = (LineChart) root.lookup("#lineChart");
			Button btnRating = (Button) root.lookup("#btnRating");
			Button btnOk = (Button) root.lookup("#btnOk");

			pieChart.setData(FXCollections.observableArrayList(
					new PieChart.Data("승리", (double) (selectedRankVO.get(0).getVictory())),
					new PieChart.Data("패배", selectedRankVO.get(0).getDefeat())));

			pieChart.setVisible(false);

			XYChart.Series rpHistory = new XYChart.Series(); // chart label
			rpHistory.setName(selectedRankVO.get(0).getNickname() + "의 RP 변화");

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

	// 개인의 랭크 전적 세팅하기
	public void getRankHistory(String rankHistorydata) {

		StringTokenizer token = new StringTokenizer(rankHistorydata, "//,");
		//기존의 전적자료 삭제
		rankHistory.removeAll(rankHistory);
		ArrayList<RankVO> temp = new ArrayList<RankVO>();
		while (token.hasMoreTokens()) {
			String nick = token.nextToken();
			int rp = Integer.parseInt(token.nextToken()); //랭크포인트
			int vic = Integer.parseInt(token.nextToken()); //승리
			int def = Integer.parseInt(token.nextToken()); //패배
			int total = Integer.parseInt(token.nextToken()); //전체게임수
			RankVO rVO = new RankVO(nick, rp, vic, def, total);
			//새로운 전작자료 추가 (최신->과거 순으로 입력됨)
			temp.add(rVO);
		}
		// 과거->최신순으로 재정렬
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
			dialog.setTitle("내 정보");
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.setResizable(false);
			dialog.setScene(scene);
			dialog.show();

		} catch (IOException e) {
			e.printStackTrace();
			ClientUser.alertDisplay(0, "내 정보 로딩오류", "내 정보를 불러오는데 오류가 발생했습니다", "서버 상태를 확인해주세요");

		}

	}

	public void getRankHistory() {
		Platform.runLater(() -> {
			rankHistory.removeAll(rankHistory);
			clientUser.alertDisplay(1, "전적 보기", "해당 플레이어는 전적이 없습니다");
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
