package controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class GameRoomController implements Initializable {
	@FXML
	private ImageView imageView1;
	@FXML
	private ImageView imageView2;
	@FXML
	private Pane pane;
	@FXML
	private Canvas canvas;
	@FXML
	private ImageView hostImg;
	@FXML
	private Label hostNick;
	@FXML
	private HBox hostHt;
	@FXML
	private Label hostCnt;
	@FXML
	private ImageView guestImg;
	@FXML
	private Label guestNick;
	@FXML
	private HBox guestHt;
	@FXML
	private Label guestCnt;
	@FXML
	private Label lblReady;
	@FXML
	private Label lblStart;
	@FXML
	private Label lblWait1;
	@FXML
	private Label lblWait2;
	@FXML
	private Label lblNext;
	@FXML
	private Button btnReady;
	@FXML
	private Button btnSurrender;
	@FXML
	private Button btnExit;

	public Stage primaryStage;

	private static ClientUser clientUser;
	private String hostNickname;

	private Thread gameThread;

	private final int totalStage = 5; // 전체 라운드 수 
	private int gameCount = 1; // 진행한 게임수

	private boolean waitForPlayer = true; // 레디할때까지 기다리는 flag
	private boolean start = false; // 모든 플레이어 레디했는가? flag(게임시작)
	private boolean nextStage = true; // 다음 스테이지로 넘어가는가? flag
	private boolean mouseRock = true; // 마우스이벤트 on off를 위한 flag

	private boolean surrender = false; //  기권한경우

	private boolean flagExit = false; // 게임을 시작하지않고 나감

	Vector<ImageView> heartArr1;
	Vector<ImageView> heartArr2;
	private int hostHeart = 4;
	private int guestHeart = 4;
	private int hostCount = 0;
	private int guestCount = 0;

	private Coordinate user;
	private Coordinate answer;
	private GraphicsContext gc = null;

	private int imageNumber = 0; // 이미지뷰에 들어갈 이미지 이름 (0~9.jpg)
	private ArrayList<AnswerList> arrAnswerList = new ArrayList<AnswerList>(); // 5라운드의 모든 정답좌표가 들어감
	private AnswerList nowAnswer = new AnswerList(); // 현재 진행중인 이미지의 정답객체 모음 (멤버변수로 coordinate리스트가 있음)

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 유저객체 받아오기
		if (clientUser == null) {
			clientUser = MainController.getClientUser();
			clientUser.setGameRoomController(this);
		} 
		clientUser.setGameRoomController(this);
		hostNickname = clientUser.getHostData();
		// 맞춘수 초기화 0, 닉네임 설정
		hostCnt.setText(hostCount + "");
		guestCnt.setText(guestCount + "");
		hostNick.setText(clientUser.getNickname());
		guestNick.setText("상대방을 기다리는 중");
		// 디폴트 이미지 가져오기
		hostImg.setImage(new Image(getClass().getResource("/resources/profile.png").toString()));
		guestImg.setImage(new Image(getClass().getResource("/resources/profile.png").toString()));
		imageView1.setImage(new Image(getClass().getResource("/resources/default.jpg").toString()));
		imageView2.setImage(new Image(getClass().getResource("/resources/default.jpg").toString()));
		// 하트 초기화(이미지설정)
		initHeartNumber();
		// 레디 비활성화, 기권 비활성화, 나가기 활성화
		btnReady.setDisable(true);
		btnSurrender.setDisable(true);
		btnExit.setDisable(false);
		// 상대방이 들어올때까지 대기 (라벨중 게임대기중만 활성화)
		lblWait1.setVisible(true);
		lblWait2.setVisible(false);
		lblReady.setVisible(false);
		lblStart.setVisible(false);
		lblNext.setVisible(false);
		// 레디버튼 액션처리
		btnReady.setOnAction(event -> {
			playerOnReady();
		});
		// 기권버튼 액션처리
		btnSurrender.setOnAction(event -> {
			playerSurrenderAction();
		});
		// 나가기버튼 액션
		btnExit.setOnAction(event -> {
			playerExitRoom();
		});
		// 마우스 이벤트
		mouseEvent();

		gameThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (waitForPlayer) { // 두명이 찰때까지 대기 (기본값true)
						System.out.println("모두 입장 완료. 준비 대기중...");
						if (flagExit == true) {
							return; // 방장이 나가면 방이 깨지므로 종료
						}
						if (start) { // 두명이 모두 들어오면 true (기본값false)

							while (nextStage) { // 라운드 시작 (기본값true)
								// 버튼및 라벨설정
								btnSurrender.setDisable(false);
								lblNext.setVisible(false);
								// 카운트다운 스레드 시작
								countDownThread();
								Thread.sleep(4000);
								nowAnswer = arrAnswerList.get(0); // 이번라운드 정답객체 받아옴
								arrAnswerList.remove(0); // 받아온 정답객체는 모체에서 삭제
								// 마우스이벤트 활성화
								mouseRock = false;
								// 게임 진입
								while (true) {
									System.out.println("라운드 진행중. . . 남은 정답갯수 : " + nowAnswer.getCd().size());
									// 5개 정답좌표 다 찾으면
									if (nowAnswer.getCd().size() == 0 && hostHeart > 0 && guestHeart > 0) {
										// 다 찾고 게임라운드도 모두 진행했으면 return
										if (gameCount == totalStage) {
											int hostScr = hostCount + (hostHeart - 5);
											int guestScr = guestCount + (guestHeart - 5);
											if (clientUser.getNickname().equals(hostNickname)) {
												if (hostCount > guestCount) {
													clientUser.send(ClientProtocol.GAME_END_HOSTWIN + "|" + hostNickname
															+ "|" + hostScr + "|" + guestScr);
												} else {
													clientUser.send(ClientProtocol.GAME_END_GUESTWIN + "|"
															+ hostNickname + "|" + hostScr + "|" + guestScr);
												}
											}
											return;
										} else {
											mouseRock = true; // 마우스 이벤트 비활성화
											System.out.println("다음 라운드로 넘어갑니다");
											gameCount++; // 진행된 게임수 +1

											lblNext.setVisible(true); // 다음게임으로 넘어감을 알림
											Thread.sleep(2000); // 2초 지연
											// 디폴트이미지 가져오기
											imageView1.setImage(new Image(
													getClass().getResource("/resources/default.jpg").toString()));
											imageView2.setImage(new Image(
													getClass().getResource("/resources/default.jpg").toString()));
											// 그려졌던 동그라미 그림 지우기
											GraphicsContext gc = canvas.getGraphicsContext2D();
											gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
											break;// while문 탈출. 카운트다운으로 돌아감.
										}
									}
									// 한쪽이 하트를 모두 잃을 경우
									if (hostHeart <= 0 || guestHeart <= 0) {
										int hostScr = hostCount + (hostHeart - 5);
										int guestScr = guestCount + (guestHeart - 5);
										if (hostHeart <= 0) {
											if (clientUser.getNickname().equals(hostNickname)) {
												clientUser.send(ClientProtocol.GAME_HOST_LOSTHEART + "|" + hostNickname
														+ "|" + hostScr + "|" + guestScr);
											}
											return;
										} else {
											if (clientUser.getNickname().equals(hostNickname)) {
												clientUser.send(ClientProtocol.GAME_GUEST_LOSTHEART + "|" + hostNickname
														+ "|" + hostScr + "|" + guestScr);
											}
											return;
										}
									}
									// 한쪽이 기권한경우
									if (surrender) {
										return;
									}
									Thread.sleep(100);// 0.1초 단위로 변화 감지.

								} // end of while

							} // end of next stage
							Thread.sleep(500);// 0.5초 단위로 변화 감지.
						} // end of start
						Thread.sleep(500);// 0.5초 단위로 변화 감지.
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		gameThread.start();

	}

	// 카운트다운 스레드
	public void countDownThread() {
		Thread thread = new Thread(new Runnable() {
			private int count = 3;

			@Override
			public void run() {
				try {
					while (true) {
						lblWait2.setVisible(false);
						lblReady.setVisible(true);
						lblStart.setVisible(false);
						Platform.runLater(() -> {
							lblReady.setText(String.valueOf(count--));
						});

						Thread.sleep(1000);
						if (count <= 0) {
							lblReady.setVisible(false);
							lblStart.setVisible(true);
							Thread.sleep(1000);
							lblStart.setVisible(false);
							// 이미지 불러오기
							imgSetting(imageNumber);
							imageNumber = imageNumber + 2;
							return;
						}
						System.out.println("카운트 스레드: " + count);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

	// 게임시작시 게임이미지 불러오기
	public void imgSetting(int number) {
		try {
			File pathFile = new File("src/resources/images/path.txt");
			String downPath = pathFile.getParent();
			
			File image1 = new File(downPath+"/"+number+".jpg");
			File image2 = new File(downPath+"/"+(number+1)+".jpg");

			String localUrl1;
			localUrl1 = image1.toURI().toURL().toString();

			String localUrl2 = image2.toURI().toURL().toString();
			Image localImage1 = new Image(localUrl1, false);
			Image localImage2 = new Image(localUrl2, false);
			imageView1.setImage(localImage1);
			imageView2.setImage(localImage2);
			return;

		} catch (MalformedURLException e) {// | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void mouseEvent() {
		pane.setOnMouseClicked(event -> {
			if (mouseRock) {
				return;
			}
			user = new Coordinate();
			// 사용자의 마우스 좌표 얻기
			user.setX((int) event.getX());
			user.setY((int) event.getY());

			// 정답을 클릭했음을 알려주는 변수
			boolean find = false;
			// 정답vector들과 사용자의 마우스 좌표를 비교하기위한 for문
			for (int i = 0; i < nowAnswer.getCd().size(); i++) {
				answer = nowAnswer.getCd().get(i);
				System.out.println("사용자의 값" + user);
				System.out.println("정답값 " + i + "=" + answer);
				// 사용자좌표와 정답좌표를 비교해서 같으면 true반환
				find = compareUserWithAnswer(user, answer);

				if (find) {
					// 서버로 정답처리 요청 보내기(모든 플레이어에게 해당좌표에 동그라미찍힘)
					clientUser.send(ClientProtocol.GAME_CD_FOUND + "|" + hostNickname + "|" + clientUser.getNickname()
							+ "|" + answer.getX() + "|" + answer.getY());
					// 동그라미그리기
					break;
				}
				System.out.println("=======================");
			} // end fo for
			if (!find) {
				System.out.println("오답클릭 " + clientUser.getNickname());
				// 서버로 오답처리 요청보내기 (하트 깎임)
				clientUser.send(ClientProtocol.GAME_CD_NOT_FOUND + "|" + hostNickname + "|" + clientUser.getNickname());
			}

		});// end of setOnMouseClicked
	}

	public void alertFullRoom(String host, String guest) {
		Platform.runLater(() -> {
			hostNick.setText(host);
			guestNick.setText(guest);
			btnReady.setDisable(false);
			btnSurrender.setDisable(true);
			btnExit.setDisable(false);
			// 라벨 레디대기중으로 바꿈
			lblWait1.setVisible(false);
			lblWait2.setVisible(true);
			try {
				Stage dialog = new Stage(StageStyle.UTILITY);
				dialog.initModality(Modality.WINDOW_MODAL);
				dialog.initOwner(imageView1.getScene().getWindow());
				dialog.setTitle("게임준비 완료");
				VBox root = (VBox) FXMLLoader.load(getClass().getResource("/view/readyMessage.fxml"));
				// ===============================
				Button btnOk = (Button) root.lookup("#btnOk");
				btnOk.setOnAction(event -> {
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	// 게임결과 알림창
	public void alertGameResult(String value, String score, String hostRp, String guestRp) {
		Platform.runLater(() -> {
			String topMsg = "TopMsg";
			String midMsg = "MidMsg";
			String rp = "Rp";
			String coin = "Coin";

			switch (value) {
			case "LostHeart_Victory":
				topMsg = "승리!";
				midMsg = "상대방이 하트를\n모두 소진했습니다!\n랭크포인트를 획득합니다\n";
				if(clientUser.getNickname().equals(hostNickname)){
					rp = hostRp;
				}else {
					rp = guestRp;
				}
				coin = "100";
				break;

			case "LostHeart_Defeat":
				topMsg = "패배!";
				midMsg = "하트를모두\n소진했습니다!\n랭크포인트가 떨어집니다\n";
				if(clientUser.getNickname().equals(hostNickname)){
					rp = hostRp;
				}else {
					rp = guestRp;
				}
				coin = "50";
				break;

			case "Surrendr_Victory":
				topMsg = "승리!";
				midMsg = "상대방이 항복을\n선언했습니다!\n랭크포인트와 코인을 획득합니다\n";
				if(clientUser.getNickname().equals(hostNickname)){
					rp = hostRp;
				}else {
					rp = guestRp;
				}
				coin = "100";
				break;

			case "Surrendr_Defeat":
				topMsg = "패배!";
				midMsg = "항복을 선언했습니다!\n랭크포인트가 떨어집니다\n";
				if(clientUser.getNickname().equals(hostNickname)){
					rp = hostRp;
				}else {
					rp = guestRp;
				}
				coin = "50";
				break;

			case "EndGame_Victory":
				topMsg = "승리!";
				midMsg = "승리하였습니다!\n랭크포인트와 코인을 획득합니다\n";
				if(clientUser.getNickname().equals(hostNickname)){
					rp = hostRp;
				}else {
					rp = guestRp;
				}
				coin = "100";
				break;

			case "EndGame_Defeat":
				topMsg = "패배!";
				midMsg = "패배했습니다!\n랭크포인트가 떨어집니다\n";
				if(clientUser.getNickname().equals(hostNickname)){
					rp = hostRp;
				}else {
					rp = guestRp;
				}
				coin = "50";
				break;

			default:
				System.out.println("게임결과 오류발생");
				break;
			}

			try {
				Stage dialog = new Stage(StageStyle.UTILITY);
				dialog.initModality(Modality.WINDOW_MODAL);
				dialog.initOwner(imageView1.getScene().getWindow());
				dialog.setTitle("경기 결과");
				VBox root = (VBox) FXMLLoader.load(getClass().getResource("/view/gameresult.fxml"));
				// ===============================
				Label lblTop = (Label) root.lookup("#lblTop");
				Label lblScore = (Label) root.lookup("#lblScore");
				Label lblMid = (Label) root.lookup("#lblMid");
				Label lblRp = (Label) root.lookup("#lblRp");
				Label lblCoin = (Label) root.lookup("#lblCoin");
				Button btnOk = (Button) root.lookup("#btnOk");

				btnOk.setOnAction(event -> {
					dialog.close();
					try {
						FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
						Parent main;
						main = (Parent) loader.load();
						Scene scene = new Scene(main);
						MainController mainController = loader.getController();
						mainController.setPrimaryStage(primaryStage);
						scene.getStylesheets()
								.add(getClass().getResource("/application/clientcss.css").toExternalForm());
						scene.getStylesheets()
								.add("https://fonts.googleapis.com/css?family=Yeon+Sung&display=swap&subset=korean");
						primaryStage.setScene(scene);
						primaryStage.show();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});

				lblTop.setText(topMsg);
				lblScore.setText(score);
				lblMid.setText(midMsg);
				lblRp.setText(rp);
				lblCoin.setText(coin);

				Scene scene = new Scene(root);
				scene.getStylesheets().add(getClass().getResource("/application/clientcss.css").toExternalForm());
				scene.getStylesheets()
						.add("https://fonts.googleapis.com/css?family=Yeon+Sung&display=swap&subset=korean");
				dialog.setScene(scene);
				dialog.setResizable(false);
				dialog.show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
	}

	// 레디눌렀으때 서버로 메세지보냄
	private void playerOnReady() {
		clientUser.send(ClientProtocol.GAMEROOM_READY + "|" + clientUser.getHostData());
		btnReady.setDisable(true);
		btnExit.setDisable(true);
	}

	// 서버로부터 게임스타트 메세지 받음
	public void alertGameStart() {
		Platform.runLater(() -> {
			start = true;
			System.out.println("게임시작 . . 스레드가 계속 진행됩니다" + start);
		});

	}

	// 서버에서 받은 정답좌표 vector로 만들기. answerList에 추가
	public void setImageFileName(String coordinateList) {
		// 정답Coordinate객체리스트 만들기

		StringTokenizer token = new StringTokenizer(coordinateList, "//^");

		while (token.hasMoreTokens()) {
			AnswerList al = new AnswerList(token.nextToken());
			arrAnswerList.add(al);
		}

	}

	// 사용자좌표값과 정답좌표값을 비교하여 맞으면 true를 반환하는 함수. 정답좌표와 사용자좌표 두 점사이의 거리를 구해서 오차범위 이내면
	// true.
	public boolean compareUserWithAnswer(Coordinate user, Coordinate answer) {
		boolean find = false;
		int marginOfError = 30; // 오차 허용범위
		double distance = Math.sqrt((user.getX() - answer.getX()) * (user.getX() - answer.getX())
				+ (user.getY() - answer.getY()) * (user.getY() - answer.getY())); // 사용자좌표와 정답좌표 사이의 거리

		if (distance < marginOfError) {
			find = true;
		}

		return find;
	}

	// 찾았을때 동그라미 그림을 그리는 함수.
	public void drawCircle(Coordinate answer, Canvas canvas, String color) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		if (color.equals("red")) {
			Image circle = new Image(getClass().getResource("/resources/redCircle.png").toString());
			gc.drawImage(circle, answer.getX() - 25, answer.getY() - 25, 50, 50);
		} else {
			Image circle = new Image(getClass().getResource("/resources/blueCircle.png").toString());
			gc.drawImage(circle, answer.getX() - 25, answer.getY() - 25, 50, 50);
		}
	}

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	public void alertFoundCoordinate(String nickname, String x, String y) {
		Platform.runLater(() -> {
			Coordinate cd = new Coordinate(Integer.parseInt(x), Integer.parseInt(y));

			if (nickname.equals(hostNickname)) {
				hostCount++;
				hostCnt.setText(hostCount + "");
				drawCircle(cd, canvas, "red");
				System.out.println(cd + "호스트가 찾음");
			} else if (nickname.equals(guestNick.getText())) {
				guestCount++;
				guestCnt.setText(guestCount + "");
				drawCircle(cd, canvas, "blue");
				System.out.println(cd + "게스트가 찾음");
			} else {
				System.out.println("alertFoundCoordinate 오류 - 호스트나 게스트의 이름을 서버에서 보내지않음");
			}
			// 서버에서 보낸 상대방(혹은 내가)이 맞춘 좌표를 cdArr에 찾아 지워주기 (이다음에 클릭하면 정답처리가 안됨)
			for (int i = 0; i < nowAnswer.getCd().size(); i++) {
				if ((nowAnswer.getCd().get(i).toString()).equals(cd.toString())) {
					nowAnswer.getCd().remove(i);
					break;
				}
			}

		});

	}

	// 정답 틀렸을때
	public void alertNotFoundCoordinate(String nickname) {
		Platform.runLater(() -> {
			if (nickname.equals(hostNickname)) {
				hostHeart--;
				hostHt.getChildren().clear();
				for (int i = 0; i < hostHeart; i++) {
					hostHt.getChildren().add(heartArr1.get(i));
				}

			} else {
				guestHeart--;
				guestHt.getChildren().clear();
				for (int i = 0; i < guestHeart; i++) {
					guestHt.getChildren().add(heartArr2.get(i));
				}
			}

		});

	}

	// 기권버튼 액션처리
	private void playerSurrenderAction() {
		int hostScr = hostCount + (hostHeart - 5);
		int guestScr = guestCount + (guestHeart - 5);

		if (clientUser.getNickname().contentEquals(hostNickname)) {
			clientUser.send(ClientProtocol.GAME_HOST_SURRENDER + "|" + hostNickname + "|" + hostScr + "|" + guestScr);
		} else {
			clientUser.send(ClientProtocol.GAME_GUEST_SURRENDER + "|" + hostNickname + "|" + hostScr + "|" + guestScr);
		}
	}

	// 호스트가 항복했음을 서버에서 받음
	public void alertHostSurrender(String hostRp, String guestRp) {
		Platform.runLater(() -> {
			surrender = true;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 호스트가 나일경우 패배
			if (clientUser.getNickname().equals(hostNickname)) {
				alertGameResult("Surrendr_Defeat", "[스코어] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
			// 게스트가 상대방일경우 승리
			else {
				alertGameResult("Surrendr_Victory", "[스코어] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
		});

	}

	// 게스트가 항복했음을 서버에서 받음
	public void alertGuestSurrender(String hostRp, String guestRp) {
		Platform.runLater(() -> {
			surrender = true;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 호스트가 나일경우 승리
			if (clientUser.getNickname().equals(hostNickname)) {
				alertGameResult("Surrendr_Victory", "[스코어] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
			// 게스트가 나일경우 패배
			else {
				alertGameResult("Surrendr_Defeat", "[스코어] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
		});
	}

	// 호스트가 하트를 잃었을때(서버에서 받음)
	public void alertHostLostHeart(String hostRp, String guestRp) {
		Platform.runLater(() -> {
			// 호스트가 나일경우 패배
			if (clientUser.getNickname().equals(hostNickname)) {
				alertGameResult("LostHeart_Defeat", "[스코어] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
			// 게스트가 나일경우 승리
			else {
				alertGameResult("LostHeart_Victory", "[스코어] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
		});
	}

	// 게스트가 하트를 잃었을때
	public void alertGuestLostHeart(String hostRp, String guestRp) {
		Platform.runLater(() -> {
			// 호스트가 나일경우 승리
			if (clientUser.getNickname().equals(hostNickname)) {
				alertGameResult("LostHeart_Victory", "[스코어] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
			// 게스트가 나일경우 패배
			else {
				alertGameResult("LostHeart_Defeat", "[스코어] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
		});
	}

	// 호스트가 더 많이 맞췄을때
	public void alertHostWinEnding(String hostRp, String guestRp) {
		Platform.runLater(() -> {
			// 호스트가 나일경우 승리
			if (clientUser.getNickname().equals(hostNickname)) {
				alertGameResult("EndGame_Victory", "[스코어] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
			// 게스트가 나일경우 패배
			else {
				alertGameResult("EndGame_Defeat", "[스코어] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
		});

	}

	// 게스트가 더 많이 맞출경우
	public void alertGuestWinEndeing(String hostRp, String guestRp) {
		Platform.runLater(() -> {
			// 호스트가 나일경우 패배
			if (clientUser.getNickname().equals(hostNickname)) {
				alertGameResult("EndGame_Defeat", "[스코어] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
			// 게스트가 나일경우 승리
			else {
				alertGameResult("EndGame_Victory", "[스코어] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
		});

	}

	// 플레이어가 나가기 버튼을 누름
	private void playerExitRoom() {
		// 호스트가 나갔을때
		if (clientUser.getNickname().equals(hostNickname)) {
			clientUser.send(ClientProtocol.GAME_HOST_EXIT + "|" + hostNickname);
		} else { // 게스트가 나갔을때
			clientUser.send(ClientProtocol.GAME_GUEST_EXIT + "|" + hostNickname);
		}
	}

	// 호스트가 나갔다고 서버로부터 알림받음
	public void alertExitHost() {
		Platform.runLater(() -> {
			if (clientUser.getNickname().equals(hostNickname)) { // 내가 호스트일때 (로비로 돌아감)
				flagExit = true;
				goToMain();

			} else { // 내가 게스트일때 (알림후 로비로)
				clientUser.alertDisplay(1, "방장 탈주", "방장이 나갔습니다", "로비로 돌아갑니다. 새로운 방에 들어가세요!");

				flagExit = true;
				goToMain();
			}
		});

	}

	// 게스트가 나갔다고 서버로부터 알림받음
	public void alertExitGuest() {
		Platform.runLater(() -> {
			if (clientUser.getNickname().equals(hostNickname)) { // 내가 호스트일때 (게스트가 나갔다고 알림받음)
				clientUser.alertDisplay(1, "상대방 탈주", "상대방이 나갔습니다", "새로운 사람이 들어올 때까지 기다려볼까요?");
			} else { // 내가 게스트일때 (바로 로비로)
				flagExit = true;
				goToMain();

			}
		});
	}

	// 메인페이지로 돌아감
	private void goToMain() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
			Parent main= (Parent) loader.load();
			Scene scene = new Scene(main);

			MainController mainController = loader.getController();
			mainController.setPrimaryStage(primaryStage);
			scene.getStylesheets().add(getClass().getResource("/application/clientcss.css").toExternalForm());
			scene.getStylesheets().add("https://fonts.googleapis.com/css?family=Yeon+Sung&display=swap&subset=korean");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initHeartNumber() {
		ImageView heart1 = new ImageView(new Image(getClass().getResource("/resources/heart.png").toString()));
		ImageView heart2 = new ImageView(new Image(getClass().getResource("/resources/heart.png").toString()));
		ImageView heart3 = new ImageView(new Image(getClass().getResource("/resources/heart.png").toString()));
		ImageView heart4 = new ImageView(new Image(getClass().getResource("/resources/heart.png").toString()));
		ImageView heart5 = new ImageView(new Image(getClass().getResource("/resources/heart.png").toString()));
		ImageView heart6 = new ImageView(new Image(getClass().getResource("/resources/heart.png").toString()));
		ImageView heart7 = new ImageView(new Image(getClass().getResource("/resources/heart.png").toString()));
		ImageView heart8 = new ImageView(new Image(getClass().getResource("/resources/heart.png").toString()));

		heartArr1 = new Vector<ImageView>();
		heartArr2 = new Vector<ImageView>();
		heartArr1.add(heart1);
		heartArr1.add(heart2);
		heartArr1.add(heart3);
		heartArr1.add(heart4);
		heartArr2.add(heart5);
		heartArr2.add(heart6);
		heartArr2.add(heart7);
		heartArr2.add(heart8);

		for (int i = 0; i < hostHeart; i++) {
			hostHt.getChildren().add(heartArr1.get(i));
		}
		for (int i = 0; i < guestHeart; i++) {
			guestHt.getChildren().add(heartArr2.get(i));
		}

	}
	
	

	public static ClientUser getClientUser() {
		return clientUser;
	}

	public static void setClientUser(ClientUser clientUser) {
		GameRoomController.clientUser = clientUser;
	}



	// 한문제에 들어있는 5개의 정답좌표를 관리하기 위한 클래스
	class AnswerList {
		ArrayList<Coordinate> cd;

		public AnswerList() {
			cd = new ArrayList<Coordinate>();
		}

		public AnswerList(ArrayList<Coordinate> cd) {
			super();
			this.cd = cd;
		}

		public AnswerList(String coordinates) {

			cd = new ArrayList<Coordinate>(); // 정답좌표를 리스트로 만듬

			StringTokenizer token = new StringTokenizer(coordinates, "//,");
			while (token.hasMoreTokens()) {
				Coordinate c = new Coordinate(Integer.parseInt(token.nextToken()), Integer.parseInt(token.nextToken()));
				cd.add(c);
			}
		}

		public ArrayList<Coordinate> getCd() {
			return cd;
		}

		public void setCd(ArrayList<Coordinate> cd) {
			this.cd = cd;
		}

	}
	
	

}
