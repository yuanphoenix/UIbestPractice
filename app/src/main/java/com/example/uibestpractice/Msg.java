package com.example.uibestpractice;

public class Msg {
    //普通的记录消息的类
    public static final int TYPE_RECEIVED=0;
    public static final int TYPE_SEND=1;
    private String content;
    private int Type;
    public Msg(String content,int Type)
    {
        this.content=content;
        this.Type=Type;
    }
    public String getContent()
    {
        return content;
    }
    public int getType()
    {
        return Type;
    }

}
