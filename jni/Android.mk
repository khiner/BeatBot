# Copyright (C) 2010 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)
ROOT_PATH := $(LOCAL_PATH)

include $(call all-subdir-makefiles)
include $(CLEAR_VARS)

LOCAL_PATH = $(ROOT_PATH)
FILE_LIST := $(wildcard $(LOCAL_PATH)/*.c)
FILE_LIST += $(wildcard $(LOCAL_PATH)/effects/*.c)
FILE_LIST += $(wildcard $(LOCAL_PATH)/generators/*.c)

APP_ABI := armeabi armeabi-v7a 
LOCAL_MODULE := nativeaudio
LOCAL_SHARED_LIBRARIES := sndfile
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog -landroid -lOpenSLES
LOCAL_CFLAGS := -ffast-math -O3 -funroll-loops -Wall -Wextra
LOCAL_ARM_MODE := arm
LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)

include $(BUILD_SHARED_LIBRARY)
