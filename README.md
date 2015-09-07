##在安卓上运行Socket.io服务端
##run socket.io servier in android
----


1. 简介
为什么会有这样一个奇葩的项目呢？这个项目是另一个项目的独立版本， 这是一个**画板的服务端+客户端**。运行项目后可以在后台启动socket.io的服务。并且启动浏览器打开项目的主页。

关于其中的画板我将会在另一个开源的项目中进行详细的说明這里就不再说明了


2.使用方法
这个项目使用的IDE为**idea14**我也转换了一个eclipse版本。所有无论你使用的那个ide都可以直接clone或下载zip 包添加到项目中即可，选择一个android4.0以上的手机或模拟器运行项目即可。若需要修改，请在基本了解项目的流程后进行。（主要是我不怎么在线，所有可能不能及时解答）。


3. 相关问题
* 该项目使用了开源的**[socketio-netty](https://github.com/178620086/socketio-netty)**原生java实现socket.io 并在其基础上进行了一些修改（将comslog换成了andorid自带的log）
* 这是一个简单的android项目并没有界面，只有一个通知栏，来控制后台的服务运行
* socket.io部分全是是java代码，其他部分如网页（直接存在assets/static 目录下，在这里可以修改网页部分）








