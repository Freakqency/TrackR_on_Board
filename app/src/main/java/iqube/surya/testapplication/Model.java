package iqube.surya.testapplication;

import io.realm.RealmObject;

public class Model extends RealmObject{
long id;
String latitude;
String longitude;
String date;

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Model{" +
                "id=" + id +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
