## 1.功能
SMBSync2是一款通过无线局域网使用SMB1、SMB2或SMB3协议在Android设备内部存储、SDCARD和PC/NAS之间进行文件同步的工具。 同步从主站到目标站是单向的，可以进行镜像、移动、复制和存档。 (内部存储、SDCARD、SMB和ZIP的组合也是可以的。)
定期同步可以由SMBSync2的调度功能或外部应用程序（如Tasker或AutoMagic）启动。
- 镜像

  将主站侧的目录和文件差额拷贝（*1）到目标站侧，复制完成后，从目标站侧删除主站侧不存在的文件和目录。

- 移动

  将主控端目录和文件差额复制到目标端，删除主控端被复制到目标端的文件。 但是，如果主文件和目标文件的名称、文件大小和修改日期相同，则主文件会被删除，而不会被复制。

- 复制

  差分将主目录中的文件复制到目标目录中。

- 封存

  如果主目录中的照片和视频是在执行存档前7天或30天之前拍摄的，则将其移动到目标目录中。 但是，你不能使用ZIP来瞄准。

*1 当满足以下三个条件中的任何一个时，该文件将被判定为差异文件，并将被复制或移动。 但是，文件大小和最后修改时间可以被同步任务的选项忽略。

1. 该文件不存在。
2. 不同的文件大小。
3. 最后修改的日期和时间相差3秒以上（秒数可以通过同步任务中的选项更改）。
## 2.常问问题
请参考下面的PDF链接。  
https://drive.google.com/file/d/1bld5J43139dflVwgNBJLlL3BTLAu199N/view?usp=sharing

## 3.正在使用的图书馆
- [jcifs-ng ClientLibrary](https://github.com/AgNO3/jcifs-ng)
- [jcifs-ng ClientLibrary](https://github.com/AgNO3/jcifs-ng)
- [jcifs-1.3.17](https://jcifs.samba.org/)
- [Zip4J 1.3.2](https://mvnrepository.com/artifact/net.lingala.zip4j/zip4j/1.3.2)
- [Metadata-extractor](https://github.com/drewnoakes/metadata-extractor)
## 4.详细信息
请参考下面的PDF链接。  
https://drive.google.com/file/d/0B77t0XpnNT7OSzBzcV9SemEwbkE/view?usp=sharing