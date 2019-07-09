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

import static net.kehui.www.t_907_origin.base.BaseActivity.COMMAND_MODE;
import static net.kehui.www.t_907_origin.base.BaseActivity.COMMAND_RANGE;
import static net.kehui.www.t_907_origin.base.BaseActivity.COMMAND_RECEIVE_DATA;
import static net.kehui.www.t_907_origin.base.BaseActivity.COMMAND_RECEIVE_RIGHT;
import static net.kehui.www.t_907_origin.base.BaseActivity.TDR;
import static net.kehui.www.t_907_origin.base.BaseActivity.ICM;
import static net.kehui.www.t_907_origin.base.BaseActivity.DECAY;
import static net.kehui.www.t_907_origin.base.BaseActivity.SIM;
import static net.kehui.www.t_907_origin.base.BaseActivity.RANGE_16_KM;
import static net.kehui.www.t_907_origin.base.BaseActivity.RANGE_1_KM;
import static net.kehui.www.t_907_origin.base.BaseActivity.RANGE_2_KM;
import static net.kehui.www.t_907_origin.base.BaseActivity.RANGE_32_KM;
import static net.kehui.www.t_907_origin.base.BaseActivity.RANGE_4_KM;
import static net.kehui.www.t_907_origin.base.BaseActivity.RANGE_500;
import static net.kehui.www.t_907_origin.base.BaseActivity.RANGE_64_KM;
import static net.kehui.www.t_907_origin.base.BaseActivity.RANGE_8_KM;
import static net.kehui.www.t_907_origin.base.BaseActivity.READ_ICM_DECAY;
import static net.kehui.www.t_907_origin.base.BaseActivity.READ_TDR_SIM;

/**
 * @author IF
 * @date 2018/12/26
 */
public class ConnectThread extends Thread {

    private final Socket    socket;
    private Handler handler;
    private OutputStream outputStream;

    private int mode   = TDR;
    private int range  = 0;
    private int wifiStreamLen   = 549;
    private boolean isCommand   = true;

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
            InputStream inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            int bytes;
            byte[] buffer = new byte[1024*20];

            while (true) {
                if (isCommand) {
                    //每次收取的wifi数据流长度
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        byte[] data = new byte[bytes];
                        System.arraycopy(buffer, 0, data, 0, bytes);
                        int[] wifiStream = new int[bytes];
                        //将传过来的字节数组转变为int数组
                        for (int i = 0; i < bytes; i++) {
                            wifiStream[i] = data[i] & 0xff;
                        }
                        //收到返回的“收取数据”命令后，准备接受波形数据
                        if ( (wifiStream[5] == COMMAND_RECEIVE_DATA) && (wifiStream[6] == COMMAND_RECEIVE_RIGHT) ) {
                            isCommand = false;
                        }
                        Message message = Message.obtain();
                        message.what = MainActivity.GET_COMMAND;
                        Bundle bundle = new Bundle();
                        bundle.putIntArray("CMD", wifiStream);
                        message.setData(bundle);
                        handler.sendMessage(message);
                        Log.e("CMD", " 指令：" + wifiStream[5] + " 传输数据：" + wifiStream[6]);
                    }
                }else {
                    //读取长度计数
                    int readCount   = 0;
                    byte[] data = new byte[wifiStreamLen];
                    //GC20190706 数据处理优化
                    do {
                        //每次收取的wifi数据流长度
                        bytes = inputStream.read(buffer);
                        //先缓存wifi数据流至data（避免数据过多时缓存不够大造成SIM64Km收数不全）
                        if (bytes > 0) {
//                            Log.e("WAVE", " 实际收取:" + bytes);
                            System.arraycopy(buffer, 0, data, readCount, bytes);
                            readCount += bytes;
                        }
                    } while (readCount != wifiStreamLen);

                    //需要传递的int型波形数据
                    int[] wifiStream = new int[wifiStreamLen];
                    //将传过来的字节数组转变为int数组
                    for (int i = 0; i < wifiStreamLen; i++) {
                        wifiStream[i] = data[i] & 0xff;
                    }

                    Message message = Message.obtain();
                    message.what = MainActivity.GET_WAVE;
                    Bundle bundle = new Bundle();
                    bundle.putIntArray("WAVE", wifiStream);
                    message.setData(bundle);
                    handler.sendMessage(message);
                    //波形数据收取完成，准备收取command
                    isCommand = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 命令发送处理   //GC20190102
     */
    public void sendCommand(byte[] request) {
        if (outputStream != null) {
            try {
                outputStream.write(request);
                //读取方式范围
                if (request[5] == COMMAND_MODE) {
                    switch (request[6]) {
                        case (byte) TDR :
                            mode = TDR;
                            break;
                        case (byte) ICM:
                            mode = ICM;
                            break;
                        case (byte) SIM :
                            mode = SIM;
                            break;
                        case (byte) DECAY :
                            mode = DECAY;
                            break;
                        default:
                            break;
                    }
                    //接收数据个数选择
                    selectWifiStreamLength();
                } else if (request[5] == COMMAND_RANGE){
                    switch (request[6]) {
                        case (byte) RANGE_500 :
                            range = 0;
                            break;
                        case (byte) RANGE_1_KM:
                            range = 1;
                            break;
                        case (byte) RANGE_2_KM :
                            range = 2;
                            break;
                        case (byte) RANGE_4_KM :
                            range = 3;
                            break;
                        case (byte) RANGE_8_KM :
                            range = 4;
                            break;
                        case (byte) RANGE_16_KM:
                            range = 5;
                            break;
                        case (byte) RANGE_32_KM :
                            range = 6;
                            break;
                        case (byte) RANGE_64_KM :
                            range = 7;
                            break;
                        default:
                            break;
                    }
                    //接收数据个数选择
                    selectWifiStreamLength();
                }
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

    /**
     * 根据方式、范围选取判断收取波形数据的点数
     */
    private void selectWifiStreamLength() {
        if (mode == TDR) {
            wifiStreamLen = READ_TDR_SIM[range] + 9;
        } else if ((mode == ICM) || (mode == DECAY)) {
            wifiStreamLen = READ_ICM_DECAY[range] + 9;
        } else if (mode == SIM) {
            wifiStreamLen = ( READ_TDR_SIM[range] + 9 ) * 9;
        }
        Log.e("WAVE", " 需要绘制:" + wifiStreamLen);
    }

}
