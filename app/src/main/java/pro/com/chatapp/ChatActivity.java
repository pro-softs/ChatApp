package pro.com.chatapp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    private Socket socket;
    {
        try {
            socket = IO.socket("http://172.28.0.1:8080");
            Log.e("Got it","htv");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private ViewGroup mLinearLayout;

    EditText messageBox;
    ImageView sendBtn, attach, user;
    ScrollView msgCon;
    TextView nameUser;

    Intent i;

    String imgDecodableString, name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        socket.connect();
        mLinearLayout = (ViewGroup) findViewById(R.id.messages);

        messageBox = (EditText) findViewById(R.id.msg_box);
        sendBtn = (ImageView) findViewById(R.id.send_btn);
        msgCon = (ScrollView) findViewById(R.id.messages_con);
        nameUser = (TextView) findViewById(R.id.appName);
        attach = (ImageView) findViewById(R.id.attach);
        user = (ImageView) findViewById(R.id.profile);

         i = getIntent();

        name = i.getExtras().getString("name");
        nameUser.setText(name);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageBox.getText().toString().trim().equals("")) {
                    //nothing
                } else {
                    sendMessage();
                }
            }
        });

        user.setOnClickListener(new View.OnClickListener() {
            @Override
        public void onClick(View v) {
                i = getIntent().setClass(ChatActivity.this, RegActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        });

        attach.setOnClickListener(new View.OnClickListener() {
            @Override
        public void onClick(View v) {
                openGallery();
            }
        });
        socket.on("message", handleIncomingMessages);

    }

    private void sendMessage() {
        String message = messageBox.getText().toString().trim();
        messageBox.setText("");
        addMessage(message, 0, "");
        JSONObject sendText = new JSONObject();
        try {
            sendText.put("text", message);
            sendText.put("name", name);
            sendText.put("image", "");

            socket.emit("message", sendText);
            Log.e("Pro", "Sent");
        }catch(JSONException e){

        }

    }

    public void sendImage(String path){
        JSONObject sendData = new JSONObject();
        try {
            sendData.put("image", encodeImage(path));
            sendData.put("name", name);
            sendData.put("text", "");

            Bitmap bmp = decodeImage(sendData.getString("image"));
            addImage(bmp, 0, "");
            socket.emit("message", sendData);
        } catch (JSONException e){

        }
    }

    private void addMessage(String message, int who, String sender) {
        if(who == 0) {
            View linearLay = LayoutInflater.from(this).inflate(R.layout.self_message, mLinearLayout, false);

            TextView Message = (TextView) linearLay.findViewById(R.id.my_msg);
            ImageView Image = (ImageView) linearLay.findViewById(R.id.my_msg_img);
            Message.setText(message);
            Message.setVisibility(View.VISIBLE);

            Image.setVisibility(View.GONE);
            mLinearLayout.addView(linearLay);
        } else if(who == 1) {
            View linearLay = LayoutInflater.from(this).inflate(R.layout.other_message, mLinearLayout, false);

            TextView Message = (TextView) linearLay.findViewById(R.id.msg);
            TextView senderName = (TextView) linearLay.findViewById(R.id.sender_name);
            ImageView Image = (ImageView) linearLay.findViewById(R.id.oth_msg_img);

            Message.setText(message);
            Message.setVisibility(View.VISIBLE);

            senderName.setText(sender);

            Image.setVisibility(View.GONE);
            mLinearLayout.addView(linearLay);

        }
        scrollToBottom();
    }

    private void addImage(Bitmap bmp, int who, String sender) {
        if(who == 0) {
            View linearLay = LayoutInflater.from(this).inflate(R.layout.self_message, mLinearLayout, false);

            TextView myMessage = (TextView) linearLay.findViewById(R.id.my_msg);
            ImageView myImage = (ImageView) linearLay.findViewById(R.id.my_msg_img);
            myMessage.setVisibility(View.GONE);
            myImage.setImageBitmap(bmp);
            myImage.setVisibility(View.VISIBLE);


            mLinearLayout.addView(linearLay);

        } else if(who == 1){
            View linearLay = LayoutInflater.from(this).inflate(R.layout.other_message, mLinearLayout, false);

            TextView Message = (TextView) linearLay.findViewById(R.id.msg);
            TextView senderName = (TextView) linearLay.findViewById(R.id.sender_name);
            ImageView Image = (ImageView) linearLay.findViewById(R.id.oth_msg_img);
            Message.setVisibility(View.GONE);

            senderName.setText(sender);
            Image.setImageBitmap(bmp);
            Image.setVisibility(View.VISIBLE);

            mLinearLayout.addView(linearLay);
        }
        scrollToBottom();
    }

    private String encodeImage(String path) {
        File imageFile = new File(path);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(imageFile);
            Log.e("xcfghjjjjj", "bhvgfcvhbjnm");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("Pro", "File not find");
        }
        Bitmap bm = BitmapFactory.decodeStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 10, baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);

        return encImage;
    }

    private Bitmap decodeImage(String data) {
        byte[] b = Base64.decode(data, Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
        return bmp;
    }

    private void scrollToBottom() {
        msgCon.post(new Runnable() {
            @Override
            public void run() {
                msgCon.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private Emitter.Listener handleIncomingMessages = new Emitter.Listener() {
        @Override
    public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message, imgTxt, name;

                    try {
                        message = data.getString("text");
                        name = data.getString("name");
                        imgTxt = data.getString("image");

                        if (message.isEmpty()) {
                            Bitmap b = decodeImage(imgTxt);
                            addImage(b, 1, name);
                        } else if(imgTxt.isEmpty()) {
                            addMessage(message, 1, name);
                        }
                        Log.e("Pro", data + ":" + message);
                    } catch (JSONException e) {

                    }
                }
            });
        }
    };

    private void openGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selected = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selected, filePathColumn, null, null, null);

            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            imgDecodableString = cursor.getString(columnIndex);
            cursor.close();

            sendImage(imgDecodableString);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }
}