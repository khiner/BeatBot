#!/bin/sh
# BeatBot/build.sh
# Compiles freeverb for Android

INSTALL_DIR="`pwd`/jni/freeverb"
SRC_DIR="`pwd`/../freeverb3-2.6.4/"

cd $SRC_DIR

export PATH="$HOME/android-ndk-r6/toolchains/arm-linux-androideabi-4.4.3/prebuilt/darwin-x86/bin/:$PATH"
export SYS_ROOT="$HOME/android-ndk-r6/platforms/android-9/arch-arm/"
export CC="arm-linux-androideabi-gcc --sysroot=$SYS_ROOT"
export LD="arm-linux-androideabi-ld"
export AR="arm-linux-androideabi-ar"
export RANLIB="arm-linux-androideabi-ranlib"
export STRIP="arm-linux-androideabi-strip"

mkdir -p $INSTALL_DIR
./configure --host=arm-eabi --build=i386-apple-darwin9.8.0 --prefix=$INSTALL_DIR LIBS="-lc -lgcc"

make
make install

exit 0
