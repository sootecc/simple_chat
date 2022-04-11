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
	
	//���� ���� �����带 ȿ�������� �����ϱ� ���� ���̺귯��
	//�������� ���� ������ �ּ� ���۽��� Client �������� ���� ���� ���� ����
	public static ExecutorService threadPool;		
	
	
	public static Vector<Client> clients = new Vector<Client>();
	
	ServerSocket serverSocket;
	
	//������ �������� Ŭ���̾�Ʈ�� ������ ��ٸ��� �޼ҵ�
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
		
		//Ŭ���̾�Ʈ�� ������ ������ ����ϴ� ������
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try{Socket socket = serverSocket.accept();
					clients.add(new Client(socket));
					System.out.printf("%s %s: %s%s","[Ŭ���̾�Ʈ ����]",socket.getRemoteSocketAddress(),Thread.currentThread().getName(),System.lineSeparator());
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
	
	//������ �۵��� ������Ű�� �޼ҵ�
	public void stopServer() {
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
		}
		//���� �۵� ���� ���Ŀ� ��ü �ڿ��� �Ҵ� ����.
		//�� �޼ҵ��� �ۼ� ���ΰ� ���� ���α׷��� ǰ���� ū ������ ��ģ��.
	}
	
	//UI�� �����ϰ� ���������� ���α׷��� �۵���Ű�� �޼ҵ��Դϴ�.		 exception�� �����°� �³�?
	@Override
	public void start(Stage primaryStage) throws Exception {
		
	}
	
	
	//���α׷��� ������.
	public static void main(String[] args) {
		launch(args);
	}
}
