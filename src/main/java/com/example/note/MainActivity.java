package com.example.note;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.note.activity.LoginActivity;
import com.example.note.activity.NoteActivity;
import com.example.note.bean.NoteBean;
import com.example.note.constant.Constants;
import com.example.note.result.Result;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView lv;
    Button btn;
    RequestQueue requestQueue;
    String tel;
    ArrayList<NoteBean> datas;//用于存放所有的备忘记录的集合
    MyAdapter myAdapter;//自定义的ListView适配器
    private boolean isExit;
    private Handler handler;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv=findViewById(R.id.lv);
        btn=findViewById(R.id.btn_add);

        actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);//添加一个返回箭头


        handler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                isExit=false;
            }
        };

        //新增按钮事件监听器
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, NoteActivity.class);
                intent.putExtra("type",1);//1代表是新增备忘
                intent.putExtra("tel",tel);//新增备忘时需要用户tel
                startActivity(intent);
            }
        });

        //ListView事件监听器
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(MainActivity.this,NoteActivity.class);
                intent.putExtra("type",2);//2代表查看备忘详情
                intent.putExtra("id",datas.get(position).getId());//查看备忘时需要备忘id
                startActivity(intent);
            }
        });

        requestQueue= Volley.newRequestQueue(this);//初始化请求队列
        tel=getIntent().getStringExtra("tel");//获取上一个页面传递过来的tel

        //长按列表项弹出对话框选择是否删除该条记录，向服务器发起删除请求
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {
                //弹出是否删除文本框
                    AlertDialog.Builder alertBuilder=new AlertDialog.Builder(MainActivity.this);
                    alertBuilder.setTitle("提示").setMessage("确认删除该备忘记录？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String url= Constants.DELETE_NOTE+"?id="+datas.get(position).getId();//拼接删除操作的请求地址
                            StringRequest stringRequest=new StringRequest(url, new Response.Listener<String>() {  //创建请求
                                @Override
                                public void onResponse(String s) {
                                    Gson gson = new Gson();
                                    Result result = gson.fromJson(s, Result.class);
                                    if (result.isSuccess) {
                                        datas.remove(position);
                                        myAdapter = new MyAdapter();
                                        lv.setAdapter(myAdapter);//刷新界面
                                        Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), result.msg, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    Toast.makeText(getApplicationContext(), "网络错误，删除失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                            requestQueue.add(stringRequest);//发起请求
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //继续在当前页面
                            dialog.cancel();
                        }
                    }).create();
                    alertBuilder.show();

                return true;
            }
        });
    }

    //服务器数据获取放在onStart中，这样页面每次处于可见状态都会重新回去刷新数据
    @Override
    protected void onStart() {
        super.onStart();
        String url=Constants.GET_ALL_NOTES+"?tel="+tel;//获取所有备忘记录请求地址需要传参tel
        StringRequest stringRequest=new StringRequest(url,new Response.Listener<String>(){

            @Override
            public void onResponse(String s) {
                Gson gson=new Gson();
                datas=gson.fromJson(s,new TypeToken<ArrayList<NoteBean>>(){}.getType());//将服务器获取到的数据转换成集合
                myAdapter=new MyAdapter();
                lv.setAdapter(myAdapter);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(stringRequest);
    }

    public class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //将列表项的布局文件转换成View对象
            convertView= LayoutInflater.from(MainActivity.this).inflate(R.layout.item,null);
            TextView tv_title=convertView.findViewById(R.id.tv_title);
            TextView tv_content=convertView.findViewById(R.id.tv_content);

            tv_title.setText(datas.get(position).getTitle());
            tv_content.setText(datas.get(position).getContent());

            return convertView;
        }
    }

    //创建选项菜单的回调
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1,1,1,"切换账号");
        return true;
    }

    //选项菜单的回调
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case 1:
                Intent intent=new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                goBack();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goBack(){
        if (!isExit){
            isExit=true;
            handler.sendEmptyMessageDelayed(0,2000);
            Toast.makeText(this,"再按一次退出",Toast.LENGTH_SHORT).show();
        }else {
            finish();
            System.exit(0);
        }
    }

    public boolean onKeyDown(int keyCode,KeyEvent event){
        if (keyCode==KeyEvent.KEYCODE_BACK){
            if (!isExit){
                isExit=true;
                handler.sendEmptyMessageDelayed(0,2000);
                Toast.makeText(this,"再按一次退出",Toast.LENGTH_SHORT).show();
                return false;
            }else {
                finish();
                System.exit(0);
            }
        }
        return super.onKeyDown(keyCode,event);
    }

}

