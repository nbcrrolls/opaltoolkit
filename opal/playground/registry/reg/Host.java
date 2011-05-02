public class Host {
    private long id;
    private String name;
    private int numCpuTotal;
    private int numCpuFree;
    private int numJobsRunning;
    private int numJobsQueued;
    private String services;

    public long getId() {
	return id;
    }
    public void setId(long id) {
	this.id = id;
    }

    public String getName() {
	return name;
    }
    public void setName(String name) {
	this.name = name;
    }

    public int getNumCpuTotal() {
	return numCpuTotal;
    }
    
    public void setNumCpuTotal(int numCpuTotal) {
	this.numCpuTotal = numCpuTotal;
    }

    public int getNumCpuFree() {
	return numCpuFree;
    }
    public void setNumCpuFree(int numCpuFree) {
	this.numCpuFree = numCpuFree;
    }

    public int getNumJobsRunning() {
	return numJobsRunning;
    }
    public void setNumJobsRunning(int numJobsRunning) {
	this.numJobsRunning = numJobsRunning;
    }

    public int getNumJobsQueued() {
	return numJobsQueued;
    }
    public void setNumJobsQueued(int numJobsQueued) {
	this.numJobsQueued = numJobsQueued;
    }

    public String getServices() {
	return services;
    }

    public void setServices(String s) {
	this.services = s;
    }
}