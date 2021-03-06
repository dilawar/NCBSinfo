package com.rohitsuratekar.NCBSinfo.fragments.settings;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.rohitsuratekar.NCBSinfo.BaseActivity;
import com.rohitsuratekar.NCBSinfo.BuildConfig;
import com.rohitsuratekar.NCBSinfo.R;
import com.rohitsuratekar.NCBSinfo.activities.SettingsInfo;
import com.rohitsuratekar.NCBSinfo.activities.developer.DevelopersOptions;
import com.rohitsuratekar.NCBSinfo.common.AppPrefs;
import com.rohitsuratekar.NCBSinfo.common.CommonTasks;
import com.rohitsuratekar.NCBSinfo.common.Helper;
import com.rohitsuratekar.NCBSinfo.database.RouteData;
import com.rohitsuratekar.NCBSinfo.fragments.home.RemoteConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.string.cancel;
import static android.R.string.ok;

/**
 * A simple {@link Fragment} subclass.
 */
public class Settings extends Fragment implements SettingsActions, SettingsAdapter.OnSelect {

    @BindView(R.id.st_recycler)
    RecyclerView recyclerView;

    private SettingsAdapter adapter;
    private List<SettingsModel> modelList = new ArrayList<>();
    private AppPrefs prefs;
    private SettingsViewModel viewModel;
    private List<RouteData> routeDataList;
    private OnSettingAction settingAction;
    private int developerCount = 0;
    private FirebaseRemoteConfig mf;

    public Settings() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.settings, container, false);
        ButterKnife.bind(this, rootView);
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        mf = FirebaseRemoteConfig.getInstance();
        prefs = new AppPrefs(getContext());
        adapter = new SettingsAdapter(modelList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setUpItems();
        getRoutes();
        viewModel.loadRoute(getContext());

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            settingAction = (OnSettingAction) context;
        } catch (Exception e) {
            Toast.makeText(context, "attach fragment interface!", Toast.LENGTH_LONG).show();
        }

    }

    private void getRoutes() {
        viewModel.getAllRoutes().observe(this, new Observer<List<RouteData>>() {
            @Override
            public void onChanged(@Nullable List<RouteData> routeData) {
                if (routeData != null) {
                    routeDataList = routeData;
                    setUpItems();
                }
            }
        });

        viewModel.getResetDone().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean != null) {
                    if (aBoolean) {
                        settingAction.settingAction(ACTION_RESET_ROUTE, null);
                    }
                }
            }
        });
    }

    private void setUpItems() {
        modelList.clear();

        for (Object[] obj : items) {
            if ((int) obj[0] == VIEW_HEADER) {
                if ((int) obj[1] != R.string.settings_egg) {
                    modelList.add(new SettingsModel(getString((int) obj[1])));
                } else {
                    if (prefs.isEggActive()) {
                        modelList.add(new SettingsModel(getString((int) obj[1])));
                    }
                }
            } else if ((int) obj[0] == VIEW_ITEM) {

                SettingsModel mod = new SettingsModel(VIEW_ITEM);
                mod.setTitle(getString((int) obj[1]));
                if ((int) obj[2] != 0) {
                    mod.setDescription(getString((int) obj[2]));
                }
                mod.setIcon((int) obj[3]);
                mod.setAction((int) obj[4]);
                switch (mod.getAction()) {
                    case ACTION_TRANSPORT_UPDATE:
                        mod.setDisabled(true);
                        break;
                    case ACTION_APP_DETAILS:
                        int versionCode = BuildConfig.VERSION_CODE;
                        String versionName = BuildConfig.VERSION_NAME;
                        mod.setDescription(getString(R.string.settings_app_details_text, versionCode, versionName));
                        break;
                    case ACTION_NOTIFICATIONS:
                        String notifications;
                        if (prefs.areNotificationsAllowed()) {
                            notifications = "Allowed";
                            mod.setIcon(R.drawable.icon_notifications_active);
                        } else {
                            notifications = "Disabled";
                            mod.setIcon(R.drawable.icon_notifications_disabled);
                        }
                        mod.setDescription(getString(R.string.settings_notification_details, notifications));
                        break;
                    case ACTION_DEFAULT_ROUTE:
                        if (routeDataList == null) {
                            mod.setDisabled(true);
                        } else {
                            if (prefs.isDefaultRouteSet()) {
                                mod.setDescription(getString(R.string.settings_default_route_name,
                                        prefs.getFavoriteOrigin().toUpperCase(),
                                        prefs.getFavoriteDestination().toUpperCase(),
                                        prefs.getFavoriteType()));
                            }
                            mod.setDisabled(false);
                        }
                        break;


                }
                if (mod.getAction() == ACTION_EGG1 || mod.getAction() == ACTION_EGG2) {
                    if (prefs.isEggActive()) {
                        modelList.add(mod);
                    }
                } else if (mod.getAction() == ACTION_DEVELOPERS_OPTIONS) {
                    if (prefs.isDeveloperActive()) {
                        modelList.add(mod);
                    }
                } else if (mod.getAction() == ACTION_REMOVE_EGG) {
                    if (prefs.isEggActive()) {
                        modelList.add(mod);
                        modelList.add(new SettingsModel(VIEW_LINE));
                    }
                } else if (mod.getAction() == ACTION_ANNOUNCE) {

                    try {
                        SimpleDateFormat f = new SimpleDateFormat(Helper.FORMAT_REMOTE_TIME, Locale.ENGLISH);
                        Date start = f.parse(mf.getString(RemoteConstants.NOTICE_START_TIME));
                        Date end = f.parse(mf.getString(RemoteConstants.NOTICE_END_TIME));
                        boolean isIt = start.before(new Date()) && end.after(new Date()) && mf.getBoolean(RemoteConstants.IS_NOTICE_ACTIVE);
                        if (isIt || mf.getString(RemoteConstants.ADMIN_CODE).equals(prefs.getAdminCode())) {
                            modelList.add(mod);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else {
                    modelList.add(mod);
                }
            } else {
                modelList.add(new SettingsModel(VIEW_LINE));
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void showTransport() {
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        b.setTitle("Select default route");

        String[] types = new String[routeDataList.size()];

        for (RouteData s : routeDataList) {
            types[routeDataList.indexOf(s)] = s.getOrigin().toUpperCase() + "-" + s.getDestination().toUpperCase() + " " + s.getType();
            if (s.getFavorite().equals("yes")) {
                types[routeDataList.indexOf(s)] = types[routeDataList.indexOf(s)] + " (default)";
            }
        }
        b.setPositiveButton(cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        b.setItems(types, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                prefs.setFavoriteOrigin(routeDataList.get(which).getOrigin());
                prefs.setFavoriteDestination(routeDataList.get(which).getDestination());
                prefs.setFavoriteType(routeDataList.get(which).getType());
                CommonTasks.sendFavoriteRoute(getContext(), routeDataList.get(which).getRouteID());
                prefs.defaultRouteSet();
                for (int i = 0; i < routeDataList.size(); i++) {
                    if (i == which) {
                        routeDataList.get(i).setFavorite("yes");
                    } else {
                        routeDataList.get(i).setFavorite("no");
                    }
                }
                dialog.dismiss();
                setUpItems();
                Snackbar snackbar = Snackbar.make(recyclerView, "Default route changed!", BaseTransientBottomBar.LENGTH_SHORT);
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green));
                snackbar.show();
            }

        });
        b.show();
    }

    private void showDialog(String title, String message) {

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.announcement_dialog, null);
        final TextView m1 = dialogView.findViewById(R.id.an_dialog_text);
        final SpannableString s = new SpannableString(message);
        Linkify.addLinks(s, Linkify.WEB_URLS);
        m1.setText(s);
        m1.setMovementMethod(LinkMovementMethod.getInstance());
        new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.icon_announcement)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    Object[][] items = {
            {VIEW_HEADER, R.string.settings_header_notice},
            {VIEW_ITEM, R.string.settings_data_notice, R.string.settings_data_notice_details, R.drawable.icon_information, ACTION_NOTICE},
            {VIEW_LINE},
            {VIEW_HEADER, R.string.settings_header_general},
            {VIEW_ITEM, R.string.settings_notification, 0, R.drawable.icon_notifications_active, ACTION_NOTIFICATIONS},
            {VIEW_ITEM, R.string.settings_announcement, R.string.settings_announcement_details, R.drawable.icon_announcement, ACTION_ANNOUNCE},
            {VIEW_LINE},
            {VIEW_HEADER, R.string.settings_header_transport},
            {VIEW_ITEM, R.string.settings_default_route, R.string.settings_default_transport_details, R.drawable.icon_bus, ACTION_DEFAULT_ROUTE},
            {VIEW_ITEM, R.string.settings_reset_transport, R.string.settings_reset_transport_details, R.drawable.icon_restore, ACTION_RESET_ROUTE},
            {VIEW_ITEM, R.string.settings_transport_last, R.string.settings_transport_last_update, 0, ACTION_TRANSPORT_UPDATE},
            {VIEW_LINE},
            {VIEW_HEADER, R.string.settings_header_legal},
            {VIEW_ITEM, R.string.settings_terms, R.string.settings_terms_details, R.drawable.icon_copyright, ACTION_TERMS},
            {VIEW_ITEM, R.string.settings_privacy, R.string.settings_privacy_details, 0, ACTION_PRIVACY},
            {VIEW_ITEM, R.string.settings_acknow, R.string.settings_acknow_details, R.drawable.icon_fav, ACTION_ACK},
            {VIEW_LINE},
            {VIEW_HEADER, R.string.settings_egg},
            {VIEW_ITEM, R.string.settings_ron, R.string.settings_ron_details, 0, ACTION_EGG1},
            {VIEW_ITEM, R.string.settings_hermione, R.string.settings_hermione_details, 0, ACTION_EGG2},
            {VIEW_ITEM, R.string.settings_remove_egg, R.string.settings_remove_egg_details, 0, ACTION_REMOVE_EGG},
            {VIEW_HEADER, R.string.settings_header_about},
            {VIEW_ITEM, R.string.settings_about, R.string.settings_about_details, R.drawable.icon_star, ACTION_ABOUT_US},
            {VIEW_ITEM, R.string.settings_github, R.string.settings_github_details, R.drawable.icon_github, ACTION_GITHUB},
            {VIEW_ITEM, R.string.settings_feedback, R.string.settings_feedback_details, R.drawable.icon_feedback, ACTION_FEEDBACK},
            {VIEW_ITEM, R.string.settings_developers, R.string.settings_developers_details, R.drawable.icon_developer, ACTION_DEVELOPERS_OPTIONS},
            {VIEW_ITEM, R.string.settings_app_details, 0, R.mipmap.ic_launcher_round, ACTION_APP_DETAILS}
    };

    private void showInfo(String action) {
        settingAction.settingAction(ACTION_INFO, action);
    }


    @Override
    public void clicked(int position) {
        if (!modelList.get(position).isDisabled()) {

            switch (modelList.get(position).getAction()) {
                case ACTION_DEFAULT_ROUTE:
                    showTransport();
                    break;
                case ACTION_NOTIFICATIONS:
                    prefs.toggleNotifications();
                    setUpItems();
                    break;
                case ACTION_TERMS:
                    showInfo(SettingsInfo.TERMS);
                    break;
                case ACTION_PRIVACY:
                    showInfo(SettingsInfo.PRIVACY);
                    break;
                case ACTION_ACK:
                    showInfo(SettingsInfo.ACK);
                    break;
                case ACTION_ABOUT_US:
                    showInfo(SettingsInfo.ABOUT);
                    break;
                case ACTION_GITHUB:
                    String currentUrl = "https://github.com/NCBSinfo/NCBSinfo";
                    Intent i2 = new Intent(Intent.ACTION_VIEW);
                    i2.setData(Uri.parse(currentUrl));
                    startActivity(i2);
                    break;
                case ACTION_RESET_ROUTE:
                    new AlertDialog.Builder(getContext())
                            .setTitle(getString(R.string.are_you_sure))
                            .setMessage(getString(R.string.settings_reset_warning))
                            .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    viewModel.startRest(getContext());
                                }
                            }).setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).show();
                    break;
                case ACTION_FEEDBACK:
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/html");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"contact@secretbiology.com", "ncbs.mod@gmail.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback on NCBSinfo v" + BuildConfig.VERSION_CODE);
                    startActivity(Intent.createChooser(intent, "Send Email"));
                    break;
                case ACTION_EGG1:
                    showInfo(SettingsInfo.EGG_ASSAY1);
                    break;
                case ACTION_EGG2:
                    showInfo(SettingsInfo.EGG_ASSAY2);
                    break;
                case ACTION_NOTICE:
                    showInfo(SettingsInfo.NOTICE);
                    break;
                case ACTION_REMOVE_EGG:
                    prefs.removeEggs();
                    Toast.makeText(getContext(), "As you wish! Apparate...", Toast.LENGTH_LONG).show();
                    settingAction.settingAction(ACTION_REMOVE_EGG, null);
                    break;
                case ACTION_APP_DETAILS:
                    developerCount++;
                    if (prefs.isDeveloperActive()) {
                        Toast.makeText(getContext(), "You are already a developer", Toast.LENGTH_SHORT).show();

                    } else {
                        if (developerCount > 4) {
                            Toast.makeText(getContext(), 8 - developerCount + " clicks to developers options", Toast.LENGTH_SHORT).show();
                        }
                        if (developerCount == 7) {
                            prefs.setDeveloperActive();
                            Intent id = new Intent(getActivity(), BaseActivity.class);
                            id.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(id);
                            Toast.makeText(getContext(), "You are now a developer", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case ACTION_DEVELOPERS_OPTIONS:
                    startActivity(new Intent(getActivity(), DevelopersOptions.class));
                    break;
                case ACTION_ANNOUNCE:
                    showDialog(getString(R.string.settings_announcement), mf.getString(RemoteConstants.SPECIAL_NOTICE));
                    break;
            }
        }
    }

    public interface OnSettingAction {
        void settingAction(int actionID, String params);
    }

}
