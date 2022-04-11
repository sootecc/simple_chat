package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application {
	
	//여러 개의 쓰레드를 효율적으로 관리하기 위한 라이브러리
	//쓰레드의 숫자 제한을 둬서 갑작스런 Client 폭증에도 서버 성능 저하 방지
	public static ExecutorService threadPool;		
	
	
	public static Vector<Client> clients = new Vector<Client>();
	
	ServerSocket serverSocket;
	
	//서버를 구동시켜 클라이언트의 연결을 기다리는 메소드
	public void startServer(String IP, int port) {
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP ,port));
		}
		catch(Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed())
				stopServer();
			return;
		}
		
		//클라이언트가 접속할 때까지 대기하는 쓰레드
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try{Socket socket = serverSocket.accept();
					clients.add(new Client(socket));
					System.out.printf("%s %s: %s%s","[클라이언트 접속]",socket.getRemoteSocketAddress(),Thread.currentThread().getName(),System.lineSeparator());
					}
					catch(Exception e) {
						if(!serverSocket.isClosed())
							stopServer();
						break;
					}
					
				}
			}
		};
		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);
	}
	
	//서버의 작동을 중지시키는 메소드
	public void stopServer() {
		try {
			//현재 작동중인 모든 소켓 닫기
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
				
			}
			
			//서버 소켓 닫기
			if(serverSocket != null && !serverSocket.isClosed())
				serverSocket.close();
		
			//쓰레드 풀 종료
			if(threadPool != null && !threadPool.isShutdown())
				threadPool.shutdown();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		//서버 작동 종료 이후에 전체 자원을 할당 해제.
		//이 메소드의 작성 여부가 서버 프로그램의 품질에 큰 영향을 미친다.
	}
	
	//UI를 생성하고 실질적으로 프로그램을 작동시키는 메소드입니다.		 exception을 던지는게 맞나?
	@Override
	public void start(Stage primaryStage) throws Exception {
		
	}
	
	
	//프로그램의 진입점.
	public static void main(String[] args) {
		launch(args);
	}
}
