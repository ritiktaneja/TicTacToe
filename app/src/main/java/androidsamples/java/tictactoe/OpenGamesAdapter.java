package androidsamples.java.tictactoe;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class OpenGamesAdapter extends RecyclerView.Adapter<OpenGamesAdapter.ViewHolder> {


  private static final String TAG = "OpenGamesAdapter" ;
  List<TicTacToe> mGames;

  public OpenGamesAdapter() {
    mGames = null;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if(mGames != null)
        {
          TicTacToe current = mGames.get(position);
          holder.bind(current);
        }
  }

  @Override
  public int getItemCount() {
    if(mGames!=null)
      return mGames.size();
    return 0;
  }

  public void setGames(List<TicTacToe> newGames)
  {
    Log.d(TAG,"Updating open Games : "+newGames.size());
    this.mGames = newGames;
    notifyDataSetChanged();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mIdView;
    public final TextView mContentView;
    private TicTacToe myGame;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mIdView = view.findViewById(R.id.item_number);
      mContentView = view.findViewById(R.id.content);
      view.setOnClickListener(this::launchGame);
    }

    private void launchGame(View view)  {
      if(myGame!=null)
      {
        Log.d(TAG,"Launching Game : "+myGame);
        NavDirections action = DashboardFragmentDirections.actionGame(myGame.gameId, "TWO_PLAYER");
        Navigation.findNavController(view).navigate(action);
      }
      else
         Log.w(TAG,"Game not initialized");
    }

    public void bind(TicTacToe game)
    {
      myGame = game;
      if(game.hasTerminated())
      {
        this.mIdView.setText("Closed Game");
        this.mIdView.setTextColor(Color.RED);
      }
      else
      {
        this.mIdView.setText("Open Game");
        this.mIdView.setTextColor(Color.GREEN);
      }
      if(game.player1!=null)
      this.mContentView.setText("By  "+game.player1.name);
    }

    @NonNull
    @Override
    public String toString() {
      return super.toString() + " '" + mContentView.getText() + "'";
    }
  }
}