package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.RankVO;

public class RankHistoryDAO {
	Connection con = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	
	// 아이디로 db에서 랭크이력 가져오기
	public ArrayList<RankVO> getGameRoomList(String nickname) {
		ArrayList<RankVO> arrRankVO = new ArrayList<RankVO>();
		
		try {
			String dml ="select nickname, rp, victory, defeat, totalgame from rankhistroy where nickname = ? order by sysdate desc limit 7";
			
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			pstmt.setString(1, nickname);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				RankVO rVO = new RankVO(rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getInt(4),
						rs.getInt(5));
				arrRankVO.add(rVO);
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
		return arrRankVO;
	}

}
