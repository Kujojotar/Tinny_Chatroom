# TinnyChatRoom

### 涉及技术

- Netty 4
- WebSocket + HTTP
- TLS1.2
- Tomcat 8

## 整体说明

整个系统运行两个服务器，Tomcat服务器负责处理常见的一般业务，包括用户登录，个人信息管理，头像更换等业务。WebSocket服务器负责处理实时性要求高的功能，比如用户聊天信息的转发推送，小容量文件的实时传输。

### 项目主要模块结构

- authentication：负责用户登录模块，包含用户登录逻辑的实现
- codec：自定义HTTP与WebSocket协议编解码器以及相关结构的定义
- config：项目全局配置信息
- controller+dao+entity：一般非实时性需求业务的实现
- handler：项目中实现的自定义ChanenlHandler，主要用于处理WebSocket握手以及TLS1.2握手

## 项目效果预览

![login](.\images\login.jpg)

<center>用户登录界面</center>

![board](.\images\board.jpg)

<center>用户好友主界面</center>

![sendTextAndFile](.\images\sendTextAndFile.jpg)

<center>用户kinsn向用户hello发送文字与图片</center>

<img src=".\images\receiveAndResponse.jpg" alt="receiveAndResponse" style="zoom:150%;" />

<center>用户hello收到消息并向kinsn回复图片</center>

![receiveResponse](.\images\receiveResponse.jpg)

<center>kinsn接收用户hello回复图片</center>