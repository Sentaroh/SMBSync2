### Test mode  

When checked, no file is deleted, copied or overwritten. No changes are done to your files on both target and master. Please use the Test mode to check the files that will be deleted/copied/replaced during the real synchronization task. Performed file operations will appear in the Messages tab.  

### Auto sync  

If checked, the task can be started automatically on planned intervals in the scheduler. Tasks that are set to automatic synchronization will start when you press the sync button on the top right corner of the main app screen.  

### Sync task name  

Specify a name for the task. Sync task name is not case-sensitive.   

### Sync type  

Currently supported sync modes are Mirror, Copy, Move or Archive. <span style="color: red; "><u>Synchronization is done in one direction of the target from the master.</u></span>  

- Mirror  
The target folder is kept as an exact copy of the master. If a file is different between the master and the target, the file on the master overwrites the file on the target. Folder and files not present on the target are copied from the master. Files and folders that do not exist on the master are also deleted from the target. Only modified files (by size and/or date/time) are updated on the target.  
- Move  
If a file is different between the master and the target, the file on the master overwrites the file on the target. Once copied to the target, files and folders are deleted from the master (like move command).
Only modified files (by size and/or date/time) are copied to the target. Identical files, based on the selected compare criteria, are deleted from the master without being copied. Files and folders on the target, not present on the master, are obviously preserved.  
- Copy  
Same as Move, but files are not deleted from the master after being copied.
If a file is different between the master and the target, the file on the master overwrites the file on the target. Once copied to the target, files and folders are kept on the master (like a copy command).
Only modified files (by size and/or date/time) are copied to the target. Identical files, based on the selected compare criteria, are ignored and not copied again.  
- Archive  
Archive photos and videos by Moving them from the master to the target folder. Specific medias criteria can be specified for archiving: shooting date/time, date and time of last archive execution (such as 7 days or earlier or 30 days or earlier). ZIP cannot be specified as a target for Archive operations.   

**Compare criteria:** 
Files are considered different based on these criteria:  

1. File/folder name exists only on master or target, not on both sides  
2. Files have different sizes  
3. Files have a different time stamp (last modification date and time)  
Check Advanced options below for more detailed information on compare criteria and more granular settings.  
### Swap source and destination  

Swap the master and target folders: master becomes the target and the target is changed to master.  

### Master folder (Source)  

Tap the storage icon/name to edit the master folder.  

### Target folder (Destination)  

Tap the storage icon/name to edit the target folder.  

### Select files for sync  

If unchecked, all files are synchronized. If you check the files filter, you get the following options:  

###File name filter  

- Sync audio files  
When checked, sync will include files with the following extensions:  
aac, aif, aifc, aiff, flac, kar, m3u, m4a, mid, midi, mp2, mp3, mpga, ogg, ra, ram, wav  
- Sync image files  
When checked, sync will include files with the following extensions:  
bmp, cgm, djv, djvu, gif, ico, ief, jpe, jpeg, jpg, pbm, pgm, png, pnm, ppm, ras, rgb, svg, tif, tiff, wbmp, xbm, xpm, xwd  
- Sync video files  
When checked, sync will include files with the following extensions:  
avi, m4u, mov, mp4, movie, mpe, mpeg, mpg, mxu, qt, wmv  
- File filter  
Is a custom include/exclude file filter. You can select the name and extension of the files you want to exclude or include from the synchronization process.   

###File size filter  

You can choose which files to sync depending on their size.  

- Less than/Greater than  
You can specify any file size.  

###File last modified filter  
You can select a file based on its last modified date.  

- If you specify 1 day older than selects files whose last modification date is between the start date and the day before the start date.  
- If you specify Older than 1 day selects files whose last modification date is before the day before the start of synchronization.  
- After the begin date of sync, the files whose last modification date is the start date of synchronization will be selected. (If the synchronization finishes on the next day, the files will be selected after the start date.)  

### Select subdirectories  

If unchecked, all subdirectories will be synchronized. If checked, a directory filter button will appear.  

### Execute sync tasks only when charging  
Auto Sync planned tasks won’t start if the device is not charging. Manually starting them is always possible.  

### Synchronize the files in root of the master directory  

If unchecked, only the folders and their files/subfolders under the master directory are synchronized. By default, it is checked and the files located directly in the root of the master directory will also be synchronized.  

### Confirm before overwrite/delete  
When checked, it will display a confirmation dialog before overwriting or deleting files.   

### WiFi AP Options  
You can set whether synchronization can start or not based on the network status.  

- Run even when off  
Will try to start the sync even if Wifi is turned off  
- Conn to any AP  
Will synchronize only when connected to a WiFi network. It will accept any WLAN SSID name.  
- Has private address  
You can initiate the synchronization only if the IP address assigned to the WiFi network is in the following ranges: 10.0.0.0 - 10.255.255.255, 172.16.0.0 - 172.31.255.255 or 192.168.0.0 - 192.168.255.255.  
- IP address list  
You can start syncing only if the WiFi IP address matches one of the specified addresses. You can also directly add the current IP address your device is connected to through the IP selection list.  
You can use wildcards for the filter. (e.g., 192.168.100.\*, 192.168.\*)  

There are several ways to synchronize when connecting to a specific WiFi. See the [FAQ](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_FAQ_EN.htm) for details.  

1. Change the IP address to something other than 192.168.0.0/24 on your WiFi router and add it to the IP address list  
2. Fixing an IP address on the Android side and registering it in the IP address list  

### Skip task if the WLAN IP does not match the specified IP address.  

Sync will skip the task if it doesn’t match the specified criteria   

### Allow sync with all IP addresses (include public)  

Sync will be allowed whenever the WiFi is connected to any network, even on public IP ranges (like in a public WiFi).  

### Shows advanced options  

**Please use it when setting detailed options.**  

### Include subdirectories  

It will recursively include subdirectories under the specified master folder.   

### Include empty directories  

Synchronizes the empty directories (even if a directory is empty on the master, it will be created on the target). If unchecked, empty directories on the master are ignored.   

### Include hidden directories  

When checked, Sync will include the hidden linux folders (those with a name starting with a dot). Note that in Windows and Samba, the hidden attribute is not set by the folder name. Thus, the synchronized folder on the SMB/Windows target won’t have the host hidden attribute.   

### Include hidden files  

When checked, Sync will include the hidden linux files (those with a name starting with a dot). Note that in Windows and Samba, the hidden attribute is not set by the file name. Thus, the synchronized file on the SMB/Windows target won’t have the host hidden attribute.  

### Overwrite destination files  
If unchecked, files on the target will never be overwritten even if the compare criteria by size and time are different.   

### Use enhanced directory selection filter  

If the upper directory is excluded by a filter while one of its sub-directories is selected/included, the sub-directories of the selected folder will be synchronized.   

### Use new filter version 2  

After enabling it, you can use the new filter. <span style="color: red; "><u>It may behave differently from the old filter, so please test it thoroughly before using it.</u></span>   

### Delete files prior to sync (Mirror mode only)  

When checked, it will first delete directories and files that do not exist in the master folder, and then copy files whose file size and last modified time are different from the master folder. If the master folder is SMB, the processing time will be longer because the files are scanned over the network. If possible, please use "SMBv2/3" for SMB protocol.  

### Remove folders and files excluded by name filters  

If enabled, **it removes directories/files that are excluded from the filter.**   

### Retry on network error  
Retry synchronization only in case of SMB server error. Up to three retries will be made, each after 30 seconds from the time of the error.  

### Limit SMB I/O write buffer to 16 KB  

When checked, it will limit I/O buffer to 16KB for writing operations to the SMB host. 
**Please try if you get an "Access is denied" error when writing to the PC/NAS folder.**  

### Write files directly to the SMB folder without using temporary files  

Checked by default (recommended). When copied to the SMB host, the file will be copied to a temporary folder on the host. Once the copy operation is succeeded, the temporary file is moved to its final destination overwriting the target file. If unchecked, the target file on the host is immediately overwritten on the start of the copy. If a connection error occurs, <span style="color: red; "><u>the file on the host remains corrupted until the next sync.</u></span>  

### Do not set last modified time of destination file to match source file  

Please enable if you get an error like SmbFile.setLastModified()/File.setLastModified() fails. It means that the remote host doesn’t allow setting file last modified time. If unchecked, the last modified time of the copied file on the target will be set to the time it was copied / synchronized. This means that the target file will appear newer than the master.   

### Obtain last modification time of files from SMBSync2 application custom list  

Try this if all files are copied every time. maintain the last modified date and time of local files using SMBSync2's own method instead of using Java File.setLastModified().  

### Use file size to determine if files are different  

When checked, files are considered different if they differ by size.   

### Size only compare (files are considered different only if size of the source is larger than the destination)  

If checked, only when the master file size is large, it will be targeted for synchronization.  

### Use time of last modification to determine if files are different  
When checked, if the last modified time of a file is different, it is judged as a difference file.  

Min allowed time difference (in seconds) between source and destination files  
Choose between 1, 3 and 10 seconds. If the difference between the last modified times of the files is within the selected time difference, no change will be made.  

### Do not overwrite destination file if it is newer than source file  
If checked, the file will be overwritten only when the master file is newer than the target file, even if the file size and last modified time are different.  

### Ignore Day Light Saving Time difference between files  
If checked, it will ignore the time difference between Daylight Saving Time and Standard Time.  

### Time difference between daylight saving time and standard time (minutes)  
Specify the time difference to ignore.  

### Skip directory and file names that contain invalid characters（"，：，\，*，<，>, |）  

If checked, it will not process directories/files that contain characters that cannot be used, but will display a warning message and process the next directory/file.  

### Ignore empty files (0 Bytes size)  

If checked, files with a file size of 0 bytes will be ignored.  

### Ignore the file if the length of the file name is longer than the specified value  

Specifies the maximum length (in bytes) of the output file name. Ignores the file if the number of bytes in the file name exceeds the maximum.  

### Manuals  
[FAQ](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_FAQ_JA.htm)  
[Manual](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_Desc_JA.htm)   
