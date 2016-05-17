package br.com.fiap.pizza;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by VyMajoriss on 5/11/2016.
 */
@JsonAutoDetect
public class Details implements Serializable {

    public String name;
    public LatLng center;
    public String avatar;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getCenter() {
        return center;
    }

    public void setCenter(LatLng center) {
        this.center = center;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String email;
    public int color;


    public Details(int color, String name, LatLng center, String avatar, String email) {
        this.color = color;
        this.name = name;
        this.center = center;
        this.avatar = avatar;
        this.email = email;
    }

    public Details() {

    }


}
