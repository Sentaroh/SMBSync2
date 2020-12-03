### Search SMB server

Scans the network to find available SMB server

### Server host name/IP address

Specify the SMB server name or the IP address 

### SMB Protocol

You can specify the SMB protocol.

1. Use SMB V1

2. Use SMB V2 (2.01)

   SMB min v2.1, max v2.1

3. Use SMB V2 (2.11)

   SMB min v2.1, max v2.1

4. Use SMB V2/3 (2.12)

   Support SMB V2 or V3 protocol

   SMB min v2.0.2, max v3.0

5. Use SMB V2/3 (2.14)

   SMB min v2.0.2, max v3.0 

### Use SMB2 negotiation

When checked, it will force setProperty("jcifs.smb.client.useSMB2Negotiation", "true") in the JcifsAuth() function. This ensures the use of SMB2 non-backward compatible negotiation style, incompatible with versions prior to SMB 2.0.2 

### SMB IPC Signing Enforced

When checked, the client is required to use SMB signing for IPC$ connections as DCERPC transport. Else it is allowed, but not required. 

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
(Shares names will be displayed when you press the “List Shares “ button. Click the desired share name, and then press OK to validate)

### List Directories

Show directories list on the selected Share.   
When you press the list directory button, the selectable directories are displayed. 

Tap on the directory you want to choose and then on the “Select” button. To recurse through subdirectories, tap the directory name. 

### Directory

Please enter the target or master directory. If you specify a directory that does not exist in the target, it will be created during synchronization. 

### Add keywords

%YEAR%, %MONTH%, %DAY% and %DAY-OF-YEAR% can be used as directory names. The keywords are converted into the year, month, and day on which the sync started.   
In the master directory it is converted to the synchronization start date and time.

**These options are displayed only when the sync type is Copy/Move.**

### Append the photo/video shooting time/date to the directory name

If checked, the EXIF data of the backed up media is used to get the shooting date and time. The shooting timestamp is used and appended to the target folder. When the app is unable to get the shooting time from the EXIF, it uses the last modified time of the file.

**The following options are displayed only when the sync type is Archive.**

### If the date and time cannot be determined by EXIF data, display a confirmation message.

If checked, when the shooting date and time cannot be acquired from EXIF , a confirmation message is displayed as to whether or not use the last modified time of the file instead. If you select Cancel in the confirmation dialog, the file will not be archived. 

### Archive files if…

Choose the time criteria to determine which files to archive. Time selection is based on the photo/video shooting date, or the last modified time if it is not possible to acquire the timestamp from the EXIF header.

1. Any date (all)

   Archive all pictures/videos

2. Older than 7 days

   Archive only files with a shooting date older than the current time by 7 days or more

3. Older than 30 days

   Archive only files with a shooting date older than the current time by 30 days or more

4. Older than 60 days

   Archive only files with a shooting date older than the current time by 60 days or more

5. Older than 90 days

   Archive only files with a shooting date older than the current time by 90 days or more

6. Older than 180 days

   Archive only files with a shooting date older than the current time by 180 days or more

7. Older than 1 year

   Archive only files with a shooting date older than the current time by 1 year or more

 

### Increment file names by appending [sequence number]

Append the incremental sequence number to the file name when archiving.

1. Do not change

   Do not append a sequence number

2. 3 digits sequence

   Append 001-999

3. 4 digits sequence

   Append 0001-9999

4. 5 digits sequence

   Append 00001-99999

5. 6 digits sequence number

   Append 000001-999999

 

### Change the name of the file before archiving it in the target folder. 

If checked, the file name of the archived file will be changed based on your selected criteria. You can append the date and time in the file name "File name template". 

### File name template

Enter the patterns to use to rename the archived files. Default value is DSC_%DATE%

1. %ORIGINAL-NAME%

   Will be replaced by original file name during archive archiving (Ex. DSC_0001)

2. %DATE%

   Will be replaced by the shooting date.(Ex. 2018-01-01)

3. %TIME%

   Will be replaced by the shooting time.(Ex. 13:01:10) 

The last line in the screen displays how your target folder and file name will look like once archived. 

### Create a directory based on the shooting date to store the files.

Creates a time stamped directory where to store the archived files.  
When checked, the "Directory name template" is displayed.

### Directory template

Enter the patterns to use to rename the directory to be created (exp. DIR-% YEAR% -% MONTH% and so on). By pressing the patterns buttons, you can enter keywords behind the cursor.

1. %YEAR%

   Will be replaced by the shooting Year. (Ex. 2018)

2. %MONTH%

   Will be replaced by the shooting Month (Ex. 01)

3. %DAY%

   Will be replaced by the shooting Day (Ex. 29) 

The last line in the screen displays how your target folder and file name will look like once archived. 

### Detail information

https://drive.google.com/file/d/0B77t0XpnNT7OYzZ0U01rR0VRMlk/view?usp=sharing

 