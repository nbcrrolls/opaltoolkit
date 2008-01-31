/* query to clean the start_time column  */
UPDATE job_status
SET start_time = to_char( to_timestamp(start_time, 'YYYY/MM/DD HH24:MI:SS') ,
                          'Mon FMDD, YYYY FMHH:MI:SS AM')
WHERE job_id IN 
(   select job_id
    from job_status 
    where start_time ~ '^[0-9][0-9]') ;



/* quiery to clean the last_update column  */
UPDATE job_status
SET last_update = to_char( to_timestamp(last_update, 'YYYY/MM/DD HH24:MI:SS') ,
                          'Mon FMDD, YYYY FMHH:MI:SS AM')
WHERE job_id IN 
(   select job_id
    from job_status 
    where last_update ~ '^[0-9][0-9]') ;


/* query to clean the start_time column with chinese locale */
/* start_time column with pm  */
UPDATE job_status
SET start_time = to_char( to_timestamp(start_time, 'M DD, YYYY HH12:MI:SS')  , 'Mon FMDD, YYYY FMHH:MI:SS') || ' PM'
WHERE job_id IN
(   select job_id
    from job_status
    where start_time ~ '午後' );

/* last_update column with pm   */
UPDATE job_status
SET last_update = to_char( to_timestamp(last_update, 'M DD, YYYY HH12:MI:SS')  , 'Mon FMDD, YYYY FMHH:MI:SS') || ' PM'
WHERE job_id IN
(   select job_id
    from job_status
    where last_update ~ '午後' );

/* start_time column with am meridian */
UPDATE job_status
SET start_time = to_char( to_timestamp(start_time, 'M DD, YYYY HH12:MI:SS')  , 'Mon FMDD, YYYY FMHH:MI:SS') || ' AM'
WHERE job_id IN
(   select job_id
    from job_status
    where start_time ~ '午前' );

/*last_update column with am meridian */
UPDATE job_status
SET last_update = to_char( to_timestamp(last_update, 'M DD, YYYY HH12:MI:SS')  , 'Mon FMDD, YYYY FMHH:MI:SS') || ' AM'
WHERE job_id IN
(   select job_id
    from job_status
    where last_update ~ '午前' );
