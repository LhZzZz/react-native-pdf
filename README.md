# react-native-qmaple-pdf

## Getting started

`$ npm install react-native-qmaple-pdf --save`

### Mostly automatic installation

`$ react-native link react-native-qmaple-pdf`

## Install

### Ios

Step 1 : add below in your PodFile and run "cd ios && pod install" in your Project
```ios

pod 'react-native-pdf', :path => '../node_modules/react-native-qmaple-pdf'
```
Step 2 : add below in AppDelegate.m 

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
### Android

maybe you should run "react-native link"


## Usage
```javascript
import Pdf from 'react-native-pdf';

// TODO: What to do with the module?
Pdf;
```
