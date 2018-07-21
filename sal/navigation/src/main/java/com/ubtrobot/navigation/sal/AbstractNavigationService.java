package com.ubtrobot.navigation.sal;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.InterruptibleAsyncTask;
import com.ubtrobot.async.InterruptibleProgressiveAsyncTask;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.master.Master;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.competition.InterruptibleTaskHelper;
import com.ubtrobot.master.context.ContextRunnable;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.navigation.GetLocationException;
import com.ubtrobot.navigation.LocateException;
import com.ubtrobot.navigation.LocateOption;
import com.ubtrobot.navigation.Location;
import com.ubtrobot.navigation.NavMap;
import com.ubtrobot.navigation.NavMapException;
import com.ubtrobot.navigation.NavigateException;
import com.ubtrobot.navigation.NavigateOption;
import com.ubtrobot.navigation.Navigator;
import com.ubtrobot.navigation.ipc.NavigationConstants;
import com.ubtrobot.navigation.ipc.NavigationConverters;
import com.ubtrobot.navigation.ipc.NavigationProto;
import com.ubtrobot.navigation.ipc.master.NavigationSystemService;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.List;
import java.util.Set;

public abstract class AbstractNavigationService implements NavigationService {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("AbstractNavigationService");

    private static final String TASK_RECEIVER_NAVIGATOR = "navigator";
    private static final String TASK_NAME_LOCATE_SELF = "locate-self";
    private static final String TASK_NAME_NAVIGATE = "navigator";

    private final InterruptibleTaskHelper mInterruptibleTaskHelper;

    public AbstractNavigationService() {
        mInterruptibleTaskHelper = new InterruptibleTaskHelper();
    }

    @Override
    public Promise<List<NavMap>, NavMapException>
    getNavMapList() {
        AsyncTask<List<NavMap>, NavMapException> task =
                createGettingNavMapListTask();
        if (task == null) {
            throw new IllegalStateException("createGettingNavMapListTask return null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<List<NavMap>, NavMapException>
    createGettingNavMapListTask();

    @Override
    public Promise<NavMap, NavMapException> getNavMap(String mapId) {
        AsyncTask<NavMap, NavMapException> task = createGettingNavMapTask(mapId);
        if (task == null) {
            throw new IllegalStateException("createGettingNavMapTask return null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<NavMap, NavMapException> createGettingNavMapTask(String mapId);

    @Override
    public Promise<NavMap, NavMapException> getSelectedNavMap() {
        AsyncTask<NavMap, NavMapException> task = createGettingSelectedNavMap();
        if (task == null) {
            throw new IllegalStateException("createGettingSelectedNavMap return null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<NavMap, NavMapException> createGettingSelectedNavMap();

    @Override
    public Promise<NavMap, NavMapException> addNavMap(NavMap navMap) {
        AsyncTask<NavMap, NavMapException> task = createAddingNavMapTask(navMap);
        if (task == null) {
            throw new IllegalStateException("createAddingNavMapTask return null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<NavMap, NavMapException> createAddingNavMapTask(NavMap navMap);

    @Override
    public Promise<NavMap, NavMapException> selectNavMap(String navMapId) {
        AsyncTask<NavMap, NavMapException> task = createSelectingNavMapTask(navMapId);
        if (task == null) {
            throw new IllegalStateException("createSelectingNavMapTask return null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<NavMap, NavMapException> createSelectingNavMapTask(String navMapId);

    @Override
    public Promise<NavMap, NavMapException> modifyNavMap(NavMap navMap) {
        AsyncTask<NavMap, NavMapException> task = createModifyingNavMapTask(navMap);
        if (task == null) {
            throw new IllegalStateException("createModifyingNavMapTask return null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<NavMap, NavMapException> createModifyingNavMapTask(NavMap navMap);

    @Override
    public Promise<NavMap, NavMapException> removeNavMap(String navMapId) {
        AsyncTask<NavMap, NavMapException> task = createRemovingNavMapTask(navMapId);
        if (task == null) {
            throw new IllegalStateException("createRemovingNavMapTask return null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<NavMap, NavMapException> createRemovingNavMapTask(String navMapId);

    @Override
    public Promise<Location, LocateException>
    locateSelf(final LocateOption option) {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_NAVIGATOR,
                TASK_NAME_LOCATE_SELF,
                new InterruptibleAsyncTask<Location, LocateException>() {
                    @Override
                    protected void onStart() {
                        startLocatingSelf(option);
                    }

                    @Override
                    protected void onCancel() {
                        stopLocatingSelf();
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<LocateException>() {
                    @Override
                    public LocateException
                    createInterruptedException(Set<String> interrupters) {
                        return new LocateException.Factory().interrupted(
                                "Interrupted by " + interrupters);
                    }
                }
        );
    }

    @Override
    public Promise<Location, GetLocationException> getCurrentLocation() {
        AsyncTask<Location, GetLocationException> task = createGettingCurrentLocationTask();
        if (task == null) {
            throw new IllegalStateException("createGettingCurrentLocationTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<Location, GetLocationException> createGettingCurrentLocationTask();

    protected abstract void startLocatingSelf(LocateOption option);

    protected abstract void stopLocatingSelf();

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
    public Promise<Boolean, AccessServiceException> isLocating() {
        AsyncTask<Boolean, AccessServiceException> task = createGettingLocatingTask();
        if (task == null) {
            throw new IllegalStateException("createGettingLocatingTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<Boolean, AccessServiceException> createGettingLocatingTask();

    @Override
    public ProgressivePromise<Void, NavigateException, Navigator.NavigatingProgress>
    navigate(final Location destination, final NavigateOption option) {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_NAVIGATOR,
                TASK_NAME_NAVIGATE,
                new InterruptibleProgressiveAsyncTask<Void, NavigateException, Navigator.NavigatingProgress>() {
                    @Override
                    protected void onStart() {
                        startNavigating(destination, option);
                    }

                    @Override
                    protected void onCancel() {
                        stopNavigating();
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<NavigateException>() {
                    @Override
                    public NavigateException
                    createInterruptedException(Set<String> interrupters) {
                        return new NavigateException.Factory().interrupted(
                                "Interrupted by " + interrupters);
                    }
                }
        );
    }

    protected abstract void
    startNavigating(Location destination, NavigateOption option);

    protected abstract void stopNavigating();

    public void reportNavigatingProgress(Navigator.NavigatingProgress progress) {
        if (progress == null) {
            throw new IllegalArgumentException("Argument progress is null.");
        }

        mInterruptibleTaskHelper.report(TASK_RECEIVER_NAVIGATOR, TASK_NAME_NAVIGATE, progress);
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

    protected void publishLocation(final Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Argument location is null.");
        }

        boolean navigationSystemServiceStarted = Master.get().execute(
                NavigationSystemService.class,
                new ContextRunnable<NavigationSystemService>() {
                    @Override
                    public void run(NavigationSystemService navigationSystemService) {
                        ProtoParam<NavigationProto.Location> param = ProtoParam.create(
                                NavigationConverters.toLocationProto(location));

                        navigationSystemService.publish(NavigationConstants.ACTION_LOCATION_CHANGE, param);
                    }
                }
        );

        if (!navigationSystemServiceStarted) {
            LOGGER.e("Publish Location failed. Pls start NavigationSystemService first.");
        }
    }

    @Override
    public Promise<Boolean, AccessServiceException> isNavigating() {
        AsyncTask<Boolean, AccessServiceException> task = createGettingNavigatingTask();
        if (task == null) {
            throw new IllegalStateException("createGettingNavigatingTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<Boolean, AccessServiceException> createGettingNavigatingTask();
}