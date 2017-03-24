蓝牙对战模块，其中包含服务端和客户端，中间使用了eventbus，使用过程如下：
0：客户端和服务端分别启动各自服务，蓝牙操作大部分在服务中操作
1：服务端开启蓝牙，处于可发现状态，等待客户端扫描连接。
2：客户端扫描周围设备，选择服务端MAC，进行连接，
      其中客户端的服务需要通过eventbus传入服务器蓝牙设备。
3：服务端和客户端会 进行配对，在配对成功后就可以相互传递信息。
4：蓝牙通讯线程是BluetoothCommunThread，其中方法有读写操作，
     其中在读取完后会将读取完的信息通过eventbus推送出去，需要在交互页面使用eventbus监听获取到的数据。
5：BluetoothExchangeMessages是交互的信息，其中的BLUETOOTHSERVICESERVICE_DATA_WRITE_TO_SERVICE可以是任何自定义的类