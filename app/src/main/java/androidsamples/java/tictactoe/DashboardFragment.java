package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";

    private NavController mNavController;
    private FirebaseAuth firebaseAuth;
    private Database db;
    private GameViewModel gameViewModel;
    private RecyclerView recyclerView;
    private TextView name,wins,losses;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DashboardFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        gameViewModel = new ViewModelProvider(getActivity()).get(GameViewModel.class);
        setHasOptionsMenu(true); // Needed to display the action menu for this fragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNavController = Navigation.findNavController(view);
        firebaseAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.list);

        name = view.findViewById(R.id.txt_score);
        wins = view.findViewById(R.id.txt_wins);
        losses = view.findViewById(R.id.txt_losses);



        Log.d(TAG, "Inside onViewCreated");

        if (firebaseAuth.getCurrentUser() == null) {
            Log.d(TAG, "Auth needed. Navigating...");
            Navigation.findNavController(view).navigate(R.id.action_need_auth);
            return;
        } else {
            Log.d(TAG, "User Session detected. Fetching user profile");

            db.UserCollection().document(firebaseAuth.getCurrentUser().getUid())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        User currentUser = task.getResult().toObject(User.class);
                        Log.d(TAG, "Fetched user profile... Creating new game!");
                        gameViewModel.setCurrentUser(currentUser);
                        init(view);
                        updateUI();
                    } else {
                        Log.w(TAG, "Could not find user profile. Logging out!");
                        firebaseAuth.signOut();
                        mNavController.navigate(R.id.action_need_auth);
                    }
                }
            });
        }
    }

    private void updateUI() {
        name.setText(gameViewModel.getCurrentUser().name);
        wins.setText("Wins : "+Integer.toString(gameViewModel.getCurrentUser().wins));
        losses.setText("Losses : "+Integer.toString(gameViewModel.getCurrentUser().losses));

        // wins.setText(gameViewModel.getCurrentUser().wins);
        // losses.setText(gameViewModel.getCurrentUser().losses);
    }

    public void init(View view)
    {
        view.findViewById(R.id.fab_new_game).setOnClickListener(v -> {

            // A listener for the positive and negative buttons of the dialog
            DialogInterface.OnClickListener listener = (dialog, which) -> {
                String gameType = "No type";
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    createNewGame(gameViewModel.getCurrentUser(), GameType.TWO_PLAYER);
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                    createNewGame(gameViewModel.getCurrentUser(), GameType.ONE_PLAYER);
                }
                Log.d(TAG, "New Game: " + gameType);
            };

            // create the dialog
            AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.new_game)
                    .setMessage(R.string.new_game_dialog_message)
                    .setPositiveButton(R.string.two_player, listener)
                    .setNegativeButton(R.string.one_player, listener)
                    .setNeutralButton(R.string.cancel, (d, which) -> d.dismiss())
                    .create();
            dialog.show();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        OpenGamesAdapter myAdapter = new OpenGamesAdapter();
        recyclerView.setAdapter(myAdapter);

        db.GameCollection().addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen Failed : ", error);
                    return;
                }
                List<TicTacToe> allGames = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    // TODO Check Game status
                    TicTacToe g = doc.toObject(TicTacToe.class);
                    if(g.player1.userid.equals(gameViewModel.getCurrentUser().userid))
                        continue;
                    if(g.gameType == GameType.ONE_PLAYER)
                        continue;

                    g.setGameId(doc.getId());
                    allGames.add(g);
                }
                myAdapter.setGames(allGames);
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_logout, menu);
        // this action menu is handled in MainActivity
    }


    private void createNewGame(User player1, GameType gameType) {

        TicTacToe game;
            if(gameType == GameType.TWO_PLAYER)
         game = new TicTacToe(player1, gameType, GameStatus.WAITING);
            else
                game = new TicTacToe(player1,gameType,GameStatus.PLAYING);

        db.GameCollection().add(game).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                String gameId = task.getResult().getId();
                Log.d(TAG,"New Game Created: with Id"+gameId);
                NavDirections action = DashboardFragmentDirections.actionGame(gameId,"-1");
                mNavController.navigate(action);
            }
        });
    }
}


class GameHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mIdView;
    public final TextView mContentView;

    public GameHolder(View view) {
        super(view);
        mView = view;
        mIdView = view.findViewById(R.id.item_number);
        mContentView = view.findViewById(R.id.content);
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + " '" + mContentView.getText() + "'";
    }
}