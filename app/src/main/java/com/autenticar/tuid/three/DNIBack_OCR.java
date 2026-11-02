package com.autenticar.tuid.three;

public class DNIBack_OCR {
    private String document;              //Numero de documento
    private boolean conf_document;
    private String surname;               //Apellido
    private boolean conf_surname;
    private String name;                  //Nombre
    private boolean conf_name;
    private String nacionality;           //Nacionalidad
    private boolean conf_nacionality;
    private String domicilio;             //Domicilio
    private boolean conf_domicilio;
    private String date_birth;            //Fecha de nacimiento
    private boolean conf_date_birth;
    private int point_1;                  //Punto de evaluacion 1
    private boolean conf_point_1;
    private int point_2;                  //Punto de evaluacion 2
    private boolean conf_point_2;
    private int point_3;                  //Punto de evaluacion 3
    private boolean conf_point_3;

    public DNIBack_OCR() {
        this("",false,"",false,"",false,"",false,"",false, "",false,0, false,0, false,0, false);
    }

    public DNIBack_OCR(String document, boolean conf_document, String surname, boolean conf_surname, String name, boolean conf_name, String nacionality, boolean conf_nacionality, String domicilio, boolean conf_domicilio,  String date_birth, boolean conf_date_birth, int point_1, boolean conf_point_1, int point_2, boolean conf_point_2, int point_3, boolean conf_point_3) {
        this.document = document;
        this.conf_document = conf_document;
        this.surname = surname;
        this.conf_surname = conf_surname;
        this.name = name;
        this.conf_name = conf_name;
        this.nacionality = nacionality;
        this.conf_nacionality = conf_nacionality;
        this.domicilio = domicilio;
        this.conf_domicilio = conf_domicilio;
        this.date_birth = date_birth;
        this.conf_date_birth = conf_date_birth;
        this.point_1 = point_1;
        this.conf_point_1 = conf_point_1;
        this.point_2 = point_2;
        this.conf_point_2 = conf_point_2;
        this.point_3 = point_3;
        this.conf_point_3 = conf_point_3;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public boolean isConf_document() {
        return conf_document;
    }

    public void setConf_document(boolean conf_document) {
        this.conf_document = conf_document;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public boolean isConf_surname() {
        return conf_surname;
    }

    public void setConf_surname(boolean conf_surname) {
        this.conf_surname = conf_surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isConf_name() {
        return conf_name;
    }

    public void setConf_name(boolean conf_name) {
        this.conf_name = conf_name;
    }

    public String getNacionality() {
        return nacionality;
    }

    public void setNacionality(String nacionality) {
        this.nacionality = nacionality;
    }

    public boolean isConf_nacionality() {
        return conf_nacionality;
    }

    public void setConf_nacionality(boolean conf_nacionality) {
        this.conf_nacionality = conf_nacionality;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public boolean isConf_domicilio() {
        return conf_domicilio;
    }

    public void setConf_domicilio(boolean conf_domicilio) {
        this.conf_domicilio = conf_domicilio;
    }

    public String getDate_birth() {
        return date_birth;
    }

    public void setDate_birth(String date_birth) {
        this.date_birth = date_birth;
    }

    public boolean isConf_date_birth() {
        return conf_date_birth;
    }

    public void setConf_date_birth(boolean conf_date_birth) {
        this.conf_date_birth = conf_date_birth;
    }

    public int getPoint_1() {
        return point_1;
    }

    public void setPoint_1(int point_1) {
        this.point_1 = point_1;
    }

    public int getPoint_2() {
        return point_2;
    }

    public void setPoint_2(int point_2) {
        this.point_2 = point_2;
    }

    public int getPoint_3() {
        return point_3;
    }

    public void setPoint_3(int point_3) {
        this.point_3 = point_3;
    }

    public boolean isConf_point_1() {
        return conf_point_1;
    }

    public void setConf_point_1(boolean conf_point_1) {
        this.conf_point_1 = conf_point_1;
    }

    public boolean isConf_point_2() {
        return conf_point_2;
    }

    public void setConf_point_2(boolean conf_point_2) {
        this.conf_point_2 = conf_point_2;
    }

    public boolean isConf_point_3() {
        return conf_point_3;
    }

    public void setConf_point_3(boolean conf_point_3) {
        this.conf_point_3 = conf_point_3;
    }

    @Override
    public String toString() {
        String value= "DNIBack_OCR{" +
                "  document='" + document + '\'' +
                ", surname='" + surname + '\'' +
                ", name='" + name + '\'' +
                ", nacionality='" + nacionality + '\'' +
                ", domicilio='" + domicilio + '\'' +
                ", date_birth='" + date_birth + '\'' +
                ", point_1='" + point_1 + '\'' +
                ", point_2='" + point_2 + '\'' +
                ", point_3='" + point_3 + '\'' +
                '}';
        //System.out.println(value);
        return value;
    }
    public double valor(){
        double valor = 0, conf=0;
        if(!document.equals("")){
            valor++;
        }
        if(conf_document){
            conf++;
        }
        if(!surname.equals("")){
            valor++;
        }
        if(conf_surname){
            conf++;
        }
        if(!name.equals("")){
            valor++;
        }
        if(conf_name){
            conf++;
        }
        if(!nacionality.equals("")){
            valor++;
        }
        if(conf_nacionality){
            conf++;
        }
        if(!domicilio.equals("")){
            valor++;
        }
        if(conf_domicilio){
            conf++;
        }
        if(!date_birth.equals("")){
            valor++;
        }
        if(conf_date_birth){
            conf++;
        }
        if(point_1>0){
            valor++;
        }
        if(conf_point_1){
            conf++;
        }
        if(point_2>0){
            valor++;
        }
        if(conf_point_2){
            conf++;
        }
        if(point_3>0){
            valor++;
        }
        if(conf_point_3){
            conf++;
        }
        //System.out.println("Valor: "+valor+" + conf: "+conf+" /18");
        double factor =(valor+conf)/18;
        return factor;
    }
}
