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

public class ConnectFourWidget extends JPanel implements ActionListener, SpotListener {

	/* Enum to identify player. */

	private enum Player {
		BLACK, RED
	};

	private JSpotBoard _board; /* SpotBoard playing area. */
	private JLabel _message; /* Label for messages. */
	private boolean _game_won; /* Indicates if games was been won already. */
	private Player _next_to_play; /* Identifies who has next turn. */
	private List<Spot> winningHighlight;

	public ConnectFourWidget() {

		/* Create SpotBoard and message label. */

		_board = new JSpotBoard(7, 6);
		_message = new JLabel();

		/* Set layout and place SpotBoard at center. */

		setLayout(new BorderLayout());
		add(_board, BorderLayout.CENTER);

		for (Spot s : _board) {
			if (s.getSpotX() % 2 != 0) {
				s.setBackground(Color.DARK_GRAY);
			} else {
				s.setBackground(Color.LIGHT_GRAY);
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
		/* Reset game won and next to play fields */
		_game_won = false;
		_next_to_play = Player.RED;

		/* Display game start message. */

		_message.setText("Welcome to Connect Four. Red to play");
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

		/*
		 * Set up player and next player name strings, and player color as local
		 * variables to be used later.
		 */

		String player_name = null;
		String next_player_name = null;
		Color player_color = null;
		int full = 0;
		winningHighlight = new ArrayList<Spot>();

		if (_next_to_play == Player.RED) {
			player_color = Color.RED;
			player_name = "Red";
			next_player_name = "Black";
			_next_to_play = Player.BLACK;
		} else {
			player_color = Color.BLACK;
			player_name = "Black";
			next_player_name = "Red";
			_next_to_play = Player.RED;
		}

		/* Set color of spot clicked and toggle. */

		if (_board.getSpotAt(s.getSpotX(), 5).isEmpty()) {
			s = _board.getSpotAt(s.getSpotX(), 5);
			s.setSpotColor(player_color);
		} else if (_board.getSpotAt(s.getSpotX(), 4).isEmpty()) {
			s = _board.getSpotAt(s.getSpotX(), 4);
			s.setSpotColor(player_color);
		} else if (_board.getSpotAt(s.getSpotX(), 3).isEmpty()) {
			s = _board.getSpotAt(s.getSpotX(), 3);
			s.setSpotColor(player_color);
		} else if (_board.getSpotAt(s.getSpotX(), 2).isEmpty()) {
			s = _board.getSpotAt(s.getSpotX(), 2);
			s.setSpotColor(player_color);
		} else if (_board.getSpotAt(s.getSpotX(), 1).isEmpty()) {
			s = _board.getSpotAt(s.getSpotX(), 1);
			s.setSpotColor(player_color);
		} else if (_board.getSpotAt(s.getSpotX(), 0).isEmpty()) {
			s = _board.getSpotAt(s.getSpotX(), 0);
			s.setSpotColor(player_color);
		} else {
			s.setSpotColor(player_color);
		}
		s.toggleSpot();

		/*
		 * Check if four in a row. If so, mark game as won.
		 */

		for (Spot spot : _board) {
			if (!spot.isEmpty()) {
				full++;
			}
		}
		_game_won = gameLogic(player_color);
		if ((full == 42) && (!_game_won)) {
			_message.setText("Draw.");
		}

		/*
		 * Update the message depending on what happened. If spot is empty, then we must
		 * have just cleared the spot. Update message accordingly. If spot is not empty
		 * and the game is won, we must have just won. Calculate score and display as
		 * part of game won message. If spot is not empty and the game is not won,
		 * update message to report spot coordinates and indicate whose turn is next.
		 */

		if (full < 42) {
			if (_game_won) {

				_message.setText(player_name + " Wins! Game over.");
			} else {
				_message.setText(next_player_name + " to play.");
			}
		}

	}

	@Override
	public void spotEntered(Spot s) {
		/* Highlight spot if game still going on. */

		if (_game_won) {
			for (Spot spot : winningHighlight) {
				spot.highlightSpot();
			}
			return;
		}

		for (int i = 0; i < 6; i++) {
			if (_board.getSpotAt(s.getSpotX(), i).isEmpty()) {
				_board.getSpotAt(s.getSpotX(), i).highlightSpot();
			}
		}
	}

	@Override
	public void spotExited(Spot s) {
		/* Unhighlight spot. */

		for (int i = 0; i < 6; i++) {
			_board.getSpotAt(s.getSpotX(), i).unhighlightSpot();
		}
	}

	public boolean gameLogic(Color player_color) {

		// vertical
		for (int i = 0; i < _board.getSpotWidth(); i++) {

			if ((_board.getSpotAt(i, 0).getSpotColor() == player_color)
					&& (_board.getSpotAt(i, 1).getSpotColor() == player_color)
					&& (_board.getSpotAt(i, 2).getSpotColor() == player_color)
					&& (_board.getSpotAt(i, 3).getSpotColor() == player_color)) {
				winningHighlight.add(_board.getSpotAt(i, 0));
				winningHighlight.add(_board.getSpotAt(i, 1));
				winningHighlight.add(_board.getSpotAt(i, 2));
				winningHighlight.add(_board.getSpotAt(i, 3));

				return true;
			}

		}
		for (int i = 0; i < _board.getSpotWidth(); i++) {

			if ((_board.getSpotAt(i, 1).getSpotColor() == player_color)
					&& (_board.getSpotAt(i, 2).getSpotColor() == player_color)
					&& (_board.getSpotAt(i, 3).getSpotColor() == player_color)
					&& (_board.getSpotAt(i, 4).getSpotColor() == player_color)) {
				winningHighlight.add(_board.getSpotAt(i, 4));
				winningHighlight.add(_board.getSpotAt(i, 1));
				winningHighlight.add(_board.getSpotAt(i, 2));
				winningHighlight.add(_board.getSpotAt(i, 3));
				return true;
			}

		}
		for (int i = 0; i < _board.getSpotWidth(); i++) {

			if ((_board.getSpotAt(i, 2).getSpotColor() == player_color)
					&& (_board.getSpotAt(i, 3).getSpotColor() == player_color)
					&& (_board.getSpotAt(i, 4).getSpotColor() == player_color)
					&& (_board.getSpotAt(i, 5).getSpotColor() == player_color)) {
				winningHighlight.add(_board.getSpotAt(i, 4));
				winningHighlight.add(_board.getSpotAt(i, 5));
				winningHighlight.add(_board.getSpotAt(i, 2));
				winningHighlight.add(_board.getSpotAt(i, 3));
				return true;
			}

		} // diagonal upper left

		if ((_board.getSpotAt(0, 0).getSpotColor() == player_color)
				&& (_board.getSpotAt(1, 1).getSpotColor() == player_color)
				&& (_board.getSpotAt(2, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 3).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(0, 0));
			winningHighlight.add(_board.getSpotAt(1, 1));
			winningHighlight.add(_board.getSpotAt(2, 2));
			winningHighlight.add(_board.getSpotAt(3, 3));
			return true;
		} else if ((_board.getSpotAt(4, 4).getSpotColor() == player_color)
				&& (_board.getSpotAt(1, 1).getSpotColor() == player_color)
				&& (_board.getSpotAt(2, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 3).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(4, 4));
			winningHighlight.add(_board.getSpotAt(1, 1));
			winningHighlight.add(_board.getSpotAt(2, 2));
			winningHighlight.add(_board.getSpotAt(3, 3));
			return true;
		} else if ((_board.getSpotAt(5, 5).getSpotColor() == player_color)
				&& (_board.getSpotAt(4, 4).getSpotColor() == player_color)
				&& (_board.getSpotAt(2, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 3).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(5, 5));
			winningHighlight.add(_board.getSpotAt(4, 4));
			winningHighlight.add(_board.getSpotAt(4, 2));
			winningHighlight.add(_board.getSpotAt(3, 3));
			return true;
		} else if ((_board.getSpotAt(0, 1).getSpotColor() == player_color)
				&& (_board.getSpotAt(1, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(2, 3).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 4).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(0, 1));
			winningHighlight.add(_board.getSpotAt(1, 2));
			winningHighlight.add(_board.getSpotAt(2, 3));
			winningHighlight.add(_board.getSpotAt(3, 4));
			return true;
		} else if ((_board.getSpotAt(1, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(2, 3).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 4).getSpotColor() == player_color)
				&& (_board.getSpotAt(4, 5).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(1, 2));
			winningHighlight.add(_board.getSpotAt(2, 3));
			winningHighlight.add(_board.getSpotAt(3, 4));
			winningHighlight.add(_board.getSpotAt(4, 5));
			return true;
		} else if ((_board.getSpotAt(0, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(4, 5).getSpotColor() == player_color)
				&& (_board.getSpotAt(2, 4).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 5).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(0, 2));
			winningHighlight.add(_board.getSpotAt(4, 5));
			winningHighlight.add(_board.getSpotAt(2, 4));
			winningHighlight.add(_board.getSpotAt(3, 5));
			return true;
		} else if ((_board.getSpotAt(1, 0).getSpotColor() == player_color)
				&& (_board.getSpotAt(2, 1).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(4, 3).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(1, 0));
			winningHighlight.add(_board.getSpotAt(2, 1));
			winningHighlight.add(_board.getSpotAt(3, 2));
			winningHighlight.add(_board.getSpotAt(4, 3));
			return true;
		} else if ((_board.getSpotAt(2, 1).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(4, 3).getSpotColor() == player_color)
				&& (_board.getSpotAt(5, 4).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(2, 1));
			winningHighlight.add(_board.getSpotAt(3, 2));
			winningHighlight.add(_board.getSpotAt(4, 3));
			winningHighlight.add(_board.getSpotAt(5, 4));
			return true;
		} else if ((_board.getSpotAt(3, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(4, 3).getSpotColor() == player_color)
				&& (_board.getSpotAt(5, 4).getSpotColor() == player_color)
				&& (_board.getSpotAt(6, 5).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(3, 2));
			winningHighlight.add(_board.getSpotAt(4, 3));
			winningHighlight.add(_board.getSpotAt(5, 4));
			winningHighlight.add(_board.getSpotAt(6, 5));
			return true;
		} else if ((_board.getSpotAt(2, 0).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 1).getSpotColor() == player_color)
				&& (_board.getSpotAt(4, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(5, 3).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(2, 0));
			winningHighlight.add(_board.getSpotAt(3, 1));
			winningHighlight.add(_board.getSpotAt(4, 2));
			winningHighlight.add(_board.getSpotAt(5, 3));
			return true;
		} else if ((_board.getSpotAt(6, 4).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 1).getSpotColor() == player_color)
				&& (_board.getSpotAt(4, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(5, 3).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(6, 4));
			winningHighlight.add(_board.getSpotAt(3, 1));
			winningHighlight.add(_board.getSpotAt(4, 2));
			winningHighlight.add(_board.getSpotAt(5, 3));
			return true;
		} else if ((_board.getSpotAt(3, 0).getSpotColor() == player_color)
				&& (_board.getSpotAt(4, 1).getSpotColor() == player_color)
				&& (_board.getSpotAt(5, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(6, 3).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(3, 0));
			winningHighlight.add(_board.getSpotAt(4, 1));
			winningHighlight.add(_board.getSpotAt(5, 2));
			winningHighlight.add(_board.getSpotAt(6, 3));
			return true;
		} // diagonal upper right
		else if ((_board.getSpotAt(0, 3).getSpotColor() == player_color)
				&& (_board.getSpotAt(1, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(2, 1).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 0).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(0, 3));
			winningHighlight.add(_board.getSpotAt(1, 2));
			winningHighlight.add(_board.getSpotAt(2, 1));
			winningHighlight.add(_board.getSpotAt(3, 0));
			return true;
		} else if ((_board.getSpotAt(0, 4).getSpotColor() == player_color)
				&& (_board.getSpotAt(1, 3).getSpotColor() == player_color)
				&& (_board.getSpotAt(2, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 1).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(0, 4));
			winningHighlight.add(_board.getSpotAt(1, 3));
			winningHighlight.add(_board.getSpotAt(2, 2));
			winningHighlight.add(_board.getSpotAt(3, 1));
			return true;
		} else if ((_board.getSpotAt(1, 3).getSpotColor() == player_color)
				&& (_board.getSpotAt(2, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 1).getSpotColor() == player_color)
				&& (_board.getSpotAt(4, 0).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(1, 3));
			winningHighlight.add(_board.getSpotAt(2, 2));
			winningHighlight.add(_board.getSpotAt(3, 1));
			winningHighlight.add(_board.getSpotAt(4, 0));
			return true;
		} else if ((_board.getSpotAt(0, 5).getSpotColor() == player_color)
				&& (_board.getSpotAt(1, 4).getSpotColor() == player_color)
				&& (_board.getSpotAt(2, 3).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 2).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(0, 5));
			winningHighlight.add(_board.getSpotAt(1, 4));
			winningHighlight.add(_board.getSpotAt(2, 3));
			winningHighlight.add(_board.getSpotAt(3, 2));
			return true;
		} else if ((_board.getSpotAt(1, 4).getSpotColor() == player_color)
				&& (_board.getSpotAt(2, 3).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(4, 1).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(1, 4));
			winningHighlight.add(_board.getSpotAt(2, 3));
			winningHighlight.add(_board.getSpotAt(3, 2));
			winningHighlight.add(_board.getSpotAt(4, 1));
			return true;
		} else if ((_board.getSpotAt(2, 3).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(4, 1).getSpotColor() == player_color)
				&& (_board.getSpotAt(5, 0).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(2, 3));
			winningHighlight.add(_board.getSpotAt(3, 2));
			winningHighlight.add(_board.getSpotAt(4, 1));
			winningHighlight.add(_board.getSpotAt(5, 0));
			return true;
		} else if ((_board.getSpotAt(1, 5).getSpotColor() == player_color)
				&& (_board.getSpotAt(2, 4).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 3).getSpotColor() == player_color)
				&& (_board.getSpotAt(4, 2).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(1, 5));
			winningHighlight.add(_board.getSpotAt(2, 4));
			winningHighlight.add(_board.getSpotAt(3, 3));
			winningHighlight.add(_board.getSpotAt(4, 2));
			return true;
		} else if ((_board.getSpotAt(2, 4).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 3).getSpotColor() == player_color)
				&& (_board.getSpotAt(4, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(5, 1).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(2, 4));
			winningHighlight.add(_board.getSpotAt(3, 3));
			winningHighlight.add(_board.getSpotAt(4, 2));
			winningHighlight.add(_board.getSpotAt(5, 1));
			return true;
		} else if ((_board.getSpotAt(3, 3).getSpotColor() == player_color)
				&& (_board.getSpotAt(4, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(5, 1).getSpotColor() == player_color)
				&& (_board.getSpotAt(6, 0).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(3, 3));
			winningHighlight.add(_board.getSpotAt(4, 2));
			winningHighlight.add(_board.getSpotAt(5, 1));
			winningHighlight.add(_board.getSpotAt(6, 0));
			return true;
		} else if ((_board.getSpotAt(2, 5).getSpotColor() == player_color)
				&& (_board.getSpotAt(3, 4).getSpotColor() == player_color)
				&& (_board.getSpotAt(4, 3).getSpotColor() == player_color)
				&& (_board.getSpotAt(5, 2).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(2, 5));
			winningHighlight.add(_board.getSpotAt(3, 4));
			winningHighlight.add(_board.getSpotAt(4, 3));
			winningHighlight.add(_board.getSpotAt(5, 2));
			return true;
		} else if ((_board.getSpotAt(3, 4).getSpotColor() == player_color)
				&& (_board.getSpotAt(4, 3).getSpotColor() == player_color)
				&& (_board.getSpotAt(5, 2).getSpotColor() == player_color)
				&& (_board.getSpotAt(6, 1).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(3, 4));
			winningHighlight.add(_board.getSpotAt(4, 3));
			winningHighlight.add(_board.getSpotAt(5, 2));
			winningHighlight.add(_board.getSpotAt(6, 1));
			return true;
		} else if ((_board.getSpotAt(3, 5).getSpotColor() == player_color)
				&& (_board.getSpotAt(4, 4).getSpotColor() == player_color)
				&& (_board.getSpotAt(5, 3).getSpotColor() == player_color)
				&& (_board.getSpotAt(6, 2).getSpotColor() == player_color)) {
			winningHighlight.add(_board.getSpotAt(3, 5));
			winningHighlight.add(_board.getSpotAt(4, 4));
			winningHighlight.add(_board.getSpotAt(5, 3));
			winningHighlight.add(_board.getSpotAt(6, 2));
			return true;
		}
		// horizontal
		for (int i = 0; i < _board.getSpotHeight(); i++) {

			if ((_board.getSpotAt(0, i).getSpotColor() == player_color)
					&& (_board.getSpotAt(1, i).getSpotColor() == player_color)
					&& (_board.getSpotAt(2, i).getSpotColor() == player_color)
					&& (_board.getSpotAt(3, i).getSpotColor() == player_color)) {
				winningHighlight.add(_board.getSpotAt(0, i));
				winningHighlight.add(_board.getSpotAt(1, i));
				winningHighlight.add(_board.getSpotAt(2, i));
				winningHighlight.add(_board.getSpotAt(3, i));
				return true;
			}

		}
		for (int i = 0; i < _board.getSpotHeight(); i++) {

			if ((_board.getSpotAt(1, i).getSpotColor() == player_color)
					&& (_board.getSpotAt(2, i).getSpotColor() == player_color)
					&& (_board.getSpotAt(3, i).getSpotColor() == player_color)
					&& (_board.getSpotAt(4, i).getSpotColor() == player_color)) {
				winningHighlight.add(_board.getSpotAt(4, i));
				winningHighlight.add(_board.getSpotAt(1, i));
				winningHighlight.add(_board.getSpotAt(2, i));
				winningHighlight.add(_board.getSpotAt(3, i));
				return true;
			}

		}
		for (int i = 0; i < _board.getSpotHeight(); i++) {

			if ((_board.getSpotAt(2, i).getSpotColor() == player_color)
					&& (_board.getSpotAt(3, i).getSpotColor() == player_color)
					&& (_board.getSpotAt(4, i).getSpotColor() == player_color)
					&& (_board.getSpotAt(5, i).getSpotColor() == player_color)) {
				winningHighlight.add(_board.getSpotAt(4, i));
				winningHighlight.add(_board.getSpotAt(5, i));
				winningHighlight.add(_board.getSpotAt(2, i));
				winningHighlight.add(_board.getSpotAt(3, i));
				return true;
			}

		}
		for (int i = 0; i < _board.getSpotHeight(); i++) {

			if ((_board.getSpotAt(3, i).getSpotColor() == player_color)
					&& (_board.getSpotAt(4, i).getSpotColor() == player_color)
					&& (_board.getSpotAt(5, i).getSpotColor() == player_color)
					&& (_board.getSpotAt(6, i).getSpotColor() == player_color)) {
				winningHighlight.add(_board.getSpotAt(6, i));
				winningHighlight.add(_board.getSpotAt(5, i));
				winningHighlight.add(_board.getSpotAt(2, i));
				winningHighlight.add(_board.getSpotAt(3, i));
				return true;
			}

		}

		return false;
	}

}
