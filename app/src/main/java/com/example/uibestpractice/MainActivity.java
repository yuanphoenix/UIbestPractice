package com.example.uibestpractice;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {
    private final  String TAG="MainActivity";
    private List<Msg> msgList=new ArrayList<>();
    private EditText editText;
    private BluetoothSPP bt;
    private Button button;
    private RecyclerView recyclerView;
    private  MsgAdapter adapter;
    private boolean Send=true;
    private ProgressBar mpogressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mpogressBar=(ProgressBar) findViewById(R.id.progress_circular) ;
        mpogressBar.setVisibility(View.GONE);
        bt=new BluetoothSPP(this);
        editText=(EditText) findViewById(R.id.input_text);
        button=(Button) findViewById(R.id.send);
        recyclerView=(RecyclerView) findViewById(R.id.msg_recycle_view);
        adapter=new MsgAdapter(msgList);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content=editText.getText().toString();
                if (!"".equals(content))
                {
                    Msg msg;
                    bt.send(content,true);
                    msg=new Msg(content,Msg.TYPE_SEND);
                    msgList.add(msg);

                    adapter.notifyItemInserted(msgList.size()-1);
                    recyclerView.scrollToPosition(msgList.size()-1);
                    editText.setText("");
                }
            }
        });

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                Msg msg=new Msg(message,Msg.TYPE_RECEIVED);
                msgList.add(msg);
                adapter.notifyItemInserted(msgList.size()-1);
                recyclerView.scrollToPosition(msgList.size()-1);

            }
        });
        //监听连接状态
        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
                mpogressBar.setVisibility(View.GONE);

            }

            public void onDeviceDisconnected() {
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });


        //定位权限
        if(Build.VERSION.SDK_INT>=23){
            //判断是否有权限
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
//向用户解释，为什么要申请该权限
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {
                    Toast.makeText(MainActivity.this,"shouldShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bluetooth,menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.open_blue:
                Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                break;
            case R.id.service:
                mpogressBar.setVisibility(View.VISIBLE);
                break;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!bt.isBluetoothEnabled()) {
            // Do somthing if bluetooth is disable
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            // Do something if bluetooth is already enable
            if (!bt.isServiceAvailable())
                setup();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bt.startService(BluetoothState.DEVICE_ANDROID);
    }

    private void setup() {
        bt.setupService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode)
        {
            case BluetoothState.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setup();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "拒绝授权", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case BluetoothState.REQUEST_CONNECT_DEVICE:
                if(resultCode == Activity.RESULT_OK)
                {
                    bt.connect(data);
                }
                break;
        }
    }
}
