package com.ubtrobot.navigation;

import android.os.Handler;

import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.Competing;
import com.ubtrobot.master.competition.CompetingItem;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.navigation.ipc.NavigationConstants;
import com.ubtrobot.navigation.ipc.NavigationConverters;
import com.ubtrobot.navigation.ipc.NavigationProto;
import com.ubtrobot.transport.message.CallException;

import java.util.Collections;
import java.util.List;

public class Navigator implements Competing {

    private final Handler mHandler;

    Navigator(Handler handler) {
        mHandler = handler;
    }

    @Override
    public List<CompetingItem> getCompetingItems() {
        return Collections.singletonList(new CompetingItem(
                NavigationConstants.SERVICE_NAME,
                NavigationConstants.COMPETING_ITEM_NAVIGATOR
        ));
    }

    public Promise<Location, LocateException, Void> locateSelf(CompetitionSession session) {
        return locateSelf(session, new LocateOption.Builder().build());
    }

    public Promise<Location, LocateException, Void>
    locateSelf(CompetitionSession session, LocateOption option) {
        checkSession(session);

        if (option == null) {
            throw new IllegalArgumentException("Argument option is null.");
        }

        ProtoCallAdapter navigationService = new ProtoCallAdapter(
                session.createSystemServiceProxy(NavigationConstants.SERVICE_NAME),
                mHandler
        );
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

    private void checkSession(CompetitionSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Argument session is null.");
        }

        if (!session.containsCompeting(this)) {
            throw new IllegalArgumentException("The competition session does NOT contain the navigator.");
        }
    }

    public Promise<Void, NavigateException, NavigatingProgress>
    navigate(CompetitionSession session, Location destination) {
        return navigate(session, destination, new NavigateOption.Builder().build());
    }

    public Promise<Void, NavigateException, NavigatingProgress>
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

    public static class NavigatingProgress {

        public static final int STATE_BEGAN = 0;
        public static final int STATE_ENDED = 1;

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