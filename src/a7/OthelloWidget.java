package a7;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class OthelloWidget extends JPanel implements ActionListener, SpotListener {

	/* Enum to identify player. */

	private enum Player {
		BLACK, WHITE
	};

	private JSpotBoard _board; /* SpotBoard playing area. */
	private JLabel _message; /* Label for messages. */
	private boolean _game_won; /* Indicates if games was been won already. */
	private Player _next_to_play; /* Identifies who has next turn. */
	private List<Spot> flipableSpots = new ArrayList<Spot>();
	private String player_name = null;
	private Color player_color = null;
	private boolean skipTurn = false;

	public OthelloWidget() {

		/* Create SpotBoard and message label. */

		_board = new JSpotBoard(8, 8);
		_message = new JLabel();

		/* Set layout and place SpotBoard at center. */

		setLayout(new BorderLayout());
		add(_board, BorderLayout.CENTER);

		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				Color bg = ((x + y) % 2 == 0) ? Color.LIGHT_GRAY : Color.DARK_GRAY;
				_board.getSpotAt(x, y).setBackground(bg);
			}
		}

		/* Create subpanel for message area and reset button. */

		JPanel reset_message_panel = new JPanel();
		reset_message_panel.setLayout(new BorderLayout());

		/* Reset button. Add ourselves as the action listener. */

		JButton reset_button = new JButton("Restart");
		reset_button.addActionListener(this);
		reset_message_panel.add(reset_button, BorderLayout.EAST);
		reset_message_panel.add(_message, BorderLayout.CENTER);

		/* Add subpanel in south area of layout. */

		add(reset_message_panel, BorderLayout.SOUTH);

		/*
		 * Add ourselves as a spot listener for all of the spots on the spot board.
		 */
		_board.addSpotListener(this);

		/* Reset game. */
		resetGame();
		_board.getSpotAt(4, 4).setSpotColor(Color.WHITE);
		_board.getSpotAt(4, 4).setSpot();
		_board.getSpotAt(3, 4).setSpotColor(Color.BLACK);
		_board.getSpotAt(3, 4).setSpot();
		_board.getSpotAt(3, 3).setSpotColor(Color.WHITE);
		_board.getSpotAt(3, 3).setSpot();
		_board.getSpotAt(4, 3).setSpotColor(Color.BLACK);
		_board.getSpotAt(4, 3).setSpot();

	}

	/*
	 * resetGame
	 * 
	 * Resets the game by clearing all the spots on the board, picking a new secret
	 * spot, resetting game status fields, and displaying start message.
	 * 
	 */

	private void resetGame() {
		/*
		 * Clear all spots on board. Uses the fact that SpotBoard implements
		 * Iterable<Spot> to do this in a for-each loop.
		 */

		for (Spot s : _board) {
			s.setSpotColor(Color.BLUE);
			s.clearSpot();
		}
		_board.getSpotAt(4, 4).setSpotColor(Color.WHITE);
		_board.getSpotAt(4, 4).setSpot();
		_board.getSpotAt(3, 4).setSpotColor(Color.BLACK);
		_board.getSpotAt(3, 4).setSpot();
		_board.getSpotAt(3, 3).setSpotColor(Color.WHITE);
		_board.getSpotAt(3, 3).setSpot();
		_board.getSpotAt(4, 3).setSpotColor(Color.BLACK);
		_board.getSpotAt(4, 3).setSpot();

		/*
		 * Reset the background of the old secret spot. Check _secret_spot for null
		 * first because call to resetGame from constructor won't have a secret spot
		 * chosen yet.
		 */
		player_name = null;
		player_color = null;
		skipTurn = false;
		/* Reset game won and next to play fields */
		_game_won = false;
		_next_to_play = Player.BLACK;

		/* Display game start message. */

		_message.setText("Welcome to Othello. Black to play");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		/* Handles reset game button. Simply reset the game. */
		resetGame();
	}

	/*
	 * Implementation of SpotListener below. Implements game logic as responses to
	 * enter/exit/click on spots.
	 */

	@Override
	public void spotClicked(Spot s) {

		/* If game already won, do nothing. */
		if (_game_won) {
			return;
		}
		if (s.getSpotColor() != Color.BLUE) {
			return;
		}

		String next_player_name = null;
		int winningScore = 0;
		String winner = null;
		int blackCount = 0;
		int whiteCount = 0;
		int losingScore = 0;

		/*
		 * Set up player and next player name strings, and player color as local
		 * variables to be used later.
		 */

		if (_next_to_play == Player.BLACK) {
			player_color = Color.BLACK;
			player_name = "Black";
			next_player_name = "White";
			_next_to_play = Player.WHITE;
		} else {
			player_color = Color.WHITE;
			player_name = "White";
			next_player_name = "Black";
			_next_to_play = Player.BLACK;
		}

		/* Set color of spot clicked and toggle. */

		flipableSpots = flipableSpots(s, player_color);
		for (Spot spot : flipableSpots) {
			if (spot != null) {
				spot.setSpotColor(player_color);
				s.setSpotColor(player_color);

			}

		}
		if (s.getSpotColor() != Color.BLUE) {
			s.toggleSpot();
		}

		if (player_color == Color.BLACK) {
			skipTurn = checkForNoMoves(_board, Color.WHITE);

		}
		if (player_color == Color.WHITE) {
			skipTurn = checkForNoMoves(_board, Color.BLACK);

		}
		if (checkForNoMoves(_board, Color.BLACK) && checkForNoMoves(_board, Color.WHITE)) {
			_game_won = true;
		}

		/*
		 * Update the message depending on what happened. If spot is empty, then we must
		 * have just cleared the spot. Update message accordingly. If spot is not empty
		 * and the game is won, we must have just won. Calculate score and display as
		 * part of game won message. If spot is not empty and the game is not won,
		 * update message to report spot coordinates and indicate whose turn is next.
		 */

		if (_game_won) {

			for (Spot spot : _board) {
				if (spot.getSpotColor() == Color.BLACK) {
					blackCount++;
				} else if (spot.getSpotColor() == Color.WHITE) {
					whiteCount++;
				}
			}
			if (blackCount > whiteCount) {
				winner = "Black";
				winningScore = blackCount;
				losingScore = whiteCount;
			} else if (whiteCount > blackCount) {
				winner = "White";
				winningScore = whiteCount;
				losingScore = blackCount;
			} else if (whiteCount == blackCount) {
				winner = "Draw! No one";
			}

			_message.setText(winner + " won! Game over. Score: " + winningScore + " to " + losingScore);
		} else if (skipTurn == true) {
			if (_next_to_play == Player.BLACK) {
				_next_to_play = Player.WHITE;
				player_color = Color.BLACK;
				player_name = "Black";
				next_player_name = "White";
			} else {
				_next_to_play = Player.BLACK;
				player_color = Color.WHITE;
				player_name = "White";
				next_player_name = "Black";

			}
			_message.setText("Skip turn. " + next_player_name + " to play.");
		} else if (s.getSpotColor() == player_color) {
			_message.setText(next_player_name + " to play.");
		}

	}

	@Override
	public void spotEntered(Spot s) {
		/* Highlight spot if game still going on. */

		if (_game_won) {
			return;
		}
		if (!s.isEmpty()) {
			return;
		}

		if (player_color == Color.BLACK) {
			if (!flipableSpots(s, Color.WHITE).isEmpty()) {
				s.highlightSpot();
			}
		} else if (player_color == Color.WHITE || player_color == null) {
			if (!flipableSpots(s, Color.BLACK).isEmpty()) {
				s.highlightSpot();
			}
		}
	}

	@Override
	public void spotExited(Spot s) {
		/* Unhighlight spot. */

		s.unhighlightSpot();

	}

	public boolean checkForNoMoves(JSpotBoard _board, Color player_color) {

		for (Spot spot : _board) {
			if (spot.isEmpty()) {
				if (!flipableSpots(spot, player_color).isEmpty()) {
					return false;
				}
			}
		}

		return true;
	}

	//Returns list of flipable spots for spot argument
	public List<Spot> flipableSpots(Spot s, Color player_color) {

		List<Spot> flipable = new ArrayList<Spot>();

		if (s.getSpotX() != 0 && s.getSpotX() != 7 && s.getSpotX() != 1 && s.getSpotX() != 6 && s.getSpotY() != 0
				&& s.getSpotY() != 7 && s.getSpotY() != 6 && s.getSpotY() != 1) {
			// LEFT
			if ((_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() - 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() - 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() - 2), (s.getSpotY() - 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() - 1)));
				}
			}
			if ((_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY())).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY())).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() - 2), (s.getSpotY())).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY())));
				}
			}
			if ((_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() + 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() + 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() - 2), (s.getSpotY() + 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() + 1)));
				}
			}
			// MID
			if ((_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 1)));
				}
			}
			if ((_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 1)));
				}
			}
			// RIGHT
			if ((_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() - 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() - 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() + 2), (s.getSpotY() - 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() - 1)));
				}
			}
			if ((_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY())).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY())).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() + 2), (s.getSpotY())).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY())));
				}
			}
			if ((_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() + 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() + 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() + 2), (s.getSpotY() + 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() + 1)));
				}
			}
		}
		// (0,0)(0,1)(1,0)(1,1)
		if ((s.getSpotX() == 0 && s.getSpotY() == 0) || (s.getSpotX() == 0 && s.getSpotY() == 1)
				|| (s.getSpotX() == 1 && s.getSpotY() == 0) || (s.getSpotX() == 1 && s.getSpotY() == 1)) {

			// MID

			if ((_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 1)));
				}
			}
			// RIGHT

			if ((_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY())).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY())).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() + 2), (s.getSpotY())).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY())));
				}
			}
			if ((_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() + 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() + 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() + 2), (s.getSpotY() + 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() + 1)));
				}
			}

		}

		// (0,6)(0,7)(1,7)(1,6)

		if ((s.getSpotX() == 0 && s.getSpotY() == 6) || (s.getSpotX() == 0 && s.getSpotY() == 7)
				|| (s.getSpotX() == 1 && s.getSpotY() == 7) || (s.getSpotX() == 1 && s.getSpotY() == 6)) {
			// MID
			if ((_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 1)));
				}
			}
			// RIGHT
			if ((_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() - 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() - 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() + 2), (s.getSpotY() - 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() - 1)));
				}
			}
			if ((_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY())).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY())).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() + 2), (s.getSpotY())).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY())));
				}
			}

		}
		// (6,7)(7,7)(7,6)(6,6)
		if ((s.getSpotX() == 6 && s.getSpotY() == 7) || (s.getSpotX() == 7 && s.getSpotY() == 7)
				|| (s.getSpotX() == 7 && s.getSpotY() == 6) || (s.getSpotX() == 6 && s.getSpotY() == 6)) {

			// LEFT
			if ((_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() - 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() - 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() - 2), (s.getSpotY() - 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() - 1)));
				}
			}
			if ((_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY())).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY())).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() - 2), (s.getSpotY())).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY())));
				}
			}
			// MID
			if ((_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 1)));
				}
			}

		}
		// (6,0)(7,0)(7,1)(6,1)
		if ((s.getSpotX() == 6 && s.getSpotY() == 0) || (s.getSpotX() == 7 && s.getSpotY() == 0)
				|| (s.getSpotX() == 7 && s.getSpotY() == 1) || (s.getSpotX() == 6 && s.getSpotY() == 1)) {

			// LEFT
			if ((_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY())).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY())).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() - 2), (s.getSpotY())).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY())));
				}
			}
			if ((_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() + 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() + 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() - 2), (s.getSpotY() + 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() + 1)));
				}
			}
			// MID
			if ((_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 1)));
				}
			}

		}

		// (0-1,2-5)
		if ((s.getSpotX() == 0 && s.getSpotY() == 2) || (s.getSpotX() == 0 && s.getSpotY() == 3)
				|| (s.getSpotX() == 0 && s.getSpotY() == 4) || (s.getSpotX() == 0 && s.getSpotY() == 5)
				|| ((s.getSpotX() == 1 && s.getSpotY() == 2) || (s.getSpotX() == 1 && s.getSpotY() == 3)
						|| (s.getSpotX() == 1 && s.getSpotY() == 4) || (s.getSpotX() == 1 && s.getSpotY() == 5))) {

			// MID
			if ((_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 1)));
				}
			}
			if ((_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 1)));
				}
			}
			// RIGHT
			if ((_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() - 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() - 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() + 2), (s.getSpotY() - 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() - 1)));
				}
			}
			if ((_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY())).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY())).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() + 2), (s.getSpotY())).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY())));
				}
			}
			if ((_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() + 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() + 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() + 2), (s.getSpotY() + 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() + 1)));
				}
			}

		}
		// (6-7,2-5)
		if ((s.getSpotX() == 7 && s.getSpotY() == 2) || (s.getSpotX() == 7 && s.getSpotY() == 3)
				|| (s.getSpotX() == 7 && s.getSpotY() == 4) || (s.getSpotX() == 7 && s.getSpotY() == 5)
				|| ((s.getSpotX() == 6 && s.getSpotY() == 2) || (s.getSpotX() == 6 && s.getSpotY() == 3)
						|| (s.getSpotX() == 6 && s.getSpotY() == 4) || (s.getSpotX() == 6 && s.getSpotY() == 5))) {

			// LEFT
			if ((_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() - 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() - 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() - 2), (s.getSpotY() - 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() - 1)));
				}
			}
			if ((_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY())).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY())).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() - 2), (s.getSpotY())).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY())));
				}
			}
			if ((_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() + 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() + 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() - 2), (s.getSpotY() + 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() + 1)));
				}
			}
			// MID
			if ((_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 1)));
				}
			}
			if ((_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 1)));
				}
			}

		}
		// (2-5,0-1)
		if ((s.getSpotX() == 2 && s.getSpotY() == 0) || (s.getSpotX() == 3 && s.getSpotY() == 0)
				|| (s.getSpotX() == 4 && s.getSpotY() == 0) || (s.getSpotX() == 5 && s.getSpotY() == 0)
				|| ((s.getSpotX() == 2 && s.getSpotY() == 1) || (s.getSpotX() == 3 && s.getSpotY() == 1)
						|| (s.getSpotX() == 4 && s.getSpotY() == 1) || (s.getSpotX() == 5 && s.getSpotY() == 1))) {
			// LEFT

			if ((_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY())).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY())).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() - 2), (s.getSpotY())).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY())));
				}
			}
			if ((_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() + 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() + 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() - 2), (s.getSpotY() + 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() + 1)));
				}
			}
			// MID

			if ((_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX()), (s.getSpotY() + 1)));
				}
			}
			// RIGHT

			if ((_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY())).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY())).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() + 2), (s.getSpotY())).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY())));
				}
			}
			if ((_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() + 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() + 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() + 2), (s.getSpotY() + 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() + 1)));
				}
			}
		}
		// (2-5,6-7)
		if ((s.getSpotX() == 2 && s.getSpotY() == 7) || (s.getSpotX() == 3 && s.getSpotY() == 7)
				|| (s.getSpotX() == 4 && s.getSpotY() == 7) || (s.getSpotX() == 5 && s.getSpotY() == 7)
				|| ((s.getSpotX() == 2 && s.getSpotY() == 6) || (s.getSpotX() == 3 && s.getSpotY() == 6)
						|| (s.getSpotX() == 4 && s.getSpotY() == 6) || (s.getSpotX() == 5 && s.getSpotY() == 6))) {
			// LEFT
			if ((_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() - 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() - 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() - 2), (s.getSpotY() - 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY() - 1)));
				}
			}
			if ((_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY())).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY())).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() - 2), (s.getSpotY())).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() - 1), (s.getSpotY())));
				}
			}
			// MID
			if ((_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX()), (s.getSpotY() - 1)));
				}
			}
			// RIGHT
			if ((_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() - 1)).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() - 1)).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() + 2), (s.getSpotY() - 2)).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY() - 1)));
				}
			}
			if ((_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY())).isEmpty() != true)
					&& (_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY())).getSpotColor() != player_color)) {
				if (_board.getSpotAt((s.getSpotX() + 2), (s.getSpotY())).getSpotColor() == player_color) {
					flipable.add(_board.getSpotAt((s.getSpotX() + 1), (s.getSpotY())));
				}
			}

		}

		return flipable;
	}
}
