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

	private final int idMinLength = 4; // 아이디 최소길이
	private final int idMaxLength = 10; // 아이디 최대길이
	private final int pwMinLength = 4; // 비번 최소길이
	private final int pwMaxLength = 10; // 비번 최대길이
	private final int nameMinLength = 1; // 닉네임 최소길이
	private final int nameMaxLength = 8; // 닉네임 최대길이

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

	// 아이디 중복 확인과 형식확인
	private void idDuplicateCheck() {
		String id = txtFieldId.getText();

		if (id.equals("") || id.length() < idMinLength || id.length() > idMaxLength || !id.matches("^[0-9a-z]*$")) {
			ClientUser.alertDisplay(0, "아이디 중복확인", "사용할 수 없는 아이디 형식입니다", "4~10자 이내의 영문 소문자와 숫자로 구성하세요");
		} else {
			// DB연동 중복확인
			clientUser.send(ClientProtocol.JOIN_IDDUPLICATE + "|" + txtFieldId.getText());
		}
	}

	// 아이디 중복검사시 서버로부터 받는 true, false 확인후 클라이언트에게 결과를 알려줌
	public void alertIdDuplicate(String data) {
		Platform.runLater(() -> {
			boolean duplicate = Boolean.parseBoolean(data); //
			if (duplicate == true) {
				ClientUser.alertDisplay(0, "아이디 중복 검사", "중복된 아이디입니다");
			} else {
				ClientUser.alertDisplay(1, "아이디 중복 검사", "사용 가능한 아이디입니다");
			}
		});
	}

	// 비번을 맞게 입력했는지 확인
	private boolean pwCheckEquality() {
		boolean pwCheck = false;
		String pw1 = pwField1.getText();
		String pw2 = pwField2.getText();

		if (pw1.length() < pwMinLength || pw2.length() < pwMinLength || pw1.length() > pwMaxLength || pw2.length() > pwMaxLength) {
			ClientUser.alertDisplay(0, "비밀번호 확인", "사용할 수 없는 비밀번호 형식입니다", "비밀번호는 4~10자 사이로 입력해주세요");
		} else {
			if (!pw1.equals(pw2)) {
				ClientUser.alertDisplay(0, "비밀번호 확인", "비밀번호가 일치하지 않습니다");
			} else {
				ClientUser.alertDisplay(1, "비밀번호 확인", "비밀번호가 일치합니다");
				pwCheck = true;
			}

		}
		return pwCheck;
	}

	// 닉네임 중복확인
	private void NameDuplicateCheck() {
		String name = nickname.getText();

		if (name.equals("") || name.length() < nameMinLength || name.length() > nameMaxLength
				|| !name.matches("^[0-9a-zA-Z가-힣]*$")) {
			ClientUser.alertDisplay(0, "닉네임 중복확인", "사용할 수 없는 닉네임 형식입니다", "1~8자 이내의 한글,영문,숫자로 구성하세요");
		} else {
			// DB연동 중복확인
			clientUser.send(ClientProtocol.JOIN_NICKDUPLICATE + "|" + nickname.getText());
		}
	}
	
	public void alertNickDuplicate(String data) {
		Platform.runLater(() -> {
			boolean duplicate = Boolean.parseBoolean(data); //
			if (duplicate == true) {
				ClientUser.alertDisplay(0, "닉네임 중복 검사", "중복된 닉네임입니다");
			} else {
				ClientUser.alertDisplay(1, "닉네임 중복 검사", "사용 가능한 닉네임입니다");
			}
		});
		
	}

	// 회원가입 ok 버튼 누를때 입력한 데이터가 형식에 맞는지 확인
	private boolean confirmAllFormat() {
		boolean result = true;
		// 아이디 확인
		boolean idConfirm = false;
		String id = txtFieldId.getText();

		if (id.equals("") || id.length() < idMinLength || id.length() > idMaxLength || !id.matches("^[0-9a-z]*$")) {
			idConfirm = false;
		} else {
			idConfirm = true;
		}

		// 비번 확인
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

		// 닉네임 확인
		boolean nameConfirm = false;
		String name = nickname.getText();

		if (name.equals("") || name.length() < nameMinLength || name.length() > nameMaxLength
				|| !name.matches("^[0-9a-zA-Z가-힣]*$")) {
			nameConfirm = false;
		} else {
			// DB연동 중복확인

			nameConfirm = true;
		}

		// 회원가입 가능여부를 알려줌
		if (!idConfirm || !pwConfirm || !nameConfirm) {
			ClientUser.alertDisplay(0, "회원가입 오류", "아이디, 비밀번호, 닉네임을 확인하세요", "형식에 맞게 적어주세요");
			result = false;
		}
		return result;
	}

	// 회원가입 ok 눌렀을때
	private void joinRequest() {
		// 입력한 데이터가 조건에 맞는지 확인하는 함수(아이디, 패스워드, 닉네임 형식). 맞으면 true반환.
		boolean result = false;
		result = confirmAllFormat();
		if (result == true) {
			clientUser.send(ClientProtocol.JOIN_REQUEST + "|" + txtFieldId.getText() + "|" + pwField1.getText() + "|"
					+ nickname.getText());
		}
	}

	// 회원가입 승인 리시브를 받고나서 실행
	public void alertJoinResult(String data) {
		Platform.runLater(() -> {
			if (Boolean.parseBoolean(data) == true) {
				ClientUser.alertDisplay(1, "회원가입 성공", "가입이 완료되었습니다");
				primaryStage.close();
			} else {
				ClientUser.alertDisplay(0, "회원가입 실패", "가입요청이 거절되었습니다", "서버상태를 확인해주세요");
			}

		});
	}

	// 로그인화면에서 이 컨트롤러를 부르기위한 스테이지 셋 함수
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

}
