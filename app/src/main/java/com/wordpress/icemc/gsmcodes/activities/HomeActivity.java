package com.wordpress.icemc.gsmcodes.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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
import android.widget.Toast;

import com.wordpress.icemc.gsmcodes.R;
import com.wordpress.icemc.gsmcodes.adapter.CodeAdapter;
import com.wordpress.icemc.gsmcodes.dao.CodeDao;
import com.wordpress.icemc.gsmcodes.dao.TagsDao;
import com.wordpress.icemc.gsmcodes.listeners.CodeAdapterListener;
import com.wordpress.icemc.gsmcodes.listeners.ContactButtonClickListener;
import com.wordpress.icemc.gsmcodes.listeners.ContactPickListener;
import com.wordpress.icemc.gsmcodes.model.Code;
import com.wordpress.icemc.gsmcodes.model.CodeItem;
import com.wordpress.icemc.gsmcodes.model.DefaultTags;
import com.wordpress.icemc.gsmcodes.model.Tag;
import com.wordpress.icemc.gsmcodes.providers.CodeProviderAPI;
import com.wordpress.icemc.gsmcodes.providers.TagProviderAPI;
import com.wordpress.icemc.gsmcodes.utilities.ApplicationConstants;
import com.wordpress.icemc.gsmcodes.utilities.GSMCodeUtils;
import com.wordpress.icemc.gsmcodes.views.CodeBottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, CodeAdapterListener, ContactButtonClickListener,  LoaderManager.LoaderCallbacks<Cursor> {

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

    //Fields for BottomSheet
    private CodeBottomSheetDialog codeBottomSheetDialog;

    //boolean flags
    //private static boolean isOtherTags = true;
    //private static boolean isFavourite = false;
    private boolean isFirstSearchIntent = true;
    private boolean isFirstNormalStart =  true;

    //Fields for filter button;
    private int currentTag = 0;
    private ArrayList<String> tags = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        operatorName = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(ApplicationConstants.LAST_OPERATOR_USED, "");
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

        //Init the tag cursor loader
        getSupportLoaderManager().initLoader(TAG_LOADER, null, this);
        handleIntent(getIntent());


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
    protected void onStart(){
        super.onStart();

    }
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Bundle bundle = new Bundle();
            bundle.putString(QUERY, query);
            if(isFirstSearchIntent) {
                isFirstSearchIntent = false;
                getSupportLoaderManager().initLoader(CODE_SEARCH_LOADER, bundle, this);
            } else {
                //Reset the loader with the new parameters
                getSupportLoaderManager().restartLoader(CODE_SEARCH_LOADER, bundle, this);
            }

        } else {
            final Bundle bundle = new Bundle();
            if(isFirstNormalStart) {
                isFirstNormalStart = false;
                bundle.putString(CODE_TAG, DefaultTags.ALL.toString());
                getSupportLoaderManager().initLoader(CODE_LOADER, bundle, this);
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
        SearchView searchView =
                (SearchView) search.getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        MenuItemCompat.setOnActionExpandListener(search, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
                    Intent intent = new Intent();
                    setIntent(intent);
                    handleIntent(intent);
                }
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
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_search) {
            return true;
        } else if(id == R.id.action_create) {
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
        } else if (id == R.id.nav_donate) {
            startActivity(new Intent(this, Donate.class));
        } else if (id == R.id.nav_help) {
            startActivity(new Intent(this, Help.class));
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

    @Override
    public void onPhoneClicked(int position) {
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

            codeBottomSheetDialog = new CodeBottomSheetDialog(this, code, this);
            contactPickListener = codeBottomSheetDialog;
            codeBottomSheetDialog.getBtn_dialog_bottom_sheet_ok()
                    .setOnClickListener(okDialogClickListener);
            codeBottomSheetDialog.getBtn_dialog_bottom_sheet_cancel()
                    .setOnClickListener(cancelDialogClickListener);
            codeBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
//                    codeIndex = -1;
                }
            });
            codeBottomSheetDialog.show();
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
                String selection = CodeProviderAPI.CodeColumns.OPERATOR_NAME + "=? " + "AND " + CodeProviderAPI.CodeColumns.NAME + " LIKE ?";
                String[] selectionArgs = {operatorName, "%"+args.getString(QUERY)+"%"};
                return new CursorLoader(this, CodeProviderAPI.CodeColumns.CONTENT_URI, null, selection, selectionArgs, sortOrder);

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
                    codeBottomSheetDialog.getCode().getCode(), codeBottomSheetDialog.getEditTexts())));
            codeIndex = -1;

            //NOT AN ERROR THERE IS NO WAY A USER WILL REACH THIS POINT WITHOUT GRANTING THE CALL_PHONE PERMISSION
            startActivity(callIntent);
            codeBottomSheetDialog.dismiss();

        }
    };

    View.OnClickListener cancelDialogClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            codeIndex = -1;
            codeBottomSheetDialog.dismiss();
        }
    };

}
