package com.example.note;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    public static String contentToProcessFromExternal = null;
    public static int destinationTabFromExternal = -1;

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabMain;
    private MaterialToolbar topAppBar;

    private Menu topMenu;
    private Intent pendingIntentToHandle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fabMain = findViewById(R.id.fabMain);
        topAppBar = findViewById(R.id.topAppBar);

        setSupportActionBar(topAppBar);

        fabMain.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddNoteActivity.class));
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (MainActivity.destinationTabFromExternal == -1) {
                navigateToFragment(item.getItemId(), null);
            }
            return true;
        });

        pendingIntentToHandle = getIntent();

        if (savedInstanceState == null && (MainActivity.destinationTabFromExternal == -1 && (pendingIntentToHandle == null || !pendingIntentToHandle.hasExtra("NAVIGATE_TO_TAB")))) {
            navigateToFragment(R.id.navigation_notes, null);
            bottomNavigationView.setSelectedItemId(R.id.navigation_notes);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_app_bar_menu, menu);
        this.topMenu = menu;

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Cari catatan...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof HomeFragment) {
                    ((HomeFragment) currentFragment).performSearch(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof HomeFragment) {
                    ((HomeFragment) currentFragment).performSearch(newText);
                }
                return true;
            }
        });

        // Inisialisasi: panggil invalidate agar onPrepareOptionsMenu dipanggil
        invalidateOptionsMenu();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Logika visibilitas dipusatkan di sini, akan dipanggil setiap kali
        // invalidateOptionsMenu() dipanggil atau saat menu perlu digambar ulang.
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        boolean shouldShowMenu = currentFragment instanceof HomeFragment;

        if(menu != null){
            menu.findItem(R.id.action_search).setVisible(shouldShowMenu);
            menu.findItem(R.id.action_sort).setVisible(shouldShowMenu);
            menu.findItem(R.id.action_category).setVisible(shouldShowMenu);
            menu.findItem(R.id.action_backup).setVisible(shouldShowMenu);
            menu.findItem(R.id.action_trash).setVisible(shouldShowMenu);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_category) {
            startActivity(new Intent(this, CategoryActivity.class));
            return true;
        } else if (itemId == R.id.action_sort) {
            Toast.makeText(this, "Fitur 'Urutkan' masih dalam pengembangan", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_backup) {
            Toast.makeText(this, "Fitur 'Cadangkan' masih dalam pengembangan", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_trash) {
            Toast.makeText(this, "Fitur 'Tempat Sampah' masih dalam pengembangan", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        pendingIntentToHandle = intent;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (destinationTabFromExternal != -1 && contentToProcessFromExternal != null) {
            int tabId = destinationTabFromExternal;
            String content = contentToProcessFromExternal;
            Bundle bundle = new Bundle();
            bundle.putString("NOTE_CONTENT_FROM_DETAIL", content);
            navigateToFragment(tabId, bundle);
            bottomNavigationView.setSelectedItemId(tabId);
            destinationTabFromExternal = -1;
            contentToProcessFromExternal = null;
        } else if (pendingIntentToHandle != null) {
            handleIntent(pendingIntentToHandle);
            pendingIntentToHandle = null;
        }
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("NAVIGATE_TO_TAB")) {
            int tabId = intent.getIntExtra("NAVIGATE_TO_TAB", -1);
            String content = intent.getStringExtra("NOTE_CONTENT_FOR_AI");
            if (tabId != -1 && content != null && !content.isEmpty()) {
                Bundle bundle = new Bundle();
                bundle.putString("NOTE_CONTENT_FROM_DETAIL", content);
                navigateToFragment(tabId, bundle);
                bottomNavigationView.setSelectedItemId(tabId);
            }
        }
    }

    public void navigateToFragment(int fragmentId, @Nullable Bundle bundle) {
        Fragment destinationFragment = null;
        if (fragmentId == R.id.navigation_notes) {
            destinationFragment = new HomeFragment();
            fabMain.show();
        } else if (fragmentId == R.id.navigation_inspiration) {
            destinationFragment = new InspirationFragment();
            fabMain.hide();
        } else if (fragmentId == R.id.navigation_flashcard) {
            destinationFragment = new FlashcardFragment();
            fabMain.hide();
        } else if (fragmentId == R.id.navigation_quiz) {
            destinationFragment = new QuizFragment();
            fabMain.hide();
        }

        if (destinationFragment != null) {
            if (bundle != null) {
                destinationFragment.setArguments(bundle);
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, destinationFragment)
                    .commit();

            // Minta Android untuk menggambar ulang menu setelah fragment diganti
            invalidateOptionsMenu();
        }
    }
}
