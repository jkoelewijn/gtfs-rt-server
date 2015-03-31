package nl.omnitrans.mmri.gtfs_rt_server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;

import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeFullUpdate;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeGuiceBindingTypes.MixedFeed;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
//import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
//import com.google.transit.realtime.GtfsRealtime.TripUpdate;
//import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
//import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;

//@SuppressWarnings("restriction")
@Singleton
public class MixedFeedProducer {

    private final Logger logger = LoggerFactory.getLogger(MixedFeedProducer.class);
    
    private GtfsRealtimeSink mixedFeedSink;

    @Inject
    public void setMixedFeedSink(@MixedFeed GtfsRealtimeSink mixedFeedSink) {
        this.mixedFeedSink = mixedFeedSink;
    }

    @PostConstruct
    public void start() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.submit(new Runnable() {
            public void run() {
                runLoop();
            }
        });
    }

    private void runLoop() {
        while (true) {
            // Create full update
            GtfsRealtimeFullUpdate fullUpdate = new GtfsRealtimeFullUpdate();
            
//            {
//                FeedEntity.Builder entity = FeedEntity.newBuilder();
//                entity.setId("3b");
//                TripUpdate.Builder tripUpdate = TripUpdate.newBuilder();
//                TripDescriptor.Builder tripDescriptor = TripDescriptor.newBuilder();
//                tripDescriptor.setStartDate("20140101");
//                tripDescriptor.setTripId("3b|1");
//                tripUpdate.setTrip(tripDescriptor);
//                StopTimeUpdate.Builder stopTimeUpdate = StopTimeUpdate.newBuilder();
//                stopTimeUpdate.setStopSequence(0);
//                stopTimeUpdate.setStopId("3b1");
//                StopTimeEvent.Builder stopTimeEvent = StopTimeEvent.newBuilder();
//                stopTimeEvent.setDelay(600);
//                stopTimeUpdate.setDeparture(stopTimeEvent);
//                tripUpdate.addStopTimeUpdate(stopTimeUpdate);
//                entity.setTripUpdate(tripUpdate);
//                fullUpdate.addEntity(entity.build());
//            }
            
            logger.info("Starting with empty gtfs-rt dataset.");
            
            // Get all pb files in working directory
            File workingDir = new File(".");
            File[] files = workingDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    boolean accept = name.toLowerCase().endsWith(".pb");
                    return accept;
                }
            });
            
            // Read each of them and combine them in one big dataset
            for (File file : files) {
                FileInputStream is;
                try {
                    is = new FileInputStream(file);
                    FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(is);
                    // Add all entities to full dataset
                    for (FeedEntity entity : feed.getEntityList()) {
                        fullUpdate.addEntity(entity);
                    }
                    logger.info("Added entities of {} to gtfs-rt dataset.", file);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            this.mixedFeedSink.handleFullUpdate(fullUpdate);
            logger.info("Created gtfs-rt full dataset.");
            
            // Sleep for awhile
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
