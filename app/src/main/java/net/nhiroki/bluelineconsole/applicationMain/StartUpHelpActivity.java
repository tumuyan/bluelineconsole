package net.nhiroki.bluelineconsole.applicationMain;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;

import net.nhiroki.bluelineconsole.R;

public class StartUpHelpActivity extends BaseWindowActivity {
    public StartUpHelpActivity() {
        super(R.layout.start_up_help_body, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHeaderFooterTexts(getString(R.string.startup_help_title_for_header_and_footer), null);

        setResult(RESULT_OK, new Intent(this, MainActivity.class));

        this.setWindowLocationGravity(Gravity.CENTER_VERTICAL);

        Point displaySize = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(displaySize);
        int maxPanelWidth = (int)(560.0 * getResources().getDisplayMetrics().density);
        int panelWidth = Math.min((int)(displaySize.x * ((displaySize.x < displaySize.y) ? 0.8 : 0.67)), maxPanelWidth);
        this.setRootPadding((displaySize.x - panelWidth) / 2, 0);

        findViewById(R.id.startUpOKButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (((CheckBox) findViewById(R.id.startUpCheckBoxNotToContinue)).isChecked()) {
                    SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(StartUpHelpActivity.this).edit();
                    prefEdit.putBoolean("pref_main_show_startup_help", false);
                    prefEdit.apply();
                }
                StartUpHelpActivity.this.finish();
            }
        });

        this.changeBaseWindowElementSize(false);
        this.enableBaseWindowAnimation();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.changeBaseWindowElementSize(hasFocus);
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }
}