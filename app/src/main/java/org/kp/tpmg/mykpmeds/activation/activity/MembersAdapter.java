package org.kp.tpmg.mykpmeds.activation.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.databinding.ObservableInt;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.controller.FrontController;

import org.kp.tpmg.mykpmeds.activation.model.User;

import java.util.List;

/**
 * Created by M1028309 on 12/6/2017.
 */

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> implements View.OnClickListener,CompoundButton.OnCheckedChangeListener,View.OnLongClickListener {
    private List<User> list;
    private ObservableInt trackCounter;
    private Context mContext;

    public MembersAdapter(List<User> list, ObservableInt trackCounter, Context context){
        this.list=list;
        this.trackCounter=trackCounter;
        this.mContext=context;
        List<User> enabledUsers = FrontController.getInstance(mContext).getAllEnabledUsers();
        for(User user:list){
            if (enabledUsers.contains(user)){
                user.setSelected(true);
                trackCounter.set(trackCounter.get()+1);
            }else{
                user.setSelected(false);
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.row_proxy_userslist,null);
       return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
       // holder.setIsRecyclable(false);
        final User user=list.get(position);
        holder.text.setText(user.getFirstName());
        holder.checkbox.setTag(position);
        holder.checkbox.setTag(R.id.txtViewUserName,holder.text);
        holder.checkbox.setChecked(user.isSelected());
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onClick(View view) {
        CheckBox checkbox = view.findViewById(R.id.chkbxUserNames);
        User user = list.get((Integer) checkbox.getTag());
        checkbox.setChecked(!user.isSelected());
        notifyItemChanged((Integer) checkbox.getTag());
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        User user = list.get((Integer) compoundButton.getTag());
        TextView userTxtView= (TextView) compoundButton.getTag(R.id.txtViewUserName);
        if(isChecked){
            userTxtView.setTextColor(ContextCompat.getColor(mContext, R.color.kp_next_color));
            if(user.isSelected()) return;
            user.setSelected(true);
            trackCounter.set(trackCounter.get()+1);

        }else {
            userTxtView.setTextColor(ContextCompat.getColor(mContext, R.color.kp_med_details_gray));
            if(!user.isSelected()) return;
            user.setSelected(false);
            trackCounter.set(trackCounter.get()-1);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        return true;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView text;
        private CheckBox checkbox;

        public ViewHolder(View view) {
            super(view);
            text = view.findViewById(R.id.txtViewUserName);
            checkbox = view.findViewById(R.id.chkbxUserNames);
            checkbox.setOnCheckedChangeListener(MembersAdapter.this);
            checkbox.setOnLongClickListener(MembersAdapter.this);
            view.setOnClickListener(MembersAdapter.this);
            view.setOnLongClickListener(MembersAdapter.this);
        }
    }
}
