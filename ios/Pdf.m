#import "Pdf.h"
#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTConvert.h>
#import <CoreGraphics/CoreGraphics.h>

@implementation Pdf

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE()

- (instancetype)init {
  NSLog(@"init------");
  self = [super init];
  [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(receivePDF:) name:@"pdf" object:nil];
//  [self startObserving];
  return self;
}

RCT_EXPORT_METHOD(convertPdf:(NSString*) Path findEvents:(RCTResponseSenderBlock)callback){
    NSLog(@"------------%@",Path);
    NSURL *pdfURL = [NSURL URLWithString:Path];
    NSMutableArray* imgArr = [self convertAndSave:pdfURL];
    NSLog(@"我是打开pdf再启动app的");
    callback(@[[NSNull null], imgArr]);
}


- (void)receivePDF:(NSNotification *) noti
{
    NSURL* pdfUrl = noti.userInfo[@"Path"];
    NSString* pdfPath = [pdfUrl absoluteString];
//    NSMutableArray* imgArr = [self convertAndSave:pdfUrl];
//    NSLog(@"我是启动后打开pdf的");
    [self.bridge.eventDispatcher sendAppEventWithName:@"pdf" body:@{@"path":pdfPath}];
}

- (NSMutableArray*)convertAndSave:(NSURL*) Path{
    
    CFURLRef ref = (__bridge CFURLRef)Path;
    CGPDFDocumentRef pdf = CGPDFDocumentCreateWithURL(ref);
    CFRelease(ref);
    int numberOfPages = (int)CGPDFDocumentGetNumberOfPages(pdf);
    NSLog(@"页数:%d",numberOfPages);
    NSMutableArray *PathArray = [[NSMutableArray alloc]init] ;
    for (int index = 1; index <= numberOfPages; index++) {
        CGPDFPageRef page = CGPDFDocumentGetPage(pdf, index);
        NSDictionary* imageInfo = [self convertToImgsAndSave:page PageIndex:index];
        if (imageInfo!=nil) {
            [PathArray addObject:imageInfo];
        }
        NSLog(@"path%d:%@",index,imageInfo);
    }
    NSLog(@"数组: %lu",(unsigned long)PathArray.count);
    return PathArray;
}

//转为图片
- (NSDictionary*)convertToImgsAndSave:(CGPDFPageRef) page PageIndex:(NSInteger)index{
    CGRect pageRect = CGPDFPageGetBoxRect(page, kCGPDFMediaBox);
    pageRect.origin = CGPointZero;
    pageRect.size.height = pageRect.size.height*2;
    pageRect.size.width = pageRect.size.width*2;
    
    UIGraphicsBeginImageContext(pageRect.size);
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSetRGBFillColor(context,1.0,1.0,1.0,1.0);
    CGContextFillRect(context,pageRect);
    CGContextSaveGState(context);
    
    CGContextTranslateCTM(context, -pageRect.size.width/2, pageRect.size.height*1.5);
    CGContextScaleCTM(context,2, -2);
    CGContextSetInterpolationQuality(context, kCGInterpolationHigh);
    CGContextSetRenderingIntent(context, kCGRenderingIntentDefault);
    CGContextConcatCTM(context, CGPDFPageGetDrawingTransform(page, kCGPDFMediaBox, pageRect,0,true));
    CGContextDrawPDFPage(context,page);
    CGContextRestoreGState(context);
    UIImage *pdfImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    NSDictionary* imageInfo =  [self save:pdfImage imgIndex:index];
//    NSString* imgW = [NSString stringWithFormat:@"%zu", CGImageGetWidth(pdfImage.CGImage)];
//    NSString* imgH = [NSString stringWithFormat:@"%zu", CGImageGetHeight(pdfImage.CGImage)];
//
//    NSData * imageData = UIImagePNGRepresentation(pdfImage);
//    NSUInteger* imageSize = [imageData length]/1024;
//    NSLog(@"图片:%@---%@",imgH,imgW);
//    return path;
//    return @{@"path":path,
//             @"imageWidth":imgW,
//             @"imageHeight":imgH
//             };
    return imageInfo;
}

//保存图片
- (NSDictionary*) save:(UIImage*)img imgIndex:(NSInteger)index {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *ImgName = [NSString stringWithFormat:@"img_%ld",(long)index];//图片名
    NSString* filePath = [[paths objectAtIndex:0]stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.jpg", ImgName]];//存储路径
    NSData *imageData = UIImageJPEGRepresentation(img, 0.6);//将图片进行压缩 压缩率0.6
    BOOL result =[imageData writeToFile:filePath atomically:YES];//保存图片
    if(result == YES) {
        NSUInteger imgSize = [imageData length]/1024;//图片大小(kb)
        NSNumber *imgSizeNum = [NSNumber numberWithUnsignedInteger:imgSize];
        UIImage *imagSaved = [UIImage imageWithData:imageData];
        NSString* imgW = [NSString stringWithFormat:@"%zu", CGImageGetWidth(imagSaved.CGImage)];//图片宽度
        NSString* imgH = [NSString stringWithFormat:@"%zu", CGImageGetHeight(imagSaved.CGImage)];//图片高度
        NSLog(@"压缩后保存成功, 大小:%lu, 宽高:%@--%@",(unsigned long)imgSize,imgW,imgH);
        NSDictionary* imgInfo = @{@"path":filePath,@"width":imgW,@"height":imgH,@"size":imgSizeNum};//图片的信息
        return imgInfo;
    }
    NSLog(@"保存失败");
    return nil;
}

//声明要监听的时间名字
- (NSArray *)supportedEvents{
  return @[@"pdf"];
}

//开始监听
//- (void)startObserving {
//  [[NSNotificationCenter defaultCenter] addObserver:self
//                                           selector:@selector(receiveSalesNotifaction:)
//                                               name:@"pdf"
//                                             object:nil];
//}

@end
