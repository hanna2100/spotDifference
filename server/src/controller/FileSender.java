package controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

class FileSender extends Thread { // 파일서버 역할 outputstream으로 파일을 클라이언트에게 전송.
	String filePath = "C:/gameImage";
	ArrayList<File> files;
	ArrayList<String> fileNames;
	ArrayList<GameImage> gameImages;
	BufferedInputStream bis;
	DataOutputStream dos;

	ServerSocket serverSocket;
	Socket socket;

	public FileSender() {

	}

	public FileSender(ArrayList<GameImage> gameImages) {
		this.gameImages = gameImages;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(9990);
			serverSocket.setSoTimeout(5000);
			socket = serverSocket.accept();
			dos = new DataOutputStream(socket.getOutputStream());
			
			fileNames = new ArrayList<String>();
			String name;
			String[] data;
			for (GameImage gi : gameImages) {
				name = gi.getFileName();
				data = name.split("\\|");
				fileNames.add(data[0]);
				fileNames.add(data[1]);
			}

			// 한 라운드당 10장의 이미지를 전송
			// 보낼파일리스트 생성
			for (String str : fileNames) {
				System.out.println("파일이름"+str);
			}
			
			files = new ArrayList<File>();
			for (int i = 0; i < 10; i++) {
				files.add(new File(filePath +"/"+ fileNames.get(i)));

			}

			// 파일전송시작
			int readLength = 0;
			for (int i = 0; i < 10; i++) { // 파일 10개 보냄
				System.out.println((i + 1) + "번 파일전송");

				long fileSize = files.get(i).length(); // 파일크기
				int bufferSize = 4096; // 버퍼단위
				int packetSize = (int) fileSize / bufferSize; // 전체 패킷수
				int lastPacket = (int) fileSize % bufferSize; // 남은 패킷수
				
				dos.writeLong(fileSize); // 총 보낼 파일크기
				bis = new BufferedInputStream(new FileInputStream(files.get(i)));

				byte[] buffer = new byte[bufferSize];
				for (int j = 0; j < packetSize; j++) { // 총 보낼 패킷수만큼 반복
					if ((readLength = bis.read(buffer, 0, bufferSize)) != -1) {
						// 0~버퍼에서 읽은 사이즈만큼 클라이언트로 보냄
						dos.write(buffer, 0, readLength);
					}
				}
				// 잔여패킷체크
				if (lastPacket > 0) {
					System.out.println("잔여패킷:" + lastPacket);
					if ((readLength = bis.read(buffer, 0, lastPacket)) != -1) {

						System.out.println("readSize: " + readLength);
						dos.write(buffer, 0, readLength);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("파일전송 실패");
		} finally {// 자원정리
			try {
				if (bis != null)
					bis.close();
				if (dos != null)
					dos.close();
				if (socket != null)
					socket.close();
				if (serverSocket != null)
					serverSocket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}