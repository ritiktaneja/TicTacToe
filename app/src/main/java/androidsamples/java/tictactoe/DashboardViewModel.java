package androidsamples.java.tictactoe;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class DashboardViewModel extends ViewModel {

    private  static final String TAG = "Dashboard ViewModel";
    private  static FirestoreRepository mFirestoreRepository;

    public DashboardViewModel()
    {
        mFirestoreRepository = FirestoreRepository.getInstance();
    }
    User getCurrentUser() {
       return mFirestoreRepository.getUser();
    }






}
