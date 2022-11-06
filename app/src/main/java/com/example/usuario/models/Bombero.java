package com.example.usuario.models;

public class Bombero {
    String id;
    String cedula;
    String name;
    String ape;
    String address;
    String sexo;
    String email;
    String ciudad;
    String image;
    String dateborn;
    String contrato;
    String token;
    private boolean online;

    public Bombero() {
    }

    public Bombero(String id, String cedula, String name, String ape, String address, String sexo, String email, String ciudad, String image, String dateborn, String contrato, String token, boolean online) {
        this.id = id;
        this.cedula = cedula;
        this.name = name;
        this.ape = ape;
        this.address = address;
        this.sexo = sexo;
        this.email = email;
        this.ciudad = ciudad;
        this.image = image;
        this.dateborn = dateborn;
        this.contrato = contrato;
        this.token = token;
        this.online = online;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApe() {
        return ape;
    }

    public void setApe(String ape) {
        this.ape = ape;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDateborn() {
        return dateborn;
    }

    public void setDateborn(String dateborn) {
        this.dateborn = dateborn;
    }

    public String getContrato() {
        return contrato;
    }

    public void setContrato(String contrato) {
        this.contrato = contrato;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
