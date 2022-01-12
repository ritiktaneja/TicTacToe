package androidsamples.java.tictactoe;

import android.util.Log;

import androidx.lifecycle.ViewModel;

public class GameViewModel extends ViewModel {

        private static final String TAG = "GameViewModel";

        private User currentUser;
        private TicTacToe currentGame;

    public void setCurrentGame(TicTacToe currentGame) {
        Log.d(TAG,"Current game : "+currentGame);
        this.currentGame = currentGame;
    }

    public TicTacToe getCurrentGame() {
        return currentGame;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

}
