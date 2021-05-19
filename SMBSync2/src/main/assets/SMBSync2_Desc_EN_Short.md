## 1.Functions  
SMBSync2 is a tool for synchronizing files via wireless LAN using SMB1,SMB2 or SMB3 protocol between the internal storage of Android terminal, SDCARD/USB-OTG and PC/NAS. Synchronization is a one-way from the master to the target. Mirror, Move, Copy and Archive modes are supported. Many storage combinations are supported (Internal storage, SDCARD, USB-OTG, SMB, ZIP)  
Sync can be automatically started by external applications (Tasker, AutoMagic etc) or SMBSync2 schedule.   
Sync occurs between two folder pairs called the Master (source folder) and the Target (destination folder). It is a one direction Sync, from the Master to the Target.
The supported Sync modes are:  

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
  Move photos and videos in the master's directory to the target if they were taken before 7 days or 30 days before the archive execution date. (However, you cannot use zip to target.)  
The following file types are eligible for archiving.  
gif, "jpg", "jpeg", "jpe", "png", "mp4", "mov".  

**Compare criteria:** 
Files are considered different based on these criteria:  

1. File name exists only on master or target, not on both sides  
2. Files have different sizes  
3. Files have a different time stamp (last modification date and time)  

In Advanced Options, many compare settings can be adjusted.(Here is an example)  
- Time tolerance interval can be set to ignore difference if less than 1, 3, 5 or 10 sec for compatibility with FAT/exFAT medias.   
- Ignore Daylight Saving time is supported.   
- Option to not overwrite target file if it is newer than the master or if it is larger in size.   

## 2.Documents  
[FAQ](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_FAQ_EN.htm)  
[Manual](https://sentaroh.github.io/Documents/SMBSync2/SMBSync2_Desc_EN.htm)  
