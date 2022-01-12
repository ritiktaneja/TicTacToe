package androidsamples.java.tictactoe;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreRepository {
   private static final String TAG = "Game Repository";

   private static FirestoreRepository sInstance = null;

   private static FirebaseFirestore db = FirebaseFirestore.getInstance();
   private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
   private static  User currentUser = null;

   private FirestoreRepository()
   {

   }
   static FirestoreRepository getInstance()
   {
      if(sInstance == null)
         sInstance = new FirestoreRepository();
      return sInstance;
   }
   public User getUser()
   {
      db.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if(task.isSuccessful() && task.getResult().exists())
            {
              currentUser = task.getResult().toObject(User.class);
            }
        }
     });
      return currentUser;
   }





}
