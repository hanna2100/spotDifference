package model;

public class RankVO {
	
	private String nickname;
	private int rp;
	private int victory;
	private int defeat;
	private int totalGame;
	
	public RankVO(String nickname, int rp, int victory, int defeat, int totalGame) {
		super();
		this.nickname = nickname;
		this.rp = rp;
		this.victory = victory;
		this.defeat = defeat;
		this.totalGame = totalGame;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getRp() {
		return rp;
	}

	public void setRp(int rp) {
		this.rp = rp;
	}

	public int getVictory() {
		return victory;
	}

	public void setVictory(int victory) {
		this.victory = victory;
	}

	public int getDefeat() {
		return defeat;
	}

	public void setDefeat(int defeat) {
		this.defeat = defeat;
	}

	public int getTotalGame() {
		return totalGame;
	}

	public void setTotalGame(int totalGame) {
		this.totalGame = totalGame;
	}

	@Override
	public String toString() {
		return nickname + "|" + rp + "|" + victory + "|" + defeat
				+ "|" + totalGame;
	}
	
	
	

}
