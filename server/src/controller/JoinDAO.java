package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.JoinVO;

public class JoinDAO {
	Connection con;
	PreparedStatement pstmt;
	ResultSet rs;

	// 회원가입>신규회원 데이터베이스에 등록
	public int insertNewMemberIntoDB(String id, String pw, String nickname) {
		// 멤버십테이블 삽입
		int count = 0;
		try {
			String dml = "insert into membership values" + "(?, ?, ?)";
			// DBUtil로 데이터베이스연결
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml); // 보안을 위해 사용
			pstmt.setString(1, id);
			pstmt.setString(2, pw);
			pstmt.setString(3, nickname);
			// SQL문을 수행후 처리 결과를 얻어옴
			count = pstmt.executeUpdate();
			if (count > 0) {
				System.out.println("멤버쉽테이블 insert성공");
			}

			dml = "insert into rank values" + "(?, ?, ?, ?, ?, ?)";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml); // 보안을 위해 사용
			pstmt.setString(1, id);
			pstmt.setString(2, nickname);
			pstmt.setInt(3, 500);
			pstmt.setInt(4, 0);
			pstmt.setInt(5, 0);
			pstmt.setInt(6, 0);
			count = count + pstmt.executeUpdate();
			if (count > 1) {
				System.out.println("랭크테이블 insert성공");
			}

			dml = "insert into cointbl values" + "(?, ?)";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml); // 보안을 위해 사용
			pstmt.setString(1, id);
			pstmt.setInt(2, 500);
			count = count + pstmt.executeUpdate();
			if (count > 2) {
				System.out.println("코인테이블 insert성공");
			}

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

	// 회원가입>아이디 중복검사 체크
	public boolean getIdCheckDuplicate(String data) {
		boolean duplicate = false; // 중복시 true리턴

		String dml = "select * from membership where id = ?";

		int count = 0;
		try {
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			pstmt.setString(1, data);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				duplicate = true;
			}
		} catch (SQLException se) {
			System.out.println(se);
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException se) {
			}
		}

		return duplicate;
	}

	// 회원가입>닉네임 중복검사체크
	public boolean getNickCheckDuplicate(String data) {
		boolean duplicate = false; // 중복시 true리턴

		String dml = "select * from membership where nickname = ?";

		try {
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			pstmt.setString(1, data);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				duplicate = true;
			}
		} catch (SQLException se) {
			System.out.println(se);
		} catch (Exception e) {
			System.out.println(e);
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

		return duplicate;
	}

	// 로그인 아이디 비밀번호 체크
	public boolean getCheckIdPw(String id, String pw) {
		boolean checkIdPw = false;
		String dml = "select * from membership where id = ? and pw = ?";

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			pstmt.setString(1, id);
			pstmt.setString(2, pw);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				checkIdPw = true;
			}
		} catch (SQLException se) {
			System.out.println(se);
		} catch (Exception e) {
			System.out.println(e);
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
		return checkIdPw;
	}

	// 메인페이지 로딩시 필요한 유저데이터(아이디, 비번, 닉네임) 모두 가져오기
	public JoinVO getJoinUserData(String id) {
		JoinVO jvo = new JoinVO();
		String dml = "select * from membership where id = ?";

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			pstmt.setString(1, id);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				jvo.setId(rs.getString(1));
				jvo.setPw(rs.getString(2));
				jvo.setNickname(rs.getString(3));
			}
		} catch (SQLException se) {
			System.out.println(se);
		} catch (Exception e) {
			System.out.println(e);
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
		return jvo;
	}

	// 아이디로 닉네임얻기
	public String getNickname(String id) {
		String nickname = null;

		String dml = "select nickname from membership where id = ?";

		try {
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			pstmt.setString(1, id);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				nickname = rs.getString(1);
			}
		} catch (SQLException se) {
			System.out.println(se);
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException se) {
			}
		}

		return nickname;
	}

	// 닉넴으로아이디얻기
	public String getUserId(String nickname) {
		String id = null;

		String dml = "select id from membership where nickname = ?";

		try {
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			pstmt.setString(1, nickname);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				id = rs.getString(1);
			}
		} catch (SQLException se) {
			System.out.println(se);
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException se) {
			}
		}

		return id;
	}
	//닉네임 변경
	public int nickNameChage(String nick, String id) {
		int count = 0;

		try {
			String dml = "update membership set nickname = ? where id = ?";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml); // 보안을 위해 사용
			pstmt.setString(1, nick);
			pstmt.setString(2, id);
			// SQL문을 수행후 처리 결과를 얻어옴
			count = pstmt.executeUpdate();
			if (count > 0) {
				System.out.println("닉네임 변경 성공");
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return count;

	}
	//멤버 탈퇴
	public int memberWithdraw(String id) {
		int count = 0;

		try {
			String dml = "delete from membership where id = ?";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml); // 보안을 위해 사용
			pstmt.setString(1, id);
			// SQL문을 수행후 처리 결과를 얻어옴
			count = pstmt.executeUpdate();
			if (count > 0) {
				System.out.println("탈퇴 성공");
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	public int changeUserPassword(String id, String pw) {
		int count = 0;

		try {
			String dml = "update membership set pw = ? where id = ?";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml); // 보안을 위해 사용
			pstmt.setString(1, pw);
			pstmt.setString(2, id);
			// SQL문을 수행후 처리 결과를 얻어옴
			count = pstmt.executeUpdate();
			if (count > 0) {
				System.out.println("비번 변경 성공");
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return count;
		
	}

}
