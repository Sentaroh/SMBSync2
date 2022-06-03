### Test mode  

When checked, no file is deleted, copied or overwritten. No changes are done to your files on both target and source. Please use the Test mode to check the files that will be deleted/copied/replaced during the real synchronization task. Performed file operations will appear in the Messages tab.  

### Auto sync  

If checked, the task can be started automatically on planned intervals in the scheduler. Tasks that are set to automatic synchronization will start when you press the sync button on the top right corner of the main app screen.  

### Sync task name  

Specify a name for the task. Sync task name is not case-sensitive.   

### Sync type  

Currently supported sync modes are Mirror, Copy, Move or Archive. <span style="color: red; "><u>Synchronization is done in one direction from source to target.</u></span>  

- Mirror  
The target directory is kept as an exact copy of the source. If a file is different between the source and the target, the file on the source overwrites the file on the target. directory and files not present on the target are copied from the source. Files and folders that do not exist on the source are also deleted from the target. Only modified files (by size and/or date/time) are updated on the target.  
- Move  
If a file is different between the source and the target, the file on the source overwrites the file on the target. Once copied to the target, files and folders are deleted from the source (like move command).
Only modified files (by size and/or date/time) are copied to the target. Identical files, based on the selected compare criteria, are deleted from the source without being copied. Files and folders on the target, not present on the source, are obviously preserved.  
- Copy  
Same as Move, but files are not deleted from the source after being copied.
If a file is different between the source and the target, the file on the source overwrites the file on the target. Once copied to the target, files and folders are kept on the source (like a copy command).
Only modified files (by size and/or date/time) are copied to the target. Identical files, based on the selected compare criteria, are ignored and not copied again.  
- Archive  
Archive photos and videos by Moving them from the source to the target directory. Specific medias criteria can be specified for archiving: shooting date/time, date and time of last archive execution (such as 7 days or earlier or 30 days or earlier). ZIP cannot be specified as a target for Archive operations.   

**Compare criteria:** 
Files are considered different based on these criteria:  

1. File/directory name exists only on source or target, not on both sides  
2. Files have different sizes  
3. Files have a different time stamp (last modification date and time)  
Check Advanced options below for more detailed information on compare criteria and more granular settings.  

### Swap source and destination  

Swap the source and target folders: source becomes the target and the target is changed to source.  

### Source folder (previously Master)  

Tap the storage icon/name to edit the source directory.  

### Target folder (Destination)  

Tap the storage icon/name to edit the target directory.  

### Select files for sync  

If unchecked, all files are synchronized. If you check the files filter, you get the following options:  

### File name filter  

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

##### If "Use new filter version 2" advanced option is checked, the following options are available:
- Wildcards like `*` (matches any characters sequence) and `?` (matches any one character) are supported.
- Multiple filters can also be quickly inserted in same field with `;` separator.
- A relative path to specific files can also be entered (relative to Source directory root)
- Exclude filter always has precedence over include filter

##### Filter v2 examples:
We suppose source directory is `/sdcard/source_dir`
- match any file named my_file.txt: `my_file.txt`
- exclude the file `/sdcard/source_dir/data/file.dat`: `data/file.dat`
- match all pdf files: `*.pdf`
- match all *tmp.log files: `*tmp.log`
- match any single char: `my?file.txt` will match my_file.txt, my-file.txt, `my file.txt`, myAfile.txt etc.
- exclude all hidden files: `.*` or just uncheck the option `include hidden files`
- specify quickly multiple files: `*.pdf;path/to/my_file.doc;file*_tmp.log`


### Sync files with a size
File size filter to choose which files to sync depending on their size.  

- Any / Less than / Greater than  
You can specify any file size.  

### Sync files if modification date is
File filter to select which files to sync depending on their last modification date

- `Any` is the default, will include files regardless of their modification time
- `Older than 1 day` will include files which last modification date is older than one day before the sync start.
- `Newer than 1 day` will include files whith a last modification date starting from 1 day before the sync start.
- `On sync start day or later` will include files whith a last modification date starting from 00:00 (midnight) on the sync start day. Even if the synchronization finishes on the next day, the files will be selected based on the sync start day (exp: sync start 01 march at 23:55 and finishes on 02 march at 00:30. Th files will be included in sync if their modification time starts from 01 march 00:00)

### Select folders for sync

If unchecked, all subdirectories will be synchronized. If checked, the below option will appear:

- Directory name filters: 
Select the name of the directories you want to exclude or include from the synchronization process.

##### If "Use new filter version 2" advanced option is checked, the following options are available:
- Wildcards `*` and `?` are supported.
- Include directory filters are always relative to Source directory. There is no such restriction for Exclude filter. This is mainly for speed processing reasons in addition that it is a rarely use case scenario.
- Exclude filter always has precedence over include filter

##### Filter v2 special characters / wildcards:
- `?` Matches any single character. exp. `dir?` matches any path that is named `dir` plus any one character (dir1, dir2, dirA…)
- `*` Matches any characters up to the next slash. exp. `*/*/dir*` matches any path that has two directories, then a file or directory that starts with the name `dir`.
- `\` (exclude filter only): Matches any directory in path. exp. `\dir` matches any path that contains a directory named `dir`.
- `trailing /` (exclude filter only): Will include (create on target) the empty excluded directory (without synchronizing its contents)

##### Filter v2 examples:
- match `source_dir/cache` directory: `cache`
- match any `cache/temp` directory: `\cache/temp` filter will match `source/cache/temp`, `source/dir/cache/temp`
- match any directory name ending with `tmp`: `\*tmp`, wil match `dir_tmp`, `my_DIRtmp`, `dir.tmp`…
- exclude all hidden directories: `\.*` or just uncheck the option `include hidden directories`
- `dir?`: will match dir1, dir2, dira, dirz…
- quickly specify multiple paths: `path/to/dir1;path/to/dir2;path/to/dir3`
- `data/*/*/personal` will match `source/data/dir1/dir2/personal/` directory
- exclude filter `\cache/` will exclude any directory named exactly `cache`. The excluded directory will be created on the target but all its subfolders and files are excluded (excluded directories appear as empty directories on target)
- exclude filter `\cache` will behave like `\cache/` except that excluded `cache` directories are not created on the target

### Execute sync tasks only when charging  
Auto Sync planned tasks won’t start if the device is not charging. Manually starting them is always possible.  

### Synchronize the files in root of the source directory  

If unchecked, only the directories and their files/subfolders under the source directory are synchronized. By default, it is checked and the files located directly in the root of the source directory will also be synchronized.  

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
You can start syncing only if the WiFi IP address matches one of the specified addresses. You can also directly add the current IP address of your device through the IP selection list.  
You can use wildcards for the filter. (e.g., `192.168.100.*`, `192.168.*`)  

There are several ways to synchronize when connecting to a specific WiFi. See the [FAQ](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_FAQ_EN.htm) for details.  

1. Change the IP address to something other than 192.168.0.0/24 on your WiFi router and add it to the IP address list  
2. Fixing an IP address on the Android side and registering it in the IP address list  

### Skip task if the WLAN IP does not match the specified IP address.  

Sync will skip the task if it doesn’t match the specified Wifi AP Options   

### Allow sync with all IP addresses (include public)  

Sync will be allowed whenever the WiFi is connected to any network, even on public IP ranges (like in a public WiFi).  

### Shows advanced options  

**Please use it when setting detailed options.**  

### Include subdirectories  

It will recursively include subdirectories under the specified source folder.   

### Include empty directories  

Synchronizes the empty directories (even if a directory is empty on the source, it will be created on the target). If unchecked, empty directories on the source are ignored.   

### Include hidden directories  

When checked, Sync will include the hidden linux directories (those with a name starting with a dot). Note that in Windows and Samba, the hidden attribute is not set by the folder name. Thus, the synchronized folder on the SMB/Windows target won’t have the host hidden attribute.   

### Include hidden files  

When checked, Sync will include the hidden linux files (those with a name starting with a dot). Note that in Windows and Samba, the hidden attribute is not set by the file name. Thus, the synchronized file on the SMB/Windows target won’t have the host hidden attribute.  

### Overwrite destination files  
If unchecked, files on the target will never be overwritten even if the compare criteria by size and time are different.   

### Use enhanced directory selection filter  
Only available when using old legacy filter mode and not the new "Filter version 2"
If the upper directory is excluded by a filter while one of its sub-directories is explicitely selected by an include filter, the sub-directories of the included directory will be synchronized.   

### Use new filter version 2  

After enabling it, you can use the new updated filter. <span style="color: red; "><u>It may behave differently from the old filter, so please test it thoroughly before using it.</u></span>
The new filter v2 is a highly recommended upgrade, but you must ensure proper setup of your filters when migrating from previous version to the new filter

##### Filter v2 syntax:
- Whole dir prefix: match anywhere in path
  + Exclude any folder named dir: `\dir`
  + Exclude all `dir1/dir2` directories: `\dir1/dir2`
  + Include ANY folder named dir: not allowed as the overhead would be important

- Create on target empty excluded directories
  + `dir1/dir2` : excluded dir2 is not created on target
  + `dir1/dir2/` : excluded dir2 is created as empty directory on target
  + `\dir and \dir/` : will behave same as above for any directory named `dir`

- All other Include and Exclude filters behavior is identical between filters.
The below filter combinations are allowed and act as expected also with Whole dir prefix `\`
  + `cache` : will match `source/cache/*`
  + `/cache` : will match `source/cache/*`
  + `cache*` : will match `source/cache*/`
  + `*cache` : will match `source/*cache`
  + `/cache/data` : will match `source/cache/data/*`
  + `/cache/data/` : will match `source/cache/data/*`
  + `cache/data` : will match `source/cache/data/*`
  + `cache/data*` : will match `source/cache/data*`

- Wildcard `?` : matches a single character
  + `dir?/dir5` : will match `source/dirX/dir5`

- Wildcard `*` will match any character up to the next dir separator `/` character
  + `dir*/dir2` : will match `source/dirxxxx/dir2`
  + `*/dir`  : will match `source/oneDirLevel/dir`
  + `dir1/*/dir2` : will match `source/dir1/oneDirLevel/dir2`
  + `dir/*` : will match `source/dir/oneDirLevel`


### Delete files prior to sync (Mirror mode only)  

When checked, it will first delete directories and files that do not exist in the source folder, and then copy files whose file size and last modification time are different from the source folder. If the source folder is SMB, the processing time will be longer because the files are scanned over the network. If possible, please use "SMBv2/3" for SMB protocol as SMB v1 is much slower.  

### Remove folders and files excluded by name filters  

If enabled, **it will remove directories and files that are either excluded or implicitely not included by filters.**   
This can cause unwanted deletes on the target if not used with cautions. Teh target will contain only directories and files that are included by all file and directory filters

### Retry on network error  
Retry synchronization only in case of SMB server error. Up to three retries will be made, each after 30 seconds from the time of the error.  

### Limit SMB I/O write buffer to 16 KB  

When checked, it will limit I/O buffer to 16KB for writing operations to the SMB host. 
**Please try this option if you get an "Access is denied" error when writing to the remote SMB directory.**  

### Write files directly to the SMB folder without using temporary files  

Unchecked by default (recommended). If unchecked, when copied to the SMB host, the file will be copied to a temporary directory on the host. Once the copy operation is succeeded, the temporary file is moved to its final destination overwriting the target file. If checked, the target file on the host is immediately overwritten on the start of the copy. If a connection error occurs, <span style="color: red; "><u>the file on the host remains corrupted until the next sync.</u></span>  

### Do not set last modification time of destination file to match source file  

Enable this option if you get an error like SmbFile.setLastModified()/File.setLastModified() fails. It means that the remote host doesn’t allow setting file last modification time. If checked, the last modification time of the copied file on the target will be set to the time it was copied / synchronized. This means that the target file will appear newer than the source.   

### Obtain last modification time of files from SMBSync2 application custom list  

Try this if all files are copied every time. It maintains the last modification date and time of local files using SMBSync2's own method instead of using Java File.setLastModified().  

### Use file size to determine if files are different  

When checked, files are considered different if they differ by size.   

### Size only compare (files are considered different only if size of the source is larger than the destination)  

If checked, only when the source file size is large, it will be targeted for synchronization.  
This will disable compare by file time.

### Use time of last modification to determine if files are different  
When checked, files are considered different if their last modification time are not the same, even if the target file has a newer date/time.

### Min allowed time difference (in seconds) between source and destination files  
Choose between 0, 1, 3, 5 and 10 seconds. If the difference between the last modification times of the files is within the selected time difference, no change will be made. Selecting 0 will require the target file to have the exact time stamp as teh source file to be considered unchanged.

### Do not overwrite destination file if it is newer than source file  
If checked, the file will be overwritten only when the source file is newer than the target file, even if the file size and last modification time are different.  

### Ignore Day Light Saving Time difference between files  
If checked, it will ignore the time difference between Daylight Saving Time and Standard Time.  

### Time difference between daylight saving time and standard time (minutes)  
Specify the time difference to ignore.  

### Skip directory and file names that contain invalid characters（"，：，\，*，<，>, |）  

If checked, sync will skip directories/files that contain characters that cannot be used, but will display a warning message and process the next directory/file.  
If unchecked, a sync error will occur if the target cannot support read/write opertaions on a file or directory because of unsupported characters and the sync operation will abort.

### Ignore empty files (0 Bytes size)  

If checked, files with a file size of 0 bytes will be ignored.  

### Ignore the file if its name length exceeds the specified value  

Specifies the maximum length (in bytes) of the output file name. Sync will skip the file if the number of bytes in the file name exceeds the specified value. For Android storage, 255 is the maximum. For SMB, the maximum allowed is 237 bytes.

### Manuals  
[FAQ](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_FAQ_JA.htm)  
[Manual](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_Desc_JA.htm)   
