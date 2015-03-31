package nl.omnitrans.mmri.gtfs_rt_server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeExporterModule;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeGuiceBindingTypes.MixedFeed;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeServlet;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeSource;
import org.onebusaway.guice.jsr250.JSR250Module;
import org.onebusaway.guice.jsr250.LifecycleService;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;

public class GtfsRealtimeProducerDemo {

    private GtfsRealtimeSource mixedFeedSource;

    private LifecycleService lifecycleService;

    @Inject
    public void setMixedFeedProducer(MixedFeedProducer producer) {
        // This is just here to make sure VehiclePositionsProducer gets instantiated.
    }

    @Inject
    public void setMixedFeedSource(
            @MixedFeed GtfsRealtimeSource mixedFeedSource) {
        this.mixedFeedSource = mixedFeedSource;
    }

    @Inject
    public void setLifecycleService(LifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    public void run() {
        Set<Module> modules = new HashSet<Module>();
        GtfsRealtimeExporterModule.addModuleAndDependencies(modules);
        JSR250Module.addModuleAndDependencies(modules);
        Injector injector = Guice.createInjector(modules);
        injector.injectMembers(this);

        GtfsRealtimeServlet servlet = injector.getInstance(GtfsRealtimeServlet.class);
        servlet.setSource(mixedFeedSource);
        try {
            servlet.setUrl(new URL("http://localhost:8088/tripUpdates"));
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        lifecycleService.start();
    }
}
