# react-native-qmaple-pdf
Convert each page of a PDF file into images

用于将pdf文件的每一页转化为图片

## Getting started

`$ npm install react-native-qmaple-pdf --save`

### Mostly automatic installation

`$ react-native link react-native-qmaple-pdf`

## Install

### Ios

Step 1 : Add below in your PodFile and run "cd ios && pod install" in your Project
```ios

pod 'react-native-pdf', :path => '../node_modules/react-native-qmaple-pdf'
```
Step 2 : Add below in AppDelegate.m 

```ios
#import <Pdf.h>

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  Pdf * pdfHandler = [[Pdf alloc] init];
  return YES;
}

- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url
  sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
{
  [[NSNotificationCenter defaultCenter] postNotificationName:@"pdf" object:nil userInfo:@{@"Path":url}];
  return [RCTLinkingManager application:application openURL:url
                      sourceApplication:sourceApplication annotation:annotation];
}    
````
Step 3 : Add Document Types and Exported UTIs in your Info.plist in Xcode

![](https://tva1.sinaimg.cn/large/007S8ZIlgy1gfj28iwdkoj311c0n0te9.jpg)



### Android

Step 1 : link it automatically using:
```
 react-native link
```
Step 2 : add below in AndroidMainfest.xml

```android
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    
    
    <intent-filter android:scheme="content"
          tools:ignore="AppLinkUrlError">
          <action android:name="android.intent.action.VIEW"></action>
          <category android:name="android.intent.category.DEFAULT"></category>
          <data android:scheme="file" />
          <data android:scheme="content" />
          <data android:mimeType="application/pdf"></data>
    </intent-filter>
```


## Usage
```
import {NativeModules, AppState} from 'react-native'
const Pdf = NativeModules.Pdf;
const NativeLinking=require("../../node_modules/react-native/Libraries/Linking/NativeLinking").default;

componentWillMount(): void {
      AppState.addEventListener('receivePdf', this._handlePdf)
  }

componentWillUnmount(): void {
  AppState.removeEventListener('receivePdf')
}

_handlerPdf = (nextAppState) => {
    if(nextAppState==='active'){
        NativeLinking.getInitialURL().then(res => {
            Pdf.convertPdf(res,(error, Imgs) => {
                console.warn(Imgs);
            });
        }
    }
    
}
```
### Methods

**convertPdf(pdfPath:String)**

The returned is a array with each image data:

```
[...,{
    path:string, //the image path
    size:number, //the image size (kb)
    width:number, //the image width
    height:number, // the image height
}]
```



