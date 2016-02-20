package pro.com.chatapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;

/**
 * Created by pR0 on 17-02-2016.
 */
public class RegActivity extends AppCompatActivity {

    EditText username;
    CardView regBtn;
    TextView welcome, enterText, enter_name;
    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        i = new Intent(this, ChatActivity.class);

        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/segoeui.ttf");
        Typeface myTypeface2 = Typeface.createFromAsset(getAssets(), "fonts/simple.ttf");

        username = (EditText) findViewById(R.id.name_text);
        regBtn = (CardView) findViewById(R.id.enter);
        welcome = (TextView) findViewById(R.id.welcome);
        enterText = (TextView) findViewById(R.id.enter_text);
        enter_name = (TextView) findViewById(R.id.no_name_err);

        username.setTypeface(myTypeface);
        enterText.setTypeface(myTypeface);
        welcome.setTypeface(myTypeface2);

        regBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == ACTION_DOWN) {
                    regBtn.setCardElevation(10);
                    return true;
                } else if (event.getAction() == ACTION_UP) {
                    regBtn.setCardElevation(6);
                    if (username.getText().toString().equals("")) {
                        enter_name.setText("Must choose a name to chat.");
                        enter_name.setVisibility(View.VISIBLE);
                    } else if (isNetworkAvailable() == false){
                        enter_name.setText("Please check your internet connection!!");
                        enter_name.setVisibility((View.VISIBLE));
                    } else {
                        i.putExtra("name", username.getText().toString());
                        startActivity(i);
                    }
                    return true;
                } else if (event.getAction() == ACTION_CANCEL) {
                    regBtn.setCardElevation(6);
                }
                return false;
            }
        });

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enter_name.setVisibility(View.GONE);
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
