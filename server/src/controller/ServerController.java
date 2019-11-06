package controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.GameRoomVO;
import model.JoinVO;
import model.RankVO;

public class ServerController implements Initializable {
	@FXML
	Button btnLunch;
	@FXML
	Button btnRef;
	@FXML
	Button btnAdd;
	@FXML
	TextArea textArea;
	@FXML
	TextField txtFieldClients;
	@FXML
	Button btnClose;

	// 여러개의 스레드를 효율적으로 관리하기 위한 일종의 라이브러리
	public static ExecutorService threadPool;
	// 접속중인 모든 클라이언트 리스트
	public static ArrayList<Client> clients = new ArrayList<Client>();
	private static HashSet<Room> roomMannager = new HashSet<Room>();

	ArrayList<String> fileName = null;

	ServerSocket serverSocket;
	Socket socket;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		String IP = "127.0.0.1";
		int port = 9000;
		btnRef.setDisable(true);
		btnLunch.setOnAction(event -> {
			if (btnLunch.getText().equals("서버 시작하기")) {
				startServer(IP, port);
				String message = String.format("[ 서버 시작 ]\n", IP, port);
				platformRun(message);
				btnLunch.setText("서버 종료하기");
				btnRef.setDisable(false);

			} else {
				stopServer();
				String message = String.format("[ 서버 종료 ]\n", IP, port);
				platformRun(message);
				btnLunch.setText("서버 시작하기");
				btnRef.setDisable(true);

			}

		});
		btnClose.setOnAction(e -> {
			stopServer();
			Platform.exit();
		});

		btnRef.setOnAction(event -> {
			catchClientHearbeat();
			Platform.runLater(() -> {
				txtFieldClients.setText(String.valueOf(clients.size()));
			});
		});
		// 이미지 추가하기
		btnAdd.setOnAction(event -> {
			try {
				Stage stage = new Stage();
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/addImg.fxml"));
				Parent main;
				main = (Parent) loader.load();
				Scene scene = new Scene(main);
				AddImgController controller = loader.getController();
				controller.setPrimaryStage(stage);
				stage.setScene(scene);
				stage.initModality(Modality.WINDOW_MODAL);
				stage.setResizable(false);
				stage.show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

	}// end of init

	// 서버를 구동시켜서 클라이언트의 연결을 기다리는 메소드
	public void startServer(String IP, int port) {
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP, port));
		} catch (Exception e) {
			// 서버가 열려있으면 닫음
			if (!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}

		// 클라이언트가 접속할때까지 기다린다
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						socket = serverSocket.accept();
						String viewData = "[ 클라이언트 접속 ]" + socket.getRemoteSocketAddress() + ": "
								+ Thread.currentThread().getName();
						platformRun(viewData + "\n");
						clients.add(new Client(socket));
						platformRun("[ 연결된 클라이언트: " + clients.size() + " ]\n");
						Platform.runLater(() -> txtFieldClients.setText(String.valueOf(clients.size())));

					} catch (Exception e) {
						if (!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}

			}
		};
		// 스레드풀 초기화
		threadPool = Executors.newCachedThreadPool();
		// 접속을 기다리는 현재 스레드를 넣어줌
		threadPool.submit(thread);

	}

	// 서버작동을 중지시키는 메소드
	public void stopServer() {
		try {
			// 작동중인 모든 소켓 닫기
			Iterator<Client> iter = clients.iterator();
			while (iter.hasNext()) {
				Client client = iter.next();
				client.socket.close();
				iter.remove();
			}
			// 서버소켓 닫기
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			// 스레드풀 종료
			if (threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}
			// 모든 스레드가 종료되었는지 확인
			while (!threadPool.isTerminated()) {
			}
			System.out.println("모든 Thread가 종료되었습니다.");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 텍스트 에어리어에 문자열 띄우기
	public void platformRun(String data) {
		Platform.runLater(() -> textArea.appendText(data));
	}

	// 클라이언트가 서버에 연결중인지 지속적 확인
	public void catchClientHearbeat() {
		try {
			for (Client client : clients) {
				client.setConnected(false);
				client.send(ServerProtocol.CONNECT_CHECK + "|");
				Thread.sleep(50);
				if (client.isConnected == false) {
					socket.close();
					clients.remove(client);
				}
			}

		} catch (InterruptedException | IOException e) {
			platformRun("[ 소켓 연결 종료 ]" + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName());
		}
	}

	// inner class start
	public class Client {

		public Socket socket = null;
		ObjectOutputStream oos;
		public boolean isConnected = true;
		public String id;
		public String nickname;

		public Client(Socket socket) {
			this.socket = socket;
			receive();
		}

		// 클라이언트로부터 메세지를 받는 메소드
		public void receive() {

			Runnable thread = new Runnable() {
				InputStream in = null;

				@Override
				public void run() {
					String viewData = null;
					try {
						while (true) {
							in = socket.getInputStream();
							byte[] buffer = new byte[1024];
							int length = in.read(buffer);
							// 메세지를 읽을때 오류가 발생한 경우 예외처리
							if (length == -1) {
								throw new IOException();
							}

							String message = new String(buffer, 0, length, "UTF-8");
							// 서버창에 메세지 출력
							viewData = "[ Recieve ]" + message + socket.getRemoteSocketAddress() + ": "
									+ Thread.currentThread().getName();
							platformRun(viewData + "\n");
							// 클라이언트로부터 받는 메세지를 "|" 단위로 잘라서 처리
							String[] data = message.split("\\|");

							JoinDAO joinDAO = new JoinDAO();
							GameRoomDAO grDAO = new GameRoomDAO();
							RankDAO rDAO = new RankDAO();
							RankHistoryDAO rhDAO = new RankHistoryDAO();

							switch (Integer.parseInt(data[0])) {

							case ServerProtocol.CONNECT_CHECK:// 클라이언트 연결체크
								isConnected = true;
								break;

							case ServerProtocol.CHATTING: // 메인채팅
								// 메세지를 받으면 다른 클라이언트들에게 그대로 전송
								for (Client client : clients) {
									client.send(message);
								}
								break;

							case ServerProtocol.JOIN_IDDUPLICATE:// 아이디 중복체크
								boolean duplicate = joinDAO.getIdCheckDuplicate(data[1]);
								if (duplicate == true) {
									send(ServerProtocol.JOIN_IDDUPLICATE + "|" + "true");
								} else {
									send(ServerProtocol.JOIN_IDDUPLICATE + "|" + "false");
								}
								break;

							case ServerProtocol.JOIN_NICKDUPLICATE: // 닉네임 중복체크
								boolean duplicateNickname = joinDAO.getNickCheckDuplicate(data[1]);
								if (duplicateNickname == true) {
									send(ServerProtocol.JOIN_NICKDUPLICATE + "|" + "true");
								} else {
									send(ServerProtocol.JOIN_NICKDUPLICATE + "|" + "false");
								}
								break;

							case ServerProtocol.JOIN_RECIEVE: // 회원가입받기
								int count = joinDAO.insertNewMemberIntoDB(data[1], data[2], data[3]); // db에 등록
								if (count != 0) {
									send(ServerProtocol.JOIN_ACCEPT + "|" + "true"); // 등록성공 메세지 보내기
									viewData = "[ 회원가입 승인 ]" + data[1] + "|" + socket.getRemoteSocketAddress() + "\n";
									platformRun(viewData);

								} else {
									send(ServerProtocol.JOIN_ACCEPT + "|" + "false"); // 등록실패 메세지 보내기
									viewData = "[ 회원가입 오류발생 ]" + data[1] + "|" + socket.getRemoteSocketAddress() + "\n";
									platformRun(viewData);

								}
								break;

							case ServerProtocol.LOGIN_RECIEVE: // 클라이언트의 로그인 요청
								boolean checkIdPw = joinDAO.getCheckIdPw(data[1], data[2]);
								boolean duplicateLogin = false; // 중복로그인 체크

								Iterator<Client> iter = clients.iterator();
								while (iter.hasNext()) {
									Client client = iter.next();
									// clients안에 있는id들중에 로그인하려는 사람의 id가 있으면 중복된로그인으로 처리
									if (client.getId() != null && client.getId().equals(data[1])) {
										duplicateLogin = true;
									}
								}

								if (checkIdPw == true && duplicateLogin == false) {
									viewData = "[ 로그인 승인 ]" + " ID: " + data[1];
									platformRun(viewData);
									send(ServerProtocol.LOGIN_ACCEPT + "|" + "true" + "|" + data[1]); // 로그인 승인메세지 보내기
									id = data[1];
									nickname = joinDAO.getNickname(data[1]);
								} else {
									send(ServerProtocol.LOGIN_ACCEPT + "|" + "false" + "|" + data[1]);
								}
								break;

							case ServerProtocol.WELCOME: // 클라이언트 입장
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e1) {
									e1.printStackTrace();
								}
								for (Client client : clients) {
									client.send(ServerProtocol.WELCOME + "|" + "[" + nickname + " 님이 입장하셨습니다]");
								}
								break;

							case ServerProtocol.USERINFO_JOIN:
								JoinVO jVO = joinDAO.getJoinUserData(data[1]);
								if (jVO != null) {
									send(ServerProtocol.USERINFO_JOIN + "|" + "true" + "|" + jVO.getId() + "|"
											+ jVO.getPw() + "|" + jVO.getNickname());
								} else {
									send(ServerProtocol.USERINFO_JOIN + "|" + "false");
								}

								break;

							case ServerProtocol.MAINPAGE_REFRESH: // 게임방리스트 새로고침
								//게임방목록 업데이트
								ArrayList<GameRoomVO> grvoList = grDAO.getGameRoomList();
								// 리스트를 하나의 스트링으로 쭉 엮기
								String listToString = "";
								if (grvoList.size() > 0) {// 방이 하나라도 생성된 경우
									for (GameRoomVO grVO : grvoList) { // grVO객체 안에 rp,방제,방장,게임중여부,공개여부 들어감
										listToString = listToString + grVO.toString();
									}
									send(ServerProtocol.MAINPAGE_REFRESH + "|" + "true" + "|" + listToString);
								} else {
									send(ServerProtocol.MAINPAGE_REFRESH + "|" + "false");
								}
								
								//랭크 리스트 업데이트
								ArrayList<RankVO> rvoList = rDAO.getRankList();
								// 리스트를 하나의 스트링으로 쭉 엮기
								String rVOtoString1 = "";
								if (rvoList.size() > 0) {
									for (RankVO rVO : rvoList) {
										rVOtoString1 = rVOtoString1 + rVO.toString();
									}
									send(ServerProtocol.RANK_LISTSET + "|" + "true" + "|" + rVOtoString1);

								} else {
									send(ServerProtocol.RANK_LISTSET + "|" + "false");
								}
								
								//현재 접속자 목록
								String players = "";
								Iterator<Client> iter2 = clients.iterator();
								while (iter2.hasNext()) {
									Client client = iter2.next();
									players = players+client.getNickname()+",";
								}
								send(ServerProtocol.MAIN_NOWPLAYERS + "|" + players);
								
								break;


							case ServerProtocol.RANK_HISTORY: // 랭크전적가져오기
								ArrayList<RankVO> rvoList2 = rhDAO.getGameRoomList(data[1]);

								// 리스트를 하나의 스트링으로 쭉 엮기
								String rVOtoString2 = "";
								if (rvoList2.size() > 0) {
									for (RankVO rVO : rvoList2) {
										rVOtoString2 = rVOtoString2 + rVO.toString();
									}
									send(ServerProtocol.RANK_HISTORY + "|" + "true" + "|" + rVOtoString2);
								} else {
									send(ServerProtocol.RANK_HISTORY + "|" + "false");
								}
								break;

							case ServerProtocol.CREATEROOM: // 게임방 만들기 요청
								boolean flag = false;
								int count1 = grDAO.insertIntoGameRoomData(data[1], data[2], data[3], data[4]);
								if (count1 != 0) { // db에 방등록 성공시
									for (Client client : clients) {
										if (client.getId().equals(data[1])) {// 접속자중에서 방만든 아이디를 찾아서
											Room room = new Room(client.getNickname(), client);// 그사람을 생성자 매개변수로 룸객체를
																								// 만들고
											roomMannager.add(room); // 그룸을 룸매니저에 추가
											flag = true;
										}
									}
									if (flag) {
										send(ServerProtocol.CREATEROOM + "|" + "true"); // 등록성공 메세지 보내기
										viewData = "[ 방 생성 완료 ]" + " ID: " + data[1] + ", roomName: " + data[2]
												+ ", roomRock: " + data[3] + ", roomPw: " + data[4] + "\n";
										platformRun(viewData);
									}
								} else {
									send(ServerProtocol.CREATEROOM + "|" + "false"); // 등록실패 메세지 보내기
									viewData = "[ 방 생성 실패 ]" + " ID: " + data[1] + ", roomName: " + data[2]
											+ ", roomRock: " + data[3] + ", roomPw: " + data[4] + "\n";
									platformRun(viewData);
								}
								break;

							case ServerProtocol.GUEST_ENTER: // 게스트가 게임방에 들어간다는 요청을 보냄 / 비밀방여부, 방장닉네임, 비번받음
								String roomRock = data[1];
								int result = 0; // 1로 바뀔경우 입장승인, 0이면 입장거절
								if (roomRock.equals("true")) {// 비밀방일때 입장가능여부 조회
									result = grDAO.guestEnterPrivateRoom(data[2], data[3], data[4]); // 호스트닉, 게스트닉, 비밀번호
								} else {// 공개방일때 입장가능여부 조회(풀방일경우 0)
									result = grDAO.guestEnterPublicRoom(data[2], data[3]); // 호스트닉, 게스트닉
								}
								if (result == 1) { // 선택된 방이 대기중이고 비번도 맞을때
									Client guest = null;
									for (Client client : clients) {
										if (client.getNickname().equals(data[3])) {
											guest = client; // 접속자중에서 게스트 클라이언트를 찾음
										}
									}
									for (Room room : roomMannager) {
										if (room.getHostNick().equals(data[2])) {// 호스트 닉의 룸객체를 찾아서
											room.setGuestNick(guest.nickname); // 게스트 닉네임을 등록하고
											room.addPlayer(guest); // 게스트를 호스트의 방에 넣음
										}
									}
									viewData = "[ 게스트 입장 ]" + " 입장한 방의 호스트: " + data[2] + "\n";
									platformRun(viewData);
									// 입장허가 메세지 보내기
									send(ServerProtocol.GUEST_ENTER + "|" + "true");
								} else {
									send(ServerProtocol.GUEST_ENTER + "|" + "false");
								}
								break;

							case ServerProtocol.GAMEROOM_FULL: // 게스트가 입장해서 풀방일때
								// 해당 룸에 있는 모든사람에게 풀방임을 알리고 레디하라는 메세지 보내기
								for (Room room : roomMannager) {
									if (room.getHostNick().equals(data[1])) {// 호스트 닉의 룸객체를 찾아서, 호스트닉, 게스트닉을 보냄
										room.broadcast(ServerProtocol.GAMEROOM_FULL + "|" + room.getHostNick() + "|"
												+ room.guestNick);
									}
								}
								break;

							case ServerProtocol.GAMEROOM_READY: // 플레이어가 레디신호를 보내올때
								// 호스트 닉네임으로 방정보 찾기
								for (Room room : roomMannager) {
									if (room.getHostNick().equals(data[1])) {
										// 레디한사람이 본인 1명일때
										if (room.getReadyCount() < 1) {
											room.addReadyCount();
										} else { // 두명 다 레디를 했을때
											// 게임 이미지 파일 전송
											GameImageDAO giDAO = new GameImageDAO();
											ArrayList<GameImage> giList = giDAO.getGameImageArray();
											new FileSender(giList).start();

											String MergeCoordinate = "";
											for (GameImage gi : giList) {
												MergeCoordinate = MergeCoordinate + gi.getCoordinates() + "^";
											}
											// 정답스트링을 보내고 게임시작하게하기
											room.broadcast(ServerProtocol.GAMEROOM_READY + "|" + MergeCoordinate);
										}
									}

								}
								break;

							case ServerProtocol.GAME_IMAGE_DOWN:
								fileName = new ArrayList<String>();

								try {

								} catch (Exception e) {
									System.out.println("FileSender실행오류");
									e.printStackTrace();
								}
								break;

							case ServerProtocol.GAME_CD_FOUND: // 사용자가 정답좌표를 찾았을때
								for (Room room : roomMannager) {
									if (room.getHostNick().equals(data[1])) {
										// 찾은사람의 닉네임, 찾은x좌표, 찾은y좌표를 전송
										room.broadcast(ServerProtocol.GAME_CD_FOUND + "|" + data[2] + "|" + data[3]
												+ "|" + data[4]);
									}
								}
								break;

							case ServerProtocol.GAME_CD_NOT_FOUND: // 사용자가 틀렸을때
								for (Room room : roomMannager) {
									if (room.getHostNick().equals(data[1])) {
										// 틀린사람의 닉네임 전송
										room.broadcast(ServerProtocol.GAME_CD_NOT_FOUND + "|" + data[2]);
									}
								}
								break;

							case ServerProtocol.GAME_HOST_SURRENDER: // 호스트가 항복선언
								registerGameResultInDB(data[1], data[2], data[3], false,
										ServerProtocol.GAME_HOST_SURRENDER);
								break;

							case ServerProtocol.GAME_GUEST_SURRENDER: // 게스트가 항복선언
								registerGameResultInDB(data[1], data[2], data[3], true,
										ServerProtocol.GAME_GUEST_SURRENDER);
								break;

							case ServerProtocol.GAME_HOST_LOSTHEART: // 호스트 하트수부족
								registerGameResultInDB(data[1], data[2], data[3], false,
										ServerProtocol.GAME_HOST_LOSTHEART);
								break;

							case ServerProtocol.GAME_GUEST_LOSTHEART: // 게스트 하트수부족
								registerGameResultInDB(data[1], data[2], data[3], true,
										ServerProtocol.GAME_GUEST_LOSTHEART);
								break;

							case ServerProtocol.GAME_END_HOSTWIN: // 호스트가 더 많이 맞춤
								registerGameResultInDB(data[1], data[2], data[3], true,
										ServerProtocol.GAME_END_HOSTWIN);
								break;

							case ServerProtocol.GAME_END_GUESTWIN: // 게스트가 더많이 맞춤
								registerGameResultInDB(data[1], data[2], data[3], false,
										ServerProtocol.GAME_END_GUESTWIN);
								break;

							case ServerProtocol.GAME_HOST_EXIT: // 호스트나감
								grDAO.deleteFromGameRoomTbl(data[1]);
								for (Room room : roomMannager) {
									if (room.getHostNick().equals(data[1])) {
										room.broadcast(ServerProtocol.GAME_HOST_EXIT + "|");
										roomMannager.remove(room); // 해당 룸객체를 룸매니저에서 삭제
									}
								}
								break;

							case ServerProtocol.GAME_GUEST_EXIT: // 게스트나감
								for (Room room : roomMannager) {
									if (room.getHostNick().equals(data[1])) {// 호스트이름으로 룸객체를찾아서
										// clients.add(room.getGuest()); //클라이언트에 게스트객체 추가
										// 게스트가 나갔다고 알림 (호스트는 알림받고 방에 머물고, 게스트는 그냥 로비로)
										room.broadcast(ServerProtocol.GAME_GUEST_EXIT + "|");
									}
								}
								grDAO.guestExitGameRoom(data[1]); // 방상태를 게임중에서 대기중으로 바꾸고 게스트이름을 지움
								break;

							case ServerProtocol.MYINFO_NICKDUPLICATE: // 닉네임 중복체크
								duplicateNickname = joinDAO.getNickCheckDuplicate(data[1]);
								if (duplicateNickname == true) {
									send(ServerProtocol.MYINFO_NICKDUPLICATE + "|" + "true");
								} else {
									send(ServerProtocol.MYINFO_NICKDUPLICATE + "|" + "false");
								}
								break;

							case ServerProtocol.MYINFO_NICK_CHANGE: // 닉네임 변경
								int count2 = 0;
								count2 = joinDAO.nickNameChage(data[1], data[2]);
								if (count2 == 1) {
									send(ServerProtocol.MYINFO_NICK_CHANGE + "|" + "true" + "|" + data[1]);
								} else {
									send(ServerProtocol.MYINFO_NICK_CHANGE + "|" + "false" + "|" + data[1]);
								}
								break;

							case ServerProtocol.MYINFO_WITHDRAW: // 회원탈퇴
								int count3 = joinDAO.memberWithdraw(data[1]);
								if (count3 == 1) {
									send(ServerProtocol.MYINFO_WITHDRAW + "|" + "true");
								} else {
									send(ServerProtocol.MYINFO_WITHDRAW + "|" + "false");
								}
								break;

							case ServerProtocol.MYINFO_HISTORY: // 랭크전적가져오기
								ArrayList<RankVO> rvoList3 = rhDAO.getGameRoomList(data[1]);

								// 리스트를 하나의 스트링으로 쭉 엮기
								String rVOtoString3 = "";
								if (rvoList3.size() > 0) {
									for (RankVO rVO : rvoList3) {
										rVOtoString3 = rVOtoString3 + rVO.toString();
									}
									send(ServerProtocol.MYINFO_HISTORY + "|" + "true" + "|" + rVOtoString3);
								} else {
									send(ServerProtocol.MYINFO_HISTORY + "|" + "false");
								}
								break;

							case ServerProtocol.MYINFO_PWCHANGE: // 비번변경하기
								int count4 = joinDAO.changeUserPassword(data[1], data[2]);
								if (count4 == 1) {
									send(ServerProtocol.MYINFO_PWCHANGE + "|" + "true");
								} else {
									send(ServerProtocol.MYINFO_PWCHANGE + "|" + "false");
								}

								break;

							default:
								break;
							}

						} // end of while

					} catch (IOException e) {
						try {
							e.printStackTrace();
							viewData = "[ Recieve Error ]" + socket.getRemoteSocketAddress() + ", "
									+ Thread.currentThread().getName() + "\n";
							platformRun(viewData);
							clients.remove(this);
							in.close();
							socket.close();

						} catch (Exception e2) {
							e2.printStackTrace();
						}
					} // end of try-catch

				}// end of run
			};
			// 스레드를 스레드풀에 등록(안정적으로 관리하기위해)
			threadPool.submit(thread);
		}// end of receive

		// 클라이언트로 메세지를 보내는 메소드
		public void send(String message) {
			try {
				OutputStream os = socket.getOutputStream();
				byte[] buffer = message.getBytes("UTF-8");
				os.write(buffer, 0, buffer.length);
				os.flush();
			} catch (IOException e) {
				try {
					String viewData = "[ Send Error ]" + socket.getRemoteSocketAddress() + ": "
							+ Thread.currentThread().getName() + "\n";
					platformRun(viewData);
					// 오류가 발생해서 클라이언트의 접속이 끊기면 서버안에서 해당 클라이언트의 정보도 지움
					clients.remove(this);
					socket.close();
					e.printStackTrace();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}

		}// end of send

		public void sendImage(File file) {
			OutputStream os = null;
			FileInputStream fis = null;
			BufferedInputStream bis = null;
			try {
				os = socket.getOutputStream();
				fis = new FileInputStream(file);
				bis = new BufferedInputStream(fis);
				int length = 0;
				byte[] buffer = new byte[1024 * 12];

				while ((length = bis.read(buffer)) > 0) {
					System.out.println("전송중..");
					os.write(buffer, 0, length);
				}
				os.flush();
				System.out.println("전송완료");
				// socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (bis != null)
						bis.close();
					if (fis != null)
						fis.close();
					if (os != null)
						os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void registerGameResultInDB(String hostNick, String hostScore, String guestScore, boolean hostWin,
				int protocol) {
			JoinDAO joinDAO = new JoinDAO();
			GameRoomDAO grDAO = new GameRoomDAO();
			// 게임룸 테이블에서 호스트닉, 게스트닉 얻기
			String[] nickList = grDAO.getAllNickname(hostNick);
			// 닉넴으로 아이디얻기
			String hostId = joinDAO.getUserId(nickList[0]);
			String guestId = joinDAO.getUserId(nickList[1]);
			// 게임결과로 얻은 rp 구하기
			int[] newRp = grDAO.gameRpUpdate(hostId, guestId, hostWin, hostScore, guestScore);
			// rp, 승수, 패수, 전체게임수 업데이트
			grDAO.gameScoreUpdate(newRp[2], newRp[3], hostId, guestId, hostWin);
			// 게임코인 업데이트
			grDAO.gameCoinUpdate(hostId, guestId, hostWin);
			// 게임테이블에서 해당 룸 데이터 지우기
			grDAO.deleteFromGameRoomTbl(hostNick);
			// 게임결과 알리기
			for (Room room : roomMannager) {
				if (room.getHostNick().equals(hostNick)) {
					room.broadcast(protocol + "|" + newRp[0] + "|" + newRp[1]);
					roomMannager.remove(room);
					System.out.println("룸매니저 사이즈" + roomMannager.size());
				}
			}
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getNickname() {
			return nickname;
		}

		public void setNickname(String nickname) {
			this.nickname = nickname;
		}

		public Socket getSocket() {
			return socket;
		}

		public boolean isConnected() {
			return isConnected;
		}

		public void setConnected(boolean isConnected) {
			this.isConnected = isConnected;
		}

	}// end of inner Client class

	class Room {
		private String hostNick;
		private String guestNick;
		private int readyCount = 0; // 레디한 인원수
		private Vector players = new Vector(); // 플레이어리스트 (호스트,게스트)
		private GameImage sheredImg;

		Room(String hostNick, Client client) {
			this.hostNick = hostNick;
			this.players.add(client);
		}

		public void broadcast(String data) { // 게임중인 사람에게 알림
			for (int i = 0; i < players.size(); i++) {
				Client player = (Client) players.elementAt(i);
				player.send(data);
			}
		}

		public GameImage getsheredImg() {
			return sheredImg;
		}

		public String getHostNick() {
			return hostNick;
		}

		public void setHostNick(String hostNick) {
			this.hostNick = hostNick;
		}

		public String getGuestNick() {
			return guestNick;
		}

		public void setGuestNick(String guestNick) {
			this.guestNick = guestNick;
		}

		private void addPlayer(Client client) {
			this.players.add(client);
		}

		public int getReadyCount() {
			return readyCount;
		}

		public int addReadyCount() {
			return readyCount++;
		}

		@Override
		public String toString() {
			return "Room [hostNick=" + hostNick + ", guestNick=" + guestNick + ", readyCount=" + readyCount + "]";
		}

	}

}
