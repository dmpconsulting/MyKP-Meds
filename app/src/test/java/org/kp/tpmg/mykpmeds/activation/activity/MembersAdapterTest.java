package org.kp.tpmg.mykpmeds.activation.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.databinding.ObservableInt;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class MembersAdapterTest {
    private HomeContainerActivity homeContainerActivity;
    private Context context;
    private List<User> list;
    private MembersAdapter membersAdapter;
    public ObservableInt trackCounter = new ObservableInt();

    @Before
    public void setUp() {
        homeContainerActivity = Robolectric.buildActivity(HomeContainerActivity.class).create().get();
        context = homeContainerActivity.getAndroidContext();
        mockData();
        membersAdapter = new MembersAdapter(list, trackCounter, context);

    }

    private void mockData() {
        list = new ArrayList<>();
        User user = new User();
        user.setUserId("123");
        user.setAge("12");
        user.setFirstName("xyz");
        list.add(user);
    }

    @Test
    public void adapterNotBeNull() {
        assertNotNull(membersAdapter);
    }

    @Test
    public void testGetItemCount() {
        assertEquals(list.size(), membersAdapter.getItemCount());
    }

    @Test
    public void testOnCreateViewHolder() {
        assertNotNull(membersAdapter.onCreateViewHolder(new RelativeLayout(context), 0));
    }

    @Test
    public void testOnBindViewHolder() {
        LayoutInflater inflater = (LayoutInflater) homeContainerActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.row_proxy_userslist, null, false);
        MembersAdapter.ViewHolder memberHolder = membersAdapter.new ViewHolder(view);
        membersAdapter.onBindViewHolder(memberHolder, 0);
    }
}
