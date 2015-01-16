package com.parse.offlinetodos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.parse.ParseACL;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRole;
import com.parse.ParseUser;
import com.parse.ui.ParseLoginBuilder;

import java.util.ArrayList;
import java.util.List;

public class TodoListActivity extends Activity {

    //TodoListFragment todoListFragment;
    public static final int LOGIN_ACTIVITY_CODE = 100;
    public static final int EDIT_ACTIVITY_CODE = 200;
    public static String EMAIL_KEY = "email";
    private ParseEventTaskQueue taskQueue;
    private DrawerLayout mDrawerLayout;
    private ListView mNavigationMenu;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private List<ParseRole> roles;
    private ActionBarDrawerToggle mDrawerToggle;
    private ImageButton mAddTodoList;
    private ArrayAdapter<String> navigationMenuAdapter;
    private RelativeLayout mLeftDrawer;
    private TodoListFragment currentFragment;
    List<Todo> todoList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_list_activity);


        //setContentView(R.layout.todo_list_fragment);
        taskQueue = new ParseEventTaskQueue();
        roles = new ArrayList<ParseRole>();

        // If User is not Logged In Start Activity Login
        if (ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
            ParseLoginBuilder builder = new ParseLoginBuilder(this);
            startActivityForResult(builder.build(), LOGIN_ACTIVITY_CODE);
        } else {
            mTitle = mDrawerTitle = getTitle();
            //todoListFragment = new TodoListFragment();
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mNavigationMenu = (ListView) findViewById(R.id.list_navigation_menu);
            mAddTodoList = (ImageButton) findViewById(R.id.add_todo_list);
            mLeftDrawer = (RelativeLayout) findViewById(R.id.left_drawer);
            //Get TodoLists Names
            roles = initTodoListRoles();

            // set a custom shadow that overlays the main content when the drawer opens
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            // Set the adapter for the list view
            navigationMenuAdapter = new ArrayAdapter<String>(this,
                    R.layout.drawer_list_item, getTodoListsNames(roles));
            mNavigationMenu.setAdapter(navigationMenuAdapter);
            AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ParseRole currentTodoList = roles.get(position);
                    selectItem(currentTodoList);
                }
            };
            mNavigationMenu.setOnItemClickListener(listener);

            // enable ActionBar app icon to behave as action to toggle nav drawer
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);

            // ActionBarDrawerToggle ties together the the proper interactions
            // between the sliding drawer and the action bar app icon
            mDrawerToggle = new ActionBarDrawerToggle(
                    this,                  /* host Activity */
                    mDrawerLayout,         /* DrawerLayout object */
                    R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                    R.string.drawer_open,  /* "open drawer" description for accessibility */
                    R.string.drawer_close  /* "close drawer" description for accessibility */
            ) {
                public void onDrawerClosed(View view) {
                    getActionBar().setTitle(mTitle);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                public void onDrawerOpened(View drawerView) {
                    getActionBar().setTitle(mDrawerTitle);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }
            };
            mAddTodoList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Make sure there's a valid user, anonymous
                    // or regular
                    AlertDialog.Builder alert = new AlertDialog.Builder(TodoListActivity.this);

                    alert.setTitle("Ajouter une liste");
                    alert.setMessage("Donnez un nom à cette nouvelle liste");

                    // Set an EditText view to get user input
                    final EditText input = new EditText(TodoListActivity.this);
                    alert.setView(input);

                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String newTodoList = input.getText().toString();
                            //TODO check if a todolist with the same name does not exist
                            int index = roles.size();
                            navigationMenuAdapter.insert(newTodoList, index);
                            ParseACL roleACL = new ParseACL();
                            roleACL.setReadAccess(ParseUser.getCurrentUser(),true);
                            roleACL.setWriteAccess(ParseUser.getCurrentUser(), true);
                            ParseRole todoListRole = new ParseRole(Utils.getRoleName(newTodoList, ParseUser.getCurrentUser()), roleACL);
                            todoListRole.put(Todo.LIST_NAME_KEY, newTodoList);
                            todoListRole.put(Todo.AUTHOR_KEY, ParseUser.getCurrentUser());
                            todoListRole.getUsers().add(ParseUser.getCurrentUser());
                            roles.add(todoListRole);
                            taskQueue.add(todoListRole);
                        }
                    });

                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Canceled.
                        }
                    });

                    alert.show();
                }
            });

        }
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            if (roles.size() > 0) {
                selectItem(roles.get(0));
            }
        }
    }

    private void displayAlertDialog(String title, String message, DialogInterface.OnClickListener positiveButtonListener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(TodoListActivity.this);

        alert.setTitle(title);
        alert.setMessage(message);

        // Set an EditText view to get user input
        final EditText input = new EditText(TodoListActivity.this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newTodoList = input.getText().toString();
                //TODO check if a todolist with the same name does not exist
                int index = roles.size();
                navigationMenuAdapter.insert(newTodoList, index);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    private List<ParseRole> initTodoListRoles() {
        ParseQuery<ParseRole> query = ParseRole.getQuery();
        List<ParseRole> requestedRoles = null;
        try {
            requestedRoles = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return requestedRoles;
    }

    private void selectItem(ParseRole selectedRole) {
        // update the main content by replacing fragments
        Fragment fragment = TodoListFragment.newInstance(selectedRole);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        this.currentFragment = (TodoListFragment) fragment;
        ft.replace(R.id.content_frame, fragment);
        ft.commit();

        // update selected item title, then close the drawer
        setTitle(selectedRole.getString(Todo.LIST_NAME_KEY));
        mDrawerLayout.closeDrawer(mLeftDrawer);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean realUser = !ParseAnonymousUtils.isLinked(ParseUser
                .getCurrentUser());
        //menu.findItem(R.id.action_login).setVisible(!realUser);
//        menu.findItem(R.id.action_logout).setVisible(realUser);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.todo_list, menu);
        return true;
    }

    /*private void openEditView(Todo todo) {
        Intent i = new Intent(this, NewTodoActivity.class);
        i.putExtra("ID", todo.getUuidString());
        startActivityForResult(i, EDIT_ACTIVITY_CODE);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            AlertDialog.Builder alert = new AlertDialog.Builder(TodoListActivity.this);
            List<ParseUser> sharedUsers = null;
            try {
                sharedUsers = currentFragment.getTodoListRole().getUsers().getQuery().find();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            alert.setTitle("Partager la liste \""+currentFragment.getTodoListName()+"\"");
            String message = "Vous partagez déjà la liste avec";
            if (sharedUsers.size() == 1) {
                message += "personne";
            } else {
                int nb_shares = 0;
                for (int i = 0; i < sharedUsers.size(); i++) {
                    //dont print current User
                    if(!sharedUsers.get(i).equals(ParseUser.getCurrentUser())) {
                        if(nb_shares == sharedUsers.size() - 2) {
                            message+=" et";
                        } else if (nb_shares != 0 ) {
                            message += ",";
                        }

                        message += " " + sharedUsers.get(i).getUsername();
                        nb_shares ++;
                        if (i == sharedUsers.size()) {
                            message += ".";
                        }
                    }
                }
            }
            alert.setMessage(message + "\n" + "e-mail de la personne à qui vous partagez la liste");

            // Set an EditText view to get user input
            final EditText input = new EditText(TodoListActivity.this);
            alert.setView(input);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String email = input.getText().toString();
                    List<ParseUser> users = null;
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo(EMAIL_KEY, email);
                    try {
                        users = query.find();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if(users.size() > 0)
                    {
                        currentFragment.shareListWithUser(users.get(0));
                    }

                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
        }

        if (item.getItemId() == R.id.action_sync) {
            currentFragment.synchroniseTodos(); 
        }

        if (item.getItemId() == R.id.action_logout) {
            // Log out the current user
            ParseUser.logOut();
            // Update the logged in label info
            //TODO uncomment line under
            //todoListFragment.updateLoggedInInfo();
            // Clear the view
            //todoList.clear();
            // Unpin all the current objects
            /*ParseObject
                    .unpinAllInBackground(TodoListApplication.TODO_GROUP_NAME);*/
            ParseLoginBuilder builder = new ParseLoginBuilder(this);
            startActivityForResult(builder.build(), LOGIN_ACTIVITY_CODE);
        }

        if (item.getItemId() == R.id.action_login) {
            ParseLoginBuilder builder = new ParseLoginBuilder(this);
            startActivityForResult(builder.build(), LOGIN_ACTIVITY_CODE);
        }

        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ParseEventTaskQueue getTaskQueue() {
        return taskQueue;
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    public List<String> getTodoListsNames (List<ParseRole> roles) {
        List<String> names = null;
        if (roles != null) {
            names =new ArrayList<String>();
            for (int i = 0; i < roles.size(); i++) {
                names.add(roles.get(i).getString(Todo.LIST_NAME_KEY));
            }
        }
        return names;
    }
}
