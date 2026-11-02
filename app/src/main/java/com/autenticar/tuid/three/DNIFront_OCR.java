package com.autenticar.tuid.three;

public class DNIFront_OCR {
    private String document;        //Numero de documento
    private boolean conf_document;
    private String surname;         //Apellido
    private boolean conf_surname;
    private String name;            //Nombre
    private boolean conf_name;
    private String sex;             //Genero
    private boolean conf_sex;
    private String nationality;     //Nacionalidad
    private boolean conf_nationality;
    private String ejemplar;        //Ejemplar del DNI
    private boolean conf_ejemplar;
    private String date_birth;      //Fecha de nacimiento
    private boolean conf_date_birth;
    private String date_issue;      //Fecha de expedicion del documento
    private boolean conf_date_issue;
    private String n_tramite;       //Numero de tramite
    private boolean conf_n_tramite;

    public DNIFront_OCR(String document, String surname, String name, String sex, String nationality, String ejemplar, String date_birth, String date_issue, String date_expiry, String n_tramite) {
        this.document = document;
        this.surname = surname;
        this.name = name;
        this.sex = sex;
        this.nationality = nationality;
        this.ejemplar = ejemplar;
        this.date_birth = date_birth;
        this.date_issue = date_issue;
        this.n_tramite = n_tramite;
    }

    public DNIFront_OCR() {
        this("","","","","","","","","","");
    }

    public boolean isConf_n_tramite() {
        return conf_n_tramite;
    }

    public void setConf_n_tramite(boolean conf_n_tramite) {
        this.conf_n_tramite = conf_n_tramite;
    }

    public boolean isConf_document() {
        return conf_document;
    }

    public void setConf_document(boolean conf_document) {
        this.conf_document = conf_document;
    }

    public boolean isConf_date_issue() {
        return conf_date_issue;
    }

    public void setConf_date_issue(boolean conf_date_issue) {
        this.conf_date_issue = conf_date_issue;
    }

    public boolean isConf_date_birth() {
        return conf_date_birth;
    }

    public void setConf_date_birth(boolean conf_date_birth) {
        this.conf_date_birth = conf_date_birth;
    }

    public boolean isConf_ejemplar() {
        return conf_ejemplar;
    }

    public void setConf_ejemplar(boolean conf_ejemplar) {
        this.conf_ejemplar = conf_ejemplar;
    }

    public boolean isConf_nationality() {
        return conf_nationality;
    }

    public void setConf_nationality(boolean conf_nationality) {
        this.conf_nationality = conf_nationality;
    }

    public boolean isConf_sex() {
        return conf_sex;
    }

    public void setConf_sex(boolean conf_sex) {
        this.conf_sex = conf_sex;
    }

    public boolean isConf_surname() {
        return conf_surname;
    }

    public void setConf_surname(boolean conf_surname) {
        this.conf_surname = conf_surname;
    }

    public boolean isConf_name() {
        return conf_name;
    }

    public void setConf_name(boolean conf_name) {
        this.conf_name = conf_name;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getEjemplar() {
        return ejemplar;
    }

    public void setEjemplar(String ejemplar) {
        this.ejemplar = ejemplar;
    }

    public String getDate_birth() {
        return date_birth;
    }

    public void setDate_birth(String date_birth) {
        this.date_birth = date_birth;
    }

    public String getDate_issue() {
        return date_issue;
    }

    public void setDate_issue(String date_issue) {
        this.date_issue = date_issue;
    }


    public String getN_tramite() {
        return n_tramite;
    }

    public void setN_tramite(String n_tramite) {
        this.n_tramite = n_tramite;
    }

    @Override
    public String toString() {
        String value= "DNIFront_OCR{" +
                "document='" + document + '\'' +
                ", surname='" + surname + '\'' +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", nationality='" + nationality + '\'' +
                ", ejemplar='" + ejemplar + '\'' +
                ", date_birth='" + date_birth + '\'' +
                ", date_issue='" + date_issue + '\'' +
                ", n_tramite='" + n_tramite + '\'' +
                '}';
        //System.out.println(value);
        return "";
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
        if(!sex.equals("")){
            valor++;
        }
        if(conf_sex){
            conf++;
        }
        if(!nationality.equals("")){
            valor++;
        }
        if(conf_nationality){
            conf++;
        }
        if(!ejemplar.equals("")){
            valor++;
        }
        if(conf_ejemplar){
            conf++;
        }
        if(!date_birth.equals("")){
            valor++;
        }
        if(conf_date_birth){
            conf++;
        }
        if(!date_issue.equals("")){
            valor++;
        }
        if(conf_date_issue){
            conf++;
        }
        if(!n_tramite.equals("")){
            valor++;
        }
        if(conf_n_tramite){
            conf++;
        }
        //System.out.println("Valor: "+valor+" + conf: "+conf+" /18");

        double factor =(valor+conf)/18;
        return factor;
    }
}
