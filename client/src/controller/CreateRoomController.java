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
	private final int pwLength = 4; // 패스워드 글자수제한
	private final int roomNameMinLength = 1; // 제목 글자수제한
	private final int roomNameMaxLength = 25; // 제목 글자수제한

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// 클라이언트 유저 가져오기
		if (clientUser == null) {
			clientUser = MainController.getClientUser();
			clientUser.setCreateRoomController(this);
		} else {
			clientUser = MainController.getClientUser();
			clientUser.setCreateRoomController(this);
		}

		// 토글버튼 액션> 공개이면 패스워드 비활성화, 만약 숫자가있었으면 지우기
		rbPublic.setOnAction(event2 -> {
			if (txtFieldPw.getText() != "")
				txtFieldPw.clear();
			txtFieldPw.setDisable(true);
		});
		rbPrivate.setOnAction(event1 -> txtFieldPw.setDisable(false));

		// 방제 텍스트필드에 글자수 25개 입력제한
		txtFieldRoomName.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue.length() > roomNameMaxLength) {
					txtFieldRoomName.setText(oldValue);
				}
			}
		});

		// 패스워드 텍스트필드에 오직숫자만 4개입력제한
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

		// 방생성 액션
		btnOk.setOnAction(event2 -> createRoomRequestToServer());
		// 취소 액션
		btnCancel.setOnAction(event3 -> primaryStage.close());
	}

	// 방생성 액션 함수 (서버로 요청보내기)
	private void createRoomRequestToServer() {
		// 제목 글자수 및 비밀번호 조건 확인
		boolean allCorrect = confirmAllFormat();
		if (allCorrect) {
			String id = clientUser.getId(); //db조회용 데이터(닉네임을 가져옴)
			String roomName = txtFieldRoomName.getText(); //방제목
			String roomRock ; //비밀방여부
			String roomPw ; //비번
			if (rbPublic.isSelected()) {
				roomRock = "공개";
				roomPw = " ";	//서버-클라이언트간 split으로 데이터를 쪼개서 통신할시 ""이 안읽혀서 공개시 비밀번호를 임의로 " "로 줌
			} else {
				roomRock = "비공개";
				roomPw = txtFieldPw.getText();
			}
			// 방생성요청 (프로토콜, 방제, 비밀번호, 클라이언트아이디)
			clientUser.send(ClientProtocol.CREATEROOM + "|" + id + "|" + roomName
					+ "|" + roomRock + "|"+ roomPw);
			clientUser.setHostData(clientUser.getNickname());

		}else {
			ClientUser.alertDisplay(0, "방 만들기 오류", "방제와 비밀번호를 확인하세요", "방제는 1~25자, 비밀번호는 숫자 4자리입니다");
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

	// 메인에서 이 컨트롤러를 부르기위한 스테이지 셋 함수
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
