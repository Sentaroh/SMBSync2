### Test mode<br>

When checked, no file is deleted, copied or overwritten. No changes are done to your files on both target and master. Please use the Test mode to check the files that will be deleted/copied/replaced during the real synchronization task. Performed file operations will appear in the Messages tab.<br>

### Auto sync<br>

If checked, the task can be started automatically on planned intervals in the scheduler. Tasks that are set to automatic synchronization will start when you press the sync button on the top right corner of the main app screen.<br>

### Sync task name<br>

Specify a name for the task. Sync task name is not case-sensitive. <br>

### Sync type<br>

Currently supported sync modes are Mirror, Copy, Move or Archive. <span style="color: red; "><u>Synchronization is done in one direction of the target from the master.</u></span><br>

- Mirror<br>
The target folder is kept as an exact copy of the master. If a file is different between the master and the target, the file on the master overwrites the file on the target. Folder and files not present on the target are copied from the master. Files and folders that do not exist on the master are also deleted from the target. Only modified files (by size and/or date/time) are updated on the target.<br>
- Move<br>
If a file is different between the master and the target, the file on the master overwrites the file on the target. Once copied to the target, files and folders are deleted from the master (like move command).
Only modified files (by size and/or date/time) are copied to the target. Identical files, based on the selected compare criteria, are deleted from the master without being copied. Files and folders on the target, not present on the master, are obviously preserved.<br>
- Copy<br>
Same as Move, but files are not deleted from the master after being copied.
If a file is different between the master and the target, the file on the master overwrites the file on the target. Once copied to the target, files and folders are kept on the master (like a copy command).
Only modified files (by size and/or date/time) are copied to the target. Identical files, based on the selected compare criteria, are ignored and not copied again.<br>
- Archive<br>
Archive photos and videos by Moving them from the master to the target folder. Specific medias criteria can be specified for archiving: shooting date/time, date and time of last archive execution (such as 7 days or earlier or 30 days or earlier). ZIP cannot be specified as a target for Archive operations. <br>

**Compare criteria:** 
Files are considered different based on these criteria:<br>

1. File/folder name exists only on master or target, not on both sides<br>
2. Files have different sizes<br>
3. Files have a different time stamp (last modification date and time)<br>
Check Advanced options below for more detailed information on compare criteria and more granular settings.<br>
### Swap source and destination<br>

Swap the master and target folders: master becomes the target and the target is changed to master.<br>

### Master folder (Source)<br>

Tap the storage icon/name to edit the master folder.<br>

### Target folder (Destination)<br>

Tap the storage icon/name to edit the target folder.<br>

### Select files for sync<br>

If unchecked, all files are synchronized. If you check the files filter, you get the following options:<br>

###File name filter<br>

- Sync audio files<br>
When checked, sync will include files with the following extensions:<br>
aac, aif, aifc, aiff, flac, kar, m3u, m4a, mid, midi, mp2, mp3, mpga, ogg, ra, ram, wav<br>
- Sync image files<br>
When checked, sync will include files with the following extensions:<br>
bmp, cgm, djv, djvu, gif, ico, ief, jpe, jpeg, jpg, pbm, pgm, png, pnm, ppm, ras, rgb, svg, tif, tiff, wbmp, xbm, xpm, xwd<br>
- Sync video files<br>
When checked, sync will include files with the following extensions:<br>
avi, m4u, mov, mp4, movie, mpe, mpeg, mpg, mxu, qt, wmv<br>
- File filter<br>
Is a custom include/exclude file filter. You can select the name and extension of the files you want to exclude or include from the synchronization process. <br>

###File size filter<br>

You can choose which files to sync depending on their size.<br>

- Less than/Greater than<br>
You can specify any file size.<br>

###File last modified filter<br>
You can select a file based on its last modified date.<br>

- If you specify 1 day older than selects files whose last modification date is between the start date and the day before the start date.<br>
- If you specify Older than 1 day selects files whose last modification date is before the day before the start of synchronization.<br>
- After the begin date of sync, the files whose last modification date is the start date of synchronization will be selected. (If the synchronization finishes on the next day, the files will be selected after the start date.)<br>

### Select subdirectories<br>

If unchecked, all subdirectories will be synchronized. If checked, a directory filter button will appear.<br>

### Execute sync tasks only when charging<br>
Auto Sync planned tasks won’t start if the device is not charging. Manually starting them is always possible.<br>

### Synchronize the files in root of the master directory<br>

If unchecked, only the folders and their files/subfolders under the master directory are synchronized. By default, it is checked and the files located directly in the root of the master directory will also be synchronized.<br>

### Confirm before overwrite/delete<br>
When checked, it will display a confirmation dialog before overwriting or deleting files. <br>

### WiFi AP Options<br>
You can set whether synchronization can start or not based on the network status.<br>

- Run even when off<br>
Will try to start the sync even if Wifi is turned off<br>
- Conn to any AP<br>
Will synchronize only when connected to a WiFi network. It will accept any WLAN SSID name.<br>
- Has private address<br>
You can initiate the synchronization only if the IP address assigned to the WiFi network is in the following ranges: 10.0.0.0 - 10.255.255.255, 172.16.0.0 - 172.31.255.255 or 192.168.0.0 - 192.168.255.255.<br>
- IP address list<br>
You can start syncing only if the WiFi IP address matches one of the specified addresses. You can also directly add the current IP address your device is connected to through the IP selection list.<br>
You can use wildcards for the filter. (e.g., 192.168.100.\*, 192.168.\*)<br>

There are several ways to synchronize when connecting to a specific WiFi. See the [FAQ](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_FAQ_EN.htm) for details.<br>

1. Change the IP address to something other than 192.168.0.0/24 on your WiFi router and add it to the IP address list<br>
2. Fixing an IP address on the Android side and registering it in the IP address list<br>

### Skip task if the WLAN IP does not match the specified IP address.<br>

Sync will skip the task if it doesn’t match the specified criteria <br>

### Allow sync with all IP addresses (include public)<br>

Sync will be allowed whenever the WiFi is connected to any network, even on public IP ranges (like in a public WiFi).<br>

### Shows advanced options<br>

**Please use it when setting detailed options.**<br>

### Include subdirectories<br>

It will recursively include subdirectories under the specified master folder. <br>

### Include empty directories<br>

Synchronizes the empty directories (even if a directory is empty on the master, it will be created on the target). If unchecked, empty directories on the master are ignored. <br>

### Include hidden directories<br>

When checked, Sync will include the hidden linux folders (those with a name starting with a dot). Note that in Windows and Samba, the hidden attribute is not set by the folder name. Thus, the synchronized folder on the SMB/Windows target won’t have the host hidden attribute. <br>

### Include hidden files<br>

When checked, Sync will include the hidden linux files (those with a name starting with a dot). Note that in Windows and Samba, the hidden attribute is not set by the file name. Thus, the synchronized file on the SMB/Windows target won’t have the host hidden attribute.<br>

### Overwrite destination files<br>
If unchecked, files on the target will never be overwritten even if the compare criteria by size and time are different. <br>

### Use enhanced directory selection filter<br>

If the upper directory is excluded by a filter while one of its sub-directories is selected/included, the sub-directories of the selected folder will be synchronized. <br>

### Use new filter version 2<br>

After enabling it, you can use the new filter. <span style="color: red; "><u>It may behave differently from the old filter, so please test it thoroughly before using it.</u></span> <br>

### Delete files prior to sync (Mirror mode only)<br>

When checked, it will first delete directories and files that do not exist in the master folder, and then copy files whose file size and last modified time are different from the master folder. If the master folder is SMB, the processing time will be longer because the files are scanned over the network. If possible, please use "SMBv2/3" for SMB protocol.<br>

### Remove folders and files excluded by name filters<br>

If enabled, **it removes directories/files that are excluded from the filter.** <br>

### Retry on network error<br>
Retry synchronization only in case of SMB server error. Up to three retries will be made, each after 30 seconds from the time of the error.<br>

### Limit SMB I/O write buffer to 16 KB<br>

When checked, it will limit I/O buffer to 16KB for writing operations to the SMB host. 
**Please try if you get an "Access is denied" error when writing to the PC/NAS folder.**<br>

### Write files directly to the SMB folder without using temporary files<br>

Checked by default (recommended). When copied to the SMB host, the file will be copied to a temporary folder on the host. Once the copy operation is succeeded, the temporary file is moved to its final destination overwriting the target file. If unchecked, the target file on the host is immediately overwritten on the start of the copy. If a connection error occurs, <span style="color: red; "><u>the file on the host remains corrupted until the next sync.</u></span><br>

### Do not set last modified time of destination file to match source file<br>

Please enable if you get an error like SmbFile.setLastModified()/File.setLastModified() fails. It means that the remote host doesn’t allow setting file last modified time. If unchecked, the last modified time of the copied file on the target will be set to the time it was copied / synchronized. This means that the target file will appear newer than the master. <br>

### Obtain last modification time of files from SMBSync2 application custom list<br>

Try this if all files are copied every time. maintain the last modified date and time of local files using SMBSync2's own method instead of using Java File.setLastModified().<br>

### Use file size to determine if files are different<br>

When checked, files are considered different if they differ by size. <br>

### Size only compare (files are considered different only if size of the source is larger than the destination)<br>

If checked, only when the master file size is large, it will be targeted for synchronization.<br>

### Use time of last modification to determine if files are different<br>
When checked, if the last modified time of a file is different, it is judged as a difference file.<br>

Min allowed time difference (in seconds) between source and destination files<br>
Choose between 1, 3 and 10 seconds. If the difference between the last modified times of the files is within the selected time difference, no change will be made.<br>

### Do not overwrite destination file if it is newer than source file<br>
If checked, the file will be overwritten only when the master file is newer than the target file, even if the file size and last modified time are different.<br>

### Ignore Day Light Saving Time difference between files<br>
If checked, it will ignore the time difference between Daylight Saving Time and Standard Time.<br>

### Time difference between daylight saving time and standard time (minutes)<br>
Specify the time difference to ignore.<br>

### Skip directory and file names that contain invalid characters（"，：，\，*，<，>, |）<br>

If checked, it will not process directories/files that contain characters that cannot be used, but will display a warning message and process the next directory/file.<br>

### Ignore empty files (0 Bytes size)<br>

If checked, files with a file size of 0 bytes will be ignored.<br>

### Manuals<br>
[FAQ](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_FAQ_JA.htm)<br>
[Manual](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_Desc_JA.htm) <br>
