package br.com.fiap.pizza;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerAdapter;

/**
 * Created by VyMajoriss on 5/13/2016.
 */
public class MyRecycleFirebaseAdapter extends FirebaseRecyclerAdapter<JsonNode, MyRecycleFirebaseAdapter.MyViewHolder> {


    public MyRecycleFirebaseAdapter(Class<JsonNode> modelClass, int modelLayout, Class<MyViewHolder> viewHolderClass, Firebase ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    public void populateViewHolder(MyViewHolder myViewHolder, JsonNode node, int position) {


    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView nameText;

        public MyViewHolder(View itemView) {
            super(itemView);
            nameText = (TextView) itemView.findViewById(android.R.id.text1);
            messageText = (TextView) itemView.findViewById(android.R.id.text2);
        }
    }


}
