package com.nostalgia.persistence.repo;

import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.Video;
import com.nostalgia.runnable.VideoGetterThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by alex on 11/4/15.
 */
public class VideoRepository {

    public void setCallback(VideoEventListener callback) {
        this.callback = callback;
    }

    public void setStatusCallback(VideoStatusChangeListener listener){
        this.statusCallback = listener;
    }

    public Video findOneById(String newAdded, boolean searchOnline, boolean persist) {
        Query query = database.getView("video_by_id").createQuery();
        query.setDescending(true);
        List<Object> keys = new ArrayList<Object>();
        keys.add(newAdded);
        query.setKeys(keys);

        QueryEnumerator result = null;
        try {
            result = query.run();
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "error querying for logged in user");
            return null;
        }

        for (Iterator<QueryRow> it = result; it.hasNext(); ) {
            Document cur = it.next().getDocument();
            Video thisVid = this.docToVideo(cur);
            return thisVid;

        }

        if (searchOnline){
            //if we're here, try and fire of a video get thread
            VideoGetterThread vidGet = new VideoGetterThread(newAdded);
            vidGet.start();
            try {
                vidGet.join();

                Video gotten = vidGet.getMatching();

                if(gotten != null && persist){
                    this.save(gotten);
                }
                return gotten;
            } catch (Exception e) {
                Log.e(TAG, "interrupted");
            }

        }
        return null;

    }

    private Document findDocById(String newAdded) {
        Query query = database.getView("video_by_id").createQuery();
        query.setDescending(true);
        List<Object> keys = new ArrayList<Object>();
        keys.add(newAdded);
        query.setKeys(keys);

        QueryEnumerator result = null;
        try {
            result = query.run();
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "error querying for logged in user");
            return null;
        }

        for (Iterator<QueryRow> it = result; it.hasNext(); ) {
            Document cur = it.next().getDocument();

            return cur;

        }
        return null;

    }

    public ArrayList<Video> getPendingWifiUploads() {

        ArrayList<Video> statusWifi = getVideosByStatus("PENDING_WIFI");

        return statusWifi;
    }

    public ArrayList<Video> getVideosByStatus(String status){
        Query query = database.getView("video_by_status").createQuery();
        query.setDescending(true);
        List<Object> keys = new ArrayList<Object>();
        keys.add(status);
        query.setKeys(keys);

        QueryEnumerator result = null;
        try {
            result = query.run();
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "error querying for logged in user");
            return null;
        }

        ArrayList<Video> videos = new ArrayList<Video>();
        for (Iterator<QueryRow> it = result; it.hasNext(); ) {
            Document cur = it.next().getDocument();
            Video thisVid = this.docToVideo(cur);
            videos.add(thisVid);
        }
        return videos;
    }

    public ArrayList<Video> getPendingTimedUploads() {

        //get all vids w/ upload time before currenttimemillis
        Query query = database.getView("video_by_status").createQuery();
        query.setDescending(true);
        List<Object> keys = new ArrayList<Object>();
        keys.add("PENDING_TIME");
        query.setKeys(keys);

        QueryEnumerator result = null;
        try {
            result = query.run();
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "error querying for logged in user");
            return null;
        }

        ArrayList<Video> videos = new ArrayList<Video>();
        long curTime = System.currentTimeMillis();
        for (Iterator<QueryRow> it = result; it.hasNext(); ) {
            QueryRow row = it.next();
            Document cur = row.getDocument();

            Object reduction = row.getValue();

            if(reduction == null){
                continue;
            }
            long uploadTime = Long.parseLong(reduction.toString());
            if(uploadTime < curTime){
                Video thisVid = this.docToVideo(cur);
                videos.add(thisVid);
                Log.i(TAG, "added video: " + thisVid.get_id() + " to processing list");
            }


        }
        return videos;
    }

    public void delete(Video toUpload) throws CouchbaseLiteException {
        Document toDel = this.findDocById(toUpload.get_id());
        toDel.purge();
    }



    public interface VideoEventListener{
        void onVideosChanged(Map<String, Long> localVideoIds);
    }

    public interface VideoStatusChangeListener{
        void onVideoStatusListChanged();
    }

    private VideoStatusChangeListener statusCallback;
    private Map<String, Long> localVids;
    private final View newestVideoView;
    private final View videoByIdView;
    private LiveQuery newestVideoQuery;
    private final View videoByStatusView;
    private LiveQuery videoStatusQuery;


    public static final String TAG = "VideoRepository";

    private final static ObjectMapper m = new ObjectMapper();
    private final Database database;

    private final Nostalgia app;
    final long MS_PER_HOUR = 1000 * 3600;
    final long MS_PER_DAY = 1000 * 3600 * 24;
    private VideoEventListener callback;

    public VideoRepository(Database db, Nostalgia app) {
        this.database = db;
        newestVideoView = database.getView("newest_video");
        videoByIdView = database.getView("video_by_id");
        videoByStatusView = database.getView("video_by_status");
        if(localVids == null){
            localVids = new HashMap<String, Long>();
        }
        this.app = app;

        newestVideoView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {

                Object type = document.get("type");
                if(type == null) return;
                if(!type.equals("Video")) return;
                String date = document.get("dateCreated").toString();
                if(date != null){
                    emitter.emit(date, null);

                }
            }
        }, "1.0");
        videoByIdView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {

                Object type = document.get("type");
                if(type == null) return;
                if(!type.equals("Video")) return;
                String id = document.get("_id").toString();

                if(id != null){
                    String date = document.get("dateCreated").toString();
                    emitter.emit(id, date);

                }
            }
        }, "1.1");

        try {
            this.startNewestVideoQuery();
        } catch (Exception e) {
            Log.e(TAG, "error starting live query for newest video", e);

        }

        videoByStatusView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {

                Object type = document.get("type");
                if(type == null) return;
                if(!type.equals("Video")) return;
                String id = document.get("_id").toString();


                if(id != null){

                    String status = document.get("status").toString();

                    if(status.contains("PENDING_TIME")) {
                        String dateCreated = document.get("dateCreated").toString();
                        String uploadTime = document.get("properties").toString();

                        if(dateCreated.contains("E")){
                            dateCreated = dateCreated.substring(0, dateCreated.lastIndexOf("E"));
                            dateCreated = dateCreated.replace(".", "");
                        }
                        long createDate = Long.parseLong(dateCreated);

                        if(uploadTime.contains(Video.WHEN_DAY)){
                            //day

                            createDate += MS_PER_DAY;

                        } else if(uploadTime.contains(Video.WHEN_HOUR)){
                            //hour

                            createDate += MS_PER_HOUR;
                        } else if(uploadTime.contains(Video.WHEN_NOW)){
                            //right fucking now, do nothing
                        } else {
                            //wifi

                        }

                        emitter.emit(status, createDate);
                    }  else {
                        emitter.emit(status, null);
                    }

                } else {
                    Log.e(TAG, "NO VIDEO ID FOUND, SKIPPING!");
                }
            }
        }, "1.1");

        try {
            this.startVideoStatusQuery();
        } catch (Exception e) {
            Log.e(TAG, "error starting live query for video status", e);

        }
    }

    public Map<String, Long> getAllVideos() {
        Query query = database.getView("newest_video").createQuery();

        QueryEnumerator result = null;
        try {
            result = query.run();
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "error querying for newest video");
            return null;
        }


        Map<String, Long> all = new HashMap<String, Long>();
        for (Iterator<QueryRow> it = result; it.hasNext(); ) {
            QueryRow me = it.next();
            String time = me.getKey().toString();

            if(time.contains("E")){
                time = time.substring(0, time.indexOf('E'));
            }

            if(time.contains(".")){
                time = time.replace(".", "");
            }
            long timeLong = Long.parseLong(time);
            all.put(me.getDocumentId(), timeLong);

        }
        return all;
    }

    private void startVideoStatusQuery() {
        if (videoStatusQuery == null) {
            videoStatusQuery = videoByStatusView.createQuery().toLiveQuery();
            videoStatusQuery.addChangeListener(new LiveQuery.ChangeListener() {
                public void changed(final LiveQuery.ChangeEvent event) {
                    if(statusCallback != null){
                        statusCallback.onVideoStatusListChanged();
                    }
                }
            });

            videoStatusQuery.start();

        }
    }


    private void startNewestVideoQuery() {
        if (newestVideoQuery == null) {
            newestVideoQuery = newestVideoView.createQuery().toLiveQuery();
            newestVideoQuery.addChangeListener(new LiveQuery.ChangeListener() {
                public void changed(final LiveQuery.ChangeEvent event) {
                    localVids.clear();
                    Map<String, Long> updated = getAllVideos();
                    localVids.putAll(updated);

                    if (callback != null) {
                        callback.onVideosChanged(updated);
                    }
                }
            });
            newestVideoQuery.start();

        }
    }

    public synchronized void save(Video vid) throws Exception {

        Map<String, Object> props = m.convertValue(vid, Map.class);
        String id = (String) props.get("_id");

        Document document;
        if (id == null) {
            throw new Exception("non null id required");
        } else {
            document = database.getExistingDocument(id);
            if (document == null) {
                document = database.getDocument(id);
            } else {
                props.put("_rev", document.getProperty("_rev"));
            }
        }

        try {
            document.putProperties(props);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private Video docToVideo(Document document) {

        return m.convertValue(document.getProperties(), Video.class);
    }


}



