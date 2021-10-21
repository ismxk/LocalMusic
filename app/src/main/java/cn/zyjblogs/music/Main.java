package cn.zyjblogs.music;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

public class Main extends AppCompatActivity implements View.OnClickListener {

    private FragmentManager manager;
    private FragmentTransaction transaction;
    private RadioButton rb_bo,rb_list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main2);

        manager=getFragmentManager();
        transaction=manager.beginTransaction();
        transaction.add(R.id.title_layout,new bo_title());
        transaction.add(R.id.content_layout,new bo_content());
        transaction.commit();
        initView();
    }

    private void initView(){
        rb_bo= (RadioButton) findViewById(R.id.rb_bo);
        rb_list= (RadioButton) findViewById(R.id.rb_list);

        rb_bo.setTextColor(Color.GREEN);
        rb_bo.setChecked(true);

        rb_bo.setOnClickListener(this);
        rb_list.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        transaction=manager.beginTransaction();
        switch (view.getId()){
            case R.id.rb_bo:
                rb_bo.setTextColor(Color.GREEN);
                rb_list.setTextColor(Color.BLACK);
                transaction.replace(R.id.title_layout,new bo_title());
                transaction.replace(R.id.content_layout,new bo_content());
                break;
            case R.id.rb_list:
                rb_list.setTextColor(Color.GREEN);
                rb_bo.setTextColor(Color.BLACK);
                transaction.replace(R.id.title_layout,new list_title());
                transaction.replace(R.id.content_layout,new list_content());
                break;
        }
        transaction.commit();
    }


}

