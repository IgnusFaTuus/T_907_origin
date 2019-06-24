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
public class ConnectThread extends Thread {

    private final Socket socket;
    private Handler handler;
    private InputStream inputStream;
    private OutputStream outputStream;

    public ConnectThread(Socket socket, Handler handler) {
        setName("ConnectThread");
        Log.w("AAA", "ConnectThread");
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
        if (socket == null) {
            return;
        }
        handler.sendEmptyMessage(MainActivity.DEVICE_CONNECTED);
        try {
            //获取数据流
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            byte[] buffer = new byte[402400];
            int bytes;

            while (true) {
                //读取数据
                if (inputStream.available() <= 0) {
                    handler.sendEmptyMessage(MainActivity.DATA_COMPLETED);
                    continue;
                } else {
                    Thread.sleep(200);
                }
                bytes = inputStream.read(buffer);
                if (bytes > 0) {
                    byte[] data = new byte[bytes];
                    System.arraycopy(buffer, 0, data, 0, bytes);
                    //GC20190103 WIFI数据流接收处理
                    int[] WIFIStream = new int[bytes];
                    //将传过来的字节数组转变为int数组
                    for (int i = 0; i < bytes; i++) {
                        WIFIStream[i] = data[i] & 0xff;
                    }
                    Message message = Message.obtain();
                    message.what = MainActivity.GET_STREAM;
                    Bundle bundle = new Bundle();
                    bundle.putIntArray("STM", WIFIStream);
                    message.setData(bundle);
                    handler.sendMessage(message);
                    Log.e("AAA",
                            "读取到数据:" + WIFIStream[0] + "指令：" + WIFIStream[5] + "数据：" + WIFIStream[6]);

                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * GC20190102
     * 命令发送处理
     */
    public void sendCommand(byte[] request) {
        if (outputStream != null) {
            try {
                outputStream.write(request);
                Message message = Message.obtain();
                message.what = MainActivity.SEND_SUCCESS;
                handler.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = MainActivity.SEND_ERROR;
                handler.sendMessage(message);
            }
        }
    }
}
