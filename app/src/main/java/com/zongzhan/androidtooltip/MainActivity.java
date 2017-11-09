package com.zongzhan.androidtooltip;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zongzhan.tooltip.Tooltip;

public class MainActivity extends AppCompatActivity {

    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);

        Tooltip.Builder builder = new Tooltip.Builder();
        Tooltip tooltip = builder.target(button)
                .position(Tooltip.Position.BELOW)
                .toolTipView(R.layout.hint)
                .closeWhenTouch(false, false)
                .consumeTouchEvent(false, false)
                .align(Tooltip.Align.CENTER)
                .build();

        button.setOnClickListener(v -> {
            if (tooltip.isShowing()) {
                tooltip.dismiss();
            } else {
                tooltip.show();
            }
        });

    }
}
