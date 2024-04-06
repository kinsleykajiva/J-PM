# Java Process Manager (JPM)


**Version:** 0.1

Welcome and thank you for using JPM!

JPM is an application process manager for Linux and Windows Operating Systems, designed to help your applications run in the background, report errors, and handle crashes. Our goal is to simplify application administration as much as possible.

## Supported Application Environments
- Java Jar apps
- Node JS apps

More support for other apps is coming soon!

## Features

- **Multi-platform Support**: JPM seamlessly works across Linux and Windows environments, offering a consistent experience regardless of your operating system.
- **Background Process Management**: Effortlessly run your applications in the background, freeing up your terminal or command prompt for other tasks.
- **Error Reporting**: Stay informed about application errors and crashes with detailed reports, enabling you to troubleshoot issues effectively.
- **Simple Administration**: JPM prioritizes ease of use, providing a straightforward interface for managing your applications.

### What's Required?
Ensure that Java and Node.js are installed globally on your system.

For development, use GraalVM-Java SDK version 22 and above.

#### How to install?
just download the jpm app or binary app found in the [executable-app](/executable-app) folder in this repository.

Make jpm as part of the global `PATH` or add the folder where jpm file is found to `PATH`

Please note the first install of running `jpm install` run as adminstrator . 

Start or run an Application as a process
```shell
jpm start app.js 
```
or
```shell
jpm start app.jar
```
[Screenshot](imgs/1.png)

<img src="imgs/1.png">


You can call jpm from anywhere on your machine or within a folder, provided it is defined in the PATH

To get list of running apps/process that jpm is managing :

```shell
jpm ls
```

[Screenshot](imgs/2.png)

<img src="imgs/2.png">

To stop application :
```shell
jpm stop <app_name|id|'all'>
```

This will remove the app from the list of apps.

How the app works as much.

JPM does not directly run the application; instead, it utilizes a backend service application to maintain them. Running `jpm install` installs this server, which operates on port 8080.

Both apps are binary executables, and the server will always be running after installation.





Built with the following for windows:

```shell
Java version: 22+36, vendor version: GraalVM CE 22+36.1
Graal compiler: optimization level: 2, target machine: x86-64-v3
C compiler: cl.exe (microsoft, x64, 19.39.33523)
Garbage collector: Serial GC
```
Please note that this app is still in development, and we are exploring its viability as a production-ready product.


## Contributions
JPM is an ongoing project, and contributions from the community are welcome. If you have ideas for improvements or new features, please feel free to submit a pull request or open an issue on the project's GitHub repository

