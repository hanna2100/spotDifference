package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.GameRoomVO;
import model.RankVO;

public class GameRoomDAO {
	Connection con = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;

	// DB�� �ش������ ���� �����ϴ� �Լ�
	public int insertIntoGameRoomData(String id, String roomName, String roomRock, String roomPw) {
		int count = 0;
		try {
			// �⺻Űid�� ��ũ���̺��� rp�����͸� �ҷ���
			String dml1 = "select rp from rank where id = ?";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml1);
			pstmt.setString(1, id);
			rs = pstmt.executeQuery();
			int rp = 0;
			if (rs != null) {
				if (rs.next()) {
					rp = rs.getInt(1);
				}
			}

			// �⺻Űid�� ��������̺��� �г��ӵ����͸� �ҷ���
			String dml2 = "select nickname from membership where id = ?";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml2);
			pstmt.setString(1, id);
			String host = null;
			rs = pstmt.executeQuery();
			if (rs.next()) {
				host = rs.getString(1);
			}

			String dml3 = "insert into gameroom values" + "(?, ?, ?, ?, ?, ?, ?)";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml3);
			pstmt.setInt(1, rp);
			pstmt.setString(2, roomName);
			pstmt.setString(3, host);
			pstmt.setString(4, " ");
			pstmt.setString(5, "�����");
			pstmt.setString(6, roomRock);
			pstmt.setString(7, roomPw);
			// SQL���� ������ ó�� ����� ����
			count = pstmt.executeUpdate();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				// �����ͺ��̽����� ���ῡ ���Ǿ��� ������Ʈ�� ����
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
			}
		}
		return count;
	}

	// DB�� ���ӷ����̺� ������ ������ (Ŭ���̾�Ʈ ���ӹ� ���̺�信 ��� ����. �׷��Ƿ� ��й�ȣ�����ʹ� ������������)
	public ArrayList<GameRoomVO> getGameRoomList() {
		ArrayList<GameRoomVO> grvoList = new ArrayList<GameRoomVO>();

		try {
			String dml = "select rp, roomName, host, state, roomRock from gameroom";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				GameRoomVO grVO = new GameRoomVO(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
						rs.getString(5));
				grvoList.add(grVO);
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
		return grvoList;
	}
	
	// �Խ�Ʈ�� ���ö� ����г������� db��ȸ�ؼ� ������(Ǯ��)���θ� ��ȸ & '������'���� db���º���
	public int guestEnterPrivateRoom(String hostName, String guestName, String pw) {
		int result = 0;
		try {
			String dml = "select state, roomPw from gameroom where host = ?";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			pstmt.setString(1, hostName);
			rs = pstmt.executeQuery();

			String dbState = null; // db���� ���� �����߿��� (Ǯ�濩��)
			String dbPw = null; // db���� ���� �н�����
			if (rs.next()) {
				dbState = rs.getString(1);
				dbPw = rs.getString(2);
			}

			if (dbState.equals("�����") && dbPw.equals(pw)) { // Ǯ���� �ƴϰ� ����� �������
				result = 1; // �� ����
				// db�� �ش� ���ӹ� ���¸� ���������� �ٲٱ�
				String dml2 = "update gameroom set state = '������' , guest = ? where host = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml2);
				pstmt.setString(1, guestName);
				pstmt.setString(2, hostName);
				int count = pstmt.executeUpdate();
				if (count != 1) {
					result = 0; // ���������� ������Ʈ�� �ȵǸ� 0�� �����ؼ� ����ڰ� �濡 ������ ��
				}
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				// �����ͺ��̽����� ���ῡ ���Ǿ��� ������Ʈ�� ����
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
			}
		}
		return result;
	}

	public int guestEnterPublicRoom(String hostName, String guestName) {
		int result = 0;
		try {
			String dml = "select state from gameroom where host = ?";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			pstmt.setString(1, hostName);
			rs = pstmt.executeQuery();

			String dbState = null; // db���� ���� �����߿��� (Ǯ�濩��)
			if (rs.next()) {
				dbState = rs.getString(1);
			}

			if (dbState.equals("�����")) { // Ǯ���� �ƴ� ��
				// db�� �ش� ���ӹ� ���¸� ���������� �ٲٱ�
				String dml2 = "update gameroom set state = '������' , guest = ? where host = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml2);
				pstmt.setString(1, guestName);
				pstmt.setString(2, hostName);
				int count = pstmt.executeUpdate();
				if (count == 0) {
					result = 0; // ���������� ������Ʈ�� �ȵǸ� 0�� �����ؼ� ����ڰ� �濡 ������ ��
				} else {
					result = 1;
				}
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				// �����ͺ��̽����� ���ῡ ���Ǿ��� ������Ʈ�� ����
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
			}
		}
		return result;
	}

	// ȣ��Ʈ�г������� ȣ��Ʈ��,�Խ�Ʈ�� ��ȸ�ؼ� �迭�� �����ϱ�
	public String[] getAllNickname(String hostName) {
		String[] nickList = null;
		try {
			String dml = "select host, guest from gameroom where host = ?";

			con = DBUtil.getConnection();

			pstmt = con.prepareStatement(dml);
			pstmt.setString(1, hostName);
			rs = pstmt.executeQuery();

			String host = null;
			String guest = null;
			if (rs.next()) {
				host = rs.getString(1);
				guest = rs.getString(2);
			}
			
			nickList = new String[] { host, guest };

		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				// �����ͺ��̽����� ���ῡ ���Ǿ��� ������Ʈ�� ����
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
			}
		}

		return nickList;
	}

	// rp ������Ʈ,
	public int[] gameRpUpdate(String hostId, String guestId, boolean hostWin, String hostScr, String guestScr) {

		int[] newRp = null;

		try {
			//db�� ��ϵ� ���� rp (���߿� ��Ʈ�� ����ȯ)
			double finalHostRp = 0;
			double finalGuestRp = 0;
			//�������� rp
			int oldHostRp = 0;
			int oldGuestRp = 0;
			//�������� ��ԵǴ� rp (���� ����� -)
			int hostGetRp = 0;
			int guestGetRp = 0;
			////////////////////////////////////////////////////////////////////////
			// ȣ��Ʈ rp���ϱ�
			String dml = "select rp from rank where id = ?";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			pstmt.setString(1, hostId);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				oldHostRp = rs.getInt(1);
			}
			// �Խ�Ʈrp���ϱ�
			dml = "select rp from rank where id = ?";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			pstmt.setString(1, guestId);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				oldGuestRp = rs.getInt(1);
			}
			////////////////////////////////////////////////////////////////////////
			// �� �÷��̾�� �Ƿ����̸� ���밪���� ȯ��
			int remainder = oldHostRp - oldGuestRp;
			if (remainder < 0) {
				remainder = ~remainder + 1; 
			} else if(remainder <9) {	// �Ƿ����� 9���� ������ ���Ƿ� 10�� �� (���� ����Ҵ� rp�� �ʹ� �۱⶧��)
				remainder = 10;
			} else if (remainder >=50) {	// �Ƿ����� 50�̻��̸� ���Ƿ� 49�� �� 
				remainder = 49;
			}
			if (hostWin) { // ȣ��Ʈ�� �̱���
				if (oldHostRp > oldGuestRp) { // ȣ��Ʈ�� �Խ�Ʈ���� ���Ұ�� (���ϴ� ����� ���ϴ»�� �̱����̽�)
					hostGetRp = 50 - remainder; // 50���� �Ƿ�����ŭ ������ ���� ����. (�Ƿ����� �ִ� 49�ϱ� �̷���� �̰ܵ� ��ԵǴ� rp��1)
					guestGetRp = -hostGetRp;// ������� ȣ��Ʈ�� ���� rp��ŭ ������
				} else if(oldHostRp == oldGuestRp) {//ȣ��Ʈ�� �Խ��� rp�� �������
					hostGetRp = 10;
					guestGetRp = -10;
				}
				else { // ȣ��Ʈ�� �Խ�Ʈ���� ���Ұ�� (���ϴ� ����� ���ϴ»�� �̱� ���̽�)
					hostGetRp = remainder; //ȣ��Ʈ�� �Ƿ�����ŭ rp ����
					guestGetRp = -remainder;//�Խ�Ʈ�� �׸�ŭ ����
				}
			} else { // �Խ�Ʈ�� �̱���
				if (oldHostRp > oldGuestRp) { //���ϴ»���� ���ϴ»�� �̱� ���̽�
					hostGetRp = -remainder;	//ȣ��Ʈ�� �Ƿ�����ŭ rp�� �Ұ�
					guestGetRp = remainder;	//�Խ�Ʈ�� �Ƿ�����ŭ ����
				}else if(oldHostRp == oldGuestRp) {
					hostGetRp = -10;
					guestGetRp = 10;
				} else { // ���ϴ»���� ���ϴ»�� �̱� ���̽�
					guestGetRp = 50-remainder;
					hostGetRp = -guestGetRp;//
				}
			}
			
			int hostScore = Integer.parseInt(hostScr);
			int guestScore = Integer.parseInt(guestScr);
			
			// ���ӿ��� (�����-Ʋ����)�� Ŭ���� �߰����� ��
			finalHostRp = hostGetRp * (((double)hostScore / 50) + 1);
			finalGuestRp = guestGetRp * (((double)guestScore / 50) + 1);

			newRp = new int[] { (int)finalHostRp, (int)finalGuestRp, oldHostRp+(int)finalHostRp, oldGuestRp+(int)finalGuestRp};
			//[����ȣ��Ʈrp, �����Խ�Ʈrp, db��ϵ� ȣ��Ʈrp, db��ϵ� �Խ�Ʈrp]


		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// �����ͺ��̽����� ���ῡ ���Ǿ��� ������Ʈ�� ����
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
			}
		}

		return newRp;
	}

	// ���Ӱ�� ������Ʈ
	public void gameScoreUpdate(int newHostRp, int newGuestRp, String hostId, String guestId, boolean hostWin) {

		
		try {
			if (hostWin) {
				int oldHostVictory = 0;
				int oldHostTotal = 0;
				
				String dml = "";
				// ȣ��Ʈ ���� �õ嵥���� ã��
				dml = "select victory, totalgame from rank where id = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml);
				pstmt.setString(1, hostId);
				rs = pstmt.executeQuery();

				if (rs.next()) {
					oldHostVictory = rs.getInt(1);
					oldHostTotal = rs.getInt(2);
				}
				
				dml = "update rank set rp = ?, victory = ?, totalgame = ? where id = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml);
				pstmt.setInt(1, newHostRp);
				pstmt.setInt(2, oldHostVictory+1);
				pstmt.setInt(3, oldHostTotal+1);
				pstmt.setString(4, hostId);
				int count = pstmt.executeUpdate();

				if (count == 1)
					System.out.println("ȣ��Ʈ�� RP, ����Ƚ��, �¼��� ������Ʈ �Ǿ����ϴ�");

				// �Խ�Ʈ �õ嵥���� ��������
				
				int oldGuestDefeat = 0;
				int oldGuestTotal = 0;
				String dml2 = "";
				
				dml2 = "select defeat, totalgame from rank where id = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml2);
				pstmt.setString(1, guestId);
				rs = pstmt.executeQuery();

				if (rs.next()) {
					oldGuestDefeat = rs.getInt(1);
					oldGuestTotal = rs.getInt(2);
				}
				
				dml2 = "update rank set rp = ?, defeat = ?, totalgame = ? where id = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml2);
				pstmt.setInt(1, newGuestRp);
				pstmt.setInt(2, oldGuestDefeat+1);
				pstmt.setInt(3, oldGuestTotal+1);
				pstmt.setString(4, guestId);
				count = pstmt.executeUpdate();

				if (count == 1)
					System.out.println("�Խ�Ʈ�� RP, ����Ƚ��, �м��� ������Ʈ �Ǿ����ϴ�");

			} else {
				//ȣ��Ʈ������������
				int oldHostDefeat = 0;
				int oldHostTotal = 0;
				String dml = "";
				// ȣ��Ʈ �õ嵥���� ��������
				dml = "select defeat, totalgame from rank where id = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml);
				pstmt.setString(1, hostId);
				rs = pstmt.executeQuery();

				if (rs.next()) {
					oldHostDefeat = rs.getInt(1);
					oldHostTotal = rs.getInt(2);
				}
				
				dml = "update rank set rp = ? , defeat = ?, totalgame = ? where id = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml);
				pstmt.setInt(1, newHostRp);
				pstmt.setInt(2, oldHostDefeat+1);
				pstmt.setInt(3, oldHostTotal+1);
				pstmt.setString(4, hostId);
				int count = pstmt.executeUpdate();

				if (count == 1)
					System.out.println("ȣ��Ʈ�� RP ����Ƚ��, �¼��� ������Ʈ �Ǿ����ϴ�");

				// �Խ�Ʈ �¼� ������Ʈ
				int oldGuestVictory = 0;
				int oldGuestTotal = 0;
				String dml2="";
				dml2 = "select victory, totalgame from rank where id = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml2);
				pstmt.setString(1, guestId);
				rs = pstmt.executeQuery();

				if (rs.next()) {
					oldGuestVictory = rs.getInt(1);
					oldGuestTotal = rs.getInt(2);
				}
				dml2 = "update rank set rp = ?, victory = ?, totalgame = ? where id = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml2);
				pstmt.setInt(1, newGuestRp);
				pstmt.setInt(2, oldGuestVictory+1);
				pstmt.setInt(3, oldGuestTotal+1);
				pstmt.setString(4, guestId);
				count = pstmt.executeUpdate();

				if (count == 1)
					System.out.println("�Խ�Ʈ�� RP, ����Ƚ��, �м��� ������Ʈ �Ǿ����ϴ�");

			} // end of hostWin
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				// �����ͺ��̽����� ���ῡ ���Ǿ��� ������Ʈ�� ����
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
			}
		}

	}

	// ���� ������Ʈ (�̱���+100, �����+50)
	public void gameCoinUpdate(String hostId, String guestId, boolean hostWin) {
		String dml;
		try {
			if (hostWin) {
				// ȣ��Ʈ ���� ����
				int oldHostCoin = 0;
				dml = "select coin from cointbl where id = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml);
				pstmt.setString(1, hostId);
				rs = pstmt.executeQuery();

				if (rs.next()) {
					oldHostCoin = rs.getInt(1);
				}
				
				dml = "update cointbl set coin = ? where id = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml);
				pstmt.setInt(1, oldHostCoin+100);
				pstmt.setString(2, hostId);
				int count = pstmt.executeUpdate();

				if (count == 1)
					System.out.println("ȣ��Ʈ�� ������ +100 ������Ʈ �Ǿ����ϴ�");

				// �Խ�Ʈ ���� ����
				int oldGuestCoin = 0;
				dml = "select coin from cointbl where id = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml);
				pstmt.setString(1, guestId);
				rs = pstmt.executeQuery();

				if (rs.next()) {
					oldGuestCoin = rs.getInt(1);
				}
				
				dml = "update cointbl set coin = ? where id = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml);
				pstmt.setInt(1, oldGuestCoin+50);
				pstmt.setString(2, guestId);
				count = pstmt.executeUpdate();

				if (count == 1)
					System.out.println("�Խ�Ʈ�� ������ +50 ������Ʈ �Ǿ����ϴ�");

			} else {
				// ȣ��Ʈ ���� ����
				int oldHostCoin = 0;
				dml = "select coin from cointbl where id = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml);
				pstmt.setString(1, hostId);
				rs = pstmt.executeQuery();

				if (rs.next()) {
					oldHostCoin = rs.getInt(1);
				}
				
				dml = "update cointbl set coin = ? where id = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml);
				pstmt.setInt(1, oldHostCoin+50);
				pstmt.setString(2, hostId);
				int count = pstmt.executeUpdate();

				if (count == 1)
					System.out.println("ȣ��Ʈ�� ������ +50 ������Ʈ �Ǿ����ϴ�");

				// �Խ�Ʈ ���� ����
				int oldGuestCoin = 0;
				dml = "select coin from cointbl where id = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml);
				pstmt.setString(1, guestId);
				rs = pstmt.executeQuery();

				if (rs.next()) {
					oldGuestCoin = rs.getInt(1);
				}
				
				dml = "update cointbl set coin = ? where id = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml);
				pstmt.setInt(1, oldGuestCoin+100);
				pstmt.setString(2, guestId);
				count = pstmt.executeUpdate();

				if (count == 1)
					System.out.println("�Խ�Ʈ�� ������ +100 ������Ʈ �Ǿ����ϴ�");
			} // end of hostWin
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// �����ͺ��̽����� ���ῡ ���Ǿ��� ������Ʈ�� ����
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
			}
		}

	}

	public void deleteFromGameRoomTbl(String hostNickname) {
		try {
			String dml = "delete from gameroom where host = ?";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			pstmt.setString(1, hostNickname);
			int count = pstmt.executeUpdate();

			if (count == 1)
				System.out.println(hostNickname + "�� ���ӷ����̺��� �������ϴ�");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// �����ͺ��̽����� ���ῡ ���Ǿ��� ������Ʈ�� ����
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}// end of delete~tbl

	public void guestExitGameRoom(String hostNickname) {

		String dml;
		try {
			dml = "update gameroom set state = '�����' , guest = ? where host = ?";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			pstmt.setString(1, null);
			pstmt.setString(2, hostNickname);
			int count = pstmt.executeUpdate();
			if (count == 0) {
				System.out.println("�Խ�Ʈ�� ���������� �濡�� ���������ϴ�");
			} else {

			}
		} catch (ClassNotFoundException | SQLException e) {
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

	}

}