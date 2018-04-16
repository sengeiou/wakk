package com.ubtrobot.navigation.sal;

import android.util.Pair;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.InterruptibleAsyncTask;
import com.ubtrobot.async.Promise;
import com.ubtrobot.master.competition.InterruptibleTaskHelper;
import com.ubtrobot.navigation.LocateException;
import com.ubtrobot.navigation.LocateOption;
import com.ubtrobot.navigation.Location;
import com.ubtrobot.navigation.NavMap;
import com.ubtrobot.navigation.NavMapException;
import com.ubtrobot.navigation.NavigateException;
import com.ubtrobot.navigation.NavigateOption;
import com.ubtrobot.navigation.Navigator;

import java.util.List;

public abstract class AbstractNavigationService implements NavigationService {

    private static final String TASK_RECEIVER_NAVIGATOR = "navigator";
    private static final String TASK_NAME_LOCATE_SELF = "locate-self";
    private static final String TASK_NAME_NAVIGATE = "navigator";

    private final InterruptibleTaskHelper mInterruptibleTaskHelper;

    public AbstractNavigationService() {
        mInterruptibleTaskHelper = new InterruptibleTaskHelper();
    }

    @Override
    public Promise<Pair<List<NavMap>, String>, NavMapException, Void>
    getNavMapList() {
        AsyncTask<Pair<List<NavMap>, String>, NavMapException, Void> task =
                createGetNavMapListTask();
        if (task == null) {
            throw new IllegalStateException("createGetNavMapListTask return null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<Pair<List<NavMap>, String>, NavMapException, Void>
    createGetNavMapListTask();

    @Override
    public Promise<NavMap, NavMapException, Void> addNavMap(NavMap navMap) {
        AsyncTask<NavMap, NavMapException, Void> task = createAddNavMapTask(navMap);
        if (task == null) {
            throw new IllegalStateException("createAddNavMapTask return null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<NavMap, NavMapException, Void> createAddNavMapTask(NavMap navMap);

    @Override
    public Promise<NavMap, NavMapException, Void> selectNavMap(String navMapId) {
        AsyncTask<NavMap, NavMapException, Void> task = createSelectNavMapTask(navMapId);
        if (task == null) {
            throw new IllegalStateException("createSelectNavMapTask return null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<NavMap, NavMapException, Void> createSelectNavMapTask(String navMapId);

    @Override
    public Promise<NavMap, NavMapException, Void> modifyNavMap(NavMap navMap) {
        AsyncTask<NavMap, NavMapException, Void> task = createModifyNavMapTask(navMap);
        if (task == null) {
            throw new IllegalStateException("createModifyNavMapTask return null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<NavMap, NavMapException, Void> createModifyNavMapTask(NavMap navMap);

    @Override
    public Promise<NavMap, NavMapException, Void> removeNavMap(String navMapId) {
        AsyncTask<NavMap, NavMapException, Void> task = createRemoveNavMapTask(navMapId);
        if (task == null) {
            throw new IllegalStateException("createRemoveNavMapTask return null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<NavMap, NavMapException, Void> createRemoveNavMapTask(String navMapId);

    @Override
    public Promise<Location, LocateException, Void>
    locateSelf(final LocateOption option) {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_NAVIGATOR,
                TASK_NAME_LOCATE_SELF,
                new InterruptibleAsyncTask<Location, LocateException, Void>() {
                    @Override
                    protected void onStart() {
                        doStartLocatingSelf(option);
                    }

                    @Override
                    protected void onCancel() {
                        doStopLocatingSelf();
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<LocateException>() {
                    @Override
                    public LocateException createInterruptedException(String interrupter) {
                        return new LocateException.Factory().interrupted("Interrupt the " +
                                interrupter + " task.");
                    }
                }
        );
    }

    protected abstract void doStartLocatingSelf(LocateOption option);

    protected abstract void doStopLocatingSelf();

    public void resolveLocatingSelf(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Argument location is null.");
        }

        mInterruptibleTaskHelper.resolve(TASK_RECEIVER_NAVIGATOR, TASK_NAME_LOCATE_SELF, location);
    }

    public void rejectLocatingSelf(LocateException e) {
        if (e == null) {
            throw new IllegalArgumentException("Argument e is null.");
        }

        mInterruptibleTaskHelper.reject(TASK_RECEIVER_NAVIGATOR, TASK_NAME_LOCATE_SELF, e);
    }

    @Override
    public Promise<Void, NavigateException, Navigator.NavigatingProgress>
    navigate(final Location destination, final NavigateOption option) {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_NAVIGATOR,
                TASK_NAME_NAVIGATE,
                new InterruptibleAsyncTask<Void, NavigateException, Navigator.NavigatingProgress>() {
                    @Override
                    protected void onStart() {
                        doStartNavigating(destination, option);
                    }

                    @Override
                    protected void onCancel() {
                        doStopNavigating();
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<NavigateException>() {
                    @Override
                    public NavigateException createInterruptedException(String interrupter) {
                        return new NavigateException.Factory().interrupted("Interrupt the " +
                                interrupter + " task.");
                    }
                }
        );
    }

    protected abstract void
    doStartNavigating(Location destination, NavigateOption option);

    protected abstract void doStopNavigating();

    public void notifyNavigatingProgress(Navigator.NavigatingProgress progress) {
        if (progress == null) {
            throw new IllegalArgumentException("Argument progress is null.");
        }

        mInterruptibleTaskHelper.notify(TASK_RECEIVER_NAVIGATOR, TASK_NAME_NAVIGATE, progress);
    }

    public void resolveNavigating() {
        mInterruptibleTaskHelper.resolve(TASK_RECEIVER_NAVIGATOR, TASK_NAME_NAVIGATE, null);
    }

    public void rejectNavigating(NavigateException e) {
        if (e == null) {
            throw new IllegalArgumentException("Argument e is null.");
        }

        mInterruptibleTaskHelper.reject(TASK_RECEIVER_NAVIGATOR, TASK_NAME_NAVIGATE, e);
    }
}