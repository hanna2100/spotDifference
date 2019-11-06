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

	// DB에 해당게임의 방목록 생성하는 함수
	public int insertIntoGameRoomData(String id, String roomName, String roomRock, String roomPw) {
		int count = 0;
		try {
			// 기본키id로 랭크테이블의 rp데이터를 불러옴
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

			// 기본키id로 멤버쉽테이블의 닉네임데이터를 불러옴
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
			pstmt.setString(5, "대기중");
			pstmt.setString(6, roomRock);
			pstmt.setString(7, roomPw);
			// SQL문을 수행후 처리 결과를 얻어옴
			count = pstmt.executeUpdate();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				// 데이터베이스와의 연결에 사용되었던 오브젝트를 해제
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

	// DB의 게임룸테이블 정보를 가져옴 (클라이언트 게임방 테이블뷰에 띄울 목적. 그러므로 비밀번호데이터는 가져오지않음)
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
	
	// 게스트가 들어올때 방장닉네임으로 db조회해서 게임중(풀방)여부를 조회 & '게임중'으로 db상태변경
	public int guestEnterPrivateRoom(String hostName, String guestName, String pw) {
		int result = 0;
		try {
			String dml = "select state, roomPw from gameroom where host = ?";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			pstmt.setString(1, hostName);
			rs = pstmt.executeQuery();

			String dbState = null; // db에서 얻은 게임중여부 (풀방여부)
			String dbPw = null; // db에서 얻은 패스워드
			if (rs.next()) {
				dbState = rs.getString(1);
				dbPw = rs.getString(2);
			}

			if (dbState.equals("대기중") && dbPw.equals(pw)) { // 풀방이 아니고 비번도 맞을경우
				result = 1; // 참 리턴
				// db에 해당 게임방 상태를 게임중으로 바꾸기
				String dml2 = "update gameroom set state = '게임중' , guest = ? where host = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml2);
				pstmt.setString(1, guestName);
				pstmt.setString(2, hostName);
				int count = pstmt.executeUpdate();
				if (count != 1) {
					result = 0; // 오류가나서 업데이트가 안되면 0을 리턴해서 사용자가 방에 못들어가게 함
				}
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				// 데이터베이스와의 연결에 사용되었던 오브젝트를 해제
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

			String dbState = null; // db에서 얻은 게임중여부 (풀방여부)
			if (rs.next()) {
				dbState = rs.getString(1);
			}

			if (dbState.equals("대기중")) { // 풀방이 아닐 때
				// db에 해당 게임방 상태를 게임중으로 바꾸기
				String dml2 = "update gameroom set state = '게임중' , guest = ? where host = ?";
				con = DBUtil.getConnection();
				pstmt = con.prepareStatement(dml2);
				pstmt.setString(1, guestName);
				pstmt.setString(2, hostName);
				int count = pstmt.executeUpdate();
				if (count == 0) {
					result = 0; // 오류가나서 업데이트가 안되면 0을 리턴해서 사용자가 방에 못들어가게 함
				} else {
					result = 1;
				}
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				// 데이터베이스와의 연결에 사용되었던 오브젝트를 해제
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

	// 호스트닉네임으로 호스트닉,게스트닉 조회해서 배열로 리턴하기
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
				// 데이터베이스와의 연결에 사용되었던 오브젝트를 해제
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

	// rp 업데이트,
	public int[] gameRpUpdate(String hostId, String guestId, boolean hostWin, String hostScr, String guestScr) {

		int[] newRp = null;

		try {
			//db에 등록될 최종 rp (나중에 인트로 형병환)
			double finalHostRp = 0;
			double finalGuestRp = 0;
			//변경이전 rp
			int oldHostRp = 0;
			int oldGuestRp = 0;
			//게임으로 얻게되는 rp (지는 사람은 -)
			int hostGetRp = 0;
			int guestGetRp = 0;
			////////////////////////////////////////////////////////////////////////
			// 호스트 rp구하기
			String dml = "select rp from rank where id = ?";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			pstmt.setString(1, hostId);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				oldHostRp = rs.getInt(1);
			}
			// 게스트rp구하기
			dml = "select rp from rank where id = ?";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			pstmt.setString(1, guestId);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				oldGuestRp = rs.getInt(1);
			}
			////////////////////////////////////////////////////////////////////////
			// 두 플레이어간의 실력차이를 절대값으로 환산
			int remainder = oldHostRp - oldGuestRp;
			if (remainder < 0) {
				remainder = ~remainder + 1; 
			} else if(remainder <9) {	// 실력차가 9보다 작으면 임의로 10을 줌 (서로 얻고잃는 rp가 너무 작기때문)
				remainder = 10;
			} else if (remainder >=50) {	// 실력차가 50이상이면 임의로 49로 줌 
				remainder = 49;
			}
			if (hostWin) { // 호스트가 이길경우
				if (oldHostRp > oldGuestRp) { // 호스트가 게스트보다 잘할경우 (잘하는 사람이 못하는사람 이긴케이스)
					hostGetRp = 50 - remainder; // 50에서 실력차만큼 차감된 값을 얻음. (실력차가 최대 49니까 이럴경우 이겨도 얻게되는 rp는1)
					guestGetRp = -hostGetRp;// 진사람은 호스트가 얻은 rp만큼 차감됨
				} else if(oldHostRp == oldGuestRp) {//호스트와 게스의 rp가 같을경우
					hostGetRp = 10;
					guestGetRp = -10;
				}
				else { // 호스트가 게스트보다 못할경우 (못하는 사람이 잘하는사람 이긴 케이스)
					hostGetRp = remainder; //호스트는 실력차만큼 rp 얻음
					guestGetRp = -remainder;//게스트는 그만큼 잃음
				}
			} else { // 게스트가 이길경우
				if (oldHostRp > oldGuestRp) { //못하는사람이 잘하는사람 이긴 케이스
					hostGetRp = -remainder;	//호스트는 실력차만큼 rp를 잃고
					guestGetRp = remainder;	//게스트는 실력차만큼 얻음
				}else if(oldHostRp == oldGuestRp) {
					hostGetRp = -10;
					guestGetRp = 10;
				} else { // 잘하는사람이 못하는사람 이긴 케이스
					guestGetRp = 50-remainder;
					hostGetRp = -guestGetRp;//
				}
			}
			
			int hostScore = Integer.parseInt(hostScr);
			int guestScore = Integer.parseInt(guestScr);
			
			// 게임에서 (맞춘수-틀린수)가 클수록 추가점수 줌
			finalHostRp = hostGetRp * (((double)hostScore / 50) + 1);
			finalGuestRp = guestGetRp * (((double)guestScore / 50) + 1);

			newRp = new int[] { (int)finalHostRp, (int)finalGuestRp, oldHostRp+(int)finalHostRp, oldGuestRp+(int)finalGuestRp};
			//[얻은호스트rp, 얻은게스트rp, db등록될 호스트rp, db등록될 게스트rp]


		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// 데이터베이스와의 연결에 사용되었던 오브젝트를 해제
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

	// 게임결과 업데이트
	public void gameScoreUpdate(int newHostRp, int newGuestRp, String hostId, String guestId, boolean hostWin) {

		
		try {
			if (hostWin) {
				int oldHostVictory = 0;
				int oldHostTotal = 0;
				
				String dml = "";
				// 호스트 승패 올드데이터 찾기
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
					System.out.println("호스트의 RP, 게임횟수, 승수가 업데이트 되었습니다");

				// 게스트 올드데이터 가져오기
				
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
					System.out.println("게스트의 RP, 게임횟수, 패수가 업데이트 되었습니다");

			} else {
				//호스트정보가져오기
				int oldHostDefeat = 0;
				int oldHostTotal = 0;
				String dml = "";
				// 호스트 올드데이터 가져오기
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
					System.out.println("호스트의 RP 게임횟수, 승수가 업데이트 되었습니다");

				// 게스트 승수 업데이트
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
					System.out.println("게스트의 RP, 게임횟수, 패수가 업데이트 되었습니다");

			} // end of hostWin
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				// 데이터베이스와의 연결에 사용되었던 오브젝트를 해제
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

	// 코인 업데이트 (이긴사람+100, 진사람+50)
	public void gameCoinUpdate(String hostId, String guestId, boolean hostWin) {
		String dml;
		try {
			if (hostWin) {
				// 호스트 코인 업뎃
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
					System.out.println("호스트의 코인이 +100 업데이트 되었습니다");

				// 게스트 코인 업뎃
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
					System.out.println("게스트의 코인이 +50 업데이트 되었습니다");

			} else {
				// 호스트 코인 업뎃
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
					System.out.println("호스트의 코인이 +50 업데이트 되었습니다");

				// 게스트 코인 업뎃
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
					System.out.println("게스트의 코인이 +100 업데이트 되었습니다");
			} // end of hostWin
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// 데이터베이스와의 연결에 사용되었던 오브젝트를 해제
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
				System.out.println(hostNickname + "이 게임룸테이블에서 지워집니다");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// 데이터베이스와의 연결에 사용되었던 오브젝트를 해제
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
			dml = "update gameroom set state = '대기중' , guest = ? where host = ?";
			con = DBUtil.getConnection();
			pstmt = con.prepareStatement(dml);
			pstmt.setString(1, null);
			pstmt.setString(2, hostNickname);
			int count = pstmt.executeUpdate();
			if (count == 0) {
				System.out.println("게스트가 성공적으로 방에서 지워졌습니다");
			} else {

			}
		} catch (ClassNotFoundException | SQLException e) {
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

	}

}