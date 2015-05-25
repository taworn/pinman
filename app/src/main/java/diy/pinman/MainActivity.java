package diy.pinman;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    private PinEdit pinEdit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.show();
        }

        pinEdit = (PinEdit) findViewById(R.id.edit_pin);
        //pinEdit.setCharSize(128, 128);
        //pinEdit.setCharDrawable(getResources().getDrawable(R.drawable.you));
        //pinEdit.setTypedCharDrawable(getResources().getDrawable(R.drawable.i));
        //pinEdit.setBackgroundColor(0xCCCCCCCC);
        //pinEdit.setSpaceBetweenChars(64);
        //pinEdit.setHideChars(true);
        //pinEdit.setGravityFix(Gravity.END | Gravity.BOTTOM);
        pinEdit.setOnCompleteListener(new PinEdit.OnCompleteListener() {
            @Override
            public void onComplete(String text) {
                if (text.equals("5555")) {
                    Toast.makeText(getApplicationContext(), String.format("%s ^_^", text), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), String.format("%s T_T", text), Toast.LENGTH_SHORT).show();
                    pinEdit.clear();
                }
            }
        });

        EditText editPinNow = (EditText) findViewById(R.id.edit_pin_now);
        editPinNow.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String temp = s.toString();
                pinEdit.setText(temp);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
