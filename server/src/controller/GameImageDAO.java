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

	// �����̹��� db���̺��� �̹������ϸ�� �ش� �̹����� ������ǥ�� �޾ƿ�
	public ArrayList<GameImage> getGameImageArray() {
		ArrayList<GameImage> gi = new ArrayList<GameImage>();
		try {
			// �������� 1���� ī��θ�Ƽ�� �����´�
			String dml = "select fileName, coordinates from gameimage order by rand() limit 5";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			rs = pstmt.executeQuery();
			// �����̹��� ��ü�� ������ ī��θ�Ƽ������ ������ list���·� �����Ѵ�
			while (rs.next()) {
				GameImage g = new GameImage(rs.getString(1), rs.getString(2));
				gi.add(g);
				System.out.println("�̹����� db���� �����ɴϴ�"+g.getFileName());
			}

		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

		return gi;
	}
	
	

}
