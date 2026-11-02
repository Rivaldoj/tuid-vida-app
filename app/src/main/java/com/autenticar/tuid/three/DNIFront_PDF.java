package com.autenticar.tuid.three;

public class DNIFront_PDF {
    private String value;
    //private String apellidos;             //Numero de documento
    private boolean conf_apellidos;
    //private String nombre;                //Apellido
    private boolean conf_nombre;
    private String documento;               //Nombre
    private boolean conf_documento;
    //private String nacionalidad;          //Nacionalidad
    private boolean conf_nacionalidad;
    //private String sexo;                  //sexo
    private boolean conf_sexo;
    //private String fecha_expe;            //Fecha de expedicion
    private boolean conf_fecha_expe;
    //private String fecha_venc;            //Fecha de vencimiento
    private boolean conf_fecha_venc;
    //private int point_1;                  //Punto de evaluacion 1
    //private boolean conf_point_1;
    //private int point_2;                  //Punto de evaluacion 2
    //private boolean conf_point_2;
    //private int point_3;                  //Punto de evaluacion 3
    //private boolean conf_point_3;

    public DNIFront_PDF() {
        this("" ,false ,"" ,false,"" ,false,"",false,"",false,"",false, "",false, 0, false,0, false,0, false);
    }

    public DNIFront_PDF(String apellidos, boolean conf_apellidos, String nombre, boolean conf_nombre, String documento, boolean conf_documento, String nacionalidad, boolean conf_nacionalidad, String sexo, boolean conf_sexo, String fecha_expe, boolean conf_fecha_expe, String fecha_venc, boolean conf_fecha_venc, int point_1, boolean conf_point_1, int point_2, boolean conf_point_2, int point_3, boolean conf_point_3) {
        //this.apellidos = apellidos;
        this.conf_apellidos = conf_apellidos;
        //this.nombre = nombre;
        this.conf_nombre = conf_nombre;
        this.documento = documento;
        this.conf_documento = conf_documento;
        //this.nacionalidad = nacionalidad;
        this.conf_nacionalidad = conf_nacionalidad;
        //this.sexo = sexo;
        this.conf_sexo = conf_sexo;
        //this.fecha_expe = fecha_expe;
        this.conf_fecha_expe = conf_fecha_expe;
        //this.fecha_venc = fecha_venc;
        this.conf_fecha_venc = conf_fecha_venc;
        //this.point_1 = point_1;
        //this.conf_point_1 = conf_point_1;
        //this.point_2 = point_2;
        //this.conf_point_2 = conf_point_2;
        //this.point_3 = point_3;
        //this.conf_point_3 = conf_point_3;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
//public String getApellidos() {
    //    return apellidos;
    //}

    //public void setApellidos(String apellidos) {
    //    this.apellidos = apellidos;
    //}

    public boolean isConf_apellidos() {
        return conf_apellidos;
    }

    public void setConf_apellidos(boolean conf_apellidos) {
        this.conf_apellidos = conf_apellidos;
    }

    //public String getNombre() {
    //    return nombre;
    //}

    //public void setNombre(String nombre) {
    //    this.nombre = nombre;
    //}

    public boolean isConf_nombre() {
        return conf_nombre;
    }

    public void setConf_nombre(boolean conf_nombre) {
        this.conf_nombre = conf_nombre;
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

    //public String getNacionalidad() {
    //    return nacionalidad;
    //}

    //public void setNacionalidad(String nacionalidad) {
    //    this.nacionalidad = nacionalidad;
    //}

    public boolean isConf_nacionalidad() {
        return conf_nacionalidad;
    }

    public void setConf_nacionalidad(boolean conf_nacionalidad) {
        this.conf_nacionalidad = conf_nacionalidad;
    }

    //public String getSexo() {
    //    return sexo;
    //}

    //public void setSexo(String sexo) {
    //    this.sexo = sexo;
    //}

    public boolean isConf_sexo() {
        return conf_sexo;
    }

    public void setConf_sexo(boolean conf_sexo) {
        this.conf_sexo = conf_sexo;
    }

    //public String getFecha_expe() {
    //    return fecha_expe;
    //}

    //public void setFecha_expe(String fecha_expe) {
    //    this.fecha_expe = fecha_expe;
    //}

    public boolean isConf_fecha_expe() {
        return conf_fecha_expe;
    }

    public void setConf_fecha_expe(boolean conf_fecha_expe) {
        this.conf_fecha_expe = conf_fecha_expe;
    }

    //public String getFecha_venc() {
    //    return fecha_venc;
    //}

    //public void setFecha_venc(String fecha_venc) {
    //   this.fecha_venc = fecha_venc;
    //}

    public boolean isConf_fecha_venc() {
        return conf_fecha_venc;
    }

    public void setConf_fecha_venc(boolean conf_fecha_venc) {
        this.conf_fecha_venc = conf_fecha_venc;
    }

    //public int getPoint_1() {
    //    return point_1;
    //}

    //public void setPoint_1(int point_1) {
    //    this.point_1 = point_1;
    //}

    //public boolean isConf_point_1() {
    //    return conf_point_1;
    //}

    //public void setConf_point_1(boolean conf_point_1) {
    //    this.conf_point_1 = conf_point_1;
    //}

    //public int getPoint_2() {
    //    return point_2;
    //}

    //public void setPoint_2(int point_2) {
    //    this.point_2 = point_2;
    //}

    //public boolean isConf_point_2() {
    //    return conf_point_2;
    //}

    //public void setConf_point_2(boolean conf_point_2) {
    //    this.conf_point_2 = conf_point_2;
    //}

    //public int getPoint_3() {
    //    return point_3;
    //}

    //public void setPoint_3(int point_3) {
    //    this.point_3 = point_3;
    //}

    //public boolean isConf_point_3() {
    //    return conf_point_3;
    //}

    //public void setConf_point_3(boolean conf_point_3) {
    //    this.conf_point_3 = conf_point_3;
    //}

    @Override
    public String toString() {
        String value = "DNIFront_PDF{" +'\n' +
                " value=" + this.value +'\n' +
                ", conf_apellidos=" + conf_apellidos +'\n' +
                ", conf_nombre=" + conf_nombre +'\n' +
                ", documento=" + documento +'\n' +
                ", conf_documento=" + conf_documento +'\n' +
                ", conf_nacionalidad=" + conf_nacionalidad +'\n' +
                ", conf_sexo=" + conf_sexo +'\n' +
                ", conf_fecha_expe=" + conf_fecha_expe +'\n' +
                ", conf_fecha_venc=" + conf_fecha_venc +'\n' +
                '}';
        //System.out.println(value);
        return value;
    }

    public double valor(){
        double valor = 0, conf=0;
        //if(!apellidos.equals("")){
        //    valor++;
        //}
        if(conf_apellidos){
            conf++;
        }
        //if(!nombre.equals("")){
        //    valor++;
        //}
        if(conf_nombre){
            conf++;
        }
        if(!documento.equals("")){
            valor++;
        }
        if(conf_documento){
            conf++;
        }
        //if(!nacionalidad.equals("")){
        //    valor++;
        //}
        if(conf_nacionalidad){
            conf++;
        }
        //if(!sexo.equals("")){
        //    valor++;
        //}
        if(conf_sexo){
            conf++;
        }
        //if(!fecha_expe.equals("")){
        //    valor++;
        //}
        if(conf_fecha_expe){
            conf++;
        }
        //if(!fecha_venc.equals("")){
        //    valor++;
        //}
        if(conf_fecha_venc){
            conf++;
        }
        //    if(point_1>0){
        //    valor++;
        //}
        //if(conf_point_1){
        //    conf++;
        //}
        //if(point_2>0){
        //    valor++;
        //}
        //if(conf_point_2){
        //    conf++;
        //}
        //if(point_3>0){
        //    valor++;
        //}
        //if(conf_point_3){
        //    conf++;
        //}

        double factor =(valor+conf)/8;
        //System.out.println("FACTOR: "+factor);
        return factor;
    }
}
