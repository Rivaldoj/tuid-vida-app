package com.autenticar.tuid.model;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.autenticar.tuid.three.ArbolBinario;
import com.autenticar.tuid.three.DNIBack_OCR;
import com.autenticar.tuid.three.DNIBack_PDF;
import com.autenticar.tuid.three.DNIFront_OCR;
import com.autenticar.tuid.three.DNIFront_PDF;
import com.autenticar.tuid.utils.HelperFunctions;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.autenticar.tuid.utils.HelperFunctions.chararterSpece;

public class Persona{

    //FRENTE OCR
    private static final String FrontOCRAPELLIDO = "Apellido";
    private static final String FrontOCRSURNAME = "Surname";
    private static final String FrontOCRNOMBRE = "Nombre";
    private static final String FrontOCRNAME = "Name";
    private static final String FrontOCRSEXO = "Sexo";
    private static final String FrontOCRSEX = "Sex";
    private static final String FrontOCRNACIONALIDAD = "Necionalidad";
    private static final String FrontOCRNATIONALITY = "Nationality";
    private static final String FrontOCREjemplar = "Ejemplar";
    private static final String FrontOCRFECHA_NACIMIENTO = "Fecha de nacimiento";
    private static final String FrontOCRDATE_BIRTH = "Date of birth";
    private static final String FrontOCRFECHA_EMISION = "Fecha de emicion";
    private static final String FrontOCRDATE_ISSUE = "Date of issue";
    private static final String FrontOCRTRAMITE = "Trámite";
    private static final String FrontOCRIDENT = "ident";
    private static final String FrontOCRDOCUMENTO = "Documento";
    private static double ResultOCRFront = 0;
    private static double ResultOCRBack = 0;
    public static double ResultPDFFront = 0;
    private static double ResultPDFBack = 0;
    private static int vuelt = 0;
    //public String scan_guide_info="";

    private static String DNI_Anterior = "";
    //DORSO
    private static final String DOMICILIO = "DOMICILIO";
    private static final String LUGAR_DE_NACIMIENTO = "LUGAR";
    private static final String FECHA_DE_INGRESO = "FECHA DE INGRESO";
    private static final String DIGITO_PUGLAR = "PUGLAR";

    public String ApellidoQR = ""; //corresponde a lectura de PDF147
    public String NombreQR = "";
    public String DocumentoQR;
    public String DocumentoOCRFront;// documento formateado
    public String TramiteQR;
    public String EjemplarQR;


    public String DocumentoPDF;
    public String FechaNacPDF;
    public Date FechaVecPDF;
    public String SexoPDF;

    public Date FechaNacQR;
    public Date FechaExpiracionDni;
    public Date FechaEmiQR;
    public String FechaVecQR;
    public String Domicilio;
    public String FechaNacimientoOCRFront;
    public String FechaEmisionOCRFront;
    public boolean EsMasculinoQR = false;

    public int PUNTO_1 = 0;
    public int PUNTO_2 = 0;
    public int PUNTO_3 = 0;
    public int PUNTO_4 = 0;
    /*
        FORMATO EVALUACION PDF FRONT
    */
    public static final String PDF_APELLIDOS = "APELLIDO/S";
    public static final String PDF_NOMBRES = "NOMBRE/S";
    public static final String PDF_N_DOCUMENTO = "NUMERO DE DOCUMENTO";
    public static final String PDF_SEXO = "SEXO";
    public static final String PDF_NACIONALIDAD = "NACIONALIDAD";
    public static final String PDF_F_EXPEDICION = "EXPEDICION";
    public static final String PDF_F_VENCIMIENTO = "VENCIMIENTO";
    /*
        FORMATO EVALUACION PDF BACK
    */
    public static final String PDF_DOMICILIO = "DOMICILIO";
    public static final String PDF_FECHA_LUGAR_NACIMIENTO = "FECHA";
    public static final String PDF_N_TRAMITE = "TRAMITE";
    public static final String PDF_OF_IDENT = "OF";


    public boolean ApellidoFrontPDFVerified;
    public boolean NombreFrontPDFVerified;
    public boolean DocumentoFrontPDFVerified;
    public boolean SexoFrontPDFVerified;
    public boolean FechaNacimientoFrontPDFVerified;
    public boolean FechaExpedicionFrontPDFVerified;
    public boolean FechaVencimientodoFrontPDFVerified;
    public boolean NacionalidadFrontPDFVerified;

    //solo se consigue leyendo el DNI
    public String DomicilioOCR;
    public String NacionalidadOCR;

    //retornan el estado de validaciones entre el PDF147 y OCR
    //si OCR ok se retorna true

    public boolean ApellidoFrontOCRVerified = false;
    public boolean ApellidoBackOCRVerified;
    public boolean NombreFrontOCRVerified = false;
    public boolean NombreBackOCRVerified;
    public boolean DomicilioOCRAsignado;
    public boolean FechaNacimientoFrontOCRVerified = false;
    public boolean DocumentoFrontOCRVerified;
    public boolean DocumentoBackOCRVerified;
    public boolean TramiteOCRVerified = false;

    public Bitmap dniFront;
    public Bitmap dniBack;
    public Bitmap seflie;
    public Bitmap bitmapFace;
    public int prog = 0;
    public static int count = 0;
    private boolean barcodeReaded;
    public boolean ocrFrontReaded;
    private boolean ocrBackReaded;
    public boolean pdfFrontReaded;
    private boolean pdfBackReaded;
    private boolean faceDetected;

    private ProgressBar progressBar;
    private Activity activity;
    private TextView scan_guide;
    private int conta = 0;
    private int determinador = 0;
    private ArbolBinario Arbol = new ArbolBinario();
    private boolean pdf = false;
    private boolean ocr = false;
    public static ArrayList<DNIFront_OCR> list_DNIFront_OCR;

    public static Persona singleton;

    public Persona() {
        singleton = this;
    }


    public void setScan_guide(TextView scan_guide) {
        this.scan_guide = scan_guide;
    }

    public void iniProgressBar() {
        progressBar.setProgress(0);
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public String FileNameToUpload(Boolean isFront, boolean isDebug) {
        if (isDebug) {
            return (isFront == null ? "99999999F_S" : (isFront ? "99999999F_F" : "99999999F_B"));
        } else {
            return this.DocumentoQR + (this.EsMasculinoQR ? "M" : "F") + "_" + (isFront == null ? "S" : (isFront ? "F" : "B"));
        }
    }

    public boolean IsBarcodeReaded() {
        return barcodeReaded;
    }

    public boolean IsFaceDetected() {
        return faceDetected;
    }

    public void setMaxProgressBar(int value) {
        progressBar.setMax(value);
    }

    public double ProcessFrontOCRReaded(String value) {
        System.out.println("ProcessFrontOCRReaded");
        value = chararterSpece(value);
        ocr = true;
        pdf = false;
        barcodeReaded = false;
        if (DNI_Anterior != this.DocumentoQR && !DNI_Anterior.trim().equals("")) {
            Arbol = null;
            //barcodeReaded=false;
            ocrFrontReaded = false;
            /*DNI_Anterior="";
            this.TramiteQR="";
            this.NombreQR="";
            this.DocumentoQR="";
            this.ApellidoQR="";
            this.EjemplarQR="";
            this.FechaEmiQR=null;
            this.FechaNacQR=null;*/
            Arbol = new ArbolBinario();
        }
        if (count == 20) {
            Arbol = null;
            count = 0;
            vuelt++;
            ResultOCRFront = 0;
        }
        DNIFront_OCR DNI_OCR = new DNIFront_OCR();
        System.out.println("OCR FrontOCRAPELLIDO:"+FrontOCRAPELLIDO);
        System.out.println("OCR FrontOCRSURNAME:"+FrontOCRSURNAME);
        System.out.println("OCR ApellidoQR:"+ApellidoQR);
        if ((value.indexOf(FrontOCRAPELLIDO) > 0 || value.indexOf(FrontOCRSURNAME) > 0) && this.ApellidoQR != null) {
            DNI_OCR.setSurname(this.ApellidoQR.toUpperCase().replace(" ", "  "));
            DNI_OCR.setConf_surname(true);
        } else if (this.ApellidoQR != null) {
            DNI_OCR.setSurname(this.ApellidoQR.toUpperCase().replace(" ", "  "));
            DNI_OCR.setConf_surname(false);
        } else {
            DNI_OCR.setSurname("");
            DNI_OCR.setConf_surname(false);
        }
        System.out.println("OCR FrontOCRNAME:"+FrontOCRNAME);
        System.out.println("OCR FrontOCRNOMBRE:"+FrontOCRNOMBRE);
        System.out.println("OCR NombreQR:"+NombreQR);
        if ((value.indexOf(FrontOCRNAME) > 0 || value.indexOf(FrontOCRNOMBRE) > 0) && this.NombreQR != null) {
            DNI_OCR.setName(this.NombreQR.toUpperCase().replace(" ", "  "));
            DNI_OCR.setConf_name(true);
        } else if (this.NombreQR != null) {
            DNI_OCR.setName(this.NombreQR.toUpperCase().replace(" ", "  "));
            DNI_OCR.setConf_name(false);
        } else {
            DNI_OCR.setName("");
            DNI_OCR.setConf_name(false);
        }
        System.out.println("OCR FrontOCRSEXO:"+FrontOCRSEXO);
        System.out.println("OCR FrontOCRSEX:"+FrontOCRSEX);
        if (value.indexOf(FrontOCRSEXO) > 0 || value.indexOf(FrontOCRSEX) > 0) {
            if (this.EsMasculinoQR) {
                DNI_OCR.setSex("M");
            } else {
                DNI_OCR.setSex("F");
            }
            DNI_OCR.setConf_sex(true);
        } else if (this.EsMasculinoQR) {
            DNI_OCR.setSex("M");
            DNI_OCR.setConf_sex(false);
        } else if (!this.EsMasculinoQR) {
            DNI_OCR.setSex("F");
            DNI_OCR.setConf_sex(false);
        } else {
            DNI_OCR.setSex("");
            DNI_OCR.setConf_name(false);
        }
        System.out.println("OCR FrontOCRNACIONALIDAD:"+FrontOCRNACIONALIDAD);
        System.out.println("OCR FrontOCRNATIONALITY:"+FrontOCRNATIONALITY);
        if ((value.indexOf(FrontOCRNACIONALIDAD) > 0 || value.indexOf(FrontOCRNATIONALITY) > 0)) {
            DNI_OCR.setNationality("ARGENTINA");
            DNI_OCR.setConf_nationality(true);
        } else {
            DNI_OCR.setNationality("");
            DNI_OCR.setConf_nationality(false);
        }
        System.out.println("OCR FrontOCREjemplar:"+FrontOCREjemplar);
        System.out.println("OCR EjemplarQR:"+EjemplarQR);
        if (value.indexOf(FrontOCREjemplar) > 0 && this.EjemplarQR != null) {
            DNI_OCR.setEjemplar(this.EjemplarQR.toUpperCase());
            DNI_OCR.setConf_ejemplar(true);
        } else if (this.EjemplarQR != null) {
            DNI_OCR.setEjemplar(this.EjemplarQR.toUpperCase());
            DNI_OCR.setConf_ejemplar(false);
        } else {
            DNI_OCR.setEjemplar("");
            DNI_OCR.setConf_ejemplar(false);
        }
        System.out.println("OCR FrontOCRFECHA_NACIMIENTO:"+FrontOCRFECHA_NACIMIENTO);
        System.out.println("OCR FrontOCRDATE_BIRTH:"+FrontOCRDATE_BIRTH);
        System.out.println("OCR FechaNacimientoOCRFront:"+FechaNacimientoOCRFront);
        if ((value.indexOf(FrontOCRFECHA_NACIMIENTO) > 0 || value.indexOf(FrontOCRDATE_BIRTH) > 0) && this.FechaNacimientoOCRFront != null) {
            DNI_OCR.setDate_birth(this.FechaNacimientoOCRFront);
            DNI_OCR.setConf_date_birth(true);
        } else if (this.FechaNacimientoOCRFront != null) {
            DNI_OCR.setDate_birth(this.FechaNacimientoOCRFront);
            DNI_OCR.setConf_date_birth(false);
        } else {
            DNI_OCR.setDate_birth("");
            DNI_OCR.setConf_date_birth(false);
        }
        System.out.println("OCR FrontOCRFECHA_EMISION:"+FrontOCRFECHA_EMISION);
        System.out.println("OCR FrontOCRDATE_ISSUE:"+FrontOCRDATE_ISSUE);
        System.out.println("OCR FechaEmisionOCRFront:"+FechaEmisionOCRFront);
        if ((value.indexOf(FrontOCRFECHA_EMISION) > 0 || value.indexOf(FrontOCRDATE_ISSUE) > 0) && this.FechaEmisionOCRFront != null) {
            DNI_OCR.setDate_issue(this.FechaEmisionOCRFront);
            DNI_OCR.setConf_date_issue(true);
        } else if (this.FechaEmisionOCRFront != null) {
            DNI_OCR.setDate_issue(this.FechaEmisionOCRFront);
            DNI_OCR.setConf_date_issue(false);
        } else {
            DNI_OCR.setDate_issue("");
            DNI_OCR.setConf_date_issue(false);
        }
        System.out.println("OCR FrontOCRDOCUMENTO:"+FrontOCRDOCUMENTO);
        System.out.println("OCR DocumentoQR:"+DocumentoQR);
        if (value.indexOf(FrontOCRDOCUMENTO) > 0 && this.DocumentoQR != null) {
            DNI_OCR.setDocument(this.DocumentoQR);
            DNI_Anterior = this.DocumentoQR;
            DNI_OCR.setConf_document(true);
        } else if (this.DocumentoQR != null) {
            DNI_OCR.setDocument(this.DocumentoQR);
            DNI_Anterior = this.DocumentoQR;
            DNI_OCR.setConf_document(false);
        } else {
            DNI_OCR.setDocument("");
            DNI_OCR.setConf_document(false);
        }
        System.out.println("OCR FrontOCRTRAMITE:"+FrontOCRTRAMITE);
        System.out.println("OCR FrontOCRIDENT:"+FrontOCRIDENT);
        System.out.println("OCR TramiteQR:"+TramiteQR);
        if ((value.indexOf(FrontOCRTRAMITE) > 0 || value.indexOf(FrontOCRIDENT) > 0) && this.TramiteQR != null) {
            DNI_OCR.setN_tramite(this.TramiteQR);
            DNI_OCR.setConf_n_tramite(true);
        } else if (this.TramiteQR != null) {
            DNI_OCR.setN_tramite(this.TramiteQR);
            DNI_OCR.setConf_n_tramite(false);
        } else {
            DNI_OCR.setN_tramite("");
            DNI_OCR.setConf_n_tramite(false);
        }
        double result = DNI_OCR.valor();
        System.out.println("VALOR "+result);
        //System.out.println("this.DocumentoQR.length() > 0: "+this.DocumentoQR.length());
        if (this.DocumentoQR.length() > 0 ) {
            System.out.println("OCR-1");
            if (result > 0.75 || ResultOCRFront / count > 0.70) {
                ocrFrontReaded = true;
                barcodeReaded = true;
                ResultOCRFront = result;
                count = 0;
                vuelt = 0;
                System.out.println("OCR-1-1");
            } else if (vuelt >= 2 && (result > 0.35 || ResultOCRFront / count > 0.3)) {
                ocrFrontReaded = true;
                barcodeReaded = true;
                ResultOCRFront = result;
                count = 0;
                vuelt = 0;
                System.out.println("OCR-1-2");
            } else {
                count++;
                ResultOCRFront += result;
                System.out.println("OCR-1-3");
            }
        } else {
            count++;
            ResultOCRFront += result;
            System.out.println("OCR-2");
        }
        //list_DNIFront_OCR.add(DNI_OCR);
        if(count>0) {
            System.out.println(vuelt+"/"+count+" RESULTADO FRONT: " + result + " AVERAGE: " + ResultOCRFront / count);
        }
        return result;
    }

    public double ProcessBackOCRReaded(String value) {
        System.out.println("ProcessBackPDFReaded");
        value = chararterSpece(value);
        System.out.println("value: "+value);
        SimpleDateFormat formato = new SimpleDateFormat("yyMMdd", Locale.getDefault());
        if (count == 20) {
            Arbol = null;
            count = 0;
            vuelt++;
            ResultOCRBack = 0;
        }
        DNIBack_OCR DNI_OCR = new DNIBack_OCR();
        System.out.println("OCR Back value.indexOf(this.DocumentoQR) "+value.indexOf(this.DocumentoQR) );
        if (value.indexOf(this.DocumentoQR) > 0) {
            DNI_OCR.setDocument(this.DocumentoQR);
            DNI_OCR.setConf_document(true);
        } else {
            int val_ini = 0;
            int val_fin = 0;
            if (value.indexOf("IDARG") > 0) {
                val_ini = value.indexOf("IDARG");
                val_fin = value.indexOf("IDARG") + 5 + 7;
            }
            String val = "";
            if (val_ini > 0) {
                val = value.substring(val_ini, val_fin).trim();
            }
            //return 0;
        }
        System.out.println("OCR Back value.indexOf(this.ApellidoQR.toUpperCase()) "+value.indexOf(this.ApellidoQR.toUpperCase()));
        System.out.println("OCR Back this.ApellidoQR "+this.ApellidoQR);
        if (value.indexOf(this.ApellidoQR.toUpperCase()) > 0 && this.ApellidoQR != null) {
            DNI_OCR.setSurname(this.ApellidoQR.toUpperCase().replace(" ", "  "));
            DNI_OCR.setConf_surname(true);
        } else {
            DNI_OCR.setSurname("");
            DNI_OCR.setConf_surname(false);
        }
        System.out.println("OCR Back value.indexOf(this.NombreQR.toUpperCase()) "+value.indexOf(this.NombreQR.toUpperCase()));
        System.out.println("OCR Back this.NombreQR "+this.NombreQR);
        if (value.indexOf(this.NombreQR.toUpperCase()) > 0 && this.NombreQR != null) {
            DNI_OCR.setName(this.NombreQR.toUpperCase().replace(" ", "  "));
            DNI_OCR.setConf_name(true);
        } else {
            DNI_OCR.setName("");
            DNI_OCR.setConf_name(false);
        }
        System.out.println("OCR Back IDARG; "+value.indexOf("IDARG"));
        if ((value.indexOf("IDARG") > 0)) {
            DNI_OCR.setNacionality("ARGENTINA");
            DNI_OCR.setConf_nacionality(true);
        } else {
            DNI_OCR.setNacionality("");
            DNI_OCR.setConf_nacionality(false);
        }

        int lugarNacimiento = value.indexOf(LUGAR_DE_NACIMIENTO);
        int domicilioIndex = lugarNacimiento - LUGAR_DE_NACIMIENTO.length() - 40;
        System.out.println("OCR Back lugarNacimiento; "+lugarNacimiento);
        System.out.println("OCR Back domicilioIndex; "+domicilioIndex);
        if (domicilioIndex > 0 && lugarNacimiento > 0 && lugarNacimiento > domicilioIndex) {
            String domi = value.substring(domicilioIndex, lugarNacimiento).toUpperCase();
            if (domi.length() > 0) {
                DNI_OCR.setDomicilio(domi);
                DNI_OCR.setConf_domicilio(true);
            } else {
                DNI_OCR.setDomicilio("");
                DNI_OCR.setConf_domicilio(false);
            }
        } else {
            DNI_OCR.setDomicilio("");
            DNI_OCR.setConf_domicilio(false);
        }
        String date_birth = formato.format(this.FechaNacQR);
        if ((value.indexOf(date_birth) > 0)) {
            DNI_OCR.setDate_birth(date_birth);
            DNI_OCR.setConf_date_birth(true);
        } else {
            DNI_OCR.setDate_birth("");
            DNI_OCR.setConf_date_birth(false);
        }
        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        /*Digito de control*/
        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //PUNTO 1
        int n_ini = 0;
        String arg = "";
        System.out.println("OCR Back value.indexOf(IDARGM); "+value.indexOf("IDARGM"));
        if (value.indexOf("IDARGM") > 0) {
            n_ini = value.indexOf("IDARGM" + this.DocumentoQR + "<");
            arg = "IDARGM";
        } else if (value.indexOf("IDARG") > 0) {
            n_ini = value.indexOf("IDARG" + this.DocumentoQR + "<");
            arg = "IDARG";
        }
        int n_end = value.indexOf("<<");
        System.out.println("OCR Back n_end; "+n_end);
        if (n_ini >= 0 && n_end >= 0) {
            String val = value.substring(n_ini + (arg + this.DocumentoQR + "<").length(), n_end).trim();
            if (Integer.valueOf(val) > 0 && Integer.valueOf(val) < 10) {
                DNI_OCR.setPoint_1(Integer.parseInt(val));
                DNI_OCR.setConf_point_1(true);
            } else {
                DNI_OCR.setPoint_1(-1);
                DNI_OCR.setConf_point_1(false);
            }
        } else {
            DNI_OCR.setPoint_1(-1);
            DNI_OCR.setConf_point_1(false);
        }
        //PUNTO 2
        String FN = formato.format(FechaNacQR);
        n_ini = value.indexOf(FN);
        System.out.println("OCR Back FN; "+FN);
        System.out.println("OCR Back n_ini; "+n_ini);
        if (n_ini >= 0) {
            String val = value.substring(n_ini + (FN).length(), n_ini + (FN).length() + 1).trim();
            if (Integer.valueOf(val.trim()) > 0 && Integer.valueOf(val.trim()) < 10) {
                DNI_OCR.setPoint_2(Integer.parseInt(val));
                DNI_OCR.setConf_point_2(true);
            } else {
                DNI_OCR.setPoint_2(-1);
                DNI_OCR.setConf_point_2(false);
            }
        } else {
            DNI_OCR.setPoint_2(-1);
            DNI_OCR.setConf_point_2(false);
        }
        //PUNTO 3
        n_end = value.indexOf("ARG<");
        System.out.println("OCR Back n_end; "+n_end);
        if (n_end >= 0) {
            String val = value.substring(n_end - 1, n_end).trim();
            FechaVecQR = value.substring(n_end - 7, n_end - 1).trim();
            if (Integer.valueOf(val) > 0 && Integer.valueOf(val) < 10) {
                DNI_OCR.setPoint_3(Integer.parseInt(val));
                DNI_OCR.setConf_point_3(true);
            } else {
                DNI_OCR.setPoint_3(-1);
                DNI_OCR.setConf_point_3(false);
            }
        } else {
            DNI_OCR.setPoint_3(-1);
            DNI_OCR.setConf_point_3(false);
        }
        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        double result = DNI_OCR.valor();
        System.out.println("OCR Back result; "+result);
        DNI_OCR.toString();
        //Arbol.insertarNodo(DNI_OCR, result);
        System.out.println("OCR Back DNI_OCR.getDomicilio(): "+DNI_OCR.getDomicilio());
        if (!DNI_OCR.getDomicilio().trim().equals("") || vuelt==2) {
            if (result > 0.75 || ResultOCRBack / count > 0.7) {
                System.out.println("OCR Back 1-1");
                ocrBackReaded = true;
                ResultOCRBack = result;
                count = 0;
                vuelt = 0;
            } else if (vuelt >= 2 && (result > 0.35 || ResultOCRBack / count > 0.3)) {
                System.out.println("OCR Back 1-2");
                ocrBackReaded = true;
                ResultOCRBack = result;
                count = 0;
                vuelt = 0;
            } else {
                System.out.println("OCR Back 1-3");
                count++;
                ResultOCRBack += result;
            }
        } else {
            System.out.println("OCR Back 2");
            count++;
            ResultOCRBack += result;
        }
        /*if(count > 0) {
            System.out.println(vuelt+"/"+count+" RESULTADO BACK: " + result + " AVERAGE: " + ResultOCRBack / count);
        }*/
        return result;
    }

    public double ProcessFrontPDFReaded(String value) {
        value = chararterSpece(value);
        ocr = false;
        pdf = true;
        /*if (DNI_Anterior != this.DocumentoQR && !DNI_Anterior.trim().equals("")) {
            Arbol = null;
            pdfFrontReaded = false;
            Arbol = new ArbolBinario();
        }*/
        if (count == 20) {
            Arbol = null;
            count = 0;
            vuelt++;
            ResultPDFFront = 0;
        }
        DNIFront_PDF DNI_PDF = new DNIFront_PDF();

        DNI_PDF.setValue(value);
        if (value.toUpperCase().indexOf(PDF_APELLIDOS) > 0) {
            DNI_PDF.setConf_apellidos(true);
        } else if (value.toUpperCase().indexOf("APELLIDOS") > 0) {
            DNI_PDF.setConf_apellidos(true);
        } else if (value.toUpperCase().indexOf("APELLIDO") > 0) {
            DNI_PDF.setConf_apellidos(true);
        }
        if (value.toUpperCase().indexOf(PDF_NOMBRES) > 0) {
            DNI_PDF.setConf_nombre(true);
        } else if (value.toUpperCase().indexOf("NOMBRES") > 0) {
            DNI_PDF.setConf_nombre(true);
        } else if (value.toUpperCase().indexOf("NOMBRE") > 0) {
            DNI_PDF.setConf_nombre(true);
        }
        if (value.toUpperCase().indexOf(PDF_N_DOCUMENTO) > 0) {
            int ini = value.toUpperCase().indexOf(PDF_N_DOCUMENTO);
            int fin = value.toUpperCase().indexOf(PDF_NACIONALIDAD);
            if (ini >= 0 && fin > ini) {
                this.DocumentoPDF = value.substring(ini + PDF_N_DOCUMENTO.length(), fin).trim();
                this.DocumentoPDF = this.DocumentoPDF.replace(".", "");
                DNI_PDF.setDocumento(this.DocumentoPDF);
                DNI_Anterior = this.DocumentoPDF;
                DNI_PDF.setConf_documento(true);
            }
        }
        if (value.toUpperCase().indexOf(PDF_SEXO) > 0) {
            DNI_PDF.setConf_sexo(true);
        }
        if (value.toUpperCase().indexOf(PDF_NACIONALIDAD) > 0) {
            DNI_PDF.setConf_nacionalidad(true);
        }
        if (value.toUpperCase().indexOf(PDF_F_EXPEDICION) > 0) {
            DNI_PDF.setConf_fecha_expe(true);
        }
        if (value.toUpperCase().indexOf(PDF_F_VENCIMIENTO) > 0) {
            DNI_PDF.setConf_fecha_venc(true);
        }

        double result = DNI_PDF.valor();
        DNI_PDF.toString();
        if (value.toUpperCase().indexOf("VOTAR") > 0 && value.toUpperCase().indexOf("IDARG") > 0) {
            // System.out.println("ResultPDFFront / count = " + (ResultPDFFront / count));
            if (result > 0.75 || ResultPDFFront / count > 0.7) {
                System.out.println("1");
                pdfFrontReaded = true;
                ResultPDFFront = result;
                count = 0;
                vuelt = 0;
            } else if (vuelt >= 2 && (result >= 0.25 || ResultPDFFront / count >= 0.2)) {
                System.out.println("2");
                pdfFrontReaded = true;
                ResultPDFFront = result;
                count = 0;
                vuelt = 0;
            } else {
                System.out.println("3");
                count++;
                ResultPDFFront += result;
            }
        } else {
            System.out.println("4");
            count++;
            ResultPDFFront += result;
        }

        return result;
    }

    public double ProcessBackPDFReaded(String value_pdf, String value) {
        System.out.println("ProcessBackPDFReaded");
        if (count == 20) {
            Arbol = null;
            count = 0;
            vuelt++;
            ResultPDFBack = 0;
        }
        DNIBack_PDF DNI_PDF = new DNIBack_PDF();
        DNI_PDF.setValue_front(value_pdf);
        System.out.println("PDF value_pdf.toUpperCase().indexOf(ApellidoQR): "+value_pdf.toUpperCase().indexOf(ApellidoQR) );
        if (value_pdf.toUpperCase().indexOf(ApellidoQR) > 0 && this.ApellidoQR != null) {
            DNI_PDF.setApellido(this.ApellidoQR.toUpperCase().replace(" ", "  "));
            DNI_PDF.setConf_apellido(true);
        } else if (value_pdf.toUpperCase().indexOf(ApellidoQR) == 0 && (this.ApellidoQR != null || !this.ApellidoQR.equals(""))) {
            DNI_PDF.setApellido(this.ApellidoQR.toUpperCase().replace(" ", "  "));
            DNI_PDF.setConf_apellido(false);
        }
        System.out.println("PDF value_pdf.toUpperCase().indexOf(NombreQR): "+value_pdf.toUpperCase().indexOf(NombreQR) );
        if (value_pdf.toUpperCase().indexOf(NombreQR) > 0 && this.NombreQR != null) {
            DNI_PDF.setNombre(this.NombreQR.toUpperCase().replace(" ", "  "));
            DNI_PDF.setConf_nombre(true);
        } else if (value_pdf.toUpperCase().indexOf(NombreQR) == 0 && (this.NombreQR != null || !this.NombreQR.equals(""))) {
            DNI_PDF.setNombre(this.NombreQR.toUpperCase().replace(" ", "  "));
            DNI_PDF.setConf_nombre(false);
        }
        System.out.println("PDF DocumentoQR: "+DocumentoQR );
        if (this.DocumentoQR != null) {
            if (value_pdf.toUpperCase().indexOf(DocumentoQR) > 0) {
                DNI_PDF.setDocumento(this.DocumentoQR.toUpperCase().replace(" ", "  "));
                DNI_PDF.setConf_documento(true);
            } else if (value_pdf.toUpperCase().indexOf(DocumentoQR) == 0 && (this.DocumentoQR != null || !this.DocumentoQR.equals(""))) {
                DNI_PDF.setDocumento(this.DocumentoQR.toUpperCase().replace(" ", "  "));
                DNI_PDF.setConf_documento(false);
            }
        }
        System.out.println("PDF EsMasculinoQR: "+EsMasculinoQR );
        if (EsMasculinoQR) {
            DNI_PDF.setSexo("M");
            DNI_PDF.setConf_sexo(true);
        } else {
            DNI_PDF.setDocumento("F");
            DNI_PDF.setConf_sexo(true);
        }
        System.out.println("PDF FechaNacQR: "+FechaNacQR );
        if (this.FechaNacQR != null) {
            DNI_PDF.setFecha_nac(String.valueOf(FechaNacQR));
            DNI_PDF.setConf_fecha_nac(true);
        }
        System.out.println("PDF value.toUpperCase().indexOf(TRAMITE): "+value.toUpperCase().indexOf("TRAMITE") );
        System.out.println("PDF FechaNacQR: "+FechaNacQR );
        if (value.toUpperCase().indexOf("TRAMITE") > 0 && value.toUpperCase().indexOf("OF") > 0) {
            int ini = value.toUpperCase().indexOf("TRAMITE");
            int fin = value.toUpperCase().indexOf("OF");
            if (ini > 0 && fin > ini) {
                DNI_PDF.setN_tramite(value.substring(ini + ("TRAMITE").length(), fin));
                DNI_PDF.setConf_n_tramite(true);
            }
        }
        System.out.println("PDF value.toUpperCase().indexOf(DOMICILIO): "+value.toUpperCase().indexOf("DOMICILIO") );
        System.out.println("PDF  value.toUpperCase().indexOf(FECHA): "+ value.toUpperCase().indexOf("FECHA") );
        if (value.toUpperCase().indexOf("DOMICILIO") > 0 && value.toUpperCase().indexOf("FECHA") > 0) {
            int ini = value.toUpperCase().indexOf("DOMICILIO");
            int fin = value.toUpperCase().indexOf("FECHA");
            if (ini > 0 && fin > ini) {
                DNI_PDF.setN_tramite(value.substring(ini + ("DOMICILIO").length(), fin));
                DNI_PDF.setConf_n_tramite(true);
            }
        }
        System.out.println("PDF value.toUpperCase().indexOf(ARGENTINA): "+value.toUpperCase().indexOf("ARGENTINA") );
        if (value.toUpperCase().indexOf("ARGENTINA") > 0) {
            DNI_PDF.setNacionalidad("ARGENTINA");
            DNI_PDF.setConf_nacionalidad(true);
        }

        double result = DNI_PDF.valor();
        System.out.println("PDF result: "+result );
        DNI_PDF.toString();

        if ((value.toUpperCase().indexOf("DOMICILIO") > 0 || value.toUpperCase().indexOf("FECHA Y LUGAR") > 0 || vuelt == 2)&& !DocumentoQR.equals("")) {
            System.out.println("PDF value.toUpperCase().indexOf(DOMICILIO) : "+  value.toUpperCase().indexOf("DOMICILIO") );
            System.out.println("PDF value.toUpperCase().indexOf(FECHA Y LUGAR) : "+  value.toUpperCase().indexOf("FECHA Y LUGAR") );
            System.out.println("PDF vuelt : "+  vuelt );
            if (result > 0.75 || ResultPDFBack / count > 0.7) {
                System.out.println("PDF 1-1 : ");
                pdfBackReaded = true;
                ResultPDFBack = result;
                count = 0;
                vuelt = 0;
            } else if (vuelt >= 2 && (result > 0.25 || ResultPDFBack / count > 0.2)) {
                System.out.println("PDF 1-2 : ");
                pdfBackReaded = true;
                ResultPDFBack = result;
                count = 0;
                vuelt = 0;
            } else {
                System.out.println("PDF 1-3 : ");
                count++;
                ResultPDFBack += result;
            }
        } else {
            System.out.println("PDF 2 Count: "+count+ " Vuelt:"+vuelt);
            count++;
            ResultPDFFront += result;
        }
        return result;
    }

    public void SetBarcodeReaded() {
        barcodeReaded = true;
    }

    public void SetFaceDetected(Bitmap face) {
        faceDetected = true;
        bitmapFace = face;
    }

    public boolean IsOCRFrontReaded() {
        return ocrFrontReaded;
    }

    public boolean IsOCRBackReaded() {
        return ocrBackReaded;
    }

    public boolean IsPDFFrontReaded() {
        return pdfFrontReaded;
    }

    public boolean IsPDFBackReaded() {
        return pdfBackReaded;
    }

    public boolean IsFilled() {
        return (IsOCRBackReaded() || IsPDFBackReaded()) && IsFaceDetected() && (IsOCRFrontReaded() || IsPDFFrontReaded()) && dniFront != null && dniBack != null;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("<b>Nombre:</b>");
        ret.append(this.NombreQR);
        ret.append("</br>");
        ret.append("<b>Apellido:</b>");
        ret.append(this.ApellidoQR);
        ret.append("</br>");
        ret.append("<b>Número Documento:</b>");
        ret.append(this.DocumentoQR);
        ret.append("</br>");
        ret.append("<b>Fecha de Nacimiento:</b>");
        ret.append(this.FechaNacQR);
        ret.append("</br>");
        ret.append("<b>Género:</b>");
        ret.append(this.EsMasculinoQR ? "Masculino" : "Femenino");
        ret.append("</br>");
        if (!TextUtils.isEmpty(this.DomicilioOCR)) {
            ret.append("<b>Domicilio:</b>");
            ret.append(this.DomicilioOCR);
            ret.append("</br>");
        }
        if (!TextUtils.isEmpty(this.DomicilioOCR)) {
            ret.append("<b>Nacionalidad:</b>");
            ret.append(this.NacionalidadOCR);
            ret.append("</br>");
        }
        return ret.toString();
    }

    public String toHTML() {
        StringBuilder ret = new StringBuilder();
        ret.append("<b>NombreQR:</b>");
        ret.append(this.NombreQR);
        ret.append("</br>");
        ret.append("<b>NombreFrontOCRVerified:</b>");
        ret.append(this.NombreFrontOCRVerified);
        ret.append("</br>");
        ret.append("<b>ApellidoQR:</b>");
        ret.append(this.ApellidoQR);
        ret.append("</br>");
        ret.append("<b>ApellidoFrontOCRVerified:</b>");
        ret.append(this.ApellidoFrontOCRVerified);
        ret.append("</br>");
        ret.append("<b>DocumentoQR:</b>");
        ret.append(this.DocumentoQR);
        ret.append("</br>");
        ret.append("<b>DocumentoFrontOCRVerified:</b>");
        ret.append(this.DocumentoFrontOCRVerified);
        ret.append("</br>");
        ret.append("<b>FechaNacQR:</b>");
        ret.append(this.FechaNacQR);
        ret.append("</br>");
        ret.append("<b>FechaNacimientoFrontOCRVerified:</b>");
        ret.append(this.FechaNacimientoFrontOCRVerified);
        ret.append("</br>");
        ret.append("<b>GéneroOR:</b>");
        ret.append(this.EsMasculinoQR ? "Masculino" : "Femenino");
        ret.append("</br>");
        ret.append("<b>TramiteQR:</b>");
        ret.append(this.TramiteQR);
        ret.append("</br>");
        ret.append("<b>TramiteOCRVerified:</b>");
        ret.append(this.TramiteOCRVerified);
        ret.append("</br>");

        if (!TextUtils.isEmpty(this.DomicilioOCR)) {
            ret.append("<b>Domicilio:</b>");
            ret.append(this.DomicilioOCR);
            ret.append("</br>");
        }
        if (!TextUtils.isEmpty(this.DomicilioOCR)) {
            ret.append("<b>Nacionalidad:</b>");
            ret.append(this.NacionalidadOCR);
            ret.append("</br>");
        }
        return ret.toString();
    }

    public Document GetRequest() {
        Document document = new Document();
        document.tramit = this.TramiteQR;
        document.number = this.DocumentoQR;
        document.lastname = this.ApellidoQR;
        document.gender = this.EsMasculinoQR ? "M" : "F";
        document.expires = this.FechaExpiracionDni;
        document.birth = this.FechaNacQR;
        document.address = this.Domicilio;
        document.name = this.NombreQR;
        document.back = HelperFunctions.encodeTobase64(this.dniBack);
        document.front = HelperFunctions.encodeTobase64(this.dniFront);
        document.valFront = String.valueOf(this.ResultOCRFront);
        document.valBack = String.valueOf(this.ResultOCRBack);
        document.toString();
        return document;
    }

    public int desmintificado_DNI() {
        int vector[] = {7, 3, 1};
        int j = 0;
        int count_1 = 0, count_2 = 0, count_3 = 0, count_4 = 0;

        for (int i = 0; i < DocumentoQR.length(); i++) {
            count_1 += (Integer.parseInt(String.valueOf(DocumentoQR.charAt(i)))) * vector[j];
            j++;
            if (j == 3) {
                j = 0;
            }
        }
        if (PUNTO_1 == count_1 % 10) {
            String FN;
            if (FechaNacQR != null) {
                SimpleDateFormat formato = new SimpleDateFormat("yyMMdd", Locale.getDefault());
                FN = formato.format(FechaNacQR);
            } else {
                FN = FechaNacPDF;
            }

            j = 0;
            for (int i = 0; i < FN.length(); i++) {
                count_2 += (Integer.parseInt(String.valueOf(FN.charAt(i)))) * vector[j];
                j++;
                if (j == 3) {
                    j = 0;
                }
            }
            if (PUNTO_2 == count_2 % 10) {
                j = 0;
                for (int i = 0; i < FechaVecQR.length(); i++) {
                    count_3 += (Integer.parseInt(String.valueOf(FechaVecQR.charAt(i)))) * vector[j];
                    j++;
                    if (j == 3) {
                        j = 0;
                    }
                }
                if (PUNTO_3 == count_3 % 10) {
                    return 1;
                } else {
                    return 2;
                }
               /* if(PUNTO_3==count_3%10) {
                    String val = DocumentoQR+String.valueOf(PUNTO_1)+FN+String.valueOf(PUNTO_2)+FechaVecQR;
                    j = 0;
                    for (int i = 0; i < val.length(); i++){
                        count_4+= (Integer.parseInt(String.valueOf(val.charAt(i))))*vector[j];
                        j++;
                        if(j==3){
                            j=0;
                        }
                    }
                    if(PUNTO_4==count_4%10) {
                        return 1;
                    }else{
                        return 2;
                    }
                }*/
            }
        }
        return 0;
    }

    private static Date parseDateDDMMYYYY(String date) {
        Calendar mCalendarFechaNac = new GregorianCalendar();
        String[] dates = date.split(" ");
        mCalendarFechaNac.set(Calendar.YEAR, Integer.parseInt(dates[2]));
        if (dates[1].equals("ENE")) {
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.JUNE);
        } else if (dates[1].equals("FEB")) {
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.FEBRUARY);
        } else if (dates[1].equals("MAR")) {
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.MARCH);
        } else if (dates[1].equals("APR")) {
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.APRIL);
        } else if (dates[1].equals("MAY")) {
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.MAY);
        } else if (dates[1].equals("JUN")) {
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.JUNE);
        } else if (dates[1].equals("JUL")) {
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.JULY);
        } else if (dates[1].equals("AGO")) {
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.AUGUST);
        } else if (dates[1].equals("SEP")) {
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.SEPTEMBER);
        } else if (dates[1].equals("OCT")) {
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.OCTOBER);
        } else if (dates[1].equals("NOV")) {
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.NOVEMBER);
        } else if (dates[1].equals("DIC")) {
            mCalendarFechaNac.set(Calendar.MONTH, Calendar.DECEMBER);
        }
        mCalendarFechaNac.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dates[0]));
        return mCalendarFechaNac.getTime();

    }

    public boolean isPdf() {
        return pdf;
    }

    public boolean isOcr() {
        return ocr;
    }

    public void setScanGuideColor(String s) {
        scan_guide.setTextColor(Color.parseColor(s));
    }

    public void setScanGuideText(String s){
        scan_guide.setText(s);
    }
}
