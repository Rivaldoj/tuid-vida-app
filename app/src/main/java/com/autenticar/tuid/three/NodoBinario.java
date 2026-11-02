package com.autenticar.tuid.three;

import android.os.Build;

import java.util.Optional;

public class NodoBinario {
    private DNIFront_OCR front_ocr;
    private DNIFront_PDF front_pdf;
    private DNIBack_OCR back_ocr;
    private DNIBack_PDF back_pdf;
    private NodoBinario Izquierda;
    private NodoBinario Dercha;
    private double value = 0;

    public NodoBinario(DNIFront_OCR front_ocr) {
        this.front_ocr = front_ocr;
        this.Izquierda = null;
        this.Dercha = null;
        this.value = 0;
    }
    public NodoBinario(DNIBack_OCR back_ocr) {
        this.back_ocr = back_ocr;
        this.Izquierda = null;
        this.Dercha = null;
        this.value = 0;
    }
    public NodoBinario(DNIFront_PDF front_pdf) {
        this.front_pdf = front_pdf;
        this.Izquierda = null;
        this.Dercha = null;
        this.value = 0;
    }
    public NodoBinario(DNIBack_PDF back_pdf) {
        this.back_pdf = back_pdf;
        this.Izquierda = null;
        this.Dercha = null;
        this.value = 0;
    }
    public NodoBinario(double value, DNIFront_OCR front_ocr) {
        this.front_ocr = front_ocr;
        this.Izquierda = null;
        this.Dercha = null;
        this.value = value;
    }
    public NodoBinario(double value, DNIBack_OCR back_ocr) {
        this.back_ocr = back_ocr;
        this.Izquierda = null;
        this.Dercha = null;
        this.value = value;
    }
    public NodoBinario(double value, DNIFront_PDF front_pdf) {
        this.front_pdf = front_pdf;
        this.Izquierda = null;
        this.Dercha = null;
        this.value = value;
    }
    public NodoBinario(double value, DNIBack_PDF back_pdf) {
        this.back_pdf = back_pdf;
        this.Izquierda = null;
        this.Dercha = null;
        this.value = value;
    }

    public NodoBinario(DNIFront_OCR front_ocr, NodoBinario izquierda, NodoBinario dercha, double value) {
        this.front_ocr = front_ocr;
        this.Izquierda = izquierda;
        this.Dercha = dercha;
        this.value = value;
    }
    public NodoBinario(DNIBack_OCR back_ocr, NodoBinario izquierda, NodoBinario dercha, double value) {
        this.back_ocr = back_ocr;
        this.Izquierda = izquierda;
        this.Dercha = dercha;
        this.value = value;
    }
    public NodoBinario(DNIFront_PDF front_pdf, NodoBinario izquierda, NodoBinario dercha, double value) {
        this.front_pdf = front_pdf;
        this.Izquierda = izquierda;
        this.Dercha = dercha;
        this.value = value;
    }
    public NodoBinario(DNIBack_PDF back_pdf, NodoBinario izquierda, NodoBinario dercha, double value) {
        this.back_pdf = back_pdf;
        this.Izquierda = izquierda;
        this.Dercha = dercha;
        this.value = value;
    }
    public void add_front_ocr(double value, DNIFront_OCR front_ocr) {
        if (value < this.value) {
            if (Izquierda != null) {
                Izquierda.add_front_ocr(value, front_ocr);
            } else {
                Izquierda = new NodoBinario(value, front_ocr);
            }
        } else {
            if (Dercha != null) {
                Dercha.add_front_ocr(value, front_ocr);
            } else {
                Dercha = new NodoBinario(value, front_ocr);
            }
        }
    }
    public void add_back_ocr(double value, DNIBack_OCR back_ocr) {
        if (value < this.value) {
            if (Izquierda != null) {
                Izquierda.add_back_ocr(value, back_ocr);
            } else {
                Izquierda = new NodoBinario(value, back_ocr);
            }
        } else {
            if (Dercha != null) {
                Dercha.add_back_ocr(value, back_ocr);
            } else {
                Dercha = new NodoBinario(value, back_ocr);
            }
        }
    }
    public void add_front_pdf(double value, DNIFront_PDF front_pdf) {
        if (value < this.value) {
            if (Izquierda != null) {
                Izquierda.add_front_pdf(value, front_pdf);
            } else {
                Izquierda = new NodoBinario(value, front_pdf);
            }
        } else {
            if (Dercha != null) {
                Dercha.add_front_pdf(value, front_pdf);
            } else {
                Dercha = new NodoBinario(value, front_pdf);
            }
        }
    }
    public void add_back_pdf(double value, DNIBack_PDF back_pdf) {
        if (value < this.value) {
            if (Izquierda != null) {
                Izquierda.add_back_pdf(value, back_pdf);
            } else {
                Izquierda = new NodoBinario(value, back_pdf);
            }
        } else {
            if (Dercha != null) {
                Dercha.add_back_pdf(value, back_pdf);
            } else {
                Dercha = new NodoBinario(value, back_pdf);
            }
        }
    }
    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public DNIFront_OCR getFront_ocr() {
        return front_ocr;
    }

    public void setFront_ocr(DNIFront_OCR front_ocr) {
        this.front_ocr = front_ocr;
    }

    public DNIBack_OCR getBack_ocr() {
        return back_ocr;
    }

    public void setBack_ocr(DNIBack_OCR back_ocr) {
        this.back_ocr = back_ocr;
    }

    public DNIFront_PDF getFront_pdf() {
        return front_pdf;
    }

    public void setFront_pdf(DNIFront_PDF front_pdf) {
        this.front_pdf = front_pdf;
    }

    public DNIBack_PDF getBack_pdf() {
        return back_pdf;
    }

    public void setBack_pdf(DNIBack_PDF back_pdf) {
        this.back_pdf = back_pdf;
    }

    public NodoBinario getIzquierda() {
        return Izquierda;
    }

    public void setIzquierda(NodoBinario izquierda) {
        Izquierda = izquierda;
    }

    public NodoBinario getDercha() {
        return Dercha;
    }

    public void setDercha(NodoBinario dercha) {
        Dercha = dercha;
    }
    public Optional<NodoBinario> find(double value) {
        if (value == this.value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return Optional.of(this);
            }
        } else if (value < this.value) {
            if (this.Izquierda != null) {
                return this.Izquierda.find(value);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    return Optional.empty();
                }
            }
        } else {
            if (this.Dercha != null) {
                return this.Dercha.find(value);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    return Optional.empty();
                }
            }
        }
        return null;
    }

    public void printInOrder() {
        if (Izquierda != null) {
            Izquierda.printInOrder();
        }
        System.out.println(value);
        if (Dercha != null) {
            Dercha.printInOrder();
        }
    }

    public void printPreOrder() {
        System.out.println(value);
        if (Izquierda != null) {
            Izquierda.printPreOrder();
        }
        if (Dercha != null) {
            Dercha.printPreOrder();
        }
    }

    public void printPosOrder() {
        if (Izquierda != null) {
            Izquierda.printPreOrder();
        }
        if (Dercha != null) {
            Dercha.printPreOrder();
        }
        System.out.println(value);
    }
}

