
package com.autenticar.tuid.v2.services;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Pago {

    @SerializedName("cuil")
    @Expose
    private Long cuil;
    @SerializedName("dni")
    @Expose
    private Integer dni;
    @SerializedName("sexo")
    @Expose
    private String sexo;
    @SerializedName("Nombres")
    @Expose
    private String nombres;
    @SerializedName("FechaNac")
    @Expose
    private String fechaNac;
    @SerializedName("Cod_local")
    @Expose
    private String codLocal;
    @SerializedName("CP")
    @Expose
    private String cp;
    @SerializedName("Familiar")
    @Expose
    private String familiar;
    @SerializedName("IDRelLab")
    @Expose
    private Integer iDRelLab;
    @SerializedName("Cuitemplea")
    @Expose
    private Integer cuitemplea;
    @SerializedName("IDBanco")
    @Expose
    private Integer iDBanco;
    @SerializedName("IDObrasoc")
    @Expose
    private Object iDObrasoc;
    @SerializedName("Telefono")
    @Expose
    private String telefono;
    @SerializedName("Celular")
    @Expose
    private String celular;
    @SerializedName("Fallecido")
    @Expose
    private Integer fallecido;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("IDEntidad")
    @Expose
    private Integer iDEntidad;
    @SerializedName("caja")
    @Expose
    private String caja;
    @SerializedName("fechaActual")
    @Expose
    private String fechaActual;
    @SerializedName("fechaProxima")
    @Expose
    private String fechaProxima;
    @SerializedName("IDSucursal")
    @Expose
    private Integer iDSucursal;
    @SerializedName("sucursal")
    @Expose
    private String sucursal;
    @SerializedName("domicilio")
    @Expose
    private String domicilio;
    @SerializedName("localidad")
    @Expose
    private String localidad;
    @SerializedName("provincia")
    @Expose
    private String provincia;
    @SerializedName("IDBANCO")
    @Expose
    private Integer idbanco;
    @SerializedName("banco")
    @Expose
    private String banco;

    public Long getCuil() {
        return cuil;
    }

    public void setCuil(Long cuil) {
        this.cuil = cuil;
    }

    public Integer getDni() {
        return dni;
    }

    public void setDni(Integer dni) {
        this.dni = dni;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(String fechaNac) {
        this.fechaNac = fechaNac;
    }

    public String getCodLocal() {
        return codLocal;
    }

    public void setCodLocal(String codLocal) {
        this.codLocal = codLocal;
    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public String getFamiliar() {
        return familiar;
    }

    public void setFamiliar(String familiar) {
        this.familiar = familiar;
    }

    public Integer getIDRelLab() {
        return iDRelLab;
    }

    public void setIDRelLab(Integer iDRelLab) {
        this.iDRelLab = iDRelLab;
    }

    public Integer getCuitemplea() {
        return cuitemplea;
    }

    public void setCuitemplea(Integer cuitemplea) {
        this.cuitemplea = cuitemplea;
    }

    public Integer getIDBanco() {
        return iDBanco;
    }

    public void setIDBanco(Integer iDBanco) {
        this.iDBanco = iDBanco;
    }

    public Object getIDObrasoc() {
        return iDObrasoc;
    }

    public void setIDObrasoc(Object iDObrasoc) {
        this.iDObrasoc = iDObrasoc;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public Integer getFallecido() {
        return fallecido;
    }

    public void setFallecido(Integer fallecido) {
        this.fallecido = fallecido;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIDEntidad() {
        return iDEntidad;
    }

    public void setIDEntidad(Integer iDEntidad) {
        this.iDEntidad = iDEntidad;
    }

    public String getCaja() {
        return caja;
    }

    public void setCaja(String caja) {
        this.caja = caja;
    }

    public String getFechaActual() {
        return fechaActual;
    }

    public void setFechaActual(String fechaActual) {
        this.fechaActual = fechaActual;
    }

    public String getFechaProxima() {
        return fechaProxima;
    }

    public void setFechaProxima(String fechaProxima) {
        this.fechaProxima = fechaProxima;
    }

    public Integer getIDSucursal() {
        return iDSucursal;
    }

    public void setIDSucursal(Integer iDSucursal) {
        this.iDSucursal = iDSucursal;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public Integer getIdbanco() {
        return idbanco;
    }

    public void setIdbanco(Integer idbanco) {
        this.idbanco = idbanco;
    }

    public String getBanco() {
        return banco;
    }

    public void setBanco(String banco) {
        this.banco = banco;
    }

}