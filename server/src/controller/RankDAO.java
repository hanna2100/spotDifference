package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.RankVO;

public class RankDAO {
	Connection con = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;

	// 랭크리스트 가져오기
	public ArrayList<RankVO> getRankList() {
		ArrayList<RankVO> rvoList = new ArrayList<RankVO>();

		try {
			String dml = "select nickname, rp, victory, defeat, totalGame from rank order by rp desc limit 30";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				RankVO rVO = new RankVO(rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5));
				rvoList.add(rVO);
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException se) {
			}
		}
		return rvoList;
	}

}
