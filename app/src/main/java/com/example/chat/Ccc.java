package com.example.chat;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

public class Ccc {
// 객체 선언
    Socket socket;  // Create socket communication Field
    AppCompatActivity ma;//activity 바로 상위 activity ex) Objective  ,다형성(부모 클래스는 자식 클래스들을 담을수 있음)

    final TextView textView;
    static private String serverIP; // enum class final 변수
    private ConnectionEnum ce = ConnectionEnum.ServerIP; //  생성자

    public static void setMyName(String myName) {
        Ccc.myName = myName;
    }
    private static String myName = "defaultName";

    public Ccc(AppCompatActivity chatActivity) {
        // chatting과 img 첨부 두개를 하기에 부모 클래스에 넣는다.
        ma = chatActivity;
        textView = ma.findViewById(R.id.textView11);
        // 레이아웃으로만 설정한 뷰들은 초기 설정만 정의를 해놔서 이벤트를 받거나 할 수 없기에 findViewById로 불ㄹ러온다.
        serverIP = ce.getIp();
    }

    void start() {
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
                // socket이 start메소드 하기전에 있거나 이미 연결되어 있는 상태라면 닫는다.
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Runnable rr = () -> { // Runnable runnable = new Runnable() // 람다식 : 식별자 없이 실행가능한 함수
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(serverIP, 9000)); //ip주소와 포트번호로부터 소켓 주소를 작성
                // 0~65535 범위의 포트번호
                makeToast("서버 - " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " 접속");
            } catch (IOException e) {
                e.printStackTrace();
            }
            recieve();
        };
        Thread t = new Thread(rr);
        t.start();
    }

    private void recieve() {
        boolean isend = false;
        while (!isend) {

            try {
                InputStream bufReader = (socket.getInputStream());

                byte[] bb = new byte[1024];
                int readByteSize = bufReader.read(bb);

                String message;
                message = new String(bb, 0, readByteSize, "UTF-8");
                Handler handler = new Handler(Looper.getMainLooper());
                //핸들러를 생성하는 스레드만이 다른 스레드가 전송하는 message와 runnable객체를 받을 수 있음.
                //Runnable 메시지는 run()메서드를 호출해 처리
                //Message는 handleMessage()메서드를 이용해 처리
                //1. Message는 다른 스레드에 속한 Message Queue에서 전달됨
                //2. Message Queue에 메세지 넣을 때 : Handler의 sendMessage()를 이용
                //3. Looper는 MessageQueue에서 Loop()을 통해 반복적으로 처리할 메시지를 Handler에 전달
                //4. Handler는 handleMessage를 통해 메시지를 처리
                // UI를 작업하는 MainThread에서 다른 작업을 추가로 하게 된다면 UI처리가 늦게 이뤄지게 된다.
                // handler를 통해서 SubThread 작업을 한다.

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 사용하고자 하는 코드
                        textView.append("\n" + message);
                        // append 뒤에 붙이는 함수
                        final int scrollAmount = textView.getLayout().getLineTop(textView.getLineCount()) - textView.getHeight();
                        // if there is no need to scroll, scrollAmount will be <=0
                        if (scrollAmount > 0)
                            textView.scrollTo(0, scrollAmount);
                        else
                            textView.scrollTo(0, 0);
                    }
                }, 0);
                // delay를 주고 어떤 동작을 하고 싶다면 Handler클래스의 postDelayed 메소드 이용
            } catch (Exception e) {
                e.printStackTrace();
                textView.setText(textView.getText() + "\n서버끊어짐");
                try {
                    socket.close();
                    isend = true;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void send(String msg) {
        Runnable rr = () -> {
            try {
                if (socket == null) {
                    makeToast("접속 먼저 하세요");
                }
                // 서버 접속
                // Server에 보낼 데이터
                OutputStream bufWriter = (socket.getOutputStream());
                bufWriter.write((myName + " : " + msg).getBytes("UTF-8"));
                bufWriter.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        Thread t = new Thread(rr);
        t.start();
    }

    public void sendPhoto(File img) {
        Runnable rr;
        rr = () -> {
            try {
                Socket pSocket = new Socket();

                pSocket.connect(new InetSocketAddress(serverIP, 7777));
                if (pSocket == null) {
                    makeToast("접속 먼저 하세요");
                }
                // 서버 접속
                // Server에 보낼 데이터
                File file = img;
                if (!file.exists()) {
                    System.out.println("File not Exist.");
                    System.exit(0);
                }
                BufferedOutputStream toServer = new BufferedOutputStream(pSocket.getOutputStream());
                DataOutputStream dos = new DataOutputStream(pSocket.getOutputStream());
                dos.writeUTF(new String("up".getBytes(), "UTF-8"));
                dos.writeUTF(new String(myName.getBytes(), "UTF-8"));
                dos.writeUTF(new String(file.getName().getBytes(), "UTF-8"));
                dos.writeUTF("" + file.length());

                OutputStream outputStream = pSocket.getOutputStream();

                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fileInputStream);

                byte[] dataBuff = new byte[(int) file.length()];
                int length = fileInputStream.read(dataBuff);
                while (length != -1) {
                    outputStream.write(dataBuff, 0, length);
                    length = fileInputStream.read(dataBuff);
                }
                System.out.println("전송 성공");

                byte[] buf = new byte[4096]; //buf 생성합니다.
                int theByte = 0;
                while ((theByte = bis.read(buf)) != -1) // BufferedInputStream으로
                {
                    toServer.write(buf, 0, theByte);
                }

                toServer.flush();
                toServer.close();
                bis.close();
                fileInputStream.close();
                pSocket.close();
            } catch (Exception e) {

                e.printStackTrace();

            }
        };
        Thread t = new Thread(rr);
        t.start();
    }

    public void recievePhoto(String fileName) {
        Runnable rr;
        rr = () -> {
            try {
                Socket pSocket = new Socket();

                pSocket.connect(new InetSocketAddress(serverIP, 7777));
                if (pSocket == null) {
                    makeToast("접속 먼저 하세요");
                }
                // 서버 접속

                // Server에 보낼 데이터
                final String LOCAL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
                String folder = LOCAL_PATH + "/downloads";
                BufferedOutputStream toServer = new BufferedOutputStream(pSocket.getOutputStream());
                DataOutputStream dos = new DataOutputStream(pSocket.getOutputStream());
                dos.writeUTF(new String("down".getBytes(), "UTF-8"));
                dos.writeUTF(new String(fileName.getBytes(), "UTF-8"));


                BufferedInputStream up = new BufferedInputStream(pSocket.getInputStream());
                DataInputStream fromClient = new DataInputStream(up);
                String filename = fromClient.readUTF();
                int filesize = Integer.parseInt(fromClient.readUTF());

                System.out.println(filename + "\t을 받습니다.");

                // client단에서 전송되는 file 내용을 server단에 생성시킨 file에 write할수 있는 stream
                File newfile = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS + "/testDown");
                if (!newfile.exists()) {
                    newfile.mkdir();
                }
                System.out.println(newfile.getCanonicalPath() + "/" + filename);
                FileOutputStream toFile = new FileOutputStream(newfile.getCanonicalPath() + "/" + filename);
                BufferedOutputStream outFile = new BufferedOutputStream(toFile);
                System.out.println((filename + " " + filesize));
                byte[] bb = new byte[filesize];
                int ch = 0;
                while ((ch = up.read()) != -1) {
                    outFile.write(ch);
                }

                makeToast(filename + "을(를) 받았습니다");
                outFile.flush();
                outFile.close();
                pSocket.close();
            } catch (Exception e) {

                e.printStackTrace();

            }
        };
        Thread t = new Thread(rr);
        t.start();
    }

    private void makeToast(String tm) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ma, tm, Toast.LENGTH_SHORT).show();
                ;
            }
        }, 0);
    }


    public static class requestImgList extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg = "사진 없음";

        @Override
        protected String doInBackground(String... strings) {

            try {
                URL url = new URL("http://" + serverIP + ":8080/ChatTest/ImageList.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoOutput(false);
                String str;
                String[] imgList;
                if (conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    // jsp에서 보낸 값을 받는 부분
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    if (buffer.length() != 0) {
                        receiveMsg = buffer.toString();
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //jsp로부터 받은 리턴 값
            return receiveMsg;
        }
    }
//http://localhost:8080/Serrrverrr/ImageList.jsp
}

