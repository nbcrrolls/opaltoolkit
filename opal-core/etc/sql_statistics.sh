#!/bin/bash
# This script print in output in a csv format which can be imported in a spreadsheet
#Change the DBCOMMAND variable accordingly to your path

DBCOMMAND="/export/portal/pgsql-8.2.6/bin/psql -U opal_user opal_db "

#building the query
QUERY="SELECT to_date(start_time, 'Mon DD, YYYY')"
for serviceName in `echo select service_name from job_status group by service_name| $DBCOMMAND -t |tr '\n' '\t'`
do
    QUERY="$QUERY, sum( CASE WHEN service_name = '$serviceName' THEN 1 ELSE 0 END) as $serviceName"
done
QUERY="$QUERY FROM job_status group by to_date(start_time, 'Mon DD, YYYY') order by to_date(start_time, 'Mon DD, YYYY') desc;"



#running the query and outputing the result
echo $QUERY | $DBCOMMAND | tr '|' ','| grep -v "\\-----"


