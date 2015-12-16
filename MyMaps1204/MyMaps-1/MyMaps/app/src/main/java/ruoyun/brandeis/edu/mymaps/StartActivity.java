package ruoyun.brandeis.edu.mymaps;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import ruoyun.brandeis.edu.mymaps.FButton;

import com.google.android.gms.maps.model.LatLng;

import static ruoyun.brandeis.edu.mymaps.R.style.QText;

public class StartActivity extends AppCompatActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener{

    //added by, used for quit app
    public static boolean terminatorFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        android.support.v7.widget.Toolbar myToolbar =
                (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setLogo(R.mipmap.ic_pink_car);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //myToolbar.setTitleTextAppearance(this, QText);
        Typeface face= Typeface.createFromAsset(getAssets(), "emmasophia.ttf");
        TextView mTitle = (TextView) myToolbar.findViewById(R.id.toolbar_title);
        mTitle.setTypeface(face);


        //modified by
        FButton btn5 = (FButton) findViewById(R.id.button5);

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent my_intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(my_intent);
            }
        });


        FButton btn4 = (FButton) findViewById(R.id.button4);

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent my_intent = new Intent(StartActivity.this, Main2Activity.class);
                startActivity(my_intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //added by
    @Override
     public void onRestart() {
        if(true == terminatorFlag) {
            this.finish();
        }

        super.onRestart();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    @Override
    public void onClick(View v) {

    }
}
