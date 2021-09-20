package com.montunosoftware.pillpopper.android.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.mymeds.databinding.BatteryOptimizerDetailsCardBinding;
import com.montunosoftware.mymeds.databinding.CurrentReminderHomeCardBinding;
import com.montunosoftware.mymeds.databinding.DiscontinueKphcHomeCardBinding;
import com.montunosoftware.mymeds.databinding.GenericHomeCardBinding;
import com.montunosoftware.mymeds.databinding.LateRemindersHomeCardBinding;
import com.montunosoftware.mymeds.databinding.NewKphcCardBinding;
import com.montunosoftware.mymeds.databinding.RefillReminderOverdueCardBinding;
import com.montunosoftware.mymeds.databinding.UpdateKphcHomeCardBinding;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.ViewClickHandler;

import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.util.List;

/**
 * Created by m1032896 on 11/14/2017.
 */

public class HomeCardAdapter extends RecyclerView.Adapter<HomeCardAdapter.ViewHolder> {

    private static final int HOME_CARD = 0;
    private static final int NEW_KPHC_CARD = 1;
    private static final int UPDATE_KPHC_CARD = 2;
    private static final int LATE_REMINDERS = 3;
    private static final int REFILL_REMINDER = 4;
    private static final int CURRENT_REMINDER = 5;
    private static final int KPHC_DISCONTINUE = 6;
    private static final int BATTERY_OPTIMIZER = 7;
    private static final int TEEN_PROXY_HOME_CARD = 8;
    private static final int GENERIC_HOME_CARD = 9;
    private final Context mContext;

    private int cardWidth, cardMargin = 60;
    private List<HomeCard> cardList;
    private ViewDataBinding binding;
    private int lateReminderUsersCount;

    public HomeCardAdapter(DisplayMetrics metrics, List<HomeCard> cardList, Context context, int lateReminderUserCount) {
        this.cardList = cardList;
        cardWidth = metrics.widthPixels - cardMargin * 2;
        mContext = context;
        this.lateReminderUsersCount = lateReminderUserCount;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = null;
        switch (viewType) {
            case HOME_CARD:
                View viewHomeCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_card, parent, false);
                return new ViewHolder(null, viewHomeCard);
            case NEW_KPHC_CARD:
                return bindKPHCCard(parent, layoutInflater, R.layout.new_kphc_card);
            case UPDATE_KPHC_CARD:
                return bindKPHCCard(parent, layoutInflater, R.layout.update_kphc_home_card);
            case LATE_REMINDERS:
                return bindKPHCCard(parent, layoutInflater, R.layout.late_reminders_home_card);
            case REFILL_REMINDER:
                return bindRefillReminderCard(parent, layoutInflater, R.layout.refill_reminder_overdue_card);
            case CURRENT_REMINDER:
                return bindRefillReminderCard(parent, layoutInflater, R.layout.current_reminder_home_card);
            case KPHC_DISCONTINUE:
                return bindRefillReminderCard(parent, layoutInflater, R.layout.discontinue_kphc_home_card);
            case BATTERY_OPTIMIZER:
                return bindKPHCCard(parent, layoutInflater, R.layout.battery_optimizer_contract_card);
            case TEEN_PROXY_HOME_CARD:
                return bindTeenProxyHomeCard(parent,layoutInflater,R.layout.teen_proxy_home_card);
            case GENERIC_HOME_CARD:
                return bindGenericHomeCard(parent,layoutInflater,R.layout.generic_home_card);

        }
        return new ViewHolder(null, view);
    }

    private ViewHolder bindRefillReminderCard(ViewGroup parent, LayoutInflater layoutInflater, int layoutId) {
        binding = DataBindingUtil.inflate(layoutInflater, layoutId, parent, false);
        binding.executePendingBindings();
        return new ViewHolder(binding, null);
    }
    private ViewHolder bindTeenProxyHomeCard(ViewGroup parent, LayoutInflater layoutInflater, int layoutId) {
        binding = DataBindingUtil.inflate(layoutInflater, layoutId, parent, false);
        binding.executePendingBindings();
        return new ViewHolder(binding, null);
    }
    private ViewHolder bindGenericHomeCard(ViewGroup parent, LayoutInflater layoutInflater, int layoutId) {
        binding = DataBindingUtil.inflate(layoutInflater, layoutId, parent, false);
        binding.executePendingBindings();
        return new ViewHolder(binding, null);
    }

    @NonNull
    private ViewHolder bindKPHCCard(ViewGroup parent, LayoutInflater layoutInflater, int layoutId) {
        binding = DataBindingUtil.inflate(layoutInflater, layoutId, parent, false);
        binding.executePendingBindings();
        return new ViewHolder(binding, null);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.setIsRecyclable(false);
        int currentItemWidth = cardWidth;
        if(!cardList.isEmpty())
            cardList.get(position).setContext(mContext);

        setPaddings(holder, position, currentItemWidth);

        bindData(holder, position);

        holder.root.setOnClickListener(view -> {
            try {
                RunTimeData.getInstance().setAppInExpandedCard(true);
                ActivityOptionsCompat options = null;
                ViewClickHandler.preventMultiClick(view);
                Intent intent = new Intent(view.getContext(), HomeCardDetailActivity.class);
                if(null!=cardList && !cardList.isEmpty()){
                    if (null != cardList.get(position).getTitle()) {
                        logGA(cardList.get(position).getTitle());
                        intent.putExtra("card", (Parcelable) cardList.get(position));
                        Pair<View, String> banner = Pair.create(view.findViewById(R.id.home_card_image), ViewCompat.getTransitionName(view.findViewById(R.id.home_card_image)));
                        Pair<View, String> title = Pair.create(view.findViewById(R.id.home_card_title), ViewCompat.getTransitionName(view.findViewById(R.id.home_card_title)));
                        Pair<View, String> description = Pair.create(view.findViewById(R.id.home_card_description), ViewCompat.getTransitionName(view.findViewById(R.id.home_card_description)));
                        options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) view.getContext(), banner, title, description);
                    } else {
                        if (cardList.get(position) instanceof KPHCCards) {
                            if (((KPHCCards) cardList.get(position)).hasNewKPHCUser()) {
                                FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                                        FireBaseConstants.Event.VIEW_CARD_HOME,
                                        FireBaseConstants.ParamName.CARD_TYPE,
                                        FireBaseConstants.ParamValue.NEW_MEDS);
                                FireBaseAnalyticsTracker.getInstance().logScreenEvent(mContext, FireBaseConstants.ScreenEvent.SCREEN_NEW_MEDS);

                            } else {

                                FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                                        FireBaseConstants.Event.VIEW_CARD_HOME,
                                        FireBaseConstants.ParamName.CARD_TYPE,
                                        FireBaseConstants.ParamValue.UPDATED_MEDS);
                                FireBaseAnalyticsTracker.getInstance().logScreenEvent(mContext, FireBaseConstants.ScreenEvent.SCREEN_UPDATED_MEDS);
                            }
                            User user = ((KPHCCards) cardList.get(position)).getUser();
                            //Toast.makeText(view.getContext(),":::"+user.getFirstName()+"::::"+((KPHCCards)cardList.get(position)).hasNewKPHCUser(),Toast.LENGTH_LONG).show();
                            intent.putExtra("Id", user.getUserId());
                            intent.putExtra("card", (Parcelable) cardList.get(position));
                            intent.putExtra("NewKPHC", ((KPHCCards) cardList.get(position)).hasNewKPHCUser());
                            intent.putExtra("UserName", user.getFirstName());
                            Pair<View, String> title = Pair.create(view.findViewById(R.id.card_title), ViewCompat.getTransitionName(view.findViewById(R.id.card_title)));
                            Pair<View, String> subtitle = Pair.create(view.findViewById(R.id.card_subtitle), ViewCompat.getTransitionName(view.findViewById(R.id.card_subtitle)));
                            Pair<View, String> username = Pair.create(view.findViewById(R.id.kphc_card_member_name), ViewCompat.getTransitionName(view.findViewById(R.id.kphc_card_member_name)));
                            options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) view.getContext(), title, subtitle, username);
                        } else if (cardList.get(position) instanceof LateRemindersHomeCard) {
                            FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                                    FireBaseConstants.Event.VIEW_CARD_HOME,
                                    FireBaseConstants.ParamName.CARD_TYPE,
                                    FireBaseConstants.ParamValue.LATE_REMINDER);
                            FireBaseAnalyticsTracker.getInstance().logScreenEvent(mContext, FireBaseConstants.ScreenEvent.SCREEN_LATE_REMINDER);
                            String userid = ((LateRemindersHomeCard) cardList.get(position)).getUserID();
                            intent.putExtra("Id", userid);
                            intent.putExtra("card", (Parcelable) cardList.get(position));
                            intent.putExtra("numberOfLateReminderUsers", lateReminderUsersCount);
                            Pair<View, String> title = Pair.create(view.findViewById(R.id.card_title), ViewCompat.getTransitionName(view.findViewById(R.id.card_title)));
                            Pair<View, String> username = Pair.create(view.findViewById(R.id.kphc_card_member_name), ViewCompat.getTransitionName(view.findViewById(R.id.kphc_card_member_name)));
                            Pair<View, String> skip = Pair.create(view.findViewById(R.id.skipped_all), ViewCompat.getTransitionName(view.findViewById(R.id.skipped_all)));
                            Pair<View, String> taken = Pair.create(view.findViewById(R.id.taken_all), ViewCompat.getTransitionName(view.findViewById(R.id.taken_all)));
                            options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) view.getContext(), title, username, skip, taken);
                        } else if (cardList.get(position) instanceof RefillReminderOverdueCard) {

                            FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                                    FireBaseConstants.Event.VIEW_CARD_HOME,
                                    FireBaseConstants.ParamName.CARD_TYPE,
                                    FireBaseConstants.ParamValue.REFILL_REMINDER);
                            FireBaseAnalyticsTracker.getInstance().logScreenEvent(mContext, FireBaseConstants.ScreenEvent.SCREEN_REFILL_REMINDER);
                            StringBuilder sb = new StringBuilder();
                            intent.putExtra("card", (Parcelable) cardList.get(position));
                            Pair<View, String> title = Pair.create(view.findViewById(R.id.card_title), ViewCompat.getTransitionName(view.findViewById(R.id.card_title)));
                            Pair<View, String> subtitle = Pair.create(view.findViewById(R.id.refill_overdue_date), ViewCompat.getTransitionName(view.findViewById(R.id.refill_overdue_date)));
                            options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) view.getContext(), title, subtitle);
                        } else if (cardList.get(position) instanceof CurrentReminderCard) {

                            FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                                    FireBaseConstants.Event.VIEW_CARD_HOME,
                                    FireBaseConstants.ParamName.CARD_TYPE,
                                    FireBaseConstants.ParamValue.CURRENT_REMINDER);
                            FireBaseAnalyticsTracker.getInstance().logScreenEvent(mContext, FireBaseConstants.ScreenEvent.SCREEN_CURRENT_REMINDER);

                            intent.putExtra("card", (Parcelable) cardList.get(position));
                            Pair<View, String> time = Pair.create(view.findViewById(R.id.card_title_time), ViewCompat.getTransitionName(view.findViewById(R.id.card_title_time)));
                            Pair<View, String> date = Pair.create(view.findViewById(R.id.card_title_date), ViewCompat.getTransitionName(view.findViewById(R.id.card_title_date)));
                            Pair<View, String> names = Pair.create(view.findViewById(R.id.card_subtitle_user), ViewCompat.getTransitionName(view.findViewById(R.id.card_subtitle_user)));
                            Pair<View, String> subtitle = Pair.create(view.findViewById(R.id.card_subtitle_reminder), ViewCompat.getTransitionName(view.findViewById(R.id.card_subtitle_reminder)));
                            options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) view.getContext(), time, date, names, subtitle);
                        }
                        else if(cardList.get(position) instanceof KPHCDiscontinueCard){

                            FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                                    FireBaseConstants.Event.VIEW_CARD_HOME,
                                    FireBaseConstants.ParamName.CARD_TYPE,
                                    FireBaseConstants.ParamValue.DISCONTINUED_MEDS);
                            FireBaseAnalyticsTracker.getInstance().logScreenEvent(mContext, FireBaseConstants.ScreenEvent.SCREEN_DISCONTINUED_MEDS);

                            intent.putExtra("card", (Parcelable) cardList.get(position));
                            Pair<View, String> title = Pair.create(view.findViewById(R.id.card_title), ViewCompat.getTransitionName(view.findViewById(R.id.card_title)));
                            Pair<View, String> subTitle = Pair.create(view.findViewById(R.id.card_subtitle), ViewCompat.getTransitionName(view.findViewById(R.id.card_subtitle)));
                            options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) view.getContext(), title, subTitle);
                        }else if(cardList.get(position) instanceof TeenProxyHomeCard){
                            FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                                    FireBaseConstants.Event.VIEW_CARD_HOME,
                                    FireBaseConstants.ParamName.CARD_TYPE,
                                    FireBaseConstants.ParamValue.TEEN_PROXY_CARD);
                            FireBaseAnalyticsTracker.getInstance().logScreenEvent(mContext, FireBaseConstants.ScreenEvent.SCREEN_TEEN_PROXY_CARD);
                            intent.putExtra("card", (Parcelable) cardList.get(position));
                            Pair<View, String> title = Pair.create(view.findViewById(R.id.card_title), ViewCompat.getTransitionName(view.findViewById(R.id.card_title)));
                            Pair<View, String> subTitle = Pair.create(view.findViewById(R.id.card_subtitle), ViewCompat.getTransitionName(view.findViewById(R.id.card_subtitle)));
                            options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) view.getContext(), title, subTitle);
                        }else if(cardList.get(position) instanceof GenericHomeCard){
                            intent.putExtra("card", (Parcelable) cardList.get(position));
                            Pair<View, String> title = Pair.create(view.findViewById(R.id.card_title), ViewCompat.getTransitionName(view.findViewById(R.id.card_title)));
                            Pair<View, String> subTitle = Pair.create(view.findViewById(R.id.card_subtitle), ViewCompat.getTransitionName(view.findViewById(R.id.card_subtitle)));
                            options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) view.getContext(), title, subTitle);
                        }
                        else if(cardList.get(position) instanceof BatteryOptimizerInfoCard){
                            FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                                    FireBaseConstants.Event.VIEW_CARD_HOME,
                                    FireBaseConstants.ParamName.CARD_TYPE,
                                    FireBaseConstants.ParamValue.BATTERY_OPTIMIZATION);
                            FireBaseAnalyticsTracker.getInstance().logScreenEvent(mContext, FireBaseConstants.ScreenEvent.SCREEN_BATTERY_OPTIMIZATION);
                            intent.putExtra("card", (Parcelable) cardList.get(position));
                            Pair<View, String> title = Pair.create(view.findViewById(R.id.card_title), ViewCompat.getTransitionName(view.findViewById(R.id.card_title)));
                            Pair<View, String> subTitle = Pair.create(view.findViewById(R.id.card_subtitle), ViewCompat.getTransitionName(view.findViewById(R.id.card_subtitle)));
                            options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) view.getContext(), title, subTitle);
                        }
                    }
                    if (null != view.getContext() && null != cardList && null != cardList.get(position)) {
                        ((Activity) view.getContext()).startActivityForResult(intent, cardList.get(position).getRequestCode(), options.toBundle());
                    }
                }

            } catch (Exception e){
                PillpopperLog.exception("Exception while Card Navigation - " + e.getMessage());
            }
        });

    }

    private void logGA(String title) {

        if(mContext.getString(R.string.card_title_view_medication).equalsIgnoreCase(title)){
            FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                    FireBaseConstants.Event.VIEW_CARD_HOME,
                    FireBaseConstants.ParamName.CARD_TYPE,
                    FireBaseConstants.ParamValue.VIEW_YOUR_MEDICATIONS_CARD);
            FireBaseAnalyticsTracker.getInstance().logScreenEvent(mContext, FireBaseConstants.ScreenEvent.SCREEN_VIEW_YOUR_MEDICATIONS);
        }else if(mContext.getString(R.string.card_title_refill_from_phone).equalsIgnoreCase(title)){
            FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                    FireBaseConstants.Event.VIEW_CARD_HOME,
                    FireBaseConstants.ParamName.CARD_TYPE,
                    FireBaseConstants.ParamValue.REFILL_FROM_YOUR_PHONE_CARD);
            FireBaseAnalyticsTracker.getInstance().logScreenEvent(mContext, FireBaseConstants.ScreenEvent.SCREEN_REFILL_FROM_YOUR_PHONE);

        }else if(mContext.getString(R.string.card_title_manage_from_phone).equalsIgnoreCase(title)){
            FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                    FireBaseConstants.Event.VIEW_CARD_HOME,
                    FireBaseConstants.ParamName.CARD_TYPE,
                    FireBaseConstants.ParamValue.MANAGE_YOUR_FAMILY_MEDICATION_CARD);
            FireBaseAnalyticsTracker.getInstance().logScreenEvent(mContext, FireBaseConstants.ScreenEvent.SCREEN_MANAGE_YOUR_FAMILY_MEMBERS);

        }else if(mContext.getString(R.string.card_title_setup_reminder).equalsIgnoreCase(title)){
            FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                    FireBaseConstants.Event.VIEW_CARD_HOME,
                    FireBaseConstants.ParamName.CARD_TYPE,
                    FireBaseConstants.ParamValue.SET_UP_REMINDERS_CARD);
            FireBaseAnalyticsTracker.getInstance().logScreenEvent(mContext, FireBaseConstants.ScreenEvent.SCREEN_SET_UP_REMINDERS);

        }else if(mContext.getString(R.string.card_title_view_reminder).equalsIgnoreCase(title)){
            FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                    FireBaseConstants.Event.VIEW_CARD_HOME,
                    FireBaseConstants.ParamName.CARD_TYPE,
                    FireBaseConstants.ParamValue.VIEW_REMINDERS_CARD);
            FireBaseAnalyticsTracker.getInstance().logScreenEvent(mContext, FireBaseConstants.ScreenEvent.SCREEN_VIEW_REMINDERS);
        }
    }

    private void bindData(ViewHolder holder, int position) {
        try {
            if (null != cardList.get(position).getTitle()) {
                holder.txtTitle.setText(cardList.get(position).getTitle());
                holder.banner.setImageResource(cardList.get(position).getBanner());
                holder.txtDescription.setText(cardList.get(position).getDescription());
                holder.root.setContentDescription(cardList.get(position).getContentDescription(holder.root));
            } else {
                if (holder.getBinding() instanceof NewKphcCardBinding) {
                    ((NewKphcCardBinding) holder.getBinding()).setHandler((KPHCCards) cardList.get(position));
                } else if (holder.getBinding() instanceof UpdateKphcHomeCardBinding) {
                    ((UpdateKphcHomeCardBinding) holder.getBinding()).setHandler((KPHCCards) cardList.get(position));
                } else if (holder.getBinding() instanceof LateRemindersHomeCardBinding) {
                    ((LateRemindersHomeCardBinding) holder.getBinding()).setHandler((LateRemindersHomeCard) cardList.get(position));
                } else if (holder.getBinding() instanceof RefillReminderOverdueCardBinding) {
                    ((RefillReminderOverdueCardBinding) holder.getBinding()).setHandler((RefillReminderOverdueCard) cardList.get(position));
                } else if (holder.getBinding() instanceof CurrentReminderHomeCardBinding) {
                    ((CurrentReminderHomeCardBinding) holder.getBinding()).setHandler((CurrentReminderCard) cardList.get(position));
                } else if (holder.getBinding() instanceof DiscontinueKphcHomeCardBinding) {
                    ((DiscontinueKphcHomeCardBinding) holder.getBinding()).setHandler((KPHCDiscontinueCard) cardList.get(position));
                } else if (holder.getBinding() instanceof BatteryOptimizerDetailsCardBinding){
                    ((BatteryOptimizerDetailsCardBinding) holder.getBinding()).setHandler((BatteryOptimizerInfoCard) cardList.get(position));
                }
                else if(holder.getBinding() instanceof GenericHomeCardBinding) {
                    ((GenericHomeCardBinding) holder.getBinding()).setActivity((GenericHomeCard) cardList.get(position));
                }
            }
        }catch (Exception e){
            LoggerUtils.exception("Exception bindData: " , e);
        }
    }

    private void setPaddings(ViewHolder holder, int position, int currentItemWidth) {
        if (position == 0) {
            currentItemWidth += cardMargin;
            holder.root.setPadding(cardMargin / 4, 0, 0, 0);
        } else if (position == getItemCount() - 1) {
            currentItemWidth += cardMargin;
            holder.root.setPadding(0, 0, cardMargin / 4, 0);
        }
        holder.root.setLayoutParams(new ViewGroup.LayoutParams(currentItemWidth, holder.root.getLayoutParams().height));
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }


    @Override
    public int getItemViewType(int position) {

        switch (cardList.get(position).getCardView()) {
            case R.layout.home_card:
                return HOME_CARD;
            case R.layout.new_kphc_card:
                return NEW_KPHC_CARD;
            case R.layout.update_kphc_home_card:
                return UPDATE_KPHC_CARD;
            case R.layout.late_reminders_home_card:
                return LATE_REMINDERS;
            case R.layout.refill_reminder_overdue_card:
                return REFILL_REMINDER;
            case R.layout.current_reminder_home_card:
                return CURRENT_REMINDER;
            case R.layout.discontinue_kphc_home_card:
                return KPHC_DISCONTINUE;
            case R.layout.battery_optimizer_contract_card:
                return BATTERY_OPTIMIZER;
            case R.layout.teen_proxy_home_card:
                return TEEN_PROXY_HOME_CARD;
            case R.layout.generic_home_card:
                return GENERIC_HOME_CARD;
        }

        return -1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDescription;
        ImageView banner;
        View root;

        public ViewHolder(ViewDataBinding dataBinding, View itemView) {
            super(dataBinding == null ? itemView : dataBinding.getRoot());
            if (dataBinding == null)
                root = itemView;
            else {
                binding = dataBinding;
                root = dataBinding.getRoot();
            }
            txtTitle = root.findViewById(R.id.home_card_title);
            txtDescription = root.findViewById(R.id.home_card_description);
            banner = root.findViewById(R.id.home_card_image);
        }

        public ViewDataBinding getBinding() {
            return binding;
        }
    }

    public void updateCards(List<HomeCard> cardList) {
        this.cardList = cardList;
    }
}
