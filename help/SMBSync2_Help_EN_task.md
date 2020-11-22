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

### File filters / Select files for sync

If unchecked, all files are synchronized. If you check the files filter, you get the following options:

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

### Directory filters / Select subdirectories

If unchecked, all folders are synchronized. If you check the subdirectories filter, you get the following options:

- Directory filter

  Is a custom include/exclude directory filter. You can select the name of the folders you want to exclude or include from the synchronization process.

### Execute sync tasks only when charging

Auto Sync planned tasks won’t start if the device is not charging. Manually starting them is always possible.

### Synchronize the files in root of the master directory

If unchecked, only the folders and their files/subfolders under the master directory are synchronized. By default, it is checked and the files located directly in the root of the master directory will also be synchronized.

### Confirm before overwrite/delete

When checked, it will display a confirmation dialog before overwriting or deleting files. 

### WiFi AP Options

- Run even when off

  Will try to start the sync even if Wifi is turned off

- Conn to any AP

  Will synchronize only when connected to a wifi network. It will accept any WLAN SSID name.

- Has private address

  You can initiate the synchronization only if the IP address assigned to the WiFi network is in the following ranges: 10.0.0.0 - 10.255.255.255, 172.16.0.0 - 172.31.255.255 or 192.168.0.0 - 192.168.255.255.

- IP address list

  You can start syncing only if the WiFi IP address matches one of the specified addresses. You can also directly add the current IP address your device is connected to through the IP selection list.  
You can use wildcards for the filter. (e.g., 192.168.100.\*, 192.168.\*)

### Skip task if the WLAN is not connected to the specified access point, or if WLAN IP does not match the specified IP address.

Auto Sync will skip the task if it doesn’t match the specified criteria 

### Allow sync with all IP addresses (include public)

Sync will be allowed whenever the Wifi is connected to any network, even on public IP ranges (like in a public Wifi). 

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

### Remove directories and files excluded by the filters

If enabled, **it removes directories/files that are excluded from the filter.** 

### Retry on network error (only for SMB shares)

On server-side connection errors, SMBSync2 will try again the synchronization for a maximum of 3 times at a 30 seconds interval. 

### Limit SMB I/O write buffer to 16KB (only for SMB shares)

**Please try if you get an "Access is denied" error when writing to the PC/NAS folder.**

When checked, it will limit I/O buffer to 16KB for writing operations to the SMB host. 

### Write files directly to the SMB folder without using temporary files (only for SMB shares)

Checked by default (recommended). When copied to the SMB host, the file will be copied to a temporary folder on the host. Once the copy operation is succeeded, the temporary file is moved to its final destination overwriting the target file. If unchecked, the target file on the host is immediately overwritten on the start of the copy. If a connection error occurs, the file on the host remains corrupted until the next sync. 

### Do not set last modified time of destination file to match source file

Please enable if you get an error like SmbFile#setLastModified()/File#setLastModified() fails. It means that the remote host doesn’t allow setting file last modified time. If unchecked, the last modified time of the copied file on the target will be set to the time it was copied / synchronized. This means that the target file will appear newer than the master. 

For next synchronizations, you can:

- stick to compare by size only, or

- you can enable the option “Do not overwrite destination file if it is newer than source file” to only copy files modified later on the master, or

- you can enable the task option “Obtain last modification time of files from SMBSync2 application custom list”. However, this option is currently not available if the target is SMB. Most SMB hosts support setting the last modified time. 

See below for a detailed info on each option. 

### Obtain last modification time of files from SMBSync2 application custom list

Please try if all the files are copied over the target files on every sync.

This option is only available for the user when the target is Internal Storage. For other storage types, SMBSync2 tries to autodetect if the target supports setting the file last modified time. If not, it will automatically use this built in function. One exception is when the target is SMB, this function will not be used. See above the option “Do not set last modified time of destination file to match source file” for other workarounds. Note that most recent android devices do not support updating the last modification time of the target file.

During the first sync, SMBSync2 will store the list of files with their timestamps in its database (cf. 1.3). Since the database has to be created the first time, all files with the same name and same size will be overwritten again with a complete copy from the master during the first sync. In the subsequent syncs, the database will be used and overwriting of all same name/same size files is no longer performed. The last modified time of local file is saved by the proprietary SMBSync code (it doesn’t rely on the Java File#setLastModified() function). 

### Delete files prior to sync (Mirror mode only)

When checked, the directories and files that are present on the target folder but that do not exist on the master, will be first deleted. After that, files and folders that are different will be copied to the target.

If the master folder is SMB, the processing time will be longer because the directory structure and their contents is scanned through the network. It is strongly recommended to enable the option " Use SMB2 negotiation" because SMB1 will be very slow.

### Use file size to determine if files are different

When checked, files are considered different if they differ by size. 

### Size only compare

Files are considered different only if size of the source is larger than the destination. This will disable compare by file time. 

### Use time of last modification to determine if files are different 

When checked, files are considered different based on their last modification time 

### Min allowed time difference (in seconds) between source and destination files

Files are considered identical if the difference between their last modified times is less or equal to the selected time in seconds. They are considered different if the time difference between the files is superior to the selected time. FAT and ExFAT need a minimum of 2 seconds tolerance. If 0 seconds is selected, the files must have exactly the same time to be considered similar.

### Do not overwrite destination file if it is newer than source file

If checked, the file will be overwritten only when the master file is newer than the target file even if the file sizes and the last update times are different. Keep in mind that if you change time zones or if the files are modified during the interval period of the Day Light Saving Time change, the last modified file could appear older than the non-updated file. This is related to the file system differences and only a manual check before overwriting the file will avoid data loss. It is generally recommended to not modify files during the interval of day light saving time change if they are meant to be auto-synchronized 

###  Ignore Day Light Saving Time difference between files

Let you select the time difference in minutes between summer and winter time. Files are considered different if the time difference is not exactly equal to the specified interval (+/- the “Min allowed time difference (in seconds)” specified in previous option)

###  Skip directory and file names that contain invalid characters(", :, \, *, <, >, |)

If checked, it will display a warning message and the sync will continue without processing the directories/files containing invalid characters. 

###  Delete the master directory when it is empty (only when Sync option is Move)

When sync mode is Move, after the files are moved to the target, the Master folder is also deleted. 

### Detail information

https://drive.google.com/file/d/0B77t0XpnNT7OYzZ0U01rR0VRMlk/view?usp=sharing

 