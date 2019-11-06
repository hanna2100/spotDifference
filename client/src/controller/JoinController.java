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

	private final int idMinLength = 4; // ¾ÆÀÌµğ ÃÖ¼Ò±æÀÌ
	private final int idMaxLength = 10; // ¾ÆÀÌµğ ÃÖ´ë±æÀÌ
	private final int pwMinLength = 4; // ºñ¹ø ÃÖ¼Ò±æÀÌ
	private final int pwMaxLength = 10; // ºñ¹ø ÃÖ´ë±æÀÌ
	private final int nameMinLength = 1; // ´Ğ³×ÀÓ ÃÖ¼Ò±æÀÌ
	private final int nameMaxLength = 8; // ´Ğ³×ÀÓ ÃÖ´ë±æÀÌ

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

	// ¾ÆÀÌµğ Áßº¹ È®ÀÎ°ú Çü½ÄÈ®ÀÎ
	private void idDuplicateCheck() {
		String id = txtFieldId.getText();

		if (id.equals("") || id.length() < idMinLength || id.length() > idMaxLength || !id.matches("^[0-9a-z]*$")) {
			ClientUser.alertDisplay(0, "¾ÆÀÌµğ Áßº¹È®ÀÎ", "»ç¿ëÇÒ ¼ö ¾ø´Â ¾ÆÀÌµğ Çü½ÄÀÔ´Ï´Ù", "4~10ÀÚ ÀÌ³»ÀÇ ¿µ¹® ¼Ò¹®ÀÚ¿Í ¼ıÀÚ·Î ±¸¼ºÇÏ¼¼¿ä");
		} else {
			// DB¿¬µ¿ Áßº¹È®ÀÎ
			clientUser.send(ClientProtocol.JOIN_IDDUPLICATE + "|" + txtFieldId.getText());
		}
	}

	// ¾ÆÀÌµğ Áßº¹°Ë»ç½Ã ¼­¹ö·ÎºÎÅÍ ¹Ş´Â true, false È®ÀÎÈÄ Å¬¶óÀÌ¾ğÆ®¿¡°Ô °á°ú¸¦ ¾Ë·ÁÁÜ
	public void alertIdDuplicate(String data) {
		Platform.runLater(() -> {
			boolean duplicate = Boolean.parseBoolean(data); //
			if (duplicate == true) {
				ClientUser.alertDisplay(0, "¾ÆÀÌµğ Áßº¹ °Ë»ç", "Áßº¹µÈ ¾ÆÀÌµğÀÔ´Ï´Ù");
			} else {
				ClientUser.alertDisplay(1, "¾ÆÀÌµğ Áßº¹ °Ë»ç", "»ç¿ë °¡´ÉÇÑ ¾ÆÀÌµğÀÔ´Ï´Ù");
			}
		});
	}

	// ºñ¹øÀ» ¸Â°Ô ÀÔ·ÂÇß´ÂÁö È®ÀÎ
	private boolean pwCheckEquality() {
		boolean pwCheck = false;
		String pw1 = pwField1.getText();
		String pw2 = pwField2.getText();

		if (pw1.length() < pwMinLength || pw2.length() < pwMinLength || pw1.length() > pwMaxLength || pw2.length() > pwMaxLength) {
			ClientUser.alertDisplay(0, "ºñ¹Ğ¹øÈ£ È®ÀÎ", "»ç¿ëÇÒ ¼ö ¾ø´Â ºñ¹Ğ¹øÈ£ Çü½ÄÀÔ´Ï´Ù", "ºñ¹Ğ¹øÈ£´Â 4~10ÀÚ »çÀÌ·Î ÀÔ·ÂÇØÁÖ¼¼¿ä");
		} else {
			if (!pw1.equals(pw2)) {
				ClientUser.alertDisplay(0, "ºñ¹Ğ¹øÈ£ È®ÀÎ", "ºñ¹Ğ¹øÈ£°¡ ÀÏÄ¡ÇÏÁö ¾Ê½À´Ï´Ù");
			} else {
				ClientUser.alertDisplay(1, "ºñ¹Ğ¹øÈ£ È®ÀÎ", "ºñ¹Ğ¹øÈ£°¡ ÀÏÄ¡ÇÕ´Ï´Ù");
				pwCheck = true;
			}

		}
		return pwCheck;
	}

	// ´Ğ³×ÀÓ Áßº¹È®ÀÎ
	private void NameDuplicateCheck() {
		String name = nickname.getText();

		if (name.equals("") || name.length() < nameMinLength || name.length() > nameMaxLength
				|| !name.matches("^[0-9a-zA-Z°¡-ÆR]*$")) {
			ClientUser.alertDisplay(0, "´Ğ³×ÀÓ Áßº¹È®ÀÎ", "»ç¿ëÇÒ ¼ö ¾ø´Â ´Ğ³×ÀÓ Çü½ÄÀÔ´Ï´Ù", "1~8ÀÚ ÀÌ³»ÀÇ ÇÑ±Û,¿µ¹®,¼ıÀÚ·Î ±¸¼ºÇÏ¼¼¿ä");
		} else {
			// DB¿¬µ¿ Áßº¹È®ÀÎ
			clientUser.send(ClientProtocol.JOIN_NICKDUPLICATE + "|" + nickname.getText());
		}
	}
	
	public void alertNickDuplicate(String data) {
		Platform.runLater(() -> {
			boolean duplicate = Boolean.parseBoolean(data); //
			if (duplicate == true) {
				ClientUser.alertDisplay(0, "´Ğ³×ÀÓ Áßº¹ °Ë»ç", "Áßº¹µÈ ´Ğ³×ÀÓÀÔ´Ï´Ù");
			} else {
				ClientUser.alertDisplay(1, "´Ğ³×ÀÓ Áßº¹ °Ë»ç", "»ç¿ë °¡´ÉÇÑ ´Ğ³×ÀÓÀÔ´Ï´Ù");
			}
		});
		
	}

	// È¸¿ø°¡ÀÔ ok ¹öÆ° ´©¸¦¶§ ÀÔ·ÂÇÑ µ¥ÀÌÅÍ°¡ Çü½Ä¿¡ ¸Â´ÂÁö È®ÀÎ
	private boolean confirmAllFormat() {
		boolean result = true;
		// ¾ÆÀÌµğ È®ÀÎ
		boolean idConfirm = false;
		String id = txtFieldId.getText();

		if (id.equals("") || id.length() < idMinLength || id.length() > idMaxLength || !id.matches("^[0-9a-z]*$")) {
			idConfirm = false;
		} else {
			idConfirm = true;
		}

		// ºñ¹ø È®ÀÎ
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

		// ´Ğ³×ÀÓ È®ÀÎ
		boolean nameConfirm = false;
		String name = nickname.getText();

		if (name.equals("") || name.length() < nameMinLength || name.length() > nameMaxLength
				|| !name.matches("^[0-9a-zA-Z°¡-ÆR]*$")) {
			nameConfirm = false;
		} else {
			// DB¿¬µ¿ Áßº¹È®ÀÎ

			nameConfirm = true;
		}

		// È¸¿ø°¡ÀÔ °¡´É¿©ºÎ¸¦ ¾Ë·ÁÁÜ
		if (!idConfirm || !pwConfirm || !nameConfirm) {
			ClientUser.alertDisplay(0, "È¸¿ø°¡ÀÔ ¿À·ù", "¾ÆÀÌµğ, ºñ¹Ğ¹øÈ£, ´Ğ³×ÀÓÀ» È®ÀÎÇÏ¼¼¿ä", "Çü½Ä¿¡ ¸Â°Ô Àû¾îÁÖ¼¼¿ä");
			result = false;
		}
		return result;
	}

	// È¸¿ø°¡ÀÔ ok ´­·¶À»¶§
	private void joinRequest() {
		// ÀÔ·ÂÇÑ µ¥ÀÌÅÍ°¡ Á¶°Ç¿¡ ¸Â´ÂÁö È®ÀÎÇÏ´Â ÇÔ¼ö(¾ÆÀÌµğ, ÆĞ½º¿öµå, ´Ğ³×ÀÓ Çü½Ä). ¸ÂÀ¸¸é true¹İÈ¯.
		boolean result = false;
		result = confirmAllFormat();
		if (result == true) {
			clientUser.send(ClientProtocol.JOIN_REQUEST + "|" + txtFieldId.getText() + "|" + pwField1.getText() + "|"
					+ nickname.getText());
		}
	}

	// È¸¿ø°¡ÀÔ ½ÂÀÎ ¸®½Ãºê¸¦ ¹Ş°í³ª¼­ ½ÇÇà
	public void alertJoinResult(String data) {
		Platform.runLater(() -> {
			if (Boolean.parseBoolean(data) == true) {
				ClientUser.alertDisplay(1, "È¸¿ø°¡ÀÔ ¼º°ø", "°¡ÀÔÀÌ ¿Ï·áµÇ¾ú½À´Ï´Ù");
				primaryStage.close();
			} else {
				ClientUser.alertDisplay(0, "È¸¿ø°¡ÀÔ ½ÇÆĞ", "°¡ÀÔ¿äÃ»ÀÌ °ÅÀıµÇ¾ú½À´Ï´Ù", "¼­¹ö»óÅÂ¸¦ È®ÀÎÇØÁÖ¼¼¿ä");
			}

		});
	}

	// ·Î±×ÀÎÈ­¸é¿¡¼­ ÀÌ ÄÁÆ®·Ñ·¯¸¦ ºÎ¸£±âÀ§ÇÑ ½ºÅ×ÀÌÁö ¼Â ÇÔ¼ö
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

}
