package controller;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

public class CreateRoomController implements Initializable {
	@FXML
	private TextField txtFieldRoomName;
	@FXML
	private TextField txtFieldPw;
	@FXML
	private RadioButton rbPublic;
	@FXML
	private RadioButton rbPrivate;
	@FXML
	private Button btnOk;
	@FXML
	private Button btnCancel;

	private static ClientUser clientUser;
	private Stage primaryStage;
	private final int pwLength = 4; // �н����� ���ڼ�����
	private final int roomNameMinLength = 1; // ���� ���ڼ�����
	private final int roomNameMaxLength = 25; // ���� ���ڼ�����

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// Ŭ���̾�Ʈ ���� ��������
		if (clientUser == null) {
			clientUser = MainController.getClientUser();
			clientUser.setCreateRoomController(this);
		} else {
			clientUser = MainController.getClientUser();
			clientUser.setCreateRoomController(this);
		}

		// ��۹�ư �׼�> �����̸� �н����� ��Ȱ��ȭ, ���� ���ڰ��־����� �����
		rbPublic.setOnAction(event2 -> {
			if (txtFieldPw.getText() != "")
				txtFieldPw.clear();
			txtFieldPw.setDisable(true);
		});
		rbPrivate.setOnAction(event1 -> txtFieldPw.setDisable(false));

		// ���� �ؽ�Ʈ�ʵ忡 ���ڼ� 25�� �Է�����
		txtFieldRoomName.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue.length() > roomNameMaxLength) {
					txtFieldRoomName.setText(oldValue);
				}
			}
		});

		// �н����� �ؽ�Ʈ�ʵ忡 �������ڸ� 4���Է�����
				DecimalFormat format = new DecimalFormat("####");
			
				txtFieldPw.setTextFormatter(new TextFormatter<>(event -> {
					if (event.getControlNewText().isEmpty()) {
						return event;
					}
					ParsePosition parsePosition = new ParsePosition(0);
					Object object = format.parse(event.getControlNewText(), parsePosition);
					if (object == null || parsePosition.getIndex() < event.getControlNewText().length()
							|| event.getControlNewText().length() == 5) {
						return null;
					} else {
						return event;
					}
				}));

		// ����� �׼�
		btnOk.setOnAction(event2 -> createRoomRequestToServer());
		// ��� �׼�
		btnCancel.setOnAction(event3 -> primaryStage.close());
	}

	// ����� �׼� �Լ� (������ ��û������)
	private void createRoomRequestToServer() {
		// ���� ���ڼ� �� ��й�ȣ ���� Ȯ��
		boolean allCorrect = confirmAllFormat();
		if (allCorrect) {
			String id = clientUser.getId(); //db��ȸ�� ������(�г����� ������)
			String roomName = txtFieldRoomName.getText(); //������
			String roomRock ; //��й濩��
			String roomPw ; //���
			if (rbPublic.isSelected()) {
				roomRock = "����";
				roomPw = " ";	//����-Ŭ���̾�Ʈ�� split���� �����͸� �ɰ��� ����ҽ� ""�� �������� ������ ��й�ȣ�� ���Ƿ� " "�� ��
			} else {
				roomRock = "�����";
				roomPw = txtFieldPw.getText();
			}
			// �������û (��������, ����, ��й�ȣ, Ŭ���̾�Ʈ���̵�)
			clientUser.send(ClientProtocol.CREATEROOM + "|" + id + "|" + roomName
					+ "|" + roomRock + "|"+ roomPw);
			clientUser.setHostData(clientUser.getNickname());

		}else {
			ClientUser.alertDisplay(0, "�� ����� ����", "������ ��й�ȣ�� Ȯ���ϼ���", "������ 1~25��, ��й�ȣ�� ���� 4�ڸ��Դϴ�");
		}

	}

	public boolean confirmAllFormat() {
		boolean result = true;
		if (txtFieldRoomName.getText().equals("") || txtFieldRoomName.getText().length() < roomNameMinLength
				|| txtFieldRoomName.getText().length() > roomNameMaxLength) {
			result = false;
		} else if (rbPrivate.isSelected()) {
			if(txtFieldPw.getText().equals("") || txtFieldPw.getText().length() != 4
					|| !txtFieldPw.getText().matches("^[0-9]*$")) {
				result = false;
			}
		} else {

		}

		return result;
	}

	// ���ο��� �� ��Ʈ�ѷ��� �θ������� �������� �� �Լ�
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	public void closeStage() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				primaryStage.close();
			}
		});
		
	}

}
