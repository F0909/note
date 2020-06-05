package com.example.note.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Service;
import android.app.TimePickerDialog;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.note.R;
import com.example.note.bean.NoteBean;
import com.example.note.constant.Constants;
import com.example.note.result.Result;
import com.google.gson.Gson;

import java.util.Date;

public class NoteActivity extends AppCompatActivity {
    public int type;//1代表使用该界面新增备忘，2代表使用该界面查看备忘详情
    RequestQueue requestQueue;
    String time;//提醒时间
    String date;//提醒日期
    AlarmManager alarmManager;

    EditText et_title,et_content;
    Button btn_date,btn_time,btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        type=getIntent().getIntExtra("type",1);
        requestQueue= Volley.newRequestQueue(this);

        et_title=findViewById(R.id.et_title);
        et_content=findViewById(R.id.et_content);
        btn_date=findViewById(R.id.btn_date);
        btn_time=findViewById(R.id.btn_time);
        btn=findViewById(R.id.btn);

        //点击日期按钮，弹出日期选择器对话框
        btn_date.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog=new DatePickerDialog(NoteActivity.this);//创建日期选择器
                datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        date=year+"年"+month+"月"+dayOfMonth+"日";
                        btn_date.setText(date);//将用户选择的日期显示在按钮上
                    }
                });
                datePickerDialog.show();//展示该日期选择对话框
            }
        });

        //点击时间按钮，弹出时间选择器对话框
        btn_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog=new TimePickerDialog(NoteActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        time=hourOfDay+":"+minute;
                        btn_time.setText(time);
                    }
                },12,0,true);
                timePickerDialog.show();
            }
        });

        if (type==1){
            btn.setText("保存");
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    add();
                }
            });
        }else if (type==2){
            btn.setText("修改");
            getNoteByID();
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    modify();
                }
            });
        }
    }
    //如果是新增备忘录，底部按钮就是保存按钮
    public void add(){
        String tel=getIntent().getStringExtra("tel");
        String title=et_title.getText().toString();
        String content=et_content.getText().toString();
        final String noteTime;

        if (content.equals("")&&title.equals("")){
            Toast.makeText(getApplicationContext(),"标题和内容不能同时为空",Toast.LENGTH_SHORT).show();
            return;
        }
        if (date==null||time==null){
            noteTime="";
        }else {
            noteTime=date+" "+time;
        }

        String url= Constants.ADD_NOTE_URL+"?tel="+tel+"&title="+title+"&content="+content+"&noteTime="+noteTime;
        StringRequest addRequest=new StringRequest(url, new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(String s) {
                Gson gson = new Gson();
                Result result = gson.fromJson(s, Result.class);
                if (result.isSuccess) {
                    alarmManager = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH：mm");
                        Date date=sdf.parse(noteTime);
                        long timeInLong=date.getTime();
                        alarmManager.set(AlarmManager.RTC_WAKEUP,timeInLong,null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        requestQueue.add(addRequest);
    }

    //如果该界面用于查看note详情，获取note详情并展示
    public void getNoteByID(){
        int id=getIntent().getIntExtra("id",0);
        String url=Constants.GET_NOTE_BY_ID+"?id="+id;
        StringRequest stringRequest=new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Gson gson = new Gson();
                NoteBean note = gson.fromJson(s, NoteBean.class);
                et_content.setText(note.getContent());
                et_title.setText(note.getTitle());
                if (note.getNoteTime() != null && !note.getNoteTime().equals("")) {
                    date = note.getNoteTime().split(" ")[0];
                    time = note.getNoteTime().split(" ")[1];
                    btn_date.setText(date);
                    btn_time.setText(time);
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

    //如果是查看note详情，底部按钮是修改按钮，点击修改执行modify
    public void modify(){
        int id=getIntent().getIntExtra("id",0);
        String title=et_title.getText().toString();
        String content=et_content.getText().toString();
        final String noteTime;

        if (content.equals("")&&title.equals("")){
            Toast.makeText(getApplicationContext(),"标题和内容不能同时为空",Toast.LENGTH_SHORT).show();
            return;
        }
        if (date==null||time==null){
            noteTime="";
        }else {
            noteTime=date+" "+time;
        }

        String url=Constants.MODIFY_NOTE_URL+"?id="+id+"&title="+title+"&content="+content+"&noteTime="+noteTime;
        StringRequest addRequest=new StringRequest(url, new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(String s) {
                Gson gson = new Gson();
                Result result = gson.fromJson(s, Result.class);
                if (result.isSuccess) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH：mm");
                        Date date = sdf.parse(noteTime);
                        long timeInLong = date.getTime();
                        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInLong, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        requestQueue.add(addRequest);
    }
}

