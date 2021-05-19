### Search SMB server  
Scans the network to find available SMB server  

### Server host name/IP address  
Specify the SMB server name or the IP address   

### SMB Protocol  
You can specify the SMB protocol.  

- Use SMB V1  
- Use SMB V2 (2.01)  
- Use SMB V2 (2.11)  
- Use SMB V2/3 (2.12)  
- Use SMB V2/3 (2.14)  

### Specify port number  
Specify the port number if different from default. If left empty, SMBSync2 will try to connect to the default standard port numbers 139 and 445.   

### Use Account name and password  
Must be completed if required by the server.   

### User name  
User account name to connect to the host. A Microsoft account cannot be used with SMBSync2. Please create a local account and use it.   

### Password  
Account password to connect to the host.   

### List Shares  
Connects to the specified host and list all available shares for the account.   

### List Directories  
When you press the list button, selectable directories will be displayed, so select it. Tap the directory name to open a subdirectory.  

### Directory  
You can enter a directory name directly. If you specify a non-existent directory, it will be created when the synchronization is executed.  
%YEAR%, %MONTH%, %DAY% and %DAY-OF-YEAR% can be used as directory names. The keywords are converted into the year, month, and day on which the sync started.  

### Append the photo/video shooting time/date to the directory name  

**These options are displayed only when the sync type is Copy/Move.**  
If checked, the EXIF data of the backed up media is used to get the shooting date and time. The shooting timestamp is used and appended to the target folder. When the app is unable to get the shooting time from the EXIF, it uses the last modified time of the file.  

**<u>The following options are displayed only when the sync type is Archive.</u>**  

### If the date and time cannot be determined by EXIF data, display a confirmation message.  

If checked, when the shooting date and time cannot be acquired from EXIF , a confirmation message is displayed as to whether or not use the last modified time of the file instead. If you select Cancel in the confirmation dialog, the file will not be archived.  

### Archive files if…  

Choose the time criteria to determine which files to archive. Time selection is based on the photo/video shooting date, or the last modified time if it is not possible to acquire the timestamp from the EXIF header.  

- Any date (all)  
Archive all pictures/videos  
- Older than 7 days  
Archive only files with a shooting date older than the current time by 7 days or more  
- Older than 30 days  
Archive only files with a shooting date older than the current time by 30 days or more  
- Older than 60 days  
Archive only files with a shooting date older than the current time by 60 days or more  
- Older than 90 days  
Archive only files with a shooting date older than the current time by 90 days or more  
- Older than 180 days  
Archive only files with a shooting date older than the current time by 180 days or more  
- Older than 1 year  
Archive only files with a shooting date older than the current time by 1 year or more  

### Rename files when archiving and store them in a specified directory.  

If checked, the file will be renamed when it is archived. You can use the "File name template" to add date and time to the file name. You can also create a directory to store the files. To store files in a directory, enable "Save to directory when archiving".   

### Increment file names by appending  

Add an order number to the file name when archiving.  

- Do not change  
Do not append a sequence number  
- 2 digits sequence  
Append 01-99  
- 3 digits sequence  
Append 001-999  
- 4 digits sequence  
 Append 0001-9999  
- digits sequence  
 Append 00001-99999  
- 6 digits sequence number  
 Append 000001-999999  

### Change the name of the file before archiving it in the target folder.   

If checked, the file name of the archived file will be changed based on your selected criteria. You can append the date and time in the file name "File name template".  

- %ORIGINAL-NAME%  
 Will be replaced by original file name during archive archiving (Ex. DSC_0001)  
- %DATE%  
 Will be replaced by the shooting date.(Ex. 2018-01-01)  
- %TIME%  
 Will be replaced by the shooting time.(Ex. 13:01:10)  
 The last line in the screen displays how your target folder and file name will look like once archived.  

### Create a directory based on the shooting date to store the files.  

Creates a time stamped directory where to store the archived files.  

### Directory template  

Enter the patterns to use to rename the directory to be created (exp. DIR-% YEAR% -% MONTH% and so on). By pressing the patterns buttons, you can enter keywords behind the cursor.  

- %YEAR%  
 Will be replaced by the shooting Year. (Ex. 2018)  
- %MONTH%  
 Will be replaced by the shooting Month (Ex. 01)  
- %DAY%  
 Will be replaced by the shooting Day (Ex. 29)  

The last line in the screen displays how your target folder and file name will look like once archived.  

### Manuals  
[FAQ](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_FAQ_EN.htm)  
[Manual](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_Desc_EN.htm)   
