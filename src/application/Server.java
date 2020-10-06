package application;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Server extends Application {

	// declare components of GUI
	private Scene scene, first;
	private TextField userText, portTextField;
	private Text portText;
	private TextArea chatArea;
	private Label errorLabel;
	private Button portBtn;
	private BorderPane border;
	private GridPane setPortGrid;
	private ScrollPane scroll;
	private ObjectOutputStream output;// declare an output stream
	private ObjectInputStream input;// declare an output stream
	private ServerSocket server;// prepare a socket in server
	private Socket connection;// create a socket for a connection
	private String msg = " Welcome to Chatty! ";
	private int port=55555; // Initiate a port number that range from 0 to 65535
	private Thread thread = new Thread();// take an object of thread

	public static void main(String[] args) {
		launch(args);
	}


	@Override
	public void start(Stage primaryStage) throws Exception {
		portScene(primaryStage);
		// primaryStage.setScene(portScene(primaryStage));
		userText = new TextField();
		userText.setEditable(false);// to prevent user from typing before another user connect
		userText.setOnAction(e -> {
			// to send massages that is typed in text field
			Messenger(userText.getText());
			userText.setText("");// clear it after sending
		});

		border = new BorderPane();
		border.setBottom(userText);// position the text field in the button of the layout

		chatArea = new TextArea();
		scroll = new ScrollPane(chatArea);
		scroll.fitToHeightProperty().set(true);// fit it to height and width
		scroll.fitToWidthProperty().set(true);
		border.setCenter(scroll);// position the text field in the center of the layout

		scene = new Scene(border, 400, 350);
		primaryStage.setTitle("Server Chat");
		primaryStage.show();// display the window

//		System.out.println(port);

		// create a new thread to start connection
		thread = new Thread(() -> {
			setupServer(port);// run the server
		});
		thread.start();

	}

	/**
	 * make the first scent displayed to get the port number
	 * 
	 * @param primaryStage
	 */
	public void portScene(Stage primaryStage) {
		// Make the root and set properties
		// first layout to set port is GridPane with 20px padding around edges
		setPortGrid = new GridPane();
		setPortGrid.setPadding(new Insets(20));
		setPortGrid.setVgap(10);
		setPortGrid.setHgap(10);
		setPortGrid.setAlignment(Pos.CENTER);

		/* Text label and field for port Number */
		portText = new Text("Port Number");
		errorLabel = new Label();// to display error statement on it
		errorLabel.setTextFill(Color.RED);
		portTextField = new TextField();
		portText.setFont(Font.font("Tahoma"));
		/*
		 * event When "Done" button is clicked,check validation of port number and start
		 * chat scene
		 */
		portBtn = new Button("Done");
		portBtn.setOnAction(event -> {
			try {

				/* Change the view of the primary stage */
				port = Integer.parseInt(portTextField.getText());
				// System.out.println(port);
				primaryStage.hide();
				primaryStage.setScene(scene);
				primaryStage.show();
			} catch (IllegalArgumentException e) {
				errorLabel.setText("Invalid port number");
			}

		});

		/* Add the views to the grid pane */
		GridPane.setConstraints(portText, 0, 0); // constrains use (child, column, row)
		GridPane.setConstraints(portTextField, 0, 1);
		GridPane.setConstraints(portBtn, 0, 2);
		GridPane.setConstraints(errorLabel, 0, 3);
		setPortGrid.getChildren().addAll(portText, portTextField, portBtn, errorLabel);

		/*
		 * Make the Scene and return it Scene has constructor (Parent, Width, Height)
		 */
		first = new Scene(setPortGrid, 400, 350);
		primaryStage.setScene(first);

	}

	/* start running the server */
	public void setupServer(int port) {
		// System.out.println(port);
		try {
			server = new ServerSocket(port, 100);// port number and max users can connect
			waitingConnection();// call waiting function
			setupConnection();// call function to setup streams
			while (true) {
				try {
					Conversation();// while chat

				} catch (SocketException e) {
					// when client is closed
					Platform.runLater(new Runnable() {
						public void run() {
							Interrupt("\nError in client");
						}

					});
					break;
				} catch (EOFException eofE) {// end of the stream (connection)
					Interrupt("\nServer is closed");
					break;
				} finally {
					// closing stream and socket after finish
					Typing(false);
					output.close();
					input.close();
					connection.close();
					thread.interrupt();
				}
			}

		} catch (IOException ioE) {
			ioE.printStackTrace();
		}
	}

	// waiting for connection then display connection information
	private void waitingConnection() {
		try {
			Interrupt("Waiting for connection... \n");
			connection = server.accept();
			Interrupt(connection.getInetAddress().getHostName() + " is connected");
		} catch (IOException ioE) {
			ioE.printStackTrace();
		}
	}

	// get stream to receive and send data
	private void setupConnection() {
		try {
			output = new ObjectOutputStream(connection.getOutputStream());
			output.flush();
			input = new ObjectInputStream(connection.getInputStream());

		} catch (IOException ioE) {
			ioE.printStackTrace();
		}
	}

	// while chatting
	private void Conversation() throws IOException {

		// Messenger(msg);
		Typing(true);
		do {
			try {
				msg = (String) input.readObject();
				Interrupt("\n" + msg);
			} catch (ClassNotFoundException classNotFoundException) {
				Interrupt("The user has sent an unknown object!");
			}
		} while (!msg.equals("Client : END"));// type END to finish the conversation
	}

	// sending messages to client
	private void Messenger(String Message) {
		try {
			output.writeObject("Server : " + Message);
			output.flush();
			Interrupt("\nServer : " + Message);

		} catch (IOException ioE) {
			chatArea.appendText("\nError in sending");
		}
	}

	// show massages on chat area
	private void Interrupt(final String Msg) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				chatArea.appendText(Msg);

			}

		});
	}

	// Ability to type in text field
	private void Typing(final boolean flag) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				userText.setEditable(flag);

			}

		});
	}

}
