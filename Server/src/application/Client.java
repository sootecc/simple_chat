package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {//�������� Ŭ���̾�Ʈ���� ������ �ۼ��� ����� �� �� �ְ� ¥��� �Ѵ�.
	
	Socket socket;
	
	//�� ���� Ŭ���̾�Ʈ�� ����ϵ��� ���ִ� Ŭ���̾�Ʈ Ŭ����
	public Client(Socket socket) {
		this.socket = socket;
		receive();
	}
	
	//�ݺ������� Ŭ���̾�Ʈ�κ��� �޽����� �޴� �޼ҵ�
	public void receive() {	//������: �޽����� ���� ���ڸ��� ��ٷ� ��� Ŭ���̾�Ʈ���� �״�� ��������
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();		//���Ͽ� ����  input stream�� �������ش�.
						byte[] buffer = new byte[512];
						
						int length = in.read(buffer);	
						if(length == -1) throw new IOException();		//������ �� -1
						System.out.printf("%s %s: %s%s","[�޽��� ���� ����]",socket.getRemoteSocketAddress(),Thread.currentThread().getName(),System.lineSeparator());
					
						String message = new String(buffer, 0, length, "UTF-8");
						for(Client client : Main.clients) { 	//Ŭ���̾�Ʈ ���ο��� �޼��� ������ �κ�
							client.send(message);				//���⼭ IP, port ������ Ư�� Ŭ���̾�Ʈ���Ը� ���� �� �ְڴ�.
						}
					}
				}
				catch(Exception e) {
					
					try {
						System.out.printf("%s %s: %s%s","[�޽��� ���� ����]",socket.getRemoteSocketAddress(),Thread.currentThread().getName(),System.lineSeparator());
						Main.clients.remove(Client.this);	
						socket.close();
					}
					catch(Exception e2) {
						e2.printStackTrace();
					}
					
				}
				
			}
		};
		Main.threadPool.submit(thread);
	}
	
	//�ش� Ŭ���̾�Ʈ�κ��� �޽����� �޴� �޼ҵ�
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				}
				catch(Exception e) {
					try {
						System.out.printf("%s %s: %s%s","[�޽��� �۽� ����]",socket.getRemoteSocketAddress(),Thread.currentThread().getName(),System.lineSeparator());
						Main.clients.remove(Client.this);
						socket.close();
					}
					catch(Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread);
	}
	
}
