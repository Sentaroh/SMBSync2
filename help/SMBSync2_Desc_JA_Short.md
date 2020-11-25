## 1.機能
SMBSync2はAndroid端末の内部ストレージ、SDCARDとPC/NASの間でSMB1, SMB2またはSMB3プロトコルを使用し無線LAN経由でファイルの同期を行うためのツールです。同期はマスターからターゲットへの一方向で、ミラー、移動、コピー、アーカイブが使用できます。（内部ストレージ、SDCARD、SMB、ZIPの組み合わせが可能です）
SMBSync2のスケジュール機能または外部アプリケーション（TaskerやAutoMagicなど）により定期的に同期を開始する事が可能です。
- ミラー  
  マスター側のディレクトリーとファイルをターゲット側に差分コピー(*1)し、コピー終了後にマスター側に存在しないターゲット側のファイルとディレクトリーを削除する。

- 移動  
  マスター側のディレクトリーとファイルをターゲット側に差分コピーし、コピー終了後にマスター側のファイルを削除する。（ただし、マスターとターゲットに同名でファイルサイズと更新日時が同じファイルはコピーせずマスター側のファイルを削除）

- コピー  
  マスター側のディレクトリーに含まれるファイルをターゲット側に差分コピーする。

- アーカイブ  
  マスター側のディレクトリーに含まれる写真とビデオをアーカイブ実行日時より撮影日時が７日以前や30日以前などの条件でターゲットに移動する。（ただし、ターゲットにZIPは使用できません）

注1: 下記の３条件のうちいずれかが成立した場合に差分ファイルと判定し、コピーや移動を行います。また、同期タスクのオプションでファイルサイズと最終更新時間を無視することができます。
1. ファイルが存在しない
2. ファイルサイズが違う
3. ファイルの最終更新日時が3秒以上違う(秒数は同期タスクのオプションにより変更可能)  
## 2.FAQ  
下記リンクのPDFを参照ください。
https://drive.google.com/file/d/1bld5J43139dflVwgNBJLlL3BTLAu199N/view?usp=sharing 

## 3.使用ライブラリー
- [jcifs-ng ClientLibrary](https://github.com/AgNO3/jcifs-ng)
- [jcifs-1.3.17](https://jcifs.samba.org/)
- [Zip4J 1.3.2](https://mvnrepository.com/artifact/net.lingala.zip4j/zip4j/1.3.2)
- [Metadata-extractor](https://github.com/drewnoakes/metadata-extractor)
## 4.詳細情報
下記リンクのPDFを参照ください。
https://drive.google.com/file/d/0B77t0XpnNT7OSzBzcV9SemEwbkE/view?usp=sharing