package xyz.nsgw.tools.nscript;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScriptRunHandler extends Thread {

    private final List<File> queue;

    private final List<Service> services;

    private boolean up;

    public ScriptRunHandler() {
        int threadLimit = Runtime.getRuntime().availableProcessors();
        queue = new ArrayList<>();
        services = new ArrayList<>();
        for(int i = 0; i < threadLimit; i++) {
            services.add(new Service(i));
        }
    }

    public void queue(final File f) {
        queue.add(f);
        synchronized (this) {
            notify();
        }
    }

    @Override
    public void run()  {
        up = true;
        Service availableService;
        while (up) {
            if(queue.isEmpty()) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(queue.isEmpty())
                continue;
            if(!up) break;
            availableService = getFirstUnlockedService();
            if(availableService != null) {
                availableService.startScript(queue.get(0));
                availableService.lock();
                queue.remove(0);
            } else {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private Service getFirstUnlockedService() {
        for (Service service : services) {
            if (!service.isLocked()) {
                return service;
            }
        }
        return null;
    }

    public void endQueue() {
        up = false;
        synchronized (this) {
            notify();
        }
    }

    public void emptyQueue() {
        queue.clear();
    }

    synchronized public void unlock(int n) {
        if(services.stream().noneMatch(Service::isUnlocked)) {
            services.get(n).unlock();
            notify();
        } else {
            services.get(n).unlock();
        }
    }

}
