package cherry.glenda.lan;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RefreshJob extends HttpServlet{
	 @Override
	    public void doPost(HttpServletRequest req, HttpServletResponse res)
	        throws ServletException, IOException
	 {
	    	
	    res.setContentType("text/plain");     	  
	    PrintWriter out = res.getWriter();
	    
	    String jobOpalIDEnv = req.getParameter("jobOpalIDEnv");
	    String url = req.getParameter("url");
	    if(jobOpalIDEnv==null||jobOpalIDEnv.equals("")){}
	    else
	    {
	    	Job jobReceive = GeneralFunction.getJobByJobOpalID(jobOpalIDEnv);
	    	if(jobReceive==null){}
	    	else{
	    	Long jobID = jobReceive.getJobID();
	    	QueryStatusEnvelope queryStatusENV;
	    	try 
	    	{
	    		queryStatusENV = SoapQueryStatus.QueryStatus(jobOpalIDEnv,url);
	    		if(queryStatusENV!=null)
	    		{
	    			PersistenceManager pm = PMF.get().getPersistenceManager();
	    			Job job = pm.getObjectById(Job.class,jobID);
					try {	
							job.setStatusCode(queryStatusENV.getcode());
							job.setMessage(queryStatusENV.getmessage());
    						job.setBeseURL(queryStatusENV.getbaseURL());
	    					if(queryStatusENV.getcode()==1)
	    					{
	    						job.setStatus("STATUS_PENDING");
	    						
	    					}
	    					if(queryStatusENV.getcode()==2)
	    					{
	    					
	    						job.setStatus("STATUS_ACTIVE");
	    						GetStaticsEnvelope gsVe1 = SoapGetJobStatistics.getStatistics(job.getJobOpalID(),url);
	    						if(gsVe1!=null)
	    						{
		    						Date activationTime =gsVe1.getactivationTime();
		    						job.setActiveTime(activationTime);
	    						}
	    					}
	    		    		if(queryStatusENV.getcode()==4)
	    		    		{
	    		    			job.setStatus("STATUS_FAILED");
	    		    			
	    		    		}
			    			if(queryStatusENV.getcode()==8)
			    			{
			    				
			    				job.setStatus("STATUS_DONE");
			    				GetStaticsEnvelope gsVe2 = SoapGetJobStatistics.getStatistics(jobOpalIDEnv,url);
			    				if(gsVe2 !=null)
			    				{
				    				Date completionTime = gsVe2.getcompletionTime();
				    				job.setEndTime(completionTime);
								}
			    			}
			    			pm.makePersistent(job);
						} finally {	pm.close();}
	    		}
	    			
	    }catch (Exception e) {	e.printStackTrace();} 
	} 
	    
	    
	    res.sendRedirect("/opal2/dashboard-jsp/launchJobHaveFile.jsp?jobOpalIDEnv="+jobOpalIDEnv+"&&url="+url); 
	    }
	 }
	
}
