# Biomodule 蓝牙SDK 

- [Biomodule 蓝牙SDK](#biomodule-%e8%93%9d%e7%89%99sdk)
  - [简介](#%e7%ae%80%e4%bb%8b)
  - [Demo](#demo)
  - [开发组件](#%e5%bc%80%e5%8f%91%e7%bb%84%e4%bb%b6)

## 简介

本 SDK 包含回车生物电采集模块的蓝牙连接和生物电采集控制。通过集成BLE基础SDK可以实现和我们的采集模块连接，并控制其进行数据的采集和停止等指令，
集成BLE UI SDK可以方便为你的app搭建设备管理界面。

## Demo
该项目提供的demo集成上述两个SDK，具体可参考[工程Demo](demo/README.md)

另外我们还提供了一个心流演示应用，其集成了蓝牙SDK，蓝牙设备管理SDK，情感云SDK等功能，详细请查看[心流演示应用](https://github.com/Entertech/Enter-Affective-Cloud-Demo-Android)

## 开发组件

工程包含以下组件：

- [1.工程Demo](demo/README.md)
- [2.基础蓝牙模块SDK](https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/tree/master/ble)
- [3.设备管理UISDK](https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/tree/master/bleuisdk)

<img src="https://github.com/EnterTech/Flowtime-BLE-SDK-Android/blob/master/docimage/flowtimeble_project.jpg" width="40%">

