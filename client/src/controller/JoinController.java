package controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class JoinController implements Initializable {
	@FXML
	private TextField txtFieldId;
	@FXML
	private Button btnIdConfirm;
	@FXML
	private PasswordField pwField1;
	@FXML
	private PasswordField pwField2;
	@FXML
	private Button btnPwConfirm;
	@FXML
	private TextField nickname;
	@FXML
	private Button btnNameConfirm;
	@FXML
	private Button btnOk;
	@FXML
	private Button btnCancel;

	private Stage primaryStage;
	private ClientUser clientUser;

	private final int idMinLength = 4; // ���̵� �ּұ���
	private final int idMaxLength = 10; // ���̵� �ִ����
	private final int pwMinLength = 4; // ��� �ּұ���
	private final int pwMaxLength = 10; // ��� �ִ����
	private final int nameMinLength = 1; // �г��� �ּұ���
	private final int nameMaxLength = 8; // �г��� �ִ����

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		if (clientUser == null) {
			clientUser = LoginController.getClientUser();
			clientUser.setJoinController(this);
		}

		btnIdConfirm.setOnAction(event -> idDuplicateCheck());
		btnPwConfirm.setOnAction(event -> pwCheckEquality());
		btnNameConfirm.setOnAction(event -> NameDuplicateCheck());
		btnOk.setOnAction(event2 -> joinRequest());
		btnCancel.setOnAction(event3 -> primaryStage.close());

	}

	// ���̵� �ߺ� Ȯ�ΰ� ����Ȯ��
	private void idDuplicateCheck() {
		String id = txtFieldId.getText();

		if (id.equals("") || id.length() < idMinLength || id.length() > idMaxLength || !id.matches("^[0-9a-z]*$")) {
			ClientUser.alertDisplay(0, "���̵� �ߺ�Ȯ��", "����� �� ���� ���̵� �����Դϴ�", "4~10�� �̳��� ���� �ҹ��ڿ� ���ڷ� �����ϼ���");
		} else {
			// DB���� �ߺ�Ȯ��
			clientUser.send(ClientProtocol.JOIN_IDDUPLICATE + "|" + txtFieldId.getText());
		}
	}

	// ���̵� �ߺ��˻�� �����κ��� �޴� true, false Ȯ���� Ŭ���̾�Ʈ���� ����� �˷���
	public void alertIdDuplicate(String data) {
		Platform.runLater(() -> {
			boolean duplicate = Boolean.parseBoolean(data); //
			if (duplicate == true) {
				ClientUser.alertDisplay(0, "���̵� �ߺ� �˻�", "�ߺ��� ���̵��Դϴ�");
			} else {
				ClientUser.alertDisplay(1, "���̵� �ߺ� �˻�", "��� ������ ���̵��Դϴ�");
			}
		});
	}

	// ����� �°� �Է��ߴ��� Ȯ��
	private boolean pwCheckEquality() {
		boolean pwCheck = false;
		String pw1 = pwField1.getText();
		String pw2 = pwField2.getText();

		if (pw1.length() < pwMinLength || pw2.length() < pwMinLength || pw1.length() > pwMaxLength || pw2.length() > pwMaxLength) {
			ClientUser.alertDisplay(0, "��й�ȣ Ȯ��", "����� �� ���� ��й�ȣ �����Դϴ�", "��й�ȣ�� 4~10�� ���̷� �Է����ּ���");
		} else {
			if (!pw1.equals(pw2)) {
				ClientUser.alertDisplay(0, "��й�ȣ Ȯ��", "��й�ȣ�� ��ġ���� �ʽ��ϴ�");
			} else {
				ClientUser.alertDisplay(1, "��й�ȣ Ȯ��", "��й�ȣ�� ��ġ�մϴ�");
				pwCheck = true;
			}

		}
		return pwCheck;
	}

	// �г��� �ߺ�Ȯ��
	private void NameDuplicateCheck() {
		String name = nickname.getText();

		if (name.equals("") || name.length() < nameMinLength || name.length() > nameMaxLength
				|| !name.matches("^[0-9a-zA-Z��-�R]*$")) {
			ClientUser.alertDisplay(0, "�г��� �ߺ�Ȯ��", "����� �� ���� �г��� �����Դϴ�", "1~8�� �̳��� �ѱ�,����,���ڷ� �����ϼ���");
		} else {
			// DB���� �ߺ�Ȯ��
			clientUser.send(ClientProtocol.JOIN_NICKDUPLICATE + "|" + nickname.getText());
		}
	}
	
	public void alertNickDuplicate(String data) {
		Platform.runLater(() -> {
			boolean duplicate = Boolean.parseBoolean(data); //
			if (duplicate == true) {
				ClientUser.alertDisplay(0, "�г��� �ߺ� �˻�", "�ߺ��� �г����Դϴ�");
			} else {
				ClientUser.alertDisplay(1, "�г��� �ߺ� �˻�", "��� ������ �г����Դϴ�");
			}
		});
		
	}

	// ȸ������ ok ��ư ������ �Է��� �����Ͱ� ���Ŀ� �´��� Ȯ��
	private boolean confirmAllFormat() {
		boolean result = true;
		// ���̵� Ȯ��
		boolean idConfirm = false;
		String id = txtFieldId.getText();

		if (id.equals("") || id.length() < idMinLength || id.length() > idMaxLength || !id.matches("^[0-9a-z]*$")) {
			idConfirm = false;
		} else {
			idConfirm = true;
		}

		// ��� Ȯ��
		boolean pwConfirm = false;
		String pw1 = pwField1.getText();
		String pw2 = pwField2.getText();

		if (!pw1.matches("^[0-9a-z]*$") || !pw2.matches("^[0-9a-z]*$") || pw1.length() < pwMinLength
				|| pw2.length() < pwMinLength || pw1.length() > pwMaxLength || pw2.length() > pwMaxLength) {
			pwConfirm = false;
		} else {
			if (!pw1.equals(pw2)) {
				pwConfirm = false;
			} else {
				pwConfirm = true;
			}
		}

		// �г��� Ȯ��
		boolean nameConfirm = false;
		String name = nickname.getText();

		if (name.equals("") || name.length() < nameMinLength || name.length() > nameMaxLength
				|| !name.matches("^[0-9a-zA-Z��-�R]*$")) {
			nameConfirm = false;
		} else {
			// DB���� �ߺ�Ȯ��

			nameConfirm = true;
		}

		// ȸ������ ���ɿ��θ� �˷���
		if (!idConfirm || !pwConfirm || !nameConfirm) {
			ClientUser.alertDisplay(0, "ȸ������ ����", "���̵�, ��й�ȣ, �г����� Ȯ���ϼ���", "���Ŀ� �°� �����ּ���");
			result = false;
		}
		return result;
	}

	// ȸ������ ok ��������
	private void joinRequest() {
		// �Է��� �����Ͱ� ���ǿ� �´��� Ȯ���ϴ� �Լ�(���̵�, �н�����, �г��� ����). ������ true��ȯ.
		boolean result = false;
		result = confirmAllFormat();
		if (result == true) {
			clientUser.send(ClientProtocol.JOIN_REQUEST + "|" + txtFieldId.getText() + "|" + pwField1.getText() + "|"
					+ nickname.getText());
		}
	}

	// ȸ������ ���� ���ú긦 �ް��� ����
	public void alertJoinResult(String data) {
		Platform.runLater(() -> {
			if (Boolean.parseBoolean(data) == true) {
				ClientUser.alertDisplay(1, "ȸ������ ����", "������ �Ϸ�Ǿ����ϴ�");
				primaryStage.close();
			} else {
				ClientUser.alertDisplay(0, "ȸ������ ����", "���Կ�û�� �����Ǿ����ϴ�", "�������¸� Ȯ�����ּ���");
			}

		});
	}

	// �α���ȭ�鿡�� �� ��Ʈ�ѷ��� �θ������� �������� �� �Լ�
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

}
