package controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ClientUser {
	// ��Ʈ�ѷ� ������ ���� �������
	private MainController mainController;
	private LoginController loginController;
	private JoinController joinController;
	private CreateRoomController createRoomController;
	private GameRoomController gameRoomController;
	private MyInfomationController myInfomationController;

	Socket socket;
	public static ExecutorService threadPool;
	Vector<String> clients = new Vector<String>();

	// �׽�Ʈ�� ���� ������, ��Ʈ
	String IP = "127.0.0.1";
	int port = 9000;

	String fileName = null; // �̹��� ���� ���۽� �����̸����� (���� ��Ʈ�ѷ����� ���� �̸��� ���⶧���� �ٲ�� �ȵ�)

	private String id; // ���̵�
	private String pw; // ���
	private String nickname; // �г���
	private String hostData; // ���ӹ� ������ �����(������� ���̺��� �⺻Ű�� ���� �ִ°� ���ҰŰ���)
	
	public void startClient(String IP, int port) {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					socket = new Socket(IP, port);
					receive();
				} catch (Exception e) {
					if (socket != null && !socket.isClosed()) {
						System.out.println("[���� ���� ����]");
						stopClient();
						Platform.exit();
					}
				}

			}
		});
		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);

	}

	public void stopClient() {
		// �����̳� ������Ǯ�� ���������� �ݱ�

		try {
			if (socket != null && !socket.isClosed())
				socket.close();
			if (!threadPool.isShutdown())
				threadPool.shutdown();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void receive() {
		Runnable thread = new Thread(new Runnable() {
			// �޼����޴� ��Ʈ��
			InputStream in = null;

			@Override
			public void run() {
				try {
					while (true) {
						in = socket.getInputStream();
						try {

							byte[] buffer = new byte[1024];

							int length = in.read(buffer, 0, buffer.length);
							if (length == -1) {
								throw new IOException();
							}

							String message = new String(buffer, 0, length, "UTF-8");
							String[] data = message.split("\\|");
							System.out.println(message);

							int protocol = Integer.parseInt(data[0]);

							switch (protocol) {
							
							case ClientProtocol.CONNECT_CHECK:// ���Ῡ�� �����Ű�
								send(ClientProtocol.CONNECT_CHECK + "");
								break;

							case ClientProtocol.JOIN_IDDUPLICATE: // ȸ������/�ߺ����̵��϶�
								joinController.alertIdDuplicate(data[1]);
								break;

							case ClientProtocol.JOIN_NICKDUPLICATE:// ȸ������/�ߺ��г����϶�
								joinController.alertNickDuplicate(data[1]);
								break;

							case ClientProtocol.JOIN_ACCEPT:// ȸ������ ����or����
								joinController.alertJoinResult(data[1]);
								break;

							case ClientProtocol.LOGIN_ACCEPT: // �α��� ����or����
								loginController.alertLoginResult(data[1], data[2]);
								break;

							case ClientProtocol.USERINFO_JOIN: // ���Խ� ���� ��������
								boolean isInfoExisted = Boolean.parseBoolean(data[1]);
								if (isInfoExisted == true) {
									mainController.setUserJoinInfo(data[2], data[3], data[4]);

								} else {
									mainController.alertNotFoundUserInfo();
								}
								break;

							case ClientProtocol.CHATTING: // ����ä��
								mainController.showMainChatting(data);
								break;
								
							case ClientProtocol.MAIN_NOWPLAYERS: // ����ä��
								mainController.showPlayersList(data[1]);
								break;	
								
							case ClientProtocol.WELCOME: // ����ä��-���ĸ޼���
								mainController.showMainChatting(data[1]);
								break;

							case ClientProtocol.CREATEROOM: // �游��� ��û�� ��������
								createRoomController.closeStage(); // �游���â �ݱ�
								mainController.alertRoomCreateResult(data[1]); // DB ����� ����� �˷���(true�ϰ�� ���ӷ����� ��)
								break;

							case ClientProtocol.MAINPAGE_REFRESH: // DB gameRoom���̺� ������
								if (data[1].equals("true")) { // db���� 1���̻��� ������ ������
									mainController.createNewRoomList(data[2]);
								} else { // �����Ͱ� ��������
									mainController.createNewRoomList();
								}

								break;

							case ClientProtocol.RANK_LISTSET: // DB �����̺� ������
								if (data[1].equals("true")) { // db���� 1���̻��� ������ ������
									mainController.createNewRankList(data[2]);
								} else { // �����Ͱ� ��������
									mainController.createNewRankList();
								}

								break;

							case ClientProtocol.RANK_HISTORY: // �� �����丮 ��������
								if (data[1].equals("true")) { // db���� 1���̻��� ������ ������
									mainController.getRankHistory(data[2]);
								} else { // �����Ͱ� ��������
									mainController.getRankHistory();
								}

								break;

							case ClientProtocol.GUEST_ENTER:// ���� �����ϱ� ���ٿ���
								mainController.alertGuestEnterResult(data[1]);
								break;

							case ClientProtocol.GAMEROOM_FULL: // Ǯ���϶� �˸�
								gameRoomController.alertFullRoom(data[1], data[2]);// ȣ��Ʈ��, �Խ�Ʈ�� ���޹���
								break;

							case ClientProtocol.GAMEROOM_READY: // ��� ����Ϸ��϶�
								gameRoomController.alertGameStart();
								try {
									new FileRecv(IP, fileName).start(); // ������ ����.
								} catch (Exception e) {
									System.out.println("FileRecv �������");
									e.printStackTrace();
								}
								gameRoomController.setImageFileName(data[1]);
								break;

							case ClientProtocol.GAME_CD_FOUND:
								gameRoomController.alertFoundCoordinate(data[1], data[2], data[3]);
								break;

							case ClientProtocol.GAME_CD_NOT_FOUND:
								gameRoomController.alertNotFoundCoordinate(data[1]);
								break;

							case ClientProtocol.GAME_HOST_SURRENDER:
								gameRoomController.alertHostSurrender(data[1], data[2]);
								break;

							case ClientProtocol.GAME_GUEST_SURRENDER:
								gameRoomController.alertGuestSurrender(data[1], data[2]);
								break;

							case ClientProtocol.GAME_HOST_LOSTHEART:
								gameRoomController.alertHostLostHeart(data[1], data[2]);
								break;

							case ClientProtocol.GAME_GUEST_LOSTHEART:
								gameRoomController.alertGuestLostHeart(data[1], data[2]);
								break;

							case ClientProtocol.GAME_END_HOSTWIN:
								gameRoomController.alertHostWinEnding(data[1], data[2]);
								break;

							case ClientProtocol.GAME_END_GUESTWIN:
								gameRoomController.alertGuestWinEndeing(data[1], data[2]);
								break;

							case ClientProtocol.GAME_HOST_EXIT:
								gameRoomController.alertExitHost();
								break;

							case ClientProtocol.GAME_GUEST_EXIT:
								gameRoomController.alertExitGuest();
								break;

							case ClientProtocol.MYINFO_NICKDUPLICATE:
								myInfomationController.alertNickDuplicate(data[1]);
								break;

							case ClientProtocol.MYINFO_NICK_CHANGE:
								myInfomationController.alertNickChangeResult(data[1], data[2]);

								break;

							case ClientProtocol.MYINFO_WITHDRAW:
								myInfomationController.alertWithdrawResult(data[1]);
								break;

							case ClientProtocol.MYINFO_HISTORY: // ������â�� �� ������������
								if (data[1].equals("true")) { // db���� 1���̻��� ������ ������
									myInfomationController.getRankHistory(data[2]);
								} else { // �����Ͱ� ��������
									myInfomationController.getRankHistory();
								}
								break;

							case ClientProtocol.MYINFO_PWCHANGE:
								myInfomationController.alertPasswordChangeResult(data[1]);

								break;

							default:
								break;
							}

						} catch (Exception e) {
							alertDisplay(0, "��ſ���", "������ ������ ���������ϴ�");
							System.out.println("[������ ����� �� �����ϴ�]");
							e.printStackTrace();
							if (in != null)
								in.close();
							stopClient();
							return;
						}
					}
				} catch (Exception e) {
					try {
						System.out.println("[������ ����� �� �����ϴ�]");
						e.printStackTrace();
						if (in != null)
							in.close();
						stopClient();
						return;
					} catch (IOException e1) {
						e1.printStackTrace();
					}

				}
			}//end of run
		});
		//thread.setDaemon(true);
		threadPool.submit(thread);
	}

	public void send(String message) {

		try {
			OutputStream out = socket.getOutputStream();
			byte[] buffer = message.getBytes("UTF-8");
			out.write(buffer);
			out.flush();
		} catch (Exception e) {
			System.out.println("[�޼��� �۽� ����]");
			e.printStackTrace();
			stopClient();
		}

	}

	public static void alertDisplay(int type, String title, String headerText, String contentText) {
		Alert alert = null;
		switch (type) {
		case 0:
			alert = new Alert(AlertType.ERROR);
			break;
		case 1:
			alert = new Alert(AlertType.INFORMATION);
			break;
		case 2:
			alert = new Alert(AlertType.WARNING);
			break;
		case 3:
			alert = new Alert(AlertType.CONFIRMATION);
			break;
		case 4:
			alert = new Alert(AlertType.NONE);
			break;
		default:
			break;
		}
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);
		alert.setResizable(false);
		alert.showAndWait();

	}

	// contentText���� alertDisplay �����ε�
	public static void alertDisplay(int type, String title, String headerText) {
		Alert alert = null;
		switch (type) {
		case 0:
			alert = new Alert(AlertType.ERROR);
			break;
		case 1:
			alert = new Alert(AlertType.INFORMATION);
			break;
		case 2:
			alert = new Alert(AlertType.WARNING);
			break;
		case 3:
			alert = new Alert(AlertType.CONFIRMATION);
			break;
		case 4:
			alert = new Alert(AlertType.NONE);
			break;
		default:
			break;
		}
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setResizable(false);
		alert.showAndWait();

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPw() {
		return pw;
	}

	public void setPw(String pw) {
		this.pw = pw;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getHostData() {
		return hostData;
	}

	public void setHostData(String hostData) {
		this.hostData = hostData;
	}

	public MainController getMainController() {
		return mainController;
	}

	public void setMainController(MainController mainController) {
		this.mainController = mainController;
	}

	public LoginController getLoginController() {
		return loginController;
	}

	public void setLoginController(LoginController loginController) {
		this.loginController = loginController;
	}

	public CreateRoomController getCreateRoomController() {
		return createRoomController;
	}

	public void setCreateRoomController(CreateRoomController createRoomController) {
		this.createRoomController = createRoomController;
	}

	public JoinController getJoinController() {
		return joinController;
	}

	public void setJoinController(JoinController joinController) {
		this.joinController = joinController;
	}

	public GameRoomController getGameRoomController() {
		return gameRoomController;
	}

	public void setGameRoomController(GameRoomController gameRoomController) {
		this.gameRoomController = gameRoomController;
	}

	public MyInfomationController getMyInfomationController() {
		return myInfomationController;
	}

	public void setMyInfomationController(MyInfomationController myInfomationController) {
		this.myInfomationController = myInfomationController;
	}

	public Socket getSocket() {
		return socket;
	}

}
