import java.awt.Graphics;
import java.io.IOException;

import javax.swing.JPanel;

public class Renderer extends JPanel {

	private static final long serialVersionUID = 1L;
	
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		
		try {
			Gradius.gradius.repaint(g);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
