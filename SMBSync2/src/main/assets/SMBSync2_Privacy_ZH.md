## 1.应用程序记录的数据

应用会记录 "同步任务列表"，根据设置，还会记录 "应用活动记录"。<span style="color: red; "><u>此外，除非用户对记录的数据进行了操作，否则应用不会将其发送出去。</u></span>

### 1.1.同步任务列表

该应用会记录执行同步所需的数据。

- 目录名、文件名、SMB服务器主机名、IP地址、端口号、账户名、密码(***1**)
- 保护应用程序启动和设置更改的应用程序密码(***1**)
- 应用设置值

***1** 用系统生成的密码加密并存储在Android Keystore中。

### 1.2.申请活动记录

该应用程序记录了以下数据，用于验证和故障排除。

- 安卓版本、设备制造商、设备名称、设备型号和应用版本。
- 目录名、文件名、文件大小、文件最后修改时间。
- SMB服务器的主机名、IP地址、端口号和账户名。
- 网络接口名称、IP地址和
- 系统设置
- 应用设置值

### 1.3.导出的同步任务列表

应用可以将 "1.1同步任务列表"导出为文件。 导出时可以进行密码保护。

- 目录名、文件名
- SMB服务器的主机名、IP地址、端口号、账户名和密码。
- 应用设置值

### 1.4.从应用程序发送数据

应用程序记录的数据可以通过以下应用程序操作来发送

- 在 "历史"选项卡中，按"分享"按钮。
- 点击系统信息中的 "发送至开发者"按钮。
- 点击日志管理中的 "分享"按钮或"发送给开发者"按钮。

## 2.所需权力

该应用使用以下权限。

### 2.1.照片、媒体和文件

**read the contents of your USB storage**  
**modify or delete the contents of your USB storage**

它用于将文件同步到内部存储和读/写管理文件。

### 2.2.存储

**read the contents of your USB storage**  
**modify or delete the contents of your USB storage**

它用于将文件同步到USB存储设备上，并读取/写入管理文件。

### 2.3.Wi-Fi连接信息

**view Wi-Fi connections**

当您开始同步时，使用此功能检查Wi-Fi状态。

### 2.4.其他

### 2.4.1.view network connections

它用于确保在同步开始时，您已连接到网络。

### 2.4.2.connect and disconnect from Wi-Fi

用于开启和关闭Wi-Fi以实现日程同步。

### 2.4.3.full network access

它是用来通过网络与SMB协议同步的。

### 2.4.4.run at startup

用于执行日程同步。

### 2.4.5.control vibration

用于在同步结束时通知用户。

### 2.4.6.prevent device from sleeping

用于从日程表或外部应用程序启动同步。

### 2.4.7.install shortcuts

用于添加同步启动快捷方式到桌面。
