package pacman;

import javax.swing.JFrame;

public class Pacman extends JFrame{
	//constructeur
	public Pacman() {
		add(new Model()); //start the window with model
	}
	
	
	public static void main(String[] args) {
		Pacman pac = new Pacman();
		pac.setVisible(true);
		pac.setTitle("Pacman"); //tirtre de la fenêtre
		pac.setSize(380,420); //taille de la fenêtre
		pac.setDefaultCloseOperation(EXIT_ON_CLOSE); //click sur croix rouge pour fermer
		pac.setLocationRelativeTo(null); //position au milieu
		
	}

}
