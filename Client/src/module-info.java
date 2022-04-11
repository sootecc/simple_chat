module simple_chat_client {
	requires javafx.controls;
	
	opens application to javafx.graphics, javafx.fxml;
}
