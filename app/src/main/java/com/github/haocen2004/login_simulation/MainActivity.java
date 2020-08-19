package com.github.haocen2004.login_simulation;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences app_pref = getDefaultSharedPreferences(this);
        if (app_pref.getBoolean("is_first_run", true) || app_pref.getInt("version",1) != 2){
            app_pref.edit()

                    .putBoolean("is_first_run",false)
                    .putInt("version",2)

                    .apply();
//                                .putString("server_type","Official")
//                    .putBoolean("enable_ad",true)
//                    .putBoolean("auto_confirm",false)
            if (!app_pref.contains("auto_confirm")){
                app_pref.edit()
                        .putBoolean("auto_confirm",false)
                        .apply();
            }
            if (!app_pref.contains("enable_ad")){
                app_pref.edit()
                        .putBoolean("enable_ad",true)
                        .apply();
            }
            if (!app_pref.contains("server_type")){
                app_pref.edit()
                        .putString("server_type","Official")
                        .apply();
            }

        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navController = Navigation.findNavController(this, R.id.hostFragment);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        appBarConfiguration = new AppBarConfiguration
                .Builder(R.id.mainFragment,R.id.settingsFragment)
                .setDrawerLayout(drawerLayout)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationView navigationView = findViewById(R.id.navigationView);
        NavigationUI.setupWithNavController(navigationView, navController);


    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();

    }

}