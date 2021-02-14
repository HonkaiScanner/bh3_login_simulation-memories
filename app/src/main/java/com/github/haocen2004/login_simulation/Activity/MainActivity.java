package com.github.haocen2004.login_simulation.Activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.databinding.ActivityMainBinding;

import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_NAME;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(binding.mainInclude.toolbar);
        navController = Navigation.findNavController(this, R.id.hostFragment);
        appBarConfiguration = new AppBarConfiguration
                .Builder(R.id.mainFragment)
                .setOpenableLayout(binding.drawerLayout)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navigationView, navController);
        binding.textView2.setText(VERSION_NAME);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            String toolbarTitle = "DEBUG WRONG TITLE";
            if (destination.getId() == R.id.mainFragment) {
                toolbarTitle = getString(R.string.page_main);
            }
            if (destination.getId() == R.id.reportFragment) {
                toolbarTitle = getString(R.string.list_report);
            }
            if (destination.getId() == R.id.supportFragment) {
                toolbarTitle = getString(R.string.list_pay);
            }
            if (destination.getId() == R.id.settingsFragment) {
                toolbarTitle = getString(R.string.list_settings);
            }
            binding.mainInclude.collapsingToolbarLayout.setTitle(toolbarTitle);
//            ((CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout)).setTitle(toolbarTitle);
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


}