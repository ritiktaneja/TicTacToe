package androidsamples.java.tictactoe;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private static FirebaseFirestore firebaseFirestore;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(), new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        // TODO if a user is logged in, go to Dashboard
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        view.findViewById(R.id.btn_log_in)
                .setOnClickListener(v -> {
                    // TODO implement sign in login
                    NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
                    Navigation.findNavController(v).navigate(action);
                });

        if(FirebaseAuth.getInstance().getCurrentUser()!=null)
        {
            Log.d(TAG,"User Already Logged in. Navigating to Dashboard...");
            Navigation.findNavController(view).navigate(R.id.action_login_successful);
        }

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.TwitterBuilder().build());

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);


        firebaseFirestore = FirebaseFirestore.getInstance();
        return view;
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        Log.d(TAG,"Signing in. Response code : "+result.getResultCode());

        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            if(response.isNewUser())
            {
                createNewUser();
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            Toast.makeText(getActivity().getApplicationContext(), "Welcome "+user.getDisplayName()+"!", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(getView()).navigate(R.id.action_login_successful);
        } else {

            Toast.makeText(getActivity().getApplicationContext(), "User Login Failed", Toast.LENGTH_SHORT).show();
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }

    private void createNewUser()
    {
        Log.d(TAG,"Creating new user");
        FirebaseUser firebaseuser = FirebaseAuth.getInstance().getCurrentUser();
        User newUser = new User(firebaseuser);
        firebaseFirestore.collection("Users").document(firebaseuser.getUid()).set(newUser);
    }


    // No options menu in login fragment.
}