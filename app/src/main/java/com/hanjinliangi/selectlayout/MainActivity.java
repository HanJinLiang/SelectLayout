package com.hanjinliangi.selectlayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    SelectLayout layout_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_layout);

        layout_container= (SelectLayout) findViewById(R.id.layout_container);

        layout_container.setOnSelectChangedListener(new SelectLayout.OnSelectChangedListener() {
            @Override
            public void onSelectChange(SelectLayout.CurrentSelect current) {
                if(current==SelectLayout.CurrentSelect.left){
                    Toast.makeText(getApplicationContext(),"选中--left",Toast.LENGTH_SHORT).show();
                }else if(current==SelectLayout.CurrentSelect.center){
                    Toast.makeText(getApplicationContext(),"选中--center",Toast.LENGTH_SHORT).show();
                }else if(current==SelectLayout.CurrentSelect.right){
                    Toast.makeText(getApplicationContext(),"选中--right",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSelectClick() {
                Toast.makeText(getApplicationContext(),"选中被点击",Toast.LENGTH_SHORT).show();
            }
        });
//        layout_container.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                layout_container.setCurrentSelect(SelectLayout.CurrentSelect.left);
//            }
//        },1000);

    }
}
