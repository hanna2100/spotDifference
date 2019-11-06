package controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class AddImgController implements Initializable {
	@FXML
	private ImageView imageView1;
	@FXML
	private ImageView imageView2;
	@FXML
	private Pane pane;
	@FXML
	private Canvas canvas;
	@FXML
	private Button btnImg1;
	@FXML
	private Button btnImg2;
	@FXML
	private Button btnExit;
	@FXML
	private Button btnSelect;
	@FXML
	private Button btnSend;
	@FXML
	private Button btnRemove;
	@FXML
	private TextField cd1;
	@FXML
	private TextField cd2;
	@FXML
	private TextField cd3;
	@FXML
	private TextField cd4;
	@FXML
	private TextField cd5;

	private Stage primaryStage;

	private File imgFile1 = null; // 선택된 이미지 파일1
	private String localUrl1 = null; // 이미지1 파일 경로
	private Image localImage1; // 보여주기 위한 이미지1객체
	private String selectedImg1; // db저장할 이미지 파일명1
	private File dirSave = new File("C:/gameImage"); // 이미지 저장위치

	private File imgFile2 = null; // 선택된 이미지 파일2
	private String localUrl2 = null; // 이미지2 파일 경로
	private Image localImage2; // 보여주기 위한 이미지2객체
	private String selectedImg2; // db저장할 이미지 파일명2

	private Coordinate admin = new Coordinate(); // 사용자의 마우스 좌표값
	private int count = 0; // 정답좌표 클릭한 수
	private GraphicsContext gc = null; // 동그라미 체크
	private boolean selectActivate = false;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 이미지 디폴트
		imageView1.setImage(new Image(getClass().getResource("/view/default.jpg").toString()));
		imageView2.setImage(new Image(getClass().getResource("/view/default.jpg").toString()));

		btnImg2.setDisable(true);
		btnSelect.setDisable(true);
		btnSend.setDisable(true);
		btnRemove.setDisable(true);
		
		btnExit.setOnAction(event -> {
			primaryStage.close();
		});

		btnImg1.setOnAction(event -> {
			handlerBtnImageFileAction1();
		});

		btnImg2.setOnAction(event -> {
			handlerBtnImageFileAction2();
		});

		btnSend.setOnAction(event -> {

			handlerBtnSendAction();
			String coordinates = cd1.getText() + "," + cd2.getText() + "," + cd3.getText() + "," + cd4.getText() + ","
					+ cd5.getText();
			int count = sendToDatabase(selectedImg1 + "|" + selectedImg2, coordinates);
			if (count != 0) {
				alertDisplay(1, "등록 완료", "DB에 등록이 완료되었습니다");
			} else {
				alertDisplay(0, "등록 실패", "DB에 등록할 수 없습니다");
			}
			initParameterAndButton();
		});

		btnSelect.setOnAction(event -> {
			Platform.runLater(() -> {
				selectActivate = true;
			});
		});
		// 클릭시 동그라미 이미지+좌표띄우기
		pane.setOnMouseClicked(event -> {
			if (selectActivate) {
				btnRemove.setDisable(false);
				// 관리자의 마우스 좌표 얻기
				if (count <= 4 && count >= 0) {
					admin.setX((int) event.getX());
					admin.setY((int) event.getY());
					count++;
					drawCircle(admin, canvas);
				}

				switch (count) {

				case 1:
					cd1.setText(admin.toString());
					break;
				case 2:
					cd2.setText(admin.toString());
					break;
				case 3:
					cd3.setText(admin.toString());
					break;
				case 4:
					cd4.setText(admin.toString());
					break;
				case 5:
					cd5.setText(admin.toString());
					btnSend.setDisable(false);
					break;

				}
			}

		});
		// 좌표 삭제버튼
		btnRemove.setOnAction(event -> {
			removeSelectedCircle(count);

		});

	}

	private void initParameterAndButton() {
		count = 0;
		selectActivate = false;

		btnImg2.setDisable(true);
		btnSelect.setDisable(true);
		btnSend.setDisable(true);
		btnRemove.setDisable(true);

		// 동그라미 지우기
		for (int i = 1; i < 6; i++) {
			removeSelectedCircle(i);
		}

		cd1.setText("");
		cd2.setText("");
		cd3.setText("");
		cd4.setText("");
		cd5.setText("");

		// 이미지 디폴트
		imageView1.setImage(new Image(getClass().getResource("/view/default.jpg").toString()));
		imageView2.setImage(new Image(getClass().getResource("/view/default.jpg").toString()));

	}

	private void removeSelectedCircle(int count) {
		Coordinate cd = new Coordinate();
		String getText;
		String[] xy;
		switch (count) {
		case 1:
			getText = cd1.getText();
			xy = getText.split(",");
			cd.setX(Integer.parseInt(xy[0]));
			cd.setY(Integer.parseInt(xy[1]));
			cd1.setText("");
			removeCircle(cd, canvas);
			break;
		case 2:
			getText = cd2.getText();
			xy = getText.split(",");
			cd.setX(Integer.parseInt(xy[0]));
			cd.setY(Integer.parseInt(xy[1]));
			cd2.setText("");
			removeCircle(cd, canvas);
			break;
		case 3:
			getText = cd3.getText();
			xy = getText.split(",");
			cd.setX(Integer.parseInt(xy[0]));
			cd.setY(Integer.parseInt(xy[1]));
			cd3.setText("");
			removeCircle(cd, canvas);
			break;
		case 4:
			getText = cd4.getText();
			xy = getText.split(",");
			cd.setX(Integer.parseInt(xy[0]));
			cd.setY(Integer.parseInt(xy[1]));
			cd4.setText("");
			removeCircle(cd, canvas);
			break;
		case 5:
			getText = cd5.getText();
			xy = getText.split(",");
			cd.setX(Integer.parseInt(xy[0]));
			cd.setY(Integer.parseInt(xy[1]));
			cd5.setText("");
			removeCircle(cd, canvas);
			btnSend.setDisable(true);
			break;
		}
		if (this.count >= 1 && this.count <= 5) {
			this.count--;
		}

	}

	private void handlerBtnImageFileAction1() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Image File", "*.png", "*.jpg", "*.gif"));
		try {
			imgFile1 = fileChooser.showOpenDialog(btnImg1.getScene().getWindow());
			if (imgFile1 != null) {
				// 이미지 파일 경로
				localUrl1 = imgFile1.toURI().toURL().toString();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		localImage1 = new Image(localUrl1, false);
		imageView1.setImage(localImage1);
		btnImg1.setDisable(false);
		btnImg2.setDisable(false);

	}

	private void handlerBtnImageFileAction2() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Image File", "*.png", "*.jpg", "*.gif"));
		try {
			imgFile2 = fileChooser.showOpenDialog(btnImg2.getScene().getWindow());
			if (imgFile2 != null) {
				// 이미지 파일 경로
				localUrl2 = imgFile2.toURI().toURL().toString();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		localImage2 = new Image(localUrl2, false);
		imageView2.setImage(localImage2);
		btnImg2.setDisable(false);
		btnSelect.setDisable(false);

	}

	private synchronized void handlerBtnSendAction() {
		File dirMake1 = new File(dirSave.getAbsolutePath());
		// 이미지 저장 폴더 생성
		if (!dirMake1.exists()) {
			dirMake1.mkdir();
		}

		// 이미지 파일 저장
		selectedImg1 = imageSave(imgFile1, dirSave, System.currentTimeMillis()%1000000000 + "");
		selectedImg2 = imageSave(imgFile2, dirSave, System.currentTimeMillis()%1000000000 + "");
	}

	private synchronized int sendToDatabase(String imageNames, String coordinates) {
		int count = 0;
		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			String dml3 = "insert into gameimage values" + "(?, ?)";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml3);
			pstmt.setString(1, imageNames);
			pstmt.setString(2, coordinates);
			// SQL문을 수행후 처리 결과를 얻어옴
			count = pstmt.executeUpdate();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// 데이터베이스와의 연결에 사용되었던 오브젝트를 해제
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
			}
		}
		return count;

	}

	public String imageSave(File file, File dirSave, String currentTime) {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;

		int numName = Integer.parseInt(currentTime);
		
		int data = -1;
		String fileName = null;
		try {
			// 이미지 파일명 생성
			fileName = "img" +numName+".jpg";
			bis = new BufferedInputStream(new FileInputStream(file));
			bos = new BufferedOutputStream(new FileOutputStream(dirSave.getAbsolutePath() + "/" + fileName));

			// 선택한 이미지 파일 InputStream의 마지막에 이르렀을 경우는 -1
			while ((data = bis.read()) != -1) {
				bos.write(data);
				bos.flush();
			}

		} catch (Exception e) {
			e.getMessage();
		} finally {
			try {
				if (bos != null) {
					bos.close();
				}
				if (bis != null) {
					bis.close();
				}
			} catch (IOException e) {
				e.getMessage();
			}

		}

		return fileName;
	}

	// 동그라미 그림을 그리는 함수.
	public void drawCircle(Coordinate admin, Canvas canvas) {
		gc = canvas.getGraphicsContext2D();
		Image circle = new Image(getClass().getResource("/view/circle.png").toString());
		gc.drawImage(circle, admin.getX() - 25, admin.getY() - 25, 50, 50);

	}

	// 동그라미 지우기
	public void removeCircle(Coordinate admin, Canvas canvas) {
		gc = canvas.getGraphicsContext2D();
		gc.clearRect(admin.getX() - 25, admin.getY() - 25, 50, 50);
	}

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	// 메세지창
	public void alertDisplay(int type, String title, String headerText) {
		Alert alert = null;
		switch (type) {
		case 0:
			alert = new Alert(AlertType.ERROR);
			break;
		case 1:
			alert = new Alert(AlertType.INFORMATION);
			break;
		default:
			break;
		}
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setResizable(false);
		alert.showAndWait();

	}

}
