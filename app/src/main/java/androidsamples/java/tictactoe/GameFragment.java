package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameFragment extends Fragment {
  private static final String TAG = "GameFragment";
  private static final int GRID_SIZE = 9;

  private final Button[] mButtons = new Button[GRID_SIZE];
  private TextView gameType;
  private NavController mNavController;
  private Database db;
  private FirebaseAuth firebaseAuth;
  private GameViewModel gameViewModel;

  private boolean init = true;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true); // Needed to display the action menu for this fragment


    // Handle the back press by adding a confirmation dialog
    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        Log.d(TAG, "Back pressed");
        if (gameViewModel.getCurrentGame() != null && (gameViewModel.getCurrentGame().gameStatus == GameStatus.PLAYING || gameViewModel.getCurrentGame().gameStatus == GameStatus.WAITING)) {
          // TODO show dialog only when the game is still in progress
          AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                  .setTitle(R.string.confirm)
                  .setMessage(R.string.forfeit_game_dialog_message)
                  .setPositiveButton(R.string.yes, (d, which) -> {
                    userLeft();
                    mNavController.popBackStack();
                  })
                  .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                  .create();
          dialog.show();
        }
        mNavController.popBackStack();
      }
    };
    requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

    gameViewModel = new ViewModelProvider(getActivity()).get(GameViewModel.class);


  }




  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_game, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mNavController = Navigation.findNavController(view);

    if(FirebaseAuth.getInstance().getCurrentUser() == null)
      mNavController.navigate(R.id.action_need_auth);


    firebaseAuth = FirebaseAuth.getInstance();

    GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());
    String gameId = args.getGameId();
    Log.d(TAG, "Game ID = " +  gameId);

    mButtons[0] = view.findViewById(R.id.button0);
    mButtons[1] = view.findViewById(R.id.button1);
    mButtons[2] = view.findViewById(R.id.button2);

    mButtons[3] = view.findViewById(R.id.button3);
    mButtons[4] = view.findViewById(R.id.button4);
    mButtons[5] = view.findViewById(R.id.button5);

    mButtons[6] = view.findViewById(R.id.button6);
    mButtons[7] = view.findViewById(R.id.button7);
    mButtons[8] = view.findViewById(R.id.button8);

    gameType = view.findViewById(R.id.gameType);


    addButtonListeners();
    db.GameCollection().document(gameId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
      @Override
      public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
        if(error != null)
        {
          Log.w(TAG,"Listen failed",error);
          return;
        }
        if(value != null && value.exists())
        {
          DocumentSnapshot dc = value;
          TicTacToe currentGame = dc.toObject(TicTacToe.class);
          currentGame.setGameId(dc.getId());
          gameViewModel.setCurrentGame(currentGame);
          Log.d(TAG," Fetched Game. Current Status : "+currentGame.gameStatus);
          if(currentGame.hasTerminated())
          {
            displayToast();
            return;
          }
          addSecondPlayer();
          updateUI();
        }
        else {
          Log.w(TAG,"Snapshot is null");
        }
      }
    });

  }


  private void addButtonListeners() {

    for (int i = 0; i < mButtons.length; i++) {
      int finalI = i;
      mButtons[i].setOnClickListener(v -> {
        TicTacToe game = gameViewModel.getCurrentGame();
        if(game == null)
        {
          Toast.makeText(getContext(),"Fetching game.",Toast.LENGTH_SHORT);
        }
        Log.d(TAG, "Button " + finalI + " clicked");
        Log.d(TAG, "Current User : " + gameViewModel.getCurrentUser().userid);

        if (game.gameStatus == GameStatus.WAITING) {
          Toast.makeText(getContext(), "Please wait for another player to join!", Toast.LENGTH_SHORT).show();
          return;
        } else if (game.gameStatus == GameStatus.PLAYER_LEFT) {
          Toast.makeText(getContext(), "Player Left. Your game has ended", Toast.LENGTH_SHORT).show();
          return;
        }

        if(game.hasTerminated())
        {

          return;
        }

        char x = game.gameState.charAt(finalI);
        if(x == '.' || x == ' ')
        {
          if(game.getCurrentChance().userid.equals (gameViewModel.getCurrentUser().userid))
          {
            Log.d(TAG,"Playing Move");
            updateGameState(finalI);
          }
          else {
                Log.d(TAG,"Not your chance! "+game.currentChance+ " - "+gameViewModel.getCurrentUser());
            Toast.makeText(getContext(), "Wait for another player!", Toast.LENGTH_SHORT).show();
          }
        }
      });
    }
  }

  private void displayToast() {

    User user = gameViewModel.getCurrentUser();
    GameStatus gameStatus = gameViewModel.getCurrentGame().gameStatus;
    User player1 = gameViewModel.getCurrentGame().player1;
    User player2 = gameViewModel.getCurrentGame().player2;
    if(user.userid.equals(player1.userid))
    {
      if(gameStatus == GameStatus.PLAYER1_WON)
      {
        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                .setTitle("You Won!")
                .setPositiveButton(R.string.yes, (d, which) -> {
                  mNavController.popBackStack();
                })
                .create();
        dialog.show();
      }
      else if(gameStatus == GameStatus.PLAYER2_WON)
      {
        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                .setTitle("You Lost!")
                .setPositiveButton(R.string.yes, (d, which) -> {
                  mNavController.popBackStack();
                })
                .create();
        dialog.show();
      }
      else if(gameStatus == GameStatus.DRAW)
      {
        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                .setTitle("Game Drawn!")
                .setPositiveButton(R.string.yes, (d, which) -> {
                  mNavController.popBackStack();
                })
                .create();
        dialog.show();
      }
    }
    else
    {
      if(gameStatus == GameStatus.PLAYER1_WON)
      {
        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                .setTitle("You Lost!")
                .setPositiveButton(R.string.yes, (d, which) -> {
                  mNavController.popBackStack();
                })
                .create();
        dialog.show();
      }
      else if(gameStatus == GameStatus.PLAYER2_WON)
      {
        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                .setTitle("You Won!")
                .setPositiveButton(R.string.yes, (d, which) -> {
                  mNavController.popBackStack();
                })
                .create();
        dialog.show();
      }
      else if(gameStatus == GameStatus.DRAW)
      {
        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                .setTitle("Game Drawn!")
                .setPositiveButton(R.string.yes, (d, which) -> {
                  mNavController.popBackStack();
                })
                .create();
        dialog.show();
      }
    }
  }

  private void addSecondPlayer() {


    TicTacToe game = gameViewModel.getCurrentGame();

    if(game.getPlayer2() != null)
        return;
    if(game.gameType == GameType.ONE_PLAYER)
    {
      game.setPlayer2(new User("Computer","computer@sdpd.com","123312","0001"));
      db.GameCollection().document(game.gameId).set(game).addOnCompleteListener(new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
          Log.d(TAG, "Second Player Joined!");
        }
      });

    }
   else if(game.gameStatus == GameStatus.WAITING && game.player1.userid.equals(gameViewModel.getCurrentUser().userid) == false)
    {
      game.setPlayer2(gameViewModel.getCurrentUser());
      game.setGameStatus(GameStatus.PLAYING);
      db.GameCollection().document(game.gameId).set(game).addOnCompleteListener(new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
          Log.d(TAG, "Second Player Joined!");
        }
      });

    }

  }

  private void updateGameState(int finalI) {
    TicTacToe newGame = gameViewModel.getCurrentGame();
    StringBuilder newGameState = new StringBuilder(newGame.gameState);

    if(newGame.player1.userid.equals(gameViewModel.getCurrentUser().userid))
    {
      newGameState.setCharAt(finalI,'X');
      newGame.setGameState(newGameState.toString());
      if(newGame.gameType == GameType.ONE_PLAYER)
      {
          newGame.setGameState(simulateMove(newGame));
      }
      else {
          newGame.setCurrentChance(newGame.player2);
      }
    }
    else
    {
      newGameState.setCharAt(finalI,'O');
      newGame.setGameState(newGameState.toString());
      newGame.setCurrentChance(newGame.player1);
    }

    GameStatus newGameStatus = getGameStatus(newGameState.toString());

    Log.d(TAG, "New Game Status : "+newGameStatus);
    newGame.setGameStatus(newGameStatus);

    db.GameCollection().document(newGame.gameId).set(newGame).addOnCompleteListener(new OnCompleteListener<Void>() {
      @Override
      public void onComplete(@NonNull Task<Void> task) {
        if(task.isSuccessful())
        {
            Log.d(TAG, "Played Move Pushed");
        }
        else
        {
          Log.d(TAG,"Error pushing played move");
        }
      }
    });

    if(newGameStatus == GameStatus.PLAYING || newGameStatus == GameStatus.WAITING || newGameStatus == GameStatus.PLAYER_LEFT)
        return;
    Log.d(TAG,"Game state : "+newGameStatus);
    User newPlayer1 = newGame.player1;
    User newPlayer2 = newGame.player2;


    if(newGameStatus == GameStatus.DRAW)
    {
      newPlayer1.setTotal_games(newPlayer1.getTotal_games()+1);
      newPlayer2.setTotal_games(newPlayer2.getTotal_games()+1);
    }
    else if(newGameStatus == GameStatus.PLAYER1_WON)
    {
      newPlayer1.setTotal_games(newPlayer1.getTotal_games()+1);
      newPlayer2.setTotal_games(newPlayer2.getTotal_games()+1);
      newPlayer1.setWins(newPlayer1.getWins()+1);
      newPlayer2.setLosses(newPlayer2.getLosses()+1);

    }
    else if(newGameStatus == GameStatus.PLAYER2_WON)
    {
      newPlayer1.setTotal_games(newPlayer1.getTotal_games()+1);
      newPlayer2.setTotal_games(newPlayer2.getTotal_games()+1);
      newPlayer2.setWins(newPlayer2.getWins()+1);
      newPlayer1.setLosses(newPlayer1.getLosses()+1);
    }

    // async tasks
    db.UserCollection().document(newPlayer1.userid).set(newPlayer1);
    db.UserCollection().document(newPlayer2.userid).set(newPlayer2);
  }



  private String simulateMove(TicTacToe newGame)
  {
      StringBuilder gameState = new StringBuilder(newGame.gameState);
      List<Integer> openPos = new ArrayList<Integer>();
      for(int i=0;i<9;i++)
      {
          if(gameState.charAt(i)=='.' || gameState.charAt(i)== ' ')
              openPos.add(i);
      }
      if(openPos.size()>0) {
        Random rand = new Random();
        int selectedPos = openPos.get(rand.nextInt(openPos.size()));
        gameState.setCharAt(selectedPos, 'O');
      }
     return gameState.toString();
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }

  private void updateUI()
  {
      for(int i=0;i<9;i++)
      {
        String state = gameViewModel.getCurrentGame().gameState;
        mButtons[i].setText(String.valueOf(state.charAt(i)));
      }
      if(gameViewModel.getCurrentGame().gameStatus.equals(GameStatus.WAITING))
        gameType.setText("Waiting for Player 2");
      else
        gameType.setText(gameViewModel.getCurrentGame().gameStatus.toString());
  }

  GameStatus getGameStatus(String gameState)
  {
    // XXX OOO XXX
    // checking Horizontals
    int cnt = 0;
    Log.d(TAG,"Checking Horizontals");
    for(int i=0;i<3;i++)
    {
        cnt = 0;
      for(int j=0;j<3;j++)
      {
        if(gameState.charAt(3*i+j)=='X')
            cnt++;
        else if(gameState.charAt(3*i+j)=='O')
            cnt--;
      }
      if(cnt == 3)
          return GameStatus.PLAYER1_WON;
      else if(cnt == -3)
          return GameStatus.PLAYER2_WON;
    }

    // Checking Verticals
    //0 3 6,1 4 7, 2 5 8
      Log.d(TAG,"Checking Verticals");
    for(int i=0;i<3;i++)
    {
      cnt = 0;
      for(int j=0;j<3;j++)
      {
        if(gameState.charAt(i+j*3)=='X')
          cnt++;
        else if(gameState.charAt(i+j*3)=='O')
          cnt--;
      }
      if(cnt == 3)
        return GameStatus.PLAYER1_WON;
      else if(cnt == -3)
        return GameStatus.PLAYER2_WON;
    }
    // Checking Diagonals
      Log.d(TAG,"Checking Leading Diagonal");
    cnt = 0;
    for(int i=0;i<9;i+=4)
    {
      if(gameState.charAt(i)=='X')
        cnt++;
      else if(gameState.charAt(i)=='O')
        cnt--;
    }

    if(cnt == 3)
      return GameStatus.PLAYER1_WON;
    else if(cnt == -3)
      return GameStatus.PLAYER2_WON;
      Log.d(TAG,"Checking Trailing Diagonal");
    cnt = 0;
    for(int i=2;i<7;i+=2)
    {
      if(gameState.charAt(i)=='X')
        cnt++;
      else if(gameState.charAt(i)=='O')
        cnt--;
    }

    if(cnt == 3)
      return GameStatus.PLAYER1_WON;
    else if(cnt == -3)
      return GameStatus.PLAYER2_WON;

    for(int i=0;i<9;i++)
        if(gameState.charAt(i) == ' ' || gameState.charAt(i) == '.')
            return GameStatus.PLAYING;

    return GameStatus.DRAW;
  }

  private void userLeft() {

    TicTacToe game = gameViewModel.getCurrentGame();
    game.setGameStatus(GameStatus.PLAYER_LEFT);

    db.GameCollection().document(game.gameId).set(game).addOnCompleteListener(new OnCompleteListener<Void>() {
      @Override
      public void onComplete(@NonNull Task<Void> task) {

      }
    });
  }

}