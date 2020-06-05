package com.example.note.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class RegisterActivity extends AppCompatActivity {
    EditText et_name,et_pass1,et_pass2,et_tell,et_qq,et_wechat;
    Button btn_r;

    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);
        et_name=findViewById(R.id.et_name);
        et_pass1=findViewById(R.id.et_pass1);
        et_pass2=findViewById(R.id.et_pass2);
        et_tell=findViewById(R.id.et_tell);
        et_qq=findViewById(R.id.et_qq);
        et_wechat=findViewById(R.id.et_wechat);
        btn_r=findViewById(R.id.btn_r);

        requestQueue= Volley.newRequestQueue(this);//初始化请求队列

        btn_r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(et_name.getText().toString(),et_pass1.getText().toString(),et_pass2.getText().toString(),et_tell.getText().toString(),et_qq.getText().toString(),et_wechat.getText().toString());
            }
        });
    }

    public  void register(String name,String pass1,String pass2,String qq,String wechat,final String tell){
        if (tell==null||tell.equals("")){
            Toast.makeText(this,"电话不允许为空",Toast.LENGTH_SHORT).show();
            return;
        }
        if (qq==null||qq.equals("")){
            Toast.makeText(this,"QQ不允许为空",Toast.LENGTH_SHORT).show();
            return;
        }
        if (wechat==null||wechat.equals("")){
            Toast.makeText(this,"微信不允许为空",Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass1==null||pass1.equals("")){
            Toast.makeText(this,"密码不允许为空",Toast.LENGTH_SHORT).show();
            return;
        }
        if (name==null||name.equals("")){
            Toast.makeText(this,"用户名不允许为空",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pass1.equals(pass2)){
            Toast.makeText(this,"两次输入的密码不一致，请重新输入",Toast.LENGTH_SHORT).show();
            return;
        }
        String url= Constants.REGISTER_URL+"?name="+name+"&pass="+pass1+"&tel="+tell+"&qq="+qq+"&wechat="+wechat;//拼接注册信息请求地址

        //创建volley请求
        StringRequest request=new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Gson gson = new Gson();
                Result result = gson.fromJson(s, Result.class);//将服务器返回的结果s解析成Result对象
                if (result.isSuccess) {
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.putExtra("tel", tell);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), result.msg, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(),"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(request);
    }
}
