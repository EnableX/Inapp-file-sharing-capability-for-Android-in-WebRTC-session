# 1-to-1 RTC: A Sample Android App for Uploading and Downloading Files with EnableX Android Toolkit

This is a Sample Android App that demonstrates the use of EnableX platform Server APIs (https://developer.enablex.io/docs/references/apis/video-api/index/) and Android Toolkit (https://developer.enablex.io/docs/references/sdks/video-sdk/android-sdk/index/) to build 1-to-1 RTC (Real Time Communication) Application.  It allows developers to ramp up on app development by hosting on their own devices. 

This App creates a virtual Room on the fly hosted on the Enablex platform using REST calls and uses the Room credentials (i.e. Room Id) to connect to the virtual Room as a mobile client.  The same Room credentials can be shared with others to join the same virtual Room to carry out an RTC session. 

> EnableX Developer Center: https://developer.enablex.io/


## 1. How to get started

### 1.1 Prerequisites

#### 1.1.1 App Id and App Key 

* Register with EnableX [https://portal.enablex.io/cpaas/trial-sign-up/] 
* Create your Application
* Get your App ID and App Key delivered to your email


#### 1.1.2 Sample Android Client 

* Clone or download this Repository [https://github.com/EnableX/Inapp-file-sharing-capability-for-Android-in-WebRTC-session.git] 


#### 1.1.3 Sample App Server 

* Clone or download this Repository [https://github.com/EnableX/One-to-One-Video-Chat-Sample-Web-Application.git ] & follow the steps further 
* You need to use App ID and App Key to run this Service. 
* Your Android Client End Point needs to connect to this Service to create Virtual Room.
* Follow README file of this Repository to setup the Service.


#### 1.1.4 Configure Android Client 

* Open the App
* Go to WebConstants and change the following:
``` 
 /* To try the App with Enablex Hosted Service you need to set the kTry = true When you setup your own Application Service, set kTry = false */
        
        public  static  final  boolean kTry = true;
        
    /* Your Web Service Host URL. Keet the defined host when kTry = true */
    
        String kBaseURL = "https://demo.enablex.io/"
        
    /* Your Application Credential required to try with EnableX Hosted Service
        When you setup your own Application Service, remove these */
        
        String kAppId = ""  
        String kAppkey = ""  
 ```

### 1.2 Test

#### 1.2.1 Open the App

* Open the App in your Device. You get a form to enter Credentials i.e. Name & Room Id.
* You need to create a Room by clicking the "Create Room" button.
* Once the Room Id is created, you can use it and share with others to connect to the Virtual Room to carry out an RTC Session.
  
## 2 Server API

EnableX Server API is a Rest API service meant to be called from Partner's Application Server to provision video enabled 
meeting rooms. API Access is given to each Application through the assigned App ID and App Key. So, the App ID and App Key 
are to be used as Username and Password respectively to pass as HTTP Basic Authentication header to access Server API.
 
For this application, the following Server API calls are used: 
* https://developer.enablex.io/docs/references/apis/video-api/content/api-routes/#create-a-room - To create new room
* https://developer.enablex.io/docs/references/apis/video-api/content/api-routes/#get-room-information - To get information of a given Room
* https://developer.enablex.io/docs/references/apis/video-api/content/api-routes/#create-a-token - To create Token for a given Room to get into a RTC Session

To know more about Server API, go to:
https://developer.enablex.io/docs/references/apis/video-api/index/


## 3 Android Toolkit

Android App to use Android Toolkit to communicate with EnableX Servers to initiate and manage Real Time Communications.  

* Documentation: https://developer.enablex.io/docs/references/sdks/video-sdk/android-sdk/index/
* Download: https://developer.enablex.io/docs/references/sdks/video-sdk/android-sdk/index/


## 4 Application Walk-through

### 4.1 Create Token

We create a Token for a Room Id to get connected to EnableX Platform to connect to the Virtual Room to carry out an RTC Session.

To create Token, we make use of Server API. Refer following documentation:
https://developer.enablex.io/docs/references/apis/video-api/content/api-routes/#create-a-token


### 4.2 Connect to a Room, Initiate & Publish Stream

We use the Token to get connected to the Virtual Room. Once connected, we intiate local stream and publish into the room. Refer following documentation for this process:
https://developer.enablex.io/docs/references/sdks/video-sdk/android-sdk/room-connection/index/#connect-to-a-room


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

We can upload a File by using the method of EnxRoom object as follows:
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

We can download a File by using the method EnxRoom object as follows:
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

We can get the list of files which are available to download by using the method of EnxRoom object as follows:
``` 
EnxRoom.getAvailableFiles();
``` 

## 5 Trial

Try a quick Video Call: https://demo.enablex.io/
Sign up for a free trial https://portal.enablex.io/cpaas/trial-sign-up/

