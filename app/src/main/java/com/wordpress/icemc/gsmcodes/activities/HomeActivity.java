package com.wordpress.icemc.gsmcodes.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.wordpress.icemc.gsmcodes.R;
import com.wordpress.icemc.gsmcodes.adapter.CodeAdapter;
import com.wordpress.icemc.gsmcodes.dao.CodeDao;
import com.wordpress.icemc.gsmcodes.dao.OperatorsDao;
import com.wordpress.icemc.gsmcodes.dao.TagMapDao;
import com.wordpress.icemc.gsmcodes.dao.TagsDao;
import com.wordpress.icemc.gsmcodes.listeners.CodeAdapterListener;
import com.wordpress.icemc.gsmcodes.listeners.ContactButtonClickListener;
import com.wordpress.icemc.gsmcodes.listeners.ContactPickListener;
import com.wordpress.icemc.gsmcodes.listeners.GetCodesListener;
import com.wordpress.icemc.gsmcodes.listeners.SaveCodesListener;
import com.wordpress.icemc.gsmcodes.listeners.TagSelectionListener;
import com.wordpress.icemc.gsmcodes.model.Code;
import com.wordpress.icemc.gsmcodes.model.CodeItem;
import com.wordpress.icemc.gsmcodes.model.CodeLoadMessage;
import com.wordpress.icemc.gsmcodes.model.DefaultTags;
import com.wordpress.icemc.gsmcodes.model.Tag;
import com.wordpress.icemc.gsmcodes.model.TagMap;
import com.wordpress.icemc.gsmcodes.providers.CodeProviderAPI;
import com.wordpress.icemc.gsmcodes.providers.TagProviderAPI;
import com.wordpress.icemc.gsmcodes.utilities.ApplicationConstants;
import com.wordpress.icemc.gsmcodes.utilities.GSMCodeUtils;
import com.wordpress.icemc.gsmcodes.utilities.JsonUtils;
import com.wordpress.icemc.gsmcodes.utilities.LocaleHelper;
import com.wordpress.icemc.gsmcodes.views.ActivateCodeBottomSheetDialog;
import com.wordpress.icemc.gsmcodes.views.CreateCodeBottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, CodeAdapterListener, SaveCodesListener,
        ContactButtonClickListener,  LoaderManager.LoaderCallbacks<Cursor>, CreateCodeBottomSheetDialog.AddCodeListener {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private static final int MY_PERMISSION_REQUEST_CALL = 1;
    private static final int MY_PERMISSION_PICK_CONTACT = 2;
    private static final int PICK_CONTACT = 3;
    private static final int CODE_LOADER = 0;
    private static final int CODE_SEARCH_LOADER = 1;
    private static final int TAG_LOADER = 2;

    private static final String CODE_TAG = "tag";
    private static final String QUERY = "query";



    private List<CodeItem> codes = new ArrayList<>();
    private RecyclerView recyclerView;
    private CodeAdapter adapter;
    private String operatorName;
    private String tag;
    private ProgressDialog pDialog;
    private Context context = this;

    private int editTextIndex = -1;
    private static int codeIndex = -1;

    //Fields for PICK Contact operation
    private ContactPickListener contactPickListener;
    private Cursor contactCursor = null;
    private String phoneNumber = "";
    private List<String> allNumbers = new ArrayList<>();
    private boolean contactPicked = false;
    private String searchQuery = "";

    private enum CurrentViewState {
        NORMAL, SEARCH
    }

    private CurrentViewState currentViewState;

    //Fields for BottomSheet
    private ActivateCodeBottomSheetDialog activateCodeBottomSheetDialog;
    private CreateCodeBottomSheetDialog createCodeBottomSheetDialog;
    private TagSelectionListener tagSelectionListener;

    //boolean flags
    //private static boolean isOtherTags = true;
    //private static boolean isFavourite = false;
    private boolean isFirstSearchIntent = true;
    private boolean isFirstNormalStart =  true;

    //Fields for filter button;
    private int currentTag = 0;
    private ArrayList<String> tags = new ArrayList<>();

    //App language
    private String appLanguage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        operatorName = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(ApplicationConstants.LAST_OPERATOR_USED, "");
        appLanguage = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(ApplicationConstants.APP_LANGUAGE, "en");
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        toolbar.setTitle(operatorName);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new CodeAdapter(this, codes, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

//        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
//        itemTouchHelper.attachToRecyclerView(recyclerView);
        //Init the tag cursor loader
        getSupportLoaderManager().initLoader(TAG_LOADER, null, this).forceLoad();
        handleIntent(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!drawer.isDrawerOpen(GravityCompat.START) && Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            Intent intent = new Intent();
            setIntent(intent);
            handleIntent(intent);
        } else{
            drawer.openDrawer(GravityCompat.START);
        }
    }

    private void tonggleSelection(int position) {
        boolean isFavourite = codes.get(position).getCode().isFavourite();
        Code.Builder builder = new Code.Builder(codes.get(position).getCode())
                .isFavourite(!isFavourite);

        Code newCode = builder.build();
        codes.get(position).setCode(newCode);
        //TODO update the database to contain the new code values;
        int total = new CodeDao(this).updateCode(newCode);
        Log.d(TAG, "Updated: " + total + " items");
        adapter.toggleSelection(position);
        //Check if filter is set to favourite and remove if code is marked as not favourite
        if(!newCode.isFavourite() && tags.get(currentTag).equals(
                DefaultTags.FAVORITE.toString())) {
            codes.remove(position);
            adapter.notifyItemRemoved(position);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchQuery = intent.getStringExtra(SearchManager.QUERY);
            Bundle bundle = new Bundle();
            bundle.putString(QUERY, searchQuery);
            if(isFirstSearchIntent) {
                isFirstSearchIntent = false;
                currentViewState = CurrentViewState.SEARCH;
                getSupportLoaderManager().initLoader(CODE_SEARCH_LOADER, bundle, this).forceLoad();
            } else {
                //Reset the loader with the new parameters
                getSupportLoaderManager().restartLoader(CODE_SEARCH_LOADER, bundle, this);
            }

        } else {
            final Bundle bundle = new Bundle();
            if(isFirstNormalStart) {
                isFirstNormalStart = false;
                currentViewState = CurrentViewState.NORMAL;
                bundle.putString(CODE_TAG, DefaultTags.ALL.toString());
                getSupportLoaderManager().initLoader(CODE_LOADER, bundle, this).forceLoad();
            } else {
                bundle.putString(CODE_TAG, tags.get(currentTag));
                getSupportLoaderManager().restartLoader(CODE_LOADER, bundle, this);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_home_menu, menu);
        //super.onCreateOptionsMenu(menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem search = menu.findItem(R.id.action_search);
        final MenuItem create = menu.findItem(R.id.action_create);
        final MenuItem sort = menu.findItem(R.id.action_sort);
        SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(search);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        MenuItemCompat.setOnActionExpandListener(search, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                sort.setVisible(false);
                create.setVisible(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
                    Intent intent = new Intent();
                    setIntent(intent);
                    handleIntent(intent);
                }
                sort.setVisible(true);
                create.setVisible(true);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_language) {
            final String currentLanguage = appLanguage;
            int index = -1;

            for(int i = 0; i < ApplicationConstants.TRANSLATIONS_AVAILABLE.length; i++) {
                if((currentLanguage).equals(ApplicationConstants.TRANSLATIONS_AVAILABLE[i])) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                //Default to english
                index = 0;
            }

            final int currentIndex = index;
            new AlertDialog.Builder(HomeActivity.this, R.style.MaterialDialog)
                    .setTitle(R.string.select_language)
                    .setSingleChoiceItems(ApplicationConstants.LANGUAGES, currentIndex, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (appLanguage.equals(ApplicationConstants.TRANSLATIONS_AVAILABLE[which])) {
                                dialog.dismiss();
                            } else {
                                PreferenceManager.getDefaultSharedPreferences(context).edit().putString(ApplicationConstants.APP_LANGUAGE, ApplicationConstants.TRANSLATIONS_AVAILABLE[which]).apply();
                                appLanguage = ApplicationConstants.TRANSLATIONS_AVAILABLE[which];
                                new LocaleHelper().updateLocale(context, appLanguage);
                                new LoadDataIntoDatabase(HomeActivity.this, CodeLoadMessage.CHANGE_LANGUAGE).execute();
                                dialog.dismiss();
                            }
                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show();
            return true;
        } else if (id == R.id.action_search) {
            return true;
        } else if(id == R.id.action_create) {
            createCodeBottomSheetDialog = new CreateCodeBottomSheetDialog(this, this);
            tagSelectionListener = createCodeBottomSheetDialog;
            createCodeBottomSheetDialog.show();
            return true;
        } else if(id == R.id.action_sort) {
            final Bundle bundle = new Bundle();

            new AlertDialog.Builder(HomeActivity.this, R.style.MaterialDialog)
                    .setTitle(getString(R.string.filter_by_tag))
                    .setSingleChoiceItems(tags.toArray(new String[tags.size()]), currentTag, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(currentTag == i) {
                                dialogInterface.dismiss();
                            } else {
                                currentTag = i;
                                if (tags.get(i).equals(DefaultTags.ALL.toString())) {
                                    bundle.putString(CODE_TAG, DefaultTags.ALL.toString());
                                } else if (tags.get(i).equals(DefaultTags.FAVORITE.toString())) {
                                    bundle.putString(CODE_TAG, DefaultTags.FAVORITE.toString());
                                } else {
                                    bundle.putString(CODE_TAG, tags.get(i));
                                }
                                getSupportLoaderManager().restartLoader(CODE_LOADER, bundle, HomeActivity.this);
                                dialogInterface.dismiss();
                            }
                        }
                    })
                    .setNegativeButton(getString(R.string.dialog_cancel), null)
                    .show();
            return true;
        } else if(id == R.id.action_licence) {
            new AlertDialog.Builder(this, R.style.MaterialDialog)
                    .setTitle(R.string.action_licence)
                    .setMessage(R.string.license)
                    .setPositiveButton(R.string.dialog_ok, null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_operators) {
            startActivity(new Intent(this, OperatorsActivity.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity1.class));
        } else if (id == R.id.nav_quit) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
        } else if (id == R.id.nav_help) {
            Intent intent = new Intent();
            intent.setData(Uri.parse(ApplicationConstants.HELP_URL));
            intent.setAction(Intent.ACTION_VIEW);
            startActivity(intent);
            //startActivity(new Intent(this, Help.class));
        }else if (id == R.id.nav_send) {
            //send me an email
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse(ApplicationConstants.EMAIL));
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.code_email));
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(HomeActivity.this, getString(R.string.about_not_found_email), Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_share) {
            //TODO share the app
            //Toast.makeText(this, getString(R.string.not_yet_implemented), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, ApplicationConstants.SHARE_CONTENT);
            intent.setType("text/plain");
            startActivity(intent);
        } else if (id == R.id.nav_reload) {
            new AlertDialog.Builder(context, R.style.MaterialDialog)
                    .setTitle(R.string.reload_codes_title)
                    .setMessage(getString(R.string.reload_codes_msg))
                    .setIcon(R.drawable.ic_get_app_black_24dp)
                    .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new LoadDataIntoDatabase(HomeActivity.this, CodeLoadMessage.RECOVER).execute();
                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_CONTACT:
                    contactCursor = null;
                    phoneNumber = "";
                    allNumbers = new ArrayList<>();
                    int phoneIdx  = 0;
                    try {
                        Uri result = data.getData();
                        contactCursor = getContentResolver().query(result, null, null, null, null);
                        phoneIdx = contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        if (contactCursor.moveToFirst()) {
                            while (contactCursor.isAfterLast() == false) {
                                phoneNumber = contactCursor.getString(phoneIdx);

                                allNumbers.add(phoneNumber);
                                contactCursor.moveToNext();
                            }
                        } else {
                            Log.d(TAG, " Not Picked");
                            //TODO notify no Contact
                        }
                    } catch (Exception e) {
                        //TODO notify this error
                        e.printStackTrace();
                    } finally {
                        if(contactCursor != null) {
                            contactCursor.close();
                            contactCursor = null;
                        }
                    }


                    contactPicked = phoneNumber.length() != 0;

                    break;
            }
        } else {
            return;
        }
    }

    private View.OnClickListener fabTopOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };


    @Override
    public void onIconClicked(int position) {
        tonggleSelection(position);
    }

    @Override
    public void onCodeRowClicked(int position) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CALL:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        codeIndex != -1) {
                    onPhoneClicked(codeIndex);
                } else {
                    //TODO show a message to inform the user he cant run the USSD code
                    codeIndex = -1;
                }

                return;
            case MY_PERMISSION_PICK_CONTACT:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        editTextIndex != -1) {
                    onContactButtonClicked(editTextIndex, false);
                } else {
                    //TODO show a message to inform the user he cant pick a contact
                    editTextIndex = -1;
                }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getSupportLoaderManager().getLoader(CODE_LOADER) != null) {
            getSupportLoaderManager().getLoader(CODE_LOADER).startLoading();
        } else {
            getSupportLoaderManager().getLoader(CODE_SEARCH_LOADER).startLoading();
        }

        if(getSupportLoaderManager().getLoader(TAG_LOADER) != null) {
            getSupportLoaderManager().getLoader(TAG_LOADER).startLoading();
        }

        if (contactPicked) {
            final CharSequence[] items = allNumbers.toArray(new String[allNumbers.size()]);
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setTitle("Choose a number");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String selectedNumber = items[which].toString();
                    selectedNumber = selectedNumber.replace("-", "");
                    contactPickListener.onContactPicked(editTextIndex, selectedNumber);
                }
            });

            AlertDialog alert = builder.create();
            if (allNumbers.size() > 1){
                alert.show();
            } else {
                String selectedNumber = phoneNumber.toString();
                selectedNumber = selectedNumber.replace("-", "");
                selectedNumber = selectedNumber.replace(" ", "");
                Log.d(TAG, " Picked: " + selectedNumber);
                contactPickListener.onContactPicked(editTextIndex, selectedNumber);
            }
        }

    }

    private boolean deleteCode = true;

    @Override
    public void onPhoneClicked(final int position) {
        codeIndex = position;
        //Check if CALL_PHONE permission is granted or not;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            //request user to accept permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    MY_PERMISSION_REQUEST_CALL);
        } else {
            final Code code = codes.get(position).getCode();
            final CodeItem codeItem = codes.get(position);

            activateCodeBottomSheetDialog = new ActivateCodeBottomSheetDialog(this, code, this);
            contactPickListener = activateCodeBottomSheetDialog;
            activateCodeBottomSheetDialog.getBtn_dialog_bottom_sheet_ok()
                    .setOnClickListener(okDialogClickListener);
            activateCodeBottomSheetDialog.getBtn_dialog_bottom_sheet_cancel()
                    .setOnClickListener(cancelDialogClickListener);
            activateCodeBottomSheetDialog.getShareButton()
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activateCodeBottomSheetDialog.dismiss();
                            Code code = activateCodeBottomSheetDialog.getCode();
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT, code.getOperator() + "\n"
                                    + code.getName() + "\n"
                                    + code.getDescription() + "\n"
                                    + GSMCodeUtils.setCodeStringUsingInputFields(
                                    code.getCode(), code.getInputFields()));
                            intent.setType("text/plain");

                            try {
                                context.startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
            activateCodeBottomSheetDialog.getDeleteButton()
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activateCodeBottomSheetDialog.dismiss();
                            final Code code = codes.get(position).getCode();
                            codes.remove(position);
                            adapter.notifyItemRemoved(position);
                            //adapter.remove(position);
                            final TagMapDao tagMapDao = new TagMapDao(context);
                            final CodeDao codeDao =  new CodeDao(context);

                            //TagMap backup list in case the user decides to undo the delete
                            //List<TagMap> tagMapList = tagMapDao.getTagMapFromCursor(tagMapDao.getTagMapCursorForCode(code));
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Snackbar.make(recyclerView, getString(R.string.code_deleted), Snackbar.LENGTH_LONG)
                                            .setAction(getString(R.string.undo), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    deleteCode = false;
                                                    codes.add(position, codeItem);
                                                    adapter.notifyItemInserted(position);
                                                }
                                            }).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                        @Override
                                        public void onDismissed(Snackbar transientBottomBar, int event) {
                                            super.onDismissed(transientBottomBar, event);
                                            if(deleteCode) {
                                                tagMapDao.deleteTagMapsForCode(code);
                                                codeDao.deleteCode(code);
                                                adapter.notifyDataSetChanged();
                                            } else {
                                                deleteCode = true;
                                            }
                                        }
                                    }).show();
                                }
                            }).run();
                        }
                    });
            activateCodeBottomSheetDialog.show();
        }
    }


    @Override
    public void onContactButtonClicked(int editTextIndex, boolean isInitiatedByContactButton) {
        this.editTextIndex = editTextIndex;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED && isInitiatedByContactButton
                ) {
            //request user to accept permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSION_PICK_CONTACT);

        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED && !isInitiatedByContactButton
                ) {
            return;
        } else {
            try {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = CodeProviderAPI.CodeColumns.NAME + " ASC";
        switch (id) {
            case CODE_LOADER:
                if (args.getString(CODE_TAG).equals(DefaultTags.ALL.toString())) {
                    String selection = CodeProviderAPI.CodeColumns.OPERATOR_NAME + "=?";
                    String[] selectionArgs = {operatorName};
                    return new CursorLoader(this, CodeProviderAPI.CodeColumns.CONTENT_URI, null, selection, selectionArgs, sortOrder);
                } else if(args.getString(CODE_TAG).equals(DefaultTags.FAVORITE.toString())) {
                    String selection = CodeProviderAPI.CodeColumns.OPERATOR_NAME + "=? " + "AND " + CodeProviderAPI.CodeColumns.IS_FAVOURITE + "=?";
                    String[] selectionArgs = {operatorName, Boolean.toString(true)};
                    return new CursorLoader(this, CodeProviderAPI.CodeColumns.CONTENT_URI, null, selection, selectionArgs, sortOrder);
                } else {
                    //Make sure the selection argument holds the name of the tag for which the codes are to be collected from the content provider
                    return new CursorLoader(this, ApplicationConstants.CODES_WITH_TAG_CONTENT_URI, null, args.getString(CODE_TAG), null, null);
                }
            case CODE_SEARCH_LOADER:
                String selection = args.getString(QUERY);
                List<String> selectionArgs = new ArrayList<>();
                for(String s: tags) {
                    if(selection.toLowerCase().contains(s.toLowerCase())) {
                        selectionArgs.add(s);
                    }
                }
//                String selection = CodeProviderAPI.CodeColumns.OPERATOR_NAME + "=?" + " AND " + CodeProviderAPI.CodeColumns.NAME + " LIKE ?";
//                String[] selectionArgs = {operatorName, "%" +args.getString(QUERY) + "%"};

                //SelectionArgs must contain the list of valid tags/categories from the query string while selection is actually the query string.
                //This logic is used to run a special query that searches data from the database: see GSMCodeContentProvider query method at case SEARCH
                return new CursorLoader(this, ApplicationConstants.SEARCH_CONTENT_URI, null, selection, selectionArgs.toArray(new String[selectionArgs.size()]), sortOrder);

            case TAG_LOADER:
                String tagSelection = TagProviderAPI.TagColumns.OPERATOR_NAME + "=? " + "OR " + TagProviderAPI.TagColumns.OPERATOR_NAME + "=?";
                String[] tagSelectionArgs = {operatorName, ApplicationConstants.TAG_ALL_OPERATORS_SENTINEL};
                return new CursorLoader(this, TagProviderAPI.TagColumns.CONTENT_URI, null, tagSelection, tagSelectionArgs, sortOrder);
                //return new CursorLoader(this, TagProviderAPI.TagColumns.CONTENT_URI, null, null, null, sortOrder);

        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == TAG_LOADER) {
            tags.clear();
            tags.add(DefaultTags.ALL.toString());
            tags.add(DefaultTags.FAVORITE.toString());
            TagsDao dao = new TagsDao(this);
            for(Tag t: dao.getTagsFromCursor(data)) {
                Log.d(TAG, "Loaded tag: " + t.getName());
                tags.add(t.getName());
            }
        } else if (loader.getId() == CODE_LOADER || loader.getId() == CODE_SEARCH_LOADER){
            codes.clear();
            CodeDao dao = new CodeDao(this);
            codes.addAll(dao.getCodeItemsFromCursor(data));
            adapter.notifyDataSetChanged();
        } else {
            throw new IllegalArgumentException("Loader with id: " + loader.getId() + " not handled in loadFinished.");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    View.OnClickListener
            okDialogClickListener = new View.OnClickListener() {
        @SuppressWarnings("MissingPermission")
        @Override
        public void onClick(View v) {
            Intent callIntent = new Intent(Intent.ACTION_CALL, GSMCodeUtils.ussdToCallableUri(GSMCodeUtils.setCodeStringUsingEditTexts(
                    activateCodeBottomSheetDialog.getCode().getCode(), activateCodeBottomSheetDialog.getEditTexts())));
            codeIndex = -1;

            //NOT AN ERROR THERE IS NO WAY A USER WILL REACH THIS POINT WITHOUT GRANTING THE CALL_PHONE PERMISSION
            startActivity(callIntent);
            activateCodeBottomSheetDialog.dismiss();

        }
    };

    View.OnClickListener cancelDialogClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            codeIndex = -1;
            activateCodeBottomSheetDialog.dismiss();
        }
    };

    @Override
    public void onOKButtonClick() {
        boolean allFine = false;
        //Do a little check on the code the user entered
        //TODO add a more rigorous check since an invalid code may cause the app to crash when used
        String usercode = createCodeBottomSheetDialog.getCode();
        if(usercode.equals("")) {
            Toast.makeText(this, getString(R.string.error_empty_code_field), Toast.LENGTH_SHORT).show();
            return;
        } else if(!usercode.startsWith("#") && !usercode.startsWith("*")) {
            Toast.makeText(this, getString(R.string.error_invalid_code_start_char), Toast.LENGTH_SHORT).show();
            return;
        } else if(!usercode.endsWith("#")) {
            Toast.makeText(this, getString(R.string.error_invalid_code_end_char), Toast.LENGTH_SHORT).show();
            return;
        } else if(createCodeBottomSheetDialog.getName().equals("")) {
            Toast.makeText(this, getString(R.string.error_empty_name_field), Toast.LENGTH_SHORT).show();
            return;
        }else if(createCodeBottomSheetDialog.getDescription().equals("")) {
            Toast.makeText(this, getString(R.string.error_empty_description_field), Toast.LENGTH_SHORT).show();
            return;
        }

        //All checks passed with success

        Code code = new Code.Builder()
                .name(createCodeBottomSheetDialog.getName())
                .code(createCodeBottomSheetDialog.getCode())
                .description(createCodeBottomSheetDialog.getDescription())
                .operator(operatorName)
                .isFavourite(true)
                .inputFields(null)
                .build();
        CodeDao dao = new CodeDao(this);
        Uri uri = dao.saveCode(dao.getValuesFromObject(code));

        //Check if code was saved successfully before saving the tagMaps
        if(uri != null && createCodeBottomSheetDialog.getTags().size() > 0) {
            TagMapDao tagMapDao = new TagMapDao(this);
            String codeId = code.getCode();
            String operatorId = code.getOperator();
            for(String tag : createCodeBottomSheetDialog.getTags()) {
                tagMapDao.saveTagMap(tagMapDao.getValuesFromObject(new
                        TagMap.Builder().codeId(codeId).tagId(tag).operatorId(operatorId).build()));
            }
            Snackbar.make(recyclerView, getString(R.string.code_saved), Snackbar.LENGTH_LONG).show();
            //Toast.makeText(this, getString(R.string.code_saved), Toast.LENGTH_LONG).show();
        } else {
            Snackbar.make(recyclerView, getString(R.string.error_code_not_saved_already_present), Snackbar.LENGTH_LONG).show();
            //Toast.makeText(this, getString(R.string.error_code_not_saved_already_present), Toast.LENGTH_LONG).show();
        }

        if(currentViewState == CurrentViewState.NORMAL) {
            //Reload data from database
            Bundle bundle = new Bundle();
            bundle.putString(CODE_TAG, tags.get(currentTag));
            getSupportLoaderManager().restartLoader(CODE_LOADER, bundle, this);
        }
        createCodeBottomSheetDialog.dismiss();
    }

    @Override
    public void onTagsButtonClick(List<String> tagList) {
        final ArrayList<String> realTags = new ArrayList<>(tags);
        realTags.remove(1);
        realTags.remove(0);
        final String[] multiChoiceItems = realTags.toArray(new String[realTags.size()]);
        final boolean[] checkedItems = new boolean[multiChoiceItems.length];
        final List<String> currentTags = new ArrayList<>(tagList);

        for (int i = 0; i < multiChoiceItems.length; i++) {
            checkedItems[i] = false;
        }

        //set Selected items
        if (currentTags != null && currentTags.size() > 0){
            for(String s: currentTags) {
                checkedItems[realTags.indexOf(s)] = true;
            }
        }

        new AlertDialog.Builder(this, R.style.MaterialDialog)
                .setTitle(getString(R.string.select_categories))
                .setMultiChoiceItems(multiChoiceItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItems[which] = isChecked;
                    }
                })
                .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<String> tags = new ArrayList<>();
                        for(int i = 0; i < multiChoiceItems.length; i++) {
                            if(checkedItems[i]) {
                                tags.add(multiChoiceItems[i]);
                            }
                        }
                        tagSelectionListener.onTagSelectionFinished(tags);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                })
                .show();

    }

    @Override
    public void onCodeLoaderFinished(CodeLoadMessage message) {
        // Dismiss the progress dialog
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }
        if(message == CodeLoadMessage.CHANGE_LANGUAGE) {
            new LocaleHelper().updateLocale(this);
            Intent intent = new Intent(this, OperatorsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("START_HOME", true);
            startActivity(intent);
        } else {
            if (currentViewState != CurrentViewState.SEARCH) {
                //Reload loaders in the best possible manner
                handleIntent(new Intent());
            }
        }

    }

    @Override
    public void onCodeLoaderStart() {
        // Showing progress dialog
        pDialog = new ProgressDialog(context);
        pDialog.setMessage(getResources().getString(R.string.saving));
        pDialog.setCancelable(false);
        pDialog.show();
    }


    /**
     * Async task to store json data into database
     */
    private class LoadDataIntoDatabase extends AsyncTask<Void, Void, Void> {
        private SaveCodesListener listener;
        private String path;
        private CodeLoadMessage message;

        public LoadDataIntoDatabase(SaveCodesListener listener, CodeLoadMessage message) {
            super();
            this.listener = listener;
            this.message = message;
            if(appLanguage.equals(ApplicationConstants.TRANSLATIONS_AVAILABLE[1])) {
                path = "fr/";
            } else {
                path = "en/";
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listener.onCodeLoaderStart();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (message == CodeLoadMessage.RECOVER) {
                JsonUtils.saveJSONCodesToDatabase(context, path + "codes.json");
            } else {
                new CodeDao(context).deleteCodes();
                new OperatorsDao(context).deleteOperators();
                new TagsDao(context).deleteTags();
                new TagMapDao(context).deleteTagMaps();

                //Save the new codes Back
                JsonUtils.saveJSONOperatorsToDatabase(context, path + "operators.json");
                JsonUtils.saveJSONCodesToDatabase(context,  path + "codes.json");
                JsonUtils.saveJSONTagsToDatabase(context, path + "tags.json");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            listener.onCodeLoaderFinished(message);
        }
    }
}
