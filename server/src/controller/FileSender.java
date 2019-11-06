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

class FileSender extends Thread { // ���ϼ��� ���� outputstream���� ������ Ŭ���̾�Ʈ���� ����.
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

			// �� ����� 10���� �̹����� ����
			// �������ϸ���Ʈ ����
			for (String str : fileNames) {
				System.out.println("�����̸�"+str);
			}
			
			files = new ArrayList<File>();
			for (int i = 0; i < 10; i++) {
				files.add(new File(filePath +"/"+ fileNames.get(i)));

			}

			// �������۽���
			int readLength = 0;
			for (int i = 0; i < 10; i++) { // ���� 10�� ����
				System.out.println((i + 1) + "�� ��������");

				long fileSize = files.get(i).length(); // ����ũ��
				int bufferSize = 4096; // ���۴���
				int packetSize = (int) fileSize / bufferSize; // ��ü ��Ŷ��
				int lastPacket = (int) fileSize % bufferSize; // ���� ��Ŷ��
				
				dos.writeLong(fileSize); // �� ���� ����ũ��
				bis = new BufferedInputStream(new FileInputStream(files.get(i)));

				byte[] buffer = new byte[bufferSize];
				for (int j = 0; j < packetSize; j++) { // �� ���� ��Ŷ����ŭ �ݺ�
					if ((readLength = bis.read(buffer, 0, bufferSize)) != -1) {
						// 0~���ۿ��� ���� �����ŭ Ŭ���̾�Ʈ�� ����
						dos.write(buffer, 0, readLength);
					}
				}
				// �ܿ���Ŷüũ
				if (lastPacket > 0) {
					System.out.println("�ܿ���Ŷ:" + lastPacket);
					if ((readLength = bis.read(buffer, 0, lastPacket)) != -1) {

						System.out.println("readSize: " + readLength);
						dos.write(buffer, 0, readLength);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("�������� ����");
		} finally {// �ڿ�����
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