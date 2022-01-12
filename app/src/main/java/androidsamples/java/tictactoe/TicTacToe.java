package androidsamples.java.tictactoe;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

public class TicTacToe {
    public User player1, player2;
    public User currentChance;
    public GameStatus gameStatus;
    public GameType gameType;
    public String gameId;
    public String gameState;
    TicTacToe(User startedBy, GameType gameType,GameStatus gameStatus)
    {
        this.player1 = startedBy;
        this.gameType = gameType;
        this.gameStatus = gameStatus;
        this.player2 = null;
        this.currentChance = player1;
        this.gameState = ".........";
    }
    TicTacToe()
    {

    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
    @Override
    public String toString()
    {
        return player1+" "+gameStatus.toString()+" "+gameId;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    public void setCurrentChance(User currentChance) {
        this.currentChance = currentChance;
    }

    public User getCurrentChance() {
        return currentChance;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public void setPlayer2(User player2) {
        this.player2 = player2;
    }

    public User getPlayer2() {
        return player2;
    }

    @Override
    public boolean equals(Object o)
    {
        Log.d("TicTacToe","Equality matching");
        if(o == this)
            return true;

        if(!(o instanceof TicTacToe))
                return false;

        TicTacToe g = (TicTacToe)o;
        return this.gameId ==  g.gameId;
    }

    public boolean hasTerminated()
    {
        return this.gameStatus == GameStatus.DRAW || this.gameStatus == GameStatus.PLAYER1_WON || this.gameStatus == GameStatus.PLAYER2_WON || this.gameStatus == GameStatus.PLAYER_LEFT;
    }
}
