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

	// ȸ������>�ű�ȸ�� �����ͺ��̽��� ���
	public int insertNewMemberIntoDB(String id, String pw, String nickname) {
		// ��������̺� ����
		int count = 0;
		try {
			String dml = "insert into membership values" + "(?, ?, ?)";
			// DBUtil�� �����ͺ��̽�����
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml); // ������ ���� ���
			pstmt.setString(1, id);
			pstmt.setString(2, pw);
			pstmt.setString(3, nickname);
			// SQL���� ������ ó�� ����� ����
			count = pstmt.executeUpdate();
			if (count > 0) {
				System.out.println("��������̺� insert����");
			}

			dml = "insert into rank values" + "(?, ?, ?, ?, ?, ?)";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml); // ������ ���� ���
			pstmt.setString(1, id);
			pstmt.setString(2, nickname);
			pstmt.setInt(3, 500);
			pstmt.setInt(4, 0);
			pstmt.setInt(5, 0);
			pstmt.setInt(6, 0);
			count = count + pstmt.executeUpdate();
			if (count > 1) {
				System.out.println("��ũ���̺� insert����");
			}

			dml = "insert into cointbl values" + "(?, ?)";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml); // ������ ���� ���
			pstmt.setString(1, id);
			pstmt.setInt(2, 500);
			count = count + pstmt.executeUpdate();
			if (count > 2) {
				System.out.println("�������̺� insert����");
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// �����ͺ��̽����� ���ῡ ���Ǿ��� ������Ʈ�� ����
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
			}
		}

		return count;

	}

	// ȸ������>���̵� �ߺ��˻� üũ
	public boolean getIdCheckDuplicate(String data) {
		boolean duplicate = false; // �ߺ��� true����

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

	// ȸ������>�г��� �ߺ��˻�üũ
	public boolean getNickCheckDuplicate(String data) {
		boolean duplicate = false; // �ߺ��� true����

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

	// �α��� ���̵� ��й�ȣ üũ
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

	// ���������� �ε��� �ʿ��� ����������(���̵�, ���, �г���) ��� ��������
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

	// ���̵�� �г��Ӿ��
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

	// �г����ξ��̵���
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
	//�г��� ����
	public int nickNameChage(String nick, String id) {
		int count = 0;

		try {
			String dml = "update membership set nickname = ? where id = ?";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml); // ������ ���� ���
			pstmt.setString(1, nick);
			pstmt.setString(2, id);
			// SQL���� ������ ó�� ����� ����
			count = pstmt.executeUpdate();
			if (count > 0) {
				System.out.println("�г��� ���� ����");
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return count;

	}
	//��� Ż��
	public int memberWithdraw(String id) {
		int count = 0;

		try {
			String dml = "delete from membership where id = ?";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml); // ������ ���� ���
			pstmt.setString(1, id);
			// SQL���� ������ ó�� ����� ����
			count = pstmt.executeUpdate();
			if (count > 0) {
				System.out.println("Ż�� ����");
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
			pstmt = con.prepareStatement(dml); // ������ ���� ���
			pstmt.setString(1, pw);
			pstmt.setString(2, id);
			// SQL���� ������ ó�� ����� ����
			count = pstmt.executeUpdate();
			if (count > 0) {
				System.out.println("��� ���� ����");
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return count;
		
	}

}
