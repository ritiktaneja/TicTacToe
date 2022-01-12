package androidsamples.java.tictactoe;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

public class User {
    public String name,email,password,userid;
    public int wins,losses,total_games;

    public User(String name,String email, String password,String uid)
    {
        this.name = name;
        this.email = email;
        this.password = password;
        this.userid = uid;
        this.wins = this.losses = this.total_games = 0;
    }
    public User()
    {

    }

    public User(FirebaseUser fb)
    {
        this.name = fb.getDisplayName();
        this.email = fb.getEmail();
        this.password = fb.getUid();
        this.userid = fb.getUid();
    }
    @Override
    public String toString()
    {
        return userid;
    }

    public void setTotal_games(int total_games) {
        this.total_games = total_games;
    }

    public int getTotal_games() {
        return total_games;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getWins() {
        return wins;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getLosses() {
        return losses;
    }

    @Override
    public boolean equals(Object o)
    {
        Log.d("UserClass","Custom Comparator");
        if(o == this)
                return true;
        if(!(o instanceof User))
            return false;
        User b = (User) o;
        return this.userid == b.userid;
    }
}
