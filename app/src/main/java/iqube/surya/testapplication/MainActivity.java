package iqube.surya.testapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.bugfender.sdk.Bugfender;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button login = findViewById(R.id.button);
        login.setGravity(Gravity.CENTER);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ed1= findViewById(R.id.editText);
                ed1.setGravity(Gravity.CENTER);
                EditText ed2 = findViewById(R.id.editText2);
                ed2.setGravity(Gravity.CENTER);
                if(ed1.getText().toString().equals("admin") && ed2.getText().toString().equals("admin")){
                    Toast.makeText(MainActivity.this,"Credentials accepted", LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, busNumber.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(MainActivity.this,"Invalid Credentials",LENGTH_LONG).show();
                }
                finish();
            }
        });
    }
}




