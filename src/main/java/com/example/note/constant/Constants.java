package com.example.note.constant;

public class Constants {
    public final static String ROOT_RUL="http://192.168.1.104:8080/note_server";//服务器和地址

    public final static String LOGIN_URL=ROOT_RUL+"/LoginServlet";//登录请求地址
    public final static String REGISTER_URL=ROOT_RUL+"/registServlet";//注册请求地址
    public final static String GET_ALL_NOTES=ROOT_RUL+"/GetAllNoteServlet";//获取所有备忘记录请求地址
    public final static String DELETE_NOTE=ROOT_RUL+"/DeleteNoteServlet";//删除备忘记录请求地址
}
