package controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javafx.scene.chart.PieChart.Data;

class FileRecv extends Thread {

	Socket socket;
	String fileName;
	
	File pathFile = new File("src/resources/images/path.txt");
	String downPath = pathFile.getParent();
	File copyFile;
	long fileSize;

	DataInputStream dis;
	BufferedOutputStream bos;
	BufferedInputStream bis;

	public FileRecv() {

	}

	public FileRecv(String ip, String fileName) {
		try {
			this.fileName = fileName;
			socket = new Socket(ip, 9990);
			System.out.println("파일다운로드 시작");
			dis = new DataInputStream(socket.getInputStream());
			bis = new BufferedInputStream(dis);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {

			for (int i = 0; i < 10; i++) { //한 라운드당 10장의 사진을 전송받음
				copyFile = new File(downPath+"/"+i+".jpg");
				
				this.fileSize = dis.readLong(); // 서버로부터 파일크기를 받아옴
				bos = new BufferedOutputStream(new FileOutputStream(copyFile, false));

				int bufferSize = 4096; // 버퍼단위
				long count = 0;
				int packetSize = (int) fileSize / bufferSize; // 전체 패킷수
				int lastPacket = (int) fileSize % bufferSize; // 남은 패킷수

				byte[] buffer = new byte[bufferSize];
				int readLength = 0;
				for (int j = 0; j < packetSize; j++) {
					if ((readLength = dis.read(buffer, 0, bufferSize)) != -1) {
						bos.write(buffer, 0, readLength);
						count += readLength;
					}
				}
				// 남은패킷 마저받기
				if (lastPacket > 0) {
					if ((readLength = dis.read(buffer, 0, lastPacket)) != -1) {
						bos.write(buffer, 0, readLength);
						count += readLength;
					}
				}
				
				// 버퍼지우기
				if (bos != null)
					this.bos.close();

			}

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("파일전송 실패");
		} finally {
			try {
				// 자원정리
				if (dis != null)
					dis.close();
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}