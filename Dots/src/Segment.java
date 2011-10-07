import java.awt.Point;


public class Segment {
	public int x,y;
    public boolean isY;	
  public Segment(int x, int y, boolean isY) {
	  this.x = x; this.y = y; this.isY = isY;
  }
  
  public String toString() {
	  return x + " ," + y + ", " + isY;
  }
}
