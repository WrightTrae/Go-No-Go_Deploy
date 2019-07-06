package com.wright.android.t_minus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;

import com.wright.android.t_minus.settings.PreferencesFragment;
import com.wright.android.t_minus.settings.account.LoginActivity;

public class OnBoardingActivity extends AppCompatActivity {
    public static final int STARTUP_DELAY = 300;
    public static final int ANIM_ITEM_DURATION = 1000;
    public static final int ITEM_DELAY = 300;
    public static final String loginScreen = "loginScreen";
    private SharedPreferences sharedPreferences;
    private boolean animationStarted = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);
        sharedPreferences = getSharedPreferences(PreferencesFragment.PREFS, MODE_PRIVATE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus || animationStarted) {
            return;
        }
        animate();
        super.onWindowFocusChanged(true);
    }

    private void changeLogin(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(loginScreen, true);
        editor.apply();
    }

    private void animate() {
        ImageView logoImageView = findViewById(R.id.img_logo);
        ViewGroup container = findViewById(R.id.container);
        findViewById(R.id.on_board_sign_in).setOnClickListener((View v)-> {
            changeLogin();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.on_board_skip).setOnClickListener((View v)-> {
            changeLogin();
            Intent intent = new Intent(getApplicationContext(), MainTabbedActivity.class);
            startActivity(intent);
            finish();
        });

        ViewCompat.animate(logoImageView)
                .translationY(-300)
                .setStartDelay(STARTUP_DELAY)
                .setDuration(ANIM_ITEM_DURATION).setInterpolator(
                new DecelerateInterpolator(1.2f)).start();

        for (int i = 0; i < container.getChildCount(); i++) {
            View subView = container.getChildAt(i);
            ViewPropertyAnimatorCompat viewAnimator;

            if (!(subView instanceof Button)) {
                viewAnimator = ViewCompat.animate(subView)
                        .translationY(50).alpha(1)
                        .setStartDelay((ITEM_DELAY * i) + 500)
                        .setDuration(1000);
            } else {
                viewAnimator = ViewCompat.animate(subView)
                        .scaleY(1).scaleX(1)
                        .setStartDelay((ITEM_DELAY * i) + 500)
                        .setDuration(500);
            }

            viewAnimator.setInterpolator(new DecelerateInterpolator()).start();
        }
    }
}
