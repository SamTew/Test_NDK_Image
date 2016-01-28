#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>

using namespace std;
using namespace cv;

extern "C" {
    JNIEXPORT void JNICALL Java_com_kensam_test_1ndk_1image_12_MainActivity_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba);
    JNIEXPORT void JNICALL Java_com_kensam_test_1ndk_1image_12_MainActivity_TplMatch(JNIEnv*, jobject, jlong matRGB, jlong matTpl, jlong matResult);

    JNIEXPORT void JNICALL Java_com_kensam_test_1ndk_1image_12_MainActivity_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba)
    {
        Mat& mGr  = *(Mat*)addrGray;
        Mat& mRgb = *(Mat*)addrRgba;
        vector<KeyPoint> v;

        FastFeatureDetector detector(50);
        detector.detect(mGr, v);
        for( unsigned int i = 0; i < v.size(); i++ )
        {
            const KeyPoint& kp = v[i];
            circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
        }
    }

    JNIEXPORT void JNICALL Java_com_kensam_test_1ndk_1image_12_MainActivity_TplMatch(JNIEnv*, jobject, jlong matRGB, jlong matTpl, jlong matResult)
    {
        Mat& src = *(Mat*)matRGB;
        Mat& tpl = *(Mat*)matTpl;
        Mat& result = *(Mat*)matResult;

        /// Do the Matching and Normalize
        matchTemplate(src, tpl, result, CV_TM_SQDIFF_NORMED);
        normalize(result, result, 0, 1, NORM_MINMAX, -1, Mat());

        /// Localizing the best match with minMaxLoc
        double minVal; double maxVal; Point minLoc; Point maxLoc;
        Point matchLoc;

        minMaxLoc(result, &minVal, &maxVal, &minLoc, &maxLoc, Mat());
        matchLoc = minLoc;

        rectangle(result, matchLoc, Point(matchLoc.x + tpl.cols, matchLoc.y + tpl.rows), Scalar::all(0), 2, 8, 0);
    }

}