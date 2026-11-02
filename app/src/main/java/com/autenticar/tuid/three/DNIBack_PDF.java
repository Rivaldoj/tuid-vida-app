package com.autenticar.tuid.three;

public class DNIBack_PDF {
    private String value_front;       //Valor de la lectura del frente del dni
    private String documento;         //Numero de documento
    private boolean conf_documento;
    private String apellido;          //Apellido
    private boolean conf_apellido;
    private String nombre;            //Nombre
    private boolean conf_nombre;
    private String sexo;              //Genero
    private boolean conf_sexo;
    private String nacionalidad;      //Nacionalidad
    private boolean conf_nacionalidad;
    private String fecha_nac;         //Fecha de nacimiento
    private boolean conf_fecha_nac;
    private String domicilio;         //Domicilio
    private boolean conf_domicilio;
    private String n_tramite;        //Numero de tramite
    private boolean conf_n_tramite;

    public DNIBack_PDF(String value_front, String documento, boolean conf_documento, String apellido, boolean conf_apellido, String nombre, boolean conf_nombre, String sexo, boolean conf_sexo, String nacionalidad, boolean conf_nacionalidad, String fecha_nac, boolean conf_fecha_nac, String domicilio, boolean conf_domicilio, String n_tramite, boolean conf_n_tramite) {
        this.value_front = value_front;
        this.documento = documento;
        this.conf_documento = conf_documento;
        this.apellido = apellido;
        this.conf_apellido = conf_apellido;
        this.nombre = nombre;
        this.conf_nombre = conf_nombre;
        this.sexo = sexo;
        this.conf_sexo = conf_sexo;
        this.nacionalidad = nacionalidad;
        this.conf_nacionalidad = conf_nacionalidad;
        this.fecha_nac = fecha_nac;
        this.conf_fecha_nac = conf_fecha_nac;
        this.domicilio = domicilio;
        this.conf_domicilio = conf_domicilio;
        this.n_tramite = n_tramite;
        this.conf_n_tramite = conf_n_tramite;
    }

    public DNIBack_PDF() {
        this("", "", false, "", false, "", false, "", false, "", false, "", false, "", false, "", false);
    }

    public String getValue_front() {
        return value_front;
    }

    public void setValue_front(String value_front) {
        this.value_front = value_front;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public boolean isConf_documento() {
        return conf_documento;
    }

    public void setConf_documento(boolean conf_documento) {
        this.conf_documento = conf_documento;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public boolean isConf_apellido() {
        return conf_apellido;
    }

    public void setConf_apellido(boolean conf_apellido) {
        this.conf_apellido = conf_apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isConf_nombre() {
        return conf_nombre;
    }

    public void setConf_nombre(boolean conf_nombre) {
        this.conf_nombre = conf_nombre;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public boolean isConf_sexo() {
        return conf_sexo;
    }

    public void setConf_sexo(boolean conf_sexo) {
        this.conf_sexo = conf_sexo;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public boolean isConf_nacionalidad() {
        return conf_nacionalidad;
    }

    public void setConf_nacionalidad(boolean conf_nacionalidad) {
        this.conf_nacionalidad = conf_nacionalidad;
    }

    public String getFecha_nac() {
        return fecha_nac;
    }

    public void setFecha_nac(String fecha_nac) {
        this.fecha_nac = fecha_nac;
    }

    public boolean isConf_fecha_nac() {
        return conf_fecha_nac;
    }

    public void setConf_fecha_nac(boolean conf_fecha_nac) {
        this.conf_fecha_nac = conf_fecha_nac;
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

    public String getN_tramite() {
        return n_tramite;
    }

    public void setN_tramite(String n_tramite) {
        this.n_tramite = n_tramite;
    }

    public boolean isConf_n_tramite() {
        return conf_n_tramite;
    }

    public void setConf_n_tramite(boolean conf_n_tramite) {
        this.conf_n_tramite = conf_n_tramite;
    }

    @Override
    public String toString() {
        String value = "DNIBack_PDF{" + '\n' +
                    "  documento='" + documento + '\n' +
                    ", conf_documento=" + conf_documento + '\n' +
                    ", apellido='" + apellido + '\n' +
                    ", conf_apellido=" + conf_apellido + '\n' +
                    ", nombre='" + nombre + '\n' +
                    ", conf_nombre=" + conf_nombre + '\n' +
                    ", sexo='" + sexo + '\n' +
                    ", conf_sexo=" + conf_sexo + '\n' +
                    ", nacionalidad='" + nacionalidad + '\n' +
                    ", conf_nacionalidad=" + conf_nacionalidad + '\n' +
                    ", fecha_nac='" + fecha_nac + '\n' +
                    ", conf_fecha_nac=" + conf_fecha_nac + '\n' +
                    ", domicilio='" + domicilio + '\n' +
                    ", conf_domicilio=" + conf_domicilio + '\n' +
                    ", n_tramite='" + n_tramite + '\n' +
                    ", conf_n_tramite=" + conf_n_tramite + '\n' +
                '}';
        //System.out.println(value);
        return value;
    }

    public boolean isValidationDNI() {
        if (conf_documento) {
            if (this.value_front.toUpperCase().indexOf(documento) > 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    public double valor(){
        double valor = 0, conf=0;
        if(!apellido.equals("")){
            valor++;
        }
        if(conf_apellido){
            conf++;
        }
        if(!nombre.equals("")){
            valor++;
        }
        if(conf_nombre){
            conf++;
        }
        if(!documento.equals("")){
            valor++;
        }
        if(conf_documento){
            conf++;
        }
        /*if(!nacionalidad.equals("")){
            valor++;
        }
        if(conf_nacionalidad){
            conf++;
        }*/
        if(!sexo.equals("")){
            valor++;
        }
        if(conf_sexo){
            conf++;
        }
        if(!fecha_nac.equals("")){
            valor++;
        }
        if(conf_fecha_nac){
            conf++;
        }
        if(!domicilio.equals("")){
            valor++;
        }
        if(conf_domicilio){
            conf++;
        }
        if(!n_tramite.equals("")){
            valor++;
        }
        if(conf_n_tramite){
            conf++;
        }
        double factor =(valor+conf)/14;
        //System.out.println("FACTOR: "+factor);
        return factor;
    }
}
