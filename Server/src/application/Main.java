package application;
	

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class Main extends Application {	//Application은 javafx의 클래스
	
	//여러 개의 쓰레드를 효율적으로 관리하기 위한 라이브러리
	//쓰레드의 숫자 제한을 둬서 갑작스런 Client 폭증에도 서버 성능 저하 방지
	public static ExecutorService threadPool;		
	
	
	public static Vector<Client> clients = new Vector<Client>();	//Vector로 짠 이유?
	
	ServerSocket serverSocket;
	
	//서버를 구동시켜 클라이언트의 연결을 기다리는 메소드
	public void startServer(String IP, int port) {		//서버를 시작할 때 소켓도 함께 생성한다.
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP ,port));		//InetSocketAddress를 String, int로 호출하면  InetAddress로 들어가 String에 해당하는 IP를 찾는다. String 부분에 "{@code www.example.com}" 또는 리터럴 IP주소형식이 들어오면 주소 형식의 유효성을 확인한다. 여기서는 IP 형식으로 
		}						//서버 소켓과 IP, port 연결
		catch(Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed())	//isClosed -> 소켓이 닫혀있으면 true //Exception이 들어왔을 때 서버 소켓이 열려 있으면 stopServer
				stopServer();				
			return;
		}
		
		//클라이언트가 접속할 때까지 대기하는 쓰레드
		Runnable thread = new Runnable() {
			@Override
			public void run() {		//서버
				while(true) {
					try{
						Socket socket = serverSocket.accept();		//accept가 listen의 역할을 하네
						clients.add(new Client(socket));				
						System.out.printf("%s %s: %s%s","[클라이언트 접속]",socket.getRemoteSocketAddress(),Thread.currentThread().getName(),System.lineSeparator());
					}
					catch(Exception e) {
						if(!serverSocket.isClosed())		//대기중 Exception 발생하면 소켓이 닫혔는지 체크 후 stopServer
							stopServer();
						break;
					}
					
				}
			}
		};
		threadPool = Executors.newCachedThreadPool();	//필요할 때, 필요한 만큼 쓰레드풀을 생성. 이미 생성된 쓰레드를 재활용할 수 있기 때문에 성능상의 이점이 있을 수 있다.
		threadPool.submit(thread);						//실행에 필요한 Runnable Future을 리턴한다. Future은 asynchronous computation의 결과를 반환한다.
	}
	
	//서버의 작동을 중지시키는 메소드
	public void stopServer() {							//연결 종료 close()의 역할 여기서 수행
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
		}//작동중인 클라이언트 소켓 -> 서버 소켓 -> 쓰레드 풀 순으로 닫아준다. 
		//서버 작동 종료 이후에 전체 자원을 할당 해제.
		//이 메소드의 작성 여부가 서버 프로그램의 품질에 큰 영향을 미친다.
	}
	
	//UI를 생성하고 실질적으로 프로그램을 작동시키는 메소드입니다.		 exception을 던지는게 맞나?
	@Override
	public void start(Stage primaryStage) throws Exception {	
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));		//패딩 5
		
		TextArea textArea = new TextArea();	//채팅 텍스트 부분
		textArea.setEditable(false);		//입력 불가, 색상 바꾸기 
		textArea.setFont(new Font("나눔고딕",15));
		root.setCenter(textArea);
		
		
		Button toggleButton = new Button("시작하기");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton,new Insets(1, 0, 0, 0));
		root.setBottom(toggleButton);
		
		//여기까지 only UI파트
		
		String IP = "127.0.0.1";				
		int port = 9876;
		//IP와 포트 세팅
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("시작하기")) {	//시작 조건
				startServer(IP, port);	//서버의 IP와 port는 고정시켜도 괜찮지 않을까?  //실질적 동작 부분 . start///////////////////////
				Platform.runLater(()->{
					String message = String.format("서버 시작\n",IP ,port);
					textArea.appendText(message);
					toggleButton.setText("종료하기");
				});
			}
			else {
				stopServer();	//실질적 동작 부분 stop////////////////////////
				Platform.runLater(()->{
					String message = String.format("서버 종료\n",IP ,port);
					textArea.appendText(message);
					toggleButton.setText("시작하기");		//버튼 토글
				});
			}
		});
		
		
		Scene scene = new Scene(root, 400, 400);
		primaryStage.setTitle("[채팅 서버]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
		
		
		
		
	}
	
	
	//프로그램의 진입점.
	public static void main(String[] args) {
		launch(args);
	}
}
