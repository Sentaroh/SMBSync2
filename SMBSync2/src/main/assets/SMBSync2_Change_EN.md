#### 2021-06-17 Ver2.54

- Prevent app crash

#### 2021-06-15 Ver2.53

- Added support for changing the execution order of tasks in the schedule.
- Added support for specifying the length of file names to ignore.

#### 2021-05-14 Ver2.52

- Fixed an error in Media Scan.

#### 2021-03-07 Ver2.51

- Fixed a bug that left a notification message.

#### 2021-03-01 Ver2.50

- Improved Chinese (zh) translation
- Resolved a bug that left a notification message when rebooting

#### 2021-03-07 Ver2.49

- Fixed the bug that notification messages remain.

#### 2021-03-01 Ver2.50

- Improved Chinese (zh)
- Resolved a bug that left a notification message when rebooting

#### 2021-02-22 Ver2.49

- Fixed bug that file size filter warning message is always displayed when app is launched.
- Fixed the bug that notification messages remain.

#### 2021-02-20 Ver2.48

- Added file size and file last modified date to file filters
- Added an option to ignore files with a file size of 0 bytes  

#### 2021-01-08 Ver2.47

- Correct translations
- Avoided app force close

#### 2020-12-26 Ver2.46

- Improved display of spinner selection in black theme
- Made new filters available

#### 2020-12-03 Ver2.45

- Enabled to add keywords (e.g. %YEAR) for synchronization date in the master directory
- Correcting the Italian translation

#### 2020-11-24 Ver2.44

- Allowed to change the font size in the settings. (80%,100%,120%,160%)

#### 2020-11-21 Ver2.43
- added support for SMB timeout value in SMBv2/3
- Corrected the Italian translation
- Scrolling Tab enabled

#### 2020-11-13 Ver2.42
- Update task help file

#### 2020-11-12 Ver2.41
- Update help file

#### 2020-11-03 Ver2.39
- Location permissions are no longer used to comply with the Google Play Privacy Policy. (Access point lists are not available as an option for synchronization tasks.)
- Add Italian translation

#### 2020-10-28 Ver2.38
- Update privacy policy

#### 2020-10-23 Ver2.37
- Add Chinese language to help.

#### 2020-10-17 Ver2.36
- Added the option to start the schedule immediately on the Schedule tab
- Add Chinese translation
#### 2020-09-21 Ver2.35
- Fixed a bug that prevented the task list from being saved in the schedule.
#### 2020-09-12 Ver2.34
- Fixed a bug where Backround location permissions are repeatedly displayed on Android 10
#### 2020-09-12 Ver2.33
- Resolved a bug that caused location permissions to appear repeatedly
#### 2020-09-09 Ver2.32
- Disabled the "," in the task and schedule names.
- Android 11 support
#### 2020-06-26 Ver2.31
- Fixed a bug in SMBv2/3 crash the app when connecting to some SMB servers. (To connect to a server that can only be used with SMB3.1, use SMBv2/3(2.14) to connect to Please note that SMBv2/3 (2.12) does not allow you to connect.)
- Fixed a bug in which crash app when trying to import a sync task list.
#### 2020-06-20 Ver2.30
- Improve message tab.
- Avoidance of forced termination on some models
#### 2020-06-07 Ver2.29
- Fixed a bug that prevented the app's settings menu from opening on Android 5.0/5.1.
- Fixed a bug that caused no response when editing a synchronization task.
#### 2020-06-02 Ver2.28
- Added synchronous task notification function to external application (QUERY/REPLY).
- Added French and Russian.
#### 2020-04-22 Ver2.27
- Prevent app crash for some models.
#### 2020-04-16 Ver2.26
- Prevent  ANR.
- Fixed a bug that prevented SMB host name resolution.
#### 2020-04-15 Ver2.25
- Added “jpeg” and “jpe” file extensions to the archive.
- Made "Conn to AP" in WiFi option to not need lotion permissions.
- The length of the file name will be an error if it is longer than 255 bytes.
- Added an option to ignore the time difference between Daylight Saving Time and Standard Time.
- Added jcifs-ng 2.1.4 to the SMB protocol.
#### 2019-11-28 Ver2.24
- Prevent app crash
#### 2019-09-23 Ver2.23
- Resolves a bug that prevents scheduled startup from overwriting charging options
#### 2019-09-07 Ver2.22
- Fixed a bug that cannot create a directory in Archive to SMB.
- The timestamp was used for temporary file names when writing to SMB.
#### 2019-09-04 Ver2.21
- Adjusted the text color in Light Theme.
#### 2019-08-16 Ver2.20
- Prevent app crash when show log file.
#### 2019-08-16 Ver2.19
- The sample task was not created.
- Don't save automatically when sync task is empty.
- Prevent forced termination by sending history.
#### 2019-08-15 Ver2.18
- It corresponds when crashing when writing to SDCARD.
- Added the option to change "Sync start only while charging" in scheduling.
- Changed the directory where the log is stored to an app-specific directory.
#### 2019-07-09 Ver2.17
- Prevent app crash when import of the sync task list.
#### 2019-07-08 Ver2.13-2.16
- Prevent app crah
#### 2019-07-08 Ver2.12
- The synchronization task list is automatically saved when the synchronization task list is changed, the schedule is changed, and the synchronization task list is imported. However, if you reinstall or initialize the app, you will not be able to recover your account name or password from the previous autosave file.
#### 2019-06-27 Ver2.11
- Fixed a bug that directory can not be created in SMB directory selection screen.
- Fixed a bug that schedule settings can not be restored.
#### 2019-06-22 Ver2.10
- Fixed a bug that cannot be renamed the schedule
#### 2019-06-21 Ver2.09
- Improve schedule notation.
#### 2019-06-21 Ver2.08
- Added schedule editing and settings tab.
- The icon at the top of the screen enables or disables the scheduling function.
- Added the option to delete the master directory when it is empty during the move.
#### 2019-06-15 Ver2.07
- Add black theme
#### 2019-06-09 Ver2.06
- The contents of the Message tab are saved up to 5000 lines.
- Improved message display when synchronous start request and accepted synchronization start request were not performed.
#### 2019-05-29 Ver2.05
- Supported when IPV6 addresses are returned in name resolution from DNS
- Resolves a bug that kills when unable to get the date and time from EXIF in the archive.
#### 2019-05-15 Ver2.04
- Solved the bug that the file name is the same but the error is caused by the difference between upper and lower case in synchronization with SDCARD
#### 2019-05-14 Ver2.03
- Avoid NPE in SMB folder editing
#### 2019-05-13 Ver2.02
- Avoid crash in sync task editing in Android 5.0
- Avoid crash on SMB Server Scan
- Change of SDCARD recognition method
#### 2019-05-11 Ver2.01
- Added option to allow sync with global IP address
#### 2019-05-02 Ver2.00
- Enabled to sync on networks other than WiFi.
- Fix a bug that shows errors on the schedule.
- The operation of the schedule list (deletion, addition, renaming, editing, etc.) is immediately reflected.
#### 2019-04-27 Ver1.99
- Resolves a bug that SDCARD cannot recognize.
#### 2019-04-19 Ver1.98
- Improved the Write permission of SDCARD/USB media in Android 7 or higher.
#### 2019-04-18 Ver1.97
- Added option for SMB V2/3 (Please enable “Use SMB2 Negotiation” when SMB V2/3 can not be used on SMB2.02 devices)
- In the schedule function, when the app does not turn on Wi-Fi, the Wi-Fi is not turned off.
#### 2019-04-10 Ver1.96
- Ver1.80 not crash when migrating from earlier versions to the latest version.
#### 2019-04-09 Ver1.95
- SMB Server Search results enabled to display the SMB version. 
- Improved SDCARD/USB media-related notation.
- The SMB protocol default when adding the synchronization task was changed from SMBv1 to SMBv2/3.
#### 2019-03-29 Ver1.94
- Support SMB V3 protocol (SMB V2/3 (2.12))
#### 2019-03-23 Ver1.93
- Adding and correcting error messages
#### 2019-03-16 Ver1.92
- NPE Prevention in renaming schedules
#### 2019-03-04 Ver1.91
- Fixed a bug that failed to break when writing a problem or question in system information.
#### 2019-03-03 Ver1.90
- Fixed a bug that would cause an error when a ZIP file does not exist in sync to zip.
#### 2019-02-23 Ver1.89
- A warning message is displayed when the location service is disabled in ANDROID9.
#### 2019-02-23 Ver1.88
- ANR avoidance.
- A warning message is displayed when the shooting date and time cannot be retrieved with Copy/move.
#### 2019-02-18 Ver1.87
- Added jcifs-ng 2.11 (SMBv2 (2.11)) to the SMB protocol
- Copy/Move allows you to use JPG/GIF/MP4/MOV shooting date in target directory name.
- Improved SDCARD processing speed
#### 2019-01-31 Ver1.86
- ANR avoidance in file selection.
#### 2019-01-26 Ver1.85
- Fixed bug that SDCARD-TO-USB and USB-TO-SDCARD are not processed
#### 2019-01-18 Ver1.84
- To include SMB v1 logs in the app log
#### 2019-01-14 Ver1.83
- NPE prevention in file/directory selection.
#### 2019-01-13 Ver1.82
- Made SDcard available for task Export/task Import
#### 2019-01-10 Ver1.81
- ANR avoidance in file selection.
- Fixed a problem that SDCARD can not be used on Android 9.
#### 2019-01-03 Ver1.80
- English notation was improved.
- Allow password authentication to access some features by settings.
- Hide SMB folder's account name and password by settings.
- Fixed a bug where the retention period was ignored in the archive.
#### 2018-11-04 Ver1.79
- Added option to skip directories/files where unusable characters are used.
#### 2018-10-26 Ver1.78
- Fixed bug that directory selection of internal storage can not be done by long press
- Added error message when synchronizing from internal storage to SMB/SDCARD/USB media when using characters that can not be used with file name or directory name
- The Wi-Fi IP address can be specified as the condition that sync can be started in the sync task. (Please use if you do not want to enable location service to get SSID on Android 9)
#### 2018-09-07 Ver1.77
- Changed not to display archive options in master folder
#### 2018-09-04 Ver1.76
- Changed to show options in archive in target folder
- Added option to use Android standard function for handling messages on message tab(Settings-Other-Text processing of sync messages)
  If you see symptoms such as no message display or forced termination on the Sync Message tab, please enable it.
#### 2018-09-01 Ver1.75
- Changed the name from "external SDCARD" to "SDCARD"
- Fix minor bug
#### 2018-08-30 Ver1.74
- Resolved a bug that could corrupt ZIP files with mirror to ZIP file
#### 2018-08-30 Ver1.73
- Add 1 to 4 minutes to the interval schedule
- Fix minor bug
#### 2018-08-27 Ver1.72
- Fix minor bug
#### 2018-08-26 Ver1.71
- A confirmation message is displayed when shooting date and time can not be acquired from Exif at the time of archiving.
- Add the monthly schedule
#### 2018-08-15 Ver1.70
- Resolved bug that file is not selected/excluded by file filter
#### 2018-08-13 Ver1.69
- Fixed a bug that crashes with synchronization from Android/data/ on SDCARD.
- Added a sync button on Sync task list
- An option for dealing with the delay of synchronization during sleep in some models was added to the setting.
- To support USB media.
#### 2018-07-11 Ver1.68
- Fixed a bug that can not be selected by pressing and holding on the directory selection screen
#### 2018-07-11 Ver1.67
- Fixed a bug that can not be selected by pressing long on the directory selection filter screen.
#### 2018-07-04 Ver1.66
- Resolved a bug that crashes with capturing date and time in archive.
- Resolved bug that SDCARD can not be selected.
#### 2018-06-28　Ver1.65
- Delete " Do not change the file contents until the copy/move is complete" of the synchronous option and always make " Do not change the file contents until the copy/move is complete".
- Improved the recognition method of SDCARD so that UUID can use 0000-0000.
- When the file information of SDCARD is different from the media store, a warning message is displayed and synchronized using the information of the file.
#### 2018-06-22　Ver1.64
- I set Coarse location permissions only for Android 8.1 so that I will not request permission every time.
- It corresponds to the fact that the last update time of the file can not be changed with some models
#### 2018-06-13　Ver1.63
- Change of SDCARD recognition method
- Enabled SDCARD selection from the menu
- It was to avoid an error in sync during sleep in Android8.1
#### 2018-06-12　Ver1.62
- Added send button to system information.
#### 2018-06-10　Ver1.61
- Fixed a bug that crashes when importing encrypted task list
#### 2018-06-08　Ver1.60
- Fixed a bug where notification messages are always displayed
#### 2018-06-07　Ver1.57-1.59
- Fixed a bug that overwrites incorrectly if you repeatedly sync to SDCARD
- Fixed a bug that may fail to write to SDCARD on Android 7 and higher
#### 2018-06-05　Ver1.56
- Fixed a bug that external SDCARD could not be selected with some models.
#### 2018-06-05　Ver1.55
- Fixed loop bug when target folder is ZIP and SDCARD selection when saving to SDCARD
#### 2018-06-03　Ver1.54
- Avoid bug that display is disturbed on some tablets
- Vibration and sound uninterrupted when the end of the sync
#### 2018-05-20　Ver1.53
- The last update time of the file written to SDCARD on Android 7.0 and later is made the same as the copy source file
- Fixed a bug where only the first scheduled items on multiple schedules are executed.
#### 2018-05-14　Ver1.52
- Prevent application crash when sync task edit.
#### 2018-05-13　Ver1.50-1.51
- Avoid the application crash when the sync start time
#### 2018-05-13　Ver1.49
- Prevent App crash
#### 2018-05-12　Ver1.47-1.48
- Add archive to sync type.
  You can move photos and video files from master to target, you can archive more than 7 days or more than 1 year since the shooting date and time.
- Added option to judge as difference file only when master file size is large. (The special option "Only when the file size of the master is large is determined as the difference file")
- Fixed a bug that only one run when multiple schedules were executed at the same time. (However, you can not schedule the same synchronization task at the same time)
#### 2018-05-02　Ver1.45-1.46
- To prevent app crash
- Added mp4 to "Sync video files"
- When the file is copied to the internal storage, it is set to the same last update time as the copy source
#### 2018-04-16　Ver1.44
- When "[" or "]" is included in the directory name or file name, it will not disappear when restarting the application
- Added option to restore SMB1 setting value to initial value in setting
#### 2018-04-12　Ver1.43
- When the last update time of the file copied/moved to the SMB folder can not be made the same as the copy source, a message is displayed so that it is not set as an error.
- Change the default value of SMB1 setting
  	lmCompatibility 		-> 3
  	Use extended security 	-> true
#### 2018-04-10　Ver1.42
- Fixed a bug that SDCARD directory can not be selected
#### 2018-04-09　Ver1.40-41
- Fixed a bug that caused Access Denied to sync to SMB server
- Fixed a bug that application crash
#### 2018-04-08　Ver1.39
- Avoid "TreeId is NULL" error on SMB2
- Resolved a bug that SMB-SMB move fails
- Resolved a bug that crashes rarely with SMB sync folder editing
- It is possible to set not to create details of synchronization history (Settings -> Sync -> Sync History)
#### 2018-04-02　Ver1.38
- A directory can be created by directory selection from SMB server
- Fixed a bug that anonymous login failure
#### 2018-04-01　Ver1.36-37
- Correction of SMB Protocol processing
#### 2018-03-31　Ver1.35
- Separate processing at SMB1 and SMB2
#### 2018-03-31　Ver1.34
- Change SMB protocol to "SMB 1 only" when creating new sync task
#### 2018-03-30　Ver1.33
- We responded to problems with SMB1 equipment accompanying SMB2 support.
#### 2018-03-28　Ver1.32
- Fixed a bug that SMB - SDCARD/Internal storage deletes files with mirror
#### 2018-03-27　Ver1.31
- SMB2(2.01) enabled.
#### 2018-03-20　Ver1.29-1.30
- To allow for multiple schedule
- “Sync is started only during charging" is added to the sync task option
- Prevented excluded directories from being created as empty directories
- Improved deletion of unusable characters by directory and file name
#### 2018-02-28　Ver1.28
- To the exclusion of higher-level directory in the directory filters, and lower directory If you select a sub-directory will be synchronized. (It becomes valid when checking "Use expanded directory selection/exclusion filter" in the synchronization task) 
- Add to sync task the option to display a warning message to the sync task at the start when you are not connected to the SSID of the Wi-Fi access point list to start the next task.
#### 2018-02-22　Ver1.27
- Fixed a bug that will be copied every time in synchronization from SMB to SDCARD
- Added keyword of year/month/day to target directory or ZIP file name. Keywords are replaced by the year, month and date at the start of sync task.
  -%YEAR% the year (such as 2018 or 2019)
  -%MONTH% month (01 to 12)
  -%DAY% day (01 to 31)
  -%DAY-OF-YEAR% days since the beginning of the year (001 to 　365, leap year 366)
#### 2018-02-16　Ver1.26
- Added synchronization between SMB servers.
- Shortcut is to be created in the Android 8.
- 0 byte files when synchronized to an external SDCARD was not allowed to be created in test mode.
#### 2016-11-07　Ver1.25
- In synchronization with the SMB server, eliminating the problem of an error during synchronization.
- Change the internal processing of external SDCARD access.
#### 2016-10-24　Ver1.24
- Using the DNS when a failed SMB name resolution has to be the name resolution. However, the need is that the name is specified by FQDN as such win-srv.test.domain.com.
#### 2016-09-30　Ver1.23
- To be able to set the volume of the notification sound.
#### 2016-09-26　Ver1.22
- Resolve the bug that option is not saved in the "To use the last update time of the file to the difference judgment" and "To use the file size to the difference judgment"
#### 2016-09-23　Ver1.21
- Added an option to show a notification message.
#### 2016-09-01　Ver1.20
- Resolve the bug that empty directory is not sync with the sync to external SDCARD.
#### 2016-08-21　Ver1.19
- Allowed to prevent the overwriting of files in the sync options.
#### 2016-08-16　Ver1.18
- Disable FastScroll in the Directory selection screen
#### 2016-08-02　Ver1.17
- Resolve the bug that duplicated in synchronization start from the external application
- Reduction of the deletion time in the mirror to the ZIP file
#### 2016-06-26　Ver1.16
- Resolve the bug that can not be external SD card recognition
#### 2016-06-26　Ver1.15
- Added USB folder path(/storage/emulated/UsbDriveA, /storage/emulated/UsbDriveB)
#### 2016-06-25　Ver1.14
- Resolve an empty directory is not reflected bug when synchronized with the ZIP
#### 2016-06-23　Ver1.13 
- Add sync, copy or move from the internal storage to the ZIP file (internal storage or external SDCARD)
#### 2016-06-17　Ver1.12 
- Add path /Removable/MicroSD for SDCARD
#### 2016-06-17　Ver1.11 
- Resolve the bug to be killed when the application is started with the intent at the time of the screen off.
#### 2016-06-13　Ver1.10 
- Resolve the bug of name resolution of the SMB host name.
- Notification messages display optimization.
#### 2016-05-29　Ver1.09 
- It was strictly a test to access the same directory on the master and the target.
- Resolve the bug to be killed in the deletion of the WiFi access point.
#### 2016-05-25　Ver1.08 
- Add help to the synchronization task add and edit screen
- Change the UI of the file selection, 1. Selection is to tap the name of the file or directory name. 2. display of the sub-directory is to tap the left edge of the icon. 3. abolish the long tap.
- Added an option to fix the orientation of the screen to portrait
#### 2016-05-16　Ver1.07 
- Change the Help file to link to the Google Drive has a small file size.
- Resolve the bug that can not be added in the file and directory filter.
- Improve the UI of the file filter
#### 2016-05-07　Ver1.06 
- Add external SDCARD path (/storage/MicroSD, /storage/extSdCard and /mnt/extSdCard)
- Resolve not copied bug in synchronization to an external SD card
#### 2016-05-03　Ver1.05 
- It was to be able to specify a wireless LAN access point to allow sync with the sync options.
- Improved the user interface for the selection of external SDCARD.
#### 2016-04-16　Ver1.04 
- To be able to import a task list from exported SMBSync profile.
#### 2016-04-14　Ver1.03 
- Improve performance
- Resolve the bug that settings value is not restored during import task list.
#### 2016-04-10　Ver1.02 
- Fixed a bug that not be moved a file from SDCARD.
- Resolve the bug can not be mirrored in the some of the directory.
#### 2016-04-09 Ver1.00
- Initial release