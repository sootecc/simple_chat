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


public class Main extends Application {	//Application�� javafx�� Ŭ����
	
	//���� ���� �����带 ȿ�������� �����ϱ� ���� ���̺귯��
	//�������� ���� ������ �ּ� ���۽��� Client �������� ���� ���� ���� ����
	public static ExecutorService threadPool;		
	
	
	public static Vector<Client> clients = new Vector<Client>();	//Vector�� § ����?
	
	ServerSocket serverSocket;
	
	//������ �������� Ŭ���̾�Ʈ�� ������ ��ٸ��� �޼ҵ�
	public void startServer(String IP, int port) {		//������ ������ �� ���ϵ� �Բ� �����Ѵ�.
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP ,port));		//InetSocketAddress�� String, int�� ȣ���ϸ�  InetAddress�� �� String�� �ش��ϴ� IP�� ã�´�. String �κп� "{@code www.example.com}" �Ǵ� ���ͷ� IP�ּ������� ������ �ּ� ������ ��ȿ���� Ȯ���Ѵ�. ���⼭�� IP �������� 
		}						//���� ���ϰ� IP, port ����
		catch(Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed())	//isClosed -> ������ ���������� true //Exception�� ������ �� ���� ������ ���� ������ stopServer
				stopServer();				
			return;
		}
		
		//Ŭ���̾�Ʈ�� ������ ������ ����ϴ� ������
		Runnable thread = new Runnable() {
			@Override
			public void run() {		//����
				while(true) {
					try{
						Socket socket = serverSocket.accept();		//accept�� listen�� ������ �ϳ�
						clients.add(new Client(socket));				
						System.out.printf("%s %s: %s%s","[Ŭ���̾�Ʈ ����]",socket.getRemoteSocketAddress(),Thread.currentThread().getName(),System.lineSeparator());
					}
					catch(Exception e) {
						if(!serverSocket.isClosed())		//����� Exception �߻��ϸ� ������ �������� üũ �� stopServer
							stopServer();
						break;
					}
					
				}
			}
		};
		threadPool = Executors.newCachedThreadPool();	//�ʿ��� ��, �ʿ��� ��ŭ ������Ǯ�� ����. �̹� ������ �����带 ��Ȱ���� �� �ֱ� ������ ���ɻ��� ������ ���� �� �ִ�.
		threadPool.submit(thread);						//���࿡ �ʿ��� Runnable Future�� �����Ѵ�. Future�� asynchronous computation�� ����� ��ȯ�Ѵ�.
	}
	
	//������ �۵��� ������Ű�� �޼ҵ�
	public void stopServer() {							//���� ���� close()�� ���� ���⼭ ����
		try {
			//���� �۵����� ��� ���� �ݱ�
			Iterator<Client> iterator = clients.iterator();	
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
				
			}
			
			//���� ���� �ݱ�
			if(serverSocket != null && !serverSocket.isClosed())
				serverSocket.close();
		
			//������ Ǯ ����
			if(threadPool != null && !threadPool.isShutdown())
				threadPool.shutdown();
		}
		catch (Exception e) {
			e.printStackTrace();
		}//�۵����� Ŭ���̾�Ʈ ���� -> ���� ���� -> ������ Ǯ ������ �ݾ��ش�. 
		//���� �۵� ���� ���Ŀ� ��ü �ڿ��� �Ҵ� ����.
		//�� �޼ҵ��� �ۼ� ���ΰ� ���� ���α׷��� ǰ���� ū ������ ��ģ��.
	}
	
	//UI�� �����ϰ� ���������� ���α׷��� �۵���Ű�� �޼ҵ��Դϴ�.		 exception�� �����°� �³�?
	@Override
	public void start(Stage primaryStage) throws Exception {	
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));		//�е� 5
		
		TextArea textArea = new TextArea();	//ä�� �ؽ�Ʈ �κ�
		textArea.setEditable(false);		//�Է� �Ұ�, ���� �ٲٱ� 
		textArea.setFont(new Font("�������",15));
		root.setCenter(textArea);
		
		
		Button toggleButton = new Button("�����ϱ�");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton,new Insets(1, 0, 0, 0));
		root.setBottom(toggleButton);
		
		//������� only UI��Ʈ
		
		String IP = "127.0.0.1";				
		int port = 9876;
		//IP�� ��Ʈ ����
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("�����ϱ�")) {	//���� ����
				startServer(IP, port);	//������ IP�� port�� �������ѵ� ������ ������?  //������ ���� �κ� . start///////////////////////
				Platform.runLater(()->{
					String message = String.format("���� ����\n",IP ,port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
				});
			}
			else {
				stopServer();	//������ ���� �κ� stop////////////////////////
				Platform.runLater(()->{
					String message = String.format("���� ����\n",IP ,port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");		//��ư ���
				});
			}
		});
		
		
		Scene scene = new Scene(root, 400, 400);
		primaryStage.setTitle("[ä�� ����]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
		
		
		
		
	}
	
	
	//���α׷��� ������.
	public static void main(String[] args) {
		launch(args);
	}
}
