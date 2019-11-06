package model;

public class JoinVO {
	private String id; //아이디
	private String pw; //패스워드
	private String nickname; //닉네임
	
	public JoinVO() {
		
	}
	
	public JoinVO(String id, String pw, String nickname) {
		super();
		this.id = id;
		this.pw = pw;
		this.nickname = nickname;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPw() {
		return pw;
	}

	public void setPw(String pw) {
		this.pw = pw;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	@Override
	public String toString() {
		return "JoinVO [id=" + id + ", pw=" + pw + ", nickname=" + nickname + "]";
	}
	
	

}
