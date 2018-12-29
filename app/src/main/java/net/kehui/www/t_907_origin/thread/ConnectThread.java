package net.kehui.www.t_907_origin.thread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import net.kehui.www.t_907_origin.view.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by IF on 2018/12/26
 */
public class ConnectThread extends Thread{

    private final Socket       socket;
    private       Handler      handler;
    private       InputStream  inputStream;
    private       OutputStream outputStream;

    public ConnectThread(Socket socket, Handler handler){
        setName("ConnectThread");
        Log.w("AAA","ConnectThread");
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
/*        if(activeConnect){
//            socket.c
        }*/
        if(socket==null){
            return;
        }
        handler.sendEmptyMessage(1);
        try {
            //获取数据流
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            byte[] buffer = new byte[1024];
            int bytes;
            while (true){
                //读取数据
                bytes = inputStream.read(buffer);
                if (bytes > 0) {
                    final byte[] data = new byte[bytes];
                    System.arraycopy(buffer, 0, data, 0, bytes);

                    Message message = Message.obtain();
                    message.what = 2;
                    Bundle bundle = new Bundle();
                    bundle.putString("MSG",new String(data));
                    message.setData(bundle);
                    handler.sendMessage(message);

                    Log.w("AAA","读取到数据:"+new String(data));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送数据
     */
    public void sendData(String msg){
        Log.w("AAA","发送数据:"+(outputStream==null));
        if(outputStream!=null){
            try {
                outputStream.write(msg.getBytes());
                Log.w("AAA","发送消息："+msg);
                Message message = Message.obtain();
                message.what = MainActivity.SEND_DATA;
                Bundle bundle = new Bundle();
                bundle.putString("MSG",new String(msg));
                message.setData(bundle);
                handler.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = 3;
                Bundle bundle = new Bundle();
                bundle.putString("MSG",new String(msg));
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }
    }
    public void sendCommand(byte[] request){
        if(outputStream!=null){
            try {
                outputStream.write(request);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
