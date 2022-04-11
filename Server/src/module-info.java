module simple_chat_server {
	requires javafx.controls;
	
	opens application to javafx.graphics, javafx.fxml;
}
