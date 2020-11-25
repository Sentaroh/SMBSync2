## 1.Functions
SMBSync2 is a tool for synchronizing files via wireless LAN using SMB1,SMB2 or SMB3 protocol between the internal storage of Android terminal, SDCARD and PC/NAS. Synchronization is a one-way from the master to the target. Mirror, Move, Copy and Archive modes are supported. Many storage combinations are supported (Internal storage, SDCARD, OTG-USB, SMB, ZIP)
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

  Archive photos and videos by Moving them from the master to the target folder. Specific medias criteria can be specified for archiving: shooting date/time, date and time of last archive execution (such as 7 days or earlier or 30 days or earlier).
  ZIP cannot be specified as a target for Archive operations.

**Compare criteria:** 
Files are considered different based on these criteria:
1. File/folder name exists only on master or target, not on both sides
2. Files have different sizes
3. Files have a different time stamp (last modification date and time)

In Advanced Options, many compare settings can be adjusted: time tolerance interval can be set to ignore difference if less than 1, 3, 5 or 10 sec for compatibility with FAT/exFAT medias. Ignore Daylight Saving time is supported. Option to not overwrite target file if it is newer than the master or if it is larger in size…
When target is on Internal Storage or on the SD Card, most Android systems do not permit setting the last modified time of the target file to match the time of the source file. When target is SMB (PC/NAS), or OTG-USB storage, this is usually not an issue. SMSync2 detects if the time/date can be set on the target to match the source file. If not, the last update time of the file is recorded in the application database files. It is then used to compare the files and check if they differ by time. In that case, if you try to synchronize the master/target pair with a third-party application or if SMBSync2 data files are erased, the source files will be copied again to the target. You can set the option to “Not overwrite destination file if it is newer than the master” in addition to comparing by size to overcome this issue.

## 2.FAQs
Please refer to the PDF link below.
https://drive.google.com/file/d/1a8CTRu9xoCD74Qn0YZxzry-LHxQ8j7dE/view?usp=sharing
## 3.Library
- [jcifs-ng ClientLibrary](https://github.com/AgNO3/jcifs-ng)
- [jcifs-1.3.17](https://jcifs.samba.org/)
- [Zip4J 1.3.2](https://mvnrepository.com/artifact/net.lingala.zip4j/zip4j/1.3.2)
- [Metadata-extractor](https://github.com/drewnoakes/metadata-extractor)
## 4.Documents
Please refer to the PDF link below.
https://drive.google.com/file/d/0B77t0XpnNT7OYzZ0U01rR0VRMlk/view?usp=sharing

