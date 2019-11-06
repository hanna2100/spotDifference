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

	private final int totalStage = 5; // ��ü ���� �� 
	private int gameCount = 1; // ������ ���Ӽ�

	private boolean waitForPlayer = true; // �����Ҷ����� ��ٸ��� flag
	private boolean start = false; // ��� �÷��̾� �����ߴ°�? flag(���ӽ���)
	private boolean nextStage = true; // ���� ���������� �Ѿ�°�? flag
	private boolean mouseRock = true; // ���콺�̺�Ʈ on off�� ���� flag

	private boolean surrender = false; //  ����Ѱ��

	private boolean flagExit = false; // ������ ���������ʰ� ����

	Vector<ImageView> heartArr1;
	Vector<ImageView> heartArr2;
	private int hostHeart = 4;
	private int guestHeart = 4;
	private int hostCount = 0;
	private int guestCount = 0;

	private Coordinate user;
	private Coordinate answer;
	private GraphicsContext gc = null;

	private int imageNumber = 0; // �̹����信 �� �̹��� �̸� (0~9.jpg)
	private ArrayList<AnswerList> arrAnswerList = new ArrayList<AnswerList>(); // 5������ ��� ������ǥ�� ��
	private AnswerList nowAnswer = new AnswerList(); // ���� �������� �̹����� ���䰴ü ���� (��������� coordinate����Ʈ�� ����)

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// ������ü �޾ƿ���
		if (clientUser == null) {
			clientUser = MainController.getClientUser();
			clientUser.setGameRoomController(this);
		} 
		clientUser.setGameRoomController(this);
		hostNickname = clientUser.getHostData();
		// ����� �ʱ�ȭ 0, �г��� ����
		hostCnt.setText(hostCount + "");
		guestCnt.setText(guestCount + "");
		hostNick.setText(clientUser.getNickname());
		guestNick.setText("������ ��ٸ��� ��");
		// ����Ʈ �̹��� ��������
		hostImg.setImage(new Image(getClass().getResource("/resources/profile.png").toString()));
		guestImg.setImage(new Image(getClass().getResource("/resources/profile.png").toString()));
		imageView1.setImage(new Image(getClass().getResource("/resources/default.jpg").toString()));
		imageView2.setImage(new Image(getClass().getResource("/resources/default.jpg").toString()));
		// ��Ʈ �ʱ�ȭ(�̹�������)
		initHeartNumber();
		// ���� ��Ȱ��ȭ, ��� ��Ȱ��ȭ, ������ Ȱ��ȭ
		btnReady.setDisable(true);
		btnSurrender.setDisable(true);
		btnExit.setDisable(false);
		// ������ ���ö����� ��� (���� ���Ӵ���߸� Ȱ��ȭ)
		lblWait1.setVisible(true);
		lblWait2.setVisible(false);
		lblReady.setVisible(false);
		lblStart.setVisible(false);
		lblNext.setVisible(false);
		// �����ư �׼�ó��
		btnReady.setOnAction(event -> {
			playerOnReady();
		});
		// ��ǹ�ư �׼�ó��
		btnSurrender.setOnAction(event -> {
			playerSurrenderAction();
		});
		// �������ư �׼�
		btnExit.setOnAction(event -> {
			playerExitRoom();
		});
		// ���콺 �̺�Ʈ
		mouseEvent();

		gameThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (waitForPlayer) { // �θ��� �������� ��� (�⺻��true)
						System.out.println("��� ���� �Ϸ�. �غ� �����...");
						if (flagExit == true) {
							return; // ������ ������ ���� �����Ƿ� ����
						}
						if (start) { // �θ��� ��� ������ true (�⺻��false)

							while (nextStage) { // ���� ���� (�⺻��true)
								// ��ư�� �󺧼���
								btnSurrender.setDisable(false);
								lblNext.setVisible(false);
								// ī��Ʈ�ٿ� ������ ����
								countDownThread();
								Thread.sleep(4000);
								nowAnswer = arrAnswerList.get(0); // �̹����� ���䰴ü �޾ƿ�
								arrAnswerList.remove(0); // �޾ƿ� ���䰴ü�� ��ü���� ����
								// ���콺�̺�Ʈ Ȱ��ȭ
								mouseRock = false;
								// ���� ����
								while (true) {
									System.out.println("���� ������. . . ���� ���䰹�� : " + nowAnswer.getCd().size());
									// 5�� ������ǥ �� ã����
									if (nowAnswer.getCd().size() == 0 && hostHeart > 0 && guestHeart > 0) {
										// �� ã�� ���Ӷ��嵵 ��� ���������� return
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
											mouseRock = true; // ���콺 �̺�Ʈ ��Ȱ��ȭ
											System.out.println("���� ����� �Ѿ�ϴ�");
											gameCount++; // ����� ���Ӽ� +1

											lblNext.setVisible(true); // ������������ �Ѿ�� �˸�
											Thread.sleep(2000); // 2�� ����
											// ����Ʈ�̹��� ��������
											imageView1.setImage(new Image(
													getClass().getResource("/resources/default.jpg").toString()));
											imageView2.setImage(new Image(
													getClass().getResource("/resources/default.jpg").toString()));
											// �׷����� ���׶�� �׸� �����
											GraphicsContext gc = canvas.getGraphicsContext2D();
											gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
											break;// while�� Ż��. ī��Ʈ�ٿ����� ���ư�.
										}
									}
									// ������ ��Ʈ�� ��� ���� ���
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
									// ������ ����Ѱ��
									if (surrender) {
										return;
									}
									Thread.sleep(100);// 0.1�� ������ ��ȭ ����.

								} // end of while

							} // end of next stage
							Thread.sleep(500);// 0.5�� ������ ��ȭ ����.
						} // end of start
						Thread.sleep(500);// 0.5�� ������ ��ȭ ����.
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		gameThread.start();

	}

	// ī��Ʈ�ٿ� ������
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
							// �̹��� �ҷ�����
							imgSetting(imageNumber);
							imageNumber = imageNumber + 2;
							return;
						}
						System.out.println("ī��Ʈ ������: " + count);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

	// ���ӽ��۽� �����̹��� �ҷ�����
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
			// ������� ���콺 ��ǥ ���
			user.setX((int) event.getX());
			user.setY((int) event.getY());

			// ������ Ŭ�������� �˷��ִ� ����
			boolean find = false;
			// ����vector��� ������� ���콺 ��ǥ�� ���ϱ����� for��
			for (int i = 0; i < nowAnswer.getCd().size(); i++) {
				answer = nowAnswer.getCd().get(i);
				System.out.println("������� ��" + user);
				System.out.println("���䰪 " + i + "=" + answer);
				// �������ǥ�� ������ǥ�� ���ؼ� ������ true��ȯ
				find = compareUserWithAnswer(user, answer);

				if (find) {
					// ������ ����ó�� ��û ������(��� �÷��̾�� �ش���ǥ�� ���׶������)
					clientUser.send(ClientProtocol.GAME_CD_FOUND + "|" + hostNickname + "|" + clientUser.getNickname()
							+ "|" + answer.getX() + "|" + answer.getY());
					// ���׶�̱׸���
					break;
				}
				System.out.println("=======================");
			} // end fo for
			if (!find) {
				System.out.println("����Ŭ�� " + clientUser.getNickname());
				// ������ ����ó�� ��û������ (��Ʈ ����)
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
			// �� ������������ �ٲ�
			lblWait1.setVisible(false);
			lblWait2.setVisible(true);
			try {
				Stage dialog = new Stage(StageStyle.UTILITY);
				dialog.initModality(Modality.WINDOW_MODAL);
				dialog.initOwner(imageView1.getScene().getWindow());
				dialog.setTitle("�����غ� �Ϸ�");
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

	// ���Ӱ�� �˸�â
	public void alertGameResult(String value, String score, String hostRp, String guestRp) {
		Platform.runLater(() -> {
			String topMsg = "TopMsg";
			String midMsg = "MidMsg";
			String rp = "Rp";
			String coin = "Coin";

			switch (value) {
			case "LostHeart_Victory":
				topMsg = "�¸�!";
				midMsg = "������ ��Ʈ��\n��� �����߽��ϴ�!\n��ũ����Ʈ�� ȹ���մϴ�\n";
				if(clientUser.getNickname().equals(hostNickname)){
					rp = hostRp;
				}else {
					rp = guestRp;
				}
				coin = "100";
				break;

			case "LostHeart_Defeat":
				topMsg = "�й�!";
				midMsg = "��Ʈ�����\n�����߽��ϴ�!\n��ũ����Ʈ�� �������ϴ�\n";
				if(clientUser.getNickname().equals(hostNickname)){
					rp = hostRp;
				}else {
					rp = guestRp;
				}
				coin = "50";
				break;

			case "Surrendr_Victory":
				topMsg = "�¸�!";
				midMsg = "������ �׺���\n�����߽��ϴ�!\n��ũ����Ʈ�� ������ ȹ���մϴ�\n";
				if(clientUser.getNickname().equals(hostNickname)){
					rp = hostRp;
				}else {
					rp = guestRp;
				}
				coin = "100";
				break;

			case "Surrendr_Defeat":
				topMsg = "�й�!";
				midMsg = "�׺��� �����߽��ϴ�!\n��ũ����Ʈ�� �������ϴ�\n";
				if(clientUser.getNickname().equals(hostNickname)){
					rp = hostRp;
				}else {
					rp = guestRp;
				}
				coin = "50";
				break;

			case "EndGame_Victory":
				topMsg = "�¸�!";
				midMsg = "�¸��Ͽ����ϴ�!\n��ũ����Ʈ�� ������ ȹ���մϴ�\n";
				if(clientUser.getNickname().equals(hostNickname)){
					rp = hostRp;
				}else {
					rp = guestRp;
				}
				coin = "100";
				break;

			case "EndGame_Defeat":
				topMsg = "�й�!";
				midMsg = "�й��߽��ϴ�!\n��ũ����Ʈ�� �������ϴ�\n";
				if(clientUser.getNickname().equals(hostNickname)){
					rp = hostRp;
				}else {
					rp = guestRp;
				}
				coin = "50";
				break;

			default:
				System.out.println("���Ӱ�� �����߻�");
				break;
			}

			try {
				Stage dialog = new Stage(StageStyle.UTILITY);
				dialog.initModality(Modality.WINDOW_MODAL);
				dialog.initOwner(imageView1.getScene().getWindow());
				dialog.setTitle("��� ���");
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

	// ���𴭷����� ������ �޼�������
	private void playerOnReady() {
		clientUser.send(ClientProtocol.GAMEROOM_READY + "|" + clientUser.getHostData());
		btnReady.setDisable(true);
		btnExit.setDisable(true);
	}

	// �����κ��� ���ӽ�ŸƮ �޼��� ����
	public void alertGameStart() {
		Platform.runLater(() -> {
			start = true;
			System.out.println("���ӽ��� . . �����尡 ��� ����˴ϴ�" + start);
		});

	}

	// �������� ���� ������ǥ vector�� �����. answerList�� �߰�
	public void setImageFileName(String coordinateList) {
		// ����Coordinate��ü����Ʈ �����

		StringTokenizer token = new StringTokenizer(coordinateList, "//^");

		while (token.hasMoreTokens()) {
			AnswerList al = new AnswerList(token.nextToken());
			arrAnswerList.add(al);
		}

	}

	// �������ǥ���� ������ǥ���� ���Ͽ� ������ true�� ��ȯ�ϴ� �Լ�. ������ǥ�� �������ǥ �� �������� �Ÿ��� ���ؼ� �������� �̳���
	// true.
	public boolean compareUserWithAnswer(Coordinate user, Coordinate answer) {
		boolean find = false;
		int marginOfError = 30; // ���� ������
		double distance = Math.sqrt((user.getX() - answer.getX()) * (user.getX() - answer.getX())
				+ (user.getY() - answer.getY()) * (user.getY() - answer.getY())); // �������ǥ�� ������ǥ ������ �Ÿ�

		if (distance < marginOfError) {
			find = true;
		}

		return find;
	}

	// ã������ ���׶�� �׸��� �׸��� �Լ�.
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
				System.out.println(cd + "ȣ��Ʈ�� ã��");
			} else if (nickname.equals(guestNick.getText())) {
				guestCount++;
				guestCnt.setText(guestCount + "");
				drawCircle(cd, canvas, "blue");
				System.out.println(cd + "�Խ�Ʈ�� ã��");
			} else {
				System.out.println("alertFoundCoordinate ���� - ȣ��Ʈ�� �Խ�Ʈ�� �̸��� �������� ����������");
			}
			// �������� ���� ����(Ȥ�� ����)�� ���� ��ǥ�� cdArr�� ã�� �����ֱ� (�̴����� Ŭ���ϸ� ����ó���� �ȵ�)
			for (int i = 0; i < nowAnswer.getCd().size(); i++) {
				if ((nowAnswer.getCd().get(i).toString()).equals(cd.toString())) {
					nowAnswer.getCd().remove(i);
					break;
				}
			}

		});

	}

	// ���� Ʋ������
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

	// ��ǹ�ư �׼�ó��
	private void playerSurrenderAction() {
		int hostScr = hostCount + (hostHeart - 5);
		int guestScr = guestCount + (guestHeart - 5);

		if (clientUser.getNickname().contentEquals(hostNickname)) {
			clientUser.send(ClientProtocol.GAME_HOST_SURRENDER + "|" + hostNickname + "|" + hostScr + "|" + guestScr);
		} else {
			clientUser.send(ClientProtocol.GAME_GUEST_SURRENDER + "|" + hostNickname + "|" + hostScr + "|" + guestScr);
		}
	}

	// ȣ��Ʈ�� �׺������� �������� ����
	public void alertHostSurrender(String hostRp, String guestRp) {
		Platform.runLater(() -> {
			surrender = true;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// ȣ��Ʈ�� ���ϰ�� �й�
			if (clientUser.getNickname().equals(hostNickname)) {
				alertGameResult("Surrendr_Defeat", "[���ھ�] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
			// �Խ�Ʈ�� �����ϰ�� �¸�
			else {
				alertGameResult("Surrendr_Victory", "[���ھ�] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
		});

	}

	// �Խ�Ʈ�� �׺������� �������� ����
	public void alertGuestSurrender(String hostRp, String guestRp) {
		Platform.runLater(() -> {
			surrender = true;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// ȣ��Ʈ�� ���ϰ�� �¸�
			if (clientUser.getNickname().equals(hostNickname)) {
				alertGameResult("Surrendr_Victory", "[���ھ�] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
			// �Խ�Ʈ�� ���ϰ�� �й�
			else {
				alertGameResult("Surrendr_Defeat", "[���ھ�] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
		});
	}

	// ȣ��Ʈ�� ��Ʈ�� �Ҿ�����(�������� ����)
	public void alertHostLostHeart(String hostRp, String guestRp) {
		Platform.runLater(() -> {
			// ȣ��Ʈ�� ���ϰ�� �й�
			if (clientUser.getNickname().equals(hostNickname)) {
				alertGameResult("LostHeart_Defeat", "[���ھ�] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
			// �Խ�Ʈ�� ���ϰ�� �¸�
			else {
				alertGameResult("LostHeart_Victory", "[���ھ�] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
		});
	}

	// �Խ�Ʈ�� ��Ʈ�� �Ҿ�����
	public void alertGuestLostHeart(String hostRp, String guestRp) {
		Platform.runLater(() -> {
			// ȣ��Ʈ�� ���ϰ�� �¸�
			if (clientUser.getNickname().equals(hostNickname)) {
				alertGameResult("LostHeart_Victory", "[���ھ�] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
			// �Խ�Ʈ�� ���ϰ�� �й�
			else {
				alertGameResult("LostHeart_Defeat", "[���ھ�] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
		});
	}

	// ȣ��Ʈ�� �� ���� ��������
	public void alertHostWinEnding(String hostRp, String guestRp) {
		Platform.runLater(() -> {
			// ȣ��Ʈ�� ���ϰ�� �¸�
			if (clientUser.getNickname().equals(hostNickname)) {
				alertGameResult("EndGame_Victory", "[���ھ�] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
			// �Խ�Ʈ�� ���ϰ�� �й�
			else {
				alertGameResult("EndGame_Defeat", "[���ھ�] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
		});

	}

	// �Խ�Ʈ�� �� ���� ������
	public void alertGuestWinEndeing(String hostRp, String guestRp) {
		Platform.runLater(() -> {
			// ȣ��Ʈ�� ���ϰ�� �й�
			if (clientUser.getNickname().equals(hostNickname)) {
				alertGameResult("EndGame_Defeat", "[���ھ�] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
			// �Խ�Ʈ�� ���ϰ�� �¸�
			else {
				alertGameResult("EndGame_Victory", "[���ھ�] " + hostCount + " : " + guestCount, hostRp, guestRp);
			}
		});

	}

	// �÷��̾ ������ ��ư�� ����
	private void playerExitRoom() {
		// ȣ��Ʈ�� ��������
		if (clientUser.getNickname().equals(hostNickname)) {
			clientUser.send(ClientProtocol.GAME_HOST_EXIT + "|" + hostNickname);
		} else { // �Խ�Ʈ�� ��������
			clientUser.send(ClientProtocol.GAME_GUEST_EXIT + "|" + hostNickname);
		}
	}

	// ȣ��Ʈ�� �����ٰ� �����κ��� �˸�����
	public void alertExitHost() {
		Platform.runLater(() -> {
			if (clientUser.getNickname().equals(hostNickname)) { // ���� ȣ��Ʈ�϶� (�κ�� ���ư�)
				flagExit = true;
				goToMain();

			} else { // ���� �Խ�Ʈ�϶� (�˸��� �κ��)
				clientUser.alertDisplay(1, "���� Ż��", "������ �������ϴ�", "�κ�� ���ư��ϴ�. ���ο� �濡 ������!");

				flagExit = true;
				goToMain();
			}
		});

	}

	// �Խ�Ʈ�� �����ٰ� �����κ��� �˸�����
	public void alertExitGuest() {
		Platform.runLater(() -> {
			if (clientUser.getNickname().equals(hostNickname)) { // ���� ȣ��Ʈ�϶� (�Խ�Ʈ�� �����ٰ� �˸�����)
				clientUser.alertDisplay(1, "���� Ż��", "������ �������ϴ�", "���ο� ����� ���� ������ ��ٷ������?");
			} else { // ���� �Խ�Ʈ�϶� (�ٷ� �κ��)
				flagExit = true;
				goToMain();

			}
		});
	}

	// ������������ ���ư�
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



	// �ѹ����� ����ִ� 5���� ������ǥ�� �����ϱ� ���� Ŭ����
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

			cd = new ArrayList<Coordinate>(); // ������ǥ�� ����Ʈ�� ����

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
