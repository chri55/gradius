import java.awt.Rectangle;
import java.util.*;

public class Enemy {
	public int x,y,width,height;
	public int motionX, motionY;
	public Random random;
	public int hits;
	public Gradius gradius;
	
	public Enemy(int x, int y, int width, int height){
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
		this.hits = 3;
		
	}
	
	public Enemy(){
		//TODO: initial enemy size with no parameters
	}
	
	public Rectangle spawn(){
		Rectangle enemy = new Rectangle(x,y,width, height);
		
		return enemy;
	}

}