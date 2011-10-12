


public class Segment {
	public int x,y;
    public boolean isY;
    public void copyTo(Segment s) {
    	s.x = this.x;
    	s.y = this.y;
    	s.isY = s.isY;
    }
  public Segment(Segment copy) {
	  copy.copyTo(this);
  }
  public Segment(int x, int y, boolean isY) {
	  this.x = x; this.y = y; this.isY = isY;
  }
  
  public String toString() {
	  return x + " ," + y + ", " + isY;
  }
  
  public String encode() {
	  return x + "," + y + "," + (isY ? "1" : "0");
  }
}
