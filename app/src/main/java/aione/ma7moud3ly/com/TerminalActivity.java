package aione.ma7moud3ly.com;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class TerminalActivity extends AppCompatActivity {
    private TextView output;
    private EditText input;
    private ScrollView scroll;
    private TextView cursor;
    private TextView directory;
    public static int history_index;
    private ArrayList<String> history = new ArrayList<>();
    private Cursor dbCursor;
    public static SQLiteDatabase settingsDB;
    private int fontSize;
    private boolean isDark = true;
    private String current_directory = "";
    private Interface mInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getEditorSettings();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terminal);
        if (MainActivity.script_list.isEmpty()) MainActivity.init_list(this);
        init_terminal();
        script();
    }

    private void script() {
        Intent intent = getIntent();
        if (intent.hasExtra("path")) {
            String run = intent.getStringExtra("run");
            String path = intent.getStringExtra("path");
            output.append("$ " + path + "\n");
            new Interface(output, input, scroll, directory, run, path);
        }

    }

    private void init_terminal() {
        LinearLayout layout = findViewById(R.id.terminal_layout);
        if (isDark) layout.setBackgroundColor(getResources().getColor(android.R.color.black));
        else layout.setBackgroundColor(getResources().getColor(android.R.color.white));
        cursor = findViewById(R.id.cursor);
        input = findViewById(R.id.input);
        scroll = findViewById(R.id.scroll);
        directory = findViewById(R.id.directory);
        directory.setText(current_directory);
        directory.setTextIsSelectable(true);
        directory.setHorizontallyScrolling(true);
        directory.setHorizontalScrollBarEnabled(true);
        output = findViewById(R.id.output);
        output.setTextIsSelectable(true);
        //output.setHorizontallyScrolling(true);
        //output.setHorizontalScrollBarEnabled(true);
        output.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        input.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        cursor.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            output.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                    scroll.requestDisallowInterceptTouchEvent(true);
                }
            });
        }
        mInterface = new Interface(output, input, scroll, directory);
    }

    @Override
    protected void onResume() {
        dbCursor = settingsDB.rawQuery("select * from 'history'", null);
        while (dbCursor.moveToNext()) history.add(dbCursor.getString(0));
        history_index = history.size() - 1;
        super.onResume();
    }

    public void insert(View v) {
        input.getText().insert(input.getSelectionEnd(), ((TextView) v).getText());
    }

    public void termBtn(View v) {
        switch (v.getId()) {
            case R.id.termClear:
                clear();
                break;
            case R.id.termBackward:
                backward();
                break;
            case R.id.termForward:
                forward();
                break;
            case R.id.termTrans:
                readInput();
                break;
            case R.id.termZoomIn:
                zoom(true);
                break;
            case R.id.termZoomOut:
                zoom(false);
                break;
            case R.id.termDarkMode:
                isDark = !isDark;
                super.recreate();
                break;
        }

    }

    private void backward() {
        if (history_index == history.size())
            history_index--;
        if (history_index >= 0 && history_index < history.size()) {
            input.setText(history.get(history_index));
            if (history_index > 0) history_index--;
        }
    }

    private void forward() {
        if (history_index == 0)
            history_index++;
        if (history_index <= history.size() - 1 && history_index >= 0) {
            input.setText(history.get(history_index));
            history_index++;
        }
    }

    private void zoom(boolean in) {
        if (in && fontSize < 40) fontSize += 5;
        else if (!in && fontSize > 5) fontSize -= 5;
        cursor.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        input.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        output.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
    }

    public void clear() {
        output.setText("");
        history_index = history.size() - 1;
        input.setText("");
    }


    private void getEditorSettings() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        fontSize = sharedPref.getInt("font_size", 12);
        isDark = sharedPref.getBoolean("dark_mode", false);
        current_directory = sharedPref.getString("pwd", "/");
        settingsDB = openOrCreateDatabase("settings", MODE_PRIVATE, null);
        settingsDB.execSQL("CREATE TABLE IF NOT EXISTS history(value VARCHAR);");

        if (isDark) super.setTheme(R.style.AppThemeDark);
        else super.setTheme(R.style.AppThemeLight);
    }

    private void setEditorSettings() {
        SharedPreferences.Editor sharedPref = getPreferences(Context.MODE_PRIVATE).edit();
        sharedPref.putInt("font_size", fontSize);
        sharedPref.putBoolean("dark_mode", isDark);
        sharedPref.putString("pwd", Interface.pwd);
        sharedPref.commit();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == 66) {
            readInput();
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onDestroy() {
        setEditorSettings();
        super.onDestroy();
    }

    public void readInput() {
        final String code = input.getText().toString();
        if (code.trim().isEmpty()) return;

        if (code.equals("cls") || code.equals("clear")) {
            clear();
            return;
        }
        if (code.equals("quit") || code.equals("exit")) {
            finish();
        }
        input.setText("");
        output.append("\n");
        output.append(Html.fromHtml("<font color=\"#15ab0d\">$ " + code + "</font>"));
        output.append("\n");
        history.add(code);
        try {
            settingsDB.execSQL("INSERT INTO history(value) VALUES('" + code + "')");
        } catch (Exception e) {
            e.printStackTrace();
        }
        history_index = history.size() - 1;
        hideKeyboard(this);
        mInterface.EvalShellCommand(code);
        setEditorSettings();
    }

    private void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        setEditorSettings();
        finish();
        //super.onBackPressed();
    }

}
