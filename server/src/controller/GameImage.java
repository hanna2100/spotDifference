package controller;

public class GameImage {
	private String fileName;
	private String coordinates;
	
	public GameImage() {
		
	}
	
	public GameImage(String fileName, String coordinates) {
		super();
		this.fileName = fileName;
		this.coordinates = coordinates;
	}
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public String toString() {
		return "GameImage [fileName=" + fileName + ", coordinates=" + coordinates + "]";
	}
	
	

}