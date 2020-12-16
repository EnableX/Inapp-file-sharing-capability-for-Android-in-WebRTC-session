package com.enablex.demoenablex.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enablex.demoenablex.R;
import com.enablex.demoenablex.adapter.DownloadFileAdapter;
import com.enablex.demoenablex.adapter.UserListAdapter;
import com.enablex.demoenablex.utilities.FileDownloadModel;
import com.enablex.demoenablex.utilities.OnDragTouchListener;
import com.enablex.demoenablex.utilities.UserListModels;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import enx_rtc_android.Controller.EnxActiveTalkerViewObserver;
import enx_rtc_android.Controller.EnxFileShare;
import enx_rtc_android.Controller.EnxFileShareObserver;
import enx_rtc_android.Controller.EnxPlayerView;
import enx_rtc_android.Controller.EnxReconnectObserver;
import enx_rtc_android.Controller.EnxRoom;
import enx_rtc_android.Controller.EnxRoomObserver;
import enx_rtc_android.Controller.EnxRtc;
import enx_rtc_android.Controller.EnxStream;
import enx_rtc_android.Controller.EnxStreamObserver;


public class VideoConferenceActivity extends AppCompatActivity
        implements EnxRoomObserver, EnxStreamObserver, View.OnClickListener, EnxReconnectObserver, EnxFileShareObserver, DownloadFileAdapter.DownloadFileClickListener, EnxActiveTalkerViewObserver, UserListAdapter.UserItemClickListener {
    EnxRtc enxRtc;
    String token;
    String name;
    EnxPlayerView enxPlayerView;
    FrameLayout moderator;
    FrameLayout participant;
    ImageView disconnect;
    ImageView mute, video, camera, volume;
    EnxRoom enxRooms;
    boolean isVideoMuted = false;
    boolean isFrontCamera = true;
    boolean isAudioMuted = false;
    RelativeLayout rl;
    Gson gson;
    EnxStream localStream;
    EnxPlayerView enxPlayerViewRemote;
    ProgressDialog progressDialog;
    DownloadFileAdapter downloadFileAdapter;
    ArrayList<FileDownloadModel> downloadFilesList;
    PopupWindow popupWindow;
    int PERMISSION_ALL = 1;
    UserListAdapter userListAdapter;
    List<UserListModels> participantList;
    private String mFileName;
    RecyclerView mRecyclerView;
    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_conference);
        getPreviousIntent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            } else {
                initialize();
            }
        }
    }

    @Override
    public void onRoomConnected(EnxRoom enxRoom, JSONObject jsonObject) {
        //received when user connected with Enablex room
        enxRooms = enxRoom;
        if (enxRooms != null) {
            enxRooms.publish(localStream);
            enxRooms.setReconnectObserver(this);
            enxRooms.setFileShareObserver(this);
            enxRooms.setActiveTalkerViewObserver(this::onActiveTalkerList);
        }
        try {
            String localClientId = jsonObject.getString("clientId");
            JSONArray userList = jsonObject.getJSONArray("userList");
            for (int i = 0; i < userList.length(); i++) {
                JSONObject userListJsonObject = userList.getJSONObject(i);
                String participantName = userListJsonObject.optString("name");
                String clientId = userListJsonObject.optString("clientId");
                if (participantName != null) {
                    if (!clientId.equalsIgnoreCase(localClientId)) {
                        UserListModels userListModel = new UserListModels();
                        userListModel.setClientId(clientId);
                        userListModel.setName(participantName);
                        userListModel.setRole(userListJsonObject.optString("role"));
                        participantList.add(userListModel);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRoomError(JSONObject jsonObject) {
        //received when any error occurred while connecting to the Enablex room
        Toast.makeText(VideoConferenceActivity.this, jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onUserConnected(JSONObject jsonObject) {
        // received when a new remote participant joins the call
        try {
            String participantName = jsonObject.optString("name");
            String clientId = jsonObject.optString("clientId");
            if (participantName != null) {
                UserListModels userListModel = new UserListModels();
                userListModel.setClientId(clientId);
                userListModel.setName(participantName);
                userListModel.setRole(jsonObject.optString("role"));
                participantList.add(userListModel);
            }
            userListAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUserDisConnected(JSONObject jsonObject) {
        // received when a  remote participant left the call
        try {
            String currentClientId = jsonObject.optString("clientId");
                for (int i = 0; i < participantList.size(); i++) {
                    String clientId = participantList.get(i).getClientId();
                    if (currentClientId.equalsIgnoreCase(clientId)) {
                        UserListModels userListModel = participantList.get(i);
                        participantList.remove(userListModel);
                    }
                }

            userListAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPublishedStream(EnxStream enxStream) {
        //received when audio video published successfully to the other remote users
    }

    @Override
    public void onUnPublishedStream(EnxStream enxStream) {
        //received when audio video unpublished successfully to the other remote users
    }

    @Override
    public void onStreamAdded(EnxStream enxStream) {
        //received when a new stream added
        if (enxStream != null) {
            enxRooms.subscribe(enxStream);
        }
    }

    @Override
    public void onSubscribedStream(EnxStream enxStream) {
        //received when a remote stream subscribed successfully
    }

    @Override
    public void onUnSubscribedStream(EnxStream enxStream) {
        //received when a remote stream unsubscribed successfully
    }

    @Override
    public void onRoomDisConnected(JSONObject jsonObject) {
        //received when Enablex room successfully disconnected
        this.finish();
    }

    @Override
    public void onActiveTalkerList(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        if (recyclerView == null) {
            participant.removeAllViews();

        } else {
            participant.removeAllViews();
            participant.addView(recyclerView);

        }
    }

    @Override
    public void onEventError(JSONObject jsonObject) {
        //received when any error occurred for any room event
        Toast.makeText(VideoConferenceActivity.this, jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEventInfo(JSONObject jsonObject) {
        // received for different events update
    }

    @Override
    public void onNotifyDeviceUpdate(String s) {
        // received when when new media device changed
    }

    @Override
    public void onAcknowledgedSendData(JSONObject jsonObject) {
        // received your chat data successfully sent to the other end
    }

    @Override
    public void onMessageReceived(JSONObject jsonObject) {
        // received when chat msg received
    }

    @Override
    public void onUserDataReceived(JSONObject jsonObject) {
        //received when custom chat data received
    }

    @Override
    public void onSwitchedUserRole(JSONObject jsonObject) {
        // received when user switch their role (from moderator  to participant)
    }

    @Override
    public void onUserRoleChanged(JSONObject jsonObject) {
        // received when user role changed successfully
    }

    @Override
    public void onAudioEvent(JSONObject jsonObject) {
        //received when audio mute/unmute happens
        try {
            String message = jsonObject.getString("msg");
            if (message.equalsIgnoreCase("Audio On")) {
                mute.setImageResource(R.drawable.unmute);
                isAudioMuted = false;
            } else if (message.equalsIgnoreCase("Audio Off")) {
                mute.setImageResource(R.drawable.mute);
                isAudioMuted = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVideoEvent(JSONObject jsonObject) {
        //received when video mute/unmute happens
        try {
            String message = jsonObject.getString("msg");
            if (message.equalsIgnoreCase("Video On")) {
                video.setImageResource(R.drawable.ic_videocam);
                isVideoMuted = false;
            } else if (message.equalsIgnoreCase("Video Off")) {
                video.setImageResource(R.drawable.ic_videocam_off);
                isVideoMuted = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceivedData(JSONObject jsonObject) {
        //received when chat data received at room level
    }

    @Override
    public void onRemoteStreamAudioMute(JSONObject jsonObject) {
        //received when any remote stream mute audio
    }

    @Override
    public void onRemoteStreamAudioUnMute(JSONObject jsonObject) {
        //received when any remote stream unmute audio
    }

    @Override
    public void onRemoteStreamVideoMute(JSONObject jsonObject) {
        //received when any remote stream mute video
    }

    @Override
    public void onRemoteStreamVideoUnMute(JSONObject jsonObject) {
        //received when any remote stream unmute audio
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.disconnect:
                roomDisconnect();
                break;
            case R.id.mute:
                if (localStream != null) {
                    if (!isAudioMuted) {
                        localStream.muteSelfAudio(true);
                    } else {
                        localStream.muteSelfAudio(false);
                    }
                }
                break;
            case R.id.video:
                if (localStream != null) {
                    if (!isVideoMuted) {
                        localStream.muteSelfVideo(true);
                    } else {
                        localStream.muteSelfVideo(false);
                    }
                }
                break;
            case R.id.camera:
                if (localStream != null) {
                    if (!isVideoMuted) {
                        if (isFrontCamera) {
                            localStream.switchCamera();
                            camera.setImageResource(R.drawable.rear_camera);
                            isFrontCamera = false;
                        } else {
                            localStream.switchCamera();
                            camera.setImageResource(R.drawable.front_camera);
                            isFrontCamera = true;
                        }
                    }
                }
                break;
            case R.id.volume:
                if (enxRooms != null) {
                    showRadioButtonDialog();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    initialize();
                } else {
                    Toast.makeText(this, "Please enable permissions to further proceed.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
//            super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (enxRooms != null) {
            enxRooms.stopVideoTracksOnApplicationBackground(true, true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (enxRooms != null) {
            enxRooms.startVideoTracksOnApplicationForeground(true, true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (enxPlayerViewRemote != null) {
            enxPlayerViewRemote.release();
        }
        if (enxPlayerView != null) {
            enxPlayerView.release();
        }
        if (localStream != null) {
            localStream.detachRenderer();
        }
        if (enxRooms != null) {
            enxRooms = null;
        }
        if (enxRtc != null) {
            enxRtc = null;
        }
    }

    private void initialize() {
        setUI();
        setClickListener();
        gson = new Gson();
        getSupportActionBar().setTitle("QuickApp");
        enxRtc = new EnxRtc(this, this, this);
        localStream = enxRtc.joinRoom(token, getLocalStreamJsonObject(), getReconnectInfo(), new JSONArray());
        enxPlayerView = new EnxPlayerView(this, EnxPlayerView.ScalingType.SCALE_ASPECT_BALANCED, true);
        participantList = new ArrayList<>();
        userListAdapter = new UserListAdapter(this, participantList, this);
        localStream.attachRenderer(enxPlayerView);
        moderator.addView(enxPlayerView);
        downloadFilesList = new ArrayList<>();
        progressDialog = new ProgressDialog(this);
    }

    private void setClickListener() {
        disconnect.setOnClickListener(this);
        mute.setOnClickListener(this);
        video.setOnClickListener(this);
        camera.setOnClickListener(this);
        volume.setOnClickListener(this);
        moderator.setOnTouchListener(new OnDragTouchListener(moderator));
    }

    private void setUI() {
        moderator = (FrameLayout) findViewById(R.id.moderator);
        participant = (FrameLayout) findViewById(R.id.participant);
        disconnect = (ImageView) findViewById(R.id.disconnect);
        mute = (ImageView) findViewById(R.id.mute);
        video = (ImageView) findViewById(R.id.video);
        camera = (ImageView) findViewById(R.id.camera);
        volume = (ImageView) findViewById(R.id.volume);
        rl = (RelativeLayout) findViewById(R.id.rl);
    }

    private JSONObject getLocalStreamJsonObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("audio", true);
            jsonObject.put("video", true);
            jsonObject.put("data", false);
            JSONObject videoSize = new JSONObject();
            videoSize.put("minWidth", 320);
            videoSize.put("minHeight", 180);
            videoSize.put("maxWidth", 1280);
            videoSize.put("maxHeight", 720);
            jsonObject.put("videoSize", videoSize);
            jsonObject.put("audioMuted", false);
            jsonObject.put("videoMuted", false);
            jsonObject.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void getPreviousIntent() {
        if (getIntent() != null) {
            token = getIntent().getStringExtra("token");
            name = getIntent().getStringExtra("name");
        }
    }

    public JSONObject getReconnectInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("allow_reconnect",true);
            jsonObject.put("number_of_attempts",3);
            jsonObject.put("timeout_interval",15);
            jsonObject.put("activeviews","view");//view

            JSONObject object = new JSONObject();
            object.put("audiomute",true);
            object.put("videomute",true);
            object.put("bandwidth",true);
            object.put("screenshot",true);
            object.put("avatar",true);

            object.put("iconColor", getResources().getColor(R.color.colorPrimary));
            object.put("iconHeight",30);
            object.put("iconWidth",30);
            object.put("avatarHeight",200);
            object.put("avatarWidth",200);
            jsonObject.put("playerConfiguration",object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showRadioButtonDialog() {
        final Dialog dialog = new Dialog(VideoConferenceActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.radiogroup);
        List<String> stringList = new ArrayList<>();  // here is list

        List<String> deviceList = enxRooms.getDevices();
        for (int i = 0; i < deviceList.size(); i++) {
            stringList.add(deviceList.get(i));
        }
        RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);
        String selectedDevice = enxRooms.getSelectedDevice();
        if (selectedDevice != null) {
            for (int i = 0; i < stringList.size(); i++) {
                RadioButton rb = new RadioButton(VideoConferenceActivity.this); // dynamically creating RadioButton and adding to RadioGroup.
                rb.setText(stringList.get(i));
                rg.addView(rb);
                if (selectedDevice.equalsIgnoreCase(stringList.get(i))) {
                    rb.setChecked(true);
                }

            }
            dialog.show();
        }

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int childCount = group.getChildCount();
                for (int x = 0; x < childCount; x++) {
                    RadioButton btn = (RadioButton) group.getChildAt(x);
                    if (btn.getId() == checkedId) {
                        enxRooms.switchMediaDevice(btn.getText().toString());
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    private void roomDisconnect() {
        if (enxRooms != null) {
            if (enxPlayerView != null) {
                enxPlayerView.release();
                enxPlayerView = null;
            }
            if (enxPlayerViewRemote != null) {
                enxPlayerViewRemote.release();
                enxPlayerViewRemote = null;
            }
            enxRooms.disconnect();
        } else {
            this.finish();
        }
    }

    @Override
    public void onReconnect(String message) {
        // received when room tries to reconnect due to low bandwidth or any connection interruption
        try {
            if (message.equalsIgnoreCase("Reconnecting")) {
                progressDialog.setMessage("Wait, Reconnecting");
                progressDialog.show();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUserReconnectSuccess(EnxRoom enxRoom, JSONObject jsonObject) {
        // received when reconnect successfully completed
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        Toast.makeText(this, "Reconnect Success", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFileUploadStarted(JSONObject jsonObject) {
        Log.e("onFileUploadStated", jsonObject.toString());
        Toast.makeText(this, jsonObject.optString("sender") + " is sharing file " +
                jsonObject.optString("name")
                + " of size " + jsonObject.optString("size"), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInitFileUpload(JSONObject jsonObject) {
        Log.e("onInitFileUpload", jsonObject.toString());
        if (progressDialog != null) {
            progressDialog.setMessage("File uploading is in progress");
            progressDialog.show();
        }
    }

    @Override
    public void onFileAvailable(JSONObject jsonObject) {
        Log.e("onFileAvailable", jsonObject.toString());
        Toast.makeText(this, "File Available", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFileUploaded(JSONObject jsonObject) {
        Log.e("onFileUploaded", jsonObject.toString());
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFileUploadFailed(JSONObject jsonObject) {
        Log.e("onFileUploadFailed", jsonObject.toString());
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        Toast.makeText(this, jsonObject.optString("desc"), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFileUploadCancelled(JSONObject jsonObject) {

    }

    @Override
    public void onFileDownloaded(JSONObject jsonObject) {

    }

    @Override
    public void onFileDownloadCancelled(JSONObject jsonObject) {

    }

    @Override
    public void onInitFileDownload(JSONObject jsonObject) {

    }

    @Override
    public void onConferencessExtended(JSONObject jsonObject) {

    }

    @Override
    public void onConferenceRemainingDuration(JSONObject jsonObject) {

    }

    @Override
    public void onAckDropUser(JSONObject jsonObject) {

    }

    @Override
    public void onAckDestroy(JSONObject jsonObject) {

    }

    @Override
    public void onAckPinUsers(JSONObject jsonObject) {

    }

    @Override
    public void onAckUnpinUsers(JSONObject jsonObject) {

    }

    @Override
    public void onPinnedUsers(JSONObject jsonObject) {

    }

    @Override
    public void onFileDownloadFailed(JSONObject jsonObject) {
        Log.e("onFileDownloadFailed", jsonObject.toString());
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        Toast.makeText(this, jsonObject.optString("desc"), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder menuBuilder = (MenuBuilder) menu;
//            menuBuilder.setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                if (enxRooms != null) {
                    enxRooms.sendFiles(true, null,this);
                }
                break;
            case R.id.action_download:
                if (enxRooms != null) {
                    try {
                        if(enxRooms.getAvailableFiles()!=null)
                            parseFilesData(enxRooms.getAvailableFiles());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (downloadFilesList.size() > 0) {
                        View vItem = findViewById(R.id.action_download);
                        LayoutInflater inflater = (LayoutInflater)
                                getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View view = inflater.inflate(R.layout.file_list_dialog, null);
                        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.user_listRV);
                        TextView textView = (TextView) view.findViewById(R.id.TV);
                        textView.setText("Download File");
                        downloadFileAdapter = new DownloadFileAdapter(this, downloadFilesList, this);
                        recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        recyclerView.setAdapter(downloadFileAdapter);
                        popupWindow = new PopupWindow(view, 400, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
                        popupWindow.showAsDropDown(vItem, -153, 0);
                    } else {
                        Toast.makeText(this, "No File Available.", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.action_userlist:
                if (enxRooms != null) {
                    if (participantList.size() > 0) {
                        View vItem = findViewById(R.id.action_userlist);
                        LayoutInflater inflater = (LayoutInflater)
                                getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View view = inflater.inflate(R.layout.user_list_dialog, null);
                        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.user_listRV);
                        recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        recyclerView.setAdapter(userListAdapter);
                        popupWindow = new PopupWindow(view, 400, RelativeLayout.LayoutParams.WRAP_CONTENT, true);

                        popupWindow.showAsDropDown(vItem, -153, 0);
                    } else {
                        Toast.makeText(this, "No Participant present.", Toast.LENGTH_SHORT).show();
                    }
                }
              break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void parseFilesData(JSONObject jsonObject) throws JSONException {
        JSONArray files = jsonObject.optJSONArray("files");
        if (files.length() == 0) {
            return;
        }
        downloadFilesList.clear();
        for (int i = 0; i < files.length(); i++) {
            FileDownloadModel model = new FileDownloadModel();
            model.setSender(files.optJSONObject(i).optString("sender"));
            model.setSenderId(files.optJSONObject(i).optString("senderId"));
            model.setName(files.optJSONObject(i).optString("name"));
            model.setSize(files.optJSONObject(i).optString("size"));
            model.setSize(files.optJSONObject(i).optString("expiresAt"));
            model.setIndex(files.optJSONObject(i).optInt("index"));
            downloadFilesList.add(model);
        }
    }

    @Override
    public void onClickFile(int position) {
        Log.e("onClickFile", String.valueOf(position));
        if (enxRooms != null) {
            try {
                if (progressDialog != null) {
                    progressDialog.setMessage("Wait, File downloading is in progress");
                    progressDialog.show();
                }
                mFileName = downloadFilesList.get(position).getName();
                JSONObject jsonObject = enxRooms.getAvailableFiles();
                JSONArray files = jsonObject.optJSONArray("files");
                enxRooms.downloadFile(files.getJSONObject(position),false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public File saveFile(final String imageData) throws IOException {
        String[] value = imageData.split(",");
        if (value[1].length() == 0) {
            return null;
        }
        byte[] imgBytesData = android.util.Base64.decode(value[1], Base64.NO_WRAP);
        File fileName;
        FileOutputStream fileOutputStream;
        if (value[0].contains("image")) {
            fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/" + mFileName);
            fileOutputStream = new FileOutputStream(fileName);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            try {
                bufferedOutputStream.write(imgBytesData);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            fileName = new File(Environment.getExternalStorageDirectory() + "/" + mFileName);
            FileOutputStream os = new FileOutputStream(fileName, true);
            os.write(imgBytesData);
            os.flush();
            os.close();
        }
        Toast.makeText(this, "File Downloaded at " + fileName + " successfully", Toast.LENGTH_SHORT).show();
        return fileName;
    }

    @Override
    public void onFileClick(int position) {
        Log.e("onFileClick", String.valueOf(position));
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        if (enxRooms != null) {
            List<String> list = new ArrayList<>();
            list.add(participantList.get(position).getClientId());
            enxRooms.sendFiles(false, list,this);
        }
    }
}
