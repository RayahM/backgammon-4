package game;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.JTextField;

import networking.Client;
import networking.Network;
import networking.Server;
import ai.AI;
import ai.aoi.Aoi;
import ai.homura.Homura;
import gui.game.Window;
import gui.options.AIPanel;
import gui.options.GameOptionWindow;

public class Game {

	public static int turn = 0;
	public static int turnNumber = 0;
	public static int blackwins = 0;
	public static int whitewins = 0;
	public static Window gameWindow;
	public static final int PIECE_NUMBER = 15;
	public static final int LOCAL = 0;
	public static int winner;
	public static boolean gameOver;
	public static boolean hasStarted;

	public static boolean whiteIsHuman;
	public static boolean blackIsHuman;
	public static boolean isServer;
	public static boolean whiteIsNetwork;
	public static boolean blackIsNetwork;
	
	public static AI whiteAI, blackAI;
	public static final long sleepTime = 40;
	public static int gamesPlayed = 0;
	public static int maxGames = 5;
	
	public static void main(String[] args) throws InterruptedException, IOException {

		Game.gameOver = false;
		Column.init();
		gameWindow = new Window();
		
		while (true){
			while (!hasStarted){
				Thread.sleep(sleepTime*2);
			}
			System.out.println("starting game...");
			while (!gameOver){
				while (Game.turn == Column.WHITE && !Game.gameOver){
					if (whiteIsNetwork){
						Network.run();
					} else {
						if (whiteIsHuman){
							Thread.sleep(sleepTime);
						} else {
							whiteAI.makeRandomMove();
							Thread.sleep(sleepTime*3);
						}						
					}
					gameWindow.repaint();
				}
				while (Game.turn == Column.BLACK && !Game.gameOver){
					if (blackIsNetwork){
						Network.run();
					} else {
						if (blackIsHuman){
							Thread.sleep(sleepTime);
						} else {
							blackAI.makeRandomMove();
							Thread.sleep(sleepTime*3);
						}
					}
					gameWindow.repaint();
				}
			}
			if (winner == Column.BLACK){
				System.out.println("Black wins");
				blackwins++;
			} else {
				System.out.println("White wins");
				whitewins++;
			}
			gamesPlayed++;
			if (gamesPlayed >= maxGames){
				System.out.println("Black won: " + blackwins);
				System.out.println("White won: " + whitewins);
				System.exit(0);
			}
			Game.reset();
		}

	}

	public static void changeTurn() {
//		System.out.println("Changing turn...");
		Move.message = Move.message.substring(0, Move.message.length() - 1) + ";";
		Network.addText(Move.message);
		Game.turnNumber++;
		if (!Game.gameOver){
			if(Column.getAll()[0].getPieces().size() == Game.PIECE_NUMBER){
				Game.winner = Column.BLACK;
				Game.gameOver = true;

			} else if (Column.getAll()[25].getPieces().size() == Game.PIECE_NUMBER) {
				Game.winner = Column.WHITE;
				Game.gameOver = true;
			}

			Game.turn = (Game.turn == Column.BLACK) ? Column.WHITE : Column.BLACK;
			gameWindow.repaint();
			Move.rollDice();
			Move.getValidMoves();
			if (!Move.checkMoves()){
				Move.message = Move.message + "(-1|-1);";
				Game.changeTurn();
			}			
		}
	}

	public static void reset(){
		gameOver = false;
		hasStarted = false;
		Column.init();
		turnNumber = 0;
	}

	public static void startGame(){
		Column.addPieces();
		Game.hasStarted = true;
		Game.turn = Column.WHITE;
		Move.rollDice();
		GameOptionWindow.optionsMenu.dispose();
		gameWindow.repaint();
	}

	public static void startLocalGame() {
		whiteIsHuman = true;
		blackIsHuman = true;
		startGame();
		
	}

	public static void startAIGame(int AIType, boolean playAsWhite) {
		if (playAsWhite){
			whiteIsHuman = true;
			blackIsHuman = false;
			
			makeAI(AIType, blackAI);
			
		} else {
			whiteIsHuman = false;
			blackIsHuman = true;
			
			makeAI(AIType,whiteAI);
			
		}
		startGame();
	}

	public static void startNetworkGame(int aiType, int port) {
		//stuff here
		blackIsNetwork = true;
		if (aiType != -1){	
			whiteIsHuman = false;
			makeAI(aiType,whiteAI);
		} else {
			whiteIsHuman = true;	
		}
		
		
		try {
			Server.start(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		startGame();
	}

	public static void joinNetworkGame(int aiType, int port, String hostName) {
		//stuff here
		//WHITE IS SERVER, BLACK IS CLIENT
		whiteIsNetwork = true;
		if (aiType != -1){
			blackIsHuman = false;
			makeAI(aiType,blackAI);
		} else {
			blackIsHuman = true;
		}
		
		try {
			Client.start(hostName, port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		startGame();
	}

	public static void startLocalAIGame(int AIType1, int AIType2) {
		whiteIsHuman = false;
		blackIsHuman = false;
		
		makeAI(AIType1,whiteAI);
		makeAI(AIType2,blackAI);
		
		startGame();
	}

	public static void makeAI(int type, AI ai){
		switch(type){
		case AIPanel.AoiIndex:
			ai = new Aoi();
			break;
		case AIPanel.HomuraIndex:
			ai = new Homura();
			break;
		case AIPanel.RandomIndex:
			ai = new AI();
			break;
		}
	}
	
}
