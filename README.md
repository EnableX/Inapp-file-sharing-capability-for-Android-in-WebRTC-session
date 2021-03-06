# 1-to-1 RTC: A Sample Android App for Uploading and Downloading Files with EnableX Android Toolkit

This is a Sample Android App demonstrates the use of EnableX (https://www.enablex.io/cpaas/) platform Server APIs and Android Toolkit to build 1-to-1 RTC (Real Time Communication) Application.  It allows developers to ramp up on app development by hosting on their own devices. 

This App creates a virtual Room on the fly hosted on the Enablex platform using REST calls and uses the Room credentials (i.e. Room Id) to connect to the virtual Room as a mobile client.  The same Room credentials can be shared with others to join the same virtual Room to carry out a RTC session. 

> EnableX Developer Center: https://developer.enablex.io/


## 1. How to get started

### 1.1 Pre-Requisites

#### 1.1.1 App Id and App Key 

* Register with EnableX [https://portal.enablex.io/cpaas/trial-sign-up/] 
* Create your Application
* Get your App ID and App Key delivered to your Email


#### 1.1.2 Sample Android Client 

* Clone or download this Repository [https://github.com/EnableX/One-to-One-Video-Call-Webrtc-Application-Sample-for-Android.git] 


#### 1.1.3 Sample App Server 

* Clone or download this Repository [https://github.com/EnableX/One-to-One-Video-Chat-Sample-Web-Application.git ] & follow the steps further 
* You need to use App ID and App Key to run this Service. 
* Your Android Client End Point needs to connect to this Service to create Virtual Room.
* Follow README file of this Repository to setup the Service.


#### 1.1.4 Configure Android Client 

* Open the App
* Go to WebConstants and change the following:
``` 
 String userName = "USERNAME"  /* HTTP Basic Auth Username of App Server */
 String password = "PASSWORD"  /* HTTP Basic Auth Password of App Server */
 String kBaseURL = "FQDN"      /* FQDN of of App Server */
 ```
 
 Note: The distributable comes with demo username and password for the Service. 

### 1.2 Test

#### 1.2.1 Open the App

* Open the App in your Device. You get a form to enter Credentials i.e. Name & Room Id.
* You need to create a Room by clicking the "Create Room" button.
* Once the Room Id is created, you can use it and share with others to connect to the Virtual Room to carry out a RTC Session.
  
## 2 Server API

EnableX Server API is a Rest API service meant to be called from Partners' Application Server to provision video enabled 
meeting rooms. API Access is given to each Application through the assigned App ID and App Key. So, the App ID and App Key 
are to be used as Username and Password respectively to pass as HTTP Basic Authentication header to access Server API.
 
For this application, the following Server API calls are used: 
* https://api.enablex.io/v1/rooms - To create new room
* https://api.enablex.io/v1/rooms/:roomId - To get information of a given Room
* https://api.enablex.io/v1/rooms/:roomId/tokens - To create Token for a given Room to get into a RTC Session

To know more about Server API, go to:
https://developer.enablex.io/api/server-api/


## 3 Android Toolkit

Android App to use Android Toolkit to communicate with EnableX Servers to initiate and manage Real Time Communications.  

* Documentation: https://developer.enablex.io/api/client-api/android-toolkit/
* Download: https://developer.enablex.io/wp-content/uploads/EnxRtcAndroid-release_0.9.2.aar


## 4 Application Walk-through

### 4.1 Create Token

We create a Token for a Room Id to get connected to EnableX Platform to connect to the Virtual Room to carry out a RTC Session.

To create Token, we make use of Server API. Refer following documentation:
https://developer.enablex.io/api/server-api/api-routes/rooms-route/#create-token


### 4.2 Connect to a Room, Initiate & Publish Stream

We use the Token to get connected to the Virtual Room. Once connected, we intiate local stream and publish into the room. Refer following documentation for this process:
https://developer.enablex.io/api/client-api/android-toolkit/enxrtc/


### 4.3 Handle Server Events

EnableX Platform will emit back many events related to the ongoing RTC Session as and when they occur implicitly or explicitly as a result of user interaction. We use Call Back Methods to handle all such events.

``` 
/*Set File Share Observer to receive callbacks */

EnxRoom.setFileShareObserver(File-Share-Observer);

/* Example of Call Back Methods */

/* Call Back Method: onRoomConnected 
Handles successful connection to the Virtual Room */ 

void onRoomConnected(EnxRoom enxRoom, JSONObject roomMetaData){
    /* You may initiate and publish stream */
}

/* Call Back Method: onRoomError
 Error handler when room connection fails */
 
void onRoomError(JSONObject jsonObject){

} 

 
/* Call Back Method: onStreamAdded
 To handle any new stream added to the Virtual Room */
 
void onStreamAdded(EnxStream stream){
    /* Subscribe Remote Stream */
} 


/* Call Back Method: onActiveTalkerList
 To handle any time Active Talker list is updated */
  
void onActiveTalkerList(JSONObject jsonObject){
    /* Handle Stream Players */
}
```
### 4.4 Upload File 

Set File Share Observer to receive callbacks 
``` 
EnxRoom.setFileShareObserver(File-Share-Observer);
``` 

We can upload a File by using the EnxRoom Method as follows:
``` 
EnxRoom.sendFiles(EnxFileShare.Position.TOP,isBrodcast, clientIdList);
``` 

Callbacks for file Uploading :
```
/* Call Back Method: onFileUploadStarted */ 

void onFileUploadStarted(JSONObject jsonObject){
    /* received when file upload started at receiver end */
}

/* Call Back Method: onInitFileUpload*/
 
void onInitFileUpload(JSONObject jsonObject){
  /* received when file upload started at sender end */
}  

/* Call Back Method: onFileAvailable*/
 
public void onFileAvailable(JSONObject jsonObject){
 /* received when file uploaded successfully at receiver end */
} 

/* Call Back Method: onFileUploaded*/
 
void onFileUploaded(JSONObject jsonObject){
 /* received when file uploaded successfully at sender end */
} 

/* Call Back Method: onFileUploadFailed*/

void onFileUploadFailed(JSONObject jsonObject){
 /* received when file upload failed at sender end */
}
``` 

### 4.5 Download File

Set File Share Observer to receive callbacks 
``` 
EnxRoom.setFileShareObserver(File-Share-Observer);
``` 

We can download a File by using the EnxRoom Method as follows:
``` 
 EnxRoom.downloadFile(file-info-JSONObject,isAutoSave);
``` 

Callbacks for file Downloading :
``` 
/* Call Back Method: onFileDownloaded*/
 
void onFileDownloaded(String data){
 /* received when file downloaded successfully*/
} 

/* Call Back Method: onFileDownloadFailed*/
 
void onFileDownloadFailed(JSONObject jsonObject){
/* received when file download failed*/
}

```

### 4.6 Get files Available to download

We can get the list of files which are available to downloading by using the EnxRoom Method as follows:
``` 
EnxRoom.getAvailableFiles();
``` 

## 5 Trial

Try a quick Video Call: https://demo.enablex.io/
Sign up for a free trial https://portal.enablex.io/cpaas/trial-sign-up/

