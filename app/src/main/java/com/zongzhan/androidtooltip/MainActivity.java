package com.zongzhan.androidtooltip;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zongzhan.tooltip.Tooltip;

public class MainActivity extends AppCompatActivity {

    Tooltip tooltip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button top = findViewById(R.id.top);
        Button down = findViewById(R.id.down);
        Button left = findViewById(R.id.left);
        Button right = findViewById(R.id.right);

        View targetView = findViewById(R.id.target_view);

        top.setOnClickListener(view -> {
            Tooltip.Builder builder = new Tooltip.Builder();
            tooltip = builder.target(targetView)
                    .position(Tooltip.Position.ABOVE)
                    .toolTipView(R.layout.hint)
                    .build();
            tooltip.show();
        });

        down.setOnClickListener(view -> {
            Tooltip.Builder builder = new Tooltip.Builder();
            tooltip = builder.target(targetView)
                    .position(Tooltip.Position.BELOW)
                    .toolTipView(R.layout.hint)
                    .build();
            tooltip.show();
        });

        left.setOnClickListener(view -> {
            Tooltip.Builder builder = new Tooltip.Builder();
            tooltip = builder.target(targetView)
                    .position(Tooltip.Position.LEFT)
                    .toolTipView(R.layout.hint)
                    .build();
            tooltip.show();
        });

        right.setOnClickListener(view -> {
            Tooltip.Builder builder = new Tooltip.Builder();
            tooltip = builder.target(targetView)
                    .position(Tooltip.Position.RIGHT)
                    .toolTipView(R.layout.hint)
                    .build();
            tooltip.show();
        });

    }
}
