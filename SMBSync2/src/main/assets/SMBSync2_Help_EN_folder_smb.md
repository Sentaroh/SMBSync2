### Search SMB server<br>
Scans the network to find available SMB server<br>

### Server host name/IP address<br>
Specify the SMB server name or the IP address <br>

### SMB Protocol<br>
You can specify the SMB protocol.<br>

- Use SMB V1<br>
- Use SMB V2 (2.01)<br>
- Use SMB V2 (2.11)<br>
- Use SMB V2/3 (2.12)<br>
- Use SMB V2/3 (2.14)<br>

### Specify port number<br>
Specify the port number if different from default. If left empty, SMBSync2 will try to connect to the default standard port numbers 139 and 445. <br>

### Use Account name and password<br>
Must be completed if required by the server. <br>

### User name<br>
User account name to connect to the host. A Microsoft account cannot be used with SMBSync2. Please create a local account and use it. <br>

### Password<br>
Account password to connect to the host. <br>

### List Shares<br>
Connects to the specified host and list all available shares for the account. <br>

### List Directories<br>
When you press the list button, selectable directories will be displayed, so select it. Tap the directory name to open a subdirectory.<br>

### Directory<br>
You can enter a directory name directly. If you specify a non-existent directory, it will be created when the synchronization is executed.<br>
%YEAR%, %MONTH%, %DAY% and %DAY-OF-YEAR% can be used as directory names. The keywords are converted into the year, month, and day on which the sync started.<br>

### Append the photo/video shooting time/date to the directory name<br>

**These options are displayed only when the sync type is Copy/Move.**<br>
If checked, the EXIF data of the backed up media is used to get the shooting date and time. The shooting timestamp is used and appended to the target folder. When the app is unable to get the shooting time from the EXIF, it uses the last modified time of the file.<br>

**<u>The following options are displayed only when the sync type is Archive.</u>**<br>

### If the date and time cannot be determined by EXIF data, display a confirmation message.<br>

If checked, when the shooting date and time cannot be acquired from EXIF , a confirmation message is displayed as to whether or not use the last modified time of the file instead. If you select Cancel in the confirmation dialog, the file will not be archived.<br>

### Archive files if…<br>

Choose the time criteria to determine which files to archive. Time selection is based on the photo/video shooting date, or the last modified time if it is not possible to acquire the timestamp from the EXIF header.<br>

- Any date (all)<br>
Archive all pictures/videos<br>
- Older than 7 days<br>
Archive only files with a shooting date older than the current time by 7 days or more<br>
- Older than 30 days<br>
Archive only files with a shooting date older than the current time by 30 days or more<br>
- Older than 60 days<br>
Archive only files with a shooting date older than the current time by 60 days or more<br>
- Older than 90 days<br>
Archive only files with a shooting date older than the current time by 90 days or more<br>
- Older than 180 days<br>
Archive only files with a shooting date older than the current time by 180 days or more<br>
- Older than 1 year<br>
Archive only files with a shooting date older than the current time by 1 year or more<br>

### Rename files when archiving and store them in a specified directory.<br>

If checked, the file will be renamed when it is archived. You can use the "File name template" to add date and time to the file name. You can also create a directory to store the files. To store files in a directory, enable "Save to directory when archiving". <br>

### Increment file names by appending<br>

Add an order number to the file name when archiving.<br>

- Do not change<br>
Do not append a sequence number<br>
- 2 digits sequence<br>
Append 01-99<br>
- 3 digits sequence<br>
Append 001-999<br>
- 4 digits sequence<br>
 Append 0001-9999<br>
- digits sequence<br>
 Append 00001-99999<br>
- 6 digits sequence number<br>
 Append 000001-999999<br>

### Change the name of the file before archiving it in the target folder. <br>

If checked, the file name of the archived file will be changed based on your selected criteria. You can append the date and time in the file name "File name template".<br>

- %ORIGINAL-NAME%<br>
 Will be replaced by original file name during archive archiving (Ex. DSC_0001)<br>
- %DATE%<br>
 Will be replaced by the shooting date.(Ex. 2018-01-01)<br>
- %TIME%<br>
 Will be replaced by the shooting time.(Ex. 13:01:10)<br>
 The last line in the screen displays how your target folder and file name will look like once archived.<br>

### Create a directory based on the shooting date to store the files.<br>

Creates a time stamped directory where to store the archived files.<br>

### Directory template<br>

Enter the patterns to use to rename the directory to be created (exp. DIR-% YEAR% -% MONTH% and so on). By pressing the patterns buttons, you can enter keywords behind the cursor.<br>

- %YEAR%<br>
 Will be replaced by the shooting Year. (Ex. 2018)<br>
- %MONTH%<br>
 Will be replaced by the shooting Month (Ex. 01)<br>
- %DAY%<br>
 Will be replaced by the shooting Day (Ex. 29)<br>

The last line in the screen displays how your target folder and file name will look like once archived.<br>

### Manuals<br>
[FAQ](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_FAQ_EN.htm)<br>
[Manual](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_Desc_EN.htm) <br>
