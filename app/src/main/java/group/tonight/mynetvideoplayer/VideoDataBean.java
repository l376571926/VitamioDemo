package group.tonight.mynetvideoplayer;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoDataBean implements Parcelable {
    private String title;
    private String size;
    private String url;

    protected VideoDataBean(Parcel in) {
        title = in.readString();
        size = in.readString();
        url = in.readString();
    }

    public static final Creator<VideoDataBean> CREATOR = new Creator<VideoDataBean>() {
        @Override
        public VideoDataBean createFromParcel(Parcel in) {
            return new VideoDataBean(in);
        }

        @Override
        public VideoDataBean[] newArray(int size) {
            return new VideoDataBean[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(size);
        parcel.writeString(url);
    }
}
