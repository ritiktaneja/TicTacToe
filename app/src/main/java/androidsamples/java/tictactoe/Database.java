package androidsamples.java.tictactoe;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Database {
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static User user = null;
    private static final String TAG = "FirestoreDB";

    public static CollectionReference UserCollection()
    {
        return db.collection("Users");
    }
    public  static CollectionReference GameCollection()
    {
        return db.collection("Games");
    }

    public static User getUser(FirebaseUser fb)
    {
        db.collection("Users").document(fb.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    Log.d(TAG,"here");
                    if(task.getResult().exists())
                    {
                        user = (User)task.getResult().toObject(User.class);
                    }
                }
                else
                {
                    Log.d(TAG,"Unsuccessful in fetching user data");
                }
            }
        });
        return user;
    }

    boolean addGame(TicTacToe g)
    {
      //  db.collection("Games").document(g.id).set(g);
        return false;
    }

}
