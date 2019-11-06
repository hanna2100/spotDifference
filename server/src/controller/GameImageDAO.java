package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

public class GameImageDAO {
	Connection con = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;

	// 게임이미지 db테이블에서 이미지파일명과 해당 이미지의 정답좌표를 받아옴
	public ArrayList<GameImage> getGameImageArray() {
		ArrayList<GameImage> gi = new ArrayList<GameImage>();
		try {
			// 랜덤으로 1개의 카디널리티를 가져온다
			String dml = "select fileName, coordinates from gameimage order by rand() limit 5";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			rs = pstmt.executeQuery();
			// 게임이미지 객체에 각각의 카디널리티정보를 저장후 list형태로 리턴한다
			while (rs.next()) {
				GameImage g = new GameImage(rs.getString(1), rs.getString(2));
				gi.add(g);
				System.out.println("이미지를 db에서 가져옵니다"+g.getFileName());
			}

		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

		return gi;
	}
	
	

}
