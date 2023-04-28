/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */
package com.goodix.gftest.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class Metadata implements Parcelable {
    private int mBioFlag = 0;
    private int mImageQuality = 0;
    private int mValidArea = 0;

    public static final int CHIP_UNSUPPORT_BIO = -2;
    public static final int CHIP_SUPPORT_BIO_DISABLE = -1;
    public static final int CHIP_SUPPORT_BIO_ENABLE_BIO_FAILED = 0;
    public static final int CHIP_SUPPORT_BIO_ENABLE_MATCH_FAILED = 1;

    public Metadata(int bioFlag, int imgQuality, int imgArea) {
        this.mBioFlag = bioFlag;
        this.mImageQuality = imgQuality;
        this.mValidArea = imgArea;
    }

    public Metadata() {
        this.mBioFlag = 0;
        this.mImageQuality = 0;
        this.mValidArea = 0;
    }

    /*
     * CHIP_UNSUPPORT_BIO : means chip not support bio
     * CHIP_SUPPORT_BIO_DISABLE : means chip support but disable bio
     * CHIP_SUPPORT_BIO_ENABLE_BIO_FAILED : means chip support and enable it,
     * failed by bio CHIP_SUPPORT_BIO_ENABLE_MATCH_FAILED : means chip support
     * and enable it, failed by match
     */
    public int getBioFlag() {
        return mBioFlag;
    }

    public int getImageQuality() {
        return mImageQuality;
    }

    public int getValidArea() {
        return mValidArea;
    }

    public void setBioFlag(int flag) {
        this.mBioFlag = flag;
    }

    public void setImageQuality(int imageQuality) {
        this.mImageQuality = imageQuality;
    }

    public void setImageArea(int validArea) {
        this.mValidArea = validArea;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mBioFlag);
        dest.writeInt(mImageQuality);
        dest.writeInt(mValidArea);
    }

    public static final Parcelable.Creator<Metadata> CREATOR = new Creator<Metadata>() {
        public Metadata createFromParcel(Parcel source) {
            Metadata mMetadata = new Metadata(source.readInt(), source.readInt(), source.readInt());
            return mMetadata;
        }

        public Metadata[] newArray(int size) {
            return new Metadata[size];
        }
    };
}