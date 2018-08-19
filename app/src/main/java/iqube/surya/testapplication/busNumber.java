package iqube.surya.testapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class busNumber extends AppCompatActivity {

    public static String busId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_number);

        Button button = findViewById(R.id.Id);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.busId);
                busId=editText.getText().toString();
                Toast.makeText(busNumber.this,"Entered Bus Number",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(busNumber.this,CameraRecorder.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
