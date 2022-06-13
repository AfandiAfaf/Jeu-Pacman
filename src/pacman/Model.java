package pacman;

import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Model extends JPanel implements ActionListener {
	//JPanel permet de créer la fenêtre du jeu
	//ActionListener permet au jeu d'interagir avec les boutons
	
	//variables:
	private Dimension d; //hight and width of the playing field
	private final Font smallFont = new Font("Arial", Font.BOLD, 14);
	
	private boolean inGame = false; //si le jeu est en marche
	private boolean dying = false; // si pacman est vivant
	
	private final int BLOCK_SIZE = 24; //la taille des blocs dans le jeu
	private final int N_BLOCKS = 15; //le nombre de blocs par coté
	private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE ; //=360=24x15
	
	private final int MAX_GHOSTS = 12; //le nombre maxi des ennemis (fontômes)
	private final int PACMAN_SPEED = 6; //la vitesse de pacman
	
	private int N_GHOSTS = 6; //nombre d'ennemis au début du jeu
	private int lives, score; //le nombre de vies possibles et le score de la partie
	private int[] dx, dy; //pour la position des ennemis
    private int[] ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed; //pour déterminer la position et le nombre des ennemis
    
    private Image heart, ghost; // image des vies (heart) et des ennemis (ghost)
    private Image up, down, left, right; //image des deplacements possibles de pacman 
    
    private int pacman_x, pacman_y, pacmand_x, pacmand_y; //position et deplacement de pacman
    private int req_dx, req_dy; //controlle la position x et y avec les boutons flèches
    
    private final int validSpeeds[] = {1, 2, 3, 4, 6, 8}; //les vitesses valides
    private final int maxSpeed = 6; //la vitesse maximale
    private int currentSpeed = 3; //la vitesse actuelle 
    private short[] screenData; //prend les valeurs du niveau précédent pour reproduire le jeu 
    private Timer timer; //pour permettre les animations 
    
    private final short levelData[] = {
        	19, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
            17, 16, 16, 16, 16, 24, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 28, 0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
            0,  0,  0,  0,  0,  0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
            19, 18, 18, 18, 18, 18, 16, 16, 16, 16, 24, 24, 24, 24, 20,
            17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 16, 24, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 18, 18, 18, 18, 20,
            17, 24, 24, 28, 0, 25, 24, 24, 16, 16, 16, 16, 16, 16, 20,
            21, 0,  0,  0,  0,  0,  0,   0, 17, 16, 16, 16, 16, 16, 20,
            17, 18, 18, 22, 0, 19, 18, 18, 16, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 26, 24, 24, 24, 24, 24, 24, 24, 24, 24, 28
        }; //On a 225 nombre (15*15) chaque valeur représente un élement de la fenêtre du jeu, ça permet de créer son propre model de jeu 
           // 0=mur, 1=left border, 2=top border, 4=right border, 8=bottom border, 16= les points que pacman collecte 
           // exemple le premier numéro 19= 1+2+16= left border+top border+point 
    
    
    public Model() {
    	//Constructeur
        loadImages(); //import et affichage des images
        initVariables(); //initialiser les variables 
        addKeyListener(new TAdapter()); //controller function
        setFocusable(true); //le focus sur la fenêtre
        initGame(); //Démarrer le jeu 
        
    }
    
    
    private void loadImages() {
    	//importer les images 
    	down = new ImageIcon("C:/Users/AFAF/eclipse-workspace/pacman/src/images/down.gif").getImage();
    	up = new ImageIcon("C:/Users/AFAF/eclipse-workspace/pacman/src/images/up.gif").getImage();
    	left = new ImageIcon("C:/Users/AFAF/eclipse-workspace/pacman/src/images/left.gif").getImage();
    	right = new ImageIcon("C:/Users/AFAF/eclipse-workspace/pacman/src/images/right.gif").getImage();
        ghost = new ImageIcon("C:/Users/AFAF/eclipse-workspace/pacman/src/images/ghost.gif").getImage();
        heart = new ImageIcon("C:/Users/AFAF/eclipse-workspace/pacman/src/images/heart.png").getImage();

    }
    
    private void initVariables() {
    	//initialiser toutes les variables de la fenêtre et des ennemies
        screenData = new short[N_BLOCKS * N_BLOCKS];
        d = new Dimension(400, 400);
        ghost_x = new int[MAX_GHOSTS];
        ghost_dx = new int[MAX_GHOSTS];
        ghost_y = new int[MAX_GHOSTS];
        ghost_dy = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];
        dx = new int[4];
        dy = new int[4];
        
        timer = new Timer(40, this); //le jeu est rechargé chaque 40 ms
        timer.start(); // démarrer timer
    }
    
    private void showIntroScreen(Graphics2D g2d) {
    	//affichage de la fenêtre de démarrage
    	String start = "Press SPACE to start";
        g2d.setColor(Color.yellow);
        g2d.drawString(start, (SCREEN_SIZE)/4, 150);//la position du texte
    }
    
    private void drawScore(Graphics2D g) {
    	//pareil pour l'introscreen on choisit une couleur et un font
        g.setFont(smallFont);
        g.setColor(new Color(5, 181, 79));
        String s = "Score: " + score;
        g.drawString(s, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16); //on détermine la position de l'affichage

        for (int i = 0; i < lives; i++) {//on vérifie combien de vies pacman a pour afficher le nombre de coeurs correspondant sur l'écran 
            g.drawImage(heart, i * 28 + 8, SCREEN_SIZE + 1, this);
        }
    }

    
    private void playGame(Graphics2D g2d) {
    	//display the graphics
        if (dying) { //pacman dead

            death();

        } else {

            movePacman();
            drawPacman(g2d);
            moveGhosts(g2d);
            checkMaze();
        }
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g); //constructor of the parent class

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black); //backgroung color
        g2d.fillRect(0, 0, d.width, d.height); //drawing the position

        //afficher les infos du jeu
        drawMaze(g2d);
        drawScore(g2d);

        if (inGame) {
            playGame(g2d);
        } else {
            showIntroScreen(g2d);
        }

        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }
    

    
    private void initGame() {
    	//initialiser les valeurs au début du jeu
    	lives = 3;
        score = 0;
        initLevel();
        N_GHOSTS = 6;
        currentSpeed = 3;
    }
    
    private void initLevel() {
    	//initialiser le niveau
        int i;
        for (i = 0; i < N_BLOCKS * N_BLOCKS; i++) { //boucle pour copier les valeurs de l'array déjà créé 
            screenData[i] = levelData[i];
        }

        continueLevel();
    }
    
    private void continueLevel() {
    	//définir les positions des ennemis au début du jeu et leur attribuer chacun une vitesse aléatoire
    	int dx = 1;
        int random;

        for (int i = 0; i < N_GHOSTS; i++) {

            ghost_y[i] = 4 * BLOCK_SIZE; //start position
            ghost_x[i] = 4 * BLOCK_SIZE;
            ghost_dy[i] = 0;
            ghost_dx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            ghostSpeed[i] = validSpeeds[random]; //la vitesse doit avoir une valeur appartenant aux valeurs déjà définies dans l'array validSpeed
        }
        //définir la position de pacman au début du jeu 
        pacman_x = 7 * BLOCK_SIZE;  //start position
        pacman_y = 11 * BLOCK_SIZE;
        pacmand_x = 0;	//reset direction move
        pacmand_y = 0;
        req_dx = 0;		// reset direction controls
        req_dy = 0;
        dying = false;
    }
    
    private void movePacman() {
    	//déplacement de pacman dans la fenêtre du jeu
        int pos;
        short ch;

        if (pacman_x % BLOCK_SIZE == 0 && pacman_y % BLOCK_SIZE == 0) {
            pos = pacman_x / BLOCK_SIZE + N_BLOCKS * (int) (pacman_y / BLOCK_SIZE);
            ch = screenData[pos];

            if ((ch & 16) != 0) { //16 est le nombre qui représente les points que pacman doit collecter
                screenData[pos] = (short) (ch & 15);
                score++; //alors le score s'incrémente de 1 
            }

            if (req_dx != 0 || req_dy != 0) { //control des mouvements de pacman avec les boutons flèches 
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0) //if pacman is on left border 
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0) //if pacman is on right border
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0) //if pacman is on top border
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) //if pacman is on bottom border 
                	{
                    pacmand_x = req_dx;
                    pacmand_y = req_dy;
                    }//if pacman is on one of the borders it can't move in the correspondant direction
            }

            // Check for standstill
            if ((pacmand_x == -1 && pacmand_y == 0 && (ch & 1) != 0)
                    || (pacmand_x == 1 && pacmand_y == 0 && (ch & 4) != 0)
                    || (pacmand_x == 0 && pacmand_y == -1 && (ch & 2) != 0)
                    || (pacmand_x == 0 && pacmand_y == 1 && (ch & 8) != 0))
            {
                pacmand_x = 0;
                pacmand_y = 0;
            }//pacman ne change pas de positions
        } //ajustement des vitesses
        pacman_x = pacman_x + PACMAN_SPEED * pacmand_x;
        pacman_y = pacman_y + PACMAN_SPEED * pacmand_y;
    }
    
    private void drawPacman(Graphics2D g2d) {
    	//vérifie quel bouton est pressé et affiche l'image qui lui correspond 
        if (req_dx == -1) {
        	g2d.drawImage(left, pacman_x + 1, pacman_y + 1, this);
        } else if (req_dx == 1) {
        	g2d.drawImage(right, pacman_x + 1, pacman_y + 1, this);
        } else if (req_dy == -1) {
        	g2d.drawImage(up, pacman_x + 1, pacman_y + 1, this);
        } else {
        	g2d.drawImage(down, pacman_x + 1, pacman_y + 1, this);
        }
    }
    
    private void moveGhosts(Graphics2D g2d) {
    	
        int pos;
        int count;

        for (int i = 0; i < N_GHOSTS; i++) { //set the positions of all our ghosts (6)
            if (ghost_x[i] % BLOCK_SIZE == 0 && ghost_y[i] % BLOCK_SIZE == 0) {
                pos = ghost_x[i] / BLOCK_SIZE + N_BLOCKS * (int) (ghost_y[i] / BLOCK_SIZE);

                count = 0;
                //using the border information we manage the mouvement of the ghosts
                if ((screenData[pos] & 1) == 0 && ghost_dx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 2) == 0 && ghost_dy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }//si l'ennemi (ghost) est sur la bordure du haut il doit forcement se déplacer vers le bas d'où dy-1

                if ((screenData[pos] & 4) == 0 && ghost_dx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 8) == 0 && ghost_dy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((screenData[pos] & 15) == 15) {
                        ghost_dx[i] = 0;
                        ghost_dy[i] = 0;
                    } else {
                        ghost_dx[i] = -ghost_dx[i];
                        ghost_dy[i] = -ghost_dy[i];
                    }//determination dans quel bloc est situé l'ennemi 
                    
                    //s'il n'y a pas d'obstacles à droite et l'ennemi ne va pas vers la gauche alors il se déplace vers la droite
                } else {

                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    ghost_dx[i] = dx[count];
                    ghost_dy[i] = dy[count];
                }

            }
            
            //ajustement des vitesses at affichage de l'image du ghost
            ghost_x[i] = ghost_x[i] + (ghost_dx[i] * ghostSpeed[i]);
            ghost_y[i] = ghost_y[i] + (ghost_dy[i] * ghostSpeed[i]);
            drawGhost(g2d, ghost_x[i] + 1, ghost_y[i] + 1);

            if (pacman_x > (ghost_x[i] - 12) && pacman_x < (ghost_x[i] + 12)
                    && pacman_y > (ghost_y[i] - 12) && pacman_y < (ghost_y[i] + 12)
                    && inGame) {

                dying = true;
            }// si pacman touche un ennemi (ghost) il perd une vie 
        }
    }
    
    private void drawGhost(Graphics2D g2d, int x, int y) {
    	g2d.drawImage(ghost, x, y, this);
        }//affichage de l'ennemi (ghost)
       
    private void checkMaze() {
    	//vérifier si il y a encore des points (numéro 16) pour pacman à manger dans notre maze
        int i = 0;
        boolean finished = true;

        while (i < N_BLOCKS * N_BLOCKS && finished) {

            if ((screenData[i]) != 0) {
                finished = false;
            }

            i++;
        }

        if (finished) {
        	//si tous les points sont collectés on augmente le score de 50 et on passe au niveau suivant 
        	//le niveau suivant dans mon cas est de redémarrer le même niveau en augmentant la vitesse des fantômes par 1
        	
            score += 50;

            if (N_GHOSTS < MAX_GHOSTS) {
                N_GHOSTS++;
            }

            if (currentSpeed < maxSpeed) {
                currentSpeed++;
            }

            initLevel();
        }
    }
    
    private void death() {
    	//si pacman meurt il perd une vie et continue le jeu
    	//lorsque toutes ses vies sont perdues le jeu se termine et pacman et ses ennemis reviennent à leurs positions initiales
    	lives--;

        if (lives == 0) {
            inGame = false;
        }

        continueLevel();
    }
    
    private void drawMaze(Graphics2D g2d) {
    	//trace le jeu à partie de 2 boucles for
        short i = 0;
        int x, y;

        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                g2d.setColor(new Color(0,72,251));
                g2d.setStroke(new BasicStroke(5));
                
                if ((levelData[i] == 0)) { //mur = bloc bleu
                	g2d.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                 }

                if ((screenData[i] & 1) != 0) { //1 est la bordure de gauche
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 2) != 0) { //la bordure du haut
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                }

                if ((screenData[i] & 4) != 0) { //la bordure de droite
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 8) != 0) { //la bordure du bas
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 16) != 0) { //les points blancs 
                    g2d.setColor(new Color(255,255,255));
                    g2d.fillOval(x + 10, y + 10, 6, 6);
               }

                i++;
            }
        }
    }
    
    

    
  //controls
    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (inGame) { //si le jeu est démarré ingame = true 
                if (key == KeyEvent.VK_LEFT) { //deplacement gauche
                    req_dx = -1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_RIGHT) { //deplacement droite
                    req_dx = 1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_UP) { //deplacement haut
                    req_dx = 0;
                    req_dy = -1;
                } else if (key == KeyEvent.VK_DOWN) { //deplacement bas
                    req_dx = 0;
                    req_dy = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false; //fin du jeu 
                } 
            } else {
                if (key == KeyEvent.VK_SPACE) { //Démarrer le jeu avec le bouton espace
                    inGame = true;
                    initGame();
                }
            }
        }
}

	
    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
    
    
}
