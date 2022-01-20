package net.nhiroki.bluelineconsole.commandSearchers.eachSearcher;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import net.nhiroki.bluelineconsole.R;
import net.nhiroki.lib.bluelinecalculator.Calculator;
import net.nhiroki.lib.bluelinecalculator.CalculatorExceptions;
import net.nhiroki.lib.bluelinecalculator.CalculatorNumber;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;
import net.nhiroki.bluelineconsole.interfaces.CommandSearcher;
import net.nhiroki.bluelineconsole.interfaces.EventLauncher;

import org.mvel2.MVEL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculatorCommandSearcher implements CommandSearcher {
    @Override
    public void refresh(Context context) {

    }
    private static Map<String, Object> paramMap = new HashMap<>();

    @Override
    @NonNull
    public List<CandidateEntry> searchCandidateEntries(String query, Context context) {
        List <CandidateEntry> cands = new ArrayList<>();

        if (query.matches(".*([0-9()]+.*)")) {

            if (Calculator.seemsExpression(query)) {
                cands.add(new CalculatorCandidateEntry(query, context, ""));
            } else {
                String result = evalMvel(query);
                if (!result.isEmpty())
                    cands.add(new CalculatorCandidateEntry(query, context, result));
            }
        }
        return cands;
    }

    public static String evalMvel(String query) {
        if (query.matches("(^|.*[+\\-*/\\s(])(rand|randint)\\s*[(].*")) {
            try {
                MVEL.eval(
                        "def rand(){ Math.random();} " +
                                "def randint(a){ b = (int)(Math.random()*a); return b>0?b:a}"
                        , paramMap);
            } catch (Exception e) {
            }
        }

        try {
            Object object = MVEL.eval(query, paramMap);
            return object == null ? "" : object.toString();
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void close() {}

    @Override
    public boolean isPrepared() {
        return true;
    }

    @Override
    public void waitUntilPrepared() {}

    private static class CalculatorCandidateEntry implements CandidateEntry {
        private final String title;
        private final List <Pair<String, String>> results;

        CalculatorCandidateEntry(String s, Context context, String result) {

            this.results = new ArrayList<>();

            final char ltr = 0x200e;
            this.title = ltr + s;

            if (result.isEmpty()) {

                try {
                    List<CalculatorNumber> res = Calculator.calculate(s);
                    for (CalculatorNumber r : res) {
                        this.results.add(new Pair<>(ltr + (r.getPrecision() == CalculatorNumber.Precision.PRECISION_NO_ERROR ? "= " : "≒ ") + r.generateFinalString(),
                                String.format(context.getString(R.string.calculator_precision_format), getPrecisionText(context, r.getPrecision()))));
                    }

                } catch (CalculatorExceptions.UnitConversionException e) {
                    this.results.add(new Pair<String, String>(String.format(context.getString(R.string.calculator_error_unit_conversion_failure), e.getFrom().calculateDisplayName(), e.getTo().calculateDisplayName()), null));

                } catch (CalculatorExceptions.IllegalFormulaException e) {
                    this.results.add(new Pair<String, String>("= ...", null));

                } catch (CalculatorExceptions.DivisionByZeroException e) {
                    this.results.add(new Pair<String, String>(context.getString(R.string.calculator_error_division_by_zero), null));

                } catch (Exception e) {
                    // Keep this function new not to happen this error
                    this.results.add(new Pair<>(context.getString(R.string.error_calculator_internal_error), e.toString()));
                }

                for (Pair<String, String> pair : this.results) {
                    if (!pair.first.contains("...")) {
                        return;
                    }
                }

                result = evalMvel(s);
                if (!result.isEmpty()) {
                    this.results.clear();
                    this.results.add(new Pair<>(ltr + "= " + result, ""));
                }

            } else {
                this.results.add(new Pair<>(ltr + "= " + result, ""));
            }
        }

        @Override
        @NonNull
        public String getTitle() {
            return title;
        }

        @Override
        public boolean hasLongView() {
            return true;
        }

        @Override
        public View getView(Context context) {
            LinearLayout ret = new LinearLayout(context);
            ret.setOrientation(LinearLayout.VERTICAL);
            ret.setTextDirection(View.TEXT_DIRECTION_LTR);

            final TypedValue baseTextColor = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.bluelineconsoleBaseTextColor, baseTextColor, true);

            for (Pair<String, String> r: this.results) {
                TextView resultView = new TextView(context);

                resultView.setText(r.first);
                resultView.setTextSize(TypedValue.COMPLEX_UNIT_SP, r.first.length() < 16 ? 60 : 36);
                resultView.setTypeface(null, Typeface.BOLD);
                resultView.setTextColor(baseTextColor.data);

                ret.addView(resultView);

                if (r.second != null) {
                    TextView precisionView = new TextView(context);
                    precisionView.setText(r.second);
                    precisionView.setTypeface(null, Typeface.BOLD);
                    ret.addView(precisionView);
                }
            }

            return ret;
        }

        private String getPrecisionText(Context context, int precision) {
            switch (precision) {
                case CalculatorNumber.Precision.PRECISION_NO_ERROR:
                    return context.getString(R.string.calculator_precision_no_error);
                case CalculatorNumber.Precision.PRECISION_SCALE_20:
                    return context.getString(R.string.calculator_precision_rounding);
                default:
                    throw new RuntimeException("Unknown precision ID");
            }
        }


        @Override
        public boolean hasEvent() {
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
        public boolean isSubItem() {
            return false;
        }

        @Override
        public boolean viewIsRecyclable() {
            return true;
        }
    }
}
