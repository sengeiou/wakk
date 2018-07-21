package com.ubtrobot.navigation;

import android.os.Handler;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Message;
import com.ubtrobot.async.Consumer;
import com.ubtrobot.async.ListenerList;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.adapter.ProtoEventReceiver;
import com.ubtrobot.master.call.CallConfiguration;
import com.ubtrobot.master.competition.Competing;
import com.ubtrobot.master.competition.CompetingItem;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.master.service.ServiceProxy;
import com.ubtrobot.navigation.ipc.NavigationConstants;
import com.ubtrobot.navigation.ipc.NavigationConverters;
import com.ubtrobot.navigation.ipc.NavigationProto;
import com.ubtrobot.transport.message.CallException;

import java.util.Collections;
import java.util.List;

public class Navigator implements Competing {

    private final ProtoCallAdapter mNavigationService;
    private final MasterContext mMasterContext;
    private final Handler mHandler;

    private final LocationEventReceiver mLocationEventReceiver;
    private final ListenerList<LocationChangeListener> mLocationChangeListeners;

    Navigator(MasterContext masterContext, ProtoCallAdapter navigationService, Handler handler) {
        mMasterContext = masterContext;
        mNavigationService = navigationService;
        mHandler = handler;

        mLocationChangeListeners = new ListenerList<>(handler);
        mLocationEventReceiver = new LocationEventReceiver();
    }

    @Override
    public List<CompetingItem> getCompetingItems() {
        return Collections.singletonList(new CompetingItem(
                NavigationConstants.SERVICE_NAME,
                NavigationConstants.COMPETING_ITEM_NAVIGATOR
        ));
    }

    public Promise<Location, GetLocationException> getCurrentLocation() {
        return mNavigationService.call(
                NavigationConstants.CALL_PATH_GET_CURRENT_LOCATION,
                new ProtoCallAdapter.DFProtoConverter<
                        Location, NavigationProto.Location, GetLocationException>() {
                    @Override
                    public Class<NavigationProto.Location> doneProtoClass() {
                        return NavigationProto.Location.class;
                    }

                    @Override
                    public Location
                    convertDone(NavigationProto.Location location) throws Exception {
                        return NavigationConverters.toLocationPojo(location);
                    }

                    @Override
                    public GetLocationException convertFail(CallException e) {
                        return new GetLocationException.Factory().from(e);
                    }
                }
        );
    }

    public Promise<Location, LocateException> locateSelf(CompetitionSession session) {
        return locateSelf(session, new LocateOption.Builder().build());
    }

    public Promise<Location, LocateException>
    locateSelf(CompetitionSession session, LocateOption option) {
        checkSession(session);

        if (option == null) {
            throw new IllegalArgumentException("Argument option is null.");
        }

        ServiceProxy serviceProxy = session.createSystemServiceProxy(NavigationConstants.SERVICE_NAME);
        if (option.getTimeout() > 0) {
            serviceProxy.setConfiguration(new CallConfiguration.Builder(
                    serviceProxy.getConfiguration()).setTimeout(option.getTimeout()).build());
        }

        ProtoCallAdapter navigationService = new ProtoCallAdapter(serviceProxy, mHandler);
        return navigationService.call(
                NavigationConstants.CALL_PATH_LOCATE_SELF,
                NavigationConverters.toLocateOptionProto(option),
                new ProtoCallAdapter.DFProtoConverter<
                        Location, NavigationProto.Location, LocateException>() {
                    @Override
                    public Class<NavigationProto.Location> doneProtoClass() {
                        return NavigationProto.Location.class;
                    }

                    @Override
                    public Location convertDone(NavigationProto.Location location) {
                        return NavigationConverters.toLocationPojo(location);
                    }

                    @Override
                    public LocateException convertFail(CallException e) {
                        return new LocateException.Factory().from(e);
                    }
                }
        );
    }

    public Promise<Boolean, AccessServiceException> isLocating() {
        return mNavigationService.call(
                NavigationConstants.CALL_PATH_QUERY_LOCATING,
                new ProtoCallAdapter.DFProtoConverter<Boolean, BoolValue, AccessServiceException>() {
                    @Override
                    public Class<BoolValue> doneProtoClass() {
                        return BoolValue.class;
                    }

                    @Override
                    public Boolean convertDone(BoolValue locating) throws Exception {
                        return locating.getValue();
                    }

                    @Override
                    public AccessServiceException convertFail(CallException e) {
                        return new AccessServiceException.Factory().from(e);
                    }
                }
        );
    }

    private void checkSession(CompetitionSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Argument session is null.");
        }

        if (!session.containsCompeting(this)) {
            throw new IllegalArgumentException("The competition session does NOT contain the navigator.");
        }
    }

    public ProgressivePromise<Void, NavigateException, NavigatingProgress>
    navigate(CompetitionSession session, Location destination) {
        return navigate(session, destination, new NavigateOption.Builder().build());
    }

    public ProgressivePromise<Void, NavigateException, NavigatingProgress>
    navigate(CompetitionSession session, Location destination, NavigateOption option) {
        checkSession(session);

        if (destination == null || option == null) {
            throw new IllegalArgumentException("Argument destination or option is null.");
        }

        ProtoCallAdapter navigationService = new ProtoCallAdapter(
                session.createSystemServiceProxy(NavigationConstants.SERVICE_NAME),
                mHandler
        );
        return navigationService.callStickily(
                NavigationConstants.CALL_PATH_NAVIGATE,
                NavigationConverters.toNavigateOptionProto(destination, option),
                new ProtoCallAdapter.DFPProtoConverter<
                        Void, Message, NavigateException,
                        NavigatingProgress, NavigationProto.NavigatingProgress>() {
                    @Override
                    public Class<Message> doneProtoClass() {
                        return Message.class;
                    }

                    @Override
                    public Void convertDone(Message protoParam) {
                        return null;
                    }

                    @Override
                    public NavigateException convertFail(CallException e) {
                        return new NavigateException.Factory().from(e);
                    }

                    @Override
                    public Class<NavigationProto.NavigatingProgress> progressProtoClass() {
                        return NavigationProto.NavigatingProgress.class;
                    }

                    @Override
                    public NavigatingProgress
                    convertProgress(NavigationProto.NavigatingProgress progress) {
                        return NavigationConverters.toNavigatingProgressPojo(progress);
                    }
                }
        );
    }

    public Promise<Boolean, AccessServiceException> isNavigating() {
        return mNavigationService.call(
                NavigationConstants.CALL_PATH_QUERY_NAVIGATING,
                new ProtoCallAdapter.DFProtoConverter<Boolean, BoolValue, AccessServiceException>() {
                    @Override
                    public Class<BoolValue> doneProtoClass() {
                        return BoolValue.class;
                    }

                    @Override
                    public Boolean convertDone(BoolValue navigating) throws Exception {
                        return navigating.getValue();
                    }

                    @Override
                    public AccessServiceException convertFail(CallException e) {
                        return new AccessServiceException.Factory().from(e);
                    }
                }
        );
    }

    public void registerLocationChangeListener(LocationChangeListener locationChangeListener){
        synchronized (mLocationChangeListeners) {
            boolean subscribed = !mLocationChangeListeners.isEmpty();
            mLocationChangeListeners.register(locationChangeListener);

            if (!subscribed) {
                mMasterContext.subscribe(mLocationEventReceiver,
                        NavigationConstants.ACTION_LOCATION_CHANGE);
            }
        }
    }

    public void unregisterLocationChangeListener(LocationChangeListener locationChangeListener){
        synchronized (mLocationChangeListeners) {
            mLocationChangeListeners.unregister(locationChangeListener);

            if (mLocationChangeListeners.isEmpty()) {
                mMasterContext.unsubscribe(mLocationEventReceiver);
            }
        }
    }

    private class LocationEventReceiver extends ProtoEventReceiver<NavigationProto.Location> {
        @Override
        protected Class<NavigationProto.Location> protoClass() {
            return NavigationProto.Location.class;
        }

        @Override
        public void onReceive(MasterContext masterContext, String action, NavigationProto.Location param) {
            synchronized (mLocationChangeListeners) {
                final Location location = NavigationConverters.toLocationPojo(param);

                mLocationChangeListeners.forEach(new Consumer<LocationChangeListener>() {
                    @Override
                    public void accept(LocationChangeListener listener) {
                        listener.onLocationChanged(location);
                    }
                });
            }
        }
    }

    public static class NavigatingProgress {

        public static final int STATE_BEGAN = 0;
        public static final int STATE_LOCATION_CHANGED = 1;
        public static final int STATE_BLOCKED = 2;
        public static final int STATE_ENDED = 3;

        private int state;
        private Location location;

        private NavigatingProgress(int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }

        public Location getLocation() {
            return location;
        }

        public boolean isBegan() {
            return state == STATE_BEGAN;
        }

        public boolean isLocationChanged() {
            return state == STATE_LOCATION_CHANGED;
        }

        public boolean isBlocked() { return state == STATE_BLOCKED; }

        public boolean isEnded() {
            return state == STATE_ENDED;
        }

        @Override
        public String toString() {
            return "NavigatingProgress{" +
                    "state=" + state +
                    ", location=" + location +
                    '}';
        }

        public static class Builder {

            private int state;
            private Location location = Location.DEFAULT;

            public Builder(int state) {
                this.state = state;

                if (state < STATE_BEGAN || state > STATE_ENDED) {
                    throw new IllegalArgumentException("Argument state < " + STATE_BEGAN + " || " +
                            "state > " + STATE_ENDED + ".");
                }
            }

            public Builder setLocation(Location location) {
                if (location == null) {
                    throw new IllegalArgumentException("Argument location is null.");
                }

                this.location = location;
                return this;
            }

            public NavigatingProgress build() {
                NavigatingProgress progress = new NavigatingProgress(state);
                progress.location = location;
                return progress;
            }
        }
    }
}