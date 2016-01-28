LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#opencv
OPENCVROOT:= D:\Android\library\OpenCV-android-sdk
OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED
include ${OPENCVROOT}\sdk\native\jni\OpenCV.mk

LOCAL_SRC_FILES := D:\Android\Project\Test_NDK_Image_2\app\src\main\jni\main.cpp \
LOCAL_LDLIBS += -llog
LOCAL_MODULE := module

include $(BUILD_SHARED_LIBRARY)