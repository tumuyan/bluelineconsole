package net.nhiroki.bluelineconsole.commandSearchers.eachSearcher;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;

import java.util.ArrayList;
import java.util.List;

public class ColorDisplayCommandSearcher implements CommandSearcher {
    @Override
    public void refresh(Context context) {}

    @Override
    public void close() {}

    @Override
    public boolean isPrepared() {
        return true;
    }

    @Override
    public void waitUntilPrepared() {}

    private static int hexCharToInt(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        return -1;
    }

    private static int[] getColorFromCode(String colorCode) {
        int[] ret = new int[3];

        for (int i = 0; i < 3; ++i) {
            int firstHex = hexCharToInt(colorCode.charAt(i * 2 + 1));
            if (firstHex < 0) {
                return null;
            }
            int secondHex = hexCharToInt(colorCode.charAt(i * 2 + 2));
            if (secondHex < 0) {
                return null;
            }

            ret[i] = firstHex * 16 + secondHex;
        }

        return ret;
    }

    @NonNull
    @Override
    public List<CandidateEntry> searchCandidateEntries(String query, Context context) {
        if (query.length() == 7 && query.charAt(0) == '#') {
            int[] color = getColorFromCode(query);

            if (color == null) {
                return new ArrayList<>();
            }

            List<CandidateEntry> ret = new ArrayList<>();
            // This query is included in detail View, so no need to write it on title.
            ret.add(new ColorDisplayCandidateEntry(null, color));
            return ret;
        }
        return new ArrayList<>();
    }

    private static class ColorDisplayCandidateEntry implements CandidateEntry {
        private final String title;
        private final int[] color;

        public ColorDisplayCandidateEntry(String title, int[] color) {
            this.title = title;
            this.color = color;
        }

        @NonNull
        @Override
        public String getTitle() {
            return this.title;
        }

        @Override
        public View getView(Context context) {
            final double pxPerDp = context.getResources().getDisplayMetrics().density;

            LinearLayout ret = new LinearLayout(context);
            ret.setOrientation(LinearLayout.HORIZONTAL);

            final int brightness = (color[0] + color[1] + color[2]) / 3;

            LinearLayout colorShowOuter = new LinearLayout(context);
            final int readableColorOnMonitor = brightness > 80 ? Color.BLACK : Color.WHITE;
            colorShowOuter.setBackgroundColor(readableColorOnMonitor);
            final int colorShowOuterPadding = (int)(2.0 * pxPerDp);
            colorShowOuter.setPadding(colorShowOuterPadding, colorShowOuterPadding, colorShowOuterPadding, colorShowOuterPadding);
            LinearLayout.LayoutParams layoutParamsForColorShowOuter = (LinearLayout.LayoutParams) colorShowOuter.getLayoutParams();
            if (layoutParamsForColorShowOuter == null) {
                layoutParamsForColorShowOuter = new LinearLayout.LayoutParams(0, 0);
            }
            layoutParamsForColorShowOuter.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParamsForColorShowOuter.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            colorShowOuter.setLayoutParams(layoutParamsForColorShowOuter);
            layoutParamsForColorShowOuter.setMarginEnd((int) (8.0 * pxPerDp));

            View colorShow = new View(context);
            colorShow.setBackgroundColor((255 << 24) + (this.color[0] << 16) + (this.color[1] << 8) + this.color[2]);
            LinearLayout.LayoutParams layoutParamsForColorShow = (LinearLayout.LayoutParams) colorShow.getLayoutParams();
            if (layoutParamsForColorShow == null) {
                layoutParamsForColorShow = new LinearLayout.LayoutParams(0, 0);
            }

            final int colorShowSize = (int) (96.0 * pxPerDp);
            layoutParamsForColorShow.height = colorShowSize;
            layoutParamsForColorShow.width = colorShowSize;
            colorShow.setLayoutParams(layoutParamsForColorShow);

            colorShowOuter.addView(colorShow);
            ret.addView(colorShowOuter);

            final TypedValue baseTextColor = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.bluelineconsoleBaseTextColor, baseTextColor, true);

            LinearLayout detail = new LinearLayout(context);
            detail.setOrientation(LinearLayout.VERTICAL);

            TextView colorCodeTextView = new TextView(context);
            colorCodeTextView.setText(String.format("#%02X%02X%02X", color[0], color[1], color[2]));
            colorCodeTextView.setTypeface(null, Typeface.BOLD);
            colorCodeTextView.setTextColor(baseTextColor.data);
            detail.addView(colorCodeTextView);

            TextView rgbTextView = new TextView(context);
            rgbTextView.setText(String.format(context.getString(R.string.rgb_color_display), color[0], color[1], color[2]));
            rgbTextView.setTextColor(baseTextColor.data);
            detail.addView(rgbTextView);

            ret.addView(detail);
            return ret;
        }

        @Override
        public boolean hasLongView() {
            return false;
        }

        @Override
        public EventLauncher getEventLauncher(Context context) {
            return null;
        }

        @Override
        public Drawable getIcon(Context context) {
            return null;
        }

        @Override
        public boolean hasEvent() {
            return false;
        }

        @Override
        public boolean isSubItem() {
            return false;
        }

        @Override
        public boolean viewIsRecyclable() {
            return true;
        }
    }
}
