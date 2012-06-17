package com.example.Raavan.UI;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.Raavan.R;

public class RaavanUI extends Activity {
	
	Button button;
	
	protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.raavanui);

        button = (Button) findViewById(R.id.button_run);
    }
	
	public void run_function(View view){
		button.setText("Sup");
		// TODO run stuff here
	}

}
