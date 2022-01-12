package androidsamples.java.tictactoe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    Fragment currentFragment  = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
    if(currentFragment == null)
    {
      Fragment fragment = new DashboardFragment();
      getSupportFragmentManager().beginTransaction().add(R.id.nav_host_fragment,fragment).commit();
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_logout) {
      Log.d(TAG, "logout clicked");
      // TODO handle log out
      FirebaseAuth.getInstance().signOut();
      navigateToLogin();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void navigateToLogin()
  {
    Navigation.findNavController(this,R.id.nav_host_fragment).navigate(R.id.action_need_auth);
  }
}