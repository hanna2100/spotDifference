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

	// �������� �����带 ȿ�������� �����ϱ� ���� ������ ���̺귯��
	public static ExecutorService threadPool;
	// �������� ��� Ŭ���̾�Ʈ ����Ʈ
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
			if (btnLunch.getText().equals("���� �����ϱ�")) {
				startServer(IP, port);
				String message = String.format("[ ���� ���� ]\n", IP, port);
				platformRun(message);
				btnLunch.setText("���� �����ϱ�");
				btnRef.setDisable(false);

			} else {
				stopServer();
				String message = String.format("[ ���� ���� ]\n", IP, port);
				platformRun(message);
				btnLunch.setText("���� �����ϱ�");
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
		// �̹��� �߰��ϱ�
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

	// ������ �������Ѽ� Ŭ���̾�Ʈ�� ������ ��ٸ��� �޼ҵ�
	public void startServer(String IP, int port) {
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP, port));
		} catch (Exception e) {
			// ������ ���������� ����
			if (!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}

		// Ŭ���̾�Ʈ�� �����Ҷ����� ��ٸ���
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						socket = serverSocket.accept();
						String viewData = "[ Ŭ���̾�Ʈ ���� ]" + socket.getRemoteSocketAddress() + ": "
								+ Thread.currentThread().getName();
						platformRun(viewData + "\n");
						clients.add(new Client(socket));
						platformRun("[ ����� Ŭ���̾�Ʈ: " + clients.size() + " ]\n");
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
		// ������Ǯ �ʱ�ȭ
		threadPool = Executors.newCachedThreadPool();
		// ������ ��ٸ��� ���� �����带 �־���
		threadPool.submit(thread);

	}

	// �����۵��� ������Ű�� �޼ҵ�
	public void stopServer() {
		try {
			// �۵����� ��� ���� �ݱ�
			Iterator<Client> iter = clients.iterator();
			while (iter.hasNext()) {
				Client client = iter.next();
				client.socket.close();
				iter.remove();
			}
			// �������� �ݱ�
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			// ������Ǯ ����
			if (threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}
			// ��� �����尡 ����Ǿ����� Ȯ��
			while (!threadPool.isTerminated()) {
			}
			System.out.println("��� Thread�� ����Ǿ����ϴ�.");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// �ؽ�Ʈ ���� ���ڿ� ����
	public void platformRun(String data) {
		Platform.runLater(() -> textArea.appendText(data));
	}

	// Ŭ���̾�Ʈ�� ������ ���������� ������ Ȯ��
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
			platformRun("[ ���� ���� ���� ]" + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName());
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

		// Ŭ���̾�Ʈ�κ��� �޼����� �޴� �޼ҵ�
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
							// �޼����� ������ ������ �߻��� ��� ����ó��
							if (length == -1) {
								throw new IOException();
							}

							String message = new String(buffer, 0, length, "UTF-8");
							// ����â�� �޼��� ���
							viewData = "[ Recieve ]" + message + socket.getRemoteSocketAddress() + ": "
									+ Thread.currentThread().getName();
							platformRun(viewData + "\n");
							// Ŭ���̾�Ʈ�κ��� �޴� �޼����� "|" ������ �߶� ó��
							String[] data = message.split("\\|");

							JoinDAO joinDAO = new JoinDAO();
							GameRoomDAO grDAO = new GameRoomDAO();
							RankDAO rDAO = new RankDAO();
							RankHistoryDAO rhDAO = new RankHistoryDAO();

							switch (Integer.parseInt(data[0])) {

							case ServerProtocol.CONNECT_CHECK:// Ŭ���̾�Ʈ ����üũ
								isConnected = true;
								break;

							case ServerProtocol.CHATTING: // ����ä��
								// �޼����� ������ �ٸ� Ŭ���̾�Ʈ�鿡�� �״�� ����
								for (Client client : clients) {
									client.send(message);
								}
								break;

							case ServerProtocol.JOIN_IDDUPLICATE:// ���̵� �ߺ�üũ
								boolean duplicate = joinDAO.getIdCheckDuplicate(data[1]);
								if (duplicate == true) {
									send(ServerProtocol.JOIN_IDDUPLICATE + "|" + "true");
								} else {
									send(ServerProtocol.JOIN_IDDUPLICATE + "|" + "false");
								}
								break;

							case ServerProtocol.JOIN_NICKDUPLICATE: // �г��� �ߺ�üũ
								boolean duplicateNickname = joinDAO.getNickCheckDuplicate(data[1]);
								if (duplicateNickname == true) {
									send(ServerProtocol.JOIN_NICKDUPLICATE + "|" + "true");
								} else {
									send(ServerProtocol.JOIN_NICKDUPLICATE + "|" + "false");
								}
								break;

							case ServerProtocol.JOIN_RECIEVE: // ȸ�����Թޱ�
								int count = joinDAO.insertNewMemberIntoDB(data[1], data[2], data[3]); // db�� ���
								if (count != 0) {
									send(ServerProtocol.JOIN_ACCEPT + "|" + "true"); // ��ϼ��� �޼��� ������
									viewData = "[ ȸ������ ���� ]" + data[1] + "|" + socket.getRemoteSocketAddress() + "\n";
									platformRun(viewData);

								} else {
									send(ServerProtocol.JOIN_ACCEPT + "|" + "false"); // ��Ͻ��� �޼��� ������
									viewData = "[ ȸ������ �����߻� ]" + data[1] + "|" + socket.getRemoteSocketAddress() + "\n";
									platformRun(viewData);

								}
								break;

							case ServerProtocol.LOGIN_RECIEVE: // Ŭ���̾�Ʈ�� �α��� ��û
								boolean checkIdPw = joinDAO.getCheckIdPw(data[1], data[2]);
								boolean duplicateLogin = false; // �ߺ��α��� üũ

								Iterator<Client> iter = clients.iterator();
								while (iter.hasNext()) {
									Client client = iter.next();
									// clients�ȿ� �ִ�id���߿� �α����Ϸ��� ����� id�� ������ �ߺ��ȷα������� ó��
									if (client.getId() != null && client.getId().equals(data[1])) {
										duplicateLogin = true;
									}
								}

								if (checkIdPw == true && duplicateLogin == false) {
									viewData = "[ �α��� ���� ]" + " ID: " + data[1];
									platformRun(viewData);
									send(ServerProtocol.LOGIN_ACCEPT + "|" + "true" + "|" + data[1]); // �α��� ���θ޼��� ������
									id = data[1];
									nickname = joinDAO.getNickname(data[1]);
								} else {
									send(ServerProtocol.LOGIN_ACCEPT + "|" + "false" + "|" + data[1]);
								}
								break;

							case ServerProtocol.WELCOME: // Ŭ���̾�Ʈ ����
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e1) {
									e1.printStackTrace();
								}
								for (Client client : clients) {
									client.send(ServerProtocol.WELCOME + "|" + "[" + nickname + " ���� �����ϼ̽��ϴ�]");
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

							case ServerProtocol.MAINPAGE_REFRESH: // ���ӹ渮��Ʈ ���ΰ�ħ
								//���ӹ��� ������Ʈ
								ArrayList<GameRoomVO> grvoList = grDAO.getGameRoomList();
								// ����Ʈ�� �ϳ��� ��Ʈ������ �� ����
								String listToString = "";
								if (grvoList.size() > 0) {// ���� �ϳ��� ������ ���
									for (GameRoomVO grVO : grvoList) { // grVO��ü �ȿ� rp,����,����,�����߿���,�������� ��
										listToString = listToString + grVO.toString();
									}
									send(ServerProtocol.MAINPAGE_REFRESH + "|" + "true" + "|" + listToString);
								} else {
									send(ServerProtocol.MAINPAGE_REFRESH + "|" + "false");
								}
								
								//��ũ ����Ʈ ������Ʈ
								ArrayList<RankVO> rvoList = rDAO.getRankList();
								// ����Ʈ�� �ϳ��� ��Ʈ������ �� ����
								String rVOtoString1 = "";
								if (rvoList.size() > 0) {
									for (RankVO rVO : rvoList) {
										rVOtoString1 = rVOtoString1 + rVO.toString();
									}
									send(ServerProtocol.RANK_LISTSET + "|" + "true" + "|" + rVOtoString1);

								} else {
									send(ServerProtocol.RANK_LISTSET + "|" + "false");
								}
								
								//���� ������ ���
								String players = "";
								Iterator<Client> iter2 = clients.iterator();
								while (iter2.hasNext()) {
									Client client = iter2.next();
									players = players+client.getNickname()+",";
								}
								send(ServerProtocol.MAIN_NOWPLAYERS + "|" + players);
								
								break;


							case ServerProtocol.RANK_HISTORY: // ��ũ������������
								ArrayList<RankVO> rvoList2 = rhDAO.getGameRoomList(data[1]);

								// ����Ʈ�� �ϳ��� ��Ʈ������ �� ����
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

							case ServerProtocol.CREATEROOM: // ���ӹ� ����� ��û
								boolean flag = false;
								int count1 = grDAO.insertIntoGameRoomData(data[1], data[2], data[3], data[4]);
								if (count1 != 0) { // db�� ���� ������
									for (Client client : clients) {
										if (client.getId().equals(data[1])) {// �������߿��� �游�� ���̵� ã�Ƽ�
											Room room = new Room(client.getNickname(), client);// �׻���� ������ �Ű������� �밴ü��
																								// �����
											roomMannager.add(room); // �׷��� ��Ŵ����� �߰�
											flag = true;
										}
									}
									if (flag) {
										send(ServerProtocol.CREATEROOM + "|" + "true"); // ��ϼ��� �޼��� ������
										viewData = "[ �� ���� �Ϸ� ]" + " ID: " + data[1] + ", roomName: " + data[2]
												+ ", roomRock: " + data[3] + ", roomPw: " + data[4] + "\n";
										platformRun(viewData);
									}
								} else {
									send(ServerProtocol.CREATEROOM + "|" + "false"); // ��Ͻ��� �޼��� ������
									viewData = "[ �� ���� ���� ]" + " ID: " + data[1] + ", roomName: " + data[2]
											+ ", roomRock: " + data[3] + ", roomPw: " + data[4] + "\n";
									platformRun(viewData);
								}
								break;

							case ServerProtocol.GUEST_ENTER: // �Խ�Ʈ�� ���ӹ濡 ���ٴ� ��û�� ���� / ��й濩��, ����г���, �������
								String roomRock = data[1];
								int result = 0; // 1�� �ٲ��� �������, 0�̸� �������
								if (roomRock.equals("true")) {// ��й��϶� ���尡�ɿ��� ��ȸ
									result = grDAO.guestEnterPrivateRoom(data[2], data[3], data[4]); // ȣ��Ʈ��, �Խ�Ʈ��, ��й�ȣ
								} else {// �������϶� ���尡�ɿ��� ��ȸ(Ǯ���ϰ�� 0)
									result = grDAO.guestEnterPublicRoom(data[2], data[3]); // ȣ��Ʈ��, �Խ�Ʈ��
								}
								if (result == 1) { // ���õ� ���� ������̰� ����� ������
									Client guest = null;
									for (Client client : clients) {
										if (client.getNickname().equals(data[3])) {
											guest = client; // �������߿��� �Խ�Ʈ Ŭ���̾�Ʈ�� ã��
										}
									}
									for (Room room : roomMannager) {
										if (room.getHostNick().equals(data[2])) {// ȣ��Ʈ ���� �밴ü�� ã�Ƽ�
											room.setGuestNick(guest.nickname); // �Խ�Ʈ �г����� ����ϰ�
											room.addPlayer(guest); // �Խ�Ʈ�� ȣ��Ʈ�� �濡 ����
										}
									}
									viewData = "[ �Խ�Ʈ ���� ]" + " ������ ���� ȣ��Ʈ: " + data[2] + "\n";
									platformRun(viewData);
									// �����㰡 �޼��� ������
									send(ServerProtocol.GUEST_ENTER + "|" + "true");
								} else {
									send(ServerProtocol.GUEST_ENTER + "|" + "false");
								}
								break;

							case ServerProtocol.GAMEROOM_FULL: // �Խ�Ʈ�� �����ؼ� Ǯ���϶�
								// �ش� �뿡 �ִ� ��������� Ǯ������ �˸��� �����϶�� �޼��� ������
								for (Room room : roomMannager) {
									if (room.getHostNick().equals(data[1])) {// ȣ��Ʈ ���� �밴ü�� ã�Ƽ�, ȣ��Ʈ��, �Խ�Ʈ���� ����
										room.broadcast(ServerProtocol.GAMEROOM_FULL + "|" + room.getHostNick() + "|"
												+ room.guestNick);
									}
								}
								break;

							case ServerProtocol.GAMEROOM_READY: // �÷��̾ �����ȣ�� �����ö�
								// ȣ��Ʈ �г������� ������ ã��
								for (Room room : roomMannager) {
									if (room.getHostNick().equals(data[1])) {
										// �����ѻ���� ���� 1���϶�
										if (room.getReadyCount() < 1) {
											room.addReadyCount();
										} else { // �θ� �� ���� ������
											// ���� �̹��� ���� ����
											GameImageDAO giDAO = new GameImageDAO();
											ArrayList<GameImage> giList = giDAO.getGameImageArray();
											new FileSender(giList).start();

											String MergeCoordinate = "";
											for (GameImage gi : giList) {
												MergeCoordinate = MergeCoordinate + gi.getCoordinates() + "^";
											}
											// ���佺Ʈ���� ������ ���ӽ����ϰ��ϱ�
											room.broadcast(ServerProtocol.GAMEROOM_READY + "|" + MergeCoordinate);
										}
									}

								}
								break;

							case ServerProtocol.GAME_IMAGE_DOWN:
								fileName = new ArrayList<String>();

								try {

								} catch (Exception e) {
									System.out.println("FileSender�������");
									e.printStackTrace();
								}
								break;

							case ServerProtocol.GAME_CD_FOUND: // ����ڰ� ������ǥ�� ã������
								for (Room room : roomMannager) {
									if (room.getHostNick().equals(data[1])) {
										// ã������� �г���, ã��x��ǥ, ã��y��ǥ�� ����
										room.broadcast(ServerProtocol.GAME_CD_FOUND + "|" + data[2] + "|" + data[3]
												+ "|" + data[4]);
									}
								}
								break;

							case ServerProtocol.GAME_CD_NOT_FOUND: // ����ڰ� Ʋ������
								for (Room room : roomMannager) {
									if (room.getHostNick().equals(data[1])) {
										// Ʋ������� �г��� ����
										room.broadcast(ServerProtocol.GAME_CD_NOT_FOUND + "|" + data[2]);
									}
								}
								break;

							case ServerProtocol.GAME_HOST_SURRENDER: // ȣ��Ʈ�� �׺�����
								registerGameResultInDB(data[1], data[2], data[3], false,
										ServerProtocol.GAME_HOST_SURRENDER);
								break;

							case ServerProtocol.GAME_GUEST_SURRENDER: // �Խ�Ʈ�� �׺�����
								registerGameResultInDB(data[1], data[2], data[3], true,
										ServerProtocol.GAME_GUEST_SURRENDER);
								break;

							case ServerProtocol.GAME_HOST_LOSTHEART: // ȣ��Ʈ ��Ʈ������
								registerGameResultInDB(data[1], data[2], data[3], false,
										ServerProtocol.GAME_HOST_LOSTHEART);
								break;

							case ServerProtocol.GAME_GUEST_LOSTHEART: // �Խ�Ʈ ��Ʈ������
								registerGameResultInDB(data[1], data[2], data[3], true,
										ServerProtocol.GAME_GUEST_LOSTHEART);
								break;

							case ServerProtocol.GAME_END_HOSTWIN: // ȣ��Ʈ�� �� ���� ����
								registerGameResultInDB(data[1], data[2], data[3], true,
										ServerProtocol.GAME_END_HOSTWIN);
								break;

							case ServerProtocol.GAME_END_GUESTWIN: // �Խ�Ʈ�� ������ ����
								registerGameResultInDB(data[1], data[2], data[3], false,
										ServerProtocol.GAME_END_GUESTWIN);
								break;

							case ServerProtocol.GAME_HOST_EXIT: // ȣ��Ʈ����
								grDAO.deleteFromGameRoomTbl(data[1]);
								for (Room room : roomMannager) {
									if (room.getHostNick().equals(data[1])) {
										room.broadcast(ServerProtocol.GAME_HOST_EXIT + "|");
										roomMannager.remove(room); // �ش� �밴ü�� ��Ŵ������� ����
									}
								}
								break;

							case ServerProtocol.GAME_GUEST_EXIT: // �Խ�Ʈ����
								for (Room room : roomMannager) {
									if (room.getHostNick().equals(data[1])) {// ȣ��Ʈ�̸����� �밴ü��ã�Ƽ�
										// clients.add(room.getGuest()); //Ŭ���̾�Ʈ�� �Խ�Ʈ��ü �߰�
										// �Խ�Ʈ�� �����ٰ� �˸� (ȣ��Ʈ�� �˸��ް� �濡 �ӹ���, �Խ�Ʈ�� �׳� �κ��)
										room.broadcast(ServerProtocol.GAME_GUEST_EXIT + "|");
									}
								}
								grDAO.guestExitGameRoom(data[1]); // ����¸� �����߿��� ��������� �ٲٰ� �Խ�Ʈ�̸��� ����
								break;

							case ServerProtocol.MYINFO_NICKDUPLICATE: // �г��� �ߺ�üũ
								duplicateNickname = joinDAO.getNickCheckDuplicate(data[1]);
								if (duplicateNickname == true) {
									send(ServerProtocol.MYINFO_NICKDUPLICATE + "|" + "true");
								} else {
									send(ServerProtocol.MYINFO_NICKDUPLICATE + "|" + "false");
								}
								break;

							case ServerProtocol.MYINFO_NICK_CHANGE: // �г��� ����
								int count2 = 0;
								count2 = joinDAO.nickNameChage(data[1], data[2]);
								if (count2 == 1) {
									send(ServerProtocol.MYINFO_NICK_CHANGE + "|" + "true" + "|" + data[1]);
								} else {
									send(ServerProtocol.MYINFO_NICK_CHANGE + "|" + "false" + "|" + data[1]);
								}
								break;

							case ServerProtocol.MYINFO_WITHDRAW: // ȸ��Ż��
								int count3 = joinDAO.memberWithdraw(data[1]);
								if (count3 == 1) {
									send(ServerProtocol.MYINFO_WITHDRAW + "|" + "true");
								} else {
									send(ServerProtocol.MYINFO_WITHDRAW + "|" + "false");
								}
								break;

							case ServerProtocol.MYINFO_HISTORY: // ��ũ������������
								ArrayList<RankVO> rvoList3 = rhDAO.getGameRoomList(data[1]);

								// ����Ʈ�� �ϳ��� ��Ʈ������ �� ����
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

							case ServerProtocol.MYINFO_PWCHANGE: // ��������ϱ�
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
			// �����带 ������Ǯ�� ���(���������� �����ϱ�����)
			threadPool.submit(thread);
		}// end of receive

		// Ŭ���̾�Ʈ�� �޼����� ������ �޼ҵ�
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
					// ������ �߻��ؼ� Ŭ���̾�Ʈ�� ������ ����� �����ȿ��� �ش� Ŭ���̾�Ʈ�� ������ ����
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
					System.out.println("������..");
					os.write(buffer, 0, length);
				}
				os.flush();
				System.out.println("���ۿϷ�");
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
			// ���ӷ� ���̺��� ȣ��Ʈ��, �Խ�Ʈ�� ���
			String[] nickList = grDAO.getAllNickname(hostNick);
			// �г����� ���̵���
			String hostId = joinDAO.getUserId(nickList[0]);
			String guestId = joinDAO.getUserId(nickList[1]);
			// ���Ӱ���� ���� rp ���ϱ�
			int[] newRp = grDAO.gameRpUpdate(hostId, guestId, hostWin, hostScore, guestScore);
			// rp, �¼�, �м�, ��ü���Ӽ� ������Ʈ
			grDAO.gameScoreUpdate(newRp[2], newRp[3], hostId, guestId, hostWin);
			// �������� ������Ʈ
			grDAO.gameCoinUpdate(hostId, guestId, hostWin);
			// �������̺��� �ش� �� ������ �����
			grDAO.deleteFromGameRoomTbl(hostNick);
			// ���Ӱ�� �˸���
			for (Room room : roomMannager) {
				if (room.getHostNick().equals(hostNick)) {
					room.broadcast(protocol + "|" + newRp[0] + "|" + newRp[1]);
					roomMannager.remove(room);
					System.out.println("��Ŵ��� ������" + roomMannager.size());
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
		private int readyCount = 0; // ������ �ο���
		private Vector players = new Vector(); // �÷��̾��Ʈ (ȣ��Ʈ,�Խ�Ʈ)
		private GameImage sheredImg;

		Room(String hostNick, Client client) {
			this.hostNick = hostNick;
			this.players.add(client);
		}

		public void broadcast(String data) { // �������� ������� �˸�
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
