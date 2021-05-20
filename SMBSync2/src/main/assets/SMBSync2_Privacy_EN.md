## 1. Collected data  
### 1.1.Data provided to SMBSync2 from users.  

The data provided by the user to use SMBSync2 will be saved in the storage area of the application.  
<span style="color: red;"><u>Data will not be sent externally unless the operation "1.4.Sending or writing data outside SMBSync2" is performed.</u></span>  

- File information (directory name, file name)  
- SMB server information if using SMB server (host name/IP address, port number, account name(**<u>\*1</u>**), account password(**<u>\*1</u>**))  
- ZIP file information if using a ZIP file (compression method, encryption method, encryption password(**<u>\*1</u>**))  
- App setting options (warning messages, language and font size, etc.)  
- Application passwords(**<u>\*1</u>**)  

**<u>\*1</u>**Data is encrypted and stored.  

### 1.2.Execution result of SMBSync2  

Save the data to the storage area in the application so that the user can check the execution result of SMBSync2.  
<span style="color: red;"><u>Data will not be sent externally unless the operation "1.4.Sending or writing data outside SMBSync2" is performed.</u></span>  

- Directory name, file name, execution status  
- File size of synchronized files, file update date and time  
- Error information  

### 1.3.Activity record of SMBSync2  

Enabling logging will save the activity record data in the app's memory area for verification of the app's execution results and technical support. If logging is disabled, data recording will be stopped, but data already recorded will not be deleted.  
<span style="color: red;"><u>Data will not be sent externally unless the operation "1.4.Sending or writing data outside SMBSync2" is performed.</u></span>  

- Device information (manufacturer name, model name, OS version, mount point, app-specific directory, StorageAccessFramework, Storage manager, IP address, WiFi enable/disable, WiFi link speed)  
- SMBSync2 version, SMBSync2 execution options  
- Directory name, file name, execution status  
- File size of synchronized files, file modification date and time  
- Debugging information  
- Error information  

### 1.4.Sending or writing data outside SMBSync2  

Data held by SMBSync2 cannot be sent or written out to outside unless operated by the user.  

- Press "Share button" from History tab.  
- Click "Send to Developer" button from System Information.  
- Click the "Send to Developer" button from the log management.  
- By executing "Export settings" from the menu, "1.1. Data provided to SMBSync2 from users" will be exported.  
By specifying a password when exporting, the information is encrypted and saved in the file.  

### 1.5.Delete the data stored in SMBSync2  

By uninstalling SMBSync2, "1.1. Data provided by users to SMBSync2" and "1.3. Activity record of SMBSync2" will be deleted from the device.  
<span style="color: red;"><u>However, the following information will not be deleted, so please delete the "/storage/emulated/0/SMBSync2" directory and saved files in the file manager. </u></span>.  

- "1.2.Execution result of SMBSync2"  
- Messages displayed (around 5000 lines)  
- Task list save information  
- Date and time the file was updated (if the model does not allow setting)  
- Information saved in the external storage by saving the task list  

### 2.Permissions required to run the application  

### 2.1.Photos, Media and Files  
**read the contents of your USB storage**.  
**modify or delete the contents of your USB storage**.  
Used for file synchronization and reading/writing management files.  

### 2.2.Storage  
**read the contents of your USB storage**.  
**modify or delete the contents of your USB storage**.  
Used for file synchronization and reading/writing management files.  

### 2.3.Wi-Fi connection information  
**view Wi-Fi connections**.  
Used to check the Wi-Fi status when synchronization starts.  

### 2.4.Others  
### 2.4.1.View network connections  
Use this to check network connections when starting synchronization.  
### 2.4.2.connect and disconnect from Wi-Fi  
This function is used to turn Wi-Fi on and off for scheduled synchronization in Andoid 5/6/7/8/9.  
### 2.4.3.Full network access  
Use this to synchronize via SMB protocol through the network.  
### 2.4.4.Run at startup  
Used to perform scheduled synchronization.  
### 2.4.5.Control vibration  
Used to notify the user when synchronization is finished.  
### 2.4.6.Prevent device from sleeping  
Used to start synchronization from a schedule or external app.  
### 2.4.7.Install shortcuts  
Used to add sync start shortcuts to the desktop.  
