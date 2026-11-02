package com.autenticar.tuid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.autenticar.tuid.three.DNIBack_OCR;
import com.autenticar.tuid.three.DNIBack_PDF;
import com.autenticar.tuid.three.DNIFront_OCR;
import com.autenticar.tuid.three.DNIFront_PDF;

import java.util.ArrayList;
import java.util.List;

public class AdapterListDebugDNIOcrPdf extends BaseAdapter {
    private List<DNIFront_OCR> item_DNIFront_OCR;
    private List<DNIFront_PDF> item_DNIFront_PDF;
    private List<DNIBack_OCR> item_DNIBack_OCR;
    private List<DNIBack_PDF> item_DNIBack_PDF;
    private List<DNIFront_OCR> item_DNIFront_OCR_Copy = new ArrayList<>();
    private List<DNIFront_PDF> item_DNIFront_PDF_Copy = new ArrayList<>();
    private List<DNIBack_OCR> item_DNIBack_OCR_Copy = new ArrayList<>();
    private List<DNIBack_PDF> item_DNIBack_PDF_Copy = new ArrayList<>();
    private Context context;

    public AdapterListDebugDNIOcrPdf(Context context, List<DNIFront_OCR> item_DNIFront_OCR, List<DNIFront_PDF> item_DNIFront_PDF, List<DNIBack_OCR> item_DNIBack_OCR, List<DNIBack_PDF> item_DNIBack_PDF) {
        this.item_DNIFront_OCR = item_DNIFront_OCR;
        this.item_DNIFront_PDF = item_DNIFront_PDF;
        this.item_DNIBack_OCR = item_DNIBack_OCR;
        this.item_DNIBack_PDF = item_DNIBack_PDF;
        this.context = context;
    }

    @Override
    public int getCount() {
        if (item_DNIFront_OCR != null) {
            return item_DNIFront_OCR.size();
        } else if (item_DNIFront_PDF != null) {
            return item_DNIFront_PDF.size();
        } else if (item_DNIBack_OCR != null) {
            return item_DNIBack_OCR.size();
        } else if (item_DNIBack_PDF != null) {
            return item_DNIBack_PDF.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        final ViewHolder_item_DNIFront_OCR viewHolder_item_DNIFront_OCR;
        final ViewHolder_item_DNIFront_PDF viewHolder_item_DNIFront_PDF;
        final ViewHolder_item_DNIBack_OCR viewHolder_item_DNIBack_OCR;
        final ViewHolder_item_DNIBack_PDF viewHolder_item_DNIBack_PDF;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.content_list_debug_dni_ocr_pdf, parent, false);
            if (item_DNIFront_OCR != null) {
                viewHolder_item_DNIFront_OCR = new ViewHolder_item_DNIFront_OCR();
                convertView.setTag(viewHolder_item_DNIFront_OCR);
                viewHolder_item_DNIFront_PDF=null;
                viewHolder_item_DNIBack_OCR=null;
                viewHolder_item_DNIBack_PDF=null;
                viewHolder_item_DNIFront_OCR.id = (TextView) convertView.findViewById(R.id.id_control);
                viewHolder_item_DNIFront_OCR.documento = (TextView) convertView.findViewById(R.id.text_content_1);
                viewHolder_item_DNIFront_OCR.name = (TextView) convertView.findViewById(R.id.text_content_2);
                viewHolder_item_DNIFront_OCR.surname = (TextView) convertView.findViewById(R.id.text_content_3);
                viewHolder_item_DNIFront_OCR.brith = (TextView) convertView.findViewById(R.id.text_content_4);
                viewHolder_item_DNIFront_OCR.nacionality = (TextView) convertView.findViewById(R.id.text_content_5);
                viewHolder_item_DNIFront_OCR.n_tramite = (TextView) convertView.findViewById(R.id.text_content_6);
                viewHolder_item_DNIFront_OCR.ejemplar = (TextView) convertView.findViewById(R.id.text_content_7);
                viewHolder_item_DNIFront_OCR.sex = (TextView) convertView.findViewById(R.id.text_content_8);
                viewHolder_item_DNIFront_OCR.issue = (TextView) convertView.findViewById(R.id.text_content_9);
            } else if (item_DNIFront_PDF != null) {
                viewHolder_item_DNIFront_PDF = new ViewHolder_item_DNIFront_PDF();
                convertView.setTag(viewHolder_item_DNIFront_PDF);
                viewHolder_item_DNIFront_OCR=null;
                viewHolder_item_DNIBack_OCR=null;
                viewHolder_item_DNIBack_PDF=null;
                viewHolder_item_DNIFront_PDF.id = (TextView) convertView.findViewById(R.id.id_control);
                viewHolder_item_DNIFront_PDF.documento = (TextView) convertView.findViewById(R.id.text_content_1);
                viewHolder_item_DNIFront_PDF.value = (TextView) convertView.findViewById(R.id.text_content_2);
            } else if (item_DNIBack_OCR != null) {
                viewHolder_item_DNIBack_OCR = new ViewHolder_item_DNIBack_OCR();
                convertView.setTag(viewHolder_item_DNIBack_OCR);
                viewHolder_item_DNIFront_PDF=null;
                viewHolder_item_DNIFront_OCR=null;
                viewHolder_item_DNIBack_PDF=null;
                viewHolder_item_DNIBack_OCR.id = (TextView) convertView.findViewById(R.id.id_control);
                viewHolder_item_DNIBack_OCR.documento = (TextView) convertView.findViewById(R.id.text_content_1);
                viewHolder_item_DNIBack_OCR.name = (TextView) convertView.findViewById(R.id.text_content_2);
                viewHolder_item_DNIBack_OCR.surname = (TextView) convertView.findViewById(R.id.text_content_3);
                viewHolder_item_DNIBack_OCR.brith = (TextView) convertView.findViewById(R.id.text_content_4);
                viewHolder_item_DNIBack_OCR.nacionality = (TextView) convertView.findViewById(R.id.text_content_5);
                viewHolder_item_DNIBack_OCR.domicilio = (TextView) convertView.findViewById(R.id.text_content_6);
                viewHolder_item_DNIBack_OCR.p1 = (TextView) convertView.findViewById(R.id.text_content_7);
                viewHolder_item_DNIBack_OCR.p2 = (TextView) convertView.findViewById(R.id.text_content_8);
                viewHolder_item_DNIBack_OCR.p3 = (TextView) convertView.findViewById(R.id.text_content_9);
            } else if (item_DNIBack_PDF != null) {
                viewHolder_item_DNIBack_PDF = new ViewHolder_item_DNIBack_PDF();
                convertView.setTag(viewHolder_item_DNIBack_PDF);
                viewHolder_item_DNIFront_PDF=null;
                viewHolder_item_DNIFront_OCR=null;
                viewHolder_item_DNIBack_OCR=null;
                viewHolder_item_DNIBack_PDF.id = (TextView) convertView.findViewById(R.id.id_control);
                viewHolder_item_DNIBack_PDF.documento = (TextView) convertView.findViewById(R.id.text_content_1);
                viewHolder_item_DNIBack_PDF.name = (TextView) convertView.findViewById(R.id.text_content_2);
                viewHolder_item_DNIBack_PDF.apellido = (TextView) convertView.findViewById(R.id.text_content_3);
                viewHolder_item_DNIBack_PDF.brith = (TextView) convertView.findViewById(R.id.text_content_4);
                viewHolder_item_DNIBack_PDF.nacionality = (TextView) convertView.findViewById(R.id.text_content_5);
                viewHolder_item_DNIBack_PDF.domicilio = (TextView) convertView.findViewById(R.id.text_content_6);
                viewHolder_item_DNIBack_PDF.n_tramite = (TextView) convertView.findViewById(R.id.text_content_7);
                viewHolder_item_DNIBack_PDF.sex = (TextView) convertView.findViewById(R.id.text_content_8);
                viewHolder_item_DNIBack_PDF.value = (TextView) convertView.findViewById(R.id.text_content_9);
            }else{
                viewHolder_item_DNIFront_PDF=null;
                viewHolder_item_DNIFront_OCR=null;
                viewHolder_item_DNIBack_OCR=null;
                viewHolder_item_DNIBack_PDF=null;
            }
        } else {
            if (item_DNIFront_OCR != null) {
                viewHolder_item_DNIFront_OCR = (ViewHolder_item_DNIFront_OCR) convertView.getTag();
                viewHolder_item_DNIFront_PDF=null;
                viewHolder_item_DNIBack_OCR=null;
                viewHolder_item_DNIBack_PDF=null;
            } else if (item_DNIFront_PDF != null) {
                viewHolder_item_DNIFront_PDF = (ViewHolder_item_DNIFront_PDF) convertView.getTag();
                viewHolder_item_DNIFront_OCR=null;
                viewHolder_item_DNIBack_OCR=null;
                viewHolder_item_DNIBack_PDF=null;
            } else if (item_DNIBack_OCR != null) {
                viewHolder_item_DNIBack_OCR = (ViewHolder_item_DNIBack_OCR) convertView.getTag();
                viewHolder_item_DNIFront_PDF=null;
                viewHolder_item_DNIFront_OCR=null;
                viewHolder_item_DNIBack_PDF=null;
            } else if (item_DNIBack_PDF != null) {
                viewHolder_item_DNIBack_PDF = (ViewHolder_item_DNIBack_PDF) convertView.getTag();
                viewHolder_item_DNIFront_PDF=null;
                viewHolder_item_DNIFront_OCR=null;
                viewHolder_item_DNIBack_OCR=null;
            }else{
                viewHolder_item_DNIFront_PDF=null;
                viewHolder_item_DNIFront_OCR=null;
                viewHolder_item_DNIBack_OCR=null;
                viewHolder_item_DNIBack_PDF=null;
            }
        }
        if (item_DNIFront_OCR != null) {
            viewHolder_item_DNIFront_OCR.id.setText(i);
            viewHolder_item_DNIFront_OCR.brith.setText(item_DNIFront_OCR.get(i).getDate_birth());
            viewHolder_item_DNIFront_OCR.documento.setText(item_DNIFront_OCR.get(i).getDocument());
            viewHolder_item_DNIFront_OCR.ejemplar.setText(item_DNIFront_OCR.get(i).getEjemplar());
            viewHolder_item_DNIFront_OCR.n_tramite.setText(item_DNIFront_OCR.get(i).getN_tramite());
            viewHolder_item_DNIFront_OCR.issue.setText(item_DNIFront_OCR.get(i).getDate_issue());
            viewHolder_item_DNIFront_OCR.nacionality.setText(item_DNIFront_OCR.get(i).getNationality());
            viewHolder_item_DNIFront_OCR.name.setText(item_DNIFront_OCR.get(i).getName());
            viewHolder_item_DNIFront_OCR.surname.setText(item_DNIFront_OCR.get(i).getName());
            viewHolder_item_DNIFront_OCR.sex.setText(item_DNIFront_OCR.get(i).getName());
        } else if (item_DNIFront_PDF != null) {
            viewHolder_item_DNIFront_PDF.id.setText(i);
            viewHolder_item_DNIFront_PDF.documento.setText(item_DNIFront_PDF.get(i).getDocumento());
            viewHolder_item_DNIFront_PDF.value.setText(item_DNIFront_PDF.get(i).getValue());
        } else if (item_DNIBack_OCR != null) {
            viewHolder_item_DNIBack_OCR.id.setText(i);
            viewHolder_item_DNIBack_OCR.brith.setText(item_DNIBack_OCR.get(i).getDate_birth());
            viewHolder_item_DNIBack_OCR.documento.setText(item_DNIBack_OCR.get(i).getDocument());
            viewHolder_item_DNIBack_OCR.domicilio.setText(item_DNIBack_OCR.get(i).getDomicilio());
            viewHolder_item_DNIBack_OCR.nacionality.setText(item_DNIBack_OCR.get(i).getNacionality());
            viewHolder_item_DNIBack_OCR.name.setText(item_DNIBack_OCR.get(i).getName());
            viewHolder_item_DNIBack_OCR.p1.setText(item_DNIBack_OCR.get(i).getPoint_1());
            viewHolder_item_DNIBack_OCR.p2.setText(item_DNIBack_OCR.get(i).getPoint_2());
            viewHolder_item_DNIBack_OCR.p3.setText(item_DNIBack_OCR.get(i).getPoint_3());
            viewHolder_item_DNIBack_OCR.surname.setText(item_DNIBack_OCR.get(i).getSurname());
        } else if (item_DNIBack_PDF != null) {
            viewHolder_item_DNIBack_PDF.id.setText(i);
            viewHolder_item_DNIBack_PDF.value.setText(item_DNIBack_PDF.get(i).getValue_front());
            viewHolder_item_DNIBack_PDF.apellido.setText(item_DNIBack_PDF.get(i).getApellido());
            viewHolder_item_DNIBack_PDF.name.setText(item_DNIBack_PDF.get(i).getNombre());
            viewHolder_item_DNIBack_PDF.brith.setText(item_DNIBack_PDF.get(i).getFecha_nac());
            viewHolder_item_DNIBack_PDF.domicilio.setText(item_DNIBack_PDF.get(i).getDomicilio());
            viewHolder_item_DNIBack_PDF.documento.setText(item_DNIBack_PDF.get(i).getDocumento());
            viewHolder_item_DNIBack_PDF.n_tramite.setText(item_DNIBack_PDF.get(i).getN_tramite());
            viewHolder_item_DNIBack_PDF.nacionality.setText(item_DNIBack_PDF.get(i).getNacionalidad());
            viewHolder_item_DNIBack_PDF.sex.setText(item_DNIBack_PDF.get(i).getSexo());
        }

        return convertView;
    }

    public static class ViewHolder_item_DNIFront_OCR {
        TextView id;
        TextView documento;
        TextView brith;
        TextView issue;
        TextView ejemplar;
        TextView n_tramite;
        TextView name;
        TextView surname;
        TextView nacionality;
        TextView sex;
        TextView value;
    }

    public static class ViewHolder_item_DNIFront_PDF {
        TextView id;
        TextView documento;
        TextView value;
    }

    public static class ViewHolder_item_DNIBack_OCR {
        TextView id;
        TextView documento;
        TextView brith;
        TextView domicilio;
        TextView nacionality;
        TextView name;
        TextView surname;
        TextView p1;
        TextView p2;
        TextView p3;
        TextView value;
    }

    public static class ViewHolder_item_DNIBack_PDF {
        TextView id;
        TextView documento;
        TextView apellido;
        TextView name;
        TextView domicilio;
        TextView brith;
        TextView n_tramite;
        TextView nacionality;
        TextView sex;
        TextView value;
    }

    public void clear_item_DNIFront_OCR() {
        item_DNIFront_OCR.clear();
    }

    public void clear_item_DNIFront_PDF() {
        item_DNIFront_PDF.clear();
    }

    public void clear_item_DNIBack_OCR() {
        item_DNIBack_OCR.clear();
    }

    public void clear_item_DNIBack_PDF() {
        item_DNIBack_PDF.clear();
    }

    public void filter(String value) {
        if (item_DNIFront_OCR != null) {
            item_DNIFront_OCR.clear();
            if (value.length() == 0) {
                item_DNIFront_OCR.addAll(item_DNIFront_OCR_Copy);
            } else {
                for (DNIFront_OCR item_DNIFront_OCR : item_DNIFront_OCR_Copy) {
                    if (item_DNIFront_OCR.getDocument().contains(value)) {
                        this.item_DNIFront_OCR.add(item_DNIFront_OCR);
                    }
                }
            }
        } else if (item_DNIFront_PDF != null) {
            item_DNIFront_PDF.clear();
            if (value.length() == 0) {
                item_DNIFront_PDF.addAll(item_DNIFront_PDF_Copy);
            } else {
                for (DNIFront_PDF item_DNIFront_PDF : item_DNIFront_PDF_Copy) {
                    if (item_DNIFront_PDF.getValue().contains(value)) {
                        this.item_DNIFront_PDF.add(item_DNIFront_PDF);
                    }
                }
            }
        } else if (item_DNIBack_OCR != null) {
            item_DNIBack_OCR.clear();
            if (value.length() == 0) {
                item_DNIBack_OCR.addAll(item_DNIBack_OCR_Copy);
            } else {
                for (DNIBack_OCR item_DNIBack_OCR : item_DNIBack_OCR_Copy) {
                    if (item_DNIBack_OCR.getDocument().contains(value)) {
                        this.item_DNIBack_OCR.add(item_DNIBack_OCR);
                    }
                }
            }
        } else if (item_DNIBack_PDF != null) {
            item_DNIBack_PDF.clear();
            if (value.length() == 0) {
                item_DNIBack_PDF.addAll(item_DNIBack_PDF_Copy);
            } else {
                for (DNIBack_PDF item_DNIBack_PDF : item_DNIBack_PDF_Copy) {
                    if (item_DNIBack_PDF.getDocumento().contains(value)) {
                        this.item_DNIBack_PDF.add(item_DNIBack_PDF);
                    }
                }
            }
        }
    }
}
