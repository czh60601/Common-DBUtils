package test;

public class Book{
	private int bid;
	private String bname;
	private int category;

	public Book() {
	}
	public Book(int bid, String bname, int category) {
		this.bid = bid;
		this.bname = bname;
		this.category = category;
	}
	public int getBid() {
		return bid;
	}
	public void setBid(int bid) {
		this.bid = bid;
	}
	public String getBname() {
		return bname;
	}
	public void setBname(String bname) {
		this.bname = bname;
	}
	public int getCategory() {
		return category;
	}
	public void setCategory(int category) {
		this.category = category;
	}
	@Override
	public String toString() {
		return "Book [bid=" + bid + ", bname=" + bname + ", category="
				+ category + "]";
	}
}
