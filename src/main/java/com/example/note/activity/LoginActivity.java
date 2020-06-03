package com.example.note.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.note.MainActivity;
import com.example.note.R;
import com.example.note.constant.Constants;
import com.example.note.result.Result;
import com.google.gson.Gson;

public class LoginActivity extends AppCompatActivity {
    EditText et_tel,et_pass;
    Button btn_login,btn_regist;
    CheckBox rb;

    RequestQueue requestQueue;//volley请求队列
    SharedPreferences sp;//记住用户名密码存储在SharedPreferences中

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        et_pass=findViewById(R.id.et_pass);
        et_tel=findViewById(R.id.et_tel);
        btn_login=findViewById(R.id.btn_login);
        btn_regist=findViewById(R.id.btn_regist);
        rb=findViewById(R.id.rb);

        //初始化volley请求队列
        requestQueue= Volley.newRequestQueue(this);

        sp=getSharedPreferences("name",MODE_PRIVATE);//初始化SharedPreferences
        String tel=sp.getString("tel","");
        String pass=sp.getString("pass","");//将初始化SharedPreferences中存储的电话和密码取出来
        et_tel.setText(tel);
        et_pass.setText(pass);//将之前在SharedPreferences记住的电话和密码展示到输入框中

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(et_tel.getText().toString(),et_pass.getText().toString());
            }
        });
        btn_regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    public void login(final String tel,final String pass){
        String url= Constants.LOGIN_URL+"?tel="+tel+"&pass="+pass;

        //创建一个volley请求
        StringRequest stringRequest=new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Gson gson = new Gson();
                Result rs = gson.fromJson(s,Result.class);
                if (rs.isSuccess) {
                    if (rb.isChecked()) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("tel", tel);
                        editor.putString("pass", pass);
                        editor.commit();
                    }

                    //跳转到主界面，并将用户电话传给主界面
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("tel", tel);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "用户名或密码错误", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(),"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(stringRequest);
    }
}
