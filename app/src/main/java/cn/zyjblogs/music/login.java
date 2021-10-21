package cn.zyjblogs.music;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class login extends AppCompatActivity implements View.OnClickListener {
    private Button bt_login;
    private EditText et_ID,et_password;
    private String username,password;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_ID = (EditText) findViewById(R.id.et_ID);
        et_password = (EditText) findViewById(R.id.et_password);
        bt_login = (Button) findViewById(R.id.bt_login);
        bt_login.setOnClickListener(this);
        sp = getSharedPreferences("user",MODE_PRIVATE);
        editor = sp.edit();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            /*
            判断账号密码，有一个为空则吐司"账号或密码不能为空！"
            账号密码都为1则登录成功，否则失败
             */
            case R.id.bt_login:
                username = et_ID.getText().toString().trim();
                password = et_password.getText().toString().trim();
                if(et_ID.getText().toString().trim().equals("") ||
                        et_password.getText().toString().trim().equals("")){
                    Toast.makeText(this,"账号或密码不能为空！",Toast.LENGTH_SHORT).show();
                }else{
                    if(username.equals("1") && password.equals("1")){
                        Toast.makeText(this,"登陆成功",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this,Main.class);
                        startActivity(intent);
                        finish();
                    }else {
                        Toast.makeText(this,"账号或密码不正确！",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
}
