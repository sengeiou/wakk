package com.ubtrobot.navigation.sal;

import android.util.Pair;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.Promise;
import com.ubtrobot.navigation.NavMap;
import com.ubtrobot.navigation.NavMapException;

import java.util.List;

public abstract class AbstractNavigationService implements NavigationService {

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
}