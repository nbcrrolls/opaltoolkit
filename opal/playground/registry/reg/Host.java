public class Host {
    private long id;
    private String name;
    private String url;
    private String host;
    private int numCpuTotal;
    private int numCpuFree;
    private int numJobsRunning;
    private int numJobsQueued;

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

    public String getUrl() {
	return url;
    }
    public void setUrl(String url) {
	this.url = url;
    }

    public String getHost() {
	return host;
    }
    public void setHost(String host) {
	this.host = host;
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
}