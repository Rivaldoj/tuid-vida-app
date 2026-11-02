package com.autenticar.tuid.three;

public class ArbolBinario {
    protected NodoBinario raiz;
    private int cont;
    private double val;

    public ArbolBinario() {
        this.raiz = null;
    }

    public ArbolBinario(DNIFront_OCR dniFront_ocr) {
        this.raiz = new NodoBinario(dniFront_ocr);
    }

    public ArbolBinario(DNIBack_OCR dniBack_ocr) {
        this.raiz = new NodoBinario(dniBack_ocr);
    }

    public ArbolBinario(DNIFront_PDF dniFront_pdf) {
        this.raiz = new NodoBinario(dniFront_pdf);
    }

    public ArbolBinario(DNIBack_PDF dniBack_pdf) {
        this.raiz = new NodoBinario(dniBack_pdf);
    }

    public NodoBinario getRaiz() {
        return raiz;
    }

    public void setRaiz(NodoBinario raiz) {
        this.raiz = raiz;
    }

    private int cal_size(NodoBinario Nodo) {
        if (Nodo == null) {
            return 0;
        } else {
            return 1 + cal_size(Nodo.getIzquierda()) + cal_size(Nodo.getDercha());
        }
    }

    public synchronized void insertarNodoFrontOCR(DNIFront_OCR dniFront_ocr, double valorInsertar) {
        if (raiz == null) {
            raiz = new NodoBinario(valorInsertar, dniFront_ocr); //crea nodo raiz
        } else {
            raiz.add_front_ocr(valorInsertar, dniFront_ocr); // llama al metodo insertar
        }
    }

    public synchronized void insertarNodoFrontPDF(DNIFront_PDF dniFront_pdf, double valorInsertar) {
        if (raiz == null) {
            raiz = new NodoBinario(valorInsertar, dniFront_pdf); //crea nodo raiz
        } else {
            raiz.add_front_pdf(valorInsertar, dniFront_pdf); // llama al metodo insertar
        }
    }

    public synchronized void insertarNodoBackOCR(DNIBack_OCR dniBack_ocr, double valorInsertar) {
        if (raiz == null) {
            raiz = new NodoBinario(valorInsertar, dniBack_ocr); //crea nodo raiz
        } else {
            raiz.add_back_ocr(valorInsertar, dniBack_ocr); // llama al metodo insertar
        }
    }

    public synchronized void insertarNodoBackPDF(DNIBack_PDF dniBack_pdf, double valorInsertar) {
        if (raiz == null) {
            raiz = new NodoBinario(valorInsertar, dniBack_pdf); //crea nodo raiz
        } else {
            raiz.add_back_pdf(valorInsertar, dniBack_pdf); // llama al metodo insertar
        }
    }

    public double averageNodo() {
        return cal_nodo(raiz);
    }

    public double cal_nodo(NodoBinario nodo) {
        if (nodo == null) {
            return 0;
        } else {
            //System.out.println("IZQ: "+nodo.getIzquierda().getValue()+" DER: "+nodo.getDercha().getValue());
            cal_nodo(nodo.getIzquierda());
            cal_size(nodo.getDercha());
            return 0;
        }
    }
}