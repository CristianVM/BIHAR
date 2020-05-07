package com.example.bihar.view.activities;

public class Practicas {


}

class ViewHolderPracticas extends RecyclerView.ViewHolder {

    public CardView cardViewPractica;
    public TextView IDPractica;
    public TextView lugarPractica;
    public TextView empresaPractica;
    public TextView descripcionPractica;

    public ViewHolderPracticas(View itemView) {
        super(itemView);
        cardViewPractica = itemView.findViewById(R.id.cardViewPractica);
        IDPractica = itemView.findViewById(R.id.IDPractica);
        lugarPractica = itemView.findViewById(R.id.lugarPractica);
        empresaPractica = itemView.findViewById(R.id.empresaPractica);
        descripcionPractica = itemView.findViewById(R.id.descripcionPractica);
    }
}

class AdapterPracticas extends RecyclerView.Adapter<ViewHolderPracticas> {

    private ArrayList<String> losIDs;
    private ArrayList<String> losLugares;
    private ArrayList<String> lasEmpresas;
    private ArrayList<String> lasDescripciones;
    private Context elContexto;

    public AdapterPracticas(ArrayList<String> IDs, ArrayList<String> lugares, ArrayList<String> empresas, ArrayList<String> descripciones, Context contexto) {
        losIDs = IDs;
        losLugares = lugares;
        lasEmpresas = empresas;
        lasDescripciones = descripciones;
        elContexto = contexto;
    }

    public ViewHolderPracticas onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View elLayoutFila = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista_practicas_elemento, null);
        ViewHolderPracticas viewHolderPracticas = new ViewHolderPracticas(elLayoutFila);
        return viewHolderPracticas;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderPracticas holder, int position) {
        holder.IDPractica.setText(losIDs.get(position));
        holder.lugarPractica.setText(losLugares.get(position));
        holder.empresaPractica.setText(lasEmpresas.get(position));
        holder.descripcionPractica.setText(lasDescripciones.get(position));
        holder.cardViewPractica.setOnClickListener(v -> {
            Intent intent = new Intent(elContexto, PracticaInformacion.class);
            intent.putExtra("IDPractica", holder.IDPractica.getText());
            elContexto.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return losIDs.size();
    }
}
