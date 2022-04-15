package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {//서버측의 클라이언트에서 데이터 송수신 기능을 할 수 있게 짜줘야 한다.
	
	Socket socket;
	
	//한 명의 클라이언트와 통신하도록 해주는 클라이언트 클래스
	public Client(Socket socket) {
		this.socket = socket;
		receive();
	}
	
	//반복적으로 클라이언트로부터 메시지를 받는 메소드
	public void receive() {	//개선점: 메시지를 전달 받자마자 곧바로 모든 클라이언트에게 그대로 전달해줌
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();		//소켓에 들어온  input stream을 리턴해준다.
						byte[] buffer = new byte[512];
						
						int length = in.read(buffer);	
						if(length == -1) throw new IOException();		//버퍼의 끝 -1
						System.out.printf("%s %s: %s%s","[메시지 수신 성공]",socket.getRemoteSocketAddress(),Thread.currentThread().getName(),System.lineSeparator());
					
						String message = new String(buffer, 0, length, "UTF-8");
						for(Client client : Main.clients) { 	//클라이언트 전부에게 메세지 보내는 부분
							client.send(message);				//여기서 IP, port 가지고 특정 클라이언트에게만 보낼 수 있겠다.
						}
					}
				}
				catch(Exception e) {
					
					try {
						System.out.printf("%s %s: %s%s","[메시지 수신 오류]",socket.getRemoteSocketAddress(),Thread.currentThread().getName(),System.lineSeparator());
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
	
	//해당 클라이언트로부터 메시지를 받는 메소드
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
						System.out.printf("%s %s: %s%s","[메시지 송신 오류]",socket.getRemoteSocketAddress(),Thread.currentThread().getName(),System.lineSeparator());
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
