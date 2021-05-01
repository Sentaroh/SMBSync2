## 1.Data recorded by the app

The app will record a ”Synchronization task list” and, depending on the settings, a ”App activity record”. <span style="color: red; "><u>In addition, the recorded data will not be sent out by the app unless the user has manipulated it.</u></span>

### 1.1.Synchronization task list

The app records the necessary data to perform the synchronization.

- Directory name, file name, SMB server host name, IP address, port number, account name, password (***1**)
- App password (***1**) to protect app launch and setting change
- App settings

***1** password is encrypted with a system generated password and stored in the AndroidKeystore.

### 1.2.App activity record

The app needs to record the following data to check the synchronization results and for troubleshooting.

- Android version, device maker, device name, device model, application version
- Directory name, file name, file size, file last modified time
- SMB server host name, IP address, port number, account name
- Network interface name, IP address
- System settings
- App settings

### 1.3. Exported settings and Sync task list 

The app can export "1.1 Synchronization task list" to a file. You can password protect the file before exporting it.

- Directory name, file name
- SMB server host name, IP address, port number, account name, password
- App settings

### 1.4.Send recorded data from the app

Data recorded by the app can be sent by following these steps:

- Press "Share button" from History tab
- Press the "Send to Developer" button from the “System info”
- Press "Share button" or "Send to developer" button from “Manage log files”

## 2. Permissions

The app uses the following permissions.

### 2.1.Photos/Media/Files

**read the contents of your USB storage**  
**modify or delete the contents of your USB storage**

Required for file synchronization to internal/external/USB storage and to read/write operations on application data files.

### 2.2.Storage

**read the contents of your USB storage**  
**modify or delete the contents of your USB storage**

Required for file synchronization to internal/external/USB storage and to read/write operations on application data files.

### 2.3.Wi-Fi Connection information

**view Wi-Fi connections**

Required to check the status of Wi-Fi (on/off) at the start of synchronization.

### 2.4.Other

### 2.4.1.view network connections

Required to confirm that device is connected to the network at the start of synchronization.

### 2.4.2.connect and disconnect from Wi-Fi

Required to turn on / off Wi-Fi before and after a scheduled synchronization.

### 2.4.3.full network access

Required to perform network synchronization using the SMB protocol.

### 2.4.4.run at startup

Required to perform scheduled synchronization.

### 2.4.5.control vibration

Required to notify the user by vibration at the end of synchronization.

### 2.4.6.prevent device from sleeping

Required to start synchronization from a scheduled or external application.

### 2.4.7.install shortcuts

Required to add a synchronization task shortcut on the home screen.
