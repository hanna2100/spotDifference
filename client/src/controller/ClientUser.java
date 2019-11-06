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
	// 컨트롤러 연동을 위한 멤버변수
	private MainController mainController;
	private LoginController loginController;
	private JoinController joinController;
	private CreateRoomController createRoomController;
	private GameRoomController gameRoomController;
	private MyInfomationController myInfomationController;

	Socket socket;
	public static ExecutorService threadPool;
	Vector<String> clients = new Vector<String>();

	// 테스트를 위한 아이피, 포트
	String IP = "127.0.0.1";
	int port = 9000;

	String fileName = null; // 이미지 파일 전송시 파일이름설정 (게임 컨트롤러에도 같은 이름을 쓰기때문에 바뀌면 안됨)

	private String id; // 아이디
	private String pw; // 비번
	private String nickname; // 닉네임
	private String hostData; // 게임방 들어갔을때 방장닉(방장닉이 테이블의 기본키라서 여기 넣는게 편할거같음)
	
	public void startClient(String IP, int port) {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					socket = new Socket(IP, port);
					receive();
				} catch (Exception e) {
					if (socket != null && !socket.isClosed()) {
						System.out.println("[서버 접속 실패]");
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
		// 소켓이나 스레드풀이 열려있으면 닫기

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
			// 메세지받는 스트림
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
							
							case ClientProtocol.CONNECT_CHECK:// 연결여부 생존신고
								send(ClientProtocol.CONNECT_CHECK + "");
								break;

							case ClientProtocol.JOIN_IDDUPLICATE: // 회원가입/중복아이디일때
								joinController.alertIdDuplicate(data[1]);
								break;

							case ClientProtocol.JOIN_NICKDUPLICATE:// 회원가입/중복닉네임일때
								joinController.alertNickDuplicate(data[1]);
								break;

							case ClientProtocol.JOIN_ACCEPT:// 회원가입 승인or거절
								joinController.alertJoinResult(data[1]);
								break;

							case ClientProtocol.LOGIN_ACCEPT: // 로그인 승인or거절
								loginController.alertLoginResult(data[1], data[2]);
								break;

							case ClientProtocol.USERINFO_JOIN: // 가입시 얻은 유저인포
								boolean isInfoExisted = Boolean.parseBoolean(data[1]);
								if (isInfoExisted == true) {
									mainController.setUserJoinInfo(data[2], data[3], data[4]);

								} else {
									mainController.alertNotFoundUserInfo();
								}
								break;

							case ClientProtocol.CHATTING: // 메인채팅
								mainController.showMainChatting(data);
								break;
								
							case ClientProtocol.MAIN_NOWPLAYERS: // 메인채팅
								mainController.showPlayersList(data[1]);
								break;	
								
							case ClientProtocol.WELCOME: // 메인채팅-웰컴메세지
								mainController.showMainChatting(data[1]);
								break;

							case ClientProtocol.CREATEROOM: // 방만들기 요청시 성공여부
								createRoomController.closeStage(); // 방만들기창 닫기
								mainController.alertRoomCreateResult(data[1]); // DB 방생성 결과를 알려줌(true일경우 게임룸으로 들어감)
								break;

							case ClientProtocol.MAINPAGE_REFRESH: // DB gameRoom테이블 데이터
								if (data[1].equals("true")) { // db에서 1개이상의 데이터 있을때
									mainController.createNewRoomList(data[2]);
								} else { // 데이터가 없었을때
									mainController.createNewRoomList();
								}

								break;

							case ClientProtocol.RANK_LISTSET: // DB 랭테이블 데이터
								if (data[1].equals("true")) { // db에서 1개이상의 데이터 있을때
									mainController.createNewRankList(data[2]);
								} else { // 데이터가 없었을때
									mainController.createNewRankList();
								}

								break;

							case ClientProtocol.RANK_HISTORY: // 랭 히스토리 가져오기
								if (data[1].equals("true")) { // db에서 1개이상의 데이터 있을때
									mainController.getRankHistory(data[2]);
								} else { // 데이터가 없었을때
									mainController.getRankHistory();
								}

								break;

							case ClientProtocol.GUEST_ENTER:// 게임 입장하기 접근여부
								mainController.alertGuestEnterResult(data[1]);
								break;

							case ClientProtocol.GAMEROOM_FULL: // 풀방일때 알림
								gameRoomController.alertFullRoom(data[1], data[2]);// 호스트닉, 게스트닉 전달받음
								break;

							case ClientProtocol.GAMEROOM_READY: // 모두 레디완료일때
								gameRoomController.alertGameStart();
								try {
									new FileRecv(IP, fileName).start(); // 쓰레드 실행.
								} catch (Exception e) {
									System.out.println("FileRecv 실행오류");
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

							case ClientProtocol.MYINFO_HISTORY: // 내정보창에 내 전적가져오기
								if (data[1].equals("true")) { // db에서 1개이상의 데이터 있을때
									myInfomationController.getRankHistory(data[2]);
								} else { // 데이터가 없었을때
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
							alertDisplay(0, "통신오류", "서버와 연결이 끊어졌습니다");
							System.out.println("[서버와 통신할 수 없습니다]");
							e.printStackTrace();
							if (in != null)
								in.close();
							stopClient();
							return;
						}
					}
				} catch (Exception e) {
					try {
						System.out.println("[서버와 통신할 수 없습니다]");
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
			System.out.println("[메세지 송신 실패]");
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

	// contentText없는 alertDisplay 오버로딩
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
