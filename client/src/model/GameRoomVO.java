package model;

public class GameRoomVO {
	private int rp;
	private String roomName;
	private String host;
	private String state;
	private String roomRock;
	private String roomPw;

	public GameRoomVO() {
	}

	public GameRoomVO(int rp, String roomName, String host, String state, String roomRock) {
		super();
		this.rp = rp;
		this.roomName = roomName;
		this.host = host;
		this.state = state;
		this.roomRock = roomRock;
	}

	public GameRoomVO(int rp, String roomName, String host, String state, String roomRock,String roomPw) {
		super();
		this.rp = rp;
		this.roomName = roomName;
		this.host = host;
		this.state = state;
		this.roomRock = roomRock;
		this.roomPw = roomPw;
	}

	public int getRp() {
		return rp;
	}

	public void setRp(int rp) {
		this.rp = rp;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getRoomRock() {
		return roomRock;
	}

	public void setRoomRock(String roomRock) {
		this.roomRock = roomRock;
	}

	public String getRoomPw() {
		return roomPw;
	}

	public void setRoomPw(String roomPw) {
		this.roomPw = roomPw;
	}

	// 클라이언트에게 grVO객체를 전달해주는용
	@Override
	public String toString() {
		return rp + "|" + roomName + "|" + host + "|" + state + "|" + roomRock ;
	}

}
